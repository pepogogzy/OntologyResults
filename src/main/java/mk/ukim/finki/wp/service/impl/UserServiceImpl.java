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
        /*if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            throw new InvalidArgumentsException();
        }

        if (!password.equals(repeatPassword)) {
            throw new PasswordsDoNotMatchException();
        }

        if (this.userRepository.findByUsername(username).isPresent()) {
            throw new UsernameAlreadyExistsException(username);
        }*/

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
        register("admin",  "admin", "admin", "admin", "admin", Role.ROLE_ADMIN);
        register("user",  "user", "user", "user", "user", Role.ROLE_USER);
        register("expert",  "expert", "expert", "expert", "expert", Role.ROLE_EXPERT);
    }
}

