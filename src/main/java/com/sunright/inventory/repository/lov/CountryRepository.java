package com.sunright.inventory.repository.lov;

import com.sunright.inventory.entity.lov.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends JpaRepository<Country, String> {
}
