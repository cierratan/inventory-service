package com.sunright.inventory.repository.lov;

import com.sunright.inventory.entity.lov.Country;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends PagingAndSortingRepository<Country, String> {
}
