package com.takima.backskeleton.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** Une place dans le conseil qu'un joueur a composé. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_council")
public class CouncilSeat {
    @EmbeddedId
    private Key id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @MapsId("sageId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sage_id")
    private Sage sage;

    @Column(name = "seat")
    private Integer seat;

    public CouncilSeat(User user, Sage sage, int seat) {
        this.id = new Key(user.getId(), sage.getId());
        this.user = user;
        this.sage = sage;
        this.seat = seat;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Key implements Serializable {
        @Column(name = "user_id")
        private Integer userId;

        @Column(name = "sage_id")
        private Integer sageId;
    }
}
