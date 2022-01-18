package com.sunright.inventory.controller;

import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.msr.MsrDTO;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.service.MSRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("msr")
public class MSRController {
    @Autowired
    private MSRService msrService;

    @PostMapping
    public ResponseEntity<MsrDTO> create(@RequestBody MsrDTO msr) {
        return new ResponseEntity<>(msrService.createMSR(msr), HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<MsrDTO> get(@PathVariable Long id) {
        return new ResponseEntity<>(msrService.findBy(id), HttpStatus.OK);
    }

    @PostMapping("search")
    public ResponseEntity<SearchResult<MsrDTO>> search(@RequestBody SearchRequest searchRequest) {
        return new ResponseEntity<>(msrService.searchBy(searchRequest), HttpStatus.OK);
    }

    @GetMapping("msr-no")
    public ResponseEntity<DocmValueDTO> getMsrNo() {
        return new ResponseEntity<>(msrService.getGeneratedNo(), HttpStatus.OK);
    }
}
