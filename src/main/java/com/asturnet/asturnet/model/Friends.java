package com.asturnet.asturnet.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "friends", uniqueConstraints = { // <-- Nombre de la tabla "friends"
    @UniqueConstraint(columnNames = {"user_id", "friend_id"}) // <-- Columnas "user_id" y "friend_id"
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friends { // <-- ¡La clase ahora se llama Friends!

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Mapea a user_id en la tabla
    private User user; // El usuario que inicia la solicitud/amistad (o el primer usuario en la relación)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false) // Mapea a friend_id en la tabla
    private User friend; // El usuario que recibe la solicitud/es amigo (o el segundo usuario en la relación)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}