package org.first.personalexpensetrackingsystem.controller;

import org.first.personalexpensetrackingsystem.model.User;
import org.first.personalexpensetrackingsystem.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserRepository repo;

    public AuthController(UserRepository repo) {
        this.repo = repo;
    }

    // SHOW LOGIN PAGE
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // SHOW REGISTER PAGE
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // ONLY REGISTER (NO LOGIN HERE)
    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String phone,
                           @RequestParam(required = false) String email,
                           @RequestParam String password) {

        User user = new User();
        user.setName(name);
        user.setPhone(phone);
        user.setEmail(email);
        user.setPassword(password); // (we will secure later if needed)

        repo.save(user);

        return "redirect:/login";
    }
}