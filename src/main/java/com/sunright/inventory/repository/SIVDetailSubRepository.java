package com.sunright.inventory.repository;

import com.sunright.inventory.entity.siv.SIVDetailSub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SIVDetailSubRepository extends JpaRepository<SIVDetailSub, Long> {
}
