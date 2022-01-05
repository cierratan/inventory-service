package com.sunright.inventory.repository;

import com.sunright.inventory.entity.DocmNo;
import com.sunright.inventory.entity.DocmNoId;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocmNoRepository extends PagingAndSortingRepository<DocmNo, DocmNoId>, JpaSpecificationExecutor<DocmNo> {

    @Query(value = "SELECT prefix||lpad(to_char(nvl(last_generated_no,0)+1),5,'0'), nvl(last_generated_no,0)+1 " +
            "FROM docm_no WHERE company_code = :companyCode and plant_no = :plantNo and type = 'GRN' AND sub_type = :subType", nativeQuery = true)
    List<Object[]> getLastGeneratedNoforGRN(String companyCode, Integer plantNo, String subType);

    @Modifying
    @Query("UPDATE DOCM_NO d set d.lastGeneratedNo = :lastGeneratedNo " +
            "WHERE d.id.companyCode = :companyCode and d.id.plantNo = :plantNo and d.id.subType = :subType and d.id.type = 'GRN'")
    void updateLastGeneratedNo(Integer lastGeneratedNo, String companyCode, Integer plantNo, String subType);

}