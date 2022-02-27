package com.sunright.inventory.service;

import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.mrv.MrvDTO;
import com.sunright.inventory.dto.mrv.MrvDetailDTO;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;

public interface MRVService {
    DocmValueDTO getGeneratedNo();
    MrvDetailDTO findSivAndPopulateMRVDetails(String sivNo);
    MrvDTO createMrv(MrvDTO input);
    SearchResult<MrvDTO> searchBy(SearchRequest searchRequest);
    MrvDTO findBy(Long id);
}
