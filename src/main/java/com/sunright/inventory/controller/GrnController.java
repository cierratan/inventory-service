package com.sunright.inventory.controller;

import com.sunright.inventory.dto.GrnDTO;
import com.sunright.inventory.dto.GrnDetDTO;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.service.GrnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("grns")
public class GrnController {

    @Autowired
    private GrnService grnService;

    @GetMapping("pono")
    public ResponseEntity<List<GrnDTO>> allPoNo() {
        return new ResponseEntity<>(grnService.findAllPoNo(), HttpStatus.OK);
    }

    @GetMapping("partno")
    public ResponseEntity<List<GrnDetDTO>> allPartNo(@RequestParam String poNo) {
        return new ResponseEntity<>(grnService.getAllPartNo(poNo), HttpStatus.OK);
    }

    @GetMapping("detail")
    public ResponseEntity<List<GrnDetDTO>> detail(@RequestParam String poNo, @RequestParam String itemNo, @RequestParam String partNo, @RequestParam Integer poRecSeq) {
        return new ResponseEntity<>(grnService.getGrnDetail(poNo, itemNo, partNo, poRecSeq), HttpStatus.OK);

    }

    @GetMapping("header")
    public ResponseEntity<List<GrnDTO>> header(@RequestParam String poNo) {
        return new ResponseEntity<>(grnService.getGrnHeader(poNo), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<GrnDTO> create(@RequestBody GrnDTO grn) {
        return new ResponseEntity<>(grnService.createGrn(grn), HttpStatus.CREATED);
    }

    @GetMapping("{grnNo}-{subType}")
    public ResponseEntity<GrnDTO> get(@PathVariable String grnNo, @PathVariable String subType) {
        return new ResponseEntity<>(grnService.findBy(grnNo, subType), HttpStatus.OK);
    }

    @PostMapping("search")
    public ResponseEntity<SearchResult<GrnDTO>> search(@RequestBody SearchRequest searchRequest) {
        return new ResponseEntity<>(grnService.searchBy(searchRequest), HttpStatus.OK);
    }
}
