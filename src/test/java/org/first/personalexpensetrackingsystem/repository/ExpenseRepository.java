package org.first.personalexpensetrackingsystem.repository;

import org.first.personalexpensetrackingsystem.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
}