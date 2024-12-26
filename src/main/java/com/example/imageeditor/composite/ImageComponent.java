package com.example.imageeditor.composite;

public interface ImageComponent {

  void add(ImageComponent imageComponent);

  void remove(ImageComponent imageComponent);

  ImageComponent getChild(int i);

  byte[] getImageData();
}
