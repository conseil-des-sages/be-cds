package com.takima.backskeleton.DAO;

import com.takima.backskeleton.models.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDao extends JpaRepository<User, Integer> {
    boolean existsByEmailUser(@NotBlank String userEmail);

    Optional<User> findByEmailUser(String email);
}
