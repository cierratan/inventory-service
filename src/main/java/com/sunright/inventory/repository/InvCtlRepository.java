package com.sunright.inventory.repository;

import com.sunright.inventory.entity.BaseIdEntity;
import com.sunright.inventory.entity.InvCtl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface InvCtlRepository extends JpaRepository<InvCtl, BaseIdEntity>, JpaSpecificationExecutor<InvCtl> {
}
