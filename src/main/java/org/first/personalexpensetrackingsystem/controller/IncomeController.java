package org.first.personalexpensetrackingsystem.controller;

import org.first.personalexpensetrackingsystem.model.Income;
import org.first.personalexpensetrackingsystem.model.User;
import org.first.personalexpensetrackingsystem.repository.UserRepository;
import org.first.personalexpensetrackingsystem.service.IncomeService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class IncomeController {

    private final IncomeService incomeService;
    private final UserRepository userRepository;

    public IncomeController(IncomeService incomeService, UserRepository userRepository) {
        this.incomeService = incomeService;
        this.userRepository = userRepository;
    }

    @PostMapping("/income/save")
    public String saveIncome(
            @RequestParam(required = false) Integer month,
            @RequestParam int year,
            @RequestParam Double amount,
            RedirectAttributes redirectAttributes
    ) {
        if (month == null) {
            month = 1;
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Income existing = incomeService.getIncome(currentUser, year, month);

        if (existing != null) {
            existing.setAmount(amount);
            incomeService.saveIncome(existing);
        } else {
            Income income = new Income(year, month, amount, currentUser);
            incomeService.saveIncome(income);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Income saved successfully.");
        return "redirect:/dashboard?month=" + month + "&year=" + year;
    }
}
