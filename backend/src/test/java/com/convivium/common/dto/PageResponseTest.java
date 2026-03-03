package com.convivium.common.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResponseTest {

    @Test
    void from_mapsPageCorrectly() {
        Page<String> page = new PageImpl<>(List.of("a", "b"), PageRequest.of(0, 10), 25);

        PageResponse<String> response = PageResponse.from(page);

        assertNotNull(response);
        assertEquals(List.of("a", "b"), response.getContent());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(25, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
        assertFalse(response.isLast());
    }

    @Test
    void from_emptyPage() {
        Page<String> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        PageResponse<String> response = PageResponse.from(page);

        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotalElements());
        assertTrue(response.isLast());
    }
}
