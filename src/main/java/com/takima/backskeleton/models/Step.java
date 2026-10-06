package com.takima.backskeleton.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Step")
public class Step {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "step_id")
    private Long id;

    @Column (name = "step_order")
    private Integer order;

    @Column(name = "step_name")
    private String name;

    @Column(name = "step_description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_status")
    private StepStatus status = StepStatus.PENDING;

    @CreationTimestamp
    @Column(name = "step_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "step_updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;




    protected Step() {
    }

    // Constructeur pratique pour créer une nouvelle étape dans le service.
    // Pas d'id, de statut ni de dates : ils sont générés automatiquement.
    public Step(Session session, Integer stepOrder, String name, String description) {
        this.session = session;
        this.stepOrder = stepOrder;
        this.name = name;
        this.description = description;
    }

    // ------------------------------------------------------------------
    // Getters / setters
    // Pas de setter pour id, createdAt et updatedAt : ils sont gérés
    // par la base et Hibernate, le code ne doit pas les modifier.
    // ------------------------------------------------------------------

    public Integer getId() {
        return id;
    }

    public Integer getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public StepStatus getStatus() {
        return status;
    }

    public void setStatus(StepStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
