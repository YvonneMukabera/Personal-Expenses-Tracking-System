package org.first.personalexpensetrackingsystem.service;

import org.first.personalexpensetrackingsystem.model.Income;
import org.first.personalexpensetrackingsystem.model.User;
import org.first.personalexpensetrackingsystem.repository.IncomeRepository;
import org.springframework.stereotype.Service;

@Service
public class IncomeService {

    private final IncomeRepository incomeRepository;

    public IncomeService(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    public Income getIncome(User user, int year, int month) {
        return incomeRepository.findByUserAndYearAndMonth(user, year, month);
    }

    public void saveIncome(Income income) {
        incomeRepository.save(income);
    }
}