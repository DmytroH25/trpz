package com.example.imageeditor.composite;


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

      BufferedImage combinedImage = null;
      for (ImageComponent component : imageComponents) {
        BufferedImage image = toBufferedImage(component.getImageData());
        if (combinedImage == null) {
          combinedImage = new BufferedImage(image.getWidth(), image.getHeight(),
              BufferedImage.TYPE_INT_ARGB);
        }
        combinedImage = overlayImages(combinedImage, image);
      }

      return toByteArray(combinedImage);
    } catch (IOException e) {
      throw new RuntimeException("Failed to overlay images", e);
    }
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

  private BufferedImage overlayImages(BufferedImage baseImage, BufferedImage overlayImage) {
    int width = Math.max(baseImage.getWidth(), overlayImage.getWidth());
    int height = Math.max(baseImage.getHeight(), overlayImage.getHeight());

    BufferedImage combinedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    combinedImage.getGraphics().drawImage(baseImage, 0, 0, null);
    combinedImage.getGraphics().drawImage(overlayImage, 0, 0, null);

    return combinedImage;
  }
}