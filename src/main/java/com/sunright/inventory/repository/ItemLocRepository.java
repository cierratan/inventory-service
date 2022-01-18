package com.sunright.inventory.repository;

import com.sunright.inventory.entity.ItemLoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemLocRepository extends JpaRepository<ItemLoc, Long> {
    List<ItemLoc> findByCompanyCodeAndPlantNoAndItemNoAndLoc(String companyCode, Integer plantNo, String itemNo, String loc);
}
