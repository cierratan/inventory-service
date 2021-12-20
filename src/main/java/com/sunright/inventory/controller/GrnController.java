package com.sunright.inventory.controller;

import com.sunright.inventory.dto.GrnDTO;
import com.sunright.inventory.service.GrnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/grn")
public class GrnController {

    @Autowired
    private GrnService grnService;

    @PostMapping("/createGrn")
    public ResponseEntity<GrnDTO> createGrn(@RequestBody @Valid GrnDTO grnDTO) {
        return new ResponseEntity<>(grnService.createGrn(grnDTO), HttpStatus.CREATED);
    }

    @GetMapping("/list")
    public ResponseEntity listGrn() {
        return new ResponseEntity<>(grnService.list(30), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{grnNo}")
    public ResponseEntity deleteGrn(@PathVariable("grnNo") GrnDTO grnDTO) {
        return new ResponseEntity<>(grnService.delete(grnDTO), HttpStatus.OK);
    }

    @PutMapping("/update/{grnNo}")
    public ResponseEntity updateGrn(@PathVariable("grnNo") @RequestBody GrnDTO grnDTO) {
        return new ResponseEntity<>(grnService.update(grnDTO), HttpStatus.OK);
    }
}
