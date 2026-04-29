package org.first.personalexpensetrackingsystem.controller;

import org.first.personalexpensetrackingsystem.model.Expense;
import org.first.personalexpensetrackingsystem.service.ExpenseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    // HOME PAGE WITH PAGINATION
    @GetMapping("/")
    public String viewHomePage(Model model,
                               @RequestParam(defaultValue = "0") int page) {

        int pageSize = 30;

        Pageable pageable = PageRequest.of(page, pageSize);

        Page<Expense> expensePage = service.getAllExpenses(pageable);

        model.addAttribute("expenses", expensePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", expensePage.getTotalPages());
        model.addAttribute("pageSize", pageSize);

        return "index";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model,
                                 @RequestParam(required = false, defaultValue = "0") int page) {

        model.addAttribute("expense", new Expense());
        model.addAttribute("page", page);

        return "form";
    }

    // SAVE (ADD + UPDATE) → RETURN TO SAME PAGE
    @PostMapping("/save")
    public String saveExpense(@ModelAttribute Expense expense,
                              @RequestParam(required = false, defaultValue = "0") int page) {

        service.saveExpense(expense);

        return "redirect:/?page=" + page;
    }

    // DELETE → RETURN TO SAME PAGE
    @GetMapping("/delete/{id}")
    public String deleteExpense(@PathVariable Long id,
                                @RequestParam(required = false, defaultValue = "0") int page) {

        service.deleteExpense(id);

        return "redirect:/?page=" + page;
    }

    // EDIT → KEEP PAGE CONTEXT
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               @RequestParam(required = false, defaultValue = "0") int page,
                               Model model) {

        Expense expense = service.getExpenseById(id);

        if (expense == null) {
            return "redirect:/";
        }

        model.addAttribute("expense", expense);
        model.addAttribute("page", page);

        return "form";
    }

    @GetMapping("/about")
    public String showAboutPage() {
        return "about"; // This matches your templates/about.html file name
    }
}