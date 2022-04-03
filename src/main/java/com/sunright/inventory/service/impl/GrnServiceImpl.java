package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.ItemDTO;
import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.bombypj.BombypjDTO;
import com.sunright.inventory.dto.company.CompanyDTO;
import com.sunright.inventory.dto.docmno.DocmNoDTO;
import com.sunright.inventory.dto.grn.GrnDTO;
import com.sunright.inventory.dto.grn.GrnDetDTO;
import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.msr.MsrDetailDTO;
import com.sunright.inventory.dto.pur.PurDTO;
import com.sunright.inventory.dto.pur.PurDetDTO;
import com.sunright.inventory.dto.search.Filter;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.dto.supplier.SupplierDTO;
import com.sunright.inventory.entity.bombypj.BombypjDetailProjection;
import com.sunright.inventory.entity.bombypj.BombypjProjection;
import com.sunright.inventory.entity.docmno.DocmNoProjection;
import com.sunright.inventory.entity.draftpur.DraftPurDetProjection;
import com.sunright.inventory.entity.draftpur.DraftPurProjection;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.grn.Grn;
import com.sunright.inventory.entity.grn.GrnDet;
import com.sunright.inventory.entity.grn.GrnDetailProjection;
import com.sunright.inventory.entity.grn.GrnSupplierProjection;
import com.sunright.inventory.entity.inaudit.InAudit;
import com.sunright.inventory.entity.item.ItemProjection;
import com.sunright.inventory.entity.itembatc.ItemBatc;
import com.sunright.inventory.entity.itembatc.ItemBatcId;
import com.sunright.inventory.entity.itemloc.ItemLoc;
import com.sunright.inventory.entity.itemloc.ItemLocProjection;
import com.sunright.inventory.entity.mrv.MRVDetailProjection;
import com.sunright.inventory.entity.msr.MSR;
import com.sunright.inventory.entity.msr.MSRDetail;
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
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;

@Transactional
@Service
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
    private MRVDetailRepository mrvDetailRepository;

    @Autowired
    private CompanyServiceImpl companyService;

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
            if (supName != null) {
                grnDTO.setSupplierName(supName.getName());
            } else {
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
        if (StringUtils.equals(statusPoNo2.getOpenClose(), "C")) {
            throw new ServerException("PO already Closed, Purchase Receipt not allowed.");
        } else if (!StringUtils.equals(statusPoNo2.getOpenClose(), "A")) {
            throw new ServerException("PO is yet to be Approved, Purchase Receipt not allowed.");
        } else if (StringUtils.equals(statusPoNo2.getOpenClose(), "V")) {
            throw new ServerException("PO already Voided, Purchase Receipt not allowed.");
        } else if (StringUtils.isBlank(statusPoNo2.getPoNo())) {
            throw new ServerException("Invalid PO No!");
        } else {
            if (!StringUtils.equals(statusPoNo2.getPoNo(), null) && !StringUtils.equals(statusPoNo2.getPoNo(), null)) {
                if (StringUtils.equals(statusPoNo.getOpenClose(), "C")) {
                    throw new ServerException("PO already Closed, Purchase Receipt not allowed.");
                } else if (!StringUtils.equals(statusPoNo.getOpenClose(), "A")) {
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
            try {
                dto.setGrnNo(docmNo.getGeneratedNo());
                dto.setSubType("M");
                dto.setStatuz("Y");
                dto.setRecdDate(sdf.parse(date));
                dto.setCurrencyRate(new BigDecimal(1));
                dto.setCurrencyCode("SGD");
                dto.setEntryUser(userProfile.getUsername());
                dto.setEntryDate(sdf.parse(date));
            } catch (ParseException e) {
                throw new ServerException(e.getMessage());
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
        UserProfile userProfile = UserProfileContext.getUserProfile();
        MSRDetailProjection found = msrRepository.findMsrNoByMsrNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), msrNo);
        if (found == null) {
            throw new ServerException("Invalid MSR No");
        } else {
            dto.setMsrNo(found.getMsrNo());
        }
        return dto;
    }

    @Override
    public GrnDetDTO checkNextItem(GrnDTO input) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        GrnDetDTO dto = GrnDetDTO.builder().build();

        if (!CollectionUtils.isEmpty(input.getGrnDetails())) {
            for (GrnDetDTO detail : input.getGrnDetails()) {
                String msrNo = input.getMsrNo();
                String poNo = input.getPoNo();
                String itemNo = detail.getItemNo();
                String partNo = detail.getPartNo();
                Integer poRecSeq = detail.getPoRecSeq();
                BigDecimal orderQty = detail.getOrderQty();
                Integer itemType = detail.getItemType();
                String projectNo = detail.getProjectNo();
                BigDecimal recdPrice = detail.getRecdPrice();
                BigDecimal recdQty = detail.getRecdQty();
                //BigDecimal retnQty = detail.getRetnQty();
                BigDecimal labelQty = detail.getLabelQty();
                BigDecimal poPrice = detail.getPoPrice();
                Integer dateCode = detail.getDateCode();
                if (detail.getItemType() == null) {
                    checkItemType();
                } else {
                    if (detail.getItemType() == 0) {
                        if (StringUtils.equals(input.getSubType(), "N")) {
                            /*if (input.getGrnDetails().size() > 1) {
                                if (StringUtils.isNotBlank(partNo)) {
                                    checkDuplicatePartNo(input);
                                }
                            }*/
                            if (StringUtils.isNotBlank(partNo)) {
                                checkDataFromPartNo(poNo, partNo, userProfile);
                            }
                            if (StringUtils.isNotBlank(itemNo)) {
                                checkDataFromItemNo(poNo, itemNo, userProfile);
                            }
                            if (StringUtils.isNotBlank(partNo)) {
                                if ((recdQty != null ? recdQty.compareTo(BigDecimal.ZERO) : 0) == 0) {
                                    throw new ServerException("Received Qty cannot be empty or zero!");
                                }
                                if (orderQty != null) {
                                    if (recdQty.compareTo(BigDecimal.ZERO) > 0 && recdQty.compareTo(orderQty) > 0) {
                                        throw new ServerException("Received more than Ordered is not allowed!");
                                    }
                                    BigDecimal recQty = recdQty;
                                    boolean isSuccess = true;
                                    if (!poRecSeq.equals(poRecSeq)) {
                                        recQty = recQty.add(recdQty);
                                        if (recQty.compareTo(orderQty) > 0) {
                                            isSuccess = false;
                                        }
                                    }

                                    if (!isSuccess) {
                                        throw new ServerException("Receiving more than Ordered is not allowed!");
                                    }
                                }

                                if ((labelQty != null ? labelQty.compareTo(BigDecimal.ZERO) : 0) == 0) {
                                    throw new ServerException("Qty per label cannot be empty or zero");
                                } else if (labelQty.compareTo(BigDecimal.ZERO) == 0) {
                                    throw new ServerException("Qty per label cannot be empty or zero");
                                } else if (labelQty.compareTo(BigDecimal.ZERO) > 0 && labelQty.compareTo(recdQty) > 0) {
                                    throw new ServerException("Qty per label is more than Received Qty !");
                                }
                            }

                            if (recdQty != null) {
                                if (recdQty.compareTo(BigDecimal.ZERO) <= 0) {
                                    checkRecdQty();
                                }
                            }

                            if (labelQty != null) {
                                if (labelQty.compareTo(BigDecimal.ZERO) <= 0) {
                                    checkLabelQty();
                                } else if (recdQty.compareTo(labelQty) < 0) {
                                    checkRecdLabelQty(recdQty);
                                }
                            }

                            if (dateCode != null) {
                                if (dateCode != 0) {
                                    Integer lengthOfDateCode = String.valueOf(dateCode).length();
                                    if (lengthOfDateCode != 4) {
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

                            if (poPrice == null || poPrice.equals(BigDecimal.ZERO)) {
                                throw new ServerException("PO Price cannot be empty or zero !");
                            } else if (recdPrice == null || recdPrice.equals(BigDecimal.ZERO)) {
                                throw new ServerException("Receiving Price cannot be empty or zero !");
                            } else {
                                if (!poPrice.equals(recdPrice)) {
                                    throw new ServerException("Unit price is not equal to receiving price !");
                                }
                            }
                        } else {
                            if (StringUtils.equals(input.getSubType(), "M")) {
                                if (!Integer.toString(itemType).contains("0") && !Integer.toString(itemType).contains("1")) {
                                    checkItemTypeNotNull();
                                }
                                if (StringUtils.isBlank(itemNo)) {
                                    checkItemNo();
                                }
                                if (StringUtils.isNotBlank(msrNo)) {
                                    checkIfMsrNoValid(input.getMsrNo());
                                    if (StringUtils.isNotBlank(itemNo)) {
                                        MSRDetailProjection countByItemNo = msrDetailRepository.getCountMsrByItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), msrNo, itemNo);
                                        if (countByItemNo.getCountItemNo() == 0) {
                                            checkMsrItemNo();
                                        } else if (countByItemNo.getCountItemNo() > 1) {
                                            List<MSRDetailProjection> lovPartNo = msrDetailRepository.showLovPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), msrNo, partNo, itemNo);
                                            for (MSRDetailProjection rec : lovPartNo) {
                                                MSRDetailProjection info = msrDetailRepository.itemInfo(userProfile.getCompanyCode(),
                                                        userProfile.getPlantNo(), msrNo, rec.getPartNo(), rec.getItemNo(), rec.getSeqNo());

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
                                                dto.setRecdPrice(info.getRetnPrice());
                                            }

                                        } else {
                                            MSRDetailProjection detailProjection = msrDetailRepository.itemInfo(userProfile.getCompanyCode(),
                                                    userProfile.getPlantNo(), msrNo, detail.getPartNo(), detail.getItemNo(), detail.getSeqNo());

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
                                            dto.setRecdPrice(detailProjection.getRetnPrice());
                                            GrnDetailProjection found = grnRepository.findPoNoByGrnNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getGrnNo());
                                            if (found != null) {
                                                dto.setPoNo(found.getPoNo());
                                            }
                                        }
                                    }

                                    if (StringUtils.isNotBlank(dto.getPartNo())) {
                                        MSRDetailProjection countByPartNo = msrDetailRepository.getCountMsrByPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), msrNo, dto.getPartNo());
                                        if (countByPartNo.getCountPartNo() == 0) {
                                            checkMsrPartNo();
                                        } else if (countByPartNo.getCountPartNo() > 1) {
                                            List<MSRDetailProjection> lovPartNo = msrDetailRepository.showLovPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), msrNo, dto.getPartNo(), itemNo);
                                            for (MSRDetailProjection rec : lovPartNo) {
                                                MSRDetailProjection info = msrDetailRepository.itemInfo(userProfile.getCompanyCode(),
                                                        userProfile.getPlantNo(), msrNo, rec.getPartNo(), rec.getItemNo(), rec.getSeqNo());

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
                                        } else {
                                            MSRDetailProjection detailProjection = msrDetailRepository.itemInfo(userProfile.getCompanyCode(),
                                                    userProfile.getPlantNo(), msrNo, dto.getPartNo(), dto.getItemNo(), detail.getSeqNo());

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
                                            GrnDetailProjection found = grnRepository.findPoNoByGrnNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getGrnNo());
                                            if (found != null) {
                                                dto.setPoNo(found.getPoNo());
                                            }
                                        }
                                    }
                                } else {
                                    if (StringUtils.isNotBlank(itemNo)) {
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
                                            ItemProjection getItemInfo = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getItemNo());
                                            dto.setPartNo(getItemInfo.getPartNo());
                                            dto.setItemNo(getItemInfo.getItemNo());
                                            dto.setDescription(getItemInfo.getDescription());
                                            dto.setLoc(getItemInfo.getLoc());
                                            dto.setUom(getItemInfo.getUom());
                                        }
                                    }

                                    if (StringUtils.isNotBlank(partNo)) {
                                        ItemProjection countPartNo = itemRepository.getCountByPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), detail.getPartNo());
                                        if (countPartNo.getCountPartNo() == 0) {
                                            checkValidPartNo();
                                        } else if (countPartNo.getCountPartNo() > 1) {
                                            List<ItemProjection> lovItemPart = itemRepository.lovItemPart(userProfile.getCompanyCode(), userProfile.getPlantNo(), detail.getPartNo(), dto.getItemNo());
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
                                                    userProfile.getPlantNo(), detail.getPartNo(), detail.getItemNo());
                                            for (ItemProjection data : byPartNo) {
                                                dto.setItemNo(data.getItemNo());
                                                dto.setPartNo(data.getPartNo());
                                            }
                                            ItemProjection getItemInfo = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getItemNo());
                                            dto.setPartNo(getItemInfo.getPartNo());
                                            dto.setItemNo(getItemInfo.getItemNo());
                                            dto.setDescription(getItemInfo.getDescription());
                                            dto.setLoc(getItemInfo.getLoc());
                                            dto.setUom(getItemInfo.getUom());
                                        }
                                    }
                                }

                                if (StringUtils.isNotBlank(projectNo)) {
                                    BombypjProjection prjNo = bombypjRepository.getPrjNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), projectNo);
                                    if (prjNo == null) {
                                        checkProjectNoIfNull(projectNo);
                                    } else if (itemType == 0) {
                                        BombypjProjection altnt = bombypjRepository.getAltrnt(userProfile.getCompanyCode(), userProfile.getPlantNo(), prjNo.getProjectNo(), itemNo);
                                        if (altnt == null) {
                                            checkItemNoInProject(itemNo, prjNo.getProjectNo());
                                        }
                                    }
                                }

                                if (StringUtils.isNotBlank(detail.getPoNo())) {
                                    PurDetProjection poNoRecSeq = purDetRepository.getPoNoAndRecSeq(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemType, detail.getItemNo(), detail.getPartNo(), detail.getPoNo());
                                    if (poNoRecSeq == null) {
                                        checkValidPoNo(detail.getPoNo());
                                    } else if (poNoRecSeq.getRecSeq() == null) {
                                        checkItemNotInPo(detail.getItemNo());
                                    }
                                }

                                if (recdPrice != null) {
                                    if (recdPrice.compareTo(BigDecimal.ZERO) < 0) {
                                        checkValidRecdPrice();
                                    }
                                }

                                if (recdQty != null) {
                                    if (recdQty.compareTo(BigDecimal.ZERO) <= 0) {
                                        checkRecdQty();
                                    }
                                }

                                if (labelQty != null) {
                                    if (labelQty.compareTo(BigDecimal.ZERO) <= 0) {
                                        checkLabelQty();
                                    } else if (recdQty.compareTo(labelQty) < 0) {
                                        checkRecdLabelQty(recdQty);
                                    }
                                }

                                if (dateCode != null) {
                                    if (dateCode != 0) {
                                        Integer lengthOfDateCode = String.valueOf(dateCode).length();
                                        if (lengthOfDateCode != 4) {
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
                    } else {
                        if (detail.getItemType() == 1) {
                            if (StringUtils.equals(input.getSubType(), "N")) {
                                if (StringUtils.isNotBlank(partNo)) {
                                    if ((recdQty != null ? recdQty.compareTo(BigDecimal.ZERO) : 0) == 0) {
                                        throw new ServerException("Received Qty cannot be empty or zero!");
                                    }
                                    if (orderQty != null) {
                                        if (recdQty.compareTo(BigDecimal.ZERO) > 0 && recdQty.compareTo(orderQty) > 0) {
                                            throw new ServerException("Received more than Ordered is not allowed!");
                                        }
                                        BigDecimal recQty = recdQty;
                                        boolean isSuccess = true;
                                        if (!poRecSeq.equals(poRecSeq)) {
                                            recQty = recQty.add(recdQty);
                                            if (recQty.compareTo(orderQty) > 0) {
                                                isSuccess = false;
                                            }
                                        }

                                        if (!isSuccess) {
                                            throw new ServerException("Receiving more than Ordered is not allowed!");
                                        }
                                    }
                                    if (labelQty.compareTo(BigDecimal.ZERO) == 0) {
                                        throw new ServerException("Qty per label cannot be empty or zero");
                                    } else if (labelQty.compareTo(BigDecimal.ZERO) > 0 && labelQty.compareTo(recdQty) > 0) {
                                        throw new ServerException("Qty per label is more than Received Qty !");
                                    }

                                    if (recdQty != null) {
                                        if (recdQty.compareTo(BigDecimal.ZERO) <= 0) {
                                            checkRecdQty();
                                        }
                                    }

                                    if (labelQty != null) {
                                        if (labelQty.compareTo(BigDecimal.ZERO) <= 0) {
                                            checkLabelQty();
                                        } else if (recdQty.compareTo(labelQty) < 0) {
                                            checkRecdLabelQty(recdQty);
                                        }
                                    }
                                }
                            } else {
                                if (recdPrice == null) {
                                    throw new ServerException("Recd Price Can Not be Blank!");
                                } else {
                                    if (recdPrice.compareTo(BigDecimal.ZERO) < 0) {
                                        checkValidRecdPrice();
                                    }
                                }

                                if (recdQty != null) {
                                    if (recdQty.compareTo(BigDecimal.ZERO) <= 0) {
                                        checkRecdQty();
                                    }
                                }

                                if (labelQty != null) {
                                    if (labelQty.compareTo(BigDecimal.ZERO) <= 0) {
                                        checkLabelQty();
                                    } else if (recdQty.compareTo(labelQty) < 0) {
                                        checkRecdLabelQty(recdQty);
                                    }
                                }

                                if (dateCode != null) {
                                    if (dateCode != 0) {
                                        Integer lengthOfDateCode = String.valueOf(dateCode).length();
                                        if (lengthOfDateCode != 4) {
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
    public byte[] generatedReportGRN(GrnDTO input) {

        try {
            UserProfile userProfile = UserProfileContext.getUserProfile();
            // Fetching the .jrxml file from the resources folder.
            InputStream resourceSubReport;
            InputStream mainReport;
            if (StringUtils.equals(input.getSubType(), "N")) {
                resourceSubReport = this.getClass().getResourceAsStream("/reports/detail.jrxml");
                mainReport = this.getClass().getResourceAsStream("/reports/header.jrxml");
            } else {
                resourceSubReport = this.getClass().getResourceAsStream("/reports/grn_manual_header.jrxml");
                mainReport = this.getClass().getResourceAsStream("/reports/grn_manual_detail.jrxml");
            }
            // Compile the Jasper report from .jrxml to .jasper
            JasperReport jasperSubReport = JasperCompileManager.compileReport(resourceSubReport);
            JasperReport jasperMainReport = JasperCompileManager.compileReport(mainReport);
            Map<String, Object> param = new HashMap<>();
            // Adding the additional parameters to the pdf.
            param.put("GRN_NO", input.getGrnNo());
            param.put("SUB_TYPE", input.getSubType());
            param.put("COMPANY_CODE", userProfile.getCompanyCode());
            param.put("PLANT_NO", userProfile.getPlantNo());
            param.put("SUB_REPORT", jasperSubReport);
            // Fetching the inventoryuser from the data source.
            Connection source = dataSource.getConnection();
            // Filling the report with the data and additional parameters information.
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperMainReport, param, source);
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (ServerException | SQLException | JRException e) {
            throw new ServerException(e.getMessage());
        }
    }

    @Override
    public byte[] generatedPickedListGRN(GrnDTO input) {

        try {
            UserProfile userProfile = UserProfileContext.getUserProfile();
            // get projectNo and orderNo
            List<BombypjDetailProjection> projectOrderNo = bombypjDetailRepository.getProjectOrderNo(userProfile.getCompanyCode(),
                    userProfile.getPlantNo(), input.getGrnNo());
            // Fetching the .jrxml file from the resources folder.
            InputStream resourceSubReport = this.getClass().getResourceAsStream("/reports/header_pick_list.jrxml");
            InputStream resourceMainReport = this.getClass().getResourceAsStream("/reports/pick_list.jrxml");
            // Compile the Jasper report from .jrxml to .jasper
            JasperReport jasperSubReport = JasperCompileManager.compileReport(resourceSubReport);
            JasperReport jasperMainReport = JasperCompileManager.compileReport(resourceMainReport);
            Map<String, Object> param = new HashMap<>();
            List<JasperPrint> jasperPrintList = new ArrayList<JasperPrint>();
            Connection source = dataSource.getConnection();
            if (projectOrderNo.size() != 0) {
                for (BombypjDetailProjection rec : projectOrderNo) {
                    param.put("USERNAME", userProfile.getUsername());
                    param.put("COMPANY_CODE", userProfile.getCompanyCode());
                    param.put("PLANT_NO", userProfile.getPlantNo());
                    if (projectOrderNo != null) {
                        param.put("PROJECT_NO", rec.getProjectNo());
                        param.put("ORDER_NO", rec.getOrderNo());
                    } else {
                        param.put("PROJECT_NO", "");
                        param.put("ORDER_NO", "");
                    }
                    param.put("SUB_REPORT", jasperSubReport);
                    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperMainReport, param, source);
                    jasperPrintList.add(jasperPrint);
                }
            } else {
                param.put("PROJECT_NO", "");
                param.put("ORDER_NO", "");
                param.put("SUB_REPORT", jasperSubReport);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperMainReport, param, source);
                jasperPrintList.add(jasperPrint);
            }
            ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
            // Generating report using List<JasperPrint>
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(SimpleExporterInput.getInstance(jasperPrintList));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(pdfOutputStream));
            SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
            configuration.setCreatingBatchModeBookmarks(true);
            exporter.setConfiguration(configuration);
            exporter.exportReport();
            byte[] jasperReport = pdfOutputStream.toByteArray();
            return jasperReport;
        } catch (ServerException | JRException | SQLException e) {
            throw new ServerException(e.getMessage());
        }
    }

    @Override
    public byte[] generatedLabelGRN(GrnDTO input) {

        try {
            UserProfile userProfile = UserProfileContext.getUserProfile();
            InputStream resource = this.getClass().getResourceAsStream("/reports/label.jrxml");
            // Compile the Jasper report from .jrxml to .jasper
            JasperReport jasperReport = JasperCompileManager.compileReport(resource);
            Map<String, Object> param = new HashMap<>();
            param.put("GRN_NO", input.getGrnNo());
            param.put("COMPANY_CODE", userProfile.getCompanyCode());
            param.put("PLANT_NO", userProfile.getPlantNo());
            Connection source = dataSource.getConnection();
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, param, source);
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (ServerException | SQLException | JRException e) {
            throw new ServerException(e.getMessage());
        }
    }

    @Override
    public List<GrnDetDTO> showPartNoByMSR(GrnDTO input) {
        UserProfile userProfile = UserProfileContext.getUserProfile();
        List<GrnDetDTO> list = new ArrayList<>();
        for (GrnDetDTO rec : input.getGrnDetails()) {
            List<MSRDetailProjection> lovPartNo = msrDetailRepository.showLovPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getMsrNo(), rec.getPartNo(), rec.getItemNo());
            for (MSRDetailProjection recPrj : lovPartNo) {
                list.add(GrnDetDTO.builder()
                        .seqNo(recPrj.getSeqNo())
                        .partNo(recPrj.getPartNo())
                        .itemNo(recPrj.getItemNo()).build());
            }
        }

        return list;
    }

    @Override
    public List<GrnDetDTO> showItemPart(GrnDTO input) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        List<GrnDetDTO> list = new ArrayList<>();
        for (GrnDetDTO rec : input.getGrnDetails()) {
            List<ItemProjection> lovItemPart = itemRepository.lovItemPart(userProfile.getCompanyCode(), userProfile.getPlantNo(), rec.getPartNo(), rec.getItemNo());
            for (ItemProjection recPrj : lovItemPart) {
                list.add(GrnDetDTO.builder()
                        .partNo(recPrj.getPartNo())
                        .itemNo(recPrj.getItemNo()).build());
            }
        }

        return list;
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

    /*private void checkRecdRetnQty(BigDecimal retnQty) {
        throw new ServerException("Return Qty only " + retnQty + " units !");
    }*/

    private void checkRecdQty() {
        throw new ServerException("Received Qty MUST be > 0 !");
    }

    private void checkValidRecdPriceForConsignedItem() {
        throw new ServerException("Invalid Recd Price for Consigned Item");
    }

    private void checkSourceStockItem() {
        throw new ServerException("Unknown source for Stock Item");
    }

    private void checkItemNotInPo(String itemNo) {
        throw new ServerException("Item " + itemNo + " not in PO");
    }

    private void checkValidPoNo(String poNo) {
        throw new ServerException("PO No : " + poNo + " is Invalid / Not found in Master File !");
    }

    private void checkItemNoInProject(String itemNo, String projectNo) {
        throw new ServerException("Item " + itemNo + " not found in project " + projectNo);
    }

    private void checkProjectNoIfNull(String projectNo) {
        throw new ServerException("Project No : " + projectNo + " is Invalid / Not found in Master File !");
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

    private void checkDataFromPartNo(String poNo, String partNo, UserProfile userProfile) {

        PurDetProjection purDetCnt = purDetRepository.countPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo, partNo);
        if (purDetCnt.getCountPartNo() == 0) {
            throw new ServerException("The Part No is either invalid or qty fully received!");
        }
    }

    /*private void checkDuplicatePartNo(GrnDTO input) {

        List<String> listA = new ArrayList<>();
        List<String> listB = new ArrayList<>();
        String partNo;
        for (GrnDetDTO dto : input.getGrnDetails()) {
            partNo = dto.getPartNo();
            if (listA.size() == 0) {
                listA.add(partNo);
            } else {
                listB.add(partNo);
            }
        }
        Collections.sort(listA);
        Collections.sort(listB);
        boolean isEqual = listA.equals(listB);
        if (isEqual) {
            throw new DuplicateException("Duplicate Part No found!");
        }
    }*/

    private PurDetDTO getDetailInfo(String poNo, String itemNo, String partNo, Integer poRecSeq, UserProfile
            userProfile) {

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
                    .unitPrice(detailInfo.getUnitPrice().setScale(4, RoundingMode.HALF_UP))
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
    public GrnDTO createGrn(GrnDTO input) {
        try {
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
                    detail.setRecdDate(new Timestamp(System.currentTimeMillis()));
                    GrnDet grnDetail = new GrnDet();
                    checkBeforeSaving(input);
                    if (StringUtils.equals(input.getSubType(), "M")) {
                        checkRecValidForGrnManual(userProfile, input, detail);
                    }
                    BeanUtils.copyProperties(detail, grnDetail);
                    grnDetail.setCompanyCode(userProfile.getCompanyCode());
                    grnDetail.setPlantNo(userProfile.getPlantNo());
                    grnDetail.setGrn(saved);
                    grnDetRepository.save(grnDetail);
                    grnDetailPostSaving(input, detail);
                }
            }

            grnPostSaving(userProfile, saved);
            if (StringUtils.equals(input.getSubType(), "N")) {
                closePO(userProfile, saved);
            }
            populateAfterSaving(input, saved);

            return input;
        } catch (ServerException | ParseException e) {
            throw new ServerException(e.getMessage());
        }
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
            if (detail.getItemType() == null) {
                detail.setItemType(2);
                if (detail.getItemType() != 1) {
                    throw new ServerException("No Item No found. GRN record NOT created !");
                }
            }
        }

        if (detail.getItemType() == null) {
            checkItemType();
        }

        if (StringUtils.isBlank(detail.getItemNo())) {
            checkItemNo();
        }

        if (detail.getItemType() != null) {
            if (!Integer.toString(detail.getItemType()).contains("0") && !Integer.toString(detail.getItemType()).contains("1")) {
                checkItemTypeNotNull();
            }

            if (StringUtils.isBlank(detail.getLoc())) {
                throw new ServerException("LOC Can Not be Blank !");
            }

            ItemDTO dto = ItemDTO.builder().build();
            BombypjDTO bombypjDTO = BombypjDTO.builder().build();
            PurDTO purDTO = PurDTO.builder().build();

            checkNextItem(input);

            if (detail.getItemType() == 0) {
                if (itemCur != null) {
                    for (ItemProjection rec : itemCur) {
                        dto.setItemNo(rec.getItemNo());
                        dto.setLoc(rec.getLoc());
                        dto.setUom(rec.getUom());
                        dto.setPartNo(rec.getPartNo());
                        dto.setDescription(rec.getDescription());
                        dto.setStdMaterial(rec.getStdMaterial());
                    }
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

            if (detail.getRecdQty() == null) {
                checkRecdQty();
            } else {
                if (detail.getRecdQty().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ServerException("Received Qty Must be > 0 !");
                } else if (detail.getRetnQty() != null) {
                    if (detail.getRetnQty().compareTo(BigDecimal.ZERO) > 0 &&
                            detail.getRecdQty().compareTo(detail.getRetnQty()) > 0) {
                        throw new ServerException("Return Qty only " + detail.getRetnQty() + " units !");
                    }
                }
            }

            if (detail.getLabelQty() == null) {
                checkLabelQty();
            } else {
                if (detail.getLabelQty().compareTo(BigDecimal.ZERO) <= 0) {
                    checkLabelQty();
                } else if (detail.getRecdQty().compareTo(detail.getLabelQty()) < 0) {
                    checkRecdLabelQty(detail.getRecdQty());
                }
            }

            if (detail.getRecdPrice() == null) {
                throw new ServerException("Recd Price Can Not be Blank!");
            } else {
                if (detail.getRecdPrice().compareTo(BigDecimal.ZERO) < 0) {
                    throw new ServerException("Invalid Recd Price");
                } else if (StringUtils.isNotBlank(detail.getItemNo())) {
                    if (itemSrc == null) {
                        throw new ServerException("Unknown source for Stock Item");
                    } else if (StringUtils.equals(itemSrc.getSource(), "C")) {
                        if (detail.getRecdPrice().compareTo(BigDecimal.ZERO) > 0) {
                            checkValidRecdPriceForConsignedItem();
                        }
                    }
                }
            }
        }

        if (StringUtils.isNotBlank(input.getMsrNo())) {
            if (!msrCur.isPresent()) {
                throw new ServerException("Invalid MSR No");
            }

            if (StringUtils.isNotBlank(input.getMsrNo())) {
                MSRDetailProjection vRecdQty = msrDetailRepository.getRecdQtyByMsrNo(userProfile.getCompanyCode(),
                        userProfile.getPlantNo(), input.getMsrNo(), detail.getItemNo(), detail.getSeqNo());
                if (vRecdQty.getRecdQty().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ServerException("There is no outstanding for this MSR#");
                }

                if (vRecdQty == null) {
                    throw new ServerException("Invalid MSR No / Item No");
                }
            }
        }
    }

    private void checkBeforeSaving(GrnDTO input) {

        if (StringUtils.equals(input.getSubType(), "N")) {
            checkNextItem(input);
        }
    }

    private void grnDetailPostSaving(GrnDTO input, GrnDetDTO grnDetail) {

        UserProfile userProfile = UserProfileContext.getUserProfile();

        if (grnDetail.getItemType() == 0) {
            BigDecimal convQty = null;
            BigDecimal costVar = BigDecimal.ZERO;
            BigDecimal currRate = input.getCurrencyRate();
            BigDecimal convCost = null;
            BigDecimal grnValue = BigDecimal.ZERO;
            BigDecimal stockValue = BigDecimal.ZERO;
            BigDecimal finalGrnVar = BigDecimal.ZERO;
            BigDecimal convUom = BigDecimal.ZERO;
            BigDecimal grnQty = BigDecimal.ZERO;
            BigDecimal grnPrice = BigDecimal.ZERO;

            ItemProjection itemUom = itemRepository.getItemUomByItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDetail.getItemNo());
            if (StringUtils.equals(input.getSubType(), "N")) {
                if (itemUom != null) {
                    if (!StringUtils.equals(itemUom.getUom(), grnDetail.getUom())) {
                        throw new ServerException("Resv UOM does not match Inv UOM. Inform MIS.");
                    }
                } else if (grnDetail.getStdPackQty() == null) {
                    throw new ServerException("Standard Pack Qty is empty. Check with Purchaser.");
                }

                convQty = grnDetail.getRecdQty().multiply(grnDetail.getStdPackQty());
                convCost = ((grnDetail.getRecdPrice().multiply(currRate)).divide(grnDetail.getStdPackQty(), 4, RoundingMode.HALF_UP));
                if (convCost == null) {
                    throw new ServerException("The convented new Std Cost is null.");
                }
                grnValue = (grnDetail.getRecdQty().multiply(grnDetail.getRecdPrice().multiply(currRate)).setScale(4, RoundingMode.HALF_UP));
                stockValue = (grnDetail.getRecdPrice().multiply(currRate).divide(grnDetail.getStdPackQty(), 4, RoundingMode.HALF_UP)
                        .multiply(grnDetail.getRecdQty().multiply(grnDetail.getStdPackQty())).setScale(4, RoundingMode.HALF_UP));
                finalGrnVar = grnValue.subtract(stockValue);
            } else {
                if (StringUtils.equals(input.getSubType(), "M")) {
                    if (StringUtils.isNotBlank(input.getMsrNo())) {
                        checkIfMsrNoValid(input.getMsrNo());
                    }
                    UOMProjection uomFactor = uomRepository.getUomFactor(grnDetail.getUom(), itemUom.getUom());
                    convUom = uomFactor == null ? BigDecimal.ONE : uomFactor.getUomFactor();
                    grnValue = (grnDetail.getRecdQty().multiply(convUom).multiply(grnDetail.getRecdPrice().multiply(currRate)
                            .divide(convUom, 4, RoundingMode.HALF_UP)).setScale(4, RoundingMode.HALF_UP));
                    stockValue = ((grnDetail.getRecdPrice().multiply(currRate).divide(convUom, 4, RoundingMode.HALF_UP))
                            .multiply(grnDetail.getRecdQty().multiply(convUom))).setScale(4, RoundingMode.HALF_UP);
                    finalGrnVar = grnValue.subtract(stockValue);
                    grnQty = (grnDetail.getRecdQty().multiply(convUom)).setScale(4, RoundingMode.HALF_UP);
                    grnPrice = (grnDetail.getRecdPrice().multiply(currRate)).divide(convUom, 4, RoundingMode.HALF_UP);
                }
            }

            ItemProjection itemCur = itemRepository.getDataItemCur(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDetail.getItemNo());
            BigDecimal itemQoh = itemCur.getQoh() == null ? BigDecimal.ZERO : itemCur.getQoh();
            BigDecimal itemOrderQty = itemCur.getOrderQty() == null ? BigDecimal.ZERO : itemCur.getOrderQty();
            BigDecimal itemVar = itemCur.getCostVariance() == null ? BigDecimal.ZERO : itemCur.getCostVariance();
            BigDecimal itemStdMat = itemCur.getStdMaterial() == null ? BigDecimal.ZERO : itemCur.getStdMaterial();
            BigDecimal itemBatchNo = itemCur.getBatchNo();
            BigDecimal itemYtdReceipt = itemCur.getYtdReceipt() == null ? BigDecimal.ZERO : itemCur.getYtdReceipt();

            NLCTLProjection batchYear = nlctlRepository.getBatchYear(userProfile.getCompanyCode(), userProfile.getPlantNo());
            Long newBatchNo = null;
            if (itemBatchNo == null) {
                newBatchNo = (batchYear.getBatchNo().multiply(BigDecimal.valueOf(10000))).add(BigDecimal.valueOf(1)).longValue();
            } else {
                String batchYr = itemBatchNo.toString().substring(0, 4);
                String batchNo = itemBatchNo.toString().substring(7);
                BigDecimal btchYr = BigDecimal.valueOf(Double.parseDouble(batchYr));
                BigDecimal btchNo = BigDecimal.valueOf(Double.parseDouble(batchNo)).add(BigDecimal.valueOf(1));

                if (btchYr.compareTo(batchYear.getBatchNo()) < 0) {
                    btchYr = batchYear.getBatchNo();
                    btchNo = BigDecimal.ZERO;
                }

                newBatchNo = (btchYr.multiply(BigDecimal.valueOf(10000))).add(btchNo).longValue();

                if (newBatchNo == null) {
                    throw new NotFoundException("New Batch No not found!");
                }
            }

            BigDecimal newStdMat = BigDecimal.ZERO;
            BigDecimal newCostVar = BigDecimal.ZERO;
            BigDecimal newQoh = BigDecimal.ZERO;
            Date lastTranDate = new Date(System.currentTimeMillis());

            if (itemCur != null) {
                if (StringUtils.equals(input.getSubType(), "N")) {
                    BigDecimal itemValue = (itemQoh.multiply(itemStdMat)).add(itemVar).add(convQty.multiply(convCost));
                    if (itemQoh.compareTo(BigDecimal.ZERO) <= 0) {
                        newStdMat = ((itemValue.subtract(itemVar)).divide(convQty.add(itemQoh)).setScale(4, RoundingMode.HALF_UP));
                    } else {
                        newStdMat = itemValue.divide((convQty.add(itemQoh)), 4, RoundingMode.HALF_UP);
                    }
                    newQoh = itemQoh.add(convQty);
                    itemOrderQty = itemOrderQty.subtract(convQty);
                    newCostVar = ((itemValue.subtract(newQoh.multiply(newStdMat))).add(finalGrnVar)).setScale(4, RoundingMode.HALF_UP);
                    costVar = ((itemValue.subtract(newQoh.multiply(newStdMat))).subtract(itemVar)).setScale(4, RoundingMode.HALF_UP);
                }

                if (StringUtils.equals(input.getSubType(), "M")) {
                    GrnDetDTO calStdMat = inpCalStdMaterial(itemQoh, itemStdMat, itemVar, currRate, grnDetail.getRecdQty(),
                            grnDetail.getRecdPrice(), convUom, newStdMat, newCostVar, newQoh);
                    newStdMat = calStdMat.getNewStdMaterial();
                    newCostVar = calStdMat.getNewCostVar().setScale(4, RoundingMode.HALF_UP);
                    newQoh = calStdMat.getConvQoh();
                    itemYtdReceipt = itemYtdReceipt.add(newQoh);
                    costVar = newCostVar.subtract(finalGrnVar).subtract(itemVar);
                    BigDecimal newQohManual = itemQoh.add(newQoh);
                    itemRepository.updateQohStdMatCostVarYtdRecLTranDateBatchNo(newQohManual, newStdMat, newCostVar,
                            itemYtdReceipt, lastTranDate, BigDecimal.valueOf(newBatchNo),
                            userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDetail.getItemNo());
                } else {
                    itemYtdReceipt = itemYtdReceipt.add(convQty);
                    itemRepository.updateQohOrderQtyStdMatCVarYtdRLTranDtLPurPrBtcNo(newQoh, itemOrderQty, newStdMat, newCostVar, itemYtdReceipt,
                            lastTranDate, convCost, BigDecimal.valueOf(newBatchNo),
                            userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDetail.getItemNo());
                }
            }

            /** ITEMLOC **/
            CompanyDTO stkLoc = companyService.getStockLocation();
            ItemLocProjection itemLocWithRecCnt = itemLocRepository.findItemLocWithRecCnt(userProfile.getCompanyCode(), userProfile.getPlantNo(),
                    grnDetail.getItemNo(), grnDetail.getLoc());

            ItemLocProjection itemLocInfo = itemLocRepository.itemLocInfo(userProfile.getCompanyCode(),
                    userProfile.getPlantNo(), grnDetail.getItemNo(), grnDetail.getLoc());
            BigDecimal qohItemLoc = null;
            BigDecimal orderQtyItemLoc = null;
            BigDecimal ytdReceiptItemLoc = null;
            Long itemLocId = null;
            if (itemLocInfo != null) {
                orderQtyItemLoc = itemLocInfo.getOrderQty() == null ? BigDecimal.ZERO : itemLocInfo.getOrderQty();
                ytdReceiptItemLoc = itemLocInfo.getYtdReceipt() == null ? BigDecimal.ZERO : itemLocInfo.getYtdReceipt();
                qohItemLoc = itemLocInfo.getQoh() == null ? BigDecimal.ZERO : itemLocInfo.getQoh();
                itemLocId = itemLocInfo.getId();
            }

            ItemLoc savedItemLoc = new ItemLoc();
            if (itemLocWithRecCnt == null) {
                if (StringUtils.equals(input.getSubType(), "M")) {
                    costVar = BigDecimal.ZERO;
                }
                ItemProjection itemInfo = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDetail.getItemNo());
                if (stkLoc != null && StringUtils.equals(stkLoc.getStockLoc(), grnDetail.getLoc())) {
                    ItemLoc itemLoc = new ItemLoc();
                    itemLoc.setCompanyCode(userProfile.getCompanyCode());
                    itemLoc.setPlantNo(userProfile.getPlantNo());
                    itemLoc.setItemNo(grnDetail.getItemNo());
                    itemLoc.setLoc(grnDetail.getLoc());
                    itemLoc.setPartNo(grnDetail.getPartNo());
                    itemLoc.setDescription(grnDetail.getDescription());
                    itemLoc.setCategoryCode(itemInfo.getCategoryCode());
                    if (StringUtils.equals(input.getSubType(), "M")) {
                        itemLoc.setQoh(itemCur.getQoh());
                        itemLoc.setYtdReceipt(itemCur.getYtdReceipt());
                        itemLoc.setStdMaterial(itemCur.getStdMaterial());
                        itemLoc.setCostVariance(itemCur.getCostVariance());
                        itemLoc.setBatchNo(itemCur.getBatchNo());
                        itemLoc.setLastTranDate(lastTranDate);
                    } else {
                        itemLoc.setQoh(convQty);
                        itemLoc.setBalbfQty(convQty);
                        itemLoc.setYtdReceipt(convQty);
                        itemLoc.setStdMaterial(newStdMat);
                        itemLoc.setCostVariance(newCostVar);
                        itemLoc.setBatchNo(BigDecimal.valueOf(newBatchNo));
                        itemLoc.setLastTranDate(lastTranDate);
                    }
                    itemLoc.setStatus(Status.ACTIVE);
                    itemLoc.setCreatedBy(userProfile.getUsername());
                    itemLoc.setCreatedAt(ZonedDateTime.now());
                    itemLoc.setUpdatedBy(userProfile.getUsername());
                    itemLoc.setUpdatedAt(ZonedDateTime.now());
                    itemLoc.setItemId(itemInfo.getItemId());
                    itemLocRepository.save(itemLoc);
                } else {
                    /** INSERT FIRST **/
                    List<ItemLoc> itemLocs = itemLocRepository.findByCompanyCodeAndPlantNoAndItemNoAndLoc(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDetail.getItemNo(), stkLoc.getStockLoc());
                    if (!CollectionUtils.isEmpty(itemLocs) && itemLocs.size() == 1) {
                        ItemLoc itemLoc = itemLocRepository.getById(itemLocs.get(0).getId());
                        itemLoc.setCompanyCode(userProfile.getCompanyCode());
                        itemLoc.setPlantNo(userProfile.getPlantNo());
                        itemLoc.setItemNo(grnDetail.getItemNo());
                        itemLoc.setLoc(stkLoc.getStockLoc());
                        itemLoc.setPartNo(grnDetail.getPartNo());
                        itemLoc.setDescription(grnDetail.getDescription());
                        itemLoc.setCategoryCode(itemInfo.getCategoryCode());
                        if (StringUtils.equals(input.getSubType(), "M")) {
                            itemLoc.setQoh(BigDecimal.ZERO);
                            itemLoc.setYtdReceipt(grnQty);
                            itemLoc.setStdMaterial(grnPrice);
                            itemLoc.setCostVariance(newCostVar);
                            itemLoc.setBatchNo(BigDecimal.valueOf(newBatchNo));
                            itemLoc.setLastTranDate(lastTranDate);
                        } else {
                            itemLoc.setQoh(BigDecimal.ZERO);
                            itemLoc.setBalbfQty(convQty);
                            itemLoc.setYtdReceipt(convQty);
                            itemLoc.setStdMaterial(newStdMat);
                            itemLoc.setCostVariance(newCostVar);
                            itemLoc.setBatchNo(BigDecimal.valueOf(newBatchNo));
                            itemLoc.setLastTranDate(lastTranDate);
                        }
                        itemLoc.setStatus(Status.ACTIVE);
                        itemLoc.setCreatedBy(userProfile.getUsername());
                        itemLoc.setCreatedAt(ZonedDateTime.now());
                        itemLoc.setUpdatedBy(userProfile.getUsername());
                        itemLoc.setUpdatedAt(ZonedDateTime.now());
                        itemLoc.setItemId(itemInfo.getItemId());
                        itemLocRepository.save(itemLoc);
                    }

                    /** INSERT SECOND **/
                    ItemLoc itemLoc2 = new ItemLoc();
                    itemLoc2.setCompanyCode(userProfile.getCompanyCode());
                    itemLoc2.setPlantNo(userProfile.getPlantNo());
                    itemLoc2.setItemNo(grnDetail.getItemNo());
                    itemLoc2.setLoc(grnDetail.getLoc());
                    itemLoc2.setPartNo(grnDetail.getPartNo());
                    itemLoc2.setDescription(grnDetail.getDescription());
                    itemLoc2.setCategoryCode(itemInfo.getCategoryCode());
                    if (StringUtils.equals(input.getSubType(), "M")) {
                        itemLoc2.setQoh(BigDecimal.ZERO);
                        itemLoc2.setYtdReceipt(grnQty);
                        itemLoc2.setStdMaterial(grnPrice);
                        itemLoc2.setCostVariance(newCostVar);
                        itemLoc2.setBatchNo(BigDecimal.valueOf(newBatchNo));
                        itemLoc2.setLastTranDate(lastTranDate);
                    } else {
                        itemLoc2.setQoh(convQty);
                        itemLoc2.setBalbfQty(convQty);
                        itemLoc2.setYtdReceipt(convQty);
                        itemLoc2.setStdMaterial(newStdMat);
                        itemLoc2.setCostVariance(BigDecimal.ZERO);
                        itemLoc2.setBatchNo(BigDecimal.valueOf(newBatchNo));
                        itemLoc2.setLastTranDate(lastTranDate);
                    }
                    itemLoc2.setStatus(Status.ACTIVE);
                    itemLoc2.setCreatedBy(userProfile.getUsername());
                    itemLoc2.setCreatedAt(ZonedDateTime.now());
                    itemLoc2.setUpdatedBy(userProfile.getUsername());
                    itemLoc2.setUpdatedAt(ZonedDateTime.now());
                    itemLoc2.setItemId(itemInfo.getItemId());
                    savedItemLoc = itemLocRepository.save(itemLoc2);
                }
            } else if (itemLocWithRecCnt.getRecCnt() > 0) {
                if (StringUtils.equals(input.getSubType(), "M")) {
                    ytdReceiptItemLoc = ytdReceiptItemLoc.add(newQoh);
                    itemLocRepository.updateStdMatYtdRecBatchNoLTranDate(newCostVar, newStdMat,
                            ytdReceiptItemLoc, BigDecimal.valueOf(newBatchNo), lastTranDate, userProfile.getCompanyCode(),
                            userProfile.getPlantNo(), grnDetail.getItemNo(), stkLoc.getStockLoc());
                } else {
                    orderQtyItemLoc = orderQtyItemLoc.subtract(convQty);
                    ytdReceiptItemLoc = ytdReceiptItemLoc.add(convQty);
                    itemLocRepository.updateOrderVarianceStdMatYtdRecBatchNoLTranDateLPurPrice(orderQtyItemLoc, newCostVar, newStdMat,
                            ytdReceiptItemLoc, BigDecimal.valueOf(newBatchNo), lastTranDate, convCost, userProfile.getCompanyCode(),
                            userProfile.getPlantNo(), grnDetail.getItemNo(), stkLoc.getStockLoc());
                }

                if (StringUtils.isBlank(stkLoc.getStockLoc())) {
                    throw new NotFoundException("Company Stock Loc not found!");
                }

                if (StringUtils.equals(input.getSubType(), "M")) {
                    itemLocRepository.updateStdMatYtdRecBatchNoLTranDate(newStdMat,
                            ytdReceiptItemLoc, BigDecimal.valueOf(newBatchNo), lastTranDate, userProfile.getCompanyCode(),
                            userProfile.getPlantNo(), grnDetail.getItemNo(), stkLoc.getStockLoc());
                } else {
                    itemLocRepository.updateStdMatYtdRecBatchNoLTranDateLPurPrice(newStdMat,
                            ytdReceiptItemLoc, BigDecimal.valueOf(newBatchNo), lastTranDate, convCost, userProfile.getCompanyCode(),
                            userProfile.getPlantNo(), grnDetail.getItemNo(), stkLoc.getStockLoc());
                }


                if (StringUtils.isBlank(itemLocWithRecCnt.getLoc())) {
                    ItemProjection itemInfo = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDetail.getItemNo());
                    ItemLoc itemLoc = new ItemLoc();
                    itemLoc.setCompanyCode(userProfile.getCompanyCode());
                    itemLoc.setPlantNo(userProfile.getPlantNo());
                    itemLoc.setLoc(grnDetail.getLoc());
                    itemLoc.setItemNo(grnDetail.getItemNo());
                    itemLoc.setCategoryCode(itemInfo.getCategoryCode());
                    itemLoc.setPartNo(grnDetail.getPartNo());
                    itemLoc.setDescription(grnDetail.getDescription());
                    if (StringUtils.equals(input.getSubType(), "M")) {
                        itemLoc.setQoh(grnQty);
                        itemLoc.setYtdReceipt(grnQty);
                        itemLoc.setStdMaterial(newStdMat);
                        itemLoc.setCostVariance(BigDecimal.ZERO);
                        itemLoc.setBatchNo(BigDecimal.valueOf(newBatchNo));
                        itemLoc.setLastTranDate(lastTranDate);
                    } else {
                        itemLoc.setQoh(convQty);
                        itemLoc.setBalbfQty(convQty);
                        itemLoc.setYtdReceipt(convQty);
                        itemLoc.setStdMaterial(newStdMat);
                        itemLoc.setCostVariance(BigDecimal.ZERO);
                        itemLoc.setBatchNo(BigDecimal.valueOf(newBatchNo));
                        itemLoc.setLastTranDate(lastTranDate);
                    }
                    itemLoc.setStatus(Status.ACTIVE);
                    itemLoc.setCreatedBy(userProfile.getUsername());
                    itemLoc.setCreatedAt(ZonedDateTime.now());
                    itemLoc.setUpdatedBy(userProfile.getUsername());
                    itemLoc.setUpdatedAt(ZonedDateTime.now());
                    itemLoc.setItemId(itemCur.getItemId());
                    itemLocRepository.save(itemLoc);
                } else {
                    if (StringUtils.equals(input.getSubType(), "N")) {
                        qohItemLoc = qohItemLoc.add(convQty);
                    } else {
                        qohItemLoc = qohItemLoc.add(newQoh);
                    }
                    itemLocRepository.updateQoh(qohItemLoc, userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDetail.getItemNo());
                }
            }

            /** INSERT ITEMBATC  **/
            ItemBatc itemBatc = new ItemBatc();
            ItemBatcId itemBatcId = new ItemBatcId();
            itemBatcId.setCompanyCode(userProfile.getCompanyCode());
            itemBatcId.setPlantNo(userProfile.getPlantNo());
            itemBatcId.setItemNo(grnDetail.getItemNo());
            itemBatcId.setLoc(grnDetail.getLoc());
            itemBatcId.setBatchNo(newBatchNo.longValue());
            itemBatc.setTranDate(lastTranDate);
            if (StringUtils.equals(input.getSubType(), "M")) {
                itemBatc.setQoh(newQoh);
                itemBatc.setOriQoh(newQoh);
            } else {
                itemBatc.setQoh(convQty);
                itemBatc.setOriQoh(convQty);
            }
            itemBatc.setStdMaterial(newStdMat);
            if (StringUtils.equals(input.getSubType(), "N")) {
                itemBatc.setPoNo(input.getPoNo());
            } else {
                if (StringUtils.isBlank(grnDetail.getPoNo())) {
                    itemBatc.setPoNo(input.getDoNo());
                } else {
                    itemBatc.setPoNo(grnDetail.getPoNo());
                }
            }
            itemBatc.setPoRecSeq(grnDetail.getPoRecSeq());
            itemBatc.setGrnNo(input.getGrnNo());
            itemBatc.setGrnSeq(grnDetail.getSeqNo());
            itemBatc.setDateCode(grnDetail.getDateCode());
            itemBatc.setId(itemBatcId);
            itemBatcRepository.save(itemBatc);

            /** INSERT INAUDIT  **/
            BigDecimal balQoh = null;
            if (StringUtils.equals(input.getSubType(), "M")) {
                balQoh = itemQoh.add(newQoh);
            }
            InAudit inAudit = new InAudit();
            inAudit.setCompanyCode(userProfile.getCompanyCode());
            inAudit.setPlantNo(userProfile.getPlantNo());
            inAudit.setItemNo(grnDetail.getItemNo());
            inAudit.setLoc(grnDetail.getLoc());
            inAudit.setTranDate(lastTranDate);
            String tranTime = FastDateFormat.getInstance("kkmmssss").format(System.currentTimeMillis());
            inAudit.setTranTime(tranTime);
            inAudit.setItemlocId(itemLocId == null ? savedItemLoc.getId() : itemLocId);
            if (StringUtils.equals(input.getSubType(), "M")) {
                inAudit.setTranType("RM");
            } else {
                inAudit.setTranType("RU");
            }
            inAudit.setDocmNo(grnDetail.getGrnNo());
            inAudit.setDocmDate(grnDetail.getRecdDate());
            inAudit.setUom(itemUom.getUom());
            inAudit.setProjectNo(grnDetail.getProjectNo());
            if (StringUtils.equals(input.getSubType(), "N")) {
                inAudit.setPoNo(input.getPoNo());
            } else {
                inAudit.setPoNo(grnDetail.getPoNo());
            }
            inAudit.setDoNo(input.getDoNo());
            inAudit.setGrnNo(grnDetail.getGrnNo());
            inAudit.setCurrencyCode(input.getCurrencyCode());
            inAudit.setCurrencyRate(currRate);
            if (StringUtils.equals(input.getSubType(), "M")) {
                inAudit.setInQty(newQoh);
                inAudit.setOrderQty(newQoh);
                inAudit.setBalQty(balQoh);
                inAudit.setActualCost(grnDetail.getRecdPrice() == null ? BigDecimal.ZERO : grnDetail.getRecdPrice());
                inAudit.setSeqNo(null);
            } else {
                inAudit.setInQty(convQty);
                inAudit.setOrderQty(convQty);
                inAudit.setBalQty(newQoh);
                inAudit.setActualCost(convCost);
                inAudit.setSeqNo(grnDetail.getSeqNo());
            }
            inAudit.setNewStdMaterial(newStdMat);
            inAudit.setOriStdMaterial(itemStdMat);
            inAudit.setCostVariance(costVar);
            inAudit.setGrnVariance(finalGrnVar);
            if (StringUtils.equals(input.getSubType(), "M")) {
                inAudit.setRemarks("ISSUED THRU MANUAL GRN : INM00002");
            }
            inAudit.setStatus(Status.ACTIVE);
            inAudit.setCreatedBy(userProfile.getUsername());
            inAudit.setCreatedAt(ZonedDateTime.now());
            inAudit.setUpdatedBy(userProfile.getUsername());
            inAudit.setUpdatedAt(ZonedDateTime.now());
            inAuditRepository.save(inAudit);

            /** MSRDET UPDATE  **/
            if (StringUtils.isNotBlank(input.getMsrNo())) {
                updateMSR(userProfile, input, grnDetail, newQoh);
            }

            String projectNoMrv = null;
            BigDecimal itemPickQty = null;
            BigDecimal itemProdnResv = null;
            if (StringUtils.equals(input.getSubType(), "M")) {
                if (StringUtils.isNotBlank(grnDetail.getProjectNo())) {
                    if (StringUtils.isNotBlank(input.getMsrNo())) {
                        MRVDetailProjection mrvDetCur = mrvDetailRepository.mrvDetCur(userProfile.getCompanyCode(),
                                userProfile.getPlantNo(), input.getMsrNo(), grnDetail.getItemNo(), grnDetail.getSeqNo());
                        if (StringUtils.isBlank(mrvDetCur.getProjectNo())) {
                            projectNoMrv = grnDetail.getProjectNo();
                        }
                    } else {
                        projectNoMrv = grnDetail.getProjectNo();
                    }

                    BigDecimal vRecdQty = (grnDetail.getRecdQty() == null ? BigDecimal.ZERO : grnDetail.getRecdQty()).multiply(convUom);
                    /***************************** BOMBYPJ UPDATE *****************************/
                    List<BombypjProjection> bombypjCurs = bombypjRepository.bombypjCurList(userProfile.getCompanyCode(),
                            userProfile.getPlantNo(), projectNoMrv, grnDetail.getItemNo());
                    for (BombypjProjection bombypjCur : bombypjCurs) {
                        String tranType = bombypjCur.getTranType();
                        String orderNo = bombypjCur.getOrderNo();
                        BigDecimal vResvQty = bombypjCur.getResvQty();
                        BigDecimal vShortQty = bombypjCur.getShortQty();
                        BigDecimal vIssuedQty = bombypjCur.getIssuedQty();
                        BigDecimal vPickedQTy = bombypjCur.getPickedQty();
                        String itemSource = itemUom.getSource();
                        if (StringUtils.isNotBlank(input.getMsrNo())) {
                            /** Returning of MSR **/
                            if (vRecdQty.compareTo(vShortQty) > 0) {
                                vPickedQTy = vPickedQTy.add(vShortQty);
                                itemPickQty = itemPickQty.add(vShortQty);
                                vRecdQty = vRecdQty.subtract(vShortQty);
                                vShortQty = BigDecimal.ZERO;
                            } else {
                                vPickedQTy = vPickedQTy.add(vRecdQty);
                                itemPickQty = itemPickQty.add(vRecdQty);
                                vShortQty = vShortQty.subtract(vRecdQty);
                                vRecdQty = BigDecimal.ZERO;
                            }
                        } else {
                            /** Consigned Item handling **/
                            if (stkLoc.getStockLoc().equals(grnDetail.getLoc())) {
                                if (StringUtils.equals(tranType, "PRJ") && StringUtils.equals(itemSource, "C")) {
                                    if (vResvQty.compareTo(vRecdQty) < 0) {
                                        itemProdnResv = vRecdQty.subtract(vResvQty);
                                        vResvQty = vRecdQty;
                                    }
                                    if (vPickedQTy.compareTo(vRecdQty) < 0) {
                                        itemPickQty = vRecdQty.subtract(vPickedQTy);
                                        vPickedQTy = vRecdQty;
                                    }
                                } else {
                                    /** Normal Item **/
                                    if ((vRecdQty.subtract(vIssuedQty)).compareTo(BigDecimal.ZERO) > 0) {
                                        if (vRecdQty.compareTo(vRecdQty.subtract(vIssuedQty)) > 0) {
                                            vPickedQTy = vPickedQTy.add((vRecdQty.subtract(vIssuedQty)));
                                            vShortQty = vShortQty.subtract((vRecdQty.subtract(vIssuedQty)));
                                            itemPickQty = itemPickQty.add((vRecdQty.subtract(vIssuedQty)));
                                            vRecdQty = vRecdQty.subtract((vRecdQty.subtract(vIssuedQty)));
                                        } else {
                                            vPickedQTy = vPickedQTy.add(vRecdQty);
                                            vShortQty = vShortQty.subtract(vRecdQty);
                                            itemPickQty = itemPickQty.add(vRecdQty);
                                            vRecdQty = BigDecimal.ZERO;
                                        }

                                        if (vShortQty.compareTo(BigDecimal.ZERO) < 0) {
                                            vShortQty = BigDecimal.ZERO;
                                        }
                                    }
                                }
                            }
                        }
                        bombypjRepository.updatePickedShortResvQtyDelvDate(vPickedQTy, vShortQty, vResvQty, grnDetail.getRecdDate(),
                                userProfile.getCompanyCode(), userProfile.getPlantNo(), orderNo,
                                bombypjCur.getAlternate(), bombypjCur.getProjectNo());
                    }

                    /** UPDATE ITEM & ITEMLOC PICKED_QTY **/
                    ItemProjection itemInfo = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDetail.getItemNo());
                    BigDecimal pickedQtyItem = (itemInfo.getPickedQty() == null ? BigDecimal.ZERO : itemInfo.getPickedQty()).add(itemPickQty);
                    BigDecimal prodnResvItem = (itemInfo.getProdnResv() == null ? BigDecimal.ZERO : itemInfo.getProdnResv()).add(itemProdnResv);
                    itemRepository.updatePickedQtyProdnResv(pickedQtyItem, prodnResvItem, userProfile.getCompanyCode(),
                            userProfile.getPlantNo(), grnDetail.getItemNo());

                    BigDecimal pickedQtyLoc = BigDecimal.ZERO;
                    BigDecimal prodnResvLoc = BigDecimal.ZERO;
                    if (itemLocInfo != null) {
                        pickedQtyLoc = (itemLocInfo.getPickedQty() == null ? BigDecimal.ZERO : itemLocInfo.getPickedQty()).add(itemPickQty);
                        prodnResvLoc = (itemLocInfo.getProdnResv() == null ? BigDecimal.ZERO : itemLocInfo.getProdnResv()).add(itemProdnResv);
                    }
                    itemLocRepository.updatePickedQtyProdnResv(pickedQtyLoc, prodnResvLoc, userProfile.getCompanyCode(),
                            userProfile.getPlantNo(), grnDetail.getItemNo(), grnDetail.getLoc());
                }
            } else {
                /***************************** Update BOM Buylist Table *****************************/
                updateBom(userProfile, input, grnDetail, stkLoc.getStockLoc(), convQty, lastTranDate);
            }
        }

        if (StringUtils.equals(input.getSubType(), "N")) {
            Date rlseDate = new Date(System.currentTimeMillis());
            Date recdDate = new Date(System.currentTimeMillis());

            /** PURDET UPDATE **/
            PurDetProjection purDetInfo = purDetRepository.purDetInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(),
                    grnDetail.getItemNo(), grnDetail.getProjectNo());
            BigDecimal rlseQty = (purDetInfo.getRlseQty() == null ? BigDecimal.ZERO : purDetInfo.getRlseQty()).add(grnDetail.getRecdQty());
            BigDecimal recdQty = (purDetInfo.getRecdQty() == null ? BigDecimal.ZERO : purDetInfo.getRecdQty()).add(grnDetail.getRecdQty());

            DraftPurDetProjection dPurDetInfo = draftPurDetRepository.draftPurDetInfo(userProfile.getCompanyCode(),
                    userProfile.getPlantNo(), grnDetail.getItemNo(), grnDetail.getProjectNo());

            BigDecimal rlseQtyDP = (dPurDetInfo.getRlseQty() == null ? BigDecimal.ZERO : dPurDetInfo.getRlseQty()).add(grnDetail.getRecdQty());
            BigDecimal recdQtyDP = (dPurDetInfo.getRecdQty() == null ? BigDecimal.ZERO : dPurDetInfo.getRecdQty()).add(grnDetail.getRecdQty());

            draftPurDetRepository.updateRlseRecdDateRlseRecdQtyRecdPrice(rlseDate, recdDate, rlseQtyDP, recdQtyDP,
                    grnDetail.getPoPrice(), userProfile.getCompanyCode(), userProfile.getPlantNo(),
                    grnDetail.getItemNo(), grnDetail.getProjectNo());
            purDetRepository.updateRlseRecdDateRlseRecdQtyRecdPrice(rlseDate, recdDate, rlseQty, recdQty,
                    grnDetail.getPoPrice(), userProfile.getCompanyCode(), userProfile.getPlantNo(),
                    grnDetail.getItemNo(), grnDetail.getProjectNo());
        }
    }

    private GrnDetDTO inpCalStdMaterial(BigDecimal oldQoh, BigDecimal oldStdMat, BigDecimal oldCostVar,
                                        BigDecimal currRate, BigDecimal tranQty, BigDecimal tranPrice,
                                        BigDecimal uomFactor, BigDecimal newStdMat, BigDecimal newCostVar, BigDecimal convQoh) {

        oldQoh = oldQoh == null ? BigDecimal.ZERO : oldQoh;
        oldStdMat = oldStdMat == null ? BigDecimal.ZERO : oldStdMat;
        oldCostVar = oldCostVar == null ? BigDecimal.ZERO : oldCostVar;
        newStdMat = oldStdMat;
        newCostVar = BigDecimal.ZERO;
        BigDecimal stdMaterial = null;
        BigDecimal convQty = tranQty.multiply(uomFactor);
        BigDecimal convPrice = tranPrice.multiply(currRate).divide(uomFactor);
        BigDecimal docValue = convQty.multiply(convPrice);
        BigDecimal newQOH = oldQoh.add(convQty);
        BigDecimal itemValue = (oldQoh.multiply(oldStdMat)).add(oldCostVar);
        itemValue = itemValue.add(docValue);
        // Exclude variance if QOH is 0
        if (oldQoh.compareTo(BigDecimal.ZERO) <= 0) {
            stdMaterial = (itemValue.subtract(oldCostVar)).divide(newQOH, 4, RoundingMode.HALF_UP);
            newStdMat = stdMaterial.setScale(4, RoundingMode.HALF_UP);
            newCostVar = (itemValue.subtract(newQOH.multiply(stdMaterial)));
        } else {
            stdMaterial = itemValue.divide(newQOH, 16, RoundingMode.HALF_UP);
            newStdMat = stdMaterial.setScale(4, RoundingMode.HALF_UP);
            newCostVar = (stdMaterial.multiply(newQOH)).subtract(newStdMat.multiply(newQOH).setScale(4, RoundingMode.HALF_UP));
        }
        convQoh = convQty;

        return GrnDetDTO.builder()
                .newStdMaterial(newStdMat)
                .newCostVar(newCostVar)
                .convQoh(convQoh).build();
    }

    private void updateMSR(UserProfile userProfile, GrnDTO input, GrnDetDTO detail, BigDecimal newQoh) {
        Optional<MSRDetail> msrDetRec = msrDetailRepository.getMSRDetailByCompanyCodeAndPlantNoAndMsrNoAndSeqNo(userProfile.getCompanyCode(),
                userProfile.getPlantNo(), input.getMsrNo(), detail.getSeqNo());
        boolean isSuccessUpdateMSR = false;
        if (msrDetRec.isPresent()) {
            MsrDetailDTO dto = MsrDetailDTO.builder().build();
            dto.setRecdQty(msrDetRec.get().getRecdQty().add(newQoh));
            dto.setRecdPrice(detail.getRecdPrice());
            msrDetailRepository.updateRecdQty(dto.getRecdQty(), dto.getRecdPrice(), userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getMsrNo());
            msrRepository.updateDocmNo(input.getDoNo(), userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getMsrNo());
            isSuccessUpdateMSR = true;
        }
        if (!isSuccessUpdateMSR) {
            throw new ServerException("Error Updating MSR");
        }
    }

    private void updateBom(UserProfile userProfile, GrnDTO input, GrnDetDTO grnDetail, String stockLoc, BigDecimal convQty, Date lastTranDate) {

        List<BombypjDetailProjection> bombypjDetCurs = bombypjDetailRepository.getBombypjDetCur(userProfile.getCompanyCode(),
                userProfile.getPlantNo(), input.getPoNo(), grnDetail.getItemNo(), grnDetail.getPoRecSeq());
        ItemLocProjection itemLocInfo = itemLocRepository.itemLocInfo(userProfile.getCompanyCode(),
                userProfile.getPlantNo(), grnDetail.getItemNo(), grnDetail.getLoc());
        ItemProjection itemInfo = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDetail.getItemNo());

        boolean isSuccess = false;
        BigDecimal itemPickQty = BigDecimal.ZERO;
        BigDecimal itemProdnResv = BigDecimal.ZERO;
        BigDecimal poResvQty = BigDecimal.ZERO;
        BigDecimal vRecdQty = convQty;

        for (BombypjDetailProjection bombypjDetCur : bombypjDetCurs) {
            BigDecimal orgDetRecdQty = bombypjDetCur.getRecdQty() == null ? BigDecimal.ZERO : bombypjDetCur.getRecdQty();
            BigDecimal accumRecdQty = bombypjDetCur.getAccumRecdQty() == null ? BigDecimal.ZERO : bombypjDetCur.getAccumRecdQty();
            BigDecimal detRecdQty = bombypjDetCur.getRecdQty() == null ? BigDecimal.ZERO : bombypjDetCur.getRecdQty();
            BigDecimal detBalQty = bombypjDetCur.getBalQty() == null ? BigDecimal.ZERO : bombypjDetCur.getBalQty();
            BigDecimal detResvQty = bombypjDetCur.getResvQty() == null ? BigDecimal.ZERO : bombypjDetCur.getResvQty();
            String vGrnNo = bombypjDetCur.getGrnNo();

            /***************************************** For Print Picked List *****************************************/
            String tranType = bombypjDetCur.getTranType();
            String projectNo = bombypjDetCur.getProjectNo();
            String orderNo = bombypjDetCur.getOrderNo();
            String assemblyNo = bombypjDetCur.getAssemblyNo();

            /***************************************** UPDATE the BOMBYPJ table *****************************************/

            List<BombypjProjection> bombypjCurs = bombypjRepository.getBombypjCur(userProfile.getCompanyCode(), userProfile.getPlantNo(),
                    tranType, projectNo, orderNo, assemblyNo, grnDetail.getItemNo());

            for (BombypjProjection bombypjCur : bombypjCurs) {
                BigDecimal inTransitQty = bombypjCur.getInTransitQty();
                BigDecimal delvQty = bombypjCur.getDelvQty();
                BigDecimal pickedQty = bombypjCur.getPickedQty();
                BigDecimal shortQty = bombypjCur.getShortQty();
                if (detBalQty.compareTo(vRecdQty) < 0) {
                    inTransitQty = inTransitQty.subtract(detBalQty);
                    delvQty = delvQty.add(detBalQty);
                    if (StringUtils.equals(stockLoc, grnDetail.getLoc())) {
                        pickedQty = pickedQty.add(detBalQty);
                        itemPickQty = itemPickQty.add(detBalQty);
                    } else {
                        shortQty = shortQty.add(detBalQty);
                    }

                    vRecdQty = vRecdQty.subtract(detBalQty);
                    accumRecdQty = accumRecdQty.add(detBalQty);
                    detRecdQty = detBalQty;
                    poResvQty = poResvQty.add(detBalQty);
                    detBalQty = BigDecimal.ZERO;
                } else { // (detBalQty >= vRecdQty)
                    inTransitQty = inTransitQty.subtract(vRecdQty);
                    delvQty = delvQty.add(vRecdQty);
                    if (StringUtils.equals(stockLoc, grnDetail.getLoc())) {
                        pickedQty = pickedQty.add(vRecdQty);
                        itemPickQty = itemPickQty.add(vRecdQty);
                    } else {
                        shortQty = shortQty.add(vRecdQty);
                    }

                    detBalQty = detBalQty.subtract(vRecdQty);
                    accumRecdQty = accumRecdQty.add(vRecdQty);
                    detRecdQty = vRecdQty;
                    poResvQty = poResvQty.add(vRecdQty);
                    vRecdQty = BigDecimal.ZERO;
                }

                /************************** for item resv thru SRP **************************/
                if (inTransitQty.compareTo(BigDecimal.ZERO) < 0) {
                    inTransitQty = BigDecimal.ZERO;
                }

                bombypjRepository.updateShortInTransitDelvPickedQtyDelvDate(shortQty, inTransitQty,
                        delvQty, pickedQty, lastTranDate, userProfile.getCompanyCode(), userProfile.getPlantNo(),
                        grnDetail.getProjectNo(), grnDetail.getItemNo());

                isSuccess = true;

                if (vRecdQty.compareTo(BigDecimal.ZERO) == 0) {
                    break;
                }
            }

            /*********************************** update/delete the BOMBYPJ_DET ***********************************/

            if (!isSuccess) {
                // fail to update bombypj
                if (projectNo.equals(assemblyNo)) {
                    // Adv PO
                    if (detBalQty.compareTo(vRecdQty) < 0) {
                        accumRecdQty = accumRecdQty.add(detBalQty);
                        poResvQty = poResvQty.add(detBalQty);
                        detRecdQty = detBalQty;
                        itemProdnResv = detBalQty;
                        vRecdQty = vRecdQty.subtract(detBalQty);
                        detBalQty = BigDecimal.ZERO;
                    } else { // detBalQty >= recdQty
                        detBalQty = detBalQty.subtract(vRecdQty);
                        accumRecdQty = accumRecdQty.add(vRecdQty);
                        detRecdQty = vRecdQty;
                        itemProdnResv = vRecdQty;
                        poResvQty = poResvQty.add(vRecdQty);
                        vRecdQty = BigDecimal.ZERO;
                    }
                    throw new ServerException("" + bombypjDetCur.getProjectNo() + " has not being reserved! Qty "
                            + itemProdnResv + " of " + grnDetail.getItemNo() + " will be open for reservation.");
                } else {
                    throw new NotFoundException("No record found for " + grnDetail.getItemNo() + " of " + bombypjDetCur.getProjectNo() +
                            " Qty " + itemProdnResv + " of " + grnDetail.getItemNo() + " will be open for reservation.");
                }
            }

            String status = null;
            if (detResvQty.compareTo(accumRecdQty) == 0) {
                status = "C";
            }

            /**accumulate BOMBYPJ_DET recdQty when under same GRN No**/
            if (vGrnNo != null) {
                if (StringUtils.equals(vGrnNo, grnDetail.getGrnNo())) {
                    detRecdQty = detRecdQty.add(orgDetRecdQty);
                }
            }

            bombypjDetailRepository.updateAccRecdStatusGrnNo(accumRecdQty, detRecdQty, status, grnDetail.getGrnNo(),
                    userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDetail.getProjectNo(), input.getPoNo(), grnDetail.getItemNo());
            if (vRecdQty.compareTo(BigDecimal.ZERO) == 0) {
                break;
            }
        }

        PurDetProjection purDetInfo = purDetRepository.purDetInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(),
                grnDetail.getItemNo(), grnDetail.getProjectNo());
        DraftPurDetProjection dPurDetInfo = draftPurDetRepository.draftPurDetInfo(userProfile.getCompanyCode(),
                userProfile.getPlantNo(), grnDetail.getItemNo(), grnDetail.getProjectNo());

        /**update PO ResvQty**/
        BigDecimal resvQty = (purDetInfo.getResvQty() == null ? BigDecimal.ZERO : purDetInfo.getResvQty()).subtract(poResvQty);
        purDetRepository.updateResvQty(resvQty, userProfile.getCompanyCode(), userProfile.getPlantNo(),
                input.getPoNo(), grnDetail.getItemNo(), grnDetail.getProjectNo());

        BigDecimal resvQtyDP = (dPurDetInfo.getResvQty() == null ? BigDecimal.ZERO : dPurDetInfo.getResvQty()).subtract(poResvQty);
        draftPurDetRepository.updateResvQty(resvQtyDP, userProfile.getCompanyCode(), userProfile.getPlantNo(),
                input.getPoNo(), grnDetail.getItemNo(), grnDetail.getProjectNo());

        /** UPDATE ITEM & ITEMLOC PICKEDQTY **/
        BigDecimal pickedQtyItem = (itemInfo.getPickedQty() == null ? BigDecimal.ZERO : itemInfo.getPickedQty()).add(itemPickQty);
        BigDecimal prodnResvItem = (itemInfo.getProdnResv() == null ? BigDecimal.ZERO : itemInfo.getProdnResv()).subtract(itemProdnResv);
        itemRepository.updatePickedQtyProdnResv(pickedQtyItem, prodnResvItem, userProfile.getCompanyCode(),
                userProfile.getPlantNo(), grnDetail.getItemNo());

        BigDecimal pickedQtyLoc = (itemLocInfo.getPickedQty() == null ? BigDecimal.ZERO : itemLocInfo.getPickedQty()).add(itemPickQty);
        BigDecimal prodnResvLoc = (itemLocInfo.getProdnResv() == null ? BigDecimal.ZERO : itemLocInfo.getProdnResv()).subtract(itemProdnResv);
        itemLocRepository.updatePickedQtyProdnResv(pickedQtyLoc, prodnResvLoc, userProfile.getCompanyCode(),
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

        if (StringUtils.isBlank(input.getGrnNo())) {
            throw new ServerException("Grn No Can Not be Blank !");
        } else if (StringUtils.isBlank(input.getSubType())) {
            throw new ServerException("Sub Type Can Not be Blank !");
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
                GrnDetDTO grnDetail = GrnDetDTO.builder().build();
                Optional<List<ItemBatc>> foundDateCode = itemBatcRepository.findItemBatcByGrnNo(detail.getGrnNo());
                Integer dateCode = null;
                if (foundDateCode.isPresent()) {
                    for (ItemBatc itemBatc : foundDateCode.get()) {
                        dateCode = itemBatc.getDateCode();
                    }
                }
                if (StringUtils.equals(grn.getSubType(), "N")) {
                    PurDetProjection detailInfo = purDetRepository.getDataFromItemAndPartNo(detail.getCompanyCode(), detail.getPlantNo(), detail.getPoNo(),
                            detail.getItemNo(), detail.getPartNo(), detail.getPoRecSeq());
                    if (detailInfo != null) {
                        grnDetail = GrnDetDTO.builder()
                                .grnNo(detail.getGrnNo())
                                .subType(detail.getSubType())
                                .orderQty(detailInfo.getOrderQty())
                                .description(detailInfo.getDescription())
                                .dueDate(detailInfo.getDueDate())
                                .dateCode(dateCode)
                                .build();
                    }
                } else {
                    ItemProjection getItemInfo = itemRepository.itemInfo(grn.getCompanyCode(), grn.getPlantNo(), detail.getItemNo());
                    if (getItemInfo != null) {
                        grnDetail = GrnDetDTO.builder()
                                .grnNo(detail.getGrnNo())
                                .subType(detail.getSubType())
                                .itemType(detail.getItemType())
                                .description(getItemInfo.getDescription())
                                .dateCode(dateCode)
                                .build();
                    }
                }

                BeanUtils.copyProperties(detail, grnDetail);

                return grnDetail;
            }).collect(Collectors.toSet());
        }

        GrnDTO grnDTO = GrnDTO.builder().build();
        if (StringUtils.equals(grn.getSubType(), "N")) {
            PurProjection purInfo = purRepository.getPurInfo(grn.getCompanyCode(), grn.getPlantNo(), grn.getPoNo());
            GrnSupplierProjection supplierNameInfo = supplierRepository.getSupplierName(grn.getCompanyCode(), grn.getPlantNo(), grn.getSupplierCode());
            if (purInfo != null && supplierNameInfo != null) {
                grnDTO = GrnDTO.builder()
                        .poRemarks(purInfo.getRemarks())
                        .buyer(purInfo.getBuyer())
                        .supplierName(supplierNameInfo.getName()).build();
            }
        } else {
            Optional<List<MSRDetail>> foundMSR = msrDetailRepository.getMSRDetailByGrnNo(grn.getGrnNo());
            String msrNo = null;
            if (foundMSR.isPresent()) {
                for (MSRDetail msrDetail : foundMSR.get()) {
                    msrNo = msrDetail.getGrnNo();
                }
            }
            grnDTO = GrnDTO.builder().msrNo(msrNo).build();
        }

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
