package org.first.personalexpensetrackingsystem.controller;

import org.first.personalexpensetrackingsystem.model.Expense;
import org.first.personalexpensetrackingsystem.model.User;
import org.first.personalexpensetrackingsystem.repository.ExpenseRepository;
import org.first.personalexpensetrackingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Controller
public class ExpenseController {

    private static final int PAGE_SIZE = 20;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/expenses")
    public String listExpenses(Model model, Authentication auth,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "") String keyword,
                               @RequestParam(defaultValue = "") String category,
                               @RequestParam(required = false) Integer filterYear,
                               @RequestParam(required = false) Integer filterMonth) {

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        int requestedPage = Math.max(page, 0);
        Page<Expense> expensePage = findExpensePage(user, requestedPage, keyword, category, filterYear, filterMonth);

        if (expensePage.getTotalPages() > 0 && requestedPage >= expensePage.getTotalPages()) {
            requestedPage = expensePage.getTotalPages() - 1;
            expensePage = findExpensePage(user, requestedPage, keyword, category, filterYear, filterMonth);
        }

        long totalItems = expensePage.getTotalElements();
        int startItem = totalItems == 0 ? 0 : requestedPage * PAGE_SIZE + 1;
        int endItem = Math.min((requestedPage + 1) * PAGE_SIZE, (int) totalItems);

        model.addAttribute("expenses", expensePage.getContent());
        model.addAttribute("currentPage", requestedPage);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("totalPages", expensePage.getTotalPages());
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("startItem", startItem);
        model.addAttribute("endItem", endItem);
        model.addAttribute("pageNumbers", getPageNumbers(requestedPage, expensePage.getTotalPages()));
        model.addAttribute("hasPrevious", expensePage.hasPrevious());
        model.addAttribute("hasNext", expensePage.hasNext());
        model.addAttribute("previousPage", Math.max(requestedPage - 1, 0));
        model.addAttribute("nextPage", Math.min(requestedPage + 1, Math.max(expensePage.getTotalPages() - 1, 0)));
        model.addAttribute("keyword", normalizeFilter(keyword));
        model.addAttribute("category", normalizeFilter(category));
        model.addAttribute("filterYear", filterYear);
        model.addAttribute("filterMonth", normalizeMonth(filterMonth));
        model.addAttribute("hasFilters", hasFilters(keyword, category, filterYear, filterMonth));
        model.addAttribute("activePage", "expenses");
        model.addAttribute("currentUrl", buildExpenseUrl(requestedPage, keyword, category, filterYear, filterMonth));
        return "index";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(required = false) String returnUrl) {
        String fallbackUrl = "/expenses?page=" + Math.max(page, 0);
        model.addAttribute("expense", new Expense());
        model.addAttribute("page", Math.max(page, 0));
        model.addAttribute("returnUrl", safeReturnUrl(returnUrl, fallbackUrl));
        model.addAttribute("activePage", "new");
        return "form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               Model model,
                               Authentication auth,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(required = false) String returnUrl) {
        String fallbackUrl = "/expenses?page=" + Math.max(page, 0);
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        ensureOwner(expense, user);
        model.addAttribute("expense", expense);
        model.addAttribute("page", Math.max(page, 0));
        model.addAttribute("returnUrl", safeReturnUrl(returnUrl, fallbackUrl));
        model.addAttribute("activePage", "new");
        return "form";
    }

    @PostMapping("/save")
    public String saveExpense(@ModelAttribute Expense expense,
                              Authentication auth,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(required = false) String returnUrl,
                              RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isNew = expense.getId() == null;
        if (!isNew) {
            Expense existing = expenseRepository.findById(expense.getId())
                    .orElseThrow(() -> new RuntimeException("Expense not found"));
            ensureOwner(existing, user);
        }
        expense.setUser(user);
        expenseRepository.save(expense);
        redirectAttributes.addFlashAttribute("successMessage", isNew ? "Expense saved successfully." : "Expense updated successfully.");
        return "redirect:" + safeReturnUrl(returnUrl, "/expenses?page=" + Math.max(page, 0));
    }

    @GetMapping("/delete/{id}")
    public String deleteExpense(@PathVariable Long id,
                                Authentication auth,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "") String keyword,
                                @RequestParam(defaultValue = "") String category,
                                @RequestParam(required = false) Integer filterYear,
                                @RequestParam(required = false) Integer filterMonth,
                                RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        ensureOwner(expense, user);
        expenseRepository.delete(expense);
        redirectAttributes.addFlashAttribute("successMessage", "Expense deleted successfully.");
        return "redirect:" + buildExpenseUrl(page, keyword, category, filterYear, filterMonth);
    }

    @GetMapping("/expenses/export")
    public void exportExpenses(Authentication auth,
                               @RequestParam(defaultValue = "") String keyword,
                               @RequestParam(defaultValue = "") String category,
                               @RequestParam(required = false) Integer filterYear,
                               @RequestParam(required = false) Integer filterMonth,
                               HttpServletResponse response) throws IOException {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=expenses.csv");
        response.getWriter().println("No,Title,Category,Quantity,Unit Cost (FRW),Total (FRW),Date");

        int rowNumber = 1;
        for (Expense expense : getFilteredExpenses(user, keyword, category, filterYear, filterMonth)) {
            response.getWriter().printf("%s,%s,%s,%s,%s,%s,%s%n",
                    rowNumber++,
                    csv(expense.getTitle()),
                    csv(expense.getCategory()),
                    csv(expense.getAmount()),
                    csv(expense.getUnitCost()),
                    csv(expense.getTotal()),
                    csv(expense.getDate()));
        }
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("activePage", "about");
        model.addAttribute("currentUrl", "/about");
        return "about";
    }

    private Page<Expense> findExpensePage(User user, int page, String keyword, String category, Integer filterYear, Integer filterMonth) {
        List<Expense> filteredExpenses = getFilteredExpenses(user, keyword, category, filterYear, filterMonth);
        int start = Math.min(page * PAGE_SIZE, filteredExpenses.size());
        int end = Math.min(start + PAGE_SIZE, filteredExpenses.size());
        return new PageImpl<>(filteredExpenses.subList(start, end), PageRequest.of(page, PAGE_SIZE), filteredExpenses.size());
    }

    private List<Expense> getFilteredExpenses(User user, String keyword, String category, Integer filterYear, Integer filterMonth) {
        String normalizedKeyword = normalizeFilter(keyword).toLowerCase();
        String normalizedCategory = normalizeFilter(category).toLowerCase();
        Integer normalizedMonth = normalizeMonth(filterMonth);

        return expenseRepository.findByUser(user).stream()
                .filter(expense -> normalizedKeyword.isBlank()
                        || safeLower(expense.getTitle()).contains(normalizedKeyword)
                        || safeLower(expense.getCategory()).contains(normalizedKeyword))
                .filter(expense -> normalizedCategory.isBlank() || safeLower(expense.getCategory()).contains(normalizedCategory))
                .filter(expense -> filterYear == null || (expense.getDate() != null && expense.getDate().getYear() == filterYear))
                .filter(expense -> normalizedMonth == null || (expense.getDate() != null && expense.getDate().getMonthValue() == normalizedMonth))
                .sorted(Comparator.comparing((Expense expense) -> expense.getDate() == null ? LocalDate.MIN : expense.getDate())
                        .thenComparing(expense -> expense.getId() == null ? 0L : expense.getId())
                        .reversed())
                .toList();
    }

    private List<Integer> getPageNumbers(int currentPage, int totalPages) {
        if (totalPages <= 0) {
            return List.of();
        }

        int startPage = Math.max(0, currentPage - 2);
        int endPage = Math.min(totalPages - 1, currentPage + 2);

        return IntStream.rangeClosed(startPage, endPage)
                .boxed()
                .toList();
    }

    private String safeReturnUrl(String returnUrl, String fallbackUrl) {
        if (returnUrl == null || returnUrl.isBlank()) {
            return fallbackUrl;
        }

        if (!returnUrl.startsWith("/") || returnUrl.startsWith("//") || returnUrl.contains("\r") || returnUrl.contains("\n")) {
            return fallbackUrl;
        }

        return returnUrl;
    }

    private boolean hasFilters(String keyword, String category, Integer filterYear, Integer filterMonth) {
        return !normalizeFilter(keyword).isBlank()
                || !normalizeFilter(category).isBlank()
                || filterYear != null
                || normalizeMonth(filterMonth) != null;
    }

    private String buildExpenseUrl(int page, String keyword, String category, Integer filterYear, Integer filterMonth) {
        StringBuilder url = new StringBuilder("/expenses?page=").append(Math.max(page, 0));
        appendParam(url, "keyword", normalizeFilter(keyword));
        appendParam(url, "category", normalizeFilter(category));
        appendParam(url, "filterYear", filterYear == null ? "" : filterYear.toString());
        Integer normalizedMonth = normalizeMonth(filterMonth);
        appendParam(url, "filterMonth", normalizedMonth == null ? "" : normalizedMonth.toString());
        return url.toString();
    }

    private void appendParam(StringBuilder url, String name, String value) {
        if (!value.isBlank()) {
            url.append("&").append(name).append("=").append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        }
    }

    private String normalizeFilter(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private Integer normalizeMonth(Integer month) {
        if (month == null || month < 1 || month > 12) {
            return null;
        }
        return month;
    }

    private String csv(Object value) {
        String text = value == null ? "" : value.toString();
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private void ensureOwner(Expense expense, User user) {
        if (expense.getUser() == null || user.getId() == null || !user.getId().equals(expense.getUser().getId())) {
            throw new RuntimeException("Expense not found");
        }
    }
}
