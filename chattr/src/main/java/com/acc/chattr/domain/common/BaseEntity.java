package com.acc.chattr.domain.common;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public abstract class BaseEntity {
    private Instant createdAt;
    private Instant deletedAt;

    public void initCreatedAt() {
        this.createdAt = Instant.now();
    }

    public void delete() {
        this.deletedAt = Instant.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
