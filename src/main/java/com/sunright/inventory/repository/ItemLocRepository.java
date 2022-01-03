package com.sunright.inventory.repository;

import com.sunright.inventory.entity.ItemLoc;
import com.sunright.inventory.entity.ItemLocId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemLocRepository extends JpaRepository<ItemLoc, ItemLocId> {
}
