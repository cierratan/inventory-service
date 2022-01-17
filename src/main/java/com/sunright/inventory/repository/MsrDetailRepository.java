package com.sunright.inventory.repository;

import com.sunright.inventory.entity.msr.MSRDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MsrDetailRepository extends JpaRepository<MSRDetail, Long> {
}
