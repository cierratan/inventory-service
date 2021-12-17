package com.sunright.inventory.controller;

import com.sunright.inventory.dto.lov.CountryDTO;
import com.sunright.inventory.service.LovService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("lov")
public class LovController {

    @Autowired
    private LovService lovService;

    @GetMapping("countries")
    public ResponseEntity<List<CountryDTO>> getCountries() {
        return new ResponseEntity<>(lovService.findAllCountries(), HttpStatus.OK);
    }
}
