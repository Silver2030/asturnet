package com.asturnet.asturnet.repository;

import com.asturnet.asturnet.model.Report;
import com.asturnet.asturnet.model.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    // Encuentra todos los reportes con un estado específico
    List<Report> findByStatus(ReportStatus status);

    // Encuentra reportes por el ID del usuario que los hizo (reporter_id en tu Report.java)
    List<Report> findByReporterId(Long reporterId);

    // Encuentra reportes por el ID del post reportado (reported_post_id en tu Report.java)
    List<Report> findByReportedPostId(Long postId);

    // Encuentra reportes por el ID del usuario reportado (reported_user_id en tu Report.java)
    List<Report> findByReportedUserId(Long reportedUserId);
}