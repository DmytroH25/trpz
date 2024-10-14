package com.example.imageeditor.service;
import com.example.imageeditor.Repository.ImageRepository;
import com.example.imageeditor.Repository.UpdatedImageRepository;
import com.example.imageeditor.model.Image;
import com.example.imageeditor.model.UpdatedImage;
import com.example.imageeditor.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ImageService {

    private final ImageRepository imageRepository;
    private final UpdatedImageRepository updatedImageRepository;

    @Autowired
    public ImageService(ImageRepository imageRepository, UpdatedImageRepository updatedImageRepository) {
        this.imageRepository = imageRepository;
        this.updatedImageRepository = updatedImageRepository;
    }

    // Збереження оригінального зображення
    public Image saveImage(MultipartFile file, User user) throws IOException {
        Image image = new Image();
        image.setContentType(file.getContentType());
        image.setFilename(file.getOriginalFilename());
        image.setImageData(file.getBytes());
        image.setUser(user);
        return imageRepository.save(image);
    }


    public Image getImageById(Long id) {
        Optional<Image> optionalImage = imageRepository.findById(id);
        return optionalImage.orElse(null);
    }

    public UpdatedImage saveUpdatedImage(Image originalImage, byte[] updatedImageData, String filename, String contentType) {
        UpdatedImage updatedImage = UpdatedImage.builder()
                .originalImage(originalImage)
                .imageData(updatedImageData)
                .filename(filename)
                .contentType(contentType)
                .build();
        return updatedImageRepository.save(updatedImage);
    }


    @Transactional
    public Image updateImage(Long id, MultipartFile file, String filename, String contentType) throws IOException {
        Image existingImage = getImageById(id);
        if (existingImage != null) {
            existingImage.setImageData(file.getBytes());
            existingImage.setFilename(filename);
            existingImage.setContentType(contentType);
            return imageRepository.save(existingImage);
        }
        return null;
    }

    public void deleteImage(Long id) {
        imageRepository.deleteById(id);
    }

    public List<Image> getAllImages() {
        return imageRepository.findAll();
    }

    // Методи для маніпуляцій зображеннями

    public byte[] rotateImage(BufferedImage originalImage, int degree) throws IOException {
        Scalr.Rotation rotation = getRotationByDegree(degree);
        BufferedImage rotatedImage = Scalr.rotate(originalImage, rotation);
        return convertImageToByteArray(rotatedImage);
    }

//    public byte[] resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException {
//        BufferedImage resizedImage = Scalr.resize(originalImage, targetWidth, targetHeight);
//        return convertImageToByteArray(resizedImage);
//    }
//
//    public byte[] cropImage(BufferedImage originalImage, int x, int y, int width, int height) throws IOException {
//        BufferedImage croppedImage = originalImage.getSubimage(x, y, width, height);
//        return convertImageToByteArray(croppedImage);
//    }

    public byte[] resizeImage(MultipartFile file, int width, int height) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));

        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpg", baos);
        return baos.toByteArray();
    }
    public byte[] cropImage(MultipartFile file, int x, int y, int width, int height) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));

        // Перевірка меж обрізки
        if (x < 0 || y < 0 || x + width > originalImage.getWidth() || y + height > originalImage.getHeight()) {
            throw new IllegalArgumentException("Некоректні координати обрізки.");
        }

        // Обрізка зображення
        BufferedImage croppedImage = originalImage.getSubimage(x, y, width, height);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(croppedImage, "jpg", baos);
        return baos.toByteArray();
    }


        // Допоміжні методи
    private Scalr.Rotation getRotationByDegree(int degree) {
        switch (degree) {
            case 90:
                return Scalr.Rotation.CW_90;
            case 180:
                return Scalr.Rotation.CW_180;
            case 270:
                return Scalr.Rotation.CW_270;
            default:
                throw new IllegalArgumentException("Invalid degree value: " + degree);
        }
    }

    private byte[] convertImageToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }

    public BufferedImage convertByteArrayToBufferedImage(byte[] imageData) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        return ImageIO.read(bais);
    }
}
