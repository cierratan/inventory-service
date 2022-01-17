package com.sunright.inventory.service;

import com.sunright.inventory.dto.lov.ItemCatDTO;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;

public interface ItemCatService {
    ItemCatDTO createItemCat(ItemCatDTO input);
    ItemCatDTO editItemCat(ItemCatDTO input);
    ItemCatDTO findBy(Long id);
    void deleteItemCat(Long id);
    SearchResult<ItemCatDTO> searchBy(SearchRequest searchRequest);
}
