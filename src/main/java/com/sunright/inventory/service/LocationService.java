package com.sunright.inventory.service;

import com.sunright.inventory.dto.SearchRequest;
import com.sunright.inventory.dto.SearchResult;
import com.sunright.inventory.dto.lov.LocationDTO;

public interface LocationService {
    LocationDTO saveLocation(LocationDTO input);
    LocationDTO findBy(String companyCode, int plantNo, String loc);
    SearchResult<LocationDTO> searchBy(SearchRequest searchRequest);
}
