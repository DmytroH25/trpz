package com.example.imageeditor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "updated_image")
public class UpdatedImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "original_image_id", nullable = false)
    private Image originalImage;

    @Lob
    @Column(columnDefinition = "BYTEA", name = "updated_image_data")
    private byte[] imageData;

    @Column(name = "filename")
    private String filename;

    @Column(name = "content_type")
    private String contentType;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

