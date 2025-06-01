package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.Report;
import com.asturnet.asturnet.model.ReportStatus;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.repository.ReportRepository;
import com.asturnet.asturnet.repository.UserRepository;
import com.asturnet.asturnet.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public ReportServiceImpl(ReportRepository reportRepository, UserRepository userRepository, PostRepository postRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @Override
    @Transactional
    public Report submitUserReport(Long reporterId, Long reportedUserId, String reason) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("Reporter not found with ID: " + reporterId));
        User reported = userRepository.findById(reportedUserId)
                .orElseThrow(() -> new RuntimeException("Reported user not found with ID: " + reportedUserId));

        Report report = new Report();
        report.setReporter(reporter);
        report.setReportedUser(reported);
        report.setReason(reason);
        report.setStatus(ReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());
        report.setReportedPost(null); // Explícitamente null para reportes de usuario
        return reportRepository.save(report);
    }

    @Override
    @Transactional
    public Report submitPostReport(Long reporterId, Long reportedPostId, String reason) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("Reporter not found with ID: " + reporterId));
        Post reportedPost = postRepository.findById(reportedPostId)
                .orElseThrow(() -> new RuntimeException("Reported post not found with ID: " + reportedPostId));

        Report report = new Report();
        report.setReporter(reporter);
        report.setReportedPost(reportedPost);
        report.setReason(reason);
        report.setStatus(ReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());

        // AQUI ESTÁ LA LÍNEA QUE FALTABA PARA ASIGNAR EL USUARIO REPORTADO EN UN REPORTE DE POST
        if (reportedPost.getUser() != null) {
            report.setReportedUser(reportedPost.getUser()); // ¡Asigna el creador del post como el usuario reportado!
        } else {
            // Esto es una advertencia, pero dado que user_id es nullable=false en Post.java,
            // esta situación no debería ocurrir si los posts se guardan correctamente.
            System.err.println("Advertencia: El post con ID " + reportedPostId + " no tiene un usuario asociado para reportar.");
            // Si quieres que el reporte falle si no hay autor, puedes lanzar una excepción aquí.
            // throw new RuntimeException("No se puede reportar un post sin autor.");
        }

        return reportRepository.save(report);
    }

    @Override
    public List<Report> findAllReports() {
        return reportRepository.findAll();
    }

    @Override
    public Optional<Report> findReportById(Long id) {
        return reportRepository.findById(id);
    }

    @Override
    @Transactional
    public void updateReportStatus(Long reportId, ReportStatus newStatus, String adminUsername, String adminNotes) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado con ID: " + reportId));

        report.setStatus(newStatus);
        report.setResolvedBy(adminUsername);
        report.setReviewedAt(LocalDateTime.now());

        String currentAdminNotes = report.getAdminNotes();
        String newNoteEntry = "[" + LocalDateTime.now() + " by " + adminUsername + "] " + newStatus.name() + " notes: " + (adminNotes != null ? adminNotes : "N/A");

        if (currentAdminNotes != null && !currentAdminNotes.isEmpty()) {
            report.setAdminNotes(currentAdminNotes + "\n" + newNoteEntry);
        } else {
            report.setAdminNotes(newNoteEntry);
        }

        reportRepository.save(report);
    }
}