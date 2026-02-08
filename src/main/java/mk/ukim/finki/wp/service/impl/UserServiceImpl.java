package mk.ukim.finki.wp.service.impl;

import jakarta.annotation.PostConstruct;
import mk.ukim.finki.wp.model.Role;
import mk.ukim.finki.wp.model.User;
import mk.ukim.finki.wp.repository.UserRepository;
import mk.ukim.finki.wp.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(String username, String password, String repeatPassword, String name, String surname, Role role) {

        User user = new User(username, passwordEncoder.encode(password), name, surname, role);

        return userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }

    @PostConstruct
    public void initUsers() {
        register("jana.stoichkova",  "bono.123", "bono.123", "Jana", "Stoichkova", Role.ROLE_ADMIN);
        register("petar.gogovski",  "medo.123", "medo.123", "Petar", "Gogovski", Role.ROLE_USER);
        register("dimitri.stojanovski",  "dolly.123", "dolly.123", "Dimitri", "Stojanovski", Role.ROLE_EXPERT);
    }
}

