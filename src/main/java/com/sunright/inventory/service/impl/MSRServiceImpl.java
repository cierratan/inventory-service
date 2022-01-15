package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.msr.MsrDTO;
import com.sunright.inventory.dto.msr.MsrDetailDTO;
import com.sunright.inventory.dto.search.Filter;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.msr.MSR;
import com.sunright.inventory.entity.msr.MSRDetail;
import com.sunright.inventory.entity.msr.MSRDetailId;
import com.sunright.inventory.entity.msr.MSRId;
import com.sunright.inventory.exception.NotFoundException;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.MSRRepository;
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
    private QueryGenerator queryGenerator;

    @Override
    public MsrDTO createMSR(MsrDTO input) {
        UserProfile userProfile = UserProfileContext.getUserProfile();

        Set<MSRDetail> msrDetails = new HashSet<>();

        if(!CollectionUtils.isEmpty(input.getMsrDetails())) {
            msrDetails = input.getMsrDetails().stream().map(detail -> {
                MSRDetailId id = new MSRDetailId();
                id.setCompanyCode(userProfile.getCompanyCode());
                id.setPlantNo(userProfile.getPlantNo());
                id.setMsrNo(detail.getMsrNo());
                id.setSeqNo(detail.getSeqNo());

                MSRDetail msrDetail = new MSRDetail();
                msrDetail.setId(id);
                BeanUtils.copyProperties(detail, msrDetail);

                return msrDetail;
            }).collect(Collectors.toSet());
        }

        MSR msr = new MSR();
        BeanUtils.copyProperties(input, msr);

        msr.setId(populateId(input.getMsrNo()));
        msr.setMsrDetails(msrDetails);
        msr.setStatus(Status.ACTIVE);
        msr.setCreatedBy(userProfile.getUsername());
        msr.setCreatedAt(ZonedDateTime.now());
        msr.setUpdatedBy(userProfile.getUsername());
        msr.setUpdatedAt(ZonedDateTime.now());

        MSR saved = msrRepository.save(msr);
        input.setVersion(saved.getVersion());

        return input;
    }

    @Override
    public MsrDTO findBy(String msrNo) {
        return convertToMsrDTO(checkIfRecordExist(populateId(msrNo)));
    }


    @Override
    public SearchResult<MsrDTO> searchBy(SearchRequest searchRequest) {
        Specification<MSR> specs = where(queryGenerator.createDefaultSpecification());

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

    private MSRId populateId(String msrNo) {
        UserProfile userProfile = UserProfileContext.getUserProfile();

        MSRId msrId = new MSRId();
        msrId.setCompanyCode(userProfile.getCompanyCode());
        msrId.setPlantNo(userProfile.getPlantNo());
        msrId.setMsrNo(msrNo);

        return msrId;
    }

    private MSR checkIfRecordExist(MSRId id) {
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
                        .msrNo(detail.getId().getMsrNo())
                        .seqNo(detail.getId().getSeqNo())
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
