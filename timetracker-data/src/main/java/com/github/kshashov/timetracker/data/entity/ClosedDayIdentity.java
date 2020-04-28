package com.github.kshashov.timetracker.data.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Embeddable
public class ClosedDayIdentity implements Serializable {

    @NotNull
    @Column(name = "obs")
    private Date obs;

    @NotNull
    @Column(name = "project_id")
    private Long projectId;
}