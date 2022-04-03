package com.sunright.inventory.controller;

import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.mrv.MrvDTO;
import com.sunright.inventory.dto.mrv.MrvDetailDTO;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.service.MRVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("mrv")
public class MRVController {

    @Autowired
    private MRVService mrvService;

    @GetMapping("mrv-no")
    public ResponseEntity<DocmValueDTO> getMsrNo() {
        return new ResponseEntity<>(mrvService.getGeneratedNo(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<MrvDTO> create(@RequestBody MrvDTO mrv) {
        return new ResponseEntity<>(mrvService.createMrv(mrv), HttpStatus.OK);
    }

    @GetMapping("siv")
    public ResponseEntity<MrvDetailDTO> findBySiv(@RequestParam String sivNo) {
        return new ResponseEntity<>(mrvService.findSivAndPopulateMRVDetails(sivNo), HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<MrvDTO> get(@PathVariable Long id) {
        return new ResponseEntity<>(mrvService.findBy(id), HttpStatus.OK);
    }

    @PostMapping("search")
    public ResponseEntity<SearchResult<MrvDTO>> search(@RequestBody SearchRequest searchRequest) {
        return new ResponseEntity<>(mrvService.searchBy(searchRequest), HttpStatus.OK);
    }

    @GetMapping("mrv-no/{mrvNo}")
    public ResponseEntity<MrvDTO> getByMrvNo(@PathVariable String mrvNo) {
        return new ResponseEntity<>(mrvService.findBy(mrvNo), HttpStatus.OK);
    }
}
