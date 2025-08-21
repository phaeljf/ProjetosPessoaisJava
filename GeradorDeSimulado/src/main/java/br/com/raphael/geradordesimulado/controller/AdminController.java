package br.com.raphael.geradordesimulado.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping
    public String adminHome(Model model, HttpSession session) {
        Object name = session.getAttribute("USER_NAME");
        model.addAttribute("userName", name != null ? name : "Admin");
        return "admin/index";
    }
}
