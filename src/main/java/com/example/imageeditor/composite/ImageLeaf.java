package com.example.imageeditor.composite;

public class ImageLeaf implements ImageComponent {

  private final byte[] imageData;

  public ImageLeaf(byte[] imageData) {
    this.imageData = imageData;
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
    return imageData;
  }
}