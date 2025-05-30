package com.asturnet.asturnet.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable; // Necesario para la clave compuesta si la usáramos, aunque aquí no es necesario
import java.time.LocalDateTime;

@Entity
@Table(name = "likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "post_id"}) // Para evitar likes duplicados
})
@Data
@NoArgsConstructor
// AllArgsConstructor no es estrictamente necesario aquí porque no tenemos campos directos aparte de las FKs y ID,
// y el constructor se manejaría normalmente al crear una instancia con user y post.
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Muchos likes a un post
    @JoinColumn(name = "post_id", nullable = false) // Columna post_id en la tabla likes
    private Post post; // El post al que se le dio like

    @ManyToOne(fetch = FetchType.LAZY) // Muchos likes de un usuario
    @JoinColumn(name = "user_id", nullable = false) // Columna user_id en la tabla likes
    private User user; // El usuario que dio el like

    @CreationTimestamp
    @Column(name = "created_at", updatable = false) // Fecha de creación del like
    private LocalDateTime createdAt;

    // Constructor personalizado si se prefiere para facilidad
    public Like(User user, Post post) {
        this.user = user;
        this.post = post;
    }
}