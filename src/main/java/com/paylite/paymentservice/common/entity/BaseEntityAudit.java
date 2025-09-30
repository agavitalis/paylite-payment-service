package com.paylite.paymentservice.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@MappedSuperclass
public class BaseEntityAudit extends BaseEntity implements Serializable {

    @Column(name = "isDeleted", columnDefinition = "boolean default false")
    private Boolean isDeleted;

    @Column(name = "deletedAt")
    private LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntityAudit)) return false;
        if (!super.equals(o)) return false;
        BaseEntityAudit that = (BaseEntityAudit) o;
        return isDeleted.equals(that.isDeleted) &&
                deletedAt.equals(that.deletedAt) &&
                createdAt.equals(that.createdAt) &&
                updatedAt.equals(that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                isDeleted, deletedAt, createdAt, updatedAt);
    }

    @PrePersist
    public void prePersist() {
        if (isDeleted == null) isDeleted = false;
    }

}
