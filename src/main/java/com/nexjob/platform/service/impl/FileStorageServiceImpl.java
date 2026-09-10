package com.nexjob.platform.service.impl;

import com.nexjob.platform.exception.BusinessException;
import com.nexjob.platform.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024;

    @Value("${app.uploads.dir}")
    private String uploadsDir;

    @Override
    public String store(MultipartFile file, String subdir) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("El archivo esta vacio");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BusinessException("El archivo excede el tamano maximo permitido (5MB)");
        }
        String original = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        String extension = extensionOf(original);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Formato de imagen no permitido. Usa: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        try {
            Path targetDir = Path.of(uploadsDir, subdir);
            Files.createDirectories(targetDir);
            String filename = UUID.randomUUID() + "." + extension;
            Path targetPath = targetDir.resolve(filename);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return "/uploads/" + subdir + "/" + filename;
        } catch (IOException e) {
            throw new BusinessException("No se pudo guardar el archivo");
        }
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase();
    }
}
