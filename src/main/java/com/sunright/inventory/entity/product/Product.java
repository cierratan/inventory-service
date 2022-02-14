package com.sunright.inventory.entity.product;

import com.sunright.inventory.entity.pur.PurId;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity(name = "PRODUCT")
@Data
@NoArgsConstructor
public class Product implements Serializable {

    @EmbeddedId
    private ProductId id;

    private String wipTracking;
}
