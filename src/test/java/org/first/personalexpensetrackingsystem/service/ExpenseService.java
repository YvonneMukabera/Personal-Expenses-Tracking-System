package org.first.personalexpensetrackingsystem.service;

import org.first.personalexpensetrackingsystem.model.Expense;
import org.first.personalexpensetrackingsystem.repository.ExpenseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository repo;

    public ExpenseService(ExpenseRepository repo) {
        this.repo = repo;
    }

    // EXISTING (unchanged)
    public List<Expense> getAllExpenses() {
        return repo.findAll();
    }

    // NEW (for pagination)
    public Page<Expense> getAllExpenses(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public void saveExpense(Expense expense) {
        repo.save(expense);
    }

    public void deleteExpense(Long id) {
        repo.deleteById(id);
    }

    public Expense getExpenseById(Long id) {
        return repo.findById(id).orElse(null);
    }
}