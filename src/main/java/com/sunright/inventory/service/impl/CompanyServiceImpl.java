package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.company.CompanyDTO;
import com.sunright.inventory.entity.company.CompanyProjection;
import com.sunright.inventory.exception.NotFoundException;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.CompanyRepository;
import com.sunright.inventory.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Override
    public CompanyDTO getCompanyAndPlantName() {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        CompanyProjection company = companyRepository.getCompanyAndPlantName(userProfile.getCompanyCode(), userProfile.getPlantNo());
        if (company == null) {
            throw new NotFoundException("No record found in COMPANY table !");
        }
        return CompanyDTO.builder()
                .companyName(company.getCompanyName())
                .plantName(company.getPlantName()).build();
    }

    @Override
    public CompanyDTO getStockLocation() {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        CompanyProjection company = companyRepository.getStockLoc(userProfile.getCompanyCode(), userProfile.getPlantNo());
        if (company == null) {
            throw new NotFoundException("No record found in COMPANY table !");
        }
        return CompanyDTO.builder()
                .stockLoc(company.getStockLoc()).build();
    }

    @Override
    public CompanyDTO getAccessCA() {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        CompanyProjection company = companyRepository.getAccCa(userProfile.getCompanyCode(), userProfile.getPlantNo());
        if (company == null) {
            throw new NotFoundException("No record found in COMPANY table !");
        }
        return CompanyDTO.builder()
                .accessCa(company.getAccessCa()).build();
    }
}