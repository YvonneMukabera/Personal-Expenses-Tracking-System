package org.first.personalexpensetrackingsystem.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String category;
    private int amount;
    private Double unitCost;
    private LocalDate date;
    private String description;


    public Expense() {}

    public Expense(String title, String category, int amount,Double unitCost,LocalDate date, String description) {
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.unitCost=unitCost;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public Double getUnitCost() { return unitCost; }
    public void setUnitCost(Double unitCost) { this.unitCost = unitCost; }
    

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getTotal() {
        if (unitCost == null) return 0.0;
        return amount * unitCost;
    }
}