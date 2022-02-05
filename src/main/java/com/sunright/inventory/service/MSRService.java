package com.sunright.inventory.service;

import com.sunright.inventory.dto.grn.GrnSupplierDTO;
import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.msr.MsrDTO;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;

public interface MSRService {
    MsrDTO createMSR(MsrDTO input);
    MsrDTO findBy(Long id);
    SearchResult<MsrDTO> searchBy(SearchRequest searchRequest);
    DocmValueDTO getGeneratedNo();
    GrnSupplierDTO findSupplierByGrnNo(String grnNo);
}
