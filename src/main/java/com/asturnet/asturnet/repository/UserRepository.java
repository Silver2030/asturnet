package com.asturnet.asturnet.repository;

import com.asturnet.asturnet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
