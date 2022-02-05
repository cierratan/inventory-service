package com.sunright.inventory.repository;

import com.sunright.inventory.entity.ItemLoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ItemLocRepository extends JpaRepository<ItemLoc, Long> {
    List<ItemLoc> findByCompanyCodeAndPlantNoAndItemNoAndLoc(String companyCode, Integer plantNo, String itemNo, String loc);
    ItemLoc findByCompanyCodeAndPlantNoAndItemNo(String companyCode, Integer plantNo, String itemNo);

    @Modifying
    @Query("UPDATE ITEMLOC i set i.pickedQty = :pickedQty, i.mrvResv = :mrvResv, i.prodnResv = :prodnResv " +
            "WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.itemNo = :itemNo ")
    void updatePickedQtyMrvResvProdnResv(BigDecimal pickedQty, BigDecimal mrvResv, BigDecimal prodnResv, String companyCode, Integer plantNo, String itemNo);
}
