package com.sunright.inventory.controller;

import com.sunright.inventory.service.InvPeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("inventory")
public class CheckInvPeriodController {

    @Autowired
    private InvPeriodService invPeriodService;

    @GetMapping("check-period")
    public ResponseEntity<Boolean> checkInvPeriod() {
        return new ResponseEntity<>(invPeriodService.checkInvPeriod(), HttpStatus.OK);
    }
}
