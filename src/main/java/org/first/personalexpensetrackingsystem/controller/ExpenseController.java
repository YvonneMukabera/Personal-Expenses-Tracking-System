package org.first.personalexpensetrackingsystem.controller;

import org.first.personalexpensetrackingsystem.model.Expense;
import org.first.personalexpensetrackingsystem.model.User;
import org.first.personalexpensetrackingsystem.repository.ExpenseRepository;
import org.first.personalexpensetrackingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.IntStream;

@Controller
public class ExpenseController {

    private static final int PAGE_SIZE = 5;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String listExpenses(Model model, Authentication auth,
                               @RequestParam(defaultValue = "0") int page) {

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        int requestedPage = Math.max(page, 0);
        Page<Expense> expensePage = findExpensePage(user, requestedPage);

        if (expensePage.getTotalPages() > 0 && requestedPage >= expensePage.getTotalPages()) {
            requestedPage = expensePage.getTotalPages() - 1;
            expensePage = findExpensePage(user, requestedPage);
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
        model.addAttribute("activePage", "expenses");
        model.addAttribute("currentUrl", "/?page=" + requestedPage);
        return "index";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(required = false) String returnUrl) {
        String fallbackUrl = "/?page=" + Math.max(page, 0);
        model.addAttribute("expense", new Expense());
        model.addAttribute("page", Math.max(page, 0));
        model.addAttribute("returnUrl", safeReturnUrl(returnUrl, fallbackUrl));
        model.addAttribute("activePage", "new");
        return "form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(required = false) String returnUrl) {
        String fallbackUrl = "/?page=" + Math.max(page, 0);
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
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
                              @RequestParam(required = false) String returnUrl) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        expense.setUser(user);
        expenseRepository.save(expense);
        return "redirect:" + safeReturnUrl(returnUrl, "/?page=" + Math.max(page, 0));
    }

    @GetMapping("/delete/{id}")
    public String deleteExpense(@PathVariable Long id,
                                @RequestParam(defaultValue = "0") int page) {
        expenseRepository.deleteById(id);
        return "redirect:/?page=" + page;
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("activePage", "about");
        model.addAttribute("currentUrl", "/about");
        return "about";
    }

    private Page<Expense> findExpensePage(User user, int page) {
        Sort sort = Sort.by(Sort.Direction.DESC, "date", "id");
        return expenseRepository.findByUser(user, PageRequest.of(page, PAGE_SIZE, sort));
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
}
