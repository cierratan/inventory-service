package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.*;
import com.sunright.inventory.entity.*;
import com.sunright.inventory.exception.NotFoundException;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.ItemLocRepository;
import com.sunright.inventory.repository.ItemRepository;
import com.sunright.inventory.service.ItemService;
import com.sunright.inventory.util.QueryGenerator;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemLocRepository itemLocRepository;

    @Autowired
    private QueryGenerator queryGenerator;

    @Override
    @Transactional
    public ItemDTO createItem(ItemDTO input) {
        UserProfile userProfile = UserProfileContext.getUserProfile();

        ItemId itemId = new ItemId();
        BeanUtils.copyProperties(input, itemId);

        Item item = new Item();
        item.setId(itemId);

        BeanUtils.copyProperties(input, item);

        item.setId(populateItemId(input.getItemNo()));
        item.setStrRohsStatus(BooleanUtils.toString(input.getRohsStatus(), "1", "0"));

        item.setStatus(Status.ACTIVE);
        item.setCreatedBy(userProfile.getUsername());
        item.setCreatedAt(ZonedDateTime.now());
        item.setUpdatedBy(userProfile.getUsername());
        item.setUpdatedAt(ZonedDateTime.now());

        prePopulateBeforeSaving(item);

        Item saved = itemRepository.save(item);
        updateAlternate(input, userProfile);

        // insert itemloc
        ItemLocId itemLocId = new ItemLocId();
        BeanUtils.copyProperties(saved.getId(), itemLocId);

        ItemLoc itemLoc = new ItemLoc();
        itemLoc.setId(itemLocId);
        itemLoc.setCategoryCode(item.getCategoryCode());
        itemLoc.setCategorySubCode(item.getCategorySubCode());
        itemLoc.setQoh(item.getQoh());
        itemLoc.setLoc(item.getLoc());
        itemLoc.setPartNo(item.getPartNo());
        itemLoc.setDescription(item.getDescription());
        itemLoc.setStatus(Status.ACTIVE);
        itemLoc.setCreatedBy(userProfile.getUsername());
        itemLoc.setCreatedAt(ZonedDateTime.now());
        itemLoc.setUpdatedBy(userProfile.getUsername());
        itemLoc.setUpdatedAt(ZonedDateTime.now());

        itemLocRepository.save(itemLoc);

        populateAfterSaving(input, item, saved);

        return input;
    }

    @Override
    @Transactional
    public ItemDTO editItem(ItemDTO input) {
        ItemId itemId = populateItemId(input.getItemNo());

        Item found = checkIfRecordExist(itemId);

        Item item = new Item();
        BeanUtils.copyProperties(input, item, "status");
        item.setId(itemId);
        item.setStatus(found.getStatus());
        item.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
        item.setUpdatedAt(ZonedDateTime.now());

        prePopulateBeforeSaving(item);

        Item saved = itemRepository.save(item);
        updateAlternate(input, UserProfileContext.getUserProfile());

        // update item loc
        ItemLocId itemLocId = new ItemLocId();
        BeanUtils.copyProperties(saved.getId(), itemLocId);

        ItemLoc itemLoc = itemLocRepository.getById(itemLocId);
        itemLoc.setPartNo(item.getPartNo());
        itemLoc.setDescription(item.getDescription());
        itemLoc.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
        itemLoc.setUpdatedAt(ZonedDateTime.now());

        itemLocRepository.save(itemLoc);

        populateAfterSaving(input, item, saved);

        return input;
    }

    @Override
    public ItemDTO findBy(String itemNo) {
        ItemId itemId = populateItemId(itemNo);

        Item found = checkIfRecordExist(itemId);

        BigDecimal qoh = found.getQoh() != null ? found.getQoh() : new BigDecimal(0);
        BigDecimal prodResv = found.getProdnResv() != null ? found.getProdnResv() : new BigDecimal(0);
        BigDecimal orderQty = found.getOrderQty() != null ? found.getOrderQty() : new BigDecimal(0);

        ItemDTO itemDTO = ItemDTO.builder()
                .eoh(qoh.subtract(prodResv).add(orderQty))
                .qryObsItem(found.getObsoleteItem())
                .rohsStatus(StringUtils.equals("1", found.getStrRohsStatus()) ? true : false)
                .build();

        BeanUtils.copyProperties(found.getId(), itemDTO);
        BeanUtils.copyProperties(found, itemDTO);

        return itemDTO;
    }

    @Override
    public void deleteItem(String itemNo) {
        ItemId itemId = populateItemId(itemNo);

        Item item = checkIfRecordExist(itemId);
        item.setStatus(Status.DELETED);
        item.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
        item.setUpdatedAt(ZonedDateTime.now());

        itemRepository.save(item);
    }

    @Override
    public SearchResult<ItemDTO> searchBy(SearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getLimit());

        Specification<Item> specs = where(queryGenerator.createDefaultSpecification());

        if(!CollectionUtils.isEmpty(searchRequest.getFilters())) {
            for (Filter filter : searchRequest.getFilters()) {
                specs = specs.and(queryGenerator.createSpecification(filter));
            }
        }

        Page<Item> pgItems = itemRepository.findAll(specs, pageable);

        SearchResult<ItemDTO> items = new SearchResult<>();
        items.setTotalRows(pgItems.getTotalElements());
        items.setTotalPages(pgItems.getTotalPages());
        items.setCurrentPageNumber(pgItems.getPageable().getPageNumber());
        items.setCurrentPageSize(pgItems.getNumberOfElements());
        items.setRows(pgItems.getContent().stream().map(item -> {
            ItemDTO itemDTO = ItemDTO.builder()
                    .eoh(item.getQoh().subtract(item.getProdnResv()).add(item.getOrderQty()))
                    .qryObsItem(item.getObsoleteItem())
                    .rohsStatus(StringUtils.equals("1", item.getStrRohsStatus()) ? true : false)
                    .build();

            BeanUtils.copyProperties(item.getId(), itemDTO);
            BeanUtils.copyProperties(item, itemDTO);
            return itemDTO;
        }).collect(Collectors.toList()));

        return items;
    }

    private ItemId populateItemId(String itemNo) {
        UserProfile userProfile = UserProfileContext.getUserProfile();

        ItemId itemId = new ItemId();
        itemId.setCompanyCode(userProfile.getCompanyCode());
        itemId.setPlantNo(userProfile.getPlantNo());
        itemId.setItemNo(itemNo);

        return itemId;
    }

    private void updateAlternate(ItemDTO input, UserProfile userProfile) {
        if(StringUtils.isNotBlank(input.getObsoleteItem())) {
            itemRepository.updateAlternate(
                    input.getItemNo(),
                    userProfile.getCompanyCode(),
                    userProfile.getPlantNo(),
                    input.getObsoleteItem()
            );

            if(StringUtils.isNotBlank(input.getQryObsItem())
                    && !StringUtils.equals(input.getQryObsItem(), input.getObsoleteItem())) {
                itemRepository.updateAlternate(
                        userProfile.getCompanyCode(),
                        userProfile.getPlantNo(),
                        input.getQryObsItem()
                );
            }
        }
    }

    private void populateAfterSaving(ItemDTO input, Item item, Item saved) {
        input.setVersion(saved.getVersion());
        input.setEoh(item.getQoh().subtract(item.getProdnResv()).add(item.getOrderQty()));
        input.setQryObsItem(item.getObsoleteItem());
        input.setRohsStatus(StringUtils.equals("1", item.getStrRohsStatus()) ? true : false);
    }

    private void prePopulateBeforeSaving(Item item) {
        if (item.getOpenClose() != null) {
            item.setCloseDate(new Date(ZonedDateTime.now().toInstant().toEpochMilli()));
        }

        if (item.getObsoleteCode() != null) {
            item.setObsoleteDate(new Date(ZonedDateTime.now().toInstant().toEpochMilli()));
        }
    }

    private Item checkIfRecordExist(ItemId itemId) {
        Optional<Item> optionalItem = itemRepository.findById(itemId);

        if (optionalItem.isEmpty()) {
            throw new NotFoundException("Record is not found");
        }
        return optionalItem.get();
    }
}
