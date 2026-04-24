package org.first.personalexpensetrackingsystem.repository;

import org.first.personalexpensetrackingsystem.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
}