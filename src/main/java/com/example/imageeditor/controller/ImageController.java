package com.example.imageeditor.controller;

import com.example.imageeditor.model.Image;
import com.example.imageeditor.model.User;
import com.example.imageeditor.service.ImageService;
import com.example.imageeditor.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;




@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;
    private final UserService userService;

    @Autowired
    public ImageController(ImageService imageService, UserService userService) {
        this.imageService = imageService;
        this.userService = userService;
    }


    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("userId") Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return new ResponseEntity<>("User not found.", HttpStatus.NOT_FOUND);
            }
            imageService.saveImage(file, user);
            return new ResponseEntity<>("Image uploaded successfully.", HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("Failed to upload image.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/rotate")
    public ResponseEntity<byte[]> rotateImage(@RequestParam("file") MultipartFile file,
                                              @RequestParam("degree") int degree) {
        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
            byte[] rotatedImage = imageService.rotateImage(originalImage, degree);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); // або інший формат зображення
            return new ResponseEntity<>(rotatedImage, headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/resize")
    public ResponseEntity<byte[]> resizeImage(@RequestParam("file") MultipartFile file,
                                              @RequestParam("width") int width,
                                              @RequestParam("height") int height) {
        try {
            byte[] resizedImageBytes = imageService.resizeImage(file, width, height);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resizedImageBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/crop")
    public ResponseEntity<byte[]> cropImage(@RequestParam("file") MultipartFile file,
                                            @RequestParam("x") int x,
                                            @RequestParam("y") int y,
                                            @RequestParam("width") int width,
                                            @RequestParam("height") int height) {
        try {
            byte[] croppedImageBytes = imageService.cropImage(file, x, y, width, height);  // Виклик методу з сервісу
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(croppedImageBytes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);  // Якщо координати обрізки некоректні
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadImage(@PathVariable Long id) {
        Image image = imageService.getImageById(id);
        if (image != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(image.getContentType()));
            headers.setContentDispositionFormData("attachment", image.getFilename());

            return new ResponseEntity<>(image.getImageData(), headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}
