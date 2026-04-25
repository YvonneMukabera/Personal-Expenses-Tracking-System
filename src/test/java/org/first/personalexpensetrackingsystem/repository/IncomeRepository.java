package org.first.personalexpensetrackingsystem.repository;

import org.first.personalexpensetrackingsystem.model.Income;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IncomeRepository extends JpaRepository<Income, Long> {

    Optional<Income> findByYearAndMonth(int year, int month);
}