package com.sunright.inventory.repository;

import com.sunright.inventory.entity.pr.PRDetail;
import com.sunright.inventory.entity.pr.PRDetailId;
import com.sunright.inventory.entity.pr.PRDetailProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PRDetailRepository extends JpaRepository<PRDetail, PRDetailId>, JpaSpecificationExecutor<PRDetail> {

    @Query(value = "select 0                              as itemType," +
            "       b.alternate                      as alternate," +
            "       decode(sum(nvl(b.short_qty, 0))," +
            "              0, sum(nvl(b.resv_qty, 0) - nvl(b.in_transit_qty, 0) - nvl(b.picked_qty, 0))," +
            "              sum(nvl(b.short_qty, 0))) as shortQty," +
            "       sum(nvl(b.picked_qty, 0))        as pickedQty," +
            "       ''                               as saleType," +
            "       :docmNo                   as docmNo," +
            "       i.part_no                        as partNo," +
            "       i.uom                            as uom," +
            "       i.loc                            as loc," +
            "       l.std_material                   as stdMaterial," +
            "       decode(tran_type, 'PR', '', '')  as remarks " +
            "from bombypj b," +
            "     item i," +
            "     itemloc l " +
            "where i.company_code = :companyCode" +
            "  and i.plant_no = :plantNo" +
            "  and i.item_no = b.alternate" +
            "  and i.loc = l.loc" +
            "  and l.company_code = :companyCode" +
            "  and l.plant_no = :plantNo" +
            "  and l.item_no = b.alternate" +
            "  and b.company_code = :companyCode" +
            "  and b.plant_no = :plantNo" +
            "  and b.tran_type = :tranType" +
            "  and b.project_no = :docmNo" +
            "  and b.order_no = :docmNo" +
            "  and (nvl(b.picked_qty, 0) > 0 or" +
            "       (decode(nvl(b.short_qty, 0)," +
            "               0, (nvl(b.resv_qty, 0) - nvl(b.in_transit_qty, 0) - nvl(b.picked_qty, 0))," +
            "               nvl(b.short_qty, 0)) > 0))" +
            "  and b.status not in ('X', 'D')" +
            "group by '0'," +
            "         b.alternate," +
            "         ''," +
            "         :docmNo," +
            "         i.part_no," +
            "         i.uom," +
            "         i.loc," +
            "         l.std_material," +
            "         decode(tran_type, 'PR', '', '') " +
            "union " +
            "select 1                        as itemType," +
            "       pd.item_no                 as itemNo," +
            "       0                          as shortQty," +
            "       pd.qty                     as pickedQty," +
            "       ''                         as saleType," +
            "       :docmNo             as docmNo," +
            "       pd.part_no                 as partNo," +
            "       pd.uom                     as uom," +
            "       'LD'                       as loc," +
            "       0                          as stdMaterial," +
            "       decode(:tranType, 'PR', :remarks, '') as remarks " +
            "from prdet pd " +
            "where company_code = :companyCode" +
            "  and plant_no = :plantNo" +
            "  and item_type = 1" +
            "  and :tranType = 'PR'" +
            "  and docm_no = :docmNo " +
            "order by 2, 6", nativeQuery = true)
    List<PRDetailProjection> bombypjCur(String docmNo, String remarks, String companyCode, Integer plantNo, String tranType);

    @Query(value = "select (sum(iss) / sum(req)) as issReq " +
            "from (select count(alternate) iss, 0 req" +
            "      from bombypj" +
            "      where company_code = :companyCode" +
            "        and plant_no = :plantNo" +
            "        and project_no = :docmNo" +
            "        and nvl(issued_qty, 0) = nvl(reqd_qty, 0)" +
            "      union" +
            "      select 0, count(rec_seq)" +
            "      from prdet" +
            "      where company_code = :companyCode" +
            "        and plant_no = :plantNo" +
            "        and docm_no = :docmNo)", nativeQuery = true)
    PRDetailProjection cComplete(String companyCode, Integer plantNo, String docmNo);
}
