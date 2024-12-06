package com.example.imageeditor.composite;


import com.example.imageeditor.model.Image;

public class ImageLeaf implements ImageComponent {

  private final Image image;

  public ImageLeaf(Image image) {
    this.image = image;
  }

  @Override
  public void add(ImageComponent imageComponent) {
    throw new UnsupportedOperationException("Cannot add to a leaf");
  }

  @Override
  public void remove(ImageComponent imageComponent) {
    throw new UnsupportedOperationException("Cannot remove from a leaf");
  }

  @Override
  public ImageComponent getChild(int i) {
    throw new UnsupportedOperationException("Cannot get child from a leaf");
  }

  @Override
  public byte[] getImageData() {
    return image.getImageData();
  }
}