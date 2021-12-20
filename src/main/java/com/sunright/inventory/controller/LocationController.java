package com.sunright.inventory.controller;

import com.sunright.inventory.dto.SearchRequest;
import com.sunright.inventory.dto.SearchResult;
import com.sunright.inventory.dto.lov.LocationDTO;
import com.sunright.inventory.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("companies/{companyCode}/plants/{plantNo}/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @PostMapping
    public ResponseEntity<LocationDTO> create(@Valid @RequestBody LocationDTO location,
                                              @PathVariable String companyCode, @PathVariable int plantNo) {

        location.setCompanyCode(companyCode);
        location.setPlantNo(plantNo);

        return new ResponseEntity<>(locationService.saveLocation(location), HttpStatus.OK);
    }

    @PutMapping("{loc}")
    public ResponseEntity<LocationDTO> edit(@RequestBody LocationDTO location,
                                            @PathVariable String companyCode, @PathVariable int plantNo,
                                            @PathVariable String loc) {
        location.setCompanyCode(companyCode);
        location.setPlantNo(plantNo);
        location.setLoc(loc);

        return new ResponseEntity<>(locationService.saveLocation(location), HttpStatus.OK);
    }

    @GetMapping("{loc}")
    public ResponseEntity<LocationDTO> get(@PathVariable String companyCode, @PathVariable int plantNo,
                                            @PathVariable String loc) {
        return new ResponseEntity<>(locationService.findBy(companyCode, plantNo, loc), HttpStatus.OK);
    }

    @PostMapping("search")
    public ResponseEntity<SearchResult<LocationDTO>> search(@RequestBody SearchRequest searchRequest) {
        return new ResponseEntity<>(locationService.searchBy(searchRequest), HttpStatus.OK);
    }
}
