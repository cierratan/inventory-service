package com.sunright.inventory.repository;

import com.sunright.inventory.entity.GrnDet;
import com.sunright.inventory.entity.GrnId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrnDetRepository extends JpaRepository<GrnDet, GrnId> {

    @Query("SELECT x FROM GRNDET x where x.grnId.grnNo = :grnNo")
    Optional<GrnDet> findGrnDetByGrnNo(String grnNo);
}
