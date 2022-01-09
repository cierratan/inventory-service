package com.sunright.inventory.repository;

import com.sunright.inventory.entity.grn.GrnDet;
import com.sunright.inventory.entity.grn.GrnId;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrnDetRepository extends PagingAndSortingRepository<GrnDet, GrnId>, JpaSpecificationExecutor<GrnDet> {

    Optional<GrnDet> findGrnDetByGrnId(GrnId grnId);

}
