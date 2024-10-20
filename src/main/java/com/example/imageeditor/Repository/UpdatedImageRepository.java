package com.example.imageeditor.Repository;

import com.example.imageeditor.model.UpdatedImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UpdatedImageRepository extends JpaRepository<UpdatedImage, Long> {
}
