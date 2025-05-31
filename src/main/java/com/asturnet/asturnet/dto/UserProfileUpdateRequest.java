package com.asturnet.asturnet.dto;

import lombok.Data;
import com.asturnet.asturnet.model.PrivacyLevel; // Importa tu Enum PrivacyLevel

@Data // Genera getters y setters
public class UserProfileUpdateRequest {
    private String bio;
    private String profilePictureUrl; // Coincide con el nombre del campo en User.java
    private String fullName;      // Coincide con el nombre del campo en User.java
    private Boolean isPrivate;
}