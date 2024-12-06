package com.example.imageeditor.service;

import com.example.imageeditor.composite.ImageComposite;
import com.example.imageeditor.composite.ImageLeaf;
import com.example.imageeditor.memento.ImageMemento;
import com.example.imageeditor.model.Image;
import com.example.imageeditor.model.User;
import com.example.imageeditor.repository.ImageRepository;
import com.example.imageeditor.repository.UpdatedImageRepository;
import com.example.imageeditor.service.exception.ImageNotFoundException;
import com.example.imageeditor.state.ImageState;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
public class ImageService {


  private final ImageRepository imageRepository;
  private final UpdatedImageRepository updatedImageRepository;

  @Setter
  private ImageState currentState;
  @Getter
  private byte[] currentImageData;
  private final Deque<ImageMemento> history = new ArrayDeque<>();
  private final Deque<ImageMemento> redoStack = new ArrayDeque<>();

  @Autowired
  public ImageService(ImageRepository imageRepository,
      UpdatedImageRepository updatedImageRepository) {
    this.imageRepository = imageRepository;
    this.updatedImageRepository = updatedImageRepository;
  }

  public Optional<Image> getImageById(Long id) {
    return imageRepository.findById(id);
  }

  public Image saveImage(MultipartFile file, User user) throws IOException {
    if (file.isEmpty() || user == null) {
      throw new IllegalArgumentException("File or user cannot be null");
    }
    Image image = new Image();
    image.setContentType(file.getContentType());
    image.setFilename(file.getOriginalFilename());
    image.setImageData(file.getBytes());
    image.setUser(user);

    currentImageData = file.getBytes();
    saveToHistory(currentImageData);
    redoStack.clear();

    log.info("Saving image for user: {}", user.getId());
    return imageRepository.save(image);
  }

  public Long applyEffect(Long imageId) throws IOException {
    Image image = getImageById(imageId).orElseThrow(
        () -> new ImageNotFoundException("Image not found"));
    Image clonedImage = image.clone();
    BufferedImage bufferedImage = readImageFromFile(image);
    byte[] modifiedImage = currentState.applyEffect(bufferedImage).getImageData();

    saveToHistory(currentImageData);
    redoStack.clear();
    currentImageData = modifiedImage;
    clonedImage.setImageData(currentImageData);
    image.setImageData(clonedImage.getImageData());

    log.info("Applying effect to image: {}", imageId);
    return image.getId();
  }

  public void undo() {
    if (!history.isEmpty()) {
      redoStack.push(new ImageMemento(currentImageData));
      ImageMemento previousState = history.pop();
      currentImageData = previousState.getImageData();

      log.info("Undoing last action");
    }
  }

  public void redo() {
    if (!redoStack.isEmpty()) {
      saveToHistory(currentImageData);
      ImageMemento nextState = redoStack.pop();
      currentImageData = nextState.getImageData();

      log.info("Redoing last undone action");
    }
  }

  public BufferedImage readImageFromFile(Image image) throws IOException {
    return ImageIO.read(new ByteArrayInputStream(image.getImageData()));
  }

  private void saveToHistory(byte[] imageData) {
    history.push(new ImageMemento(imageData));
  }
  public byte[] createCollage(List<Image> images) {
    ImageComposite collage = new ImageComposite();
    for (Image image : images) {
      collage.add(new ImageLeaf(image));
    }
    return collage.getImageData();
  }
}