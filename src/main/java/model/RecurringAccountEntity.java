package model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "conta_recorrente")
public class RecurringAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "descricao", nullable = false, length = 160)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private MovementType type;

    @Column(name = "valor", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id")
    private CategoryEntity category;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequencia", nullable = false, length = 20)
    private RecurrenceFrequency frequency;

    @Column(name = "intervalo_dias")
    private Integer customIntervalDays;

    @Column(name = "proxima_ocorrencia", nullable = false)
    private LocalDate nextOccurrence;

    @Column(name = "ativa", nullable = false)
    private boolean active = true;

    @Column(name = "observacoes", length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected RecurringAccountEntity() {
    }

    public RecurringAccountEntity(String description, MovementType type, BigDecimal amount, CategoryEntity category,
                                  RecurrenceFrequency frequency, Integer customIntervalDays, LocalDate nextOccurrence,
                                  String notes) {
        this.description = description;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.frequency = frequency;
        this.customIntervalDays = customIntervalDays;
        this.nextOccurrence = nextOccurrence;
        this.notes = notes;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MovementType getType() {
        return type;
    }

    public void setType(MovementType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }

    public RecurrenceFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(RecurrenceFrequency frequency) {
        this.frequency = frequency;
    }

    public Integer getCustomIntervalDays() {
        return customIntervalDays;
    }

    public void setCustomIntervalDays(Integer customIntervalDays) {
        this.customIntervalDays = customIntervalDays;
    }

    public LocalDate getNextOccurrence() {
        return nextOccurrence;
    }

    public void setNextOccurrence(LocalDate nextOccurrence) {
        this.nextOccurrence = nextOccurrence;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCategoryName() {
        return category == null ? "-" : category.getName();
    }
}
