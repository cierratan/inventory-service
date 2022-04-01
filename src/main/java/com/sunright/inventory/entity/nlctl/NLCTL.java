package com.sunright.inventory.entity.nlctl;

import com.sunright.inventory.entity.base.BaseIdEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name = "NLCTL")
@Data
@NoArgsConstructor
public class NLCTL {

    @EmbeddedId
    private BaseIdEntity id;

    @Column(name = "START_PERIOD")
    private Date startPeriod;

    @Column(name = "START_YEAR", precision = 4, scale = 0)
    private BigDecimal startYear;

    private String inventoryEnabled;
    private Integer inventoryMonth;
    private Long inventoryYear;
}
