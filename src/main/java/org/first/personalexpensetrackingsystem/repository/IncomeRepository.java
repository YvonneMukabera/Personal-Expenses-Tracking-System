package org.first.personalexpensetrackingsystem.repository;

import org.first.personalexpensetrackingsystem.model.Income;
import org.first.personalexpensetrackingsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {
    Income findByUserAndYearAndMonth(User user, int year, int month);
}
