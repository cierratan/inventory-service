package com.sunright.inventory.repository;

import com.sunright.inventory.entity.grn.GrnDet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrnDetRepository extends JpaRepository<GrnDet, Long>, JpaSpecificationExecutor<GrnDet> {

    List<GrnDet> findGrnDetByGrnNo(String grnNo);
}
