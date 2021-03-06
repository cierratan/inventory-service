package com.sunright.inventory.controller;

import com.sunright.inventory.dto.grn.GrnSupplierDTO;
import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.msr.MsrDTO;
import com.sunright.inventory.dto.msr.MsrDetailDTO;
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

    @GetMapping("suppliers")
    public ResponseEntity<GrnSupplierDTO> getSupplierByGrnNo(@RequestParam String grnNo) {
        return new ResponseEntity<>(msrService.findSupplierByGrnNo(grnNo), HttpStatus.OK);
    }

    @GetMapping("mrv/{mrvId}")
    public ResponseEntity<MsrDetailDTO> populateMsrDetailByMrv(@PathVariable Long mrvId) {
        return new ResponseEntity<>(msrService.populateMsrDetailByMrv(mrvId), HttpStatus.OK);
    }
}
