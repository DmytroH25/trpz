package com.example.imageeditor.state;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RotateState implements ImageState {
    private final double angle;

    public RotateState(double angle) {
        this.angle = angle;
    }

    @Override
    public byte[] applyEffect(BufferedImage image, Object... params) throws IOException {
        double radians = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        int width = image.getWidth();
        int height = image.getHeight();
        int newWidth = (int) Math.floor(width * cos + height * sin);
        int newHeight = (int) Math.floor(height * cos + width * sin);

        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D g2d = rotatedImage.createGraphics();

        AffineTransform at = new AffineTransform();
        at.translate((newWidth - width) / 2, (newHeight - height) / 2);
        at.rotate(radians, width / 2, height / 2);

        g2d.setTransform(at);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return convertImageToByteArray(rotatedImage);
    }

    private byte[] convertImageToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }
}