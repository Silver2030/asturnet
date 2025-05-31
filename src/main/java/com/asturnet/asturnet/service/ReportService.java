package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.Report;
import com.asturnet.asturnet.model.ReportStatus;

import java.util.List;
import java.util.Optional;

public interface ReportService {

    Report submitUserReport(Long reporterId, Long reportedUserId, String reason);
    Report submitPostReport(Long reporterId, Long reportedPostId, String reason);

    List<Report> findAllReports();
    Optional<Report> findReportById(Long id);

    void updateReportStatus(Long reportId, ReportStatus newStatus, String adminUsername, String adminNotes);
}