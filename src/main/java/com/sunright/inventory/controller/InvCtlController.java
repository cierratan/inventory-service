package com.sunright.inventory.controller;

import com.sunright.inventory.dto.InvCtlDTO;
import com.sunright.inventory.dto.SearchRequest;
import com.sunright.inventory.dto.SearchResult;
import com.sunright.inventory.service.InvCtlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("inventory-controls")
public class InvCtlController {
    @Autowired
    private InvCtlService invCtlService;

    @PostMapping
    public ResponseEntity<InvCtlDTO> create(@Valid @RequestBody InvCtlDTO invCtlDTO) {
        return new ResponseEntity<>(invCtlService.createInvCtl(invCtlDTO), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<InvCtlDTO> edit(@RequestBody InvCtlDTO invCtlDTO) {
        return new ResponseEntity<>(invCtlService.editInvCtl(invCtlDTO), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<InvCtlDTO> get() {
        return new ResponseEntity<>(invCtlService.findBy(), HttpStatus.OK);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete() {
        invCtlService.deleteInvCtl();
    }

    @PostMapping("search")
    public ResponseEntity<SearchResult<InvCtlDTO>> search(@RequestBody SearchRequest searchRequest) {
        return new ResponseEntity<>(invCtlService.searchBy(searchRequest), HttpStatus.OK);
    }
}
