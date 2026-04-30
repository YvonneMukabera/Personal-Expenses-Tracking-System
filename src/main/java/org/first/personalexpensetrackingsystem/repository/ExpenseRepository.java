package org.first.personalexpensetrackingsystem.repository;

import org.first.personalexpensetrackingsystem.model.Expense;
import org.first.personalexpensetrackingsystem.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Page<Expense> findByUser_Name(String name, Pageable pageable);

    List<Expense> findByUser(User user);

    Page<Expense> findByUser(User user, Pageable pageable);
}
