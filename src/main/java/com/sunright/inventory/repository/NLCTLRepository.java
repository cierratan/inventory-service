package com.sunright.inventory.repository;

import com.sunright.inventory.entity.base.BaseIdEntity;
import com.sunright.inventory.entity.nlctl.NLCTL;
import com.sunright.inventory.entity.nlctl.NLCTLProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NLCTLRepository extends JpaRepository<NLCTL, BaseIdEntity>, JpaSpecificationExecutor<NLCTL> {

    @Query(value = "select decode(to_number(to_char(start_period, 'YYYY')) - " +
            "to_number(to_char(add_months(last_day(start_period), 11), 'YYYY')),0, start_year, start_year + 1, " +
            "decode(to_number(to_char(start_period, 'YYYY')) - " +
            "to_number(to_char(add_months(last_day(start_period), 11), 'YYYY')), 0, start_year + 1,start_year + 2)," +
            "decode(to_number(to_char(start_period, 'YYYY')) - " +
            "to_number(to_char(add_months(last_day(start_period), 11), 'YYYY'))," +
            "0, start_year, start_year + 1)) as batchNo from NLCTL where company_code = :companyCode and plant_no = :plantNo", nativeQuery = true)
    NLCTLProjection getBatchYear(String companyCode, Integer plantNo);

    @Query("SELECT n.inventoryEnabled as inventoryEnabled, n.inventoryMonth as inventoryMonth, n.inventoryYear as inventoryYear " +
            "FROM NLCTL n " +
            "WHERE n.id.companyCode = :companyCode AND n.id.plantNo = :plantNo")
    NLCTLProjection checkInvPeriod(String companyCode, Integer plantNo);
}
