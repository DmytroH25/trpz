package com.example.imageeditor.state;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface ImageState {
    byte[] applyEffect(BufferedImage image, Object... params) throws IOException;
}