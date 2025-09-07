package com.abhiruchi.csvanalyzer.repository;

import com.abhiruchi.csvanalyzer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}