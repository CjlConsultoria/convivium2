package com.convivium.storage;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * S3FileStorageService validates input before S3 calls.
 * Constructor builds real S3Client - may fail if no AWS credentials in env.
 */
class S3FileStorageServiceTest {

    @Test
    void store_throwsWhenFileEmpty() {
        S3FileStorageService service = new S3FileStorageService("", "us-east-1", "test-bucket", "", "");
        MockMultipartFile empty = new MockMultipartFile("file", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> service.store("path/file.txt", empty));
    }

    @Test
    void store_throwsWhenFileNull() {
        S3FileStorageService service = new S3FileStorageService("", "us-east-1", "test-bucket", "", "");
        assertThrows(IllegalArgumentException.class, () -> service.store("path/file.txt", null));
    }
}
