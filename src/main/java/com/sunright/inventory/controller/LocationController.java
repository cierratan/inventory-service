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
@RequestMapping("locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @PostMapping
    public ResponseEntity<LocationDTO> create(@Valid @RequestBody LocationDTO location) {
        return new ResponseEntity<>(locationService.createLocation(location), HttpStatus.OK);
    }

    @PutMapping("{loc}")
    public ResponseEntity<LocationDTO> edit(@RequestBody LocationDTO location,
                                            @PathVariable String loc) {
        location.setLoc(loc);
        return new ResponseEntity<>(locationService.editLocation(location), HttpStatus.OK);
    }

    @GetMapping("{loc}")
    public ResponseEntity<LocationDTO> get(@PathVariable String loc) {
        return new ResponseEntity<>(locationService.findBy(loc), HttpStatus.OK);
    }

    @DeleteMapping("{loc}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String loc) {
        locationService.deleteLocation(loc);
    }

    @PostMapping("search")
    public ResponseEntity<SearchResult<LocationDTO>> search(@RequestBody SearchRequest searchRequest) {
        return new ResponseEntity<>(locationService.searchBy(searchRequest), HttpStatus.OK);
    }
}
