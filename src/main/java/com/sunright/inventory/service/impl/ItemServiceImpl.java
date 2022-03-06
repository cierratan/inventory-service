package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.ItemDTO;
import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.search.Filter;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.entity.bom.BomProjection;
import com.sunright.inventory.entity.bombypj.BombypjProjection;
import com.sunright.inventory.entity.bomproj.BomprojProjection;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.inaudit.InAuditProjection;
import com.sunright.inventory.entity.item.Item;
import com.sunright.inventory.entity.item.ItemProjection;
import com.sunright.inventory.entity.itembatc.ItemBatchProjection;
import com.sunright.inventory.entity.itemeql.Itemeql;
import com.sunright.inventory.entity.itemloc.ItemLoc;
import com.sunright.inventory.exception.DuplicateException;
import com.sunright.inventory.exception.NotFoundException;
import com.sunright.inventory.exception.ServerException;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.*;
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
    private ItemBatcRepository itemBatcRepository;

    @Autowired
    private InAuditRepository inAuditRepository;

    @Autowired
    private ItemLocRepository itemLocRepository;

    @Autowired
    private BomRepository bomRepository;

    @Autowired
    private BombypjRepository bombypjRepository;

    @Autowired
    private ItemeqlRepository itemeqlRepository;

    @Autowired
    private BomprojRepository bomprojRepository;

    @Autowired
    private QueryGenerator queryGenerator;

    @Override
    @Transactional
    public ItemDTO createItem(ItemDTO input) {
        UserProfile userProfile = UserProfileContext.getUserProfile();

        List<Item> found = itemRepository.findByCompanyCodeAndPlantNoAndItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getItemNo());
        if (!CollectionUtils.isEmpty(found)) {
            // add logic by Arya for bug fixing soft deleted
            for (Item rec : found) {
                Optional<Item> optionalItem = itemRepository.findById(rec.getId());
                ItemLoc foundItemLoc = itemLocRepository.findItemLocByCompanyCodeAndPlantNoAndItemId(userProfile.getCompanyCode(), userProfile.getPlantNo(), rec.getId());
                if (optionalItem.isPresent() && foundItemLoc != null) {
                    if (optionalItem.get().getStatus() == Status.DELETED) {
                        Item item = new Item();
                        if (input.getSource().equals(rec.getSource())) {
                            input.setSource(null);
                        }
                        if (input.getPartNo().equals(rec.getPartNo())) {
                            input.setPartNo(null);
                        }
                        if (input.getObsoleteItem().equals(rec.getObsoleteItem())) {
                            input.setObsoleteItem(null);
                        }
                        String process = "CREATE";
                        checkRecValid(userProfile, input, process);
                        BeanUtils.copyProperties(input, item);
                        item.setCompanyCode(UserProfileContext.getUserProfile().getCompanyCode());
                        item.setPlantNo(UserProfileContext.getUserProfile().getPlantNo());
                        item.setStrRohsStatus(BooleanUtils.toString(input.getRohsStatus(), "1", "0"));
                        item.setStatus(Status.ACTIVE);
                        item.setCreatedBy(rec.getCreatedBy());
                        item.setCreatedAt(rec.getCreatedAt());
                        item.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
                        item.setUpdatedAt(ZonedDateTime.now());
                        item.setId(rec.getId());
                        item.setVersion(rec.getVersion());
                        prePopulateBeforeSaving(item);
                        Item saved = itemRepository.save(item);
                        updateAlternate(input, userProfile);

                        // insert itemloc
                        ItemLoc itemLoc = new ItemLoc();
                        BeanUtils.copyProperties(saved, itemLoc);
                        itemLoc.setItemId(foundItemLoc.getItemId());
                        itemLoc.setVersion(foundItemLoc.getVersion());
                        itemLoc.setStatus(Status.ACTIVE);
                        itemLoc.setCreatedBy(foundItemLoc.getCreatedBy());
                        itemLoc.setCreatedAt(foundItemLoc.getCreatedAt());
                        itemLoc.setUpdatedBy(userProfile.getUsername());
                        itemLoc.setUpdatedAt(ZonedDateTime.now());
                        itemLoc.setId(foundItemLoc.getId());
                        itemLocRepository.save(itemLoc);

                        input.setEoh(item.getQoh().subtract(item.getProdnResv()).add(item.getOrderQty()));
                        input.setQryObsItem(item.getObsoleteItem());
                        input.setRohsStatus(StringUtils.equals("1", item.getStrRohsStatus()) ? true : false);
                    } else {
                        throw new DuplicateException("Item Record exists !");
                    }
                }
            }
        } else {
            Item item = new Item();
            String process = "CREATE";
            checkRecValid(userProfile, input, process);
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
        }
        return input;
    }

    private void checkRecValid(UserProfile userProfile, ItemDTO input, String process) {
        if (StringUtils.isNotBlank(input.getSource())) {
            if (input.getSource().equals("A") || input.getSource().equals("W")) {
                BomprojProjection recBomproj = bomprojRepository.foundProjectNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getItemNo());
                if (recBomproj == null) {
                    throw new ServerException("Item No. Not Found in BOMPROJ table !");
                }
            }
        }
        if (process.equalsIgnoreCase("CREATE")) {
            if (StringUtils.isNotBlank(input.getPartNo())) {
                ItemProjection recPartNoExists = itemRepository.foundPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getPartNo());
                if (recPartNoExists != null) {
                    if (recPartNoExists.getPartNo() != null) {
                        //throw new ServerException("Part No exists in ITEM master.");
                        input.setMessage("Part No exists in ITEM master.");
                    }
                }
            }
        }
        if (StringUtils.isNotBlank(input.getObsoleteItem())) {
            ItemProjection recObsItem = itemRepository.foundObsoleteItem(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getObsoleteItem());
            if (recObsItem == null) {
                throw new ServerException("Invalid Stock Code!");
            }
        }
    }

    @Override
    @Transactional
    public ItemDTO editItem(ItemDTO input) {
        UserProfile userProfile = UserProfileContext.getUserProfile();
        Item found = checkIfRecordExist(input.getId());

        Item item = new Item();
        if (input.getSource().equals(found.getSource())) {
            input.setSource(null);
        }
        if (input.getPartNo().equals(found.getPartNo())) {
            input.setPartNo(null);
        }
        if (input.getObsoleteItem().equals(found.getObsoleteItem())) {
            input.setObsoleteItem(null);
        }
        String process = "UPDATE";
        checkRecValid(userProfile, input, process);
        BeanUtils.copyProperties(input, item, "status", "createdBy", "createdAt");
        item.setCompanyCode(UserProfileContext.getUserProfile().getCompanyCode());
        item.setPlantNo(UserProfileContext.getUserProfile().getPlantNo());
        item.setStatus(found.getStatus());
        item.setCreatedBy(found.getCreatedBy());
        item.setCreatedAt(found.getCreatedAt());

        item.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
        item.setUpdatedAt(ZonedDateTime.now());
        prePopulateBeforeSaving(item);
        Item saved = itemRepository.save(item);
        updateAlternate(input, UserProfileContext.getUserProfile());

        // update item loc
        List<ItemLoc> itemLocs = itemLocRepository.findByCompanyCodeAndPlantNoAndItemNoAndLoc(saved.getCompanyCode(), saved.getPlantNo(), saved.getItemNo(), saved.getLoc());

        if (!CollectionUtils.isEmpty(itemLocs) && itemLocs.size() == 1) {
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
    @Transactional
    public void deleteItem(Long id) {
        // add logic by Arya for delete item
        Item item = checkIfRecordExist(id);
        //before deletion, check for any QOH of that item
        ItemProjection itemCur = itemRepository.getQohByItemNo(item.getCompanyCode(), item.getPlantNo(), item.getItemNo(), item.getLoc());
        if (itemCur != null) {
            if (itemCur.getQoh().compareTo(BigDecimal.ZERO) > 0) {
                throw new ServerException("Qty On Hand for this item > 0, CANNOT delete !");
            } else if (itemCur.getOrderQty().compareTo(BigDecimal.ZERO) > 0) {
                throw new ServerException("Order Qty for this item > 0, CANNOT delete !");
            }
        }
        //before deletion, check for any movement of that item
        ItemBatchProjection itembatcCur = itemBatcRepository.itembatcCur(item.getCompanyCode(), item.getPlantNo(), item.getItemNo());
        if (itembatcCur != null) {
            if (itembatcCur.getCountItemBatc() > 0) {
                throw new ServerException("This is NOT a NON-MOVEMENT item (ITEMBATC), CANNOT delete !");
            }
        }
        InAuditProjection inauditCur = inAuditRepository.inauditCur(item.getCompanyCode(), item.getPlantNo(), item.getItemNo());
        if (inauditCur != null) {
            if (inauditCur.getCountInAudit() > 0) {
                throw new ServerException("This is NOT a NON-MOVEMENT item (INAUDIT), CANNOT delete !");
            }
        }
        List<BomProjection> bomCur = bomRepository.bomCur(item.getCompanyCode(), item.getPlantNo(), item.getItemNo());
        if (bomCur.size() > 0) {
            for (BomProjection rec : bomCur) {
                if (StringUtils.isNotBlank(rec.getComponent())) {
                    throw new ServerException("This item still in-use in BOM, CANNOT delete !");
                }
            }
        }
        List<BombypjProjection> bombypjCur = bombypjRepository.bombypjCur(item.getCompanyCode(), item.getPlantNo(), item.getItemNo());
        if (bombypjCur.size() != 0) {
            for (BombypjProjection rec : bombypjCur) {
                if (StringUtils.isNotBlank(rec.getProjectNo())) {
                    throw new ServerException(String.format("This item still in-use in Project %s , CANNOT delete !", rec.getProjectNo()));
                }
            }

        }
        // if not error then
        // delete itemLoc
        ItemLoc foundItemLoc = itemLocRepository.findItemLocByCompanyCodeAndPlantNoAndItemId(item.getCompanyCode(), item.getPlantNo(), item.getId());
        if (foundItemLoc != null) {
            ItemLoc itemLoc = new ItemLoc();
            BeanUtils.copyProperties(foundItemLoc, itemLoc);
            itemLoc.setCompanyCode(UserProfileContext.getUserProfile().getCompanyCode());
            itemLoc.setPlantNo(UserProfileContext.getUserProfile().getPlantNo());
            itemLoc.setStatus(Status.DELETED);
            itemLoc.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
            itemLoc.setUpdatedAt(ZonedDateTime.now());
            itemLoc.setItemId(foundItemLoc.getItemId());
            itemLoc.setVersion(foundItemLoc.getVersion());
            itemLoc.setCreatedBy(foundItemLoc.getCreatedBy());
            itemLoc.setCreatedAt(foundItemLoc.getCreatedAt());
            itemLoc.setId(foundItemLoc.getId());
            itemLocRepository.save(itemLoc);
        }
        // delete Itemeql
        Itemeql foundItemeqlItemNo = itemeqlRepository.findItemeqlByIdCompanyCodeAndIdPlantNoAndIdItemNo(item.getCompanyCode(), item.getPlantNo(), item.getItemNo());
        if (foundItemeqlItemNo != null) {
            itemeqlRepository.deleteItemeqlByIdCompanyCodeAndIdPlantNoAndIdItemNo(item.getCompanyCode(), item.getPlantNo(), item.getItemNo());
        }
        Itemeql foundItemeqlAlternate = itemeqlRepository.findItemeqlByIdCompanyCodeAndIdPlantNoAndIdAlternate(item.getCompanyCode(), item.getPlantNo(), item.getItemNo());
        if (foundItemeqlAlternate != null) {
            itemeqlRepository.deleteItemeqlByIdCompanyCodeAndIdPlantNoAndIdAlternate(item.getCompanyCode(), item.getPlantNo(), item.getItemNo());
        }
        // delete item
        item.setStatus(Status.DELETED);
        item.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
        item.setUpdatedAt(ZonedDateTime.now());
        itemRepository.save(item);
    }

    @Override
    public SearchResult<ItemDTO> searchBy(SearchRequest searchRequest) {
        Specification<Item> specs = where(queryGenerator.createDefaultSpec());

        if (!CollectionUtils.isEmpty(searchRequest.getFilters())) {
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
        if (StringUtils.isNotBlank(input.getObsoleteItem())) {
            itemRepository.updateAlternate(
                    input.getItemNo(),
                    userProfile.getCompanyCode(),
                    userProfile.getPlantNo(),
                    input.getObsoleteItem()
            );

            if (StringUtils.isNotBlank(input.getQryObsItem())
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

        if (!optionalItem.isPresent()) {
            throw new NotFoundException("Record is not found");
        }
        return optionalItem.get();
    }
}
