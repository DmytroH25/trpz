package com.example.imageeditor.composite;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class ImageComposite implements ImageComponent {

  private final List<ImageComponent> imageComponents = new ArrayList<>();

  @Override
  public void add(ImageComponent imageComponent) {
    imageComponents.add(imageComponent);
  }

  @Override
  public void remove(ImageComponent imageComponent) {
    imageComponents.remove(imageComponent);
  }

  @Override
  public ImageComponent getChild(int i) {
    return imageComponents.get(i);
  }

  @Override
  public byte[] getImageData() {
    try {
      if (imageComponents.isEmpty()) {
        return new byte[0];
      }

      BufferedImage combinedImage = createCollage();
      return toByteArray(combinedImage);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create collage", e);
    }
  }

  private BufferedImage createCollage() throws IOException {
    int collageWidth = 0;
    int collageHeight = 0;
    List<BufferedImage> bufferedImages = new ArrayList<>();

    for (ImageComponent component : imageComponents) {
      BufferedImage image = toBufferedImage(component.getImageData());
      bufferedImages.add(image);
      collageWidth = Math.max(collageWidth, image.getWidth());
      collageHeight = Math.max(collageHeight, image.getHeight());
    }

    BufferedImage collage = new BufferedImage(collageWidth, collageHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = collage.createGraphics();

    if (!bufferedImages.isEmpty()) {
      BufferedImage firstImage = bufferedImages.get(0);
      g2d.drawImage(firstImage, 0, 0, collageWidth, collageHeight, null);
    }

    for (int i = 1; i < bufferedImages.size(); i++) {
      BufferedImage image = bufferedImages.get(i);

      int newHeight = collageHeight / 3;
      int newWidth = (image.getWidth() * newHeight) / image.getHeight();

      BufferedImage resizedOverlayImage = new BufferedImage(newWidth, newHeight, image.getType());
      Graphics2D g2dOverlay = resizedOverlayImage.createGraphics();
      g2dOverlay.drawImage(image, 0, 0, newWidth, newHeight, null);
      g2dOverlay.dispose();

      int x = (i % 2 == 0) ? collageWidth - newWidth : 0;
      int y = (i < 3) ? 0 : collageHeight - newHeight;

      g2d.drawImage(resizedOverlayImage, x, y, null);
    }

    g2d.dispose();
    return collage;
  }

  private BufferedImage toBufferedImage(byte[] imageData) throws IOException {
    ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
    return ImageIO.read(bis);
  }

  private byte[] toByteArray(BufferedImage image) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ImageIO.write(image, "png", bos);
    return bos.toByteArray();
  }
}