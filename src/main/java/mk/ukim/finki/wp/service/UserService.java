package mk.ukim.finki.wp.service;

import mk.ukim.finki.wp.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import mk.ukim.finki.wp.model.Role;

public interface UserService extends UserDetailsService {
    User register(String username, String password, String repeatPassword, String name, String surname, Role role);
    User findByUsername(String username);
}

