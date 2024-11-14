package com.example.imageeditor.model;

import com.example.imageeditor.prototype.CloneableImage;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "image")
public class Image implements CloneableImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Lob
    @Column(columnDefinition = "BYTEA", name = "image_data")
    private byte[] imageData;

    @Column(name = "filename")
    private String filename;

    @Column(name = "content_type")
    private String contentType;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Override
    public Image clone() {
        try {
            Image clonedImage = new Image();
            clonedImage.setId(this.id);
            clonedImage.setImageData(this.imageData != null ? this.imageData.clone() : null);
            clonedImage.setFilename(this.filename);
            clonedImage.setContentType(this.contentType);
            clonedImage.setUser(this.user);
            return clonedImage;
        } catch (Exception e) {
            throw new RuntimeException("Failed to clone Image object.", e);
        }
    }
}
