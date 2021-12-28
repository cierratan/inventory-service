package com.sunright.inventory.service;

import com.sunright.inventory.dto.lov.*;

import java.util.List;

public interface LovService {
    List<CountryDTO> findAllCountries();
    List<DefaultCodeDetailDTO> findSources();
    List<DefaultCodeDetailDTO> findUOMs();
    List<ItemCatDTO> findItemCategories();
    List<CategorySubDTO> findSubCategories(String categoryCode);
    List<CodeDescDTO> findMSL();
}
