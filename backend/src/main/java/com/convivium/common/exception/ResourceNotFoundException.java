package com.convivium.common.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String entityName;
    private final Object id;

    public ResourceNotFoundException(String entityName, Object id) {
        super("%s nao encontrado com id: %s".formatted(entityName, id));
        this.entityName = entityName;
        this.id = id;
    }
}
