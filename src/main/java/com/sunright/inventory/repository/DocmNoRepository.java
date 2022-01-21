package com.sunright.inventory.repository;

import com.sunright.inventory.entity.docmno.DocmNo;
import com.sunright.inventory.entity.docmno.DocmNoId;
import com.sunright.inventory.entity.docmno.DocmNoProjection;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocmNoRepository extends PagingAndSortingRepository<DocmNo, DocmNoId>, JpaSpecificationExecutor<DocmNo> {

    @Query(value = "SELECT concat(prefix, lpad(to_char(coalesce(last_generated_no,0)+1),5,'0')) as generatedNo, (coalesce(last_generated_no,0)+1) as docmNo " +
            "FROM DOCM_NO where company_code = :companyCode and plant_no = :plantNo and type = :type and sub_type = :subType")
    DocmNoProjection getLastGeneratedNo(String companyCode, Integer plantNo, String type, String subType);

    @Modifying
    @Query(value = "update DOCM_NO set last_generated_no = :lastGeneratedNo where company_code = :companyCode " +
            "and plant_no = :plantNo and sub_type = :subType and type = :type")
    void updateLastGeneratedNo(Integer lastGeneratedNo, String companyCode, Integer plantNo, String subType, String type);
}