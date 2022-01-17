package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.company.CompanyDTO;
import com.sunright.inventory.entity.company.Company;
import com.sunright.inventory.exception.ErrorMessage;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.CompanyRepository;
import com.sunright.inventory.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Override
    public CompanyDTO getCompanyAndPlantName() {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        CompanyDTO dto = CompanyDTO.builder().build();
        List<Object[]> companyPlantName = companyRepository.companyAndPlantName(userProfile.getCompanyCode(), userProfile.getPlantNo());
        if (companyPlantName == null || companyPlantName.size() == 0) {
            ErrorMessage.builder().message("No record found in COMPANY table !").build();
        } else {
            for (Object[] data : companyPlantName) {
                dto.setCompanyName((String) data[0]);
            }
        }

        return dto;
    }

    @Override
    public CompanyDTO getStockLocation() {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        CompanyDTO dto = CompanyDTO.builder().build();
        Optional<Company> companyOptional = companyRepository.findCompanyById_CompanyCodeAndId_PlantNo(userProfile.getCompanyCode(), userProfile.getPlantNo());
        companyOptional.ifPresent(company -> dto.setStockLoc(company.getStockLoc()));
        return dto;
    }

    @Override
    public CompanyDTO getAccessCA() {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        CompanyDTO dto = CompanyDTO.builder().build();
        Optional<Company> companyOptional = companyRepository.findCompanyById_CompanyCodeAndId_PlantNo(userProfile.getCompanyCode(), userProfile.getPlantNo());
        if (companyOptional.isPresent()) {
            dto.setAccessCa(companyOptional.get().getAccessCa());
        } else {
            ErrorMessage.builder().message("No such Company has been setup !!").build();
        }
        return dto;
    }
}