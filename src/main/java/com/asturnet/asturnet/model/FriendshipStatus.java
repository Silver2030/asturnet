package com.asturnet.asturnet.model;

public enum FriendshipStatus {
    NOT_FRIENDS, // Añade esta línea: Indica que no hay una relación de amistad
    PENDING,     // Solicitud enviada y esperando aceptación
    ACCEPTED,    // Solicitud aceptada, son amigos
    REJECTED,    // Solicitud rechazada
}