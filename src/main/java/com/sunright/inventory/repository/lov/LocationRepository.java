package com.sunright.inventory.repository.lov;

import com.sunright.inventory.entity.lov.Location;
import com.sunright.inventory.entity.lov.LocationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, LocationId>, JpaSpecificationExecutor<Location> {

}
