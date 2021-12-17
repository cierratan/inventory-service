package com.sunright.inventory.service;

import com.sunright.inventory.dto.lov.LocationDTO;

public interface LocationService {
    LocationDTO saveLocation(LocationDTO input);
    LocationDTO findBy(String companyCode, int plantNo, String loc);
}
