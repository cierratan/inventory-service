package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.ItemDTO;
import com.sunright.inventory.entity.Item;
import com.sunright.inventory.entity.ItemId;
import com.sunright.inventory.repository.ItemRepository;
import com.sunright.inventory.service.ItemService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Override
    public ItemDTO createItem(ItemDTO input) {
        ItemId itemId = new ItemId();
        BeanUtils.copyProperties(input, itemId);

        Item item = new Item();
        item.setItemId(itemId);

        BeanUtils.copyProperties(input, item);

        itemRepository.save(item);

        return input;
    }
}
