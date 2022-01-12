package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.search.Filter;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.lov.ItemCatDTO;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.lov.ItemCat;
import com.sunright.inventory.entity.lov.ItemCatId;
import com.sunright.inventory.exception.NotFoundException;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.lov.ItemCatRepository;
import com.sunright.inventory.service.ItemCatService;
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
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private ItemCatRepository itemCatRepository;

    @Autowired
    private QueryGenerator queryGenerator;

    @Override
    public ItemCatDTO createItemCat(ItemCatDTO input) {
        UserProfile userProfile = UserProfileContext.getUserProfile();

        ItemCat itemCat = new ItemCat();
        BeanUtils.copyProperties(input, itemCat);
        itemCat.setId(populateItemCatId(input.getCategoryCode(), input.getCategorySubCode(), input.getCategoryGroup()));
        itemCat.setStatus(Status.ACTIVE);
        itemCat.setCreatedBy(userProfile.getUsername());
        itemCat.setCreatedAt(ZonedDateTime.now());
        itemCat.setUpdatedBy(userProfile.getUsername());
        itemCat.setUpdatedAt(ZonedDateTime.now());

        ItemCat saved = itemCatRepository.save(itemCat);

        input.setVersion(saved.getVersion());
        return input;
    }

    @Override
    public ItemCatDTO editItemCat(ItemCatDTO input) {
        ItemCatId itemCatId = populateItemCatId(input.getCategoryCode(), input.getCategorySubCode(), input.getCategoryGroup());

        ItemCat found = checkIfRecordExist(itemCatId);

        ItemCat itemCat = new ItemCat();
        BeanUtils.copyProperties(input, itemCat, "status");
        itemCat.setId(itemCatId);
        itemCat.setStatus(found.getStatus());
        itemCat.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
        itemCat.setUpdatedAt(ZonedDateTime.now());

        ItemCat saved = itemCatRepository.save(itemCat);

        input.setVersion(saved.getVersion());
        return input;
    }

    @Override
    public ItemCatDTO findBy(String categoryCode, String categorySubCode, String categoryGroup) {
        ItemCatId itemCatId = populateItemCatId(categoryCode, categorySubCode, categoryGroup);

        ItemCat itemCat = checkIfRecordExist(itemCatId);

        ItemCatDTO itemCatDTO = ItemCatDTO.builder().build();
        BeanUtils.copyProperties(itemCat, itemCatDTO);
        BeanUtils.copyProperties(itemCat.getId(), itemCatDTO);

        return itemCatDTO;
    }

    @Override
    public void deleteItemCat(String categoryCode, String categorySubCode, String categoryGroup) {
        ItemCatId itemCatId = populateItemCatId(categoryCode, categorySubCode, categoryGroup);

        ItemCat itemCat = checkIfRecordExist(itemCatId);

        itemCat.setStatus(Status.DELETED);
        itemCat.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
        itemCat.setUpdatedAt(ZonedDateTime.now());

        itemCatRepository.save(itemCat);
    }

    @Override
    public SearchResult<ItemCatDTO> searchBy(SearchRequest searchRequest) {
        Specification<ItemCat> specs = where(queryGenerator.createDefaultSpecification());

        if(!CollectionUtils.isEmpty(searchRequest.getFilters())) {
            for (Filter filter : searchRequest.getFilters()) {
                specs = specs.and(queryGenerator.createSpecification(filter));
            }
        }

        Page<ItemCat> pgItemCat = itemCatRepository.findAll(specs, queryGenerator.constructPageable(searchRequest));

        SearchResult<ItemCatDTO> itemCategories = new SearchResult<>();
        itemCategories.setTotalRows(pgItemCat.getTotalElements());
        itemCategories.setTotalPages(pgItemCat.getTotalPages());
        itemCategories.setCurrentPageNumber(pgItemCat.getPageable().getPageNumber());
        itemCategories.setCurrentPageSize(pgItemCat.getNumberOfElements());
        itemCategories.setRows(pgItemCat.getContent().stream().map(location -> {
            ItemCatDTO itemCatDTO = ItemCatDTO.builder().build();
            BeanUtils.copyProperties(location.getId(), itemCatDTO);
            BeanUtils.copyProperties(location, itemCatDTO);
            return itemCatDTO;
        }).collect(Collectors.toList()));

        return itemCategories;
    }

    private ItemCatId populateItemCatId(String categoryCode, String categorySubCode, String categoryGroup) {
        UserProfile userProfile = UserProfileContext.getUserProfile();

        ItemCatId itemCatId = new ItemCatId();
        itemCatId.setCompanyCode(userProfile.getCompanyCode());
        itemCatId.setPlantNo(userProfile.getPlantNo());
        itemCatId.setCategoryCode(categoryCode);
        itemCatId.setCategorySubCode(categorySubCode);
        itemCatId.setCategoryGroup(categoryGroup);

        return itemCatId;
    }

    private ItemCat checkIfRecordExist(ItemCatId itemCatId) {
        Optional<ItemCat> optionalItemCat = itemCatRepository.findById(itemCatId);

        if (optionalItemCat.isEmpty()) {
            throw new NotFoundException("Record is not found");
        }
        return optionalItemCat.get();
    }
}
