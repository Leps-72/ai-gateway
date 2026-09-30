package com.aigateway.security;

import java.security.Principal;

public record AuthenticatedUser(Long userId, String username) implements Principal {

    @Override
    public String getName() {
        return username;
    }
}
