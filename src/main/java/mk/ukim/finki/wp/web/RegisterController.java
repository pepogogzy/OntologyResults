package mk.ukim.finki.wp.web;

import mk.ukim.finki.wp.model.Role;
import mk.ukim.finki.wp.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping
public class RegisterController {
    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String getRegisterPage(@RequestParam(required = false) String error, Model model) {
        if (error != null && !error.isEmpty()) {
            model.addAttribute("hasError", true);
            model.addAttribute("error", error);
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String repeatedPassword,
                           @RequestParam String name,
                           @RequestParam String surname,
                           @RequestParam String role) {
        try {
            Role selectedRole;
            try {
                selectedRole = Role.valueOf(role);
            } catch (IllegalArgumentException e) {
                return "redirect:/register?error=Invalid role selected";
            }

            this.userService.register(username, password, repeatedPassword, name, surname, selectedRole);
            return "redirect:/login";
        } catch (RuntimeException ex) {
            return "redirect:/register?error=" + ex.getMessage();
        }
    }

    @GetMapping("/login")
    public String getLoginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null && !error.isEmpty()) {
            model.addAttribute("hasError", true);
            model.addAttribute("error", error.equals("BadCredentials") ? "Invalid username or password" : error);
        }
        return "login";
    }
}

