package com.sunright.inventory.controller;

import com.sunright.inventory.dto.lov.*;
import com.sunright.inventory.service.LovService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("sources")
    public ResponseEntity<List<DefaultCodeDetailDTO>> getSources() {
        return new ResponseEntity<>(lovService.findSources(), HttpStatus.OK);
    }

    @GetMapping("uom")
    public ResponseEntity<List<DefaultCodeDetailDTO>> getUOMs() {
        return new ResponseEntity<>(lovService.findUOMs(), HttpStatus.OK);
    }

    @GetMapping("item-categories")
    public ResponseEntity<List<ItemCatDTO>> getItemCategories() {
        return new ResponseEntity<>(lovService.findItemCategories(), HttpStatus.OK);
    }

    @GetMapping("sub-categories")
    public ResponseEntity<List<CategorySubDTO>> getSubCategories(@RequestParam String categoryCode) {
        return new ResponseEntity<>(lovService.findSubCategories(categoryCode), HttpStatus.OK);
    }

    @GetMapping("msl")
    public ResponseEntity<List<CodeDescDTO>> getMSLs() {
        return new ResponseEntity<>(lovService.findMSL(), HttpStatus.OK);
    }

    @GetMapping("category-groups")
    public ResponseEntity<List<ValueDescDTO>> getCategoryGroups() {
        return new ResponseEntity<>(lovService.findCategoryGroups(), HttpStatus.OK);
    }
}
