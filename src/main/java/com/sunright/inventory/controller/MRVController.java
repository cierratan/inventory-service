package com.sunright.inventory.controller;

import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.service.MRVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("mrv")
public class MRVController {

    @Autowired
    private MRVService mrvService;

    @GetMapping("mrv-no")
    public ResponseEntity<DocmValueDTO> getMsrNo() {
        return new ResponseEntity<>(mrvService.getGeneratedNo(), HttpStatus.OK);
    }
}
