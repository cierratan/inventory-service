package com.sunright.inventory.controller;

import com.sunright.inventory.dto.grn.GrnDTO;
import com.sunright.inventory.dto.grn.GrnDetDTO;
import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.service.GrnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("grns")
public class GrnController {

    @Autowired
    private GrnService grnService;

    @GetMapping("print-label")
    public ResponseEntity<GrnDTO> generateLabel(HttpServletRequest request, HttpServletResponse response, @RequestParam String grnNo) {
        return new ResponseEntity<>(grnService.generateLabelGrn(request, response, grnNo), HttpStatus.OK);
    }

    @GetMapping("print-pick-list")
    public ResponseEntity<GrnDTO> generatePickList(HttpServletRequest request, HttpServletResponse response,
                                                   @RequestParam String grnNo, @RequestParam String projectNo,
                                                   @RequestParam String orderNo) {
        return new ResponseEntity<>(grnService.generatePickListGrn(request, response, grnNo, projectNo, orderNo), HttpStatus.OK);
    }

    @GetMapping("print-report-grn")
    public ResponseEntity<GrnDTO> generateReport(HttpServletRequest request, HttpServletResponse response,
                                                 @RequestParam String grnNo, @RequestParam String subType,
                                                 @RequestParam String type) {
        return new ResponseEntity<>(grnService.generateReportGrn(request, response, grnNo, subType, type), HttpStatus.OK);
    }

    @GetMapping("grn-no-manual")
    public ResponseEntity<DocmValueDTO> getGrnNoManual() {
        return new ResponseEntity<>(grnService.getGeneratedNoManual(), HttpStatus.OK);
    }

    @GetMapping("grn-no")
    public ResponseEntity<DocmValueDTO> getGrnNo() {
        return new ResponseEntity<>(grnService.getGeneratedNo(), HttpStatus.OK);
    }

    @PostMapping("check-next-item")
    public ResponseEntity<GrnDetDTO> nextItem(@RequestBody GrnDTO input) {
        return new ResponseEntity<>(grnService.checkNextItem(input), HttpStatus.OK);
    }

    @GetMapping("check-msrno-valid")
    public ResponseEntity<GrnDTO> valid(@RequestParam String msrNo) {
        return new ResponseEntity<>(grnService.checkIfMsrNoValid(msrNo), HttpStatus.OK);
    }

    @GetMapping("check-grnno-exists")
    public ResponseEntity<GrnDTO> exists(@RequestParam String grnNo) {
        return new ResponseEntity<>(grnService.checkIfGrnExists(grnNo), HttpStatus.OK);
    }

    @GetMapping("default-value")
    public ResponseEntity<GrnDTO> value() {
        return new ResponseEntity<>(grnService.getDefaultValueForGrnManual(), HttpStatus.OK);
    }

    @GetMapping("pono")
    public ResponseEntity<List<GrnDTO>> pono() {
        return new ResponseEntity<>(grnService.findAllPoNo(), HttpStatus.OK);
    }

    @GetMapping("partno")
    public ResponseEntity<List<GrnDetDTO>> partno(@RequestParam String poNo,
                                                  @RequestParam String partNo, @RequestParam String itemNo) {
        return new ResponseEntity<>(grnService.getAllPartNo(poNo, partNo, itemNo), HttpStatus.OK);
    }

    @GetMapping("detail")
    public ResponseEntity<GrnDetDTO> detail(@RequestParam String poNo, @RequestParam String itemNo,
                                            @RequestParam String partNo, @RequestParam Integer poRecSeq) {
        return new ResponseEntity<>(grnService.getGrnDetail(poNo, itemNo, partNo, poRecSeq), HttpStatus.OK);

    }

    @GetMapping("header")
    public ResponseEntity<GrnDTO> header(@RequestParam String poNo) {
        return new ResponseEntity<>(grnService.getGrnHeader(poNo), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<GrnDTO> create(@RequestBody GrnDTO grn) throws ParseException {
        return new ResponseEntity<>(grnService.createGrn(grn), HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    public ResponseEntity<GrnDTO> get(@PathVariable Long id) {
        return new ResponseEntity<>(grnService.findBy(id), HttpStatus.OK);
    }

    @PostMapping("search")
    public ResponseEntity<SearchResult<GrnDTO>> search(@RequestBody SearchRequest searchRequest) {
        return new ResponseEntity<>(grnService.searchBy(searchRequest), HttpStatus.OK);
    }
}
