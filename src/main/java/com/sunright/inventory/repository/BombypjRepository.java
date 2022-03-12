package com.sunright.inventory.repository;

import com.sunright.inventory.entity.bombypj.Bombypj;
import com.sunright.inventory.entity.bombypj.BombypjId;
import com.sunright.inventory.entity.bombypj.BombypjProjection;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.math.BigDecimal;
import java.util.Date;
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

    @Query("SELECT b.mrvResv as mrvResv, b.id.orderNo as orderNo, " +
            "b.id.assemblyNo as assemblyNo, b.id.component as component, b.id.alternate as alternate, b.id.projectNo as projectNo, " +
            "b.resvQty as resvQty, b.shortQty as shortQty, b.pickedQty as pickedQty, b.issuedQty as issuedQty " +
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

    @Modifying
    @Query("UPDATE BOMBYPJ b " +
            "set b.resvQty = :resvQty, b.pickedQty = :pickedQty, b.shortQty = :shortQty," +
            "b.mrvQty = :mrvQty, b.mrvResv = :mrvResv " +
            "WHERE b.id.companyCode = :companyCode and b.id.plantNo = :plantNo " +
            "and b.id.component = :component and b.id.orderNo = :orderNo " +
            "and b.id.alternate = :alternate and b.id.projectNo = :projectNo " +
            "and b.id.assemblyNo = :assemblyNo ")
    void updateResvQtyAndPickedQtyAndShortQtyAndMrvQtyAndMrvResv(BigDecimal resvQty, BigDecimal pickedQty, BigDecimal shortQty,
                                    BigDecimal mrvQty, BigDecimal mrvResv,
                                    String companyCode, Integer plantNo,
                                    String component, String orderNo,
                                    String alternate, String projectNo,
                                    String assemblyNo);

    @Query("SELECT DISTINCT b.id.projectNo as projectNo FROM BOMBYPJ b WHERE b.id.companyCode = :companyCode " +
            "AND b.id.plantNo = :plantNo AND (b.statuz IN ('R','B','E','C') OR b.statuz is null) " +
            "AND COALESCE(b.pickedQty,0) > 0 ORDER BY b.id.projectNo")
    List<BombypjProjection> getPrjNoByStatus(String companyCode, Integer plantNo);

    @Query("select b.id.projectNo as projectNo, b.id.alternate as alternate , i.partNo as partNo, i.loc as loc, i.uom as uom, l.stdMaterial as stdMaterial, " +
            "coalesce(sum(coalesce(b.shortQty, 0)),0, sum(coalesce(b.resvQty, 0) - coalesce(b.inTransitQty, 0) - coalesce(b.pickedQty, 0)), " +
            "sum(coalesce(b.shortQty, 0))) as shortQty, sum(coalesce(b.pickedQty, 0)) as pickedQty " +
            "from BOMBYPJ b left join ITEM i on i.companyCode = b.id.companyCode and i.plantNo = b.id.plantNo and i.itemNo = b.id.alternate left join ITEMLOC l " +
            "on l.loc = i.loc and l.companyCode = i.companyCode and l.plantNo = i.plantNo and l.itemNo = i.itemNo " +
            "where b.id.companyCode = :companyCode and b.id.plantNo = :plantNo and coalesce(b.statuz, 'R') NOT IN ('D', 'X') " +
            "and (coalesce(b.pickedQty, 0) > 0 or (coalesce(coalesce(b.shortQty, 0),0, " +
            "(coalesce(b.resvQty, 0) - coalesce(b.inTransitQty, 0) - coalesce(b.pickedQty, 0)), coalesce(b.shortQty, 0)) > 0)) " +
            "and b.id.projectNo = :projectNo group by b.id.projectNo, b.id.alternate,i.partNo,i.loc,i.uom,l.stdMaterial ORDER BY b.id.alternate")
    List<BombypjProjection> getDataByProjectNo(String companyCode, Integer plantNo, String projectNo);

    @Query("SELECT coalesce(b.resvQty, 0) as resvQty, coalesce(b.shortQty,0) as shortQty, coalesce(b.inTransitQty,0) as inTransitQty, " +
            "coalesce(b.delvQty,0) as delvQty, coalesce(b.pickedQty,0) as pickedQty FROM BOMBYPJ b WHERE b.id.companyCode = :companyCode " +
            "AND b.id.plantNo = :plantNo AND b.tranType = :tranType AND b.id.projectNo = :projectNo AND b.id.orderNo = :orderNo " +
            "AND b.id.assemblyNo = :assemblyNo AND b.id.alternate = :itemNo AND b.resvQty > 0 AND b.inTransitQty > 0")
    List<BombypjProjection> getBombypjCur(String companyCode, Integer plantNo, String tranType, String projectNo,
                                          String orderNo, String assemblyNo, String itemNo);

    @Modifying
    @Query("UPDATE BOMBYPJ b set b.shortQty = :shortQty, b.inTransitQty = :inTransitQty, b.delvQty = :delvQty, " +
            "b.pickedQty = :pickedQty, b.delvDate = :delvDate WHERE b.id.companyCode = :companyCode AND b.id.plantNo = :plantNo " +
            "AND b.id.projectNo = :projectNo AND b.id.alternate = :itemNo")
    void updateShortInTransitDelvPickedQtyDelvDate(BigDecimal shortQty, BigDecimal inTransitQty, BigDecimal delvQty,
                                                   BigDecimal pickedQty, Date delvDate, String companyCode, Integer plantNo,
                                                   String projectNo, String itemNo);

    @Query("SELECT coalesce(b.resvQty,0) as resvQty, coalesce(b.issuedQty,0) as issuedQty, " +
            "coalesce(b.pickedQty,0) as pickedQty, coalesce(b.shortQty,0) as shortQty FROM BOMBYPJ b " +
            "WHERE b.id.companyCode = :companyCode AND b.id.plantNo = :plantNo AND b.id.projectNo = :projectNo " +
            "AND b.id.alternate = :itemNo AND ((b.pickedQty > 0) OR ((coalesce(:shortQty,0) > 0) AND coalesce(b.shortQty,0) > 0)) " +
            "AND COALESCE(b.statuz, 'R') NOT IN ('D', 'X') ORDER BY b.id.assemblyNo, b.id.alternate")
    List<BombypjProjection> getBombypjInfoByStatus(String companyCode, Integer plantNo, String projectNo, String itemNo, BigDecimal shortQty);

    @Modifying
    @Query("UPDATE BOMBYPJ b set b.resvQty = :resvQty, b.issuedQty = :issuedQty, b.pickedQty = :pickedQty, " +
            "b.shortQty = :shortQty WHERE b.id.companyCode = :companyCode AND b.id.plantNo = :plantNo " +
            "AND b.id.projectNo = :projectNo AND b.id.alternate = :itemNo")
    void updateResvIssuedPickedShortQty(BigDecimal resvQty, BigDecimal issuedQty, BigDecimal pickedQty,
                                        BigDecimal shortQty, String companyCode, Integer plantNo, String projectNo, String itemNo);

    @Modifying
    @Query("UPDATE BOMBYPJ b set b.issuedQty = :issuedQty WHERE b.id.companyCode = :companyCode AND b.id.plantNo = :plantNo " +
            "AND b.id.orderNo = :orderNo AND b.id.projectNo = :projectNo AND b.id.alternate = :itemNo")
    void updateIssuedQty(BigDecimal issuedQty, String companyCode, Integer plantNo, String orderNo, String projectNo, String itemNo);

    @Query("select i.partNo as partNo, i.loc as loc, i.uom as uom, i.source as source, l.stdMaterial as stdMaterial, " +
            "sum(coalesce(b.pickedQty, 0)) as pickedQty " +
            "from BOMBYPJ b join ITEM i on i.companyCode = b.id.companyCode and i.plantNo = b.id.plantNo and i.itemNo = b.id.alternate join ITEMLOC l " +
            "on l.loc = i.loc and l.companyCode = i.companyCode and l.plantNo = i.plantNo and l.itemNo = i.itemNo " +
            "where b.id.companyCode = :companyCode and b.id.plantNo = :plantNo and coalesce(b.statuz, 'R') NOT IN ('D', 'X') " +
            "and b.id.projectNo = :projectNo and b.id.alternate = :itemNo group by i.partNo,i.loc,i.uom,i.source,l.stdMaterial")
    BombypjProjection bombypjCur(String companyCode, Integer plantNo, String projectNo, String itemNo);

    @Query("SELECT b.tranType as tranType, b.id.orderNo as orderNo, b.id.projectNo as projectNo,b.id.alternate as alternate," +
            "coalesce(b.recdQty,0) as recdQty, coalesce(b.resvQty,0) as resvQty, " +
            "coalesce(b.shortQty,0) as shortQty, coalesce(b.issuedQty,0) as issuedQty, coalesce(b.pickedQty,0) as pickedQty FROM BOMBYPJ b " +
            "WHERE b.id.companyCode = :companyCode AND b.id.plantNo = :plantNo AND b.id.projectNo = :projectNo " +
            "AND b.id.alternate = :itemNo AND coalesce(b.resvQty,0) > 0 " +
            "AND COALESCE(b.statuz, 'R') NOT IN ('D', 'X') ORDER BY b.id.assemblyNo, b.id.alternate")
    List<BombypjProjection> bombypjCurList(String companyCode, Integer plantNo, String projectNo, String itemNo);

    @Modifying
    @Query("UPDATE BOMBYPJ b set b.pickedQty = :pickedQty, b.shortQty = :shortQty, b.resvQty = :resvQty, b.delvDate = :delvDate " +
            "WHERE b.id.companyCode = :companyCode and b.id.plantNo = :plantNo " +
            "and b.id.orderNo = :orderNo and b.id.alternate = :alternate and b.id.projectNo = :projectNo")
    void updatePickedShortResvQtyDelvDate(BigDecimal pickedQty, BigDecimal shortQty, BigDecimal resvQty, Date delvDate,
                                          String companyCode, Integer plantNo,
                                          String orderNo, String alternate, String projectNo);

    @Query("SELECT CASE WHEN (SUM(COALESCE(b.pickedQty,0)) = 0) THEN (SUM(coalesce(b.shortQty, 0))) ELSE (SUM(COALESCE (b.pickedQty, 0))) END as pickedQty " +
            "FROM BOMBYPJ b WHERE b.id.companyCode = :companyCode AND b.id.plantNo = :plantNo AND b.id.projectNo = :projectNo " +
            "AND b.id.alternate = :itemNo")
    BombypjProjection bombypjCurCaseWhen(String companyCode, Integer plantNo, String projectNo, String itemNo);

    @Query(value = "SELECT nvl(b.issued_qty,0) as issuedQty  " +
            "FROM bombypj b," +
            "     bomproj t " +
            "WHERE t.company_code = :companyCode" +
            "  AND t.plant_no = :plantNo" +
            "  AND t.project_no = :projectNo" +
            "  AND nvl(t.open_close, 'O') not in ('C', 'V')" +
            "  AND t.project_no = b.project_no" +
            "  AND t.order_no = b.order_no" +
            "  AND b.company_code = :companyCode" +
            "  and b.plant_no = :plantNo" +
            "  and b.alternate = :itemNo" +
            "  AND b.order_no = :projectNo" +
            "  AND nvl(b.status, 'R') not in ('D', 'X') " +
            "union " +
            "SELECT nvl(b.issued_qty,0) as issuedQty " +
            "FROM bombypj b " +
            "WHERE b.company_code = :companyCode" +
            "  AND b.plant_no = :plantNo" +
            "  AND b.project_no = :projectNo" +
            "  AND b.order_no = :projectNo" +
            "  AND alternate = :itemNo" +
            "  AND nvl(status, 'R') not in ('D', 'X')", nativeQuery = true)
    BombypjProjection bomProjCur(String companyCode, Integer plantNo, String projectNo, String itemNo);

    @Query("SELECT DISTINCT b.id.projectNo as projectNo FROM BOMBYPJ b WHERE b.id.companyCode = :companyCode " +
            "AND b.id.plantNo = :plantNo AND b.id.alternate = :itemNo")
    List<BombypjProjection> bombypjCur(String companyCode, Integer plantNo, String itemNo);
}
