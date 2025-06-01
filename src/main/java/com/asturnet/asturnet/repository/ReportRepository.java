package com.asturnet.asturnet.repository;

import com.asturnet.asturnet.model.Report;
import com.asturnet.asturnet.model.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByStatus(ReportStatus status);

    List<Report> findByReporterId(Long reporterId);

    List<Report> findByReportedPostId(Long postId);

    List<Report> findByReportedUserId(Long reportedUserId);
}