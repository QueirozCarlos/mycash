package model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "categoria")
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 40)
    private CategoryType type;

    @Column(name = "cor_hex", length = 16)
    private String colorHex;

    @Column(name = "ativa", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected CategoryEntity() {
    }

    public CategoryEntity(String name, CategoryType type, String colorHex) {
        this.name = name;
        this.type = type;
        this.colorHex = colorHex;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CategoryType getType() {
        return type;
    }

    public String getColorHex() {
        return colorHex;
    }

    public boolean isActive() {
        return active;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public void setActive(boolean active) {
        this.active = active;
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
}