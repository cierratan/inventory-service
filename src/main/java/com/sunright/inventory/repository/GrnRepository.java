package com.sunright.inventory.repository;

import com.sunright.inventory.entity.grn.Grn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrnRepository extends JpaRepository<Grn, Long>, JpaSpecificationExecutor<Grn> {

    Optional<Grn> findGrnByGrnNo(String grnNo);
}
