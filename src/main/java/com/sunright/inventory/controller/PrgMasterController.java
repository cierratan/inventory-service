package com.sunright.inventory.controller;

import com.sunright.inventory.dto.prgmaster.PrgMasterDTO;
import com.sunright.inventory.service.PrgMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("prgmaster")
public class PrgMasterController {

    @Autowired
    private PrgMasterService prgMasterService;

    @GetMapping("program-title")
    public ResponseEntity<PrgMasterDTO> get(@RequestParam String prgId, @RequestParam String moduleCd) {
        return new ResponseEntity<>(prgMasterService.getProgramTitle(prgId, moduleCd), HttpStatus.OK);
    }
}
