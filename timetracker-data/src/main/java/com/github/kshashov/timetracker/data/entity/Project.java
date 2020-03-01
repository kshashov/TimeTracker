package com.github.kshashov.timetracker.data.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "projects", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "projects_unique_title", columnNames = "title")
})
public class Project implements BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "title")
    private String title;

    @NotNull
    @Column(name = "active")
    private Boolean isActive;

    @OneToMany(mappedBy = "project")
    private Set<Action> actions;
}
