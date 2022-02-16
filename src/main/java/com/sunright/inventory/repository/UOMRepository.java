package com.sunright.inventory.repository;

import com.sunright.inventory.entity.uom.UOM;
import com.sunright.inventory.entity.uom.UOMId;
import com.sunright.inventory.entity.uom.UOMProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UOMRepository extends JpaRepository<UOM, UOMId>, JpaSpecificationExecutor<UOM> {

    @Query("SELECT u.uomFactor as uomFactor FROM UOM u WHERE u.id.uomFrom = :uomFrom and u.id.uomTo = :uomTo")
    UOMProjection getUomFactor(String uomFrom, String uomTo);
}
