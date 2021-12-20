package com.sunright.inventory.service;

import com.sunright.inventory.dto.Filter;
import com.sunright.inventory.dto.SearchRequest;
import com.sunright.inventory.dto.lov.LocationDTO;

import java.util.List;

public interface LocationService {
    LocationDTO saveLocation(LocationDTO input);
    LocationDTO findBy(String companyCode, int plantNo, String loc);
    List<LocationDTO> searchBy(SearchRequest searchRequest);
}
