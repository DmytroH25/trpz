package com.example.imageeditor.memento;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ImageMemento {

  private final byte[] imageData;

  public byte[] getImageData() {
    return imageData.clone(); // Повертаємо копію, щоб зберегти незмінність
  }
}

