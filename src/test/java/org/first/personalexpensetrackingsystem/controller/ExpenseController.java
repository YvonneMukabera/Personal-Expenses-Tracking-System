package org.first.personalexpensetrackingsystem.controller;

import org.first.personalexpensetrackingsystem.model.Expense;
import org.first.personalexpensetrackingsystem.service.ExpenseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    // HOME PAGE (UPDATED FOR PAGINATION)
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

        if (expense == null) {
            return "redirect:/";
        }

        model.addAttribute("expense", expense);
        return "form";
    }
    //financial dashboard

}