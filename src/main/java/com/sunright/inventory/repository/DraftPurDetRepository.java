package com.sunright.inventory.repository;

import com.sunright.inventory.entity.DraftPurDet;
import com.sunright.inventory.entity.DraftPurDetId;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DraftPurDetRepository extends PagingAndSortingRepository<DraftPurDet, DraftPurDetId>, JpaSpecificationExecutor<DraftPurDet> {
}
