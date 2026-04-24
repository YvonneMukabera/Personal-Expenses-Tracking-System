package org.first.personalexpensetrackingsystem.service;

import org.first.personalexpensetrackingsystem.model.Expense;
import org.first.personalexpensetrackingsystem.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository repo;

    public ExpenseService(ExpenseRepository repo) {
        this.repo = repo;
    }

    public List<Expense> getAllExpenses() {
        return repo.findAll();
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