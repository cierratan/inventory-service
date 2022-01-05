package com.sunright.inventory.service;

import com.sunright.inventory.dto.DocmNoDTO;
import com.sunright.inventory.dto.GrnDTO;
import com.sunright.inventory.dto.GrnDetDTO;
import com.sunright.inventory.entity.grn.Grn;
import com.sunright.inventory.exception.ErrorMessage;

import java.util.List;
import java.util.Map;

public interface GrnService {

    Map<String, Object> create(GrnDTO input);

    List<Grn> get();

    Map<String, Object> getLastGeneratedNoforGRN(DocmNoDTO docmNoDTO);

    ErrorMessage checkStatusPoNo(GrnDTO grnDTO);

    Map<String, Object> getPurInfo(GrnDTO grnDTO);

    Map<String, Object> getPurDetInfo(GrnDTO grnDTO);

    Map<String, Object> checkItemNoAndPartNo(GrnDTO grnDTO, GrnDetDTO grnDetDTO);
}