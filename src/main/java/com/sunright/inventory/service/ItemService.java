package com.sunright.inventory.service;

import com.sunright.inventory.dto.ItemDTO;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;

public interface ItemService {
    ItemDTO createItem(ItemDTO input);
    ItemDTO editItem(ItemDTO input);
    ItemDTO findBy(Long id);
    void deleteItem(Long id);
    SearchResult<ItemDTO> searchBy(SearchRequest searchRequest);
}
