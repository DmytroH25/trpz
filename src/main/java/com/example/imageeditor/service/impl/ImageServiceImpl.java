package com.example.imageeditor.service.impl;

import com.example.imageeditor.memento.ImageMemento;
import com.example.imageeditor.repository.ImageRepository;
import com.example.imageeditor.repository.UserRepository;
import com.example.imageeditor.repository.entity.Image;
import com.example.imageeditor.repository.entity.User;
import com.example.imageeditor.service.ImageService;
import com.example.imageeditor.service.exception.ImageNotFoundException;
import com.example.imageeditor.state.ImageState;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

  private final ImageRepository imageRepository;
  private final UserRepository userRepository;
  private final Map<Long, Deque<ImageMemento>> usersHistory = new HashMap<>();
  private final Map<Long, Deque<ImageMemento>> usersRedoStack = new HashMap<>();

  @Override
  public Image getImageById(Long id) {
    return imageRepository.findById(id)
        .orElseThrow(() -> new ImageNotFoundException(id));
  }

  @Override
  public Image saveImage(MultipartFile file, User user) throws IOException {
    Deque<ImageMemento> redoStack = usersRedoStack.computeIfAbsent(user.getId(),
        k -> new ArrayDeque<>());
    if (file.isEmpty()) {
      throw new IllegalArgumentException("File cannot be null");
    }
    if (user.getImage() != null) {
      Image image = user.getImage();
      image.setContentType(file.getContentType());
      image.setFilename(file.getOriginalFilename());
      image.setImageData(file.getBytes());
    } else {
      Image image = Image.builder()
          .user(user)
          .contentType(file.getContentType())
          .filename(file.getOriginalFilename())
          .imageData(file.getBytes())
          .build();
      user.setImage(image);
    }
    saveToHistory(file.getBytes(), user);
    redoStack.clear();
    log.info("Saving image for user: {}", user.getId());
    return userRepository.save(user).getImage();
  }

  @Override
  public Image saveImage(Image image) {
    image.setImageData(getCurrentImage(image.getUser()));
    return imageRepository.save(image);
  }

  @Override
  public void applyEffect(Image image, ImageState imageState) throws IOException {
    Deque<ImageMemento> redoStack = usersRedoStack.computeIfAbsent(image.getUser().getId(),
        k -> new ArrayDeque<>());
    Image clonedImage = image.clone();
    clonedImage.setImageData(getCurrentImage(image.getUser()));
    BufferedImage bufferedImage = readImageFromFile(clonedImage);
    byte[] modifiedImage = imageState.applyEffect(bufferedImage).getImageData();
    saveToHistory(modifiedImage, image.getUser());
    redoStack.clear();
    clonedImage.setImageData(modifiedImage);
    image.setImageData(clonedImage.getImageData());
    log.info("Applying effect to image: {}", image.getFilename());
  }


  @Override
  public byte[] createCollageFromData(List<byte[]> imageDatas, User user) throws IOException {
    if (imageDatas.isEmpty()) {
      throw new IllegalArgumentException("No images provided");
    }

    BufferedImage baseImage = ImageIO.read(new ByteArrayInputStream(imageDatas.get(0)));
    Graphics2D g2d = baseImage.createGraphics();

    for (int i = 1; i < imageDatas.size(); i++) {
      BufferedImage overlayImage = ImageIO.read(new ByteArrayInputStream(imageDatas.get(i)));
      int newHeight = baseImage.getHeight() / 3;
      int newWidth = (overlayImage.getWidth() * newHeight) / overlayImage.getHeight();
      BufferedImage resizedOverlayImage = new BufferedImage(newWidth, newHeight, overlayImage.getType());
      Graphics2D g2dOverlay = resizedOverlayImage.createGraphics();
      g2dOverlay.drawImage(overlayImage, 0, 0, newWidth, newHeight, null);
      g2dOverlay.dispose();
      int x = (i % 2 == 0) ? baseImage.getWidth() - newWidth : 0;
      int y = (i < 3) ? 0 : baseImage.getHeight() - newHeight;
      g2d.drawImage(resizedOverlayImage, x, y, null);
    }

    g2d.dispose();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(baseImage, "png", baos);
    byte[] collageData = baos.toByteArray();
    saveToHistory(collageData, user);
    return collageData;
  }

  @Override
  public void undo(User user) {
    Deque<ImageMemento> history = usersHistory.computeIfAbsent(user.getId(), k ->
        new ArrayDeque<>(Collections.singleton(new ImageMemento(user.getImage().getImageData()))));
    Deque<ImageMemento> redoStack = usersRedoStack.computeIfAbsent(user.getId(),
        k -> new ArrayDeque<>());
    if (!history.isEmpty()) {
      redoStack.push(history.pop());
      log.info("Undoing last action");
    }
  }

  @Override
  public void redo(User user) {
    Deque<ImageMemento> history = usersHistory.computeIfAbsent(user.getId(), k ->
        new ArrayDeque<>(Collections.singleton(new ImageMemento(user.getImage().getImageData()))));
    Deque<ImageMemento> redoStack = usersRedoStack.computeIfAbsent(user.getId(),
        k -> new ArrayDeque<>());
    if (!redoStack.isEmpty()) {
      history.push(redoStack.pop());
      log.info("Redoing last undone action");
    }
  }

  @Override
  public byte[] getCurrentImage(User user) {
    Deque<ImageMemento> history = usersHistory.computeIfAbsent(user.getId(), k ->
        new ArrayDeque<>(Collections.singleton(new ImageMemento(user.getImage().getImageData()))));
    if (history.isEmpty()) {
      return new byte[0];
    }
    return history.getFirst().imageData();
  }

  private BufferedImage readImageFromFile(Image image) throws IOException {
    return ImageIO.read(new ByteArrayInputStream(image.getImageData()));
  }

  private void saveToHistory(byte[] imageData, User user) {
    Deque<ImageMemento> history = usersHistory.computeIfAbsent(user.getId(), k ->
        new ArrayDeque<>(Collections.singleton(new ImageMemento(user.getImage().getImageData()))));
    history.push(new ImageMemento(imageData));
  }
}