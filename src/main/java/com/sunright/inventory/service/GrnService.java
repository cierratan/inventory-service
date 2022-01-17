package com.sunright.inventory.service;

import com.sunright.inventory.dto.grn.GrnDTO;
import com.sunright.inventory.dto.grn.GrnDetDTO;
import com.sunright.inventory.dto.msr.MsrDTO;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;

import java.util.List;

public interface GrnService {

    GrnDTO createGrn(GrnDTO input);

    GrnDTO findBy(String grnNo, String subType);

    SearchResult<GrnDTO> searchBy(SearchRequest searchRequest);

    List<GrnDTO> findAllPoNo();

    GrnDTO getGrnHeader(String poNo);

    List<GrnDetDTO> getAllPartNo(String poNo);

    GrnDetDTO getGrnDetail(String poNo, String itemNo, String partNo, Integer poRecSeq);

    GrnDTO getDefaultValueForGrnManual();

    GrnDTO checkIfGrnExists(String grnNo);

    MsrDTO checkIfMsrNoValid(String msrNo);

    GrnDetDTO checkNextItem(GrnDTO input);
}