package com.sunright.inventory.repository;

import com.sunright.inventory.entity.bombypj.BombypjDet;
import com.sunright.inventory.entity.bombypj.BombypjDetailProjection;
import com.sunright.inventory.entity.bombypj.BombypjId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface BombypjDetailRepository extends JpaRepository<BombypjDet, BombypjId>, JpaSpecificationExecutor<BombypjDet> {

    @Query("SELECT b.tranType as tranType, b.id.projectNo as projectNo, b.id.orderNo as orderNo, " +
            "b.id.assemblyNo as assemblyNo, b.grnNo as grnNo, b.resvQty as resvQty, coalesce(b.accumRecdQty, 0) as accumRecdQty, " +
            "(b.resvQty - coalesce(b.accumRecdQty, 0)) as balQty, coalesce(b.recdQty,0) as recdQty " +
            "FROM BOMBYPJ_DET b WHERE b.id.companyCode = :companyCode AND b.id.plantNo = :plantNo AND b.poNo = :poNo " +
            "AND b.id.alternate = :itemNo AND b.seqNo = :seqNo AND coalesce(b.status,'O') not in ('C', 'V') " +
            "AND (b.tranType <> 'APP' or b.tranType = 'APP')")
    BombypjDetailProjection getBombypjDetCur(String companyCode, Integer plantNo, String poNo, String itemNo, Integer seqNo);

    @Modifying
    @Query("UPDATE BOMBYPJ_DET b set b.accumRecdQty = :accumRecdQty, b.recdQty = :recdQty, b.status = :status, " +
            "b.grnNo = :grnNo WHERE b.id.alternate = :itemNo")
    void updateAccRecdStatusGrnNo(BigDecimal accumRecdQty, BigDecimal recdQty, String status, String grnNo, String itemNo);
}
