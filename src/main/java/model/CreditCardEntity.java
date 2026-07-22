package model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cartao")
public class CreditCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String name;

    @Column(name = "limite", nullable = false, precision = 19, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "dia_fechamento", nullable = false)
    private int closingDay;

    @Column(name = "dia_vencimento", nullable = false)
    private int dueDay;

    @Column(name = "ativo", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected CreditCardEntity() {
    }

    public CreditCardEntity(String name, BigDecimal creditLimit, int closingDay, int dueDay) {
        this.name = name;
        this.creditLimit = creditLimit;
        this.closingDay = closingDay;
        this.dueDay = dueDay;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public int getClosingDay() {
        return closingDay;
    }

    public void setClosingDay(int closingDay) {
        this.closingDay = closingDay;
    }

    public int getDueDay() {
        return dueDay;
    }

    public void setDueDay(int dueDay) {
        this.dueDay = dueDay;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
