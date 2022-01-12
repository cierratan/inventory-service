package com.sunright.inventory.controller;

import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.dto.UomDTO;
import com.sunright.inventory.service.UOMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("uoms")
public class UOMController {
    /**
     * ==============================================================================================================
     * UOM Maintenance
     * ==============================================================================================================
     */

    @Autowired
    private UOMService uomService;

    @PostMapping
    public ResponseEntity<UomDTO> createUOM(@Valid @RequestBody UomDTO uom) {
        return new ResponseEntity<>(uomService.createUOM(uom), HttpStatus.OK);
    }

    @PutMapping("{uomFrom}-{uomTo}")
    public ResponseEntity<UomDTO> editUOM(@RequestBody UomDTO uom,
                                          @PathVariable String uomFrom, @PathVariable String uomTo) {
        uom.setUomFrom(uomFrom);
        uom.setUomTo(uomTo);

        return new ResponseEntity<>(uomService.editUOM(uom), HttpStatus.OK);
    }

    @GetMapping("{uomFrom}-{uomTo}")
    public ResponseEntity<UomDTO> getUOM(@PathVariable String uomFrom, @PathVariable String uomTo) {
        return new ResponseEntity<>(uomService.getUOM(uomFrom, uomTo), HttpStatus.OK);
    }

    @DeleteMapping("{uomFrom}-{uomTo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String uomFrom, @PathVariable String uomTo) {
        uomService.deleteUOM(uomFrom, uomTo);
    }

    @PostMapping("search")
    public ResponseEntity<SearchResult<UomDTO>> searchUOM(@RequestBody SearchRequest searchRequest) {
        return new ResponseEntity<>(uomService.searchBy(searchRequest), HttpStatus.OK);
    }
}
