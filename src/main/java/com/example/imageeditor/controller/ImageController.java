package com.example.imageeditor.controller;

import com.example.imageeditor.facade.ImageFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageFacade imageFacade;

    @Autowired
    public ImageController(ImageFacade imageFacade) {
        this.imageFacade = imageFacade;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("userId") Long userId) {
        try {
            byte[] imageData = imageFacade.uploadImage(file, userId).getImageData();
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(imageData);
        } catch (IOException e) {
            return new ResponseEntity<>("Failed to upload image.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/rotate")
    public ResponseEntity<byte[]> rotateImage(@RequestParam("imageId") Long imageId, @RequestParam("degree") double degree) {
        try {
            byte[] imageData = imageFacade.rotateImage(imageId, degree);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(imageData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/resize")
    public ResponseEntity<byte[]> resizeImage(@RequestParam("imageId") Long imageId,
        @RequestParam("width") int width,
        @RequestParam("height") int height) {
        try {
            byte[] imageData = imageFacade.resizeImage(imageId, width, height);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(imageData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/crop")
    public ResponseEntity<byte[]> cropImage(@RequestParam("imageId") Long imageId,
        @RequestParam("x") int x,
        @RequestParam("y") int y,
        @RequestParam("width") int width,
        @RequestParam("height") int height) {
        try {
            byte[] imageData = imageFacade.cropImage(imageId, x, y, width, height);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(imageData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/undo")
    public ResponseEntity<byte[]> undo() {
        byte[] imageData = imageFacade.undo();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
            .body(imageData);
    }

    @PostMapping("/redo")
    public ResponseEntity<byte[]> redo() {
        byte[] imageData = imageFacade.redo();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
            .body(imageData);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadImage(@PathVariable Long id) {
        try {
            byte[] imageData = imageFacade.downloadImage(id);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(imageData);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}