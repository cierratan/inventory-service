package com.sunright.inventory.controller;

import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.dto.siv.SIVDTO;
import com.sunright.inventory.dto.siv.SIVDetailDTO;
import com.sunright.inventory.service.SIVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("siv")
public class SIVController {

    @Autowired
    private SIVService sivService;

    @PostMapping("docm-no-next-item")
    public ResponseEntity<List<SIVDetailDTO>> nextItem(@RequestBody SIVDTO input) {
        return new ResponseEntity<>(sivService.getSIVManualDetails(input), HttpStatus.OK);
    }

    @GetMapping("default-value")
    public ResponseEntity<SIVDTO> defValue(@RequestParam String subType) throws ParseException {
        return new ResponseEntity<>(sivService.getDefaultValueSIV(subType), HttpStatus.OK);
    }

    @GetMapping("check-valid-issued-qty")
    public ResponseEntity<SIVDetailDTO> check(@RequestParam String projectNo,
                                              @RequestParam String itemNo,
                                              @RequestParam BigDecimal issuedQty,
                                              @RequestParam int seqNo) {
        return new ResponseEntity<>(sivService.checkValidIssuedQty(projectNo, itemNo, issuedQty, seqNo), HttpStatus.OK);
    }

    @GetMapping("populate-batch-list")
    public ResponseEntity<List<SIVDetailDTO>> populateBatc(@RequestParam String projectNo) {
        return new ResponseEntity<>(sivService.populateBatchList(projectNo), HttpStatus.OK);
    }

    @GetMapping("populate-detail")
    public ResponseEntity<List<SIVDetailDTO>> populateDet(@RequestParam String projectNo) {
        return new ResponseEntity<>(sivService.populateSivDetail(projectNo), HttpStatus.OK);
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
