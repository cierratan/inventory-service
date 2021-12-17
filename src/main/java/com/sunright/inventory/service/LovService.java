package com.sunright.inventory.service;

import com.sunright.inventory.dto.lov.CountryDTO;

import java.util.List;

public interface LovService {
    List<CountryDTO> findAllCountries();
}
