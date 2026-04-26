package org.first.personalexpensetrackingsystem.service;

import org.first.personalexpensetrackingsystem.model.Income;
import org.first.personalexpensetrackingsystem.repository.IncomeRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IncomeService {

    private final IncomeRepository repo;

    public IncomeService(IncomeRepository repo) {
        this.repo = repo;
    }

    // Save or update income
    public Income saveIncome(Income income) {
        return repo.save(income);
    }

    // Get income for a specific month/year
    public Income getIncome(int year, int month) {
        return repo.findByYearAndMonth(year, month)
                .orElse(null);
    }
}