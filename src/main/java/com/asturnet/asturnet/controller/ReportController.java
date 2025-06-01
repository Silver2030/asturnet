package com.asturnet.asturnet.controller;

import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.service.ReportService;
import com.asturnet.asturnet.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReportController {
    private final ReportService reportService;
    private final UserService userService;

    public ReportController(ReportService reportService, UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }

    // Endpoint para reportar un post
    @PostMapping("/report/post")
    public String reportPost(@RequestParam("postId") Long postId,
                             @RequestParam("reason") String selectedReason, 
                             @RequestParam(value = "description", required = false) String description, 
                             @AuthenticationPrincipal UserDetails currentUserDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            User reporter = userService.findByUsername(currentUserDetails.getUsername());
            Long reporterId = reporter.getId();

            String fullReason = selectedReason;
            if (description != null && !description.trim().isEmpty()) {
                fullReason += ": " + description.trim();
            }

            reportService.submitPostReport(reporterId, postId, fullReason); 
            redirectAttributes.addFlashAttribute("successMessage", "Publicación reportada con éxito. Gracias por tu contribución para mantener la comunidad segura.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al reportar la publicación: " + e.getMessage());
        }
        return "redirect:/home";
    }

    // Endpoint para reportar a un user
    @PostMapping("/report/user")
    public String reportUser(@RequestParam("reportedUserId") Long reportedUserId,
                             @RequestParam("reason") String selectedReason, 
                             @RequestParam(value = "description", required = false) String description, 
                             @AuthenticationPrincipal UserDetails currentUserDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            User reporter = userService.findByUsername(currentUserDetails.getUsername());
            Long reporterId = reporter.getId();

            String fullReason = selectedReason;
            if (description != null && !description.trim().isEmpty()) {
                fullReason += ": " + description.trim();
            }

            reportService.submitUserReport(reporterId, reportedUserId, fullReason); 
            redirectAttributes.addFlashAttribute("successMessage", "Usuario reportado con éxito. Gracias por tu contribución para mantener la comunidad segura.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al reportar el usuario: " + e.getMessage());
        }
        return "redirect:/profile/" + userService.findById(reportedUserId).map(User::getUsername).orElse("home");
    }
}