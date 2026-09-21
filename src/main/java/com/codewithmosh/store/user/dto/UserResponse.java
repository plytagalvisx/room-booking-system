package com.codewithmosh.store.user.dto;

public record UserResponse(
        Long id,
        String name,
        String email
) {
}
