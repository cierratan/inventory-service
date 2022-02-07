package com.sunright.inventory.repository;

import com.sunright.inventory.entity.itembatc.ItemBatc;
import com.sunright.inventory.entity.itembatc.ItemBatcId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ItemBatcRepository extends JpaRepository<ItemBatc, ItemBatcId> {

    @Modifying
    @Query("UPDATE ITEMBATC i " +
            "SET i.qoh = :balance " +
            "WHERE i.id.companyCode = :companyCode and i.id.plantNo = :plantNo and i.id.batchNo = :batchNo " +
            " and i.id.itemNo = :itemNo and i.id.loc = :loc")
    void updateQoh(BigDecimal balance, String companyCode, Integer plantNo, Long batchNo, String itemNo, String loc);
}
