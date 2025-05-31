package com.asturnet.asturnet.model;

public enum ReportStatus {
    PENDING,    // Reporte recién creado, esperando revisión
    REVIEWED,   // Reporte ha sido visto por un admin, pero no resuelto/desestimado
    RESOLVED,   // Acción tomada (ej. usuario baneado, post borrado)
    DISMISSED   // Reporte desestimado (ej. no es válido, no hay acción necesaria)
}