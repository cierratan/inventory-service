package com.sunright.inventory.service;

import com.sunright.inventory.dto.SearchRequest;
import com.sunright.inventory.dto.SearchResult;
import com.sunright.inventory.dto.UomDTO;

public interface UOMService {
    UomDTO createUOM(UomDTO input);
    UomDTO editUOM(UomDTO input);
    UomDTO getUOM(String uomFrom, String uomTo);
    void deleteUOM(String uomFrom, String uomTo);
    SearchResult<UomDTO> searchBy(SearchRequest searchRequest);
}
