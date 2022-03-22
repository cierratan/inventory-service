package com.sunright.inventory.repository;

import com.sunright.inventory.entity.bombypj.BombypjProjection;
import com.sunright.inventory.entity.siv.SIVDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface SIVDetailRepository extends JpaRepository<SIVDetail, Long>, JpaSpecificationExecutor<SIVDetail> {

    @Query(value = "SELECT GREATEST(:bomQtyA,:bomQtyB,:bomQtyC,:bomQtyD,:bomQtyE) as greatestQty FROM DUAL", nativeQuery = true)
    BombypjProjection findGreatestQty(BigDecimal bomQtyA, BigDecimal bomQtyB, BigDecimal bomQtyC, BigDecimal bomQtyD, BigDecimal bomQtyE);
}