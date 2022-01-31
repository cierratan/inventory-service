package com.sunright.inventory.repository;

import com.sunright.inventory.entity.bombypj.Bombypj;
import com.sunright.inventory.entity.bombypj.BombypjId;
import com.sunright.inventory.entity.bombypj.BombypjProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BombypjRepository extends JpaRepository<Bombypj, BombypjId>, JpaSpecificationExecutor<Bombypj> {

    @Query("SELECT DISTINCT b.id.projectNo as projectNo FROM BOMBYPJ b WHERE b.id.companyCode = :companyCode " +
            "AND b.id.plantNo = :plantNo AND b.id.projectNo = :projectNo")
    BombypjProjection getPrjNo(String companyCode, Integer plantNo, String projectNo);

    @Query("SELECT b.id.alternate as alternate FROM BOMBYPJ b WHERE b.id.companyCode = :companyCode " +
            "AND b.id.plantNo = :plantNo AND b.id.projectNo = :projectNo AND b.id.alternate = :itemNo " +
            "AND COALESCE(b.statuz, 'R') NOT IN ('D', 'X')")
    BombypjProjection getAltrnt(String companyCode, Integer plantNo, String projectNo, String itemNo);
}
