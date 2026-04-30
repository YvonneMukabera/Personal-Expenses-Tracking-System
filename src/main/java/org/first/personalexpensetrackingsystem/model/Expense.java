package org.first.personalexpensetrackingsystem.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "expense")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private Double amount;
    private Double unitCost;
    private String category;
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Double getTotal() {
        double quantity = this.amount != null ? this.amount : 0.0;
        if (this.unitCost == null) {
            return quantity;
        }
        return quantity * this.unitCost;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title != null ? title : description; }
    public void setTitle(String title) {
        this.title = title;
        this.description = title;
    }
    public String getDescription() { return description != null ? description : title; }
    public void setDescription(String description) {
        this.description = description;
        if (this.title == null) {
            this.title = description;
        }
    }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public Double getUnitCost() { return unitCost; }
    public void setUnitCost(Double unitCost) { this.unitCost = unitCost; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
