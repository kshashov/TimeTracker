package com.github.kshashov.timetracker.data.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Entity
@Table(name = "actions", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "actions_unique_project_id_title", columnNames = {"project_id", "title"})
})
@NamedEntityGraph(name = "Action.project", attributeNodes = @NamedAttributeNode("project"))
public class Action implements BaseEntity {
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

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;
}
