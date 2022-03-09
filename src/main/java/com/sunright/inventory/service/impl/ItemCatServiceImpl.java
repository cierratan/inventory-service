package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.lov.ItemCatDTO;
import com.sunright.inventory.dto.search.Filter;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.entity.enums.MRPStatus;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.lov.ItemCat;
import com.sunright.inventory.entity.lov.ValueDescProjection;
import com.sunright.inventory.exception.DuplicateException;
import com.sunright.inventory.exception.NotFoundException;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.lov.ItemCatRepository;
import com.sunright.inventory.service.ItemCatService;
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
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private ItemCatRepository itemCatRepository;

    @Autowired
    private QueryGenerator queryGenerator;

    @Override
    public ItemCatDTO createItemCat(ItemCatDTO input) {
        UserProfile userProfile = UserProfileContext.getUserProfile();

        List<ItemCat> found = itemCatRepository.findByCompanyCodeAndPlantNoAndCategoryCodeAndCategorySubCodeAndCategoryGroup(
                userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getCategoryCode(),
                input.getCategorySubCode(), input.getCategoryGroup());

        if (!CollectionUtils.isEmpty(found)) {
            for (ItemCat rec : found) {
                Optional<ItemCat> optionalItemCat = itemCatRepository.findById(rec.getId());
                if (optionalItemCat.isPresent()) {
                    if (optionalItemCat.get().getStatus() == Status.DELETED) {
                        ItemCat itemCat = new ItemCat();
                        BeanUtils.copyProperties(input, itemCat);
                        itemCat.setCompanyCode(UserProfileContext.getUserProfile().getCompanyCode());
                        itemCat.setPlantNo(UserProfileContext.getUserProfile().getPlantNo());
                        itemCat.setMrpStatus(MRPStatus.Y);
                        itemCat.setStatus(Status.ACTIVE);
                        itemCat.setCreatedBy(rec.getCreatedBy());
                        itemCat.setCreatedAt(rec.getCreatedAt());
                        itemCat.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
                        itemCat.setUpdatedAt(ZonedDateTime.now());
                        itemCat.setId(rec.getId());
                        itemCat.setVersion(rec.getVersion());
                        itemCatRepository.save(itemCat);
                    } else {
                        throw new DuplicateException(String.format(
                                "Duplicate record categoryCode: %s, categorySubCode: %s, categoryGroup: %s",
                                input.getCategoryCode(), input.getCategorySubCode(), input.getCategoryGroup()
                        ));
                    }
                }
            }
        } else {
            ItemCat itemCat = new ItemCat();
            BeanUtils.copyProperties(input, itemCat);
            itemCat.setCompanyCode(userProfile.getCompanyCode());
            itemCat.setPlantNo(userProfile.getPlantNo());
            itemCat.setStatus(Status.ACTIVE);
            itemCat.setCreatedBy(userProfile.getUsername());
            itemCat.setCreatedAt(ZonedDateTime.now());
            itemCat.setUpdatedBy(userProfile.getUsername());
            itemCat.setUpdatedAt(ZonedDateTime.now());

            ItemCat saved = itemCatRepository.save(itemCat);
            input.setId(saved.getId());
            input.setVersion(saved.getVersion());
        }

        return input;
    }

    @Override
    public ItemCatDTO editItemCat(ItemCatDTO input) {
        ItemCat found = checkIfRecordExist(input.getId());

        ItemCat itemCat = new ItemCat();
        BeanUtils.copyProperties(input, itemCat, "status");
        itemCat.setCompanyCode(UserProfileContext.getUserProfile().getCompanyCode());
        itemCat.setPlantNo(UserProfileContext.getUserProfile().getPlantNo());
        itemCat.setStatus(found.getStatus());
        itemCat.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
        itemCat.setUpdatedAt(ZonedDateTime.now());

        ItemCat saved = itemCatRepository.save(itemCat);

        input.setVersion(saved.getVersion());
        return input;
    }

    @Override
    public ItemCatDTO findBy(Long id) {
        ItemCat itemCat = checkIfRecordExist(id);

        ItemCatDTO itemCatDTO = ItemCatDTO.builder().build();
        BeanUtils.copyProperties(itemCat, itemCatDTO);
        BeanUtils.copyProperties(itemCat.getId(), itemCatDTO);

        return itemCatDTO;
    }

    @Override
    public void deleteItemCat(Long id) {
        ItemCat itemCat = checkIfRecordExist(id);

        itemCat.setStatus(Status.DELETED);
        itemCat.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
        itemCat.setUpdatedAt(ZonedDateTime.now());

        itemCatRepository.save(itemCat);
    }

    @Override
    public SearchResult<ItemCatDTO> searchBy(SearchRequest searchRequest) {
        Specification<ItemCat> specs = where(queryGenerator.createDefaultSpec());

        if (!CollectionUtils.isEmpty(searchRequest.getFilters())) {
            for (Filter filter : searchRequest.getFilters()) {
                specs = specs.and(createSpecificationItemCat(filter));
            }
        }

        Page<ItemCat> pgItemCat = itemCatRepository.findAll(specs, queryGenerator.constructPageable(searchRequest));

        SearchResult<ItemCatDTO> itemCategories = new SearchResult<>();
        itemCategories.setTotalRows(pgItemCat.getTotalElements());
        itemCategories.setTotalPages(pgItemCat.getTotalPages());
        itemCategories.setCurrentPageNumber(pgItemCat.getPageable().getPageNumber());
        itemCategories.setCurrentPageSize(pgItemCat.getNumberOfElements());
        itemCategories.setRows(pgItemCat.getContent().stream().map(itemCat -> {
            ItemCatDTO itemCatDTO = ItemCatDTO.builder().build();
            BeanUtils.copyProperties(itemCat.getId(), itemCatDTO);
            BeanUtils.copyProperties(itemCat, itemCatDTO);
            defineMrpStatusAndCategoryGroup(itemCat, itemCatDTO);
            return itemCatDTO;
        }).collect(Collectors.toList()));

        return itemCategories;
    }

    private void defineMrpStatusAndCategoryGroup(ItemCat itemCat, ItemCatDTO itemCatDTO) {
        if (itemCat.getMrpStatus() == MRPStatus.Y) {
            itemCatDTO.setMrpStatusDesc("YES");
        } else if (itemCat.getMrpStatus() == MRPStatus.N) {
            itemCatDTO.setMrpStatusDesc("NO");
        }

        List<ValueDescProjection> listCatGrp = itemCatRepository.findCategoryGroups();
        for (ValueDescProjection rec : listCatGrp) {
            if (itemCat.getCategoryGroup().equals(rec.getCodeValue())) {
                itemCatDTO.setCategoryGroup(rec.getCodeDesc());
            }
        }
    }

    private ItemCat checkIfRecordExist(Long id) {
        Optional<ItemCat> optionalItemCat = itemCatRepository.findById(id);

        if (!optionalItemCat.isPresent()) {
            throw new NotFoundException("Record is not found");
        }
        return optionalItemCat.get();
    }

    private Specification createSpecificationItemCat(Filter input) {
        if (input.getField().equals("mrpStatus")) {
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