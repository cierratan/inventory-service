package com.sunright.inventory.service;

import com.sunright.inventory.dto.SearchRequest;
import com.sunright.inventory.dto.lov.LocationDTO;
import com.sunright.inventory.entity.lov.Location;
import com.sunright.inventory.entity.lov.LocationId;
import com.sunright.inventory.repository.lov.LocationRepository;
import com.sunright.inventory.util.QueryGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationServiceImpl implements LocationService {

    @Autowired
    private QueryGenerator queryGenerator;

    @Autowired
    private LocationRepository locationRepository;

    @Override
    public LocationDTO saveLocation(LocationDTO input) {
        Location location = new Location();
        BeanUtils.copyProperties(input, location);
        location.setLocationId(populateLocationId(input.getCompanyCode(), input.getPlantNo(), input.getLoc()));

        Location saved = locationRepository.save(location);

        input.setVersion(saved.getVersion());
        return input;
    }

    @Override
    public LocationDTO findBy(String companyCode, int plantNo, String loc) {
        LocationId locationId = populateLocationId(companyCode, plantNo, loc);

        Location location = locationRepository.findById(locationId).get();

        LocationDTO locationDTO = new LocationDTO();
        BeanUtils.copyProperties(location, locationDTO);
        BeanUtils.copyProperties(location.getLocationId(), locationDTO);

        return locationDTO;
    }

    @Override
    public List<LocationDTO> searchBy(SearchRequest searchRequest) {


        return null;
    }

    private LocationId populateLocationId(String companyCode, Integer plantNo, String loc) {
        LocationId locationId = new LocationId();
        locationId.setCompanyCode(companyCode);
        locationId.setPlantNo(plantNo);
        locationId.setLoc(loc);
        return locationId;
    }
}
