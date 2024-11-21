package com.example.imageeditor.controller;

import com.example.imageeditor.model.Image;
import com.example.imageeditor.model.User;
import com.example.imageeditor.service.ImageService;
import com.example.imageeditor.service.UserService;
import com.example.imageeditor.state.CropState;
import com.example.imageeditor.state.ResizeState;
import com.example.imageeditor.state.RotateState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

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
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("userId") Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return new ResponseEntity<>("User not found.", HttpStatus.NOT_FOUND);
            }
            // Завантажуємо зображення і зберігаємо в базі
            Image savedImage = imageService.saveImage(file, user);

            // Повертаємо байтові дані зображення
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(savedImage.getImageData());
        } catch (IOException e) {
            return new ResponseEntity<>("Failed to upload image.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/rotate")
    public ResponseEntity<byte[]> rotateImage(@RequestParam("imageId") Long imageId, @RequestParam("degree") double degree) {
        try {
            // Встановлюємо поточний стан для ефекту
            imageService.setCurrentState(new RotateState(degree));

            // Отримуємо ID зміненої картинки
            Long editedImageId = imageService.applyEffect(imageId);

            // Отримуємо зображення за ID
            Optional<Image> editedImageOpt = imageService.getImageById(editedImageId);

            if (editedImageOpt.isPresent()) {
                Image editedImage = editedImageOpt.get();
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")  // Вказуємо тип контенту
                        .body(editedImage.getImageData());  // Відправляємо зображення в тілі відповіді
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);  // Якщо зображення не знайдено
            }

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();  // Помилка
        }
    }

    @PostMapping("/resize")
    public ResponseEntity<byte[]> resizeImage(@RequestParam("imageId") Long imageId,
                                              @RequestParam("width") int width,
                                              @RequestParam("height") int height) {
        try {
            imageService.setCurrentState(new ResizeState(width, height));

            // Отримуємо ID зміненої картинки
            Long editedImageId = imageService.applyEffect(imageId);

            // Отримуємо зображення за ID
            Optional<Image> editedImageOpt = imageService.getImageById(editedImageId);

            if (editedImageOpt.isPresent()) {
                Image editedImage = editedImageOpt.get();
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                        .body(editedImage.getImageData());
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

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
            imageService.setCurrentState(new CropState(x, y, width, height));

            // Отримуємо ID зміненої картинки
            Long editedImageId = imageService.applyEffect(imageId);

            // Отримуємо зображення за ID
            Optional<Image> editedImageOpt = imageService.getImageById(editedImageId);

            if (editedImageOpt.isPresent()) {
                Image editedImage = editedImageOpt.get();
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                        .body(editedImage.getImageData());
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PostMapping("/undo")
    public ResponseEntity<byte[]> undo() {
        imageService.undo();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
            .body(imageService.getCurrentImageData());
    }

    @PostMapping("/redo")
    public ResponseEntity<byte[]> redo() {
        imageService.redo();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
            .body(imageService.getCurrentImageData());
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

