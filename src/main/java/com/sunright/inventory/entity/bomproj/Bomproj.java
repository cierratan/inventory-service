package com.sunright.inventory.entity.bomproj;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name = "BOMPROJ")
@Data
@NoArgsConstructor
public class Bomproj implements Serializable {

    @EmbeddedId
    private BomprojId id;

    private String projectNo;
    private String orderNo;

    @Column(name = "ORDER_QTY", precision = 16, scale = 4)
    private BigDecimal orderQty;

    private String openClose;
    private String pickedStatus;
    private Date mbiDate;
    private String sivNo;
    private Date explodeDate;
    private Date resvDate;
    private String loc;
}
