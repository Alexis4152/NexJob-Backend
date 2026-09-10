package com.nexjob.platform.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /** Guarda el archivo bajo {@code uploads/<subdir>/} y regresa la ruta relativa publica (ej. "/uploads/services/x.jpg"). */
    String store(MultipartFile file, String subdir);
}
