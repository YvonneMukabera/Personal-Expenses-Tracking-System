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
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String yearly,
            @RequestParam(required = false) Integer page
    ) {

        LocalDate now = LocalDate.now();
        List<Expense> allExpenses = expenseService.getAllExpenses();

        int currentPage = (page == null ? 0 : page);
        model.addAttribute("page", currentPage);

        /* ================= YEAR VIEW ================= */
        if ("true".equals(yearly)) {

            int y = (year != null ? year : now.getYear());

            double[] incomePerMonth = new double[12];
            double[] expensePerMonth = new double[12];

            List<Expense> yearExpenses = allExpenses.stream()
                    .filter(e -> e.getDate() != null && e.getDate().getYear() == y)
                    .collect(Collectors.toList());

            for (Expense e : yearExpenses) {

                int m = e.getDate().getMonthValue() - 1;

                double total = e.getAmount() *
                        (e.getUnitCost() != null ? e.getUnitCost() : 0);

                expensePerMonth[m] += total;
            }

            double totalIncome = 0.0;

            for (int m = 1; m <= 12; m++) {
                Income income = incomeService.getIncome(y, m);
                double amt = (income != null ? income.getAmount() : 0.0);
                incomePerMonth[m - 1] = amt;
                totalIncome += amt;
            }

            double totalExpenses = 0.0;
            for (double v : expensePerMonth) totalExpenses += v;

            model.addAttribute("expenses", yearExpenses);
            model.addAttribute("totalIncome", totalIncome);
            model.addAttribute("totalExpenses", totalExpenses);
            model.addAttribute("balance", totalIncome - totalExpenses);

            model.addAttribute("incomePerMonth", incomePerMonth);
            model.addAttribute("expensePerMonth", expensePerMonth);

            model.addAttribute("yearOnly", true);
            model.addAttribute("year", y);

            // ✅ SAFE FOR CHART JS
            model.addAttribute("incomeJson", incomePerMonth);
            model.addAttribute("expenseJson", expensePerMonth);

            return "dashboard";
        }

        /* ================= MONTH VIEW ================= */

        int selectedMonth = (month != null ? month : now.getMonthValue());
        int selectedYear = (year != null ? year : now.getYear());

        List<Expense> filteredExpenses = allExpenses.stream()
                .filter(e -> e.getDate() != null &&
                        e.getDate().getMonthValue() == selectedMonth &&
                        e.getDate().getYear() == selectedYear)
                .collect(Collectors.toList());

        double totalExpenses = filteredExpenses.stream()
                .mapToDouble(e -> e.getAmount() *
                        (e.getUnitCost() != null ? e.getUnitCost() : 0))
                .sum();

        Income income = incomeService.getIncome(selectedYear, selectedMonth);
        double totalIncome = (income != null ? income.getAmount() : 0.0);

        model.addAttribute("expenses", filteredExpenses);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("balance", totalIncome - totalExpenses);

        model.addAttribute("month", selectedMonth);
        model.addAttribute("year", selectedYear);

        model.addAttribute("yearOnly", false);

        // SAFE CHART VALUES
        model.addAttribute("incomeJson", new double[]{totalIncome});
        model.addAttribute("expenseJson", new double[]{totalExpenses});

        return "dashboard";
    }
}