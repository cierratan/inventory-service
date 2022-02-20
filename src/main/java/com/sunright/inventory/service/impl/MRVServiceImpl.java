package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.entity.docmno.DocmNoProjection;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.DocmNoRepository;
import com.sunright.inventory.service.MRVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MRVServiceImpl implements MRVService {

    @Autowired
    private DocmNoRepository docmNoRepository;

    @Override
    public DocmValueDTO getGeneratedNo() {
        DocmNoProjection docmNo = docmNoRepository.getLastGeneratedNo(
                UserProfileContext.getUserProfile().getCompanyCode(),
                UserProfileContext.getUserProfile().getPlantNo(),
                "MRV",
                "N");

        return DocmValueDTO.builder()
                .generatedNo(docmNo.getGeneratedNo())
                .docmNo(docmNo.getDocmNo())
                .build();
    }
}
