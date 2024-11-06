package com.example.imageeditor.state;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CropState implements ImageState {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public CropState(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public byte[] applyEffect(BufferedImage image, Object... params) throws IOException {
        if (x < 0 || y < 0 || x + width > image.getWidth() || y + height > image.getHeight()) {
            throw new IllegalArgumentException("Invalid crop coordinates.");
        }

        BufferedImage croppedImage = image.getSubimage(x, y, width, height);
        return convertImageToByteArray(croppedImage);
    }

    private byte[] convertImageToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }
}
