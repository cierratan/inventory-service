package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.docmno.DocmNoDTO;
import com.sunright.inventory.dto.grn.GrnDTO;
import com.sunright.inventory.dto.grn.GrnDetDTO;
import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.pur.PurDTO;
import com.sunright.inventory.dto.pur.PurDetDTO;
import com.sunright.inventory.dto.search.Filter;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.dto.supplier.SupplierDTO;
import com.sunright.inventory.entity.ItemProjection;
import com.sunright.inventory.entity.bombypj.BombypjProjection;
import com.sunright.inventory.entity.docmno.DocmNoProjection;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.grn.Grn;
import com.sunright.inventory.entity.grn.GrnDet;
import com.sunright.inventory.entity.grn.GrnSupplierProjection;
import com.sunright.inventory.entity.msr.MSR;
import com.sunright.inventory.entity.msr.MSRDetailProjection;
import com.sunright.inventory.entity.pur.DraftPurProjection;
import com.sunright.inventory.entity.pur.PurDetProjection;
import com.sunright.inventory.entity.pur.PurProjection;
import com.sunright.inventory.exception.DuplicateException;
import com.sunright.inventory.exception.NotFoundException;
import com.sunright.inventory.exception.ServerException;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.*;
import com.sunright.inventory.repository.lov.DefaultCodeDetailRepository;
import com.sunright.inventory.service.GrnService;
import com.sunright.inventory.util.QueryGenerator;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.File;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;

@Transactional
@Service
@Slf4j
public class GrnServiceImpl implements GrnService {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private GrnRepository grnRepository;

    @Autowired
    private GrnDetRepository grnDetRepository;

    @Autowired
    private DocmNoRepository docmNoRepository;

    @Autowired
    private PurRepository purRepository;

    @Autowired
    private PurDetRepository purDetRepository;

    @Autowired
    private DraftPurRepository draftPurRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private MSRRepository msrRepository;

    @Autowired
    private MsrDetailRepository msrDetailRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private DefaultCodeDetailRepository defaultCodeDetailRepository;

    @Autowired
    private BombypjRepository bombypjRepository;

    @Autowired
    private QueryGenerator queryGenerator;

    @Override
    public List<GrnDTO> findAllPoNo() {

        List<PurProjection> listPoNo = purRepository.getAllPoNo(
                UserProfileContext.getUserProfile().getCompanyCode(),
                UserProfileContext.getUserProfile().getPlantNo());

        return listPoNo.stream().map(pur -> GrnDTO.builder()
                .poNo(pur.getPoNo())
                .build()).collect(Collectors.toList());
    }

