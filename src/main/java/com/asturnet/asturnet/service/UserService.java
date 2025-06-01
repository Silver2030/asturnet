package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.User; 
import com.asturnet.asturnet.dto.UserProfileUpdateRequest; 

import java.util.List;
import java.util.Optional; 

public interface UserService {
    User registerNewUser(String username, String email, String password);
    User findByUsername(String username); 
    User findByEmail(String email);

    Optional<User> findById(Long id); 
    User updateUser(User user);       

    User updateProfile(Long userId, UserProfileUpdateRequest request);

    List<User> searchUsers(String query);

    void banUser(Long userId);
    void unbanUser(Long userId);
}