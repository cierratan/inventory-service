package com.sunright.inventory.service;

import com.sunright.inventory.dto.grn.GrnDTO;
import com.sunright.inventory.dto.grn.GrnDetDTO;
import com.sunright.inventory.dto.grn.GrnSupplierDTO;
import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.dto.supplier.SupplierDTO;
import net.sf.jasperreports.engine.JRException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

public interface GrnService {

    GrnDTO createGrn(GrnDTO input);

    GrnDTO findBy(Long id);

    SearchResult<GrnDTO> searchBy(SearchRequest searchRequest);

    List<GrnDTO> findAllPoNo();

    GrnDTO getGrnHeader(String poNo);

    List<GrnDetDTO> getAllPartNo(String poNo, String partNo, String itemNo);

    GrnDetDTO getGrnDetail(String poNo, String itemNo, String partNo, Integer poRecSeq);

    GrnDTO getDefaultValueForGrnManual();

    GrnDTO checkIfGrnExists(String grnNo);

    GrnDTO checkIfMsrNoValid(String msrNo);

    GrnDetDTO checkNextItem(GrnDTO input);

    DocmValueDTO getGeneratedNo();

    DocmValueDTO getGeneratedNoManual();

    GrnDTO generateReportGrn(HttpServletRequest request, HttpServletResponse response, String grnNo, String subType, String type);

    GrnDTO generatePickListGrn(HttpServletRequest request, HttpServletResponse response, String grnNo, String projectNo, String orderNo);

    GrnDTO generateLabelGrn(HttpServletRequest request, HttpServletResponse response, String grnNo);
}