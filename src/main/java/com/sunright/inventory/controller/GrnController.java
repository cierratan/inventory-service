package com.sunright.inventory.controller;

import com.sunright.inventory.dto.*;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.service.GrnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("grns")
public class GrnController {

    @Autowired
    private GrnService grnService;

    @GetMapping("pono")
    public ResponseEntity getAllPoNo() {
        UserProfile userProfile = UserProfileContext.getUserProfile();
        Map<String, Object> result = grnService.getAllPoNo(userProfile);
        try {
            return ResponseEntity.ok(Response.builder()
                    .timeStamp(LocalDateTime.now())
                    .data(new HashMap<String, Object>() {{
                        put("grn", result.get("contentData"));
                    }})
                    .totalRecords((Long) result.get("totalRecords"))
                    .message("Fetching all PO No.")
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

    @PostMapping("partno")
    public ResponseEntity getAllPartNo(@RequestBody @Valid GrnDTO grnDTO) {
        UserProfile userProfile = UserProfileContext.getUserProfile();
        Map<String, Object> result = grnService.getAllPartNo(grnDTO, userProfile);
        try {
            return ResponseEntity.ok(Response.builder()
                    .timeStamp(LocalDateTime.now())
                    .data(new HashMap<String, Object>() {{
                        put("grn", result.get("contentData"));
                    }})
                    .totalRecords((Long) result.get("totalRecords"))
                    .message("Fetching Data from Part No. ")
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

    @PostMapping("detail")
    public ResponseEntity<Object> getGrnDetail(@RequestBody GrnDTO grnDTO, GrnDetDTO grnDetDTO) {
        Map<String, Object> result = grnService.getGrnDetail(grnDTO,grnDetDTO);
        try {
            return ResponseEntity.ok(Response.builder()
                    .timeStamp(LocalDateTime.now())
                    .data(new HashMap<String, Object>() {{
                        put("grn", result.get("contentData"));
                    }})
                    .totalRecords((Long) result.get("totalRecords"))
                    .message("Fetching All Data for GRN Detail")
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

    @PostMapping("header")
    public ResponseEntity<Object> getGrnHeader(@RequestBody @Valid GrnDTO grnDTO) {
        Map<String, Object> result = grnService.getGrnHeader(grnDTO);
        try {
            return ResponseEntity.ok(Response.builder()
                    .timeStamp(LocalDateTime.now())
                    .data(new HashMap<String, Object>() {{
                        put("grn", result.get("contentData"));
                    }})
                    .totalRecords((Long) result.get("totalRecords"))
                    .message("Fetching All Data for GRN Header")
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
                        .totalRecords(0L)
                        .message(result.get("grnExists").toString())
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
            }
            return ResponseEntity.ok(Response.builder()
                    .timeStamp(LocalDateTime.now())
                    .data(new HashMap<String, Object>() {{
                        put("grn", result.get("contentData"));
                    }})
                    .totalRecords((Long) result.get("totalRecords"))
                    .message(result.get("successSave").toString())
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

    @PostMapping("search")
    public ResponseEntity<SearchResult<GrnDTO>> search(@RequestBody SearchRequest searchRequest) {
        return new ResponseEntity<>(grnService.searchBy(searchRequest), HttpStatus.OK);
    }
}
