package com.sunright.inventory.repository;

import com.sunright.inventory.entity.bombypj.Bombypj;
import com.sunright.inventory.entity.bombypj.BombypjId;
import com.sunright.inventory.entity.bombypj.BombypjProjection;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BombypjRepository extends JpaRepository<Bombypj, BombypjId>, JpaSpecificationExecutor<Bombypj> {

    @Query("SELECT DISTINCT b.id.projectNo as projectNo FROM BOMBYPJ b WHERE b.id.companyCode = :companyCode " +
            "AND b.id.plantNo = :plantNo AND b.id.projectNo = :projectNo")
    BombypjProjection getPrjNo(String companyCode, Integer plantNo, String projectNo);

    @Query("SELECT b.id.alternate as alternate FROM BOMBYPJ b WHERE b.id.companyCode = :companyCode " +
            "AND b.id.plantNo = :plantNo AND b.id.projectNo = :projectNo AND b.id.alternate = :itemNo " +
            "AND COALESCE(b.statuz, 'R') NOT IN ('D', 'X')")
    BombypjProjection getAltrnt(String companyCode, Integer plantNo, String projectNo, String itemNo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    @Query("SELECT b.mrvResv as mrvResv, b.id.orderNo as orderNo, " +
            "b.id.assemblyNo as assemblyNo, b.id.component as component, b.id.alternate as alternate, b.id.projectNo as projectNo, " +
            "b.resvQty as resvQty, b.shortQty as shortQty, b.pickedQty as pickedQty " +
            "FROM BOMBYPJ b " +
            "WHERE b.id.companyCode = :companyCode AND b.id.plantNo = :plantNo" +
            "   AND b.id.projectNo = :projectNo AND b.id.alternate = :alternate ")
    BombypjProjection getBombypjInfo(String companyCode, Integer plantNo, String projectNo, String alternate);

    @Modifying
    @Query("UPDATE BOMBYPJ b set b.pickedQty = :pickedQty, b.shortQty = :shortQty " +
            "WHERE b.id.companyCode = :companyCode and b.id.plantNo = :plantNo " +
            "and b.id.component = :component and b.id.orderNo = :orderNo " +
            "and b.id.alternate = :alternate and b.id.projectNo = :projectNo " +
            "and b.id.assemblyNo = :assemblyNo ")
    void updatePickedQtyAndShortQty(BigDecimal pickedQty, BigDecimal shortQty,
                                    String companyCode, Integer plantNo,
                                    String component, String orderNo,
                                    String alternate, String projectNo,
                                    String assemblyNo);

    @Query("SELECT DISTINCT b.id.projectNo as projectNo FROM BOMBYPJ b WHERE b.id.companyCode = :companyCode " +
            "AND b.id.plantNo = :plantNo AND (b.statuz IN ('R','B','E','C') OR b.statuz is null) " +
            "AND COALESCE(b.pickedQty,0) > 0 ORDER BY b.id.projectNo")
    List<BombypjProjection> getPrjNoByStatus(String companyCode, Integer plantNo);
}
