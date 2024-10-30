package com.example.imageeditor.repository;

import com.example.imageeditor.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}