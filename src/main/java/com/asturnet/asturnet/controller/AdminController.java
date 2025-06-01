package com.asturnet.asturnet.controller;

import com.asturnet.asturnet.model.Report;
import com.asturnet.asturnet.model.ReportStatus;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.service.ReportService;
import com.asturnet.asturnet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final ReportService reportService;

    @Autowired
    public AdminController(UserService userService, ReportService reportService) {
        this.userService = userService;
        this.reportService = reportService;
    }

    // Endpoint para banear un usuario
    @PostMapping("/users/ban")
    public String banUser(@RequestParam("userId") Long userId,
                          @RequestParam(value = "reason", required = false) String reason,
                          RedirectAttributes redirectAttributes) {
        try {
            userService.banUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario con ID " + userId + " ha sido baneado correctamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al banear usuario: " + e.getMessage());
        }
        Optional<User> userOptional = userService.findById(userId);
        return userOptional.map(user -> "redirect:/profile/" + user.getUsername())
                           .orElse("redirect:/admin/reports");
    }

    // Endpoint para desbanear un usuario
    @PostMapping("/users/unban")
    public String unbanUser(@RequestParam("userId") Long userId,
                            RedirectAttributes redirectAttributes) {
        try {
            userService.unbanUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario con ID " + userId + " ha sido desbaneado correctamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al desbanear usuario: " + e.getMessage());
        }
        Optional<User> userOptional = userService.findById(userId);
        return userOptional.map(user -> "redirect:/profile/" + user.getUsername())
                           .orElse("redirect:/admin/reports");
    }

    // Endpoint para recuperar la lista de reportes
    @GetMapping("/reports")
    public String listReports(Model model) {
        List<Report> reports = reportService.findAllReports();
        model.addAttribute("reports", reports);
        return "admin/report-list"; 
    }

    // Endpoint para recuperar un reporte
    @GetMapping("/reports/{id}")
    public String viewReportDetails(@PathVariable Long id, Model model) {
        Report report = reportService.findReportById(id)
                                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporte no encontrado"));
        model.addAttribute("report", report);
        return "admin/report-details"; 
    }

    // Endpoint para actualizar el estado de un reporte
    @PostMapping("/reports/{id}/update-status")
    public String updateReportStatus(@PathVariable Long id,
                                     @RequestParam("status") String status,
                                     @RequestParam(value = "adminNotes", required = false) String adminNotes,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        try {
            ReportStatus newStatus = ReportStatus.valueOf(status.toUpperCase());
            reportService.updateReportStatus(id, newStatus, principal.getName(), adminNotes);
            redirectAttributes.addFlashAttribute("successMessage", "Estado del reporte actualizado a " + newStatus.name() + ".");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Estado de reporte inválido: " + status);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el estado del reporte: " + e.getMessage());
        }
        return "redirect:/admin/reports/" + id; 
    }
    
}