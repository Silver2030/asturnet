package com.asturnet.asturnet.model;

public enum ReportStatus {
    PENDING,   // El reporte ha sido creado y está pendiente de revisión
    REVIEWED,  // El reporte ha sido revisado (acción tomada o no)
    DISMISSED  // El reporte ha sido desestimado (no se ha tomado ninguna acción)
}