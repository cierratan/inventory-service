package com.sunright.inventory.repository;

import com.sunright.inventory.entity.Item;
import com.sunright.inventory.entity.ItemId;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends PagingAndSortingRepository<Item, ItemId> {

}
