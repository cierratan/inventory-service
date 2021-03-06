package com.sunright.inventory.service;

import com.sunright.inventory.dto.ItemDTO;
import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.dto.siv.SIVDTO;
import com.sunright.inventory.dto.siv.SIVDetailDTO;

import java.util.List;

public interface SIVService {
    SIVDTO createSIV(SIVDTO input);

    SIVDTO findBy(Long id);

    SearchResult<SIVDTO> searchBy(SearchRequest searchRequest);

    DocmValueDTO getGeneratedNoSIV(SIVDTO input);

    List<SIVDTO> getProjectNoByStatus();

    List<SIVDetailDTO> populateSivDetail(String projectNo);

    SIVDTO getDefaultValueSIV(String subType, String sivType);

    List<SIVDetailDTO> populateBatchList(String subType, String projectNo, String itemNo, String sivType);

    List<SIVDetailDTO> populateSIVManualDetails(SIVDTO input);

    byte[] generatedLabelSIV(SIVDTO input);

    byte[] generatedReportSIV(SIVDTO input);

    SIVDetailDTO checkNextItem(SIVDTO input);

    List<ItemDTO> getAllItemNo();

    List<SIVDTO> populateSIVCombineDetails(SIVDTO input);
}
