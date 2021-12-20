package com.sunright.inventory.service;

import com.sunright.inventory.dto.GrnDTO;
import com.sunright.inventory.entity.Grn;

import java.util.List;

public interface GrnService {
    GrnDTO createGrn(GrnDTO input);

    List<Grn> list(int limit);

    Boolean delete(GrnDTO input);

    GrnDTO update(GrnDTO grnDTO);
}