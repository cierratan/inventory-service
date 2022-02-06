package com.sunright.inventory.repository;

import com.sunright.inventory.entity.itembatclog.ItemBatcLog;
import com.sunright.inventory.entity.itembatclog.ItemBatcLogId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemBatcLogRepository extends JpaRepository<ItemBatcLog, ItemBatcLogId> {
}
