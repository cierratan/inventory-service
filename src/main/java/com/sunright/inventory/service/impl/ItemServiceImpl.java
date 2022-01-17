package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.ItemDTO;
import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.search.Filter;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.entity.Item;
import com.sunright.inventory.entity.ItemLoc;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.exception.DuplicateException;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
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

        List<Item> found = itemRepository.findByCompanyCodeAndPlantNoAndItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getItemNo());

        if(!CollectionUtils.isEmpty(found)) {
            throw new DuplicateException(String.format("Duplicate item with itemNo: %s", input.getItemNo()));
        }

        Item item = new Item();
        BeanUtils.copyProperties(input, item);

        item.setCompanyCode(userProfile.getCompanyCode());
        item.setPlantNo(userProfile.getPlantNo());
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
        ItemLoc itemLoc = new ItemLoc();
        BeanUtils.copyProperties(saved, itemLoc);
        itemLoc.setItemId(saved.getId());
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
        Item found = checkIfRecordExist(input.getId());

        Item item = new Item();
        BeanUtils.copyProperties(input, item, "status");
        item.setCompanyCode(UserProfileContext.getUserProfile().getCompanyCode());
        item.setPlantNo(UserProfileContext.getUserProfile().getPlantNo());
        item.setStatus(found.getStatus());
        item.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
        item.setUpdatedAt(ZonedDateTime.now());

        prePopulateBeforeSaving(item);

        Item saved = itemRepository.save(item);
        updateAlternate(input, UserProfileContext.getUserProfile());

        // update item loc
        List<ItemLoc> itemLocs = itemLocRepository.findByCompanyCodeAndPlantNoAndItemNoAndLoc(saved.getCompanyCode(), saved.getPlantNo(), saved.getItemNo(), saved.getLoc());

        if(!CollectionUtils.isEmpty(itemLocs) && itemLocs.size() == 1) {
            ItemLoc itemLoc = itemLocRepository.getById(itemLocs.get(0).getId());
            itemLoc.setPartNo(item.getPartNo());
            itemLoc.setDescription(item.getDescription());
            itemLoc.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
            itemLoc.setUpdatedAt(ZonedDateTime.now());

            itemLocRepository.save(itemLoc);
        }

        populateAfterSaving(input, item, saved);

        return input;
    }

    @Override
    public ItemDTO findBy(Long id) {
        Item found = checkIfRecordExist(id);

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
    public void deleteItem(Long id) {
        Item item = checkIfRecordExist(id);
        item.setStatus(Status.DELETED);
        item.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
        item.setUpdatedAt(ZonedDateTime.now());

        itemRepository.save(item);
    }

    @Override
    public SearchResult<ItemDTO> searchBy(SearchRequest searchRequest) {
        Specification<Item> specs = where(queryGenerator.createDefaultSpec());

        if(!CollectionUtils.isEmpty(searchRequest.getFilters())) {
            for (Filter filter : searchRequest.getFilters()) {
                specs = specs.and(queryGenerator.createSpecification(filter));
            }
        }

        Page<Item> pgItems = itemRepository.findAll(specs, queryGenerator.constructPageable(searchRequest));

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
        input.setId(saved.getId());
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

    private Item checkIfRecordExist(Long id) {
        Optional<Item> optionalItem = itemRepository.findById(id);

        if (optionalItem.isEmpty()) {
            throw new NotFoundException("Record is not found");
        }
        return optionalItem.get();
    }
}
