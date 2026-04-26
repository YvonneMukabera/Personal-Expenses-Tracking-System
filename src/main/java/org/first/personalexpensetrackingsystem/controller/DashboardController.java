package org.first.personalexpensetrackingsystem.controller;

import org.first.personalexpensetrackingsystem.model.Expense;
import org.first.personalexpensetrackingsystem.model.Income;
import org.first.personalexpensetrackingsystem.service.ExpenseService;
import org.first.personalexpensetrackingsystem.service.IncomeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    public DashboardController(ExpenseService expenseService, IncomeService incomeService) {
        this.expenseService = expenseService;
        this.incomeService = incomeService;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            Model model,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {

        // 1. Default to current month/year if not provided
        LocalDate now = LocalDate.now();
        int selectedMonth = (month != null) ? month : now.getMonthValue();
        int selectedYear = (year != null) ? year : now.getYear();

        // 2. Get all expenses
        List<Expense> allExpenses = expenseService.getAllExpenses();

        // 3. Filter expenses by month/year
        List<Expense> filteredExpenses = allExpenses.stream()
                .filter(e -> e.getDate() != null &&
                        e.getDate().getMonthValue() == selectedMonth &&
                        e.getDate().getYear() == selectedYear)
                .collect(Collectors.toList());

        // 4. Calculate total expenses
        double totalExpenses = filteredExpenses.stream()
                .mapToDouble(e -> e.getAmount() *
                        (e.getUnitCost() != null ? e.getUnitCost() : 0))
                .sum();

        // 5. Get income for selected month/year
        Income income = incomeService.getIncome(selectedYear, selectedMonth);
        double totalIncome = (income != null) ? income.getAmount() : 0.0;

        // 6. Balance
        double balance = totalIncome - totalExpenses;

        // 7. Send data to view
        model.addAttribute("expenses", filteredExpenses);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("balance", balance);
        model.addAttribute("month", selectedMonth);
        model.addAttribute("year", selectedYear);

        return "dashboard";
    }
}