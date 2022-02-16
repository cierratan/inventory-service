package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.ItemDTO;
import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.bombypj.BombypjDTO;
import com.sunright.inventory.dto.docmno.DocmNoDTO;
import com.sunright.inventory.dto.grn.GrnDTO;
import com.sunright.inventory.dto.grn.GrnDetDTO;
import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.msr.MsrDTO;
import com.sunright.inventory.dto.pur.PurDTO;
import com.sunright.inventory.dto.pur.PurDetDTO;
import com.sunright.inventory.dto.search.Filter;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.dto.supplier.SupplierDTO;
import com.sunright.inventory.entity.InAudit;
import com.sunright.inventory.entity.ItemLoc;
import com.sunright.inventory.entity.ItemLocProjection;
import com.sunright.inventory.entity.ItemProjection;
import com.sunright.inventory.entity.bombypj.BombypjDetailProjection;
import com.sunright.inventory.entity.bombypj.BombypjProjection;
import com.sunright.inventory.entity.company.CompanyProjection;
import com.sunright.inventory.entity.docmno.DocmNoProjection;
import com.sunright.inventory.entity.draftpur.DraftPurDetProjection;
import com.sunright.inventory.entity.draftpur.DraftPurProjection;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.grn.Grn;
import com.sunright.inventory.entity.grn.GrnDet;
import com.sunright.inventory.entity.grn.GrnSupplierProjection;
import com.sunright.inventory.entity.itembatc.ItemBatc;
import com.sunright.inventory.entity.itembatc.ItemBatcId;
import com.sunright.inventory.entity.msr.MSR;
import com.sunright.inventory.entity.msr.MSRDetailProjection;
import com.sunright.inventory.entity.nlctl.NLCTLProjection;
import com.sunright.inventory.entity.pur.PurDetProjection;
import com.sunright.inventory.entity.pur.PurProjection;
import com.sunright.inventory.entity.uom.UOMProjection;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
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
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
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
    private DraftPurDetRepository draftPurDetRepository;

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
    private BombypjDetailRepository bombypjDetailRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ItemLocRepository itemLocRepository;

    @Autowired
    private NLCTLRepository nlctlRepository;

    @Autowired
    private ItemBatcRepository itemBatcRepository;

    @Autowired
    private InAuditRepository inAuditRepository;

    @Autowired
    private UOMRepository uomRepository;

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

        UserProfile userProfile = UserProfileContext.getUserProfile();
        GrnDTO grnDTO = GrnDTO.builder().build();
        checkStatusPoNo(poNo, userProfile);
        DocmNoDTO prefixGrnNo = getLastGeneratedNoforGRN(userProfile);
        PurDTO purInfoList = getPurInfo(poNo, userProfile);
        grnDTO.setGrnNo(prefixGrnNo.getPrefix());
        if (StringUtils.isBlank(grnDTO.getGrnNo())) {
            throw new ServerException("Grn No Can Not be Blank !");
        } else {
            grnDTO.setSupplierCode(purInfoList.getSupplierCode());
            grnDTO.setCurrencyCode(purInfoList.getCurrencyCode());
            grnDTO.setCurrencyRate(purInfoList.getCurrencyRate());
            grnDTO.setBuyer(purInfoList.getBuyer());
            grnDTO.setRlseDate(purInfoList.getRlseDate());
            grnDTO.setPoRemarks(purInfoList.getRemarks());
            grnDTO.setRecdDate(new Timestamp(System.currentTimeMillis()));
            SupplierDTO supName = supplierName(grnDTO.getSupplierCode(), userProfile);
            grnDTO.setSupplierName(supName.getName());
            if (StringUtils.isBlank(grnDTO.getSupplierName())) {
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
        } else if (StringUtils.isBlank(statusPoNo2.getPoNo())) {
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
        if (StringUtils.isBlank(docmNo.getGeneratedNo())) {
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
        String poNo = input.getPoNo();

        if (!CollectionUtils.isEmpty(input.getGrnDetails())) {
            for (GrnDetDTO detail : input.getGrnDetails()) {
                String itemNo = detail.getItemNo();
                String partNo = detail.getPartNo();
                Integer poRecSeq = detail.getPoRecSeq();

                PurDetDTO purDetInfo = getDetailInfo(poNo, itemNo, partNo, poRecSeq, userProfile);

                BigDecimal orderQty = purDetInfo.getOrderQty();
                Integer itemType = detail.getItemType();
                String projectNo = detail.getProjectNo();
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
                                    List<ItemProjection> byPartNo = itemRepository.getItemAndPartNoByPartNo(userProfile.getCompanyCode(),
                                            userProfile.getPlantNo(), dto.getPartNo(), dto.getItemNo());
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
                                if (recdQty.compareTo(BigDecimal.ZERO) <= 0) {
                                    checkRecdQty();
                                }
                                if (input.getSubType().equalsIgnoreCase("M")) {
                                    if (retnQty.compareTo(BigDecimal.ZERO) > 0 && recdQty.compareTo(BigDecimal.ZERO) > retnQty.compareTo(BigDecimal.ZERO)) {
                                        checkRecdRetnQty(retnQty);
                                    }
                                }
                            }

                            if (labelQty != null) {
                                if (labelQty.compareTo(BigDecimal.ZERO) <= 0) {
                                    checkLabelQty();
                                } else if (recdQty.compareTo(BigDecimal.ZERO) < labelQty.compareTo(BigDecimal.ZERO)) {
                                    checkRecdLabelQty(recdQty);
                                }
                            }
                        }
                    }
                }

                if (StringUtils.isNotBlank(partNo)) {
                    if ((recdQty != null ? recdQty.compareTo(BigDecimal.ZERO) : 0) == 0) {
                        throw new ServerException("Received Qty cannot be empty or zero!");
                    }
                    if (orderQty != null) {
                        if (recdQty.compareTo(BigDecimal.ZERO) > 0 &&
                                recdQty.compareTo(BigDecimal.ZERO) > orderQty.compareTo(BigDecimal.ZERO)) {
                            throw new ServerException("Received more than Ordered is not allowed!");
                        }
                        BigDecimal recQty = detail.getRecdQty();
                        boolean isSuccess = true;
                        if (!poRecSeq.equals(detail.getPoRecSeq())) {
                            recQty = recQty.add(detail.getRecdQty());
                            if (recQty.compareTo(BigDecimal.ZERO) > orderQty.compareTo(BigDecimal.ZERO)) {
                                isSuccess = false;
                            }
                        }

                        if (!isSuccess) {
                            throw new ServerException("Receiving more than Ordered is not allowed!");
                        }
                    }
                }

                if (StringUtils.isNotBlank(partNo)) {
                    if (detail.getLabelQty().compareTo(BigDecimal.ZERO) == 0) {
                        throw new ServerException("Qty per label cannot be empty or zero");
                    } else if (detail.getLabelQty().compareTo(BigDecimal.ZERO) > 0 &&
                            detail.getLabelQty().compareTo(BigDecimal.ZERO) > recdQty.compareTo(BigDecimal.ZERO)) {
                        throw new ServerException("Qty per label is more than Received Qty !");
                    }
                }

                if (StringUtils.isNotBlank(partNo)) {
                    checkDataFromPartNo(input.getPoNo(), detail.getPartNo(), detail.getPoRecSeq(), userProfile);
                }

                if (StringUtils.isNotBlank(itemNo)) {
                    checkDataFromItemNo(input.getPoNo(), detail.getItemNo(), userProfile);
                }

                if (detail.getDateCode() != null) {
                    if (detail.getDateCode() != 0) {
                        Integer dateCode = detail.getDateCode();
                        int lengthOfDateCode = String.valueOf(dateCode).length();
                        if (lengthOfDateCode < 4) {
                            throw new ServerException("Invalid Date Code! Please provide in YYWW format.");
                        }
                        String strWK = detail.getDateCode().toString().substring(2);
                        int wK = Integer.parseInt(strWK);
                        if (wK == 0 || wK > 52) {
                            throw new ServerException("Invalid Week !");
                        }
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyww");
                        String strDate = formatter.format(calendar.getTime());
                        if (dateCode > Integer.parseInt(strDate)) {
                            throw new ServerException("Invalid Date Code ! The Date is in the future");
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
    public void generateReportGrn(HttpServletResponse response, String grnNo, String subType) {

        try {
            UserProfile userProfile = UserProfileContext.getUserProfile();
            File mainReport = ResourceUtils.getFile("classpath:header.jrxml");
            Connection con = dataSource.getConnection();
            String filename = "" + grnNo + "_Report" + ".pdf";
            JasperDesign jasperDesign = JRXmlLoader.load(mainReport.getAbsolutePath());
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("GRN_NO", grnNo);
            parameters.put("SUB_TYPE", subType);
            parameters.put("COMPANY_CODE", userProfile.getCompanyCode());
            parameters.put("PLANT_NO", userProfile.getPlantNo());
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, con);
            response.setContentType("application/x-download");
            response.addHeader("Content-disposition", "attachment; filename=" + filename);
            OutputStream out = response.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, out);
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void generatePickListGrn(HttpServletRequest request, HttpServletResponse response, String grnNo,
                                    String projectNo, String orderNo) {
        try {
            UserProfile userProfile = UserProfileContext.getUserProfile();
            File file = ResourceUtils.getFile("classpath:pick_list.jrxml");
            File headerPath = ResourceUtils.getFile("classpath:header_pick_list.jrxml");
            Connection con = dataSource.getConnection();
            String filename = "" + grnNo + "_PickList" + ".pdf";
            JasperDesign jasperDesign = JRXmlLoader.load(file.getAbsolutePath());
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void generateLabelGrn(HttpServletRequest request, HttpServletResponse response, String grnNo) {

        try {
            UserProfile userProfile = UserProfileContext.getUserProfile();
            File file = ResourceUtils.getFile("classpath:label.jrxml");
            Connection con = dataSource.getConnection();
            String filename = "" + grnNo + "_Label" + ".pdf";
            JasperDesign jasperDesign = JRXmlLoader.load(file.getAbsolutePath());
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
        } catch (Exception e) {
            e.printStackTrace();
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

        PurDetProjection purDetCnt = purDetRepository.countPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo, partNo);
        PurDetProjection cPartNo = purDetRepository.checkDuplicatePartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo, partNo, poRecSeq);

        if (purDetCnt.getCountPartNo() == 0) {
            throw new ServerException("The Part No is either invalid or qty fully received!");
        }

        if (cPartNo != null) {
            String vPartNo = cPartNo.getPartNo();
            Integer vRecSeq = cPartNo.getRecSeq();

            if (partNo.equals(vPartNo) & poRecSeq.equals(vRecSeq)) {
                throw new DuplicateException("Duplicate Part No found!'");
            }
        }
    }

    private PurDetDTO getDetailInfo(String poNo, String itemNo, String partNo, Integer poRecSeq, UserProfile userProfile) {

        PurDetProjection detailInfo = purDetRepository.getDataFromItemAndPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo,
                itemNo, partNo, poRecSeq);
        PurDetDTO dto = PurDetDTO.builder().build();
        if (detailInfo != null) {
            PurDetDTO purDetDTO = PurDetDTO.builder().partNo(detailInfo.getPartNo())
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

            BeanUtils.copyProperties(purDetDTO, dto);
        }

        return dto;
    }

    @Override
    public GrnDTO createGrn(GrnDTO input) throws ParseException {
        UserProfile userProfile = UserProfileContext.getUserProfile();
        Grn grn = new Grn();
        input.setRecdDate(new Timestamp(System.currentTimeMillis()));
        BeanUtils.copyProperties(input, grn);
        grn.setCompanyCode(userProfile.getCompanyCode());
        grn.setPlantNo(userProfile.getPlantNo());
        grn.setStatus(Status.ACTIVE);
        grn.setCreatedBy(userProfile.getUsername());
        grn.setCreatedAt(ZonedDateTime.now());
        grn.setUpdatedBy(userProfile.getUsername());
        grn.setUpdatedAt(ZonedDateTime.now());
        preSavingGrn(input);
        Grn saved = grnRepository.save(grn);
        if (!CollectionUtils.isEmpty(input.getGrnDetails())) {
            for (GrnDetDTO detail : input.getGrnDetails()) {
                GrnDet grnDetail = new GrnDet();
                checkRecdQtyBeforeSaving(detail, userProfile);
                if (input.getSubType().equalsIgnoreCase("M")) {
                    checkRecValidForGrnManual(userProfile, input, detail);
                }
                BeanUtils.copyProperties(detail, grnDetail);
                grnDetail.setCompanyCode(userProfile.getCompanyCode());
                grnDetail.setPlantNo(userProfile.getPlantNo());
                grnDetail.setGrn(saved);
                grnDetRepository.save(grnDetail);
                grnDetailPostSaving(saved, grnDetail, detail);
            }
        }

        grnPostSaving(userProfile, saved);
        closePO(userProfile, saved);
        populateAfterSaving(input, saved);

        return input;
    }

    private void checkRecValidForGrnManual(UserProfile userProfile, GrnDTO input, GrnDetDTO detail) {
        List<ItemProjection> itemCur = itemRepository.getItemAndPartNoByPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(),
                detail.getPartNo(), detail.getItemNo());
        BombypjProjection projCur = bombypjRepository.getPrjNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), detail.getProjectNo());
        BombypjProjection bombItemCur = bombypjRepository.getAltrnt(userProfile.getCompanyCode(), userProfile.getPlantNo(),
                detail.getProjectNo(), detail.getItemNo());
        PurDetProjection poCur = purDetRepository.getPoNoAndRecSeq(userProfile.getCompanyCode(), userProfile.getPlantNo(),
                detail.getItemType(), detail.getItemNo(), detail.getPartNo(), detail.getPoNo());
        Optional<MSR> msrCur = msrRepository.findMSRByMsrNo(input.getMsrNo());
        ItemProjection itemSrc = itemRepository.getSource(userProfile.getCompanyCode(), userProfile.getPlantNo(), detail.getItemNo());

        if (StringUtils.isNotBlank(input.getGrnNo()) && StringUtils.isBlank(detail.getItemNo())) {
            if (detail.getItemType() != 1) {
                throw new ServerException("No Item No found. GRN record NOT created !");
            }
        }

        if (!Integer.toString(detail.getItemType()).contains("0") && !Integer.toString(detail.getItemType()).contains("1")) {
            checkItemTypeNotNull();
        }

        ItemDTO dto = ItemDTO.builder().build();
        BombypjDTO bombypjDTO = BombypjDTO.builder().build();
        PurDTO purDTO = PurDTO.builder().build();

        if (detail.getItemType() == 0) {
            for (ItemProjection rec : itemCur) {
                dto.setItemNo(rec.getItemNo());
                dto.setLoc(rec.getLoc());
                dto.setUom(rec.getUom());
                dto.setPartNo(rec.getPartNo());
                dto.setDescription(rec.getDescription());
                dto.setStdMaterial(rec.getStdMaterial());
            }

            if (StringUtils.isBlank(dto.getItemNo())) {
                throw new ServerException("Item No is not Buy or Consigned Item !");
            } else {
                detail.setLoc(dto.getLoc());
                detail.setUom(dto.getUom());
                detail.setPartNo(dto.getPartNo());
                detail.setDescription(dto.getDescription());
                if (detail.getRecdPrice() == null) {
                    detail.setRecdPrice(dto.getStdMaterial());
                }
            }
        }

        if (StringUtils.isNotBlank(detail.getProjectNo())) {
            if (!detail.getProjectNo().equals("STOCK")) {
                dto.setItemNo(projCur.getProjectNo());
                if (StringUtils.isBlank(dto.getItemNo())) {
                    throw new ServerException("Project No : " + detail.getProjectNo() + " is Invalid / Not found in Master File !");
                } else if (detail.getItemType() == 0) {
                    bombypjDTO.setAlternate(bombItemCur.getAlternate());
                    if (StringUtils.isBlank(bombypjDTO.getAlternate())) {
                        throw new NotFoundException("Item" + detail.getItemNo() + " not found in project " + detail.getProjectNo() + "");
                    }
                }
            }
        }

        if (StringUtils.isNotBlank(detail.getPoNo())) {
            purDTO.setPoNo(poCur.getPoNo());
            detail.setPoRecSeq(poCur.getRecSeq());
            if (StringUtils.isBlank(purDTO.getPoNo())) {
                throw new ServerException("PO No : " + detail.getPoNo() + " is Invalid / Not found in Master File !");
            } else if (detail.getPoRecSeq() == null) {
                throw new ServerException("Item, " + detail.getItemNo() + " not in PO " + detail.getPoNo() + "");
            }
        }

        if (detail.getRecdQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServerException("Received Qty Must be > 0 !");
        } else if (detail.getRetnQty() != null) {
            if (detail.getRetnQty().compareTo(BigDecimal.ZERO) > 0 &&
                    detail.getRecdQty().compareTo(BigDecimal.ZERO) > detail.getRetnQty().compareTo(BigDecimal.ZERO)) {
                throw new ServerException("Return Qty only " + detail.getRetnQty() + " units !");
            }
        }

        if (detail.getLabelQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServerException("Qty/Label Must be > 0 !");
        } else if (detail.getRecdQty().compareTo(BigDecimal.ZERO) < detail.getLabelQty().compareTo(BigDecimal.ZERO)) {
            throw new ServerException("Received Qty only " + detail.getRecdQty() + " units !");
        }

        if (StringUtils.isNotBlank(input.getMsrNo())) {
            MsrDTO msrDTO = MsrDTO.builder().build();
            if (msrCur.isPresent()) {
                msrDTO.setMsrNo(msrCur.get().getMsrNo());
                if (StringUtils.isBlank(msrDTO.getMsrNo())) {
                    throw new ServerException("Invalid MSR No, " + input.getMsrNo() + "");
                }
            }

            if (StringUtils.isNotBlank(input.getMsrNo())) {
                MSRDetailProjection vRecdQty = msrDetailRepository.getRecdQtyByMsrNo(userProfile.getCompanyCode(),
                        userProfile.getPlantNo(), input.getMsrNo(), detail.getItemNo(), detail.getSeqNo());
                if (vRecdQty.getRecdQty().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ServerException("There is no outstanding for this MSR#" + input.getMsrNo() + "");
                }

                if (vRecdQty == null) {
                    throw new ServerException("Invalid MSR No / Item No");
                }
            }
        }

        if (detail.getRecdPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServerException("Invalid Recd Price");
        } else if (StringUtils.isNotBlank(detail.getItemNo())) {
            if (StringUtils.isBlank(itemSrc.getSource())) {
                throw new ServerException("Unknown source for Stock Item");
            } else if (itemSrc.getSource().equals("C")) {
                throw new ServerException("Invalid Recd Price for Consigned Item");
            }
        }
    }

    private void checkRecdQtyBeforeSaving(GrnDetDTO detail, UserProfile userProfile) {
        PurDetDTO purDetInfo = getDetailInfo(detail.getPoNo(), detail.getItemNo(), detail.getPartNo(), detail.getPoRecSeq(), userProfile);
        BigDecimal orderQty = purDetInfo.getOrderQty();
        if (orderQty != null) {
            Integer poRecSeq = 0;
            boolean isSuccess = true;
            if (!poRecSeq.equals(detail.getPoRecSeq())) {
                poRecSeq = detail.getPoRecSeq();
                BigDecimal recQty = detail.getRecdQty();
                if (detail.getPoRecSeq().equals(poRecSeq)) {
                    recQty = recQty.add(detail.getRecdQty());
                    if (recQty.compareTo(BigDecimal.ZERO) > orderQty.compareTo(BigDecimal.ZERO)) {
                        isSuccess = false;
                    }
                }
            }

            if (!isSuccess) {
                throw new ServerException("Receiving more than Ordered is not allowed!");
            }
        }
    }

    private void grnDetailPostSaving(Grn input, GrnDet grnDetail, GrnDetDTO detail) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        ItemProjection itemCur = itemRepository.getDataItemCur(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDetail.getItemNo());
        CompanyProjection stkLoc = companyRepository.getStockLoc(userProfile.getCompanyCode(), userProfile.getPlantNo());
        ItemLocProjection itemLc = itemLocRepository.getItemLocByItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDetail.getLoc(), grnDetail.getItemNo());
        ItemProjection itemUom = itemRepository.getItemUomByItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDetail.getItemNo());
        ItemLocProjection itemLocInfo = itemLocRepository.itemLocInfo(userProfile.getCompanyCode(),
                userProfile.getPlantNo(), grnDetail.getItemNo(), grnDetail.getLoc());


        if (grnDetail.getItemType() == 0) {
            String iUom = itemUom.getUom();
            BigDecimal costVar = BigDecimal.ZERO;
            BigDecimal convQty = null;
            BigDecimal convUom = BigDecimal.ZERO;

            if (!iUom.equals(grnDetail.getUom())) {
                throw new ServerException("Resv UOM does not match Inv UOM. Inform MIS.");
            } else if (grnDetail.getStdPackQty() == null) {
                throw new ServerException("Standard Pack Qty is empty. Check with Purchaser.");
            }
            BigDecimal currRate = input.getCurrencyRate();
            convQty = grnDetail.getRecdQty().multiply(grnDetail.getStdPackQty());
            BigDecimal convCost = (grnDetail.getRecdPrice().multiply(currRate)).divide(grnDetail.getStdPackQty());

            if (convCost == null) {
                throw new ServerException("The convented new Std Cost is null.");
            }
            BigDecimal grnValue = BigDecimal.ZERO;
            BigDecimal stockValue = BigDecimal.ZERO;
            BigDecimal finalGrnVar = BigDecimal.ZERO;
            DecimalFormat df = new DecimalFormat("#.####");
            df.setRoundingMode(RoundingMode.CEILING);
            if (input.getSubType().equals("M")) {
                UOMProjection uomFactor = uomRepository.getUomFactor(detail.getUom(), detail.getUom());
                convUom = uomFactor.getUomFactor();
                grnValue = (grnDetail.getRecdQty().multiply(convUom)).multiply(detail.getRecdPrice().multiply(currRate).divide(convUom));
                stockValue = (grnDetail.getRecdPrice().multiply(currRate).divide(convUom))
                        .multiply(grnDetail.getRecdQty().multiply(convUom));
                Double grnValues = Double.valueOf(df.format(grnValue));
                Double stockValues = Double.valueOf(df.format(stockValue));
                Double grnVar = grnValues - stockValues;
                finalGrnVar = BigDecimal.valueOf(grnVar);
            } else {
                grnValue = grnDetail.getRecdQty().multiply(grnDetail.getRecdPrice().multiply(currRate));
                stockValue = (grnDetail.getRecdPrice().multiply(currRate).divide(grnDetail.getStdPackQty()))
                        .multiply(grnDetail.getRecdQty().multiply(grnDetail.getStdPackQty()));
                Double grnValues = Double.valueOf(df.format(grnValue));
                Double stockValues = Double.valueOf(df.format(stockValue));
                Double grnVar = grnValues - stockValues;
                finalGrnVar = BigDecimal.valueOf(grnVar);
            }

            NLCTLProjection batchYear = nlctlRepository.getBatchYear(userProfile.getCompanyCode(), userProfile.getPlantNo());
            BigDecimal newBatchNo = BigDecimal.ZERO;
            if (itemCur.getBatchNo() == null) {
                newBatchNo = (batchYear.getBatchNo().multiply(BigDecimal.valueOf(10000))).add(BigDecimal.valueOf(1));
            } else {
                String batchYr = itemCur.getBatchNo().toString().substring(0, 4);
                String batchNo = itemCur.getBatchNo().toString().substring(7);
                BigDecimal btchYr = BigDecimal.valueOf(Double.parseDouble(batchYr));
                BigDecimal btchNo = BigDecimal.valueOf(Double.parseDouble(batchNo));

                if (BigDecimal.valueOf(Double.parseDouble(batchYr)).intValue() < batchYear.getBatchNo().intValue()) {
                    btchYr = batchYear.getBatchNo();
                    btchNo = BigDecimal.ZERO;
                }

                newBatchNo = (btchYr.multiply(BigDecimal.valueOf(10000))).add(btchNo);

                if (newBatchNo == null) {
                    throw new NotFoundException("New Batch No not found!");
                }
            }

            BigDecimal itemValue = (itemCur.getQoh().multiply(itemCur.getStdMaterial())).add(itemCur.getCostVariance()).add(convQty.multiply(convCost));
            BigDecimal newStdMat = BigDecimal.ZERO;
            if (itemCur.getQoh().compareTo(BigDecimal.ZERO) <= 0) {
                newStdMat = (itemValue.subtract(itemCur.getCostVariance())).divide(convQty.add(itemCur.getQoh()));
            } else {
                newStdMat = itemValue.divide(convQty.add(itemCur.getQoh()));
            }

            BigDecimal newQoh = itemCur.getQoh().add(convQty);
            BigDecimal itemOrderQty = itemCur.getOrderQty().subtract(convQty);
            BigDecimal newCostVar = (itemValue.subtract(newQoh.multiply(newStdMat))).add(grnValue.subtract(stockValue));
            costVar = (itemValue.subtract(newQoh.multiply(newStdMat))).subtract(itemCur.getCostVariance());

            Date lastTranDate = new Date(System.currentTimeMillis());

            BigDecimal ytdReceipt = (itemLocInfo.getYtdReceipt() == null ? BigDecimal.ZERO : itemLocInfo.getYtdReceipt()).add(convQty);
            BigDecimal qoh = (itemLocInfo.getQoh() == null ? BigDecimal.ZERO : itemLocInfo.getQoh()).add(convQty);

            itemRepository.updateDataItems(newQoh, itemOrderQty, newStdMat, newCostVar, ytdReceipt, new Timestamp(System.currentTimeMillis()), convCost, newBatchNo,
                    userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDetail.getItemNo());

            ItemLoc itemLoc = new ItemLoc();
            if (itemLc.getRecCnt() == null) {
                if (stkLoc.getStockLoc().equals(grnDetail.getLoc())) {
                    itemLoc.setCompanyCode(userProfile.getCompanyCode());
                    itemLoc.setPlantNo(userProfile.getPlantNo());
                    itemLoc.setItemNo(grnDetail.getItemNo());
                    itemLoc.setLoc(grnDetail.getLoc());
                    itemLoc.setPartNo(grnDetail.getPartNo());
                    itemLoc.setDescription(grnDetail.getRemarks());
                    itemLoc.setCategoryCode(grnDetail.getItemNo().substring(0, 3));
                    itemLoc.setQoh(convQty);
                    itemLoc.setBalbfQty(convQty);
                    itemLoc.setYtdReceipt(convQty);
                    itemLoc.setStdMaterial(newStdMat);
                    itemLoc.setCostVariance(newCostVar);
                    itemLoc.setBatchNo(newBatchNo);
                    itemLoc.setLastTranDate(lastTranDate);
                    itemLoc.setStatus(Status.ACTIVE);
                    itemLoc.setCreatedBy(userProfile.getUsername());
                    itemLoc.setCreatedAt(ZonedDateTime.now());
                    itemLoc.setUpdatedBy(userProfile.getUsername());
                    itemLoc.setUpdatedAt(ZonedDateTime.now());
                } else {
                    itemLoc.setCompanyCode(userProfile.getCompanyCode());
                    itemLoc.setPlantNo(userProfile.getPlantNo());
                    itemLoc.setItemNo(grnDetail.getItemNo());
                    itemLoc.setLoc(stkLoc.getStockLoc());
                    itemLoc.setPartNo(grnDetail.getPartNo());
                    itemLoc.setDescription(grnDetail.getRemarks());
                    itemLoc.setCategoryCode(grnDetail.getItemNo().substring(0, 3));
                    itemLoc.setQoh(BigDecimal.ZERO);
                    itemLoc.setBalbfQty(convQty);
                    itemLoc.setYtdReceipt(convQty);
                    itemLoc.setStdMaterial(newStdMat);
                    itemLoc.setCostVariance(BigDecimal.ZERO);
                    itemLoc.setBatchNo(newBatchNo);
                    itemLoc.setLastTranDate(lastTranDate);
                    itemLoc.setStatus(Status.ACTIVE);
                    itemLoc.setCreatedBy(userProfile.getUsername());
                    itemLoc.setCreatedAt(ZonedDateTime.now());
                    itemLoc.setUpdatedBy(userProfile.getUsername());
                    itemLoc.setUpdatedAt(ZonedDateTime.now());
                }
                ItemLoc saved = itemLocRepository.save(itemLoc);
                itemLoc.setId(saved.getId());
                itemLoc.setVersion(saved.getVersion());
            } else if (itemLc.getRecCnt() > 0) {
                itemLocRepository.updateQohVarianceStdMatYtdRecBatchNoLTranDateLPurPrice(convQty, newCostVar, newStdMat,
                        ytdReceipt, newBatchNo, lastTranDate, convCost, userProfile.getCompanyCode(),
                        userProfile.getPlantNo(), grnDetail.getItemNo(), stkLoc.getStockLoc());

                if (StringUtils.isBlank(stkLoc.getStockLoc())) {
                    throw new NotFoundException("Company Stock Loc not found!");
                }
                itemLocRepository.updateStdMatYtdRecBatchNoLTranDateLPurPrice(newStdMat,
                        ytdReceipt, newBatchNo, lastTranDate, convCost, userProfile.getCompanyCode(),
                        userProfile.getPlantNo(), grnDetail.getItemNo(), stkLoc.getStockLoc());

                if (StringUtils.isBlank(itemLc.getLoc())) {
                    itemLoc.setCompanyCode(userProfile.getCompanyCode());
                    itemLoc.setPlantNo(userProfile.getPlantNo());
                    itemLoc.setItemNo(grnDetail.getItemNo());
                    itemLoc.setLoc(grnDetail.getLoc());
                    itemLoc.setPartNo(grnDetail.getPartNo());
                    itemLoc.setDescription(grnDetail.getRemarks());
                    itemLoc.setCategoryCode(grnDetail.getItemNo().substring(0, 3));
                    itemLoc.setQoh(convQty);
                    itemLoc.setBalbfQty(convQty);
                    itemLoc.setYtdReceipt(convQty);
                    itemLoc.setStdMaterial(newStdMat);
                    itemLoc.setCostVariance(BigDecimal.ZERO);
                    itemLoc.setBatchNo(newBatchNo);
                    itemLoc.setLastTranDate(lastTranDate);
                    itemLoc.setStatus(Status.ACTIVE);
                    itemLoc.setCreatedBy(userProfile.getUsername());
                    itemLoc.setCreatedAt(ZonedDateTime.now());
                    itemLoc.setUpdatedBy(userProfile.getUsername());
                    itemLoc.setUpdatedAt(ZonedDateTime.now());
                    ItemLoc saved = itemLocRepository.save(itemLoc);
                    itemLoc.setId(saved.getId());
                    itemLoc.setVersion(saved.getVersion());
                } else {
                    itemLocRepository.updateQoh(qoh, userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDetail.getItemNo());
                }
            }

            ItemBatc itemBatc = new ItemBatc();
            ItemBatcId itemBatcId = new ItemBatcId();
            itemBatcId.setCompanyCode(userProfile.getCompanyCode());
            itemBatcId.setPlantNo(userProfile.getPlantNo());
            itemBatcId.setItemNo(grnDetail.getItemNo());
            itemBatcId.setLoc(grnDetail.getLoc());
            itemBatcId.setBatchNo(newBatchNo.longValue());
            itemBatc.setTranDate(lastTranDate);
            itemBatc.setQoh(convQty);
            itemBatc.setOriQoh(convQty);
            itemBatc.setStdMaterial(newStdMat);
            itemBatc.setPoNo(input.getPoNo());
            itemBatc.setPoRecSeq(grnDetail.getPoRecSeq());
            itemBatc.setGrnNo(input.getGrnNo());
            itemBatc.setGrnSeq(grnDetail.getSeqNo());
            itemBatc.setDateCode(detail.getDateCode());
            itemBatc.setId(itemBatcId);
            itemBatcRepository.save(itemBatc);

            // update item loc
            List<ItemLoc> itemLocs = itemLocRepository.findByCompanyCodeAndPlantNoAndItemNoAndLoc(
                    userProfile.getCompanyCode(), userProfile.getPlantNo(), detail.getItemNo(), detail.getLoc());

            if (!CollectionUtils.isEmpty(itemLocs) && itemLocs.size() == 1) {
                itemLoc = itemLocRepository.getById(itemLocs.get(0).getId());
            }

            InAudit inAudit = new InAudit();
            inAudit.setCompanyCode(userProfile.getCompanyCode());
            inAudit.setPlantNo(userProfile.getPlantNo());
            inAudit.setItemNo(grnDetail.getItemNo());
            inAudit.setLoc(grnDetail.getLoc());
            inAudit.setTranDate(lastTranDate);
            String tranTime = FastDateFormat.getInstance("kkmmss").format(System.currentTimeMillis());
            inAudit.setTranTime(tranTime);
            inAudit.setItemlocId(itemLoc.getId());
            inAudit.setTranType("RU");
            inAudit.setDocmNo(input.getGrnNo());
            inAudit.setDocmDate(grnDetail.getRecdDate());
            inAudit.setUom(itemUom.getUom());
            inAudit.setProjectNo(grnDetail.getProjectNo());
            inAudit.setPoNo(input.getPoNo());
            inAudit.setDoNo(inAudit.getDoNo());
            inAudit.setGrnNo(inAudit.getGrnNo());
            inAudit.setSeqNo(grnDetail.getSeqNo());
            inAudit.setCurrencyCode(input.getCurrencyCode());
            inAudit.setCurrencyRate(currRate);
            inAudit.setInQty(convQty);
            inAudit.setOrderQty(convQty);
            inAudit.setBalQty(newQoh);
            inAudit.setActualCost(convCost);
            inAudit.setNewStdMaterial(newStdMat);
            inAudit.setOriStdMaterial(itemCur.getStdMaterial());
            inAudit.setCostVariance(costVar);
            inAudit.setGrnVariance(finalGrnVar);
            inAudit.setStatus(Status.ACTIVE);
            inAudit.setCreatedBy(userProfile.getUsername());
            inAudit.setCreatedAt(ZonedDateTime.now());
            inAudit.setUpdatedBy(userProfile.getUsername());
            inAudit.setUpdatedAt(ZonedDateTime.now());
            InAudit saved = inAuditRepository.save(inAudit);
            inAudit.setId(saved.getId());
            inAudit.setVersion(saved.getVersion());

            /***************************** Update BOM Buylist Table *****************************/
            procBomUpdate(userProfile, grnDetail, stkLoc.getStockLoc(), convQty);

            Date rlseDate = new Date(System.currentTimeMillis());
            Date recdDate = new Date(System.currentTimeMillis());

            /**PURDET UPDATE*/
            PurDetProjection purDetInfo = purDetRepository.purDetInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getPoNo(), grnDetail.getSeqNo());
            BigDecimal rlseQty = (purDetInfo.getRlseQty() == null ? BigDecimal.ZERO : purDetInfo.getRlseQty()).add(grnDetail.getRecdQty());
            BigDecimal recdQty = (purDetInfo.getRecdQty() == null ? BigDecimal.ZERO : purDetInfo.getRecdQty()).add(grnDetail.getRecdQty());

            DraftPurDetProjection dPurDetInfo = draftPurDetRepository.draftPurDetInfo(userProfile.getCompanyCode(),
                    userProfile.getPlantNo(), grnDetail.getPoNo(), grnDetail.getSeqNo());

            BigDecimal rlseQtyDP = (dPurDetInfo.getRlseQty() == null ? BigDecimal.ZERO : dPurDetInfo.getRlseQty()).add(grnDetail.getRecdQty());
            BigDecimal recdQtyDP = (dPurDetInfo.getRecdQty() == null ? BigDecimal.ZERO : dPurDetInfo.getRecdQty()).add(grnDetail.getRecdQty());

            draftPurDetRepository.updateRlseRecdDateRlseRecdQtyRecdPrice(rlseDate, recdDate, rlseQtyDP, recdQtyDP,
                    grnDetail.getPoPrice(), userProfile.getCompanyCode(), userProfile.getPlantNo(),
                    grnDetail.getPoNo(), grnDetail.getSeqNo());
            purDetRepository.updateRlseRecdDateRlseRecdQtyRecdPrice(rlseDate, recdDate, rlseQty, recdQty,
                    grnDetail.getPoPrice(), userProfile.getCompanyCode(), userProfile.getPlantNo(),
                    grnDetail.getPoNo(), grnDetail.getPoRecSeq());
        }
    }

    private void procBomUpdate(UserProfile userProfile, GrnDet grnDetail, String stockLoc, BigDecimal convQty) {
        List<BombypjDetailProjection> bombypjDetCurs = bombypjDetailRepository.getBombypjDetCur(userProfile.getCompanyCode(),
                userProfile.getPlantNo(), grnDetail.getPoNo(), grnDetail.getItemNo(), grnDetail.getPoRecSeq());
        ItemLocProjection itemLocInfo = itemLocRepository.itemLocInfo(userProfile.getCompanyCode(),
                userProfile.getPlantNo(), grnDetail.getItemNo(), grnDetail.getLoc());
        PurDetProjection purDetInfo = purDetRepository.purDetInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(),
                grnDetail.getPoNo(), grnDetail.getSeqNo());
        ItemProjection itemInfo = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDetail.getItemNo());

        boolean isSuccess = false;
        BigDecimal recdQty = convQty;
        BigDecimal itemPickQty = BigDecimal.ZERO;
        BigDecimal itemProdnResv = BigDecimal.ZERO;
        BigDecimal poResvQty = BigDecimal.ZERO;

        for (BombypjDetailProjection bombypjDetCur : bombypjDetCurs) {
            List<BombypjProjection> bombypjCurs = bombypjRepository.getBombypjCur(userProfile.getCompanyCode(), userProfile.getPlantNo(),
                    bombypjDetCur.getTranType(), bombypjDetCur.getProjectNo(), bombypjDetCur.getOrderNo(),
                    bombypjDetCur.getAssemblyNo(), grnDetail.getItemNo());

            BigDecimal orgDetRecdQty = bombypjDetCur.getRecdQty();
            BigDecimal accumRecdQty = bombypjDetCur.getAccumRecdQty();
            BigDecimal detRecdQty = bombypjDetCur.getRecdQty();
            BigDecimal detBalQty = bombypjDetCur.getBalQty();

            /***************************************** For Print Picked List *****************************************/
            String projectNoPickList = bombypjDetCur.getProjectNo();
            String orderNoPickList = bombypjDetCur.getOrderNo();

            /***************************************** UPDATE BOMBYPJ *****************************************/

            for (BombypjProjection bombypjCur : bombypjCurs) {
                BigDecimal inTransitQty = bombypjCur.getInTransitQty();
                BigDecimal delvQty = bombypjCur.getDelvQty();
                BigDecimal pickedQty = bombypjCur.getPickedQty();
                BigDecimal shortQty = bombypjCur.getShortQty();

                if (bombypjDetCur.getBalQty().compareTo(BigDecimal.ZERO) < bombypjDetCur.getRecdQty().compareTo(BigDecimal.ZERO)) {
                    inTransitQty = bombypjCur.getInTransitQty().subtract(bombypjDetCur.getBalQty());
                    delvQty = bombypjCur.getDelvQty().add(bombypjDetCur.getBalQty());
                    if (stockLoc.equals(grnDetail.getLoc())) {
                        pickedQty = bombypjCur.getPickedQty().add(bombypjDetCur.getBalQty());
                        itemPickQty = itemPickQty.add(bombypjDetCur.getBalQty());
                    } else {
                        shortQty = bombypjCur.getShortQty().add(bombypjDetCur.getBalQty());
                    }

                    recdQty = recdQty.subtract(bombypjDetCur.getBalQty());
                    accumRecdQty = bombypjDetCur.getAccumRecdQty().add(bombypjDetCur.getBalQty());
                    detRecdQty = bombypjDetCur.getBalQty();
                    poResvQty = poResvQty.add(bombypjDetCur.getBalQty());
                    detBalQty = BigDecimal.ZERO;
                } else {
                    inTransitQty = bombypjCur.getInTransitQty().subtract(bombypjDetCur.getRecdQty());
                    delvQty = bombypjCur.getDelvQty().add(bombypjDetCur.getRecdQty());
                    if (stockLoc.equals(grnDetail.getLoc())) {
                        pickedQty = bombypjCur.getPickedQty().add(bombypjDetCur.getRecdQty());
                        itemPickQty = itemPickQty.add(bombypjDetCur.getRecdQty());
                    } else {
                        shortQty = bombypjCur.getShortQty().add(bombypjDetCur.getRecdQty());
                    }

                    detBalQty = bombypjDetCur.getBalQty().subtract(bombypjDetCur.getRecdQty());
                    accumRecdQty = accumRecdQty.add(recdQty);
                    detRecdQty = recdQty;
                    poResvQty = poResvQty.add(recdQty);
                    recdQty = BigDecimal.ZERO;
                }

                /************************** for item resv thru SRP **************************/
                if (inTransitQty.compareTo(BigDecimal.ZERO) < 0) {
                    inTransitQty = BigDecimal.ZERO;
                }

                ZonedDateTime now = ZonedDateTime.now();
                Date delvDate = new Date(now.toLocalDate().toEpochDay());

                bombypjRepository.updateShortInTransitDelvPickedQtyDelvDate(shortQty, inTransitQty,
                        delvQty, pickedQty, delvDate, userProfile.getCompanyCode(), userProfile.getPlantNo(),
                        grnDetail.getProjectNo(), grnDetail.getItemNo());

                isSuccess = true;
            }

            /*********************************** update/delete the BOMBYPJ_DET ***********************************/

            if (!isSuccess) {
                // fail to update bombypj
                if (bombypjDetCur.getProjectNo().equals(bombypjDetCur.getAssemblyNo())) {
                    // Adv PO
                    if (detBalQty.compareTo(BigDecimal.ZERO) < recdQty.compareTo(BigDecimal.ZERO)) {
                        accumRecdQty = accumRecdQty.add(detBalQty);
                        poResvQty = poResvQty.add(detBalQty);
                        detRecdQty = detBalQty;
                        itemProdnResv = detBalQty;
                        recdQty = recdQty.subtract(detBalQty);
                        detBalQty = BigDecimal.ZERO;
                    } else { // detBalQty >= recdQty
                        detBalQty = detBalQty.subtract(recdQty);
                        accumRecdQty = accumRecdQty.add(recdQty);
                        detRecdQty = recdQty;
                        itemProdnResv = recdQty;
                        poResvQty = poResvQty.add(recdQty);
                        recdQty = BigDecimal.ZERO;
                    }
                    throw new ServerException("" + bombypjDetCur.getProjectNo() + " has not being reserved! Qty "
                            + itemProdnResv + " of " + grnDetail.getItemNo() + " will be open for reservation.");
                } else {
                    throw new NotFoundException("No record found for " + grnDetail.getItemNo() + " of " + bombypjDetCur.getProjectNo() +
                            " Qty " + itemProdnResv + " of " + grnDetail.getItemNo() + " will be open for reservation.");
                }
            }

            String status = null;
            if (bombypjDetCur.getResvQty().compareTo(BigDecimal.ZERO) == accumRecdQty.compareTo(BigDecimal.ZERO)) {
                status = "C";
            }

            /**accumulate BOMBYPJ_DET recdQty when under same GRN No**/
            if (bombypjDetCur.getGrnNo() != null) {
                if (bombypjDetCur.getGrnNo().equals(grnDetail.getGrnNo())) {
                    detRecdQty = detRecdQty.add(orgDetRecdQty);
                }
            }

            bombypjDetailRepository.updateAccRecdStatusGrnNo(accumRecdQty, detRecdQty, status, grnDetail.getGrnNo(),
                    userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDetail.getProjectNo(), grnDetail.getPoNo(), grnDetail.getItemNo());
        }

        BigDecimal pickedQtyUpdate = (itemLocInfo.getPickedQty() == null ? BigDecimal.ZERO : itemLocInfo.getPickedQty()).subtract(itemPickQty);
        BigDecimal prodnResvUpdate = (itemLocInfo.getProdnResv() == null ? BigDecimal.ZERO : itemLocInfo.getProdnResv()).subtract(itemProdnResv);
        BigDecimal resvQty = (purDetInfo.getResvQty() == null ? BigDecimal.ZERO : purDetInfo.getResvQty()).subtract(poResvQty);

        DraftPurDetProjection dPurDetInfo = draftPurDetRepository.draftPurDetInfo(userProfile.getCompanyCode(),
                userProfile.getPlantNo(), grnDetail.getPoNo(), grnDetail.getSeqNo());

        BigDecimal resvQtyDP = (dPurDetInfo.getResvQty() == null ? BigDecimal.ZERO : dPurDetInfo.getResvQty()).subtract(poResvQty);
        BigDecimal pickedQtyItem = (itemInfo.getPickedQty() == null ? BigDecimal.ZERO : itemInfo.getPickedQty()).add(itemPickQty);
        BigDecimal prodnResvItem = (itemInfo.getProdnResv() == null ? BigDecimal.ZERO : itemInfo.getProdnResv()).subtract(itemProdnResv);

        /**update PO ResvQty**/
        purDetRepository.updateResvQty(resvQty, userProfile.getCompanyCode(), userProfile.getPlantNo(),
                grnDetail.getPoNo(), grnDetail.getItemNo(), grnDetail.getSeqNo());
        draftPurDetRepository.updateResvQty(resvQtyDP, userProfile.getCompanyCode(), userProfile.getPlantNo(),
                grnDetail.getPoNo(), grnDetail.getItemNo(), grnDetail.getSeqNo());

        /**update ITEM & ITEMLOC PICKEDQTY**/
        itemRepository.updatePickedQtyProdnResv(pickedQtyItem, prodnResvItem, userProfile.getCompanyCode(),
                userProfile.getPlantNo(), grnDetail.getItemNo());
        itemLocRepository.updatePickedQtyProdnResv(pickedQtyUpdate, prodnResvUpdate, userProfile.getCompanyCode(),
                userProfile.getPlantNo(), grnDetail.getItemNo(), grnDetail.getLoc());
    }

    private void closePO(UserProfile userProfile, Grn input) throws ParseException {
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

    private void preSavingGrn(GrnDTO input) {

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

    private void grnPostSaving(UserProfile userProfile, Grn input) {

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
