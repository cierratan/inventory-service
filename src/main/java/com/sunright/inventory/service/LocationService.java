package com.sunright.inventory.service;

import com.sunright.inventory.dto.SearchRequest;
import com.sunright.inventory.dto.SearchResult;
import com.sunright.inventory.dto.lov.LocationDTO;

public interface LocationService {
    LocationDTO createLocation(LocationDTO input);
    LocationDTO editLocation(LocationDTO input);
    LocationDTO findBy(String loc);
    void deleteLocation(String loc);
    SearchResult<LocationDTO> searchBy(SearchRequest searchRequest);
}