package com.asturnet.asturnet.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Muchos comentarios a un post
    @JoinColumn(name = "post_id", nullable = false) // Columna post_id en la tabla comments
    private Post post; // El post al que pertenece este comentario

    @ManyToOne(fetch = FetchType.LAZY) // Muchos comentarios a un usuario
    @JoinColumn(name = "user_id", nullable = false) // Columna user_id en la tabla comments
    private User user; // El usuario que hizo el comentario

    @Column(columnDefinition = "TEXT", nullable = false) // Contenido del comentario
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false) // Fecha de creación del comentario
    private LocalDateTime createdAt;
}