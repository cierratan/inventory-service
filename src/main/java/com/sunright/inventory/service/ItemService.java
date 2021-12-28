package com.sunright.inventory.service;

import com.sunright.inventory.dto.ItemDTO;
import com.sunright.inventory.dto.SearchRequest;
import com.sunright.inventory.dto.SearchResult;

public interface ItemService {
    ItemDTO createItem(ItemDTO input);
    ItemDTO editItem(ItemDTO input);
    ItemDTO findBy(String itemNo);
    void deleteItem(String itemNo);
    SearchResult<ItemDTO> searchBy(SearchRequest searchRequest);
}
