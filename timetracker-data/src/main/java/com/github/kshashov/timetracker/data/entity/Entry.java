package com.github.kshashov.timetracker.data.entity;

import com.github.kshashov.timetracker.data.entity.user.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "entries", schema = "public")
public class Entry implements BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "obs", nullable = false)
    private Date obs;

    @NotNull
    @Column(name = "hours", scale = 1, nullable = false)
    private Double hours;

    @NotNull
    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "closed")
    private Boolean isClosed;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "action_id", nullable = false)
    private Action action;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
