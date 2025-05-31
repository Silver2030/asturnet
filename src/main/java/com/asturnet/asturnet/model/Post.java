package com.asturnet.asturnet.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
// import java.util.Set; // Dejamos esto comentado por ahora, para no añadir complejidad con Comments/Likes

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // Contenido textual del post

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl; // URL de la imagen si la hay

    @Column(name = "video_url", columnDefinition = "TEXT")
    private String videoUrl; // URL del video si lo hay

    @ManyToOne(fetch = FetchType.LAZY) // Relación Muchos a Uno con User (Muchos posts pueden ser de UN usuario)
    @JoinColumn(name = "user_id", nullable = false) // Columna en la tabla 'posts' que guarda el ID del usuario
    private User user; // El objeto User que es el autor de la publicación

    @CreationTimestamp // Anotación de Hibernate para establecer la fecha de creación automáticamente
    @Column(name = "created_at", updatable = false) // Columna created_at: no actualizable después de la creación
    private LocalDateTime createdAt;

    @UpdateTimestamp // Anotación de Hibernate para actualizar la fecha automáticamente
    @Column(name = "updated_at") // Columna updated_at
    private LocalDateTime updatedAt;

    // Si en el futuro añades Comentarios o Likes, sus relaciones OneToMany irían aquí:
    /*
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Like> likes;
    */
}