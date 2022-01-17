package com.sunright.inventory.controller;

import com.sunright.inventory.dto.company.CompanyDTO;
import com.sunright.inventory.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("companys")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping("initialise")
    public ResponseEntity<CompanyDTO> get() {
        return new ResponseEntity<>(companyService.getCompanyAndPlantName(), HttpStatus.OK);
    }

    @GetMapping("stock-location")
    public ResponseEntity<CompanyDTO> stockLoc() {
        return new ResponseEntity<>(companyService.getStockLocation(), HttpStatus.OK);
    }

    @GetMapping("access-ca")
    public ResponseEntity<CompanyDTO> access() {
        return new ResponseEntity<>(companyService.getAccessCA(), HttpStatus.OK);
    }
}
