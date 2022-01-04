package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.lov.*;
import com.sunright.inventory.entity.lov.CategorySubProjection;
import com.sunright.inventory.entity.lov.CodeDesc;
import com.sunright.inventory.entity.lov.DefaultCodeDetail;
import com.sunright.inventory.entity.lov.ItemCatProjection;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.lov.CountryRepository;
import com.sunright.inventory.repository.lov.DefaultCodeDetailRepository;
import com.sunright.inventory.repository.lov.ItemCatRepository;
import com.sunright.inventory.service.LovService;
import com.sunright.inventory.util.QueryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;

@Service
public class LovServiceImpl implements LovService {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private DefaultCodeDetailRepository defaultCodeDetailRepository;

    @Autowired
    private ItemCatRepository itemCatRepository;

    @Autowired
    private QueryGenerator queryGenerator;

    @Override
    public List<CountryDTO> findAllCountries() {
        return Streamable.of(countryRepository.findAll()).stream().map(c -> CountryDTO.builder()
                .countryCode(c.getCountryCode())
                .description(c.getDescription())
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<DefaultCodeDetailDTO> findSources() {
        Specification<DefaultCodeDetail> defaultCode = ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id").get("defaultCode"), "ITEM.SOURCE"));

        Specification<DefaultCodeDetail> specs = where(queryGenerator.createDefaultSpecification().and(defaultCode));

        List<DefaultCodeDetail> defaultCodeDetail = defaultCodeDetailRepository.findAll(specs, Sort.by(Sort.Direction.ASC, "id.seqNo"));
        return defaultCodeDetail.stream().map(dtl -> DefaultCodeDetailDTO.builder()
                .defaultCode(dtl.getId().getDefaultCode())
                .codeValue(dtl.getCodeValue())
                .codeDesc(dtl.getCodeDesc())
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<DefaultCodeDetailDTO> findUOMs() {
        List<DefaultCodeDetail> defaultCodeDetails = defaultCodeDetailRepository.findDefaultCodeDetailBy(
                UserProfileContext.getUserProfile().getCompanyCode(), UserProfileContext.getUserProfile().getPlantNo(),
                "INVENTORY.UOM",
                Sort.by(Sort.Direction.ASC, "codeDesc")
        );

        return defaultCodeDetails.stream().map(dtl -> DefaultCodeDetailDTO.builder()
                .defaultCode(dtl.getId().getDefaultCode())
                .codeValue(dtl.getCodeValue())
                .codeDesc(dtl.getCodeDesc())
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<ItemCatDTO> findItemCategories() {
        List<ItemCatProjection> itemCategories = itemCatRepository.findItemCatBy(
                "ITEMCAT_SRC_GRP",
                "B",
                UserProfileContext.getUserProfile().getCompanyCode(),
                UserProfileContext.getUserProfile().getPlantNo(), Sort.by(Sort.Direction.ASC, "id.categoryCode"));

        return itemCategories.stream().map(item -> ItemCatDTO.builder()
                .categoryCode(item.getCategoryCode())
                .description(item.getDescription())
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<CategorySubDTO> findSubCategories(String categoryCode) {
        List<CategorySubProjection> subCategories = itemCatRepository.findSubCatBy(
                categoryCode,
                "ITEMCAT_SRC_GRP",
                "B",
                UserProfileContext.getUserProfile().getCompanyCode(),
                UserProfileContext.getUserProfile().getPlantNo(), Sort.by(Sort.Direction.ASC, "id.categoryCode"));

        return subCategories.stream().map(categorySub -> CategorySubDTO.builder()
                .categorySubCode(categorySub.getCategorySubCode())
                .subDescription(categorySub.getSubDescription())
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<CodeDescDTO> findMSL() {
        List<CodeDesc> msl = itemCatRepository.findCodeDescBy(
                "INMSL",
                Sort.by(Sort.Direction.ASC, "id.subType").and(Sort.by(Sort.Direction.ASC, "subtypeDesc")));

        return msl.stream().map(item -> CodeDescDTO.builder()
                .subType(item.getId().getSubType())
                .subtypeDesc(item.getSubtypeDesc())
                .build()).collect(Collectors.toList());
    }
}
