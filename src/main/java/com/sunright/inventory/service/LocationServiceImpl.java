package com.sunright.inventory.service;

import com.sunright.inventory.dto.Filter;
import com.sunright.inventory.dto.SearchRequest;
import com.sunright.inventory.dto.SearchResult;
import com.sunright.inventory.dto.lov.LocationDTO;
import com.sunright.inventory.entity.lov.Location;
import com.sunright.inventory.entity.lov.LocationId;
import com.sunright.inventory.repository.lov.LocationRepository;
import com.sunright.inventory.util.QueryGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;

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
    public SearchResult<LocationDTO> searchBy(SearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getLimit());

        Page<Location> pgLocations;

        if(!CollectionUtils.isEmpty(searchRequest.getFilters())) {
            Specification<Location> specs = where(queryGenerator.createSpecification(searchRequest.getFilters().remove(0)));
            for (Filter filter : searchRequest.getFilters()) {
                specs = specs.and(queryGenerator.createSpecification(filter));
            }
            pgLocations = locationRepository.findAll(specs, pageable);
        } else {
            pgLocations = locationRepository.findAll(pageable);
        }

        SearchResult<LocationDTO> locations = new SearchResult<>();
        locations.setTotalRows(pgLocations.getTotalElements());
        locations.setTotalPages(pgLocations.getTotalPages());
        locations.setCurrentPageNumber(pgLocations.getPageable().getPageNumber());
        locations.setCurrentPageSize(pgLocations.getNumberOfElements());
        locations.setRows(pgLocations.getContent().stream().map(location -> {
            LocationDTO locationDTO = new LocationDTO();
            BeanUtils.copyProperties(location.getLocationId(), locationDTO);
            BeanUtils.copyProperties(location, locationDTO);
            return locationDTO;
        }).collect(Collectors.toList()));

        return locations;
    }

    private LocationId populateLocationId(String companyCode, Integer plantNo, String loc) {
        LocationId locationId = new LocationId();
        locationId.setCompanyCode(companyCode);
        locationId.setPlantNo(plantNo);
        locationId.setLoc(loc);
        return locationId;
    }
}
