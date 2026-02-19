package com.convivium.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private final Path basePath;

    public LocalFileStorageService(
            @Value("${app.storage.local.path:./uploads}") String localPath) {
        this.basePath = Paths.get(localPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(basePath);
        } catch (IOException e) {
            log.warn("Could not create uploads directory: {}", basePath, e);
        }
    }

    @Override
    public String store(String relativePath, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio");
        }
        try {
            Path target = basePath.resolve(relativePath).normalize();
            if (!target.startsWith(basePath)) {
                throw new IllegalArgumentException("Path invalido");
            }
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target);
            return "/uploads/" + relativePath.replace("\\", "/");
        } catch (IOException e) {
            log.error("Erro ao salvar arquivo local: {}", relativePath, e);
            throw new RuntimeException("Erro ao salvar arquivo", e);
        }
    }
}
