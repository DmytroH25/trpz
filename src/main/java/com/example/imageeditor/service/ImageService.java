package com.example.imageeditor.service;
import com.example.imageeditor.repository.ImageRepository;
import com.example.imageeditor.repository.UpdatedImageRepository;
import com.example.imageeditor.model.Image;
import com.example.imageeditor.model.User;
import com.example.imageeditor.state.ImageState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.Optional;


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
    private ImageState currentState;

    @Autowired
    public ImageService(ImageRepository imageRepository, UpdatedImageRepository updatedImageRepository) {
        this.imageRepository = imageRepository;
        this.updatedImageRepository = updatedImageRepository;
    }
    public Optional<Image> getImageById(Long id) {
        return imageRepository.findById(id);
    }
    public Image saveImage(MultipartFile file, User user) throws IOException {
        Image image = new Image();
        image.setContentType(file.getContentType());
        image.setFilename(file.getOriginalFilename());
        image.setImageData(file.getBytes());
        image.setUser(user);
        return imageRepository.save(image);
    }

    public BufferedImage readImageFromFile(Image image) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(image.getImageData()));
    }

    public void setCurrentState(ImageState state) {
        this.currentState = state;
    }

    public byte[] applyEffect(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream()); // Читання зображення з файлу
        return currentState.applyEffect(image);
    }

    // Інші методи залишаються без змін
}
