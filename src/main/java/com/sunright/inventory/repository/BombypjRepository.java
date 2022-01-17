package com.sunright.inventory.repository;

import com.sunright.inventory.entity.bombypj.Bombypj;
import com.sunright.inventory.entity.bombypj.BombypjId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BombypjRepository extends JpaRepository<Bombypj, BombypjId>, JpaSpecificationExecutor<Bombypj> {

    @Query(value = "select distinct project_no from bombypj where company_code = :companyCode " +
            "and plant_no = :plantNo and project_no = :projectNo", nativeQuery = true)
    List<Object[]> getPrjNo(String companyCode, Integer plantNo, String projectNo);

    @Query(value = "select alternate from bombypj where company_code = :companyCode " +
            "and plant_no = :plantNo and project_no = :projectNo and alternate = :itemNo " +
            "and nvl(status_1, 'R') not in ('D', 'X')", nativeQuery = true)
    List<Object[]> getAltrnt(String companyCode, Integer plantNo, String projectNo, String itemNo);
}
