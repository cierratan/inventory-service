package com.sunright.inventory.entity.lov;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class Country {
    @Id
    private String countryCode;

    private String description;

    @Column(name = "LETTER_2")
    private String letter2;

    @Column(name = "DIGIT_3")
    private String digit3;
    private String wtPct;
    private String remarks;
}
