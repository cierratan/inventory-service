package com.sunright.inventory.service;

import com.sunright.inventory.dto.company.CompanyDTO;

public interface CompanyService {

    CompanyDTO getCompanyAndPlantName();

    CompanyDTO getStockLocation();

    CompanyDTO getAccessCA();
}
