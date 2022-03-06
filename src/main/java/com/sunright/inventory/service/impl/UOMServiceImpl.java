package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.UomDTO;
import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.search.Filter;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.uom.UOM;
import com.sunright.inventory.entity.uom.UOMId;
import com.sunright.inventory.exception.DuplicateException;
import com.sunright.inventory.exception.NotFoundException;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.UOMRepository;
import com.sunright.inventory.service.UOMService;
import com.sunright.inventory.util.QueryGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
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

        List<UOM> found = uomRepository.findUOMByIdUomFromAndIdUomTo(input.getUomFrom(), input.getUomTo());
        if (!CollectionUtils.isEmpty(found)) {
            for (UOM rec : found) {
                Optional<UOM> optionalUOM = uomRepository.findById(rec.getId());
                if (optionalUOM.get().getStatus() == Status.DELETED) {
                    UOM uom = new UOM();
                    BeanUtils.copyProperties(input, uom);
                    uom.setStatus(Status.ACTIVE);
                    uom.setCreatedBy(rec.getCreatedBy());
                    uom.setCreatedAt(rec.getCreatedAt());
                    uom.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
                    uom.setUpdatedAt(ZonedDateTime.now());
                    uom.setId(rec.getId());
                    uom.setVersion(rec.getVersion());
                    uomRepository.save(uom);
                } else {
                    throw new DuplicateException("UOM Record Exists!");
                }
            }
        } else {
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
        }

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
        if (!CollectionUtils.isEmpty(searchRequest.getFilters())) {
            for (Filter filter : searchRequest.getFilters()) {
                specs = specs.and(createSpecificationUom(filter));
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

        if (!optionalUom.isPresent()) {
            throw new NotFoundException("Record is not found");
        }
        return optionalUom.get();
    }

    private Specification createSpecificationUom(Filter input) {
        if (input.getField().equals("uomFrom") || input.getField().equals("uomTo")) {
            switch (input.getOperator()) {
                case EQUALS:
                    return (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(root.get(input.getField()),
                                    castToRequiredType(root.get("id").get(input.getField()).getJavaType(), input.getValue()));
                case LIKE:
                    final String strValue;
                    if (StringUtils.contains(input.getValue(), "_")) {
                        strValue = StringUtils.replace(input.getValue(), "_", "\\_");
                    } else if (StringUtils.contains(input.getValue(), "%")) {
                        strValue = StringUtils.replace(input.getValue(), "%", "\\%");
                    } else {
                        strValue = input.getValue();
                    }

                    return (root, query, criteriaBuilder) -> // add by Arya (case-insensitive like matching anywhere)
                            criteriaBuilder.like(criteriaBuilder.lower(root.get(input.getField())), "%" + strValue.toLowerCase(Locale.ROOT) + "%", '\\');
            }

        } else if (input.getField().equals("uomFactor")) {
            switch (input.getOperator()) {
                case EQUALS:
                    return (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(root.get(input.getField()),
                                    castToRequiredType(root.get(input.getField()).getJavaType(), input.getValue()));
                case LIKE:
                    final String strValue;
                    if (StringUtils.contains(input.getValue(), "_")) {
                        strValue = StringUtils.replace(input.getValue(), "_", "\\_");
                    } else if (StringUtils.contains(input.getValue(), "%")) {
                        strValue = StringUtils.replace(input.getValue(), "%", "\\%");
                    } else {
                        strValue = input.getValue();
                    }

                    return (root, query, criteriaBuilder) -> // add by Arya (case-insensitive like matching anywhere)
                            criteriaBuilder.like(criteriaBuilder.lower(root.get(input.getField()).as(String.class)), "%" + strValue.toLowerCase(Locale.ROOT) + "%", '\\');
            }
        }
        switch (input.getOperator()) {
            case EQUALS:
                return (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get(input.getField()),
                                castToRequiredType(root.get(input.getField()).getJavaType(), input.getValue()));
            case LIKE:
                final String strValue;
                if (StringUtils.contains(input.getValue(), "_")) {
                    strValue = StringUtils.replace(input.getValue(), "_", "\\_");
                } else if (StringUtils.contains(input.getValue(), "%")) {
                    strValue = StringUtils.replace(input.getValue(), "%", "\\%");
                } else {
                    strValue = input.getValue();
                }

                return (root, query, criteriaBuilder) -> // add by Arya (case-insensitive like matching anywhere)
                        criteriaBuilder.like(criteriaBuilder.lower(root.get(input.getField())), "%" + strValue.toLowerCase(Locale.ROOT) + "%", '\\');
            default:
                throw new RuntimeException("Operation not supported yet");
        }
    }

    private Object castToRequiredType(Class fieldType, String value) {
        if (fieldType.isAssignableFrom(Double.class)) {
            return Double.valueOf(value);
        } else if (fieldType.isAssignableFrom(Integer.class)) {
            return Integer.valueOf(value);
        } else if (Enum.class.isAssignableFrom(fieldType)) {
            return Enum.valueOf(fieldType, value);
        } else if (fieldType.isAssignableFrom(String.class)) {
            return value;
        } else if (fieldType.isAssignableFrom(BigDecimal.class)) {
            return new BigDecimal(value);
        }

        return null;
    }
}
