package com.asturnet.asturnet.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false) // El usuario que realiza el reporte
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id") // El usuario reportado (puede ser nulo si se reporta un post)
    private User reportedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_post_id") // El post reportado (puede ser nulo si se reporta un usuario)
    private Post reportedPost;

    @Column(columnDefinition = "TEXT", nullable = false) // Razón o descripción del reporte
    private String reason;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING) // Estado del reporte
    private ReportStatus status = ReportStatus.PENDING; // Valor por defecto

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Puedes añadir un campo para la revisión del admin si lo deseas, ej:
    // @Column(columnDefinition = "TEXT")
    // private String adminNotes;
    // private LocalDateTime reviewedAt;
}