package com.sunright.inventory.repository;

import com.sunright.inventory.entity.siv.SIVDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SIVDetailRepository extends JpaRepository<SIVDetail, Long>, JpaSpecificationExecutor<SIVDetail> {
}