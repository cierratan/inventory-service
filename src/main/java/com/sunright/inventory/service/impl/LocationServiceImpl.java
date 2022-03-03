package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.lov.LocationDTO;
import com.sunright.inventory.dto.search.Filter;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.lov.Country;
import com.sunright.inventory.entity.lov.Location;
import com.sunright.inventory.exception.DuplicateException;
import com.sunright.inventory.exception.NotFoundException;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.lov.CountryRepository;
import com.sunright.inventory.repository.lov.LocationRepository;
import com.sunright.inventory.service.LocationService;
import com.sunright.inventory.util.QueryGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;

@Service
@Transactional
public class LocationServiceImpl implements LocationService {

    @Autowired
    private QueryGenerator queryGenerator;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Override
    public LocationDTO createLocation(LocationDTO input) {
        UserProfile userProfile = UserProfileContext.getUserProfile();

        // comment by Arya
        /*List<Location> found = locationRepository.findByCompanyCodeAndPlantNoAndLoc(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getLoc());
        if(!CollectionUtils.isEmpty(found)) {
            throw new DuplicateException(String.format("Location %s already exist", input.getLoc()));
        }

        Location location = new Location();
        BeanUtils.copyProperties(input, location);
        location.setCompanyCode(userProfile.getCompanyCode());
        location.setPlantNo(userProfile.getPlantNo());
        location.setStatus(Status.ACTIVE);
        location.setCreatedBy(userProfile.getUsername());
        location.setCreatedAt(ZonedDateTime.now());
        location.setUpdatedBy(userProfile.getUsername());
        location.setUpdatedAt(ZonedDateTime.now());

        Location saved = locationRepository.save(location);

        input.setId(saved.getId());
        input.setVersion(saved.getVersion());*/

        // add logic by Arya for bug fixing soft deleted
        List<Location> found = locationRepository.findByCompanyCodeAndPlantNoAndLoc(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getLoc());
        if (!CollectionUtils.isEmpty(found)) {
            for (Location rec : found) {
                Optional<Location> optionalLocation = locationRepository.findById(rec.getId());
                if (optionalLocation.isPresent()) {
                    if (optionalLocation.get().getStatus() == Status.DELETED) {
                        Location location = new Location();
                        BeanUtils.copyProperties(input, location);
                        location.setCompanyCode(UserProfileContext.getUserProfile().getCompanyCode());
                        location.setPlantNo(UserProfileContext.getUserProfile().getPlantNo());
                        location.setStatus(Status.ACTIVE);
                        location.setCreatedBy(rec.getCreatedBy());
                        location.setCreatedAt(rec.getCreatedAt());
                        location.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
                        location.setUpdatedAt(ZonedDateTime.now());
                        location.setId(rec.getId());
                        location.setVersion(rec.getVersion());
                        locationRepository.save(location);
                    } else {
                        throw new DuplicateException(String.format("Location %s already exist", input.getLoc()));
                    }
                }
            }
        } else {
            Location location = new Location();
            BeanUtils.copyProperties(input, location);
            location.setCompanyCode(userProfile.getCompanyCode());
            location.setPlantNo(userProfile.getPlantNo());
            location.setStatus(Status.ACTIVE);
            location.setCreatedBy(userProfile.getUsername());
            location.setCreatedAt(ZonedDateTime.now());
            location.setUpdatedBy(userProfile.getUsername());
            location.setUpdatedAt(ZonedDateTime.now());
            Location saved = locationRepository.save(location);
            input.setId(saved.getId());
            input.setVersion(saved.getVersion());
        }

        return input;
    }

    @Override
    public LocationDTO editLocation(LocationDTO input) {
        Location found = checkIfRecordExist(input.getId());

        Location location = new Location();
        BeanUtils.copyProperties(input, location, "status");
        location.setCompanyCode(UserProfileContext.getUserProfile().getCompanyCode());
        location.setPlantNo(UserProfileContext.getUserProfile().getPlantNo());
        location.setStatus(found.getStatus());
        location.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
        location.setUpdatedAt(ZonedDateTime.now());

        Location saved = locationRepository.save(location);

        input.setVersion(saved.getVersion());
        return input;
    }

    @Override
    public LocationDTO findBy(Long id) {
        Location location = checkIfRecordExist(id);

        LocationDTO locationDTO = new LocationDTO();
        BeanUtils.copyProperties(location, locationDTO);
        BeanUtils.copyProperties(location.getId(), locationDTO);

        return locationDTO;
    }

    @Override
    public void deleteLocation(Long id) {
        Location location = checkIfRecordExist(id);

        location.setStatus(Status.DELETED);
        location.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
        location.setUpdatedAt(ZonedDateTime.now());

        locationRepository.save(location);
    }

    @Override
    public SearchResult<LocationDTO> searchBy(SearchRequest searchRequest) {
        Specification<Location> specs = where(queryGenerator.createDefaultSpec());

        if (!CollectionUtils.isEmpty(searchRequest.getFilters())) {
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
            defineCountryName(location, locationDTO);
            return locationDTO;
        }).collect(Collectors.toList()));

        return locations;
    }

    private void defineCountryName(Location location, LocationDTO locationDTO) {
        List<Country> listCountry = countryRepository.findAll();
        for (Country rec : listCountry) {
            if (location.getCountryCode().equals(rec.getCountryCode())) {
                locationDTO.setCountryName(rec.getDescription());
            }
        }
    }

    private Location checkIfRecordExist(Long id) {
        Optional<Location> optionalLocation = locationRepository.findById(id);

        if (!optionalLocation.isPresent()) {
            throw new NotFoundException("Record is not found");
        }
        return optionalLocation.get();
    }
}
