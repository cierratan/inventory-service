package com.sunright.inventory.repository;

import com.sunright.inventory.entity.bombypj.Bombypj;
import com.sunright.inventory.entity.bombypj.BombypjId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BombypjRepository extends JpaRepository<Bombypj, BombypjId>, JpaSpecificationExecutor<Bombypj> {

    @Query(value = "select distinct b.id.projectNo from BOMBYPJ b where b.id.companyCode = :companyCode " +
            "and b.id.plantNo = :plantNo and b.id.projectNo = :projectNo")
    Bombypj getPrjNo(String companyCode, Integer plantNo, String projectNo);

    @Query(value = "select b.id.alternate from BOMBYPJ b where b.id.companyCode = :companyCode " +
            "and b.id.plantNo = :plantNo and b.id.projectNo = :projectNo and b.id.alternate = :itemNo " +
            "and coalesce(status_1, 'R') not in ('D', 'X')")
    Bombypj getAltrnt(String companyCode, Integer plantNo, String projectNo, String itemNo);
}
