package com.sunright.inventory.repository.lov;

import com.sunright.inventory.entity.lov.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long>, JpaSpecificationExecutor<Location> {
    List<Location> findByCompanyCodeAndPlantNoAndLoc(String companyCode, Integer plantNo, String location);
}
