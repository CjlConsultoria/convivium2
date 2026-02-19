package com.convivium.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Armazena arquivos localmente (pasta do sistema) ou em S3 (online).
 * Retorna a URL ou caminho para acesso ao arquivo.
 */
public interface FileStorageService {

    /**
     * Armazena o arquivo e retorna a URL ou path para acesso.
     * @param relativePath ex: "parcels/1/abc.jpg"
     * @param file arquivo enviado
     * @return URL (S3) ou path relativo (local, ex: /uploads/parcels/1/abc.jpg)
     */
    String store(String relativePath, MultipartFile file);
}
