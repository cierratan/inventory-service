package com.sunright.inventory.controller;

import com.sunright.inventory.dto.lov.ItemCatDTO;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.service.ItemCatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("item-categories")
public class ItemCatController {

    @Autowired
    private ItemCatService itemCatService;

    @PostMapping
    public ResponseEntity<ItemCatDTO> create(@Valid @RequestBody ItemCatDTO itemCat) {
        return new ResponseEntity<>(itemCatService.createItemCat(itemCat), HttpStatus.OK);
    }

    @PutMapping("{id}")
    public ResponseEntity<ItemCatDTO> edit(@RequestBody ItemCatDTO itemCat,
                                            @PathVariable Long id) {
        itemCat.setId(id);

        return new ResponseEntity<>(itemCatService.editItemCat(itemCat), HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<ItemCatDTO> get(@PathVariable Long id) {
        return new ResponseEntity<>(itemCatService.findBy(id), HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        itemCatService.deleteItemCat(id);
    }

    @PostMapping("search")
    public ResponseEntity<SearchResult<ItemCatDTO>> search(@RequestBody SearchRequest searchRequest) {
        return new ResponseEntity<>(itemCatService.searchBy(searchRequest), HttpStatus.OK);
    }
}
