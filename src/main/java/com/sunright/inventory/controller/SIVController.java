package com.sunright.inventory.controller;

import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.dto.siv.SIVDTO;
import com.sunright.inventory.dto.siv.SIVDetailDTO;
import com.sunright.inventory.service.SIVService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("siv")
public class SIVController {

    @Autowired
    private SIVService sivService;

    @PostMapping("label")
    public ResponseEntity<byte[]> label(@RequestBody SIVDTO input) throws SQLException,
            JRException, FileNotFoundException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", input.getSivNo() + "_Label" + ".pdf");
        return new ResponseEntity<>(sivService.generatedLabelSIV(input), headers, HttpStatus.OK);
    }

    @PostMapping("report")
    public ResponseEntity<byte[]> report(@RequestBody SIVDTO input) throws SQLException,
            JRException, IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", input.getSivNo() + "_Report" + ".pdf");
        return new ResponseEntity<>(sivService.generatedReportSIV(input), headers, HttpStatus.OK);
    }

    @PostMapping("populate-detail-manual")
    public ResponseEntity<List<SIVDetailDTO>> populateDetManual(@RequestBody SIVDTO input) {
        return new ResponseEntity<>(sivService.populateSIVManualDetails(input), HttpStatus.OK);
    }

    @GetMapping("default-value")
    public ResponseEntity<SIVDTO> defValue(@RequestParam String subType) throws ParseException {
        return new ResponseEntity<>(sivService.getDefaultValueSIV(subType), HttpStatus.OK);
    }

    @PostMapping("check-next-item")
    public ResponseEntity<SIVDetailDTO> nextItem(@RequestBody SIVDTO input) {
        return new ResponseEntity<>(sivService.checkNextItem(input), HttpStatus.OK);
    }

    @PostMapping("check-valid-item-no")
    public ResponseEntity<SIVDetailDTO> checkItemNo(@RequestBody SIVDTO input) {
        return new ResponseEntity<>(sivService.checkValidItemNo(input), HttpStatus.OK);
    }

    @PostMapping("check-valid-issued-qty")
    public ResponseEntity<SIVDetailDTO> checkIssuedQty(@RequestBody SIVDTO input) {
        return new ResponseEntity<>(sivService.checkValidIssuedQty(input), HttpStatus.OK);
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
    public ResponseEntity<SIVDTO> create(@Valid @RequestBody SIVDTO siv) {
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
