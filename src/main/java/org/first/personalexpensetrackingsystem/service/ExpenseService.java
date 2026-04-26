package org.first.personalexpensetrackingsystem.service;

import org.first.personalexpensetrackingsystem.model.Expense;
import org.first.personalexpensetrackingsystem.repository.ExpenseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    // ===================== PAGINATED VERSION (USED IN CONTROLLER) =====================
    public Page<Expense> getAllExpenses(Pageable pageable) {
        return repository.findAll(pageable);
    }

    // ===================== LEGACY SUPPORT (IMPORTANT FIX) =====================
    // prevents old code from breaking anywhere else in project
    public List<Expense> getAllExpenses() {
        return repository.findAll();
    }

    public Expense getExpenseById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Expense saveExpense(Expense expense) {
        return repository.save(expense);
    }

    public void deleteExpense(Long id) {
        repository.deleteById(id);
    }
}