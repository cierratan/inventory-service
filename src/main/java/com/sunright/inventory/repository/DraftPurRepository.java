package com.sunright.inventory.repository;

import com.sunright.inventory.entity.DraftPur;
import com.sunright.inventory.entity.DraftPurId;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DraftPurRepository extends PagingAndSortingRepository<DraftPur, DraftPurId>, JpaSpecificationExecutor<DraftPur> {

    @Query(value = "select po_no, nvl(open_close,'O') status " +
            "from s_draft_pur where company_code = :companyCode and plant_no = :plantNo and po_no = :poNo", nativeQuery = true)
    List<Object[]> checkStatusPoNoDraftPur(String companyCode, Integer plantNo, String poNo);
}
