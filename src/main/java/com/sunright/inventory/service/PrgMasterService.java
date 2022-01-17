package com.sunright.inventory.service;

import com.sunright.inventory.dto.prgmaster.PrgMasterDTO;

public interface PrgMasterService {
    PrgMasterDTO getProgramTitle(String prgId, String moduleCd);
}
