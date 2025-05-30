package com.asturnet.asturnet.model;

import jakarta.persistence.*; // Importa todas las anotaciones de JPA
import lombok.Data;          // Anotación de Lombok para getters, setters, etc.
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp; // Para el timestamp de creación
import org.hibernate.annotations.UpdateTimestamp;   // Para el timestamp de actualización

import java.time.LocalDateTime; // Para manejar fechas y horas
import java.util.Set; // Usaremos Set para colecciones en las relaciones (evita duplicados)

@Entity // Indica que esta clase es una entidad JPA y se mapeará a una tabla de BD
@Table(name = "users") // Especifica el nombre de la tabla en la base de datos
@Data // Genera automáticamente getters, setters, toString(), equals() y hashCode()
@NoArgsConstructor // Genera un constructor sin argumentos
@AllArgsConstructor // Genera un constructor con todos los argumentos
public class User {

    @Id // Marca esta columna como la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Generación automática de ID (PostgreSQL SERIAL)
    private Long id; // Usamos Long para IDs de base de datos SERIAL

    @Column(unique = true, nullable = false, length = 50) // Columna username: única, no nula, max 50 chars
    private String username;

    @Column(unique = true, nullable = false, length = 100) // Columna email: única, no nula, max 100 chars
    private String email;

    @Column(nullable = false) // Columna password: no nula (aquí guardaremos la contraseña cifrada)
    private String password;

    @Column(columnDefinition = "TEXT") // Columna bio: tipo TEXT para cadenas largas
    private String bio;

    @Column(name = "photo_url", columnDefinition = "TEXT") // Columna photo_url: tipo TEXT
    private String photoUrl;

    // Para la privacidad, usamos un Enum. Se mapeará a VARCHAR en la BD.
    // 'privacy_level' en tu script, pero un Enum ofrece mejor control.
    // Asegúrate de que el nombre del enum coincida con tu columna VARCHAR(20) si usas esta estrategia
    @Enumerated(EnumType.STRING) // Guarda el nombre del enum (PUBLIC, PRIVATE) como String en la BD
    @Column(name = "privacy_level", nullable = false, length = 20)
    private PrivacyLevel privacyLevel = PrivacyLevel.PUBLIC; // Valor por defecto

    @CreationTimestamp // Anotación de Hibernate para establecer la fecha de creación automáticamente
    @Column(name = "created_at", updatable = false) // Columna created_at: no actualizable después de la creación
    private LocalDateTime createdAt;

    @UpdateTimestamp // Anotación de Hibernate para actualizar la fecha automáticamente
    @Column(name = "updated_at") // Columna updated_at
    private LocalDateTime updatedAt;

    // --- Relaciones de JPA (OneToMany, ManyToMany, etc.) ---
    // NO las incluyas todas de golpe si estás empezando, puedes añadirlas progresivamente.
    // Te las dejo comentadas para que sepas cómo se harían:

    /*
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Post> posts; // Los posts creados por este usuario

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments; // Los comentarios hechos por este usuario

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Like> likes; // Los likes dados por este usuario

    // Relaciones de amistad: Un usuario puede ser 'user_id' o 'friend_id'
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Friend> sentFriendRequests; // Solicitudes de amistad iniciadas por este usuario

    @OneToMany(mappedBy = "friend", cascade = CascadeType.ALL)
    private Set<Friend> receivedFriendRequests; // Solicitudes de amistad recibidas por este usuario

    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL)
    private Set<Report> sentReports; // Reportes hechos por este usuario

    @OneToMany(mappedBy = "reportedUser", cascade = CascadeType.ALL)
    private Set<Report> receivedReports; // Reportes donde este usuario es el reportado (nullable en DB)
    */
}