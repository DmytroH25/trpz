package com.example.imageeditor.Repository;

import com.example.imageeditor.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
