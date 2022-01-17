package com.sunright.inventory.repository;

import com.sunright.inventory.entity.docmno.DocmNo;
import com.sunright.inventory.entity.docmno.DocmNoId;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DocmNoRepository extends PagingAndSortingRepository<DocmNo, DocmNoId>, JpaSpecificationExecutor<DocmNo> {

    @Query(value = "SELECT prefix||lpad(to_char(nvl(last_generated_no,0)+1),5,'0') prefix , nvl(last_generated_no,0)+1 last_generated_no " +
            "FROM docm_no WHERE company_code = :companyCode and plant_no = :plantNo and type = 'GRN' AND sub_type = 'N'", nativeQuery = true)
    List<Object[]> getLastGeneratedNoforGRN(String companyCode, Integer plantNo);

    @Modifying
    @Query(value = "update docm_no set last_generated_no = :lastGeneratedNo where company_code = :companyCode " +
            "and plant_no = :plantNo and sub_type = 'N' and type = 'GRN'", nativeQuery = true)
    void updateLastGeneratedNo(BigDecimal lastGeneratedNo, String companyCode, Integer plantNo);

    @Query(value = "SELECT prefix||lpad(to_char(nvl(last_generated_no,0)+1),5,'0') prefix, nvl(last_generated_no,0)+1 last_generated_no " +
            "FROM docm_no WHERE company_code = :companyCode and plant_no = :plantNo and type = 'GRN' AND sub_type = 'M'", nativeQuery = true)
    List<Object[]> getLastGeneratedNoforGRNManual(String companyCode, Integer plantNo);

}