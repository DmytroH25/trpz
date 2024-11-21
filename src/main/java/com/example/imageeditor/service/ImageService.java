package com.example.imageeditor.service;

import com.example.imageeditor.memento.ImageMemento;
import com.example.imageeditor.model.Image;
import com.example.imageeditor.model.User;
import com.example.imageeditor.repository.ImageRepository;
import com.example.imageeditor.repository.UpdatedImageRepository;
import com.example.imageeditor.state.ImageState;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import javax.imageio.ImageIO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
@Transactional
public class ImageService {

  private final ImageRepository imageRepository;
  private final UpdatedImageRepository updatedImageRepository;

  @Setter
  private ImageState currentState;
  @Getter
  private byte[] currentImageData; // Поточний стан зображення
  private final Deque<ImageMemento> history = new ArrayDeque<>(); // Історія станів (Undo)
  private final Deque<ImageMemento> redoStack = new ArrayDeque<>(); // Історія для Redo

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
    Image image = new Image();
    image.setContentType(file.getContentType());
    image.setFilename(file.getOriginalFilename());
    image.setImageData(file.getBytes());
    image.setUser(user);

    currentImageData = file.getBytes();
    saveToHistory(currentImageData); // Зберігаємо стан
    redoStack.clear(); // Очищаємо стек Redo
    return imageRepository.save(image);
  }

  public Long applyEffect(Long imageId) throws IOException {
    Image image = getImageById(imageId).orElseThrow(
        () -> new IllegalArgumentException("Image not found"));
    Image clonedImage = image.clone();
    BufferedImage bufferedImage = readImageFromFile(image);
    byte[] modifiedImage = currentState.applyEffect(bufferedImage).getImageData();

    saveToHistory(currentImageData); // Зберігаємо попередній стан
    redoStack.clear(); // Очищаємо стек Redo
    currentImageData = modifiedImage; // Оновлюємо поточний стан
    clonedImage.setImageData(currentImageData);
    image.setImageData(clonedImage.getImageData());
    return image.getId();
  }

  public void undo() {
    if (!history.isEmpty()) {
      redoStack.push(new ImageMemento(currentImageData)); // Зберігаємо для Redo
      ImageMemento previousState = history.pop();
      currentImageData = previousState.getImageData(); // Відновлюємо попередній стан
    }
  }

  public void redo() {
    if (!redoStack.isEmpty()) {
      saveToHistory(currentImageData); // Зберігаємо для Undo
      ImageMemento nextState = redoStack.pop();
      currentImageData = nextState.getImageData(); // Відновлюємо наступний стан
    }
  }

  public BufferedImage readImageFromFile(Image image) throws IOException {
    return ImageIO.read(new ByteArrayInputStream(image.getImageData()));
  }

  private void saveToHistory(byte[] imageData) {
    history.push(new ImageMemento(imageData)); // Зберігаємо стан в історію
  }
}

