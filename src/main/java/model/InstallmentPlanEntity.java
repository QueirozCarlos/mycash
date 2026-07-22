package model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "parcelamento")
public class InstallmentPlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cartao_id")
    private CreditCardEntity creditCard;

    @Column(name = "descricao", nullable = false, length = 160)
    private String description;

    @Column(name = "valor_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "quantidade_parcelas", nullable = false)
    private int installmentCount;

    @Column(name = "valor_parcela", nullable = false, precision = 19, scale = 2)
    private BigDecimal installmentAmount;

    @Column(name = "parcelas_pagas", nullable = false)
    private int paidInstallments;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate startDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id")
    private CategoryEntity category;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected InstallmentPlanEntity() {
    }

    public InstallmentPlanEntity(CreditCardEntity creditCard, String description, BigDecimal totalAmount,
                                 int installmentCount, BigDecimal installmentAmount, LocalDate startDate,
                                 CategoryEntity category) {
        this.creditCard = creditCard;
        this.description = description;
        this.totalAmount = totalAmount;
        this.installmentCount = installmentCount;
        this.installmentAmount = installmentAmount;
        this.paidInstallments = 0;
        this.startDate = startDate;
        this.category = category;
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

    public CreditCardEntity getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCardEntity creditCard) {
        this.creditCard = creditCard;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getInstallmentCount() {
        return installmentCount;
    }

    public void setInstallmentCount(int installmentCount) {
        this.installmentCount = installmentCount;
    }

    public BigDecimal getInstallmentAmount() {
        return installmentAmount;
    }

    public void setInstallmentAmount(BigDecimal installmentAmount) {
        this.installmentAmount = installmentAmount;
    }

    public int getPaidInstallments() {
        return paidInstallments;
    }

    public void setPaidInstallments(int paidInstallments) {
        this.paidInstallments = paidInstallments;
    }

    public int getPendingInstallments() {
        return Math.max(0, installmentCount - paidInstallments);
    }

    public BigDecimal getRemainingAmount() {
        return installmentAmount.multiply(BigDecimal.valueOf(getPendingInstallments()));
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }

    public String getCreditCardName() {
        return creditCard == null ? "-" : creditCard.getName();
    }
}
