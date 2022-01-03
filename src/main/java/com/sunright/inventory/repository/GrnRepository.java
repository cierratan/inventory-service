package com.sunright.inventory.repository;

import com.sunright.inventory.entity.Grn;
import com.sunright.inventory.entity.GrnId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrnRepository extends JpaRepository<Grn, GrnId> {

    @Query("SELECT x FROM GRN x where x.grnId.grnNo = :grnNo")
    Optional<Grn> findByGrnNo(String grnNo);
}