    @Override
    public GrnDTO getGrnHeader(String poNo) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String date = sdf.format(new Date());
        UserProfile userProfile = UserProfileContext.getUserProfile();
        GrnDTO grnDTO = GrnDTO.builder().build();
        checkStatusPoNo(poNo, userProfile);
        DocmNoDTO prefixGrnNo = getLastGeneratedNoforGRN(userProfile);
        PurDTO purInfoList = getPurInfo(poNo, userProfile);
        grnDTO.setGrnNo(prefixGrnNo.getPrefix());
        if (grnDTO.getGrnNo() == null) {
            throw new ServerException("Grn No Can Not be Blank !");
        } else {
            grnDTO.setSupplierCode(purInfoList.getSupplierCode());
            grnDTO.setCurrencyCode(purInfoList.getCurrencyCode());
            grnDTO.setCurrencyRate(purInfoList.getCurrencyRate());
            grnDTO.setBuyer(purInfoList.getBuyer());
            grnDTO.setRlseDate(purInfoList.getRlseDate());
            grnDTO.setPoRemarks(purInfoList.getRemarks());
            try {
                grnDTO.setRecdDate(sdf.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SupplierDTO supName = supplierName(grnDTO.getSupplierCode(), userProfile);
            grnDTO.setSupplierName(supName.getName());
            if (grnDTO.getSupplierName() == null) {
                throw new NotFoundException("Supplier Code not found");
            }
        }
        return grnDTO;
    }

    private DocmNoDTO getLastGeneratedNoforGRN(UserProfile userProfile) {

        DocmNoProjection generatedNo = docmNoRepository.getLastGeneratedNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), "GRN", "N");
        return DocmNoDTO.builder().prefix(generatedNo.getGeneratedNo()).lastGeneratedNo(generatedNo.getDocmNo()).build();
    }

    private void checkStatusPoNo(String poNo, UserProfile userProfile) {

        PurProjection statusPoNo = purRepository.checkStatusPoNoPur(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo);
        DraftPurProjection statusPoNo2 = draftPurRepository.checkStatusPoNoDraftPur(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo);
        if (statusPoNo2.getOpenClose().equalsIgnoreCase("C")) {
            throw new ServerException("PO already Closed, Purchase Receipt not allowed.");
        } else if (!statusPoNo2.getOpenClose().equalsIgnoreCase("A")) {
            throw new ServerException("PO is yet to be Approved, Purchase Receipt not allowed.");
        } else if (statusPoNo2.getOpenClose().equalsIgnoreCase("V")) {
            throw new ServerException("PO already Voided, Purchase Receipt not allowed.");
        } else if (statusPoNo2.getPoNo() == null) {
            throw new ServerException("Invalid PO No!");
        } else {
            if (statusPoNo2.getPoNo() != null && statusPoNo2.getOpenClose() != null) {
                if (statusPoNo.getOpenClose().equalsIgnoreCase("C")) {
                    throw new ServerException("PO already Closed, Purchase Receipt not allowed.");
                } else if (!statusPoNo.getOpenClose().equalsIgnoreCase("A")) {
                    throw new ServerException("PO is yet to be Approved, Purchase Receipt not allowed.");
                }
            }
        }
    }

    private PurDTO getPurInfo(String poNo, UserProfile userProfile) {

        PurProjection purInfo = purRepository.getPurInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo);
        return PurDTO.builder().supplierCode(purInfo.getSupplierCode()).currencyCode(purInfo.getCurrencyCode())
                .currencyRate(purInfo.getCurrencyRate()).buyer(purInfo.getBuyer()).rlseDate(purInfo.getRlseDate())
                .remarks(purInfo.getRemarks()).build();
    }

    private SupplierDTO supplierName(String supplierCode, UserProfile userProfile) {

        GrnSupplierProjection supplierNameInfo = supplierRepository.getSupplierName(userProfile.getCompanyCode(), userProfile.getPlantNo(), supplierCode);
        return SupplierDTO.builder().name(supplierNameInfo.getName()).build();
    }

    @Override
    public List<GrnDetDTO> getAllPartNo(String poNo, String partNo, String itemNo) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        List<PurDetProjection> list = purDetRepository.getDataFromPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo, partNo, itemNo);
        return list.stream().map(purDet -> GrnDetDTO.builder()
                .seqNo(purDet.getSeqNo())
                .partNo(purDet.getPartNo())
                .itemNo(purDet.getItemNo())
                .poRecSeq(purDet.getRecSeq())
                .build()).collect(Collectors.toList());
    }

    @Override
    public GrnDetDTO getGrnDetail(String poNo, String itemNo, String partNo, Integer poRecSeq) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        GrnDetDTO grnDetDTO = GrnDetDTO.builder().build();
        PurDetDTO getDataDetail = getDetailInfo(poNo, itemNo, partNo, poRecSeq, userProfile);

        if (partNo != null) {
            checkDataFromPartNo(poNo, partNo, poRecSeq, userProfile);
        } else {
            checkDataFromItemNo(poNo, itemNo, userProfile);
        }

        if (partNo == null && itemNo == null) {
            checkDataFromPartNo(poNo, null, poRecSeq, userProfile);
        }

        grnDetDTO.setPoRecSeq(getDataDetail.getRecSeq());
        grnDetDTO.setItemNo(getDataDetail.getItemNo());
        grnDetDTO.setPartNo(getDataDetail.getPartNo());
        grnDetDTO.setLoc(getDataDetail.getLoc());
        grnDetDTO.setItemType(getDataDetail.getItemType());
        grnDetDTO.setProjectNo(getDataDetail.getProjectNo());
        grnDetDTO.setPoNo(getDataDetail.getPoNo());
        grnDetDTO.setDescription(getDataDetail.getDescription());
        grnDetDTO.setMslCode(getDataDetail.getMslCode());
        String codeDesc = defaultCodeDetailRepository.findCodeDescBy(userProfile.getCompanyCode(), userProfile.getPlantNo(), getDataDetail.getUom());
        grnDetDTO.setUom(codeDesc);
        grnDetDTO.setOrderQty(getDataDetail.getOrderQty());
        grnDetDTO.setPoPrice(getDataDetail.getUnitPrice());
        grnDetDTO.setDueDate(getDataDetail.getDueDate());
        grnDetDTO.setResvQty(getDataDetail.getResvQty());
        grnDetDTO.setInvUom(getDataDetail.getInvUom());
        grnDetDTO.setStdPackQty(getDataDetail.getStdPackQty());
        grnDetDTO.setRemarks(getDataDetail.getRemarks());

        return grnDetDTO;
    }

    @Override
    public GrnDTO getDefaultValueForGrnManual() {

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String date = sdf.format(new Date());
        UserProfile userProfile = UserProfileContext.getUserProfile();
        GrnDTO dto = GrnDTO.builder().build();
        DocmNoProjection docmNo = docmNoRepository.getLastGeneratedNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), "GRN", "M");
        if (docmNo == null) {
            throw new NotFoundException("Not found in DOCM_NO table for type GRN-M !");
        } else {
            dto.setGrnNo(docmNo.getGeneratedNo());
            dto.setSubType("M");
            dto.setStatuz("Y");
            try {
                dto.setRecdDate(sdf.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            dto.setCurrencyRate(new BigDecimal(1));
            dto.setCurrencyCode("USD");
            dto.setEntryUser(userProfile.getUsername());
            try {
                dto.setEntryDate(sdf.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return dto;
    }

    @Override
    public GrnDTO checkIfGrnExists(String grnNo) {

        GrnDTO dto = GrnDTO.builder().build();
        Optional<Grn> grnOptional = grnRepository.findGrnByGrnNo(grnNo);
        if (grnOptional.isPresent()) {
            throw new DuplicateException("GRN Record exists ! New GRN No: " + grnNo + " is being assigned !");
        }
        return dto;
    }

    @Override
    public GrnDTO checkIfMsrNoValid(String msrNo) {

        GrnDTO dto = GrnDTO.builder().build();
        Optional<MSR> msrOptional = msrRepository.findMSRByMsrNo(msrNo);
        if (!msrOptional.isPresent()) {
            throw new ServerException("Invalid MSR No");
        } else {
            dto.setMsrNo(msrOptional.get().getMsrNo());
        }
        return dto;
    }

    @Override
    public GrnDetDTO checkNextItem(GrnDTO input) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        GrnDetDTO dto = GrnDetDTO.builder().build();
        String msrNo = input.getMsrNo();

        if (!CollectionUtils.isEmpty(input.getGrnDetails())) {
            for (GrnDetDTO detail : input.getGrnDetails()) {
                String itemNo = detail.getItemNo();
                String partNo = detail.getPartNo();
                Integer itemType = detail.getItemType();
                String projectNo = detail.getProjectNo();
                String poNo = detail.getPoNo();
                BigDecimal recdPrice = detail.getRecdPrice();
                BigDecimal recdQty = detail.getRecdQty();
                BigDecimal retnQty = detail.getRetnQty();
                BigDecimal labelQty = detail.getLabelQty();

                if (itemType == null) {
                    checkItemType();
                }

                if (itemNo == null) {
                    checkItemNo();
                }

                if (itemType != null) {
                    if (!Integer.toString(itemType).contains("0") && !Integer.toString(itemType).contains("1")) {
                        checkItemTypeNotNull();
                    }
                }

                if (msrNo != null) {
                    if (itemNo != null) {
                        MSRDetailProjection countByItemNo = msrDetailRepository.getCountMsrByItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), msrNo, itemNo);
                        if (countByItemNo.getCountItemNo() == 0) {
                            checkMsrItemNo();
                        } else if (countByItemNo.getCountItemNo() > 1) {
                            MSRDetailProjection lovPartNo = msrDetailRepository.showLovPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), msrNo, partNo, itemNo);
                            MSRDetailProjection info = msrDetailRepository.itemInfo(userProfile.getCompanyCode(),
                                    userProfile.getPlantNo(), msrNo, lovPartNo.getPartNo(), lovPartNo.getItemNo(), lovPartNo.getSeqNo());

                            dto.setPartNo(info.getPartNo());
                            dto.setSeqNo(info.getSeqNo());
                            dto.setItemNo(info.getItemNo());
                            dto.setDescription(info.getRemarks());
                            dto.setMslCode(info.getMslCode());
                            dto.setItemType(info.getItemType());
                            dto.setLoc(info.getLoc());
                            dto.setUom(info.getUom());
                            dto.setProjectNo(info.getProjectNo());
                            dto.setGrnNo(info.getGrnNo());
                            dto.setRetnQty(info.getRetnQty());
                            dto.setRetnPrice(info.getRetnPrice());
                        }

                        MSRDetailProjection countByPartNo = msrDetailRepository.getCountMsrByPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), msrNo, partNo);
                        if (countByPartNo.getCountPartNo() == 0) {
                            checkMsrPartNo();
                        } else if (countByPartNo.getCountPartNo() > 1) {
                            MSRDetailProjection lovPartNo = msrDetailRepository.showLovPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), msrNo, partNo, itemNo);
                            MSRDetailProjection info = msrDetailRepository.itemInfo(userProfile.getCompanyCode(),
                                    userProfile.getPlantNo(), msrNo, lovPartNo.getPartNo(), lovPartNo.getItemNo(), lovPartNo.getSeqNo());

                            dto.setPartNo(info.getPartNo());
                            dto.setSeqNo(info.getSeqNo());
                            dto.setItemNo(info.getItemNo());
                            dto.setDescription(info.getRemarks());
                            dto.setMslCode(info.getMslCode());
                            dto.setItemType(info.getItemType());
                            dto.setLoc(info.getLoc());
                            dto.setUom(info.getUom());
                            dto.setProjectNo(info.getProjectNo());
                            dto.setGrnNo(info.getGrnNo());
                            dto.setRetnQty(info.getRetnQty());
                            dto.setRetnPrice(info.getRetnPrice());
                        }

                        if (!(countByItemNo.getCountItemNo() == 0 && countByItemNo.getCountItemNo() > 1 && countByPartNo.getCountPartNo() == 0 && countByPartNo.getCountPartNo() > 1)) {
                            MSRDetailProjection detailProjection = msrDetailRepository.itemInfo(userProfile.getCompanyCode(),
                                    userProfile.getPlantNo(), msrNo, dto.getPartNo(), dto.getItemNo(), dto.getSeqNo());

                            dto.setPartNo(detailProjection.getPartNo());
                            dto.setSeqNo(detailProjection.getSeqNo());
                            dto.setItemNo(detailProjection.getItemNo());
                            dto.setDescription(detailProjection.getRemarks());
                            dto.setMslCode(detailProjection.getMslCode());
                            dto.setItemType(detailProjection.getItemType());
                            dto.setLoc(detailProjection.getLoc());
                            dto.setUom(detailProjection.getUom());
                            dto.setProjectNo(detailProjection.getProjectNo());
                            dto.setGrnNo(detailProjection.getGrnNo());
                            dto.setRetnQty(detailProjection.getRetnQty());
                            dto.setRetnPrice(detailProjection.getRetnPrice());
                            Optional<Grn> optionalGrn = grnRepository.findGrnByGrnNo(dto.getGrnNo());
                            if (optionalGrn.isPresent()) {
                                dto.setPoNo(optionalGrn.get().getPoNo());
                            }

                        }
                    }
                }

                if (itemType != null) {
                    if (itemType == 0) {
                        if (itemNo == null) {
                            checkItemNo();
                        } else {
                            if (itemNo != null) {
                                ItemProjection countItemNo = itemRepository.getCountByItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo);
                                if (countItemNo.getCountItemNo() == 0) {
                                    checkValidItemNo();
                                } else if (countItemNo.getCountItemNo() > 1) {
                                    List<ItemProjection> lovItemPart = itemRepository.lovItemPart(userProfile.getCompanyCode(), userProfile.getPlantNo(), partNo, itemNo);
                                    for (ItemProjection data : lovItemPart) {
                                        dto.setPartNo(data.getPartNo());
                                        dto.setItemNo(data.getItemNo());
                                    }
                                    ItemProjection getItemInfo = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getItemNo());
                                    dto.setPartNo(getItemInfo.getPartNo());
                                    dto.setItemNo(getItemInfo.getItemNo());
                                    dto.setDescription(getItemInfo.getDescription());
                                    dto.setLoc(getItemInfo.getLoc());
                                    dto.setUom(getItemInfo.getUom());
                                } else {
                                    List<ItemProjection> byItemNo = itemRepository.getItemAndPartNoByItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo);
                                    for (ItemProjection data : byItemNo) {
                                        dto.setItemNo(data.getItemNo());
                                        dto.setPartNo(data.getPartNo());
                                    }
                                }

                                ItemProjection countPartNo = itemRepository.getCountByPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getPartNo());
                                if (countPartNo.getCountPartNo() == 0) {
                                    checkValidPartNo();
                                } else if (countPartNo.getCountPartNo() > 1) {
                                    List<ItemProjection> lovItemPart = itemRepository.lovItemPart(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getPartNo(), dto.getItemNo());
                                    for (ItemProjection data : lovItemPart) {
                                        dto.setPartNo(data.getPartNo());
                                        dto.setItemNo(data.getItemNo());
                                    }
                                    ItemProjection getItemInfo = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getItemNo());
                                    dto.setPartNo(getItemInfo.getPartNo());
                                    dto.setItemNo(getItemInfo.getItemNo());
                                    dto.setDescription(getItemInfo.getDescription());
                                    dto.setLoc(getItemInfo.getLoc());
                                    dto.setUom(getItemInfo.getUom());
                                } else {
                                    List<ItemProjection> byPartNo = itemRepository.getItemAndPartNoByPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getPartNo());
                                    for (ItemProjection data : byPartNo) {
                                        dto.setItemNo(data.getItemNo());
                                        dto.setPartNo(data.getPartNo());
                                    }
                                }

                                ItemProjection getItemInfo = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getItemNo());
                                dto.setPartNo(getItemInfo.getPartNo());
                                dto.setItemNo(getItemInfo.getItemNo());
                                dto.setDescription(getItemInfo.getDescription());
                                dto.setLoc(getItemInfo.getLoc());
                                dto.setUom(getItemInfo.getUom());
                            }

                            if (projectNo != null) {
                                BombypjProjection prjNo = bombypjRepository.getPrjNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), projectNo);
                                if (prjNo == null) {
                                    checkProjectNoIfNull();
                                } else if (itemType == 0) {
                                    BombypjProjection altnt = bombypjRepository.getAltrnt(userProfile.getCompanyCode(), userProfile.getPlantNo(), prjNo.getProjectNo(), itemNo);
                                    if (altnt == null) {
                                        checkItemNoInProject();
                                    }
                                }
                            }

                            if (poNo != null) {
                                PurDetProjection poNoRecSeq = purDetRepository.getPoNoAndRecSeq(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemType, dto.getItemNo(), dto.getPartNo(), poNo);
                                if (poNoRecSeq == null) {
                                    checkValidPoNo();
                                } else if (poNoRecSeq.getRecSeq() == null) {
                                    checkItemNotInPo();
                                }
                            }

                            if (recdPrice != null) {
                                if (recdPrice.intValue() < 0) {
                                    checkValidRecdPrice();
                                } else if (itemNo != null) {
                                    ItemProjection src = itemRepository.getSource(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo);
                                    if (src == null) {
                                        checkSourceStockItem();
                                    }

                                    if (src.getSource().equalsIgnoreCase("C")) {
                                        if (recdPrice.intValue() > 0) {
                                            checkValidRecdPriceForConsignedItem();
                                        }
                                    }
                                }
                            }

                            if (recdQty != null) {
                                if (recdQty.intValue() <= 0) {
                                    checkRecdQty();
                                } else if (retnQty.intValue() > 0 && recdQty.intValue() > retnQty.intValue()) {
                                    checkRecdRetnQty(retnQty);
                                }
                            }

                            if (labelQty != null) {
                                if (labelQty.intValue() <= 0) {
                                    checkLabelQty();
                                } else if (recdQty.intValue() < labelQty.intValue()) {
                                    checkRecdLabelQty(recdQty);
                                }
                            }
                        }
                    }
                }
            }
        }

        return dto;
    }

    @Override
    public DocmValueDTO getGeneratedNo() {
        DocmNoProjection docmNo = docmNoRepository.getLastGeneratedNo(
                UserProfileContext.getUserProfile().getCompanyCode(),
                UserProfileContext.getUserProfile().getPlantNo(),
                "GRN",
                "N");

        return DocmValueDTO.builder()
                .generatedNo(docmNo.getGeneratedNo())
                .docmNo(docmNo.getDocmNo())
                .build();
    }

    @Override
    public DocmValueDTO getGeneratedNoManual() {
        DocmNoProjection docmNo = docmNoRepository.getLastGeneratedNo(
                UserProfileContext.getUserProfile().getCompanyCode(),
                UserProfileContext.getUserProfile().getPlantNo(),
                "GRN",
                "M");

        return DocmValueDTO.builder()
                .generatedNo(docmNo.getGeneratedNo())
                .docmNo(docmNo.getDocmNo())
                .build();
    }

    @Override
    public GrnDTO generateReportGrn(HttpServletRequest request, HttpServletResponse response, String grnNo, String subType, String type) {

        try {
            File file = ResourceUtils.getFile("classpath:detail.jrxml");
            File headerPath = ResourceUtils.getFile("classpath:header.jasper");
            Connection con = dataSource.getConnection();
            String filename = "" + grnNo + "_Report" + ".pdf";
            JasperDesign jasperDesign = JRXmlLoader.load(file);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("GRN_NO", grnNo);
            parameters.put("SUB_TYPE", subType);
            parameters.put("TYPE", type);
            parameters.put("SUB_REPORT", headerPath);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, con);
            response.setContentType("application/x-download");
            response.addHeader("Content-disposition", "attachment; filename=" + filename);
            OutputStream out = response.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, out);
            con.close();
            return GrnDTO.builder().message("Successfully Generate Report !").build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServerException(e.getMessage());
        }
    }

    @Override
    public GrnDTO generatePickListGrn(HttpServletRequest request, HttpServletResponse response, String grnNo,
                                      String projectNo, String orderNo) {
        try {
            UserProfile userProfile = UserProfileContext.getUserProfile();
            File file = ResourceUtils.getFile("classpath:pick_list.jrxml");
            File headerPath = ResourceUtils.getFile("classpath:header_pick_list.jasper");
            Connection con = dataSource.getConnection();
            String filename = "" + grnNo + "_PickList" + ".pdf";
            JasperDesign jasperDesign = JRXmlLoader.load(file);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("USERNAME", userProfile.getUsername());
            parameters.put("COMPANY_CODE", userProfile.getCompanyCode());
            parameters.put("PLANT_NO", userProfile.getPlantNo());
            parameters.put("PROJECT_NO", projectNo);
            parameters.put("ORDER_NO", orderNo);
            parameters.put("SUB_REPORT", headerPath);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, con);
            response.setContentType("application/x-download");
            response.addHeader("Content-disposition", "attachment; filename=" + filename);
            OutputStream out = response.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, out);
            con.close();
            return GrnDTO.builder().message("Successfully Generate Pick List !").build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServerException(e.getMessage());
        }
    }

    @Override
    public GrnDTO generateLabelGrn(HttpServletRequest request, HttpServletResponse response, String grnNo) {

        try {
            UserProfile userProfile = UserProfileContext.getUserProfile();
            File file = ResourceUtils.getFile("classpath:label.jrxml");
            Connection con = dataSource.getConnection();
            String filename = "" + grnNo + "_Label" + ".pdf";
            JasperDesign jasperDesign = JRXmlLoader.load(file);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("GRN_NO", grnNo);
            parameters.put("COMPANY_CODE", userProfile.getCompanyCode());
            parameters.put("PLANT_NO", userProfile.getPlantNo());
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, con);
            response.setContentType("application/x-download");
            response.addHeader("Content-disposition", "attachment; filename=" + filename);
            OutputStream out = response.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, out);
            return GrnDTO.builder().message("Successfully Generate Label !").build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServerException(e.getMessage());
        }
    }

    private void checkItemTypeNotNull() {
        throw new ServerException("Please enter 0-Stock, 1-Non Stock !");
    }

    private void checkValidRecdPrice() {
        throw new ServerException("Invalid Recd Price");
    }

    private void checkRecdLabelQty(BigDecimal recdQty) {
        throw new ServerException("Received Qty only " + recdQty + " units !");
    }

    private void checkLabelQty() {
        throw new ServerException("Qty/Label MUST be > 0 !");
    }

    private void checkRecdRetnQty(BigDecimal retnQty) {
        throw new ServerException("Return Qty only " + retnQty + " units !");
    }

    private void checkRecdQty() {
        throw new ServerException("Received Qty MUST be > 0 !");
    }

    private void checkValidRecdPriceForConsignedItem() {
        throw new ServerException("Invalid Recd Price for Consigned Item");
    }

    private void checkSourceStockItem() {
        throw new ServerException("Unknown source for Stock Item");
    }

    private void checkItemNotInPo() {
        throw new ServerException("Item not in PO");
    }

    private void checkValidPoNo() {
        throw new ServerException("PO No is Invalid / Not found in Master File !");
    }

    private void checkItemNoInProject() {
        throw new ServerException("Item not found in project");
    }

    private void checkProjectNoIfNull() {
        throw new ServerException("Project No is Invalid / Not found in Master File !");
    }

    private void checkValidPartNo() {
        throw new ServerException("The Part No is invalid!");
    }

    private void checkValidItemNo() {
        throw new ServerException("The Item No is invalid!");
    }

    private void checkMsrPartNo() {
        throw new ServerException("The Part No is either invalid or qty fully received!");
    }

    private void checkMsrItemNo() {
        throw new ServerException("The Item No is either invalid or qty fully received!");
    }

    private void checkItemType() {
        throw new ServerException("Item Type Can Not be Blank !");
    }

    private void checkItemNo() {
        throw new ServerException("Item No Can Not be Blank !");
    }

    private void checkDataFromItemNo(String poNo, String itemNo, UserProfile userProfile) {

        PurDetProjection checkItemNo = purDetRepository.countItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo, itemNo);
        if (checkItemNo.getCountItemNo() == 0) {
            throw new ServerException("The Part No is either invalid or qty fully received!");
        }
    }

    private void checkDataFromPartNo(String poNo, String partNo, Integer poRecSeq, UserProfile userProfile) {

        PurDetProjection checkPartNo = purDetRepository.countPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo, partNo);
        PurDetProjection duplicatePartNo = purDetRepository.checkDuplicatePartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo, partNo, poRecSeq);

        if (checkPartNo.getCountPartNo() == 0) {
            throw new ServerException("The Part No is either invalid or qty fully received!");
        }
        PurDetDTO dto = PurDetDTO.builder().build();
        dto.setRecSeq(duplicatePartNo.getRecSeq());
        dto.setPartNo(duplicatePartNo.getPartNo());
        String partno = dto.getPartNo();
        int recSeq = dto.getRecSeq();

        if (partNo.equals(partno) & poRecSeq == recSeq) {
            throw new DuplicateException("Duplicate Part No found!'");
        }
    }

    private PurDetDTO getDetailInfo(String poNo, String itemNo, String partNo, Integer poRecSeq, UserProfile userProfile) {

        PurDetProjection detailInfo = purDetRepository.getDataFromItemAndPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo,
                itemNo, partNo, poRecSeq);

        return PurDetDTO.builder().partNo(detailInfo.getPartNo())
                .recSeq(detailInfo.getRecSeq())
                .itemNo(detailInfo.getItemNo())
                .description(detailInfo.getDescription())
                .mslCode(detailInfo.getMslCode())
                .itemType(detailInfo.getItemType())
                .loc(detailInfo.getLoc())
                .uom(detailInfo.getUom())
                .projectNo(detailInfo.getProjectNo())
                .orderQty(detailInfo.getOrderQty())
                .unitPrice(detailInfo.getUnitPrice())
                .dueDate(detailInfo.getDueDate())
                .resvQty(detailInfo.getResvQty())
                .invUom(detailInfo.getInvUom())
                .stdPackQty(detailInfo.getStdPackQty())
                .remarks(detailInfo.getRemarks()).build();
    }

    @Override
    public GrnDTO createGrn(GrnDTO input) throws ParseException {
        UserProfile userProfile = UserProfileContext.getUserProfile();
        Grn grn = new Grn();
        BeanUtils.copyProperties(input, grn);
        grn.setCompanyCode(userProfile.getCompanyCode());
        grn.setPlantNo(userProfile.getPlantNo());
        grn.setStatus(Status.ACTIVE);
        grn.setCreatedBy(userProfile.getUsername());
        grn.setCreatedAt(ZonedDateTime.now());
        grn.setUpdatedBy(userProfile.getUsername());
        grn.setUpdatedAt(ZonedDateTime.now());

        Grn saved = grnRepository.save(grn);
        if (!CollectionUtils.isEmpty(input.getGrnDetails())) {
            for (GrnDetDTO detail : input.getGrnDetails()) {
                GrnDet grnDetail = new GrnDet();
                BeanUtils.copyProperties(detail, grnDetail);
                grnDetail.setCompanyCode(userProfile.getCompanyCode());
                grnDetail.setPlantNo(userProfile.getPlantNo());
                grnDetail.setGrn(saved);

                preSavingGrnDetail(input);
                grnDetRepository.save(grnDetail);
                grnDetailPostSaving(grnDetail);
            }
        }

        postSaving(userProfile, input);
        closePO(userProfile, input);
        populateAfterSaving(input, saved);

        return input;
    }

    private void grnDetailPostSaving(GrnDet grnDetail) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
    }

    private void closePO(UserProfile userProfile, GrnDTO input) throws ParseException {
        PurDetProjection purDetProjection = purDetRepository.getSumOrderQtyByPoNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getPoNo());
        if (purDetProjection.getOrderQty() != null && purDetProjection.getOrderQty().compareTo(BigDecimal.ZERO) == 0) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String strDate = formatter.format(calendar.getTime());
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date closeDate = df.parse(strDate);
            draftPurRepository.updatePOClose(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getPoNo(), closeDate);
            purRepository.updatePOClose(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getPoNo(), closeDate);
        }
    }

    private void preSavingGrnDetail(GrnDTO input) {

        if (input.getSubType().equalsIgnoreCase("N")) {
            for (GrnDetDTO detail : input.getGrnDetails()) {
                BigDecimal poPrice = detail.getPoPrice();
                BigDecimal recdPrice = detail.getRecdPrice();
                if (!poPrice.equals(recdPrice)) {
                    throw new ServerException("Unit price is not equal to receiving price !");
                }
            }
        }

        Optional<Grn> grnOptional = grnRepository.findGrnByGrnNo(input.getGrnNo());
        if (grnOptional.isPresent()) {
            throw new DuplicateException("GRN Record exists ! New GRN No: " + input.getGrnNo() + " is being assigned !");
        }
    }

    private void populateAfterSaving(GrnDTO input, Grn saved) {
        input.setId(saved.getId());
        input.setVersion(saved.getVersion());
    }

    private void postSaving(UserProfile userProfile, GrnDTO input) {

        String type = "GRN";
        DocmNoProjection docmNo = docmNoRepository.getLastGeneratedNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), type, input.getSubType());
        docmNoRepository.updateLastGeneratedNo(docmNo.getDocmNo(), userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getSubType(), type);
    }

    @Override
    public GrnDTO findBy(Long id) {
        return convertToGrnDTO(checkIfRecordExist(id));
    }

    private GrnDTO convertToGrnDTO(Grn grn) {
        Set<GrnDetDTO> grnDetails = new HashSet<>();
        if (!CollectionUtils.isEmpty(grn.getGrnDetails())) {
            grnDetails = grn.getGrnDetails().stream().map(detail -> {
                GrnDetDTO grnDetail = GrnDetDTO.builder()
                        .grnNo(detail.getGrnNo())
                        .subType(detail.getSubType())
                        .build();

                BeanUtils.copyProperties(detail, grnDetail);

                return grnDetail;
            }).collect(Collectors.toSet());
        }

        GrnDTO grnDTO = GrnDTO.builder().build();

        BeanUtils.copyProperties(grn.getId(), grnDTO);
        BeanUtils.copyProperties(grn, grnDTO);
        grnDTO.setGrnDetails(grnDetails);
        return grnDTO;
    }

    private Grn checkIfRecordExist(Long id) {

        Optional<Grn> optionalGrn = grnRepository.findById(id);

        if (!optionalGrn.isPresent()) {
            throw new NotFoundException("Record is not found");
        }
        return optionalGrn.get();
    }

    @Override
    public SearchResult<GrnDTO> searchBy(SearchRequest searchRequest) {
        Specification<Grn> specs = where(queryGenerator.createDefaultSpec());

        if (!CollectionUtils.isEmpty(searchRequest.getFilters())) {
            for (Filter filter : searchRequest.getFilters()) {
                specs = specs.and(queryGenerator.createSpecification(filter));
            }
        }

        Page<Grn> pgGRN = grnRepository.findAll(specs, queryGenerator.constructPageable(searchRequest));

        SearchResult<GrnDTO> result = new SearchResult<>();
        result.setTotalRows(pgGRN.getTotalElements());
        result.setTotalPages(pgGRN.getTotalPages());
        result.setCurrentPageNumber(pgGRN.getPageable().getPageNumber());
        result.setCurrentPageSize(pgGRN.getNumberOfElements());
        result.setRows(pgGRN.getContent().stream().map(grn -> convertToGrnDTO(grn)).collect(Collectors.toList()));

        return result;
    }
}
