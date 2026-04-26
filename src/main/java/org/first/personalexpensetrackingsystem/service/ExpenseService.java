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

    // PAGINATED VERSION
    public Page<Expense> getAllExpenses(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public List<Expense> getAllExpenses() {
        return repository.findAll();
    }

    public Expense getExpenseById(Long id) {
        return repository.findById(id).orElse(null);
    }

    //  FIXED SAVE LOGIC
    public Expense saveExpense(Expense expense) {

        if (expense.getId() != null) {

            Expense existing = repository.findById(expense.getId()).orElse(null);

            if (existing != null) {

                existing.setTitle(expense.getTitle());
                existing.setCategory(expense.getCategory());
                existing.setAmount(expense.getAmount());
                existing.setUnitCost(expense.getUnitCost());
                existing.setDescription(expense.getDescription());

                // IMPORTANT FIX: keep old date if new one is null
                if (expense.getDate() != null) {
                    existing.setDate(expense.getDate());
                }

                return repository.save(existing);
            }
        }

        // NEW EXPENSE
        return repository.save(expense);
    }

    public void deleteExpense(Long id) {
        repository.deleteById(id);
    }
}