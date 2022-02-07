package com.sunright.inventory.repository;

import com.sunright.inventory.entity.InAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InAuditRepository extends JpaRepository<InAudit, Long> {
}
