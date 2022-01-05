package com.sunright.inventory.controller;

import com.sunright.inventory.dto.DocmNoDTO;
import com.sunright.inventory.dto.GrnDTO;
import com.sunright.inventory.dto.GrnDetDTO;
import com.sunright.inventory.dto.Response;
import com.sunright.inventory.exception.ErrorMessage;
import com.sunright.inventory.service.GrnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("grn")
public class GrnController {

    @Autowired
    private GrnService grnService;

    @PostMapping("checkItemNoAndPartNo")
    public ResponseEntity<Object> checkItemNoAndPartNo(@RequestBody @Valid GrnDTO grnDTO, GrnDetDTO grnDetDTO) {
        Map<String, Object> result = grnService.checkItemNoAndPartNo(grnDTO, grnDetDTO);
        try {
            return ResponseEntity.ok(Response.builder()
                    .timeStamp(LocalDateTime.now())
                    .data(new HashMap<String, Object>() {{
                        put("grn", result.get("contentData"));
                    }})
                    .totalRecords((Long) result.get("totalRecords"))
                    .message("Check Item No and Part No")
                    .status(OK)
                    .statusCode(OK.value())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(Response.builder()
                    .timeStamp(LocalDateTime.now())
                    .data(new HashMap<String, Object>() {{
                        put("grn", result.get("contentData"));
                    }})
                    .totalRecords((Long) result.get("totalRecords"))
                    .message(e.getMessage())
                    .status(INTERNAL_SERVER_ERROR)
                    .statusCode(INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    @PostMapping("getPurDetInfo")
    public ResponseEntity<Object> getPurDetInfo(@RequestBody @Valid GrnDTO grnDTO) {
        Map<String, Object> result = grnService.getPurDetInfo(grnDTO);
        try {
            return ResponseEntity.ok(Response.builder()
                    .timeStamp(LocalDateTime.now())
                    .data(new HashMap<String, Object>() {{
                        put("grn", result.get("contentData"));
                    }})
                    .totalRecords((Long) result.get("totalRecords"))
                    .message("Fetching Grn Detail")
                    .status(OK)
                    .statusCode(OK.value())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(Response.builder()
                    .timeStamp(LocalDateTime.now())
                    .data(new HashMap<String, Object>() {{
                        put("grn", result.get("contentData"));
                    }})
                    .totalRecords((Long) result.get("totalRecords"))
                    .message(e.getMessage())
                    .status(INTERNAL_SERVER_ERROR)
                    .statusCode(INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    @PostMapping("getPurInfo")
    public ResponseEntity<Object> getPurInfo(@RequestBody @Valid GrnDTO grnDTO) {
        Map<String, Object> result = grnService.getPurInfo(grnDTO);
        try {
            return ResponseEntity.ok(Response.builder()
                    .timeStamp(LocalDateTime.now())
                    .data(new HashMap<String, Object>() {{
                        put("grn", result.get("contentData"));
                    }})
                    .totalRecords((Long) result.get("totalRecords"))
                    .message("Fetching Pur Info")
                    .status(OK)
                    .statusCode(OK.value())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(Response.builder()
                    .timeStamp(LocalDateTime.now())
                    .data(new HashMap<String, Object>() {{
                        put("grn", result.get("contentData"));
                    }})
                    .totalRecords((Long) result.get("totalRecords"))
                    .message(e.getMessage())
                    .status(INTERNAL_SERVER_ERROR)
                    .statusCode(INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    @PostMapping("checkStatusPoNo")
    public ResponseEntity<Object> checkStatusPoNo(@RequestBody @Valid GrnDTO grnDTO) {
        ErrorMessage result = grnService.checkStatusPoNo(grnDTO);
        try {
            return ResponseEntity.ok(Response.builder()
                    .timeStamp(LocalDateTime.now())
                    .data(new HashMap<String, Object>() {{
                        put("grn", result.getMessage());
                    }})
                    .totalRecords(1L)
                    .message("Check Status PO NO")
                    .status(OK)
                    .statusCode(OK.value())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(Response.builder()
                    .timeStamp(LocalDateTime.now())
                    .data(new HashMap<String, Object>() {{
                        put("grn", result.getMessage());
                    }})
                    .totalRecords(1L)
                    .message(e.getMessage())
                    .status(INTERNAL_SERVER_ERROR)
                    .statusCode(INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    @PostMapping("getGeneratedGrnNo")
    public ResponseEntity<Object> getGeneratedGrnNo(@RequestBody @Valid DocmNoDTO docmNoDTO) {
        Map<String, Object> result = grnService.getLastGeneratedNoforGRN(docmNoDTO);
        try {
            return ResponseEntity.ok(Response.builder()
                    .timeStamp(LocalDateTime.now())
                    .data(new HashMap<String, Object>() {{
                        put("grn", result.get("contentData"));
                    }})
                    .totalRecords((Long) result.get("totalRecords"))
                    .message("Generated number for GRN")
                    .status(OK)
                    .statusCode(OK.value())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(Response.builder()
                    .timeStamp(LocalDateTime.now())
                    .data(new HashMap<String, Object>() {{
                        put("grn", result.get("contentData"));
                    }})
                    .totalRecords((Long) result.get("totalRecords"))
                    .message(e.getMessage())
                    .status(INTERNAL_SERVER_ERROR)
                    .statusCode(INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody @Valid GrnDTO grnDTO) {
        Map<String, Object> result = grnService.create(grnDTO);
        try {
            if (result.get("grnExists") != null) {
                return ResponseEntity.ok(Response.builder()
                        .timeStamp(LocalDateTime.now())
                        .data(new HashMap<String, Object>() {{
                            put("grn", result.get("grnExists"));
                        }})
                        .totalRecords((Long) result.get("totalRecords"))
                        .message(result.get("grnExists").toString())
                        .status(CREATED)
                        .statusCode(CREATED.value())
                        .build());
            }
            return ResponseEntity.ok(Response.builder()
                    .timeStamp(LocalDateTime.now())
                    .data(new HashMap<String, Object>() {{
                        put("grn", result.get("contentData"));
                    }})
                    .totalRecords((Long) result.get("totalRecords"))
                    .message("Completed")
                    .status(CREATED)
                    .statusCode(CREATED.value())
                    .build());

        } catch (Exception e) {
            return ResponseEntity.ok(Response.builder()
                    .timeStamp(LocalDateTime.now())
                    .data(new HashMap<String, Object>() {{
                        put("grn", result.get("contentData"));
                    }})
                    .totalRecords((Long) result.get("totalRecords"))
                    .message(e.getMessage())
                    .status(INTERNAL_SERVER_ERROR)
                    .statusCode(INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    @GetMapping
    public ResponseEntity get() {
        return ResponseEntity.ok(Response.builder()
                .timeStamp(LocalDateTime.now())
                .data(new HashMap<String, Object>() {{
                    put("grn", grnService.get());
                }})
                .message("Fetching all GRN")
                .status(OK)
                .statusCode(OK.value())
                .build());
    }
}
