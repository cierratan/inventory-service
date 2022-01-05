package com.sunright.inventory.repository;

import com.sunright.inventory.entity.uom.UOM;
import com.sunright.inventory.entity.uom.UOMId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UOMRepository extends JpaRepository<UOM, UOMId>, JpaSpecificationExecutor<UOM> {
}