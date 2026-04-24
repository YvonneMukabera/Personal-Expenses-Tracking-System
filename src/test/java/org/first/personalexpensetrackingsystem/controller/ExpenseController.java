package org.first.personalexpensetrackingsystem.controller;

import org.first.personalexpensetrackingsystem.model.Expense;
import org.first.personalexpensetrackingsystem.service.ExpenseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    // HOME PAGE
    @GetMapping("/")
    public String viewHomePage(Model model) {
        model.addAttribute("expenses", service.getAllExpenses());
        return "index";
    }

    // SHOW ADD FORM
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("expense", new Expense());
        return "form";
    }

    // SAVE (CREATE + UPDATE)
    @PostMapping("/save")
    public String saveExpense(@ModelAttribute Expense expense) {
        service.saveExpense(expense);
        return "redirect:/";
    }

    // DELETE
    @GetMapping("/delete/{id}")
    public String deleteExpense(@PathVariable Long id) {
        service.deleteExpense(id);
        return "redirect:/";
    }

    // SHOW EDIT FORM
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {

        Expense expense = service.getExpenseById(id);

        // IMPORTANT: prevent null crash
        if (expense == null) {
            return "redirect:/";
        }

        model.addAttribute("expense", expense);
        return "form";
    }
}