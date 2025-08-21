package br.com.raphael.geradordesimulado.controller;

import br.com.raphael.geradordesimulado.domain.User;
import br.com.raphael.geradordesimulado.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public AuthController(UserRepository userRepository, BCryptPasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "logout", required = false) String logout, Model model) {
        if (logout != null) {
            model.addAttribute("info", "Você saiu da sessão.");
        }
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) {

        Optional<User> opt = userRepository.findByEmail(email);
        if (opt.isEmpty()) {
            model.addAttribute("error", "E-mail ou senha inválidos.");
            return "login";
        }

        User u = opt.get();
        boolean ok = encoder.matches(password, u.getPassword());
        if (!ok || !u.isActive()) {
            model.addAttribute("error", "E-mail ou senha inválidos.");
            return "login";
        }

        session.setAttribute("USER_ID", u.getId());
        session.setAttribute("USER_NAME", u.getName());
        session.setAttribute("USER_ROLE", u.getRole().name());

        return "redirect:/admin";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login?logout";
    }
}
