package com.example.imageeditor.controller;

import com.example.imageeditor.model.User;
import com.example.imageeditor.service.ImageService;
import com.example.imageeditor.service.UserService;
import com.example.imageeditor.state.CropState;
import com.example.imageeditor.state.ResizeState;
import com.example.imageeditor.state.RotateState;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;


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
            return new ResponseEntity<>("Image uploaded successfully.", HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>("Failed to upload image.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/rotate")
    public ResponseEntity<byte[]> rotateImage(@RequestParam("file") MultipartFile file, @RequestParam("degree") double degree) {
        try {
            // Встановлюємо стан для повороту зображення
            imageService.setCurrentState(new RotateState(degree));
            byte[] rotatedImage = imageService.applyEffect(file);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(rotatedImage);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/resize")
    public ResponseEntity<byte[]> resizeImage(@RequestParam("file") MultipartFile file,
                                              @RequestParam("width") int width,
                                              @RequestParam("height") int height) {
        try {
            // Встановлюємо стан для зміни розміру зображення
            imageService.setCurrentState(new ResizeState(width, height));
            byte[] resizedImageBytes = imageService.applyEffect(file);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resizedImageBytes);
        } catch (IOException e) {
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
            // Встановлюємо стан для обрізки зображення
            imageService.setCurrentState(new CropState(x, y, width, height));
            byte[] croppedImageBytes = imageService.applyEffect(file);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(croppedImageBytes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadImage(@PathVariable Long id) {
        return imageService.getImageById(id)
                .map(image -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.parseMediaType(image.getContentType()));
                    headers.setContentDispositionFormData("attachment", image.getFilename());
                    return new ResponseEntity<>(image.getImageData(), headers, HttpStatus.OK);
                })
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }
}

