package com.github.kshashov.timetracker.data.entity.user;


import com.github.kshashov.timetracker.data.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Entity
@Table(name = "users", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "users_unique_email", columnNames = "email")
})
public class User implements BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "email")
    private String email;

    @NotNull
    @Column(name = "name")
    private String name;
}
