package com.sunright.inventory.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;

@MappedSuperclass
@Data
@NoArgsConstructor
public class BaseEntity implements Serializable {
    @Enumerated(EnumType.ORDINAL)
    private Status status;

    @Column(updatable = false)
    private String createdBy;

    @Column(name = "CREATED_AT", columnDefinition = "TIMESTAMP WITH TIME ZONE", updatable = false)
    private ZonedDateTime createdAt;

    private String updatedBy;

    @Column(name = "UPDATED_AT", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime updatedAt;
}
