package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.*;
import com.sunright.inventory.dto.search.Filter;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.uom.UOM;
import com.sunright.inventory.entity.uom.UOMId;
import com.sunright.inventory.exception.NotFoundException;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.UOMRepository;
import com.sunright.inventory.service.UOMService;
import com.sunright.inventory.util.QueryGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;

@Service
public class UOMServiceImpl implements UOMService {

    @Autowired
    private UOMRepository uomRepository;

    @Autowired
    private QueryGenerator queryGenerator;

    @Override
    public UomDTO createUOM(UomDTO input) {
        UserProfile userProfile = UserProfileContext.getUserProfile();

        UOM uom = new UOM();
        uom.setId(populateUomId(input.getUomFrom(), input.getUomTo()));
        BeanUtils.copyProperties(input, uom);
        uom.setStatus(Status.ACTIVE);
        uom.setCreatedBy(userProfile.getUsername());
        uom.setCreatedAt(ZonedDateTime.now());
        uom.setUpdatedBy(userProfile.getUsername());
        uom.setUpdatedAt(ZonedDateTime.now());

        UOM saved = uomRepository.save(uom);
        input.setVersion(saved.getVersion());

        return input;
    }

    @Override
    public UomDTO editUOM(UomDTO input) {
        UOMId uomId = populateUomId(input.getUomFrom(), input.getUomTo());

        UOM found = checkIfRecordExist(uomId);

        UOM uom = new UOM();
        BeanUtils.copyProperties(input, uom, "status");
        uom.setId(uomId);
        uom.setStatus(found.getStatus());
        uom.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
        uom.setUpdatedAt(ZonedDateTime.now());

        UOM saved = uomRepository.save(uom);

        input.setVersion(saved.getVersion());
        return input;
    }

    @Override
    public UomDTO getUOM(String uomFrom, String uomTo) {
        UOMId uomId = populateUomId(uomFrom, uomTo);

        UOM uom = checkIfRecordExist(uomId);

        UomDTO uomDTO = new UomDTO();
        BeanUtils.copyProperties(uom, uomDTO);
        BeanUtils.copyProperties(uom.getId(), uomDTO);

        return uomDTO;
    }

    @Override
    public void deleteUOM(String uomFrom, String uomTo) {
        UOMId uomId = populateUomId(uomFrom, uomTo);

        UOM uom = checkIfRecordExist(uomId);

        uom.setStatus(Status.DELETED);
        uom.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
        uom.setUpdatedAt(ZonedDateTime.now());

        uomRepository.save(uom);
    }

    @Override
    public SearchResult<UomDTO> searchBy(SearchRequest searchRequest) {
        Specification activeStatus = ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), Status.ACTIVE));

        Specification<UOM> specs = where(activeStatus);

        if(!CollectionUtils.isEmpty(searchRequest.getFilters())) {
            for (Filter filter : searchRequest.getFilters()) {
                specs = specs.and(queryGenerator.createSpecification(filter));
            }
        }

        Page<UOM> pgUOM = uomRepository.findAll(specs, queryGenerator.constructPageable(searchRequest));

        SearchResult<UomDTO> uom = new SearchResult<>();
        uom.setTotalRows(pgUOM.getTotalElements());
        uom.setTotalPages(pgUOM.getTotalPages());
        uom.setCurrentPageNumber(pgUOM.getPageable().getPageNumber());
        uom.setCurrentPageSize(pgUOM.getNumberOfElements());
        uom.setRows(pgUOM.getContent().stream().map(location -> {
            UomDTO uomDTO = new UomDTO();
            BeanUtils.copyProperties(location.getId(), uomDTO);
            BeanUtils.copyProperties(location, uomDTO);
            return uomDTO;
        }).collect(Collectors.toList()));

        return uom;
    }

    private UOMId populateUomId(String uomFrom, String uomTo) {
        UOMId uomId = new UOMId();
        uomId.setUomFrom(uomFrom);
        uomId.setUomTo(uomTo);

        return uomId;
    }

    private UOM checkIfRecordExist(UOMId uomId) {
        Optional<UOM> optionalUom = uomRepository.findById(uomId);

        if (optionalUom.isEmpty()) {
            throw new NotFoundException("Record is not found");
        }
        return optionalUom.get();
    }
}
