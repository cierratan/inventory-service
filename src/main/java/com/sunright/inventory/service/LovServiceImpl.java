package com.sunright.inventory.service;

import com.sunright.inventory.dto.lov.CountryDTO;
import com.sunright.inventory.repository.lov.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LovServiceImpl implements LovService {

    @Autowired
    private CountryRepository countryRepository;

    @Override
    public List<CountryDTO> findAllCountries() {
        return Streamable.of(countryRepository.findAll()).stream().map(c -> CountryDTO.builder()
                .countryCode(c.getCountryCode())
                .description(c.getDescription())
                .build()).collect(Collectors.toList());
    }
}
