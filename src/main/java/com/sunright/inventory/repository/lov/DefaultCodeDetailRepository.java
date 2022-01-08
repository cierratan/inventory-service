package com.sunright.inventory.repository.lov;

import com.sunright.inventory.entity.lov.DefaultCodeDetail;
import com.sunright.inventory.entity.lov.DefaultCodeDetailId;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefaultCodeDetailRepository extends JpaRepository<DefaultCodeDetail, DefaultCodeDetailId>, JpaSpecificationExecutor<DefaultCodeDetail> {

    @Query("SELECT dtl " +
            "FROM DEFAULT_CODE_DET dtl join DEFAULT_CODE header on header.id.companyCode = dtl.id.companyCode and" +
            " header.id.plantNo = dtl.id.plantNo and header.id.defaultCode = dtl.id.defaultCode and header.dbStatus = 'Y' " +
            "WHERE dtl.id.companyCode = :companyCode and dtl.id.plantNo = :plantNo and dtl.id.defaultCode = :defaultCode")
    List<DefaultCodeDetail> findDefaultCodeDetailBy(String companyCode, Integer plantNo, String defaultCode, Sort sort);

    @Query("SELECT dtl.codeDesc " +
            "FROM DEFAULT_CODE_DET dtl join DEFAULT_CODE header on header.id.companyCode = dtl.id.companyCode and" +
            " header.id.plantNo = dtl.id.plantNo and header.id.defaultCode = dtl.id.defaultCode and header.dbStatus = 'Y' " +
            "WHERE dtl.id.companyCode = :companyCode and dtl.id.plantNo = :plantNo and dtl.id.defaultCode = 'INVENTORY.UOM' " +
            "and dtl.codeValue = :codeValue")
    String findCodeDescBy(String companyCode, Integer plantNo, String codeValue);
}
