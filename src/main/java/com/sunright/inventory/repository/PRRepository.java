package com.sunright.inventory.repository;

import com.sunright.inventory.entity.pr.PR;
import com.sunright.inventory.entity.pr.PRId;
import com.sunright.inventory.entity.pr.PRProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface PRRepository extends JpaRepository<PR, PRId>, JpaSpecificationExecutor<PR> {

    @Query("SELECT pr.remarks as remarks FROM PR pr where pr.id.companyCode = :companyCode and pr.id.plantNo = :plantNo " +
            "and pr.id.docmNo = :docmNo")
    PRProjection prRmk(String companyCode, Integer plantNo, String docmNo);

    @Query("SELECT pr.id.docmNo as docmNo, pr.status as status FROM PR pr where pr.id.companyCode = :companyCode " +
            "and pr.id.plantNo = :plantNo and pr.id.docmNo = :docmNo")
    PRProjection cPr(String companyCode, Integer plantNo, String docmNo);

    @Query("SELECT count(pr) as countReqItem FROM PR pr WHERE pr.id.companyCode = :companyCode AND pr.id.plantNo = :plantNo " +
            "AND pr.id.docmNo = :docmNo and pr.status = 'A'")
    PRProjection cReqItem(String companyCode, Integer plantNo, String docmNo);

    @Modifying
    @Query("UPDATE PR pr set pr.status = 'C', pr.closedDate = :closedDate " +
            "WHERE pr.id.companyCode = :companyCode and pr.id.plantNo = :plantNo " +
            "and pr.id.docmNo = :docmNo")
    void updateStatusClosedDate(Date closedDate, String companyCode, Integer plantNo, String docmNo);
}
