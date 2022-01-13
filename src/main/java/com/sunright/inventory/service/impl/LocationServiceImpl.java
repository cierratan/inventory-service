package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.search.Filter;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.lov.LocationDTO;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.lov.Location;
import com.sunright.inventory.entity.lov.LocationId;
import com.sunright.inventory.exception.NotFoundException;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.lov.LocationRepository;
import com.sunright.inventory.service.LocationService;
import com.sunright.inventory.util.QueryGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;

@Service
public class LocationServiceImpl implements LocationService {

    @Autowired
    private QueryGenerator queryGenerator;

    @Autowired
    private LocationRepository locationRepository;

    @Override
    public LocationDTO createLocation(LocationDTO input) {
        UserProfile userProfile = UserProfileContext.getUserProfile();

        Location location = new Location();
        BeanUtils.copyProperties(input, location);
        location.setId(populateLocationId(input.getLoc()));
        location.setStatus(Status.ACTIVE);
        location.setCreatedBy(userProfile.getUsername());
        location.setCreatedAt(ZonedDateTime.now());
        location.setUpdatedBy(userProfile.getUsername());
        location.setUpdatedAt(ZonedDateTime.now());

        Location saved = locationRepository.save(location);

        input.setVersion(saved.getVersion());
        return input;
    }

    @Override
    public LocationDTO editLocation(LocationDTO input) {
        LocationId locationId = populateLocationId(input.getLoc());

        Location found = checkIfRecordExist(locationId);

        Location location = new Location();
        BeanUtils.copyProperties(input, location, "status");
        location.setId(locationId);
        location.setStatus(found.getStatus());
        location.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
        location.setUpdatedAt(ZonedDateTime.now());

        Location saved = locationRepository.save(location);

        input.setVersion(saved.getVersion());
        return input;
    }

    @Override
    public LocationDTO findBy(String loc) {
        LocationId locationId = populateLocationId(loc);

        Location location = checkIfRecordExist(locationId);

        LocationDTO locationDTO = new LocationDTO();
        BeanUtils.copyProperties(location, locationDTO);
        BeanUtils.copyProperties(location.getId(), locationDTO);

        return locationDTO;
    }

    @Override
    public void deleteLocation(String loc) {
        LocationId locationId = populateLocationId(loc);

        Location location = checkIfRecordExist(locationId);

        location.setStatus(Status.DELETED);
        location.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
        location.setUpdatedAt(ZonedDateTime.now());

        locationRepository.save(location);
    }

    @Override
    public SearchResult<LocationDTO> searchBy(SearchRequest searchRequest) {
        Specification<Location> specs = where(queryGenerator.createDefaultSpecification());

        if(!CollectionUtils.isEmpty(searchRequest.getFilters())) {
            for (Filter filter : searchRequest.getFilters()) {
                specs = specs.and(queryGenerator.createSpecification(filter));
            }
        }

        Page<Location> pgLocations = locationRepository.findAll(specs, queryGenerator.constructPageable(searchRequest));

        SearchResult<LocationDTO> locations = new SearchResult<>();
        locations.setTotalRows(pgLocations.getTotalElements());
        locations.setTotalPages(pgLocations.getTotalPages());
        locations.setCurrentPageNumber(pgLocations.getPageable().getPageNumber());
        locations.setCurrentPageSize(pgLocations.getNumberOfElements());
        locations.setRows(pgLocations.getContent().stream().map(location -> {
            LocationDTO locationDTO = new LocationDTO();
            BeanUtils.copyProperties(location.getId(), locationDTO);
            BeanUtils.copyProperties(location, locationDTO);
            return locationDTO;
        }).collect(Collectors.toList()));

        return locations;
    }

    private LocationId populateLocationId(String loc) {
        UserProfile userProfile = UserProfileContext.getUserProfile();

        LocationId locationId = new LocationId();
        locationId.setCompanyCode(userProfile.getCompanyCode());
        locationId.setPlantNo(userProfile.getPlantNo());
        locationId.setLoc(loc);
        return locationId;
    }

    private Location checkIfRecordExist(LocationId locationId) {
        Optional<Location> optionalLocation = locationRepository.findById(locationId);

        if (optionalLocation.isEmpty()) {
            throw new NotFoundException("Record is not found");
        }
        return optionalLocation.get();
    }
}
