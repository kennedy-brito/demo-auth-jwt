package com.kennedy.demo_auth_jwt.repositories;

import com.kennedy.demo_auth_jwt.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
