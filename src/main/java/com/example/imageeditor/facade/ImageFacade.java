package com.example.imageeditor.facade;
import com.example.imageeditor.model.Image;
import com.example.imageeditor.model.User;
import com.example.imageeditor.service.ImageService;
import com.example.imageeditor.service.UserService;
import com.example.imageeditor.state.CropState;
import com.example.imageeditor.state.ResizeState;
import com.example.imageeditor.state.RotateState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Component
public class ImageFacade {

  private final ImageService imageService;
  private final UserService userService;

  @Autowired
  public ImageFacade(ImageService imageService, UserService userService) {
    this.imageService = imageService;
    this.userService = userService;
  }

  public Image uploadImage(MultipartFile file, Long userId) throws IOException {
    User user = userService.getUserById(userId);
    if (user == null) {
      throw new IllegalArgumentException("User not found");
    }
    return imageService.saveImage(file, user);
  }

  public byte[] rotateImage(Long imageId, double degree) throws IOException {
    imageService.setCurrentState(new RotateState(degree));
    Long editedImageId = imageService.applyEffect(imageId);
    return getImageDataById(editedImageId);
  }

  public byte[] resizeImage(Long imageId, int width, int height) throws IOException {
    imageService.setCurrentState(new ResizeState(width, height));
    Long editedImageId = imageService.applyEffect(imageId);
    return getImageDataById(editedImageId);
  }

  public byte[] cropImage(Long imageId, int x, int y, int width, int height) throws IOException {
    imageService.setCurrentState(new CropState(x, y, width, height));
    Long editedImageId = imageService.applyEffect(imageId);
    return getImageDataById(editedImageId);
  }

  public byte[] undo() {
    imageService.undo();
    return imageService.getCurrentImageData();
  }

  public byte[] redo() {
    imageService.redo();
    return imageService.getCurrentImageData();
  }

  public byte[] downloadImage(Long id) {
    return getImageDataById(id);
  }

  private byte[] getImageDataById(Long id) {
    Optional<Image> imageOpt = imageService.getImageById(id);
    if (imageOpt.isPresent()) {
      return imageOpt.get().getImageData();
    } else {
      throw new IllegalArgumentException("Image not found");
    }
  }
}