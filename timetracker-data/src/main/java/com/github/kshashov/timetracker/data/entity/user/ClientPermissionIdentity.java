package com.github.kshashov.timetracker.data.entity.user;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Embeddable
public class ClientPermissionIdentity implements Serializable {
    @NotNull
    @Column(name = "client_id")
    private Long clientId;

    @NotNull
    @Column(name = "user_id")
    private Long userId;
}
