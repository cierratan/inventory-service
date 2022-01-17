package com.sunright.inventory.service;

import com.sunright.inventory.dto.lov.LocationDTO;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;

public interface LocationService {
    LocationDTO createLocation(LocationDTO input);
    LocationDTO editLocation(LocationDTO input);
    LocationDTO findBy(Long id);
    void deleteLocation(Long id);
    SearchResult<LocationDTO> searchBy(SearchRequest searchRequest);
}
