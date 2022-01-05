package com.sunright.inventory.repository;

import com.sunright.inventory.entity.grn.Grn;
import com.sunright.inventory.entity.grn.GrnId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrnRepository extends PagingAndSortingRepository<Grn, Long> {

    Optional<Grn> findGrnByGrnIdGrnNo(String grnNo);

}
