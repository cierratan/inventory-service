package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.prgmaster.PrgMasterDTO;
import com.sunright.inventory.entity.prgmaster.PrgMaster;
import com.sunright.inventory.exception.ErrorMessage;
import com.sunright.inventory.repository.PrgMasterRepository;
import com.sunright.inventory.service.PrgMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PrgMasterServiceImpl implements PrgMasterService {

    @Autowired
    private PrgMasterRepository prgMasterRepository;

    @Override
    public PrgMasterDTO getProgramTitle(String prgId, String moduleCd) {

        Optional<PrgMaster> prgMasterOptional = prgMasterRepository.findPrgMasterByPrgIdAndModuleCd(prgId, moduleCd);
        PrgMasterDTO dto = PrgMasterDTO.builder().build();
        if (prgMasterOptional.isPresent()) {
            dto.setPrgDesc(prgMasterOptional.get().getPrgDesc());
        } else {
            ErrorMessage.builder().message("No record found in PRG_MASTER table !").build();
        }
        return dto;
    }
}
