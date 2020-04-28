package com.github.kshashov.timetracker.data.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "closed_days", schema = "public")
public class ClosedDay {

    @EmbeddedId
    private ClosedDayIdentity identity;

    @MapsId("project_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Project project;
}
