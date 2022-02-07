package com.sunright.inventory.repository;

import com.sunright.inventory.entity.ItemLoc;
import com.sunright.inventory.entity.ItemLocProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Repository
public interface ItemLocRepository extends JpaRepository<ItemLoc, Long> {
    List<ItemLoc> findByCompanyCodeAndPlantNoAndItemNoAndLoc(String companyCode, Integer plantNo, String itemNo, String loc);

    @Modifying
    @Query("UPDATE ITEMLOC i set i.pickedQty = :pickedQty, i.mrvResv = :mrvResv, i.prodnResv = :prodnResv " +
            "WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.itemNo = :itemNo and i.loc = :loc ")
    void updatePickedQtyMrvResvProdnResv(BigDecimal pickedQty, BigDecimal mrvResv, BigDecimal prodnResv, String companyCode, Integer plantNo, String itemNo, String loc);

    @Modifying
    @Query("UPDATE ITEMLOC i set i.qoh = :qoh, i.ytdProd = :ytdProd, i.ytdIssue = :ytdIssue, i.lastTranDate = :lastTranDate  " +
            "WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.itemNo = :itemNo and i.loc = :loc ")
    void updateQohYtdProdYtdIssueLastTranDate(BigDecimal qoh, BigDecimal ytdProd, BigDecimal ytdIssue,
                                              Date lastTranDate, String companyCode, Integer plantNo, String itemNo, String loc);

    @Query("select (coalesce (i.qoh, 0) - coalesce(i.pickedQty, 0) - coalesce(i.rpcResv, 0) - coalesce(i.mrvResv, 0)) as availQty " +
            "from ITEMLOC i where i.companyCode = :companyCode and i.plantNo = :plantNo " +
            "and i.itemNo = :itemNo and i.loc = (select c.stockLoc from COMPANY c " +
            "where c.id.companyCode = :companyCode and c.id.plantNo = :plantNo)")
    ItemLocProjection getAvailQtyByItemNo(String companyCode, Integer plantNo, String itemNo);
}
