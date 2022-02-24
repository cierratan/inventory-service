package com.sunright.inventory.repository;

import com.sunright.inventory.entity.coq.COQDetailSub;
import com.sunright.inventory.entity.coq.COQDetailSubId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface COQDetailSubRepository extends JpaRepository<COQDetailSub, COQDetailSubId>, JpaSpecificationExecutor<COQDetailSub> {

}
