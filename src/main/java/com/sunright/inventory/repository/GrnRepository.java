package com.sunright.inventory.repository;

import com.sunright.inventory.entity.grn.Grn;
import com.sunright.inventory.entity.grn.GrnId;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrnRepository extends PagingAndSortingRepository<Grn, GrnId>, JpaSpecificationExecutor<Grn> {

    Optional<Grn> findGrnByIdsGrnNo(String grnNo);

    Optional<Grn> findGrnByIds(GrnId grnId);

    Optional<Grn> findGrnByIds_CompanyCodeAndIds_PlantNoAndIds_GrnNo(String companyCode, Integer plantNo, String grnNo);
}
