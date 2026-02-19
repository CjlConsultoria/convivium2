package com.convivium.module.user.dto;

/**
 * Corpo opcional para aprovação de usuário.
 * unitId: unidade a atribuir/confirmar ao aprovar (opcional; se não enviado, mantém a já vinculada no cadastro).
 */
public record ApproveUserRequest(Long unitId) {
}
