package org.first.personalexpensetrackingsystem.controller;

import org.first.personalexpensetrackingsystem.model.Expense;
import org.first.personalexpensetrackingsystem.model.Income;
import org.first.personalexpensetrackingsystem.model.User;
import org.first.personalexpensetrackingsystem.repository.UserRepository;
import org.first.personalexpensetrackingsystem.service.ExpenseService;
import org.first.personalexpensetrackingsystem.service.IncomeService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final UserRepository userRepository;

    public DashboardController(ExpenseService expenseService,
                               IncomeService incomeService,
                               UserRepository userRepository) {
        this.expenseService = expenseService;
        this.incomeService = incomeService;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            Model model,
            Authentication auth,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String yearly,
            @RequestParam(required = false) Integer page
    ) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate now = LocalDate.now();

        List<Expense> allExpenses = expenseService.getAllExpenses();

        int currentPage = (page == null ? 0 : page);
        model.addAttribute("page", currentPage);
        model.addAttribute("activePage", "dashboard");

        if ("true".equals(yearly)) {
            int y = (year != null ? year : now.getYear());
            double[] incomePerMonth = new double[12];
            double[] expensePerMonth = new double[12];
            double[] balancePerMonth = new double[12];

            List<Expense> yearExpenses = allExpenses.stream()
                    .filter(e -> e.getDate() != null && e.getDate().getYear() == y)
                    .collect(Collectors.toList());

            for (Expense e : yearExpenses) {
                int m = e.getDate().getMonthValue() - 1;
                expensePerMonth[m] += e.getTotal();
            }

            double totalIncome = 0.0;
            for (int m = 1; m <= 12; m++) {
                Income income = incomeService.getIncome(user, y, m);
                double amt = (income != null ? income.getAmount() : 0.0);
                incomePerMonth[m - 1] = amt;
                totalIncome += amt;
            }

            for (int i = 0; i < 12; i++) {
                balancePerMonth[i] = incomePerMonth[i] - expensePerMonth[i];
            }

            double totalExpenses = 0.0;
            for (double v : expensePerMonth) totalExpenses += v;

            model.addAttribute("expenses", yearExpenses);
            model.addAttribute("totalIncome", totalIncome);
            model.addAttribute("totalExpenses", totalExpenses);
            model.addAttribute("balance", totalIncome - totalExpenses);
            model.addAttribute("incomePerMonth", incomePerMonth);
            model.addAttribute("expensePerMonth", expensePerMonth);
            model.addAttribute("balancePerMonth", balancePerMonth);
            model.addAttribute("yearOnly", true);
            model.addAttribute("year", y);
            model.addAttribute("periodLabel", y);
            model.addAttribute("currentUrl", "/dashboard?yearly=true&year=" + y);
            model.addAttribute("chartTitle", "Yearly FRW Histogram");
            model.addAttribute("chartLabels", getMonthLabels());
            model.addAttribute("chartIncomeData", toList(incomePerMonth));
            model.addAttribute("chartExpenseData", toList(expensePerMonth));
            model.addAttribute("chartBalanceData", toList(balancePerMonth));

            return "dashboard";
        }

        int selectedMonth = normalizeMonth(month, now.getMonthValue());
        int selectedYear = (year != null ? year : now.getYear());

        List<Expense> filteredExpenses = allExpenses.stream()
                .filter(e -> e.getDate() != null &&
                        e.getDate().getMonthValue() == selectedMonth &&
                        e.getDate().getYear() == selectedYear)
                .collect(Collectors.toList());

        double totalExpenses = filteredExpenses.stream().mapToDouble(Expense::getTotal).sum();

        Income income = incomeService.getIncome(user, selectedYear, selectedMonth);
        double totalIncome = (income != null ? income.getAmount() : 0.0);
        double balance = totalIncome - totalExpenses;
        String monthLabel = Month.of(selectedMonth).getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        model.addAttribute("expenses", filteredExpenses);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("balance", balance);
        model.addAttribute("month", selectedMonth);
        model.addAttribute("year", selectedYear);
        model.addAttribute("yearOnly", false);
        model.addAttribute("periodLabel", monthLabel + " " + selectedYear);
        model.addAttribute("currentUrl", "/dashboard?month=" + selectedMonth + "&year=" + selectedYear);
        model.addAttribute("chartTitle", "Monthly FRW Histogram");
        model.addAttribute("chartLabels", List.of(monthLabel));
        model.addAttribute("chartIncomeData", List.of(totalIncome));
        model.addAttribute("chartExpenseData", List.of(totalExpenses));
        model.addAttribute("chartBalanceData", List.of(balance));

        return "dashboard";
    }

    private List<String> getMonthLabels() {
        return Arrays.stream(Month.values())
                .map(month -> month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                .toList();
    }

    private List<Double> toList(double[] values) {
        return Arrays.stream(values)
                .boxed()
                .toList();
    }

    private int normalizeMonth(Integer month, int fallbackMonth) {
        if (month == null) {
            return fallbackMonth;
        }
        return Math.min(Math.max(month, 1), 12);
    }
}
