package com.sunright.inventory.service;

import com.sunright.inventory.dto.*;
import com.sunright.inventory.entity.grn.Grn;

import java.util.List;
import java.util.Map;

public interface GrnService {

    Map<String, Object> create(GrnDTO input);

    List<Grn> get();

    SearchResult<GrnDTO> searchBy(SearchRequest searchRequest);

    Map<String, Object> getAllPoNo(UserProfile userProfile);

    Map<String, Object> getGrnHeader(GrnDTO grnDTO);

    Map<String, Object> getAllPartNo(GrnDTO grnDTO, UserProfile userProfile);

    Map<String, Object> getGrnDetail(GrnDTO grnDTO, GrnDetDTO grnDetDTO);
}