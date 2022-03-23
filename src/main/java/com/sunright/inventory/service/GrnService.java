package com.sunright.inventory.service;

import com.sunright.inventory.dto.grn.GrnDTO;
import com.sunright.inventory.dto.grn.GrnDetDTO;
import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;

import java.text.ParseException;
import java.util.List;

public interface GrnService {

    GrnDTO createGrn(GrnDTO input);

    GrnDTO findBy(Long id);

    SearchResult<GrnDTO> searchBy(SearchRequest searchRequest);

    List<GrnDTO> findAllPoNo();

    GrnDTO getGrnHeader(String poNo);

    List<GrnDetDTO> getAllPartNo(String poNo, String partNo, String itemNo);

    GrnDetDTO getGrnDetail(String poNo, String itemNo, String partNo, Integer poRecSeq);

    GrnDTO getDefaultValueForGrnManual();

    GrnDTO checkIfGrnExists(String grnNo);

    GrnDTO checkIfMsrNoValid(String msrNo);

    GrnDetDTO checkNextItem(GrnDTO input);

    DocmValueDTO getGeneratedNo();

    DocmValueDTO getGeneratedNoManual();

    byte[] generatedReportGRN(GrnDTO input);

    byte[] generatedPickedListGRN(GrnDTO input);

    byte[] generatedLabelGRN(GrnDTO input);

    List<GrnDetDTO> showPartNoByMSR(GrnDTO input);

    List<GrnDetDTO> showItemPart(GrnDTO input);
}