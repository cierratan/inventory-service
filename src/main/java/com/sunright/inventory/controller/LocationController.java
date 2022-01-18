package com.sunright.inventory.controller;

import com.sunright.inventory.dto.lov.LocationDTO;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
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

    @PutMapping("{id}")
    public ResponseEntity<LocationDTO> edit(@RequestBody LocationDTO location,
                                            @PathVariable Long id) {
        location.setId(id);
        return new ResponseEntity<>(locationService.editLocation(location), HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<LocationDTO> get(@PathVariable Long id) {
        return new ResponseEntity<>(locationService.findBy(id), HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        locationService.deleteLocation(id);
    }

    @PostMapping("search")
    public ResponseEntity<SearchResult<LocationDTO>> search(@RequestBody SearchRequest searchRequest) {
        return new ResponseEntity<>(locationService.searchBy(searchRequest), HttpStatus.OK);
    }
}
