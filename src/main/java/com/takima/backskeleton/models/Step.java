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
}
