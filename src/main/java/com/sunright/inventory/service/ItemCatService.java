package com.sunright.inventory.service;

import com.sunright.inventory.dto.SearchRequest;
import com.sunright.inventory.dto.SearchResult;
import com.sunright.inventory.dto.lov.ItemCatDTO;

public interface ItemCatService {
    ItemCatDTO createItemCat(ItemCatDTO input);
    ItemCatDTO editItemCat(ItemCatDTO input);
    ItemCatDTO findBy(String categoryCode, String categorySubCode, String categoryGroup);
    void deleteItemCat(String categoryCode, String categorySubCode, String categoryGroup);
    SearchResult<ItemCatDTO> searchBy(SearchRequest searchRequest);
}
