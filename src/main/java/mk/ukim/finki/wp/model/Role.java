package mk.ukim.finki.wp.model;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_USER,
    ROLE_EXPERT,
    ROLE_ADMIN;

    @Override
    public String getAuthority() {
        return name();
    }

    }
