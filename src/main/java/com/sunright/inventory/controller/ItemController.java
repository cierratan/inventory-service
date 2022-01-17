package com.sunright.inventory.controller;

import com.sunright.inventory.dto.ItemDTO;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("items")
public class ItemController {
    @Autowired
    private ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDTO> create(@RequestBody ItemDTO item) {
        return new ResponseEntity<>(itemService.createItem(item), HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<ItemDTO> get(@PathVariable Long id) {
        return new ResponseEntity<>(itemService.findBy(id), HttpStatus.OK);
    }

    @PutMapping("{id}")
    public ResponseEntity<ItemDTO> edit(@RequestBody ItemDTO item,
                                            @PathVariable Long id) {
        item.setId(id);
        return new ResponseEntity<>(itemService.editItem(item), HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        itemService.deleteItem(id);
    }

    @PostMapping("search")
    public ResponseEntity<SearchResult<ItemDTO>> search(@RequestBody SearchRequest searchRequest) {
        return new ResponseEntity<>(itemService.searchBy(searchRequest), HttpStatus.OK);
    }
}
