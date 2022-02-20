package com.sunright.inventory.service;

import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.dto.siv.SIVDTO;
import com.sunright.inventory.dto.siv.SIVDetailDTO;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;

public interface SIVService {
    SIVDTO createSIV(SIVDTO input);

    SIVDTO findBy(Long id);

    SearchResult<SIVDTO> searchBy(SearchRequest searchRequest);

    DocmValueDTO getGeneratedNoSIV(SIVDTO input);

    List<SIVDTO> getProjectNoByStatus();

    List<SIVDetailDTO> populateSivDetail(String projectNo);

    SIVDTO getDefaultValueSIV(String subType) throws ParseException;

    List<SIVDetailDTO> populateBatchList(String projectNo);

    SIVDetailDTO checkValidIssuedQty(String projectNo, String itemNo, BigDecimal issuedQty, int seqNo);

    List<SIVDetailDTO> getSIVManualDetails(SIVDTO input);
}
