package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.msr.MsrDTO;
import com.sunright.inventory.dto.msr.MsrDetailDTO;
import com.sunright.inventory.dto.search.Filter;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.entity.docmno.DocmNoProjection;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.msr.MSR;
import com.sunright.inventory.entity.msr.MSRDetail;
import com.sunright.inventory.exception.NotFoundException;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.DocmNoRepository;
import com.sunright.inventory.repository.MSRRepository;
import com.sunright.inventory.repository.MsrDetailRepository;
import com.sunright.inventory.service.MSRService;
import com.sunright.inventory.util.QueryGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;

@Service
public class MSRServiceImpl implements MSRService {

    @Autowired
    private MSRRepository msrRepository;

    @Autowired
    private MsrDetailRepository msrDetailRepository;

    @Autowired
    private DocmNoRepository docmNoRepository;

    @Autowired
    private QueryGenerator queryGenerator;

    @Override
    public MsrDTO createMSR(MsrDTO input) {
        UserProfile userProfile = UserProfileContext.getUserProfile();

        MSR msr = new MSR();
        BeanUtils.copyProperties(input, msr);

        msr.setCompanyCode(userProfile.getCompanyCode());
        msr.setPlantNo(userProfile.getPlantNo());
        msr.setStatus(Status.ACTIVE);
        msr.setCreatedBy(userProfile.getUsername());
        msr.setCreatedAt(ZonedDateTime.now());
        msr.setUpdatedBy(userProfile.getUsername());
        msr.setUpdatedAt(ZonedDateTime.now());

        MSR saved = msrRepository.save(msr);

        if(!CollectionUtils.isEmpty(input.getMsrDetails())) {
            for (MsrDetailDTO detail : input.getMsrDetails()) {
                MSRDetail msrDetail = new MSRDetail();
                BeanUtils.copyProperties(detail, msrDetail);
                msrDetail.setCompanyCode(userProfile.getCompanyCode());
                msrDetail.setPlantNo(userProfile.getPlantNo());
                msrDetail.setMsr(saved);

                msrDetailRepository.save(msrDetail);
            }
        }

        input.setId(saved.getId());
        input.setVersion(saved.getVersion());
        return input;
    }

    @Override
    public MsrDTO findBy(Long id) {
        return convertToMsrDTO(checkIfRecordExist(id));
    }


    @Override
    public SearchResult<MsrDTO> searchBy(SearchRequest searchRequest) {
        Specification<MSR> specs = where(queryGenerator.createDefaultSpec());

        if(!CollectionUtils.isEmpty(searchRequest.getFilters())) {
            for (Filter filter : searchRequest.getFilters()) {
                specs = specs.and(queryGenerator.createSpecification(filter));
            }
        }

        Page<MSR> pgMSR = msrRepository.findAll(specs, queryGenerator.constructPageable(searchRequest));

        SearchResult<MsrDTO> result = new SearchResult<>();
        result.setTotalRows(pgMSR.getTotalElements());
        result.setTotalPages(pgMSR.getTotalPages());
        result.setCurrentPageNumber(pgMSR.getPageable().getPageNumber());
        result.setCurrentPageSize(pgMSR.getNumberOfElements());
        result.setRows(pgMSR.getContent().stream().map(msr -> convertToMsrDTO(msr)).collect(Collectors.toList()));

        return result;
    }

    @Override
    public DocmValueDTO getGeneratedNo() {
        DocmNoProjection docmNo = docmNoRepository.getLastGeneratedNo(
                UserProfileContext.getUserProfile().getCompanyCode(),
                UserProfileContext.getUserProfile().getPlantNo(),
                "MSR",
                "N");

        return DocmValueDTO.builder()
                .generatedNo(docmNo.getGeneratedNo())
                .docmNo(docmNo.getDocmNo())
                .build();
    }

    private MSR checkIfRecordExist(Long id) {
        Optional<MSR> optionalItem = msrRepository.findById(id);

        if (optionalItem.isEmpty()) {
            throw new NotFoundException("Record is not found");
        }
        return optionalItem.get();
    }

    private MsrDTO convertToMsrDTO(MSR msr) {
        Set<MsrDetailDTO> msrDetails = new HashSet<>();
        if(!CollectionUtils.isEmpty(msr.getMsrDetails())) {
            msrDetails = msr.getMsrDetails().stream().map(detail -> {
                MsrDetailDTO msrDetail = MsrDetailDTO.builder()
                        .msrNo(detail.getMsrNo())
                        .seqNo(detail.getSeqNo())
                        .build();

                BeanUtils.copyProperties(detail, msrDetail);

                return msrDetail;
            }).collect(Collectors.toSet());
        }

        MsrDTO msrDTO = MsrDTO.builder().build();

        BeanUtils.copyProperties(msr.getId(), msrDTO);
        BeanUtils.copyProperties(msr, msrDTO);
        msrDTO.setMsrDetails(msrDetails);
        return msrDTO;
    }
}
