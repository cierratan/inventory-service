package com.sunright.inventory.controller;

import com.sunright.inventory.dto.ItemDTO;
import com.sunright.inventory.dto.grn.GrnDTO;
import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.lov.LocationDTO;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.dto.siv.SIVDTO;
import com.sunright.inventory.dto.siv.SIVDetailDTO;
import com.sunright.inventory.service.SIVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("siv")
public class SIVController {

    @Autowired
    private SIVService sivService;

    @GetMapping("default-value")
    public ResponseEntity<SIVDTO> defValue(@RequestParam String subType) throws ParseException {
        return new ResponseEntity<>(sivService.getDefaultValueSIVEntry(subType), HttpStatus.OK);
    }

    @PostMapping("check-valid-issued-qty")
    public ResponseEntity<SIVDetailDTO> checkValid(@RequestBody SIVDetailDTO input) {
        return new ResponseEntity<>(sivService.checkValidIssuedQty(input), HttpStatus.OK);
    }

    @PostMapping("check-next-item")
    public ResponseEntity<List<SIVDetailDTO>> nextItem(@RequestBody SIVDTO input) {
        return new ResponseEntity<>(sivService.checkNextItem(input), HttpStatus.OK);
    }

    @GetMapping("item")
    public ResponseEntity<List<ItemDTO>> getItemNo() {
        return new ResponseEntity<>(sivService.getItemNo(), HttpStatus.OK);
    }

    @GetMapping("loc")
    public ResponseEntity<List<LocationDTO>> getLoc() {
        return new ResponseEntity<>(sivService.getLocAndDesc(), HttpStatus.OK);
    }

    @GetMapping("project-no")
    public ResponseEntity<List<SIVDTO>> getProjectNo() {
        return new ResponseEntity<>(sivService.getProjectNoByStatus(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<SIVDTO> create(@RequestBody SIVDTO siv) {
        return new ResponseEntity<>(sivService.createSIV(siv), HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<SIVDTO> get(@PathVariable Long id) {
        return new ResponseEntity<>(sivService.findBy(id), HttpStatus.OK);
    }

    @PostMapping("search")
    public ResponseEntity<SearchResult<SIVDTO>> search(@RequestBody SearchRequest searchRequest) {
        return new ResponseEntity<>(sivService.searchBy(searchRequest), HttpStatus.OK);
    }

    @PostMapping("siv-no")
    public ResponseEntity<DocmValueDTO> getSivNo(@RequestBody SIVDTO siv) {
        return new ResponseEntity<>(sivService.getGeneratedNoSIV(siv), HttpStatus.OK);
    }
}
