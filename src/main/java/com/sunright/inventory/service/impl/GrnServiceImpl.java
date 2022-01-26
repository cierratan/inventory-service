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
import com.sunright.inventory.entity.Item;
import com.sunright.inventory.entity.bombypj.Bombypj;
import com.sunright.inventory.entity.docmno.DocmNoProjection;
import com.sunright.inventory.entity.draftpur.DraftPur;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.grn.Grn;
import com.sunright.inventory.entity.grn.GrnDet;
import com.sunright.inventory.entity.msr.MSR;
import com.sunright.inventory.entity.msr.MSRDetail;
import com.sunright.inventory.entity.pur.Pur;
import com.sunright.inventory.entity.pur.PurDet;
import com.sunright.inventory.entity.supplier.Supplier;
import com.sunright.inventory.entity.supplier.SupplierProjection;
import com.sunright.inventory.exception.NotFoundException;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.*;
import com.sunright.inventory.repository.lov.DefaultCodeDetailRepository;
import com.sunright.inventory.service.GrnService;
import com.sunright.inventory.util.QueryGenerator;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
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

        UserProfile userProfile = UserProfileContext.getUserProfile();
        List<GrnDTO> list = new ArrayList<>();
        List<Pur> getAllPONo = purRepository.getAllPoNo(userProfile.getCompanyCode(), userProfile.getPlantNo());
        for (Pur data : getAllPONo) {
            GrnDTO dto = GrnDTO.builder().build();
            dto.setPoNo(data.getId().getPoNo());
            list.add(dto);
        }
        return list;
    }

    @Override
    public GrnDTO getGrnHeader(String poNo) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String date = sdf.format(new Date());
        UserProfile userProfile = UserProfileContext.getUserProfile();
        GrnDTO grnDTO = GrnDTO.builder().build();
        checkStatusPoNo(poNo, userProfile);
        DocmNoDTO prefixGrnNo = getLastGeneratedNoforGRN(userProfile);
        List<PurDTO> purInfoList = getPurInfo(poNo, userProfile);
        grnDTO.setGrnNo(prefixGrnNo.getPrefix());
        if (grnDTO.getGrnNo() == null) {
            grnDTO.setMessage("Grn No Can Not be Blank !");
        } else {
            for (PurDTO dtoPur : purInfoList) {
                grnDTO.setSupplierCode(dtoPur.getSupplierCode());
                grnDTO.setCurrencyCode(dtoPur.getCurrencyCode());
                grnDTO.setCurrencyRate(dtoPur.getCurrencyRate());
                grnDTO.setBuyer(dtoPur.getBuyer());
                grnDTO.setRlseDate(dtoPur.getRlseDate());
                grnDTO.setPoRemarks(dtoPur.getRemarks());
                try {
                    grnDTO.setRecdDate(sdf.parse(date));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                SupplierDTO supName = supplierName(grnDTO.getSupplierCode(), userProfile);
                grnDTO.setSupplierName(supName.getName());
                if (grnDTO.getSupplierName() == null) {
                    grnDTO.setMessage("Supplier Code not found");
                }
            }
        }
        return grnDTO;
    }

    private DocmNoDTO getLastGeneratedNoforGRN(UserProfile userProfile) {

        DocmNoDTO dto = DocmNoDTO.builder().build();
        DocmNoProjection docmNo = docmNoRepository.getLastGeneratedNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), "N", "GRN");
        dto.setPrefix(docmNo.getGeneratedNo());
        dto.setLastGeneratedNo(docmNo.getDocmNo());

        return dto;
    }

    private GrnDetDTO checkStatusPoNo(String poNo, UserProfile userProfile) {

        GrnDetDTO detDTO = GrnDetDTO.builder().build();
        List<Pur> pur = purRepository.checkStatusPoNoPur(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo);
        List<DraftPur> draftPur = draftPurRepository.checkStatusPoNoDraftPur(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo);
        for (DraftPur dataDraftPur : draftPur) {
            PurDTO dto = PurDTO.builder().build();
            dto.setPoNo(dataDraftPur.getId().getPoNo());
            dto.setOpenClose(dataDraftPur.getOpenClose());
            if (dto.getOpenClose().equalsIgnoreCase("C")) {
                detDTO.setMessage("PO already Closed, Purchase Receipt not allowed.");
            } else if (!dto.getOpenClose().equalsIgnoreCase("A")) {
                detDTO.setMessage("PO is yet to be Approved, Purchase Receipt not allowed.");
            } else if (dto.getOpenClose().equalsIgnoreCase("V")) {
                detDTO.setMessage("PO already Voided, Purchase Receipt not allowed.");
            } else if (dto.getPoNo() == null) {
                detDTO.setMessage("Invalid PO No!");
            } else {
                if (dto.getPoNo() != null && dto.getOpenClose() != null) {
                    for (Pur data : pur) {
                        dto.setPoNo(data.getId().getPoNo());
                        dto.setOpenClose(data.getOpenClose());
                        if (dto.getOpenClose().equalsIgnoreCase("C")) {
                            detDTO.setMessage("PO already Closed, Purchase Receipt not allowed.");
                        } else if (!dto.getOpenClose().equalsIgnoreCase("A")) {
                            detDTO.setMessage("PO is yet to be Approved, Purchase Receipt not allowed.");
                        }
                    }
                }
            }
        }

        return detDTO;
    }

    private List<PurDTO> getPurInfo(String poNo, UserProfile userProfile) {

        List<PurDTO> list = new ArrayList<>();
        List<Pur> purInfo = purRepository.getPurInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo);
        for (Pur data : purInfo) {
            PurDTO dto = PurDTO.builder().build();
            dto.setSupplierCode(data.getSupplierCode());
            dto.setCurrencyCode(data.getCurrencyCode());
            dto.setCurrencyRate(data.getCurrencyRate());
            dto.setBuyer(data.getBuyer());
            dto.setRlseDate(data.getRlseDate());
            dto.setRemarks(data.getRemarks());
            list.add(dto);
        }

        return list;
    }

    private SupplierDTO supplierName(String supplierCode, UserProfile userProfile) {

        SupplierDTO dto = SupplierDTO.builder().build();
        Supplier supName = supplierRepository.getSupplierName(userProfile.getCompanyCode(), userProfile.getPlantNo(), supplierCode);
        dto.setName(supName.getName());
        return dto;
    }

    @Override
    public List<GrnDetDTO> getAllPartNo(String poNo, String partNo, String itemNo) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        List<GrnDetDTO> list = new ArrayList<>();
        List<PurDet> getDataFromPartNo = purRepository.getDataFromPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo, partNo, itemNo);
        for (PurDet data : getDataFromPartNo) {
            GrnDetDTO dto = GrnDetDTO.builder().build();
            dto.setSeqNo(data.getSeqNo());
            dto.setPartNo(data.getPartNo());
            dto.setItemNo(data.getItemNo());
            dto.setPoRecSeq(data.getSeqNo());
            list.add(dto);
        }

        return list;
    }

    @Override
    public GrnDetDTO getGrnDetail(String poNo, String itemNo, String partNo, Integer poRecSeq) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        GrnDetDTO grnDetDTO = GrnDetDTO.builder().build();
        List<PurDetDTO> getDataDetail = getDetailInfo(poNo, itemNo, partNo, poRecSeq, userProfile);

        if (partNo != null) {
            checkDataFromPartNo(poNo, partNo, poRecSeq, userProfile);
        } else {
            checkDataFromItemNo(poNo, itemNo, userProfile);
        }

        if (partNo == null && itemNo == null) {
            checkDataFromPartNo(poNo, partNo, poRecSeq, userProfile);
        }

        for (PurDetDTO dto : getDataDetail) {
            grnDetDTO.setPoRecSeq(dto.getRecSeq());
            grnDetDTO.setItemNo(dto.getItemNo());
            grnDetDTO.setPartNo(dto.getPartNo());
            grnDetDTO.setLoc(dto.getLoc());
            grnDetDTO.setItemType(dto.getItemType());
            grnDetDTO.setProjectNo(dto.getProjectNo());
            grnDetDTO.setPoNo(dto.getPoNo());
            grnDetDTO.setDescription(dto.getDescription());
            grnDetDTO.setMslCode(dto.getMslCode());
            String codeDesc = defaultCodeDetailRepository.findCodeDescBy(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getUom());
            grnDetDTO.setUom(codeDesc);
            grnDetDTO.setOrderQty(dto.getOrderQty());
            grnDetDTO.setPoPrice(dto.getUnitPrice());
            grnDetDTO.setDueDate(dto.getDueDate());
            grnDetDTO.setResvQty(dto.getResvQty());
            grnDetDTO.setInvUom(dto.getInvUom());
            grnDetDTO.setStdPackQty(dto.getStdPackQty());
            grnDetDTO.setRemarks(dto.getRemarks());
        }

        return grnDetDTO;
    }

    @Override
    public GrnDTO getDefaultValueForGrnManual() {

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String date = sdf.format(new Date());
        UserProfile userProfile = UserProfileContext.getUserProfile();
        GrnDTO dto = GrnDTO.builder().build();
        DocmNoProjection docmNo = docmNoRepository.getLastGeneratedNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), "M", "GRN");
        if (docmNo == null) {
            dto.setMessage("Not found in DOCM_NO table for type GRN-M !");
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

        UserProfile userProfile = UserProfileContext.getUserProfile();
        GrnDTO dto = GrnDTO.builder().build();
        Optional<Grn> grnOptional = grnRepository.findGrnByGrnNo(grnNo);
        DocmNoProjection docmNo = docmNoRepository.getLastGeneratedNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), "M", "GRN");
        dto.setGrnNo(docmNo.getGeneratedNo());
        if (grnOptional.isPresent()) {
            if (grnOptional.get().getGrnNo().equals(dto.getGrnNo())) {
                dto.setMessage("GRN Record exists ! New GRN No: " + dto.getGrnNo() + " is being assigned !");
            }
        }
        return dto;
    }

    @Override
    public GrnDTO checkIfMsrNoValid(String msrNo) {

        GrnDTO dto = GrnDTO.builder().build();
        Optional<MSR> msrOptional = msrRepository.findMSRByMsrNo(msrNo);
        if (!msrOptional.isPresent()) {
            dto.setMessage("Invalid MSR No");
        } else {
            dto.setMsrNo(msrOptional.get().getMsrNo());
        }
        return dto;
    }

    @Override
    public GrnDetDTO checkNextItem(GrnDTO input) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        GrnDetDTO dto = GrnDetDTO.builder().build();
        long countMsrItemNo;
        long countMsrPartNo;
        long countItemNo;
        long countPartNo;
        String msrNo = input.getMsrNo();
        String itemNo = input.getGrnDetails().stream().map(GrnDetDTO::getItemNo).toString();
        String partNo = input.getGrnDetails().stream().map(GrnDetDTO::getPartNo).toString();
        Integer itemType = input.getGrnDetails().stream().mapToInt(GrnDetDTO::getItemType).sum();
        String projectNo = input.getGrnDetails().stream().map(GrnDetDTO::getProjectNo).toString();
        String poNo = input.getGrnDetails().stream().map(GrnDetDTO::getPoNo).toString();
        BigDecimal recdPrice = input.getGrnDetails().stream().map(GrnDetDTO::getRecdPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal recdQty = input.getGrnDetails().stream().map(GrnDetDTO::getRecdQty).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal retnQty = input.getGrnDetails().stream().map(GrnDetDTO::getRetnQty).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal labelQty = input.getGrnDetails().stream().map(GrnDetDTO::getLabelQty).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (itemType == null) {
            checkItemType(dto);
        }

        if (itemNo == null) {
            checkItemNo(dto);
        }

        if (itemType != null) {
            if (!Integer.toString(itemType).contains("0") && !Integer.toString(itemType).contains("1")) {
                checkItemTypeNotNull(dto);
            }
        }

        if (msrNo != null) {
            if (itemNo != null) {
                Long countByItemNo = msrDetailRepository.countMsrByItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), msrNo, itemNo);

                countMsrItemNo = countByItemNo;

                if (countMsrItemNo == 0) {
                    checkMsrItemNo(dto);
                } else if (countMsrItemNo > 1) {
                    List<MSRDetail> lovPartNo = msrDetailRepository.showLovPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), msrNo, partNo, itemNo);
                    for (MSRDetail data : lovPartNo) {
                        dto.setSeqNo(data.getSeqNo());
                        dto.setPartNo(data.getPartNo());
                        dto.setItemNo(data.getItemNo());
                    }
                    List<MSRDetail> objects = msrDetailRepository.itemInfo(userProfile.getCompanyCode(),
                            userProfile.getPlantNo(), msrNo, dto.getPartNo(), dto.getItemNo(), dto.getSeqNo());
                    for (MSRDetail data : objects) {
                        dto.setPartNo(data.getPartNo());
                        dto.setSeqNo(data.getSeqNo());
                        dto.setItemNo(data.getItemNo());
                        dto.setDescription(data.getRemarks());
                        dto.setMslCode(null);
                        dto.setItemType(data.getItemType());
                        dto.setLoc(data.getLoc());
                        dto.setUom(data.getUom());
                        dto.setProjectNo(data.getProjectNo());
                        dto.setGrnNo(data.getGrnNo());
                        dto.setRetnQty(data.getRetnQty());
                        dto.setRetnPrice(data.getRetnPrice());
                    }
                }

                Long countByPartNo = msrDetailRepository.countMsrByPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), msrNo, partNo);
                countMsrPartNo = countByPartNo;
                if (countMsrPartNo == 0) {
                    checkMsrPartNo(dto);
                } else if (countMsrPartNo > 1) {
                    List<MSRDetail> lovPartNo = msrDetailRepository.showLovPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), msrNo, partNo, itemNo);
                    for (MSRDetail data : lovPartNo) {
                        dto.setSeqNo(data.getSeqNo());
                        dto.setPartNo(data.getPartNo());
                        dto.setItemNo(data.getItemNo());
                    }
                    List<MSRDetail> objects = msrDetailRepository.itemInfo(userProfile.getCompanyCode(),
                            userProfile.getPlantNo(), msrNo, dto.getPartNo(), dto.getItemNo(), dto.getSeqNo());
                    for (MSRDetail data : objects) {
                        dto.setPartNo(data.getPartNo());
                        dto.setSeqNo(data.getSeqNo());
                        dto.setItemNo(data.getItemNo());
                        dto.setDescription(data.getRemarks());
                        dto.setMslCode(null);
                        dto.setItemType(data.getItemType());
                        dto.setLoc(data.getLoc());
                        dto.setUom(data.getUom());
                        dto.setProjectNo(data.getProjectNo());
                        dto.setGrnNo(data.getGrnNo());
                        dto.setRetnQty(data.getRetnQty());
                        dto.setRetnPrice(data.getRetnPrice());
                    }
                }

                if (!(countMsrItemNo == 0 && countMsrItemNo > 1 && countMsrPartNo == 0 && countMsrPartNo > 1)) {
                    List<MSRDetail> objects = msrDetailRepository.itemInfo(userProfile.getCompanyCode(),
                            userProfile.getPlantNo(), msrNo, partNo, itemNo, dto.getSeqNo());
                    for (MSRDetail data : objects) {
                        dto.setPartNo(data.getPartNo());
                        dto.setSeqNo(data.getSeqNo());
                        dto.setItemNo(data.getItemNo());
                        dto.setDescription(data.getRemarks());
                        dto.setMslCode(null);
                        dto.setItemType(data.getItemType());
                        dto.setLoc(data.getLoc());
                        dto.setUom(data.getUom());
                        dto.setProjectNo(data.getProjectNo());
                        dto.setGrnNo(data.getGrnNo());
                        dto.setRetnQty(data.getRetnQty());
                        dto.setRetnPrice(data.getRetnPrice());
                        Optional<Grn> optionalGrn = grnRepository.findGrnByGrnNo(dto.getGrnNo());
                        if (optionalGrn.isPresent()) {
                            dto.setPoNo(optionalGrn.get().getPoNo());
                        }
                    }
                }
            }
        }

        if (itemType != null) {
            if (itemType == 0) {
                if (itemNo == null) {
                    checkItemNo(dto);
                } else {
                    if (itemNo != null) {
                        Long countsItemNo = itemRepository.countByItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo);
                        countItemNo = countsItemNo;
                        if (countItemNo == 0) {
                            checkValidItemNo(dto);
                        } else if (countItemNo > 1) {
                            List<Item> lovItemPart = itemRepository.lovItemPart(userProfile.getCompanyCode(), userProfile.getPlantNo(), partNo, itemNo);
                            for (Item data : lovItemPart) {
                                dto.setPartNo(data.getPartNo());
                                dto.setItemNo(data.getItemNo());
                            }
                            List<Item> itemInfo = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getItemNo());
                            for (Item data : itemInfo) {
                                dto.setPartNo(data.getPartNo());
                                dto.setItemNo(data.getItemNo());
                                dto.setDescription(data.getDescription());
                                dto.setLoc(data.getLoc());
                                dto.setUom(data.getUom());
                            }
                        } else {
                            List<Item> byItemNo = itemRepository.getItemAndPartNoByItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo);
                            for (Item data : byItemNo) {
                                dto.setItemNo(data.getItemNo());
                                dto.setPartNo(data.getPartNo());
                            }
                        }

                        Long countsPartNo = itemRepository.countByPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getPartNo());
                        countPartNo = countsPartNo;
                        if (countPartNo == 0) {
                            checkValidPartNo(dto);
                        } else if (countPartNo > 1) {
                            List<Item> lovItemPart = itemRepository.lovItemPart(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getPartNo(), dto.getItemNo());
                            for (Item data : lovItemPart) {
                                dto.setPartNo(data.getPartNo());
                                dto.setItemNo(data.getItemNo());
                            }
                            List<Item> itemInfo = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getItemNo());
                            for (Item data : itemInfo) {
                                dto.setPartNo(data.getPartNo());
                                dto.setItemNo(data.getItemNo());
                                dto.setDescription(data.getDescription());
                                dto.setLoc(data.getLoc());
                                dto.setUom(data.getUom());
                            }
                        } else {
                            List<Item> byPartNo = itemRepository.getItemAndPartNoByPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getPartNo());
                            for (Item data : byPartNo) {
                                dto.setItemNo(data.getItemNo());
                                dto.setPartNo(data.getPartNo());
                            }
                        }

                        List<Item> itemInfo = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getItemNo());
                        for (Item data : itemInfo) {
                            dto.setPartNo(data.getPartNo());
                            dto.setItemNo(data.getItemNo());
                            dto.setDescription(data.getDescription());
                            dto.setLoc(data.getLoc());
                            dto.setUom(data.getUom());
                        }
                    }

                    if (projectNo != null) {
                        Bombypj objects = bombypjRepository.getPrjNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), projectNo);
                        dto.setProjectNo(objects.getId().getProjectNo());
                        if (dto.getProjectNo() == null) {
                            checkProjectNoIfNull(dto);
                        } else if (itemType == 0) {
                            Bombypj bombypj = bombypjRepository.getAltrnt(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getProjectNo(), itemNo);
                            dto.setItemNo(bombypj.getId().getAlternate());
                            if (dto.getItemNo() == null) {
                                checkItemNoInProject(dto);
                            }
                        }
                    }

                    if (poNo != null) {
                        List<PurDet> objects = purRepository.getPoNoAndRecSeq(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemType, itemNo, partNo, poNo);
                        for (PurDet data : objects) {
                            dto.setPoNo(data.getId().getPoNo());
                            dto.setSeqNo(data.getId().getRecSeq());
                        }
                        if (dto.getPoNo() == null) {
                            checkValidPoNo(dto);
                        } else if (dto.getSeqNo() == null) {
                            checkItemNotInPo(dto);
                        }
                    }

                    if (recdPrice != null) {
                        if (recdPrice.intValue() < 0) {
                            checkValidRecdPrice(dto);
                        } else if (itemNo != null) {
                            String source = null;
                            Item objects = itemRepository.getSource(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo);
                            source = objects.getSource();
                            if (source == null) {
                                checkSourceStockItem(dto);
                            }

                            if (source.equals("C")) {
                                if (recdPrice.intValue() > 0) {
                                    checkValidRecdPriceForConsignedItem(dto);
                                }
                            }
                        }
                    }

                    if (recdQty != null) {
                        if (recdQty.intValue() <= 0) {
                            checkRecdQty(dto);
                        } else if (retnQty.intValue() > 0 && recdQty.intValue() > retnQty.intValue()) {
                            checkRecdRetnQty(dto, retnQty);
                        }
                    }

                    if (labelQty != null) {
                        if (labelQty.intValue() <= 0) {
                            checkLabelQty(dto);
                        } else if (recdQty.intValue() < labelQty.intValue()) {
                            checkRecdLabelQty(dto, recdQty);
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

    private JasperPrint exportPdf(String grnNo, String subType, String type) throws SQLException, FileNotFoundException, JRException {
        Connection con = dataSource.getConnection();
        File file = ResourceUtils.getFile("classpath:detail.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("GRN_NO", grnNo);
        parameters.put("SUB_TYPE", subType);
        parameters.put("TYPE", type);
        return JasperFillManager.fillReport(jasperReport, parameters, con);
    }

    @Override
    public void generateReportGrn(HttpServletResponse response, String grnNo, String subType, String type) throws IOException, SQLException, JRException {
        String filename = "" + grnNo + ".pdf";
        response.setContentType("application/x-download");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        OutputStream out = response.getOutputStream();
        JasperPrint jasperPrint = exportPdf(grnNo, subType, type);
        JasperExportManager.exportReportToPdfStream(jasperPrint, out);
    }

    private void checkItemTypeNotNull(GrnDetDTO dto) {
        dto.setMessage("Please enter 0-Stock, 1-Non Stock !");
    }

    private void checkValidRecdPrice(GrnDetDTO dto) {
        dto.setMessage("Invalid Recd Price");
    }

    private void checkRecdLabelQty(GrnDetDTO dto, BigDecimal recdQty) {
        dto.setMessage("Received Qty only " + recdQty + " units !");
    }

    private void checkLabelQty(GrnDetDTO dto) {
        dto.setMessage("Qty/Label MUST be > 0 !");
    }

    private void checkRecdRetnQty(GrnDetDTO dto, BigDecimal retnQty) {
        dto.setMessage("Return Qty only " + retnQty + " units !");
    }

    private void checkRecdQty(GrnDetDTO dto) {
        dto.setMessage("Received Qty MUST be > 0 !");
    }

    private void checkValidRecdPriceForConsignedItem(GrnDetDTO dto) {
        dto.setMessage("Invalid Recd Price for Consigned Item");
    }

    private void checkSourceStockItem(GrnDetDTO dto) {
        dto.setMessage("Unknown source for Stock Item");
    }

    private void checkItemNotInPo(GrnDetDTO dto) {
        dto.setMessage("Item not in PO");
    }

    private void checkValidPoNo(GrnDetDTO dto) {
        dto.setMessage("PO No is Invalid / Not found in Master File !");
    }

    private void checkItemNoInProject(GrnDetDTO dto) {
        dto.setMessage("Item not found in project");
    }

    private void checkProjectNoIfNull(GrnDetDTO dto) {
        dto.setMessage("Project No is Invalid / Not found in Master File !");
    }

    private void checkValidPartNo(GrnDetDTO dto) {
        dto.setMessage("The Part No is invalid!");
    }

    private void checkValidItemNo(GrnDetDTO dto) {
        dto.setMessage("The Item No is invalid!");
    }

    private void checkMsrPartNo(GrnDetDTO dto) {
        dto.setMessage("The Part No is either invalid or qty fully received!");
    }

    private void checkMsrItemNo(GrnDetDTO dto) {
        dto.setMessage("The Item No is either invalid or qty fully received!");
    }

    private void checkItemType(GrnDetDTO dto) {
        dto.setMessage("Item Type Can Not be Blank !");
    }

    private void checkItemNo(GrnDetDTO dto) {
        dto.setMessage("Item No Can Not be Blank !");
    }

    private GrnDetDTO checkDataFromItemNo(String poNo, String itemNo, UserProfile userProfile) {

        Long counts = purRepository.countItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo, itemNo);
        GrnDetDTO detDTO = GrnDetDTO.builder().build();
        if (counts == 0) {
            detDTO.setMessage("The Part No is either invalid or qty fully received!");
        }
        return detDTO;
    }

    private GrnDetDTO checkDataFromPartNo(String poNo, String partNo, Integer poRecSeq, UserProfile userProfile) {

        int recSeq = 0;
        String partno = "";
        Long counts = purRepository.countPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo, partNo);
        List<PurDet> checkDuplicatePartNo = purRepository.checkDuplicatePartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo, partNo, poRecSeq);
        GrnDetDTO detDTO = GrnDetDTO.builder().build();
        if (counts == 0) {
            detDTO.setMessage("The Part No is either invalid or qty fully received!");
        }
        for (PurDet data : checkDuplicatePartNo) {
            PurDetDTO dto = PurDetDTO.builder().build();
            dto.setRecSeq(data.getSeqNo());
            dto.setPartNo(data.getPartNo());
            partno = dto.getPartNo();
            recSeq = dto.getRecSeq();
        }
        if (partNo.equals(partno) & poRecSeq == recSeq) {
            detDTO.setMessage("Duplicate Part No found!'");
        }
        return detDTO;
    }

    private List<PurDetDTO> getDetailInfo(String poNo, String itemNo, String partNo, Integer poRecSeq, UserProfile userProfile) {

        List<PurDetDTO> list = new ArrayList<>();
        List<PurDet> detailInfo = purRepository.getDataFromItemAndPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo,
                itemNo, partNo, poRecSeq);
        for (PurDet data : detailInfo) {
            PurDetDTO dto = PurDetDTO.builder().build();
            dto.setPartNo(data.getPartNo());
            dto.setRecSeq(data.getId().getRecSeq());
            dto.setItemNo(data.getItemNo());
            dto.setDescription(data.getRemarks());
            dto.setMslCode(null);
            dto.setItemType(data.getItemType());
            dto.setLoc(data.getLoc());
            dto.setUom(data.getUom());
            dto.setProjectNo(data.getProjectNo());
            dto.setOrderQty(data.getOrderQty());
            dto.setUnitPrice(data.getUnitPrice());
            dto.setDueDate(data.getDueDate());
            dto.setResvQty(data.getResvQty());
            dto.setInvUom(data.getInvUom());
            dto.setStdPackQty(data.getStdPackQty());
            dto.setRemarks(data.getRemarks());
            list.add(dto);
        }
        return list;
    }

    @Override
    public GrnDTO createGrn(GrnDTO input) {
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
        populateAfterSaving(input, saved);
        postSaving(userProfile, input);

        if (!CollectionUtils.isEmpty(input.getGrnDetails())) {
            for (GrnDetDTO detail : input.getGrnDetails()) {
                GrnDet grnDetail = new GrnDet();
                BeanUtils.copyProperties(detail, grnDetail);
                grnDetail.setCompanyCode(userProfile.getCompanyCode());
                grnDetail.setPlantNo(userProfile.getPlantNo());
                grnDetail.setGrn(saved);

                grnDetRepository.save(grnDetail);
            }
        }

        return input;
    }

    private void populateAfterSaving(GrnDTO input, Grn saved) {
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

    @Override
    public SupplierDTO findSupplierByGrnNo(String grnNo) {
        SupplierProjection supplier = supplierRepository.getSupplierByGrn(UserProfileContext.getUserProfile().getCompanyCode(),
                UserProfileContext.getUserProfile().getPlantNo(), grnNo);

        if(supplier == null) {
            throw new NotFoundException(String.format("GrnNo: %s is not found", grnNo));
        }

        return SupplierDTO.builder()
                .supplierCode(supplier.getSupplierCode())
                .name(supplier.getName())
                .build();
    }
}
