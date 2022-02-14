package com.sunright.inventory.service;

import com.sunright.inventory.dto.ItemDTO;
import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.lov.LocationDTO;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.dto.siv.SIVDTO;
import com.sunright.inventory.dto.siv.SIVDetailDTO;

import java.text.ParseException;
import java.util.List;

public interface SIVService {
    SIVDTO createSIV(SIVDTO input);

    SIVDTO findBy(Long id);

    SearchResult<SIVDTO> searchBy(SearchRequest searchRequest);

    DocmValueDTO getGeneratedNoSIV(SIVDTO input);

    List<SIVDTO> getProjectNoByStatus();

    List<LocationDTO> getLocAndDesc();

    List<ItemDTO> getItemNo();

    List<SIVDetailDTO> checkNextItem(SIVDTO input);

    SIVDetailDTO checkValidIssuedQty(SIVDetailDTO input);

    SIVDTO getDefaultValueSIVEntry() throws ParseException;
}
