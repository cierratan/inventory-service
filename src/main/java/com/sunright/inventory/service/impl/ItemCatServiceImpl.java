package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.lov.ItemCatDTO;
import com.sunright.inventory.dto.search.Filter;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.lov.ItemCat;
import com.sunright.inventory.exception.DuplicateException;
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
import java.util.List;
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

        if(!CollectionUtils.isEmpty(found)) {
            throw new DuplicateException(String.format(
                    "Duplicate record categoryCode: %s, categorySubCode: %s, categoryGroup: %s",
                    input.getCategoryCode(), input.getCategorySubCode(), input.getCategoryGroup()
            ));
        }

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

    private ItemCat checkIfRecordExist(Long id) {
        Optional<ItemCat> optionalItemCat = itemCatRepository.findById(id);

        if (optionalItemCat.isEmpty()) {
            throw new NotFoundException("Record is not found");
        }
        return optionalItemCat.get();
    }
}
