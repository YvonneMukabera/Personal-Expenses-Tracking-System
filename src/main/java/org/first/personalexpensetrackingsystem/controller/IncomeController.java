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
            @RequestParam(required = false) Integer month,
            @RequestParam int year,
            @RequestParam Double amount
    ) {

        // SAFETY: fallback if month is missing (prevents 400 error)
        if (month == null) {
            month = 1;
        }

        Income existing = incomeService.getIncome(year, month);

        if (existing != null) {
            existing.setAmount(amount);
            incomeService.saveIncome(existing);
        } else {
            Income income = new Income(year, month, amount);
            incomeService.saveIncome(income);
        }

        return "redirect:/dashboard?month=" + month + "&year=" + year;
    }
}