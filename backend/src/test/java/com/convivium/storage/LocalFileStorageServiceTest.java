package com.convivium.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalFileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void store_throwsWhenFileEmpty() {
        LocalFileStorageService service = new LocalFileStorageService(tempDir.toString());
        MockMultipartFile empty = new MockMultipartFile("file", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> service.store("path/file.txt", empty));
    }

    @Test
    void store_throwsWhenFileNull() {
        LocalFileStorageService service = new LocalFileStorageService(tempDir.toString());

        assertThrows(IllegalArgumentException.class, () -> service.store("path/file.txt", null));
    }

    @Test
    void store_returnsPath() {
        LocalFileStorageService service = new LocalFileStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        String result = service.store("parcels/1/test.txt", file);

        assertNotNull(result);
        assertTrue(result.contains("/uploads/"));
        assertTrue(result.contains("parcels/1/test.txt"));
    }
}
