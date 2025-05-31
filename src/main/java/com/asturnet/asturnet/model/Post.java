package com.asturnet.asturnet.model;

import jakarta.persistence.*; // Asegúrate de usar jakarta.persistence para JPA
import lombok.Data; // Para generar getters, setters, equals, hashCode, toString
import lombok.NoArgsConstructor; // Para el constructor sin argumentos
import lombok.AllArgsConstructor; // Para un constructor con todos los argumentos
import org.hibernate.annotations.CreationTimestamp; // Para la fecha de creación automática
import org.hibernate.annotations.UpdateTimestamp; // Para la fecha de actualización automática

import java.time.LocalDateTime; // Para manejar fechas y horas
import java.util.ArrayList; // Necesario para inicializar las listas
import java.util.List;     // Para las colecciones de comentarios y usuarios que dieron like

@Entity // Marca esta clase como una entidad JPA
@Table(name = "posts") // Define el nombre de la tabla en la base de datos
@Data // Anotación de Lombok para generar automáticamente getters, setters, toString, equals y hashCode
@NoArgsConstructor // Genera un constructor sin argumentos (necesario para JPA)
@AllArgsConstructor // Genera un constructor con todos los campos como argumentos (opcional, pero útil)
public class Post {

    @Id // Marca el campo 'id' como la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura la estrategia de generación de ID (autoincremento)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false) // Mapea a una columna de tipo TEXT y no puede ser nula
    private String content; // Contenido textual del post

    @Column(name = "image_url", columnDefinition = "TEXT") // Mapea a la columna 'image_url' de tipo TEXT
    private String imageUrl; // URL de la imagen si la hay

    @Column(name = "video_url", columnDefinition = "TEXT") // Mapea a la columna 'video_url' de tipo TEXT
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

    // *** INICIO DE RELACIONES DESCOMENTADAS Y CORREGIDAS ***

    // Relación OneToMany con Comment (Un Post puede tener MÚLTIPLES comentarios)
    // 'mappedBy = "post"' indica que el campo 'post' en la entidad Comment es el dueño de la relación.
    // 'cascade = CascadeType.ALL' significa que las operaciones (persist, remove, refresh, merge, detach)
    // realizadas en el Post se propagarán a sus Comments.
    // 'orphanRemoval = true' significa que si un comentario es desvinculado de un Post, será eliminado de la DB.
    // 'fetch = FetchType.LAZY' (carga perezosa) los comentarios no se cargan hasta que se acceden.
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>(); // ¡Importante inicializar para evitar NullPointerException!

    // Relación ManyToMany con User para los "likes" (Muchos Posts pueden ser gustados por Muchos Usuarios)
    // @JoinTable define la tabla intermedia que mapea esta relación.
    // 'name = "post_likes"' es el nombre de la tabla intermedia.
    // 'joinColumns' define la columna que referencia a esta entidad (Post) en la tabla intermedia.
    // 'inverseJoinColumns' define la columna que referencia a la entidad "inversa" (User) en la tabla intermedia.
    @ManyToMany
    @JoinTable(
            name = "post_likes",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> likedByUsers = new ArrayList<>(); // ¡Importante inicializar!

    // *** FIN DE RELACIONES DESCOMENTADAS Y CORREGIDAS ***

    // Nota: Gracias a @Data de Lombok, no necesitas escribir los getters y setters explícitamente
    // para 'comments' y 'likedByUsers'. Lombok los generará automáticamente.
    // Si usas el constructor @AllArgsConstructor, asegúrate de que todos los campos
    // (incluyendo comments y likedByUsers) estén en el orden correcto si tienes problemas,
    // o simplemente confía en @NoArgsConstructor y los setters.
    // Para simplificar, a menudo se usa @Data y @NoArgsConstructor, y se inicializan las listas directamente.
}