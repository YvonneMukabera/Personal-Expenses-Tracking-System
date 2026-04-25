package org.first.personalexpensetrackingsystem.controller;

import org.first.personalexpensetrackingsystem.model.Income;
import org.first.personalexpensetrackingsystem.service.IncomeService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IncomeController {

    private final IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @PostMapping("/income/save")
    public String saveIncome(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam Double amount
    ) {

        // 1. Check if income already exists for this month/year
        Income existing = incomeService.getIncome(year, month);

        if (existing != null) {
            // Update existing income
            existing.setAmount(amount);
            incomeService.saveIncome(existing);
        } else {
            // Create new income
            Income income = new Income(year, month, amount);
            incomeService.saveIncome(income);
        }

        // 2. Redirect back to same dashboard month/year
        return "redirect:/dashboard?month=" + month + "&year=" + year;
    }
}