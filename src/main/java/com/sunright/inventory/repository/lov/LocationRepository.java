package com.sunright.inventory.repository.lov;

import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.lov.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long>, JpaSpecificationExecutor<Location> {

    List<Location> findByCompanyCodeAndPlantNoAndLoc(String companyCode, Integer plantNo, String location);

    @Modifying
    @Query("UPDATE LOC l set l.loc = :loc, l.description = :description, l.address1 = :address1, l.address2 = :address2, " +
            "l.address3 = :address3, l.address4 = :address4, l.pcode = :pcode, l.telNo = :telNo, l.faxNo = :faxNo, " +
            "l.countryCode=:countryCode, l.personInCharge=:personInCharge, l.stateCode=:stateCode, l.regionCode=:regionCode, " +
            "l.cityCode=:cityCode, l.remarks=:remarks, l.status =:status, " +
            "l.updatedBy=:updatedBy, l.updatedAt=:updatedAt " +
            "WHERE l.companyCode = :companyCode AND l.plantNo = :plantNo AND l.id = :id ")
    void updateLocation(String loc, String description, String address1, String address2, String address3,
                        String address4, String pcode, String telNo, String faxNo, String countryCode, String personInCharge,
                        String stateCode, String regionCode, String cityCode, String remarks,
                        Status status, String updatedBy, ZonedDateTime updatedAt, String companyCode, Integer plantNo, Long id);
}
