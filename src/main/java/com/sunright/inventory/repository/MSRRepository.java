package com.sunright.inventory.repository;

import com.sunright.inventory.entity.msr.MSR;
import com.sunright.inventory.entity.msr.MSRId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MSRRepository extends JpaRepository<MSR, MSRId>, JpaSpecificationExecutor<MSR> {
}
