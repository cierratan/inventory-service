package com.sunright.inventory.repository;

import com.sunright.inventory.entity.msr.MSR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MSRRepository extends JpaRepository<MSR, Long>, JpaSpecificationExecutor<MSR> {

    Optional<MSR> findMSRByMsrNo(String msrNo);
}