package com.sunright.inventory.service;

import com.sunright.inventory.dto.GrnDTO;
import com.sunright.inventory.dto.GrnDetDTO;
import com.sunright.inventory.dto.SearchRequest;
import com.sunright.inventory.dto.SearchResult;

import java.util.List;

public interface GrnService {

    GrnDTO createGrn(GrnDTO input);

    GrnDTO findBy(String grnNo, String subType);

    SearchResult<GrnDTO> searchBy(SearchRequest searchRequest);

    List<GrnDTO> findAllPoNo();

    List<GrnDTO> getGrnHeader(String poNo);

    List<GrnDetDTO> getAllPartNo(String poNo);

    List<GrnDetDTO> getGrnDetail(String poNo, String itemNo, String partNo, Integer poRecSeq);
}