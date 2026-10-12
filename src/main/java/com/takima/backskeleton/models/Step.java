package com.takima.backskeleton.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Step")
public class Step {

    // 1. Les attributs
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

    // 2. Le constructeur, juste après le dernier attribut
    public Step(Session session, Integer stepOrder, String name, String description) {
        this.session = session;
        this.order = stepOrder;
        this.name = name;
        this.description = description;
    }

}
