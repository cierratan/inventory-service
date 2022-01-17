package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.docmno.DocmNoDTO;
import com.sunright.inventory.dto.grn.GrnDTO;
import com.sunright.inventory.dto.grn.GrnDetDTO;
import com.sunright.inventory.dto.msr.MsrDTO;
import com.sunright.inventory.dto.pur.PurDTO;
import com.sunright.inventory.dto.pur.PurDetDTO;
import com.sunright.inventory.dto.search.Filter;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.dto.supplier.SupplierDTO;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.grn.Grn;
import com.sunright.inventory.entity.grn.GrnDet;
import com.sunright.inventory.entity.grn.GrnId;
import com.sunright.inventory.entity.msr.MSR;
import com.sunright.inventory.exception.NotFoundException;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.*;
import com.sunright.inventory.repository.lov.DefaultCodeDetailRepository;
import com.sunright.inventory.service.GrnService;
import com.sunright.inventory.util.QueryGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;

@Transactional
@Service
@Slf4j
public class GrnServiceImpl implements GrnService {

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
        List<Object[]> getAllPONo = purRepository.getAllPoNo(userProfile.getCompanyCode(), userProfile.getPlantNo());
        for (Object[] data : getAllPONo) {
            GrnDTO dto = GrnDTO.builder().build();
            dto.setPoNo((String) data[0]);
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
            grnDTO.setMessage("Grn No. must not be empty");
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
        List<Object[]> generatedNoforGRN = docmNoRepository.getLastGeneratedNoforGRN(userProfile.getCompanyCode(), userProfile.getPlantNo());
        for (Object[] data : generatedNoforGRN) {
            BigDecimal d = new BigDecimal(String.valueOf(data[1]));
            dto.setPrefix((String) data[0]);
            dto.setLastGeneratedNo(d.intValue());
        }
        return dto;
    }

    private GrnDetDTO checkStatusPoNo(String poNo, UserProfile userProfile) {

        GrnDetDTO detDTO = GrnDetDTO.builder().build();
        List<Object[]> pur = purRepository.checkStatusPoNoPur(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo);
        List<Object[]> draftPur = draftPurRepository.checkStatusPoNoDraftPur(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo);
        for (Object[] dataDraftPur : draftPur) {
            PurDTO dto = PurDTO.builder().build();
            dto.setPoNo((String) dataDraftPur[0]);
            dto.setOpenClose((String) dataDraftPur[1]);
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
                    for (Object[] data : pur) {
                        dto.setPoNo((String) data[0]);
                        dto.setOpenClose((String) data[1]);
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
        List<Object[]> purInfo = purRepository.getPurInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo);
        for (Object[] data : purInfo) {
            PurDTO dto = PurDTO.builder().build();
            dto.setSupplierCode((String) data[0]);
            dto.setCurrencyCode((String) data[1]);
            dto.setCurrencyRate((BigDecimal) data[2]);
            dto.setBuyer((String) data[3]);
            dto.setRlseDate((Date) data[4]);
            dto.setRemarks((String) data[5]);
            list.add(dto);
        }

        return list;
    }

    private SupplierDTO supplierName(String supplierCode, UserProfile userProfile) {

        SupplierDTO dto = SupplierDTO.builder().build();
        List<Object[]> supplierNameInfo = supplierRepository.getSupplierName(userProfile.getCompanyCode(), userProfile.getPlantNo(), supplierCode);
        for (Object[] data : supplierNameInfo) {
            dto.setName((String) data[0]);
        }
        return dto;
    }

    @Override
    public List<GrnDetDTO> getAllPartNo(String poNo) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        List<GrnDetDTO> list = new ArrayList<>();
        List<Object[]> getDataFromPartNo = purRepository.getDataFromPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo);
        for (Object[] data : getDataFromPartNo) {
            GrnDetDTO dto = GrnDetDTO.builder().build();
            BigDecimal seqNo = new BigDecimal(String.valueOf(data[0]));
            dto.setSeqNo(seqNo.intValue());
            dto.setPartNo((String) data[1]);
            dto.setItemNo((String) data[2]);
            BigDecimal recSeq = new BigDecimal(String.valueOf(data[3]));
            dto.setPoRecSeq(recSeq.intValue());
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
        List<Object[]> prefix = docmNoRepository.getLastGeneratedNoforGRNManual(userProfile.getCompanyCode(), userProfile.getPlantNo());
        if (prefix == null || prefix.size() == 0) {
            dto.setMessage("Not found in DOCM_NO table for type GRN-M !");
        } else {
            for (Object[] data : prefix) {
                dto.setGrnNo((String) data[0]);
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
        }
        return dto;
    }

    @Override
    public GrnDTO checkIfGrnExists(String grnNo) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        GrnDTO dto = GrnDTO.builder().build();
        Optional<Grn> grnOptional = grnRepository.findGrnByIdsGrnNo(grnNo);
        List<Object[]> prefix = docmNoRepository.getLastGeneratedNoforGRNManual(userProfile.getCompanyCode(), userProfile.getPlantNo());
        for (Object[] data : prefix) {
            dto.setGrnNo((String) data[0]);
        }
        if(grnOptional.isPresent()){
            if (grnOptional.get().getIds().getGrnNo().equals(dto.getGrnNo())) {
                dto.setMessage("GRN Record exists ! New GRN No: " + dto.getGrnNo() + " is being assigned !");
            }
        }
        return dto;
    }

    @Override
    public MsrDTO checkIfMsrNoValid(String msrNo) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        MsrDTO dto = MsrDTO.builder().build();
        Optional<MSR> msrOptional = msrRepository.findMSRByCompanyCodeAndPlantNoAndMsrNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), msrNo);
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
        int countMsrItemNo = 0;
        int countMsrPartNo = 0;
        int countItemNo = 0;
        int countPartNo = 0;
        String msrNo = input.getMsrNo();
        for (int i = 0; i < input.getGrnDetList().size(); i++) {

            String itemNo = input.getGrnDetList().get(i).getItemNo();
            String partNo = input.getGrnDetList().get(i).getPartNo();
            Integer itemType = input.getGrnDetList().get(i).getItemType();
            String projectNo = input.getGrnDetList().get(i).getProjectNo();
            String poNo = input.getGrnDetList().get(i).getPoNo();
            BigDecimal recdPrice = input.getGrnDetList().get(i).getRecdPrice();
            BigDecimal recdQty = input.getGrnDetList().get(i).getRecdQty();
            BigDecimal retnQty = input.getGrnDetList().get(i).getRetnQty();
            BigDecimal labelQty = input.getGrnDetList().get(i).getLabelQty();

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
                    List<Object[]> countByItemNo = msrRepository.getCountMsrByItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), msrNo, itemNo);
                    for (Object[] data : countByItemNo) {
                        BigDecimal count = new BigDecimal(String.valueOf(data[0]));
                        countMsrItemNo = count.intValue();
                    }
                    if (countMsrItemNo == 0) {
                        checkMsrItemNo(dto);
                    } else if (countMsrItemNo > 1) {
                        List<Object[]> lovPartNo = msrRepository.showLovPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), msrNo, partNo, itemNo);
                        for (Object[] data : lovPartNo) {
                            dto.setSeqNo((Integer) data[0]);
                            dto.setPartNo((String) data[1]);
                            dto.setItemNo((String) data[2]);
                        }
                        List<Object[]> objects = msrRepository.itemInfo(userProfile.getCompanyCode(),
                                userProfile.getPlantNo(), msrNo, dto.getPartNo(), dto.getItemNo(), dto.getSeqNo());
                        for (Object[] data : objects) {
                            dto.setPartNo((String) data[0]);
                            dto.setSeqNo((Integer) data[1]);
                            dto.setItemNo((String) data[2]);
                            dto.setDescription((String) data[3]);
                            dto.setMslCode((String) data[4]);
                            dto.setItemType((Integer) data[5]);
                            dto.setLoc((String) data[6]);
                            dto.setUom((String) data[7]);
                            dto.setProjectNo((String) data[8]);
                            dto.setGrnNo((String) data[9]);
                            dto.setRetnQty((BigDecimal) data[10]);
                            dto.setRetnPrice((BigDecimal) data[11]);
                        }
                    }

                    List<Object[]> countByPartNo = msrRepository.getCountMsrByPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), msrNo, partNo);
                    for (Object[] data : countByPartNo) {
                        BigDecimal count = new BigDecimal(String.valueOf(data[0]));
                        countMsrPartNo = count.intValue();
                    }
                    if (countMsrPartNo == 0) {
                        checkMsrPartNo(dto);
                    } else if (countMsrPartNo > 1) {
                        List<Object[]> lovPartNo = msrRepository.showLovPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), msrNo, partNo, itemNo);
                        for (Object[] data : lovPartNo) {
                            dto.setSeqNo((Integer) data[0]);
                            dto.setPartNo((String) data[1]);
                            dto.setItemNo((String) data[2]);
                        }
                        List<Object[]> objects = msrRepository.itemInfo(userProfile.getCompanyCode(),
                                userProfile.getPlantNo(), msrNo, dto.getPartNo(), dto.getItemNo(), dto.getSeqNo());
                        for (Object[] data : objects) {
                            dto.setPartNo((String) data[0]);
                            dto.setSeqNo((Integer) data[1]);
                            dto.setItemNo((String) data[2]);
                            dto.setDescription((String) data[3]);
                            dto.setMslCode((String) data[4]);
                            dto.setItemType((Integer) data[5]);
                            dto.setLoc((String) data[6]);
                            dto.setUom((String) data[7]);
                            dto.setProjectNo((String) data[8]);
                            dto.setGrnNo((String) data[9]);
                            dto.setRetnQty((BigDecimal) data[10]);
                            dto.setRetnPrice((BigDecimal) data[11]);
                        }
                    }

                    if (!(countMsrItemNo == 0 && countMsrItemNo > 1 && countMsrPartNo == 0 && countMsrPartNo > 1)) {
                        List<Object[]> objects = msrRepository.itemInfo(userProfile.getCompanyCode(),
                                userProfile.getPlantNo(), msrNo, partNo, itemNo, dto.getSeqNo());
                        for (Object[] data : objects) {
                            dto.setPartNo((String) data[0]);
                            dto.setSeqNo((Integer) data[1]);
                            dto.setItemNo((String) data[2]);
                            dto.setDescription((String) data[3]);
                            dto.setMslCode((String) data[4]);
                            dto.setItemType((Integer) data[5]);
                            dto.setLoc((String) data[6]);
                            dto.setUom((String) data[7]);
                            dto.setProjectNo((String) data[8]);
                            dto.setGrnNo((String) data[9]);
                            dto.setRetnQty((BigDecimal) data[10]);
                            dto.setRetnPrice((BigDecimal) data[11]);
                            Optional<Grn> optionalGrn = grnRepository.findGrnByIds_CompanyCodeAndIds_PlantNoAndIds_GrnNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getGrnNo());
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
//                        if (itemNo != null) {
//                            List<Object[]> itemType0 = itemRepository.getCountByItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo);
//                            for (Object[] data : itemType0) {
//                                BigDecimal count = new BigDecimal(String.valueOf(data[0]));
//                                countItemNo = count.intValue();
//                            }
//                            if (countItemNo == 0) {
//                                checkValidItemNo(dto);
//                            } else if (countItemNo > 1) {
//                                List<Object[]> lovItemPart = itemRepository.lovItemPart(userProfile.getCompanyCode(), userProfile.getPlantNo(), partNo, itemNo);
//                                for (Object[] data : lovItemPart) {
//                                    dto.setPartNo((String) data[0]);
//                                    dto.setItemNo((String) data[1]);
//                                }
//                                List<Object[]> itemInfo = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getItemNo());
//                                for (Object[] data : itemInfo) {
//                                    dto.setPartNo((String) data[0]);
//                                    dto.setItemNo((String) data[1]);
//                                    dto.setDescription((String) data[2]);
//                                    dto.setLoc((String) data[3]);
//                                    dto.setUom((String) data[4]);
//                                }
//                            } else {
//                                List<Object[]> byItemNo = itemRepository.getItemAndPartNoByItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo);
//                                for (Object[] data : byItemNo) {
//                                    dto.setItemNo((String) data[0]);
//                                    dto.setPartNo((String) data[1]);
//                                }
//                            }
//
//                            List<Object[]> objects = itemRepository.getCountByPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getPartNo());
//                            for (Object[] data : objects) {
//                                BigDecimal count = new BigDecimal(String.valueOf(data[0]));
//                                countPartNo = count.intValue();
//                            }
//                            if (countPartNo == 0) {
//                                checkValidPartNo(dto);
//                            } else if (countPartNo > 1) {
//                                List<Object[]> lovItemPart = itemRepository.lovItemPart(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getPartNo(), dto.getItemNo());
//                                for (Object[] data : lovItemPart) {
//                                    dto.setPartNo((String) data[0]);
//                                    dto.setItemNo((String) data[1]);
//                                }
//                                List<Object[]> itemInfo = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getItemNo());
//                                for (Object[] data : itemInfo) {
//                                    dto.setPartNo((String) data[0]);
//                                    dto.setItemNo((String) data[1]);
//                                    dto.setDescription((String) data[2]);
//                                    dto.setLoc((String) data[3]);
//                                    dto.setUom((String) data[4]);
//                                }
//                            } else {
//                                List<Object[]> byPartNo = itemRepository.getItemAndPartNoByPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getPartNo());
//                                for (Object[] data : byPartNo) {
//                                    dto.setItemNo((String) data[0]);
//                                    dto.setPartNo((String) data[1]);
//                                }
//                            }
//
//                            List<Object[]> itemInfo = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getItemNo());
//                            for (Object[] data : itemInfo) {
//                                dto.setPartNo((String) data[0]);
//                                dto.setItemNo((String) data[1]);
//                                dto.setDescription((String) data[2]);
//                                dto.setLoc((String) data[3]);
//                                dto.setUom((String) data[4]);
//                            }
//                        }
//
//                        if (projectNo != null) {
//                            List<Object[]> objects = bombypjRepository.getPrjNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), projectNo);
//                            for (Object[] data : objects) {
//                                dto.setProjectNo((String) data[0]);
//                            }
//                            if (dto.getProjectNo() == null) {
//                                checkProjectNoIfNull(dto);
//                            } else if (itemType == 0) {
//                                List<Object[]> alternate = bombypjRepository.getAltrnt(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getProjectNo(), itemNo);
//                                for (Object[] data : alternate) {
//                                    dto.setItemNo((String) data[0]);
//                                }
//                                if (dto.getItemNo() == null) {
//                                    checkItemNoInProject(dto);
//                                }
//                            }
//                        }
//
//                        if (poNo != null) {
//                            List<Object[]> objects = purRepository.getPoNoAndRecSeq(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemType, itemNo, partNo, poNo);
//                            for (Object[] data : objects) {
//                                BigDecimal seqNo = new BigDecimal(String.valueOf(data[1]));
//                                dto.setPoNo((String) data[0]);
//                                dto.setSeqNo(seqNo.intValue());
//                            }
//                            if (dto.getPoNo() == null) {
//                                checkValidPoNo(dto);
//                            } else if (dto.getSeqNo() == null) {
//                                checkItemNotInPo(dto);
//                            }
//                        }
//
//                        if (recdPrice != null) {
//                            if (recdPrice.intValue() < 0) {
//                                checkValidRecdPrice(dto);
//                            } else if (itemNo != null) {
//                                String source = null;
//                                List<Object[]> objects = itemRepository.getSource(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo);
//                                for (Object[] data : objects) {
//                                    source = (String) data[0];
//                                }
//                                if (source == null) {
//                                    checkSourceStockItem(dto);
//                                }
//
//                                if (source.equals("C")) {
//                                    if (recdPrice.intValue() > 0) {
//                                        checkValidRecdPriceForConsignedItem(dto);
//                                    }
//                                }
//                            }
//                        }
//
//                        if (recdQty != null) {
//                            if (recdQty.intValue() <= 0) {
//                                checkRecdQty(dto);
//                            } else if (retnQty.intValue() > 0 && recdQty.intValue() > retnQty.intValue()) {
//                                checkRecdRetnQty(dto, retnQty);
//                            }
//                        }
//
//                        if (labelQty != null) {
//                            if (labelQty.intValue() <= 0) {
//                                checkLabelQty(dto);
//                            } else if (recdQty.intValue() < labelQty.intValue()) {
//                                checkRecdLabelQty(dto, recdQty);
//                            }
//                        }
                    }
                }
            }
        }
        return dto;
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

        int countItemNo = 0;
        List<Object[]> checkItemNo = purRepository.checkItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo, itemNo);
        GrnDetDTO detDTO = GrnDetDTO.builder().build();

        for (Object[] data : checkItemNo) {
            BigDecimal count = new BigDecimal(String.valueOf(data[0]));
            countItemNo = count.intValue();
        }
        if (countItemNo == 0) {
            detDTO.setMessage("The Part No is either invalid or qty fully received!");
        }
        return detDTO;
    }

    private GrnDetDTO checkDataFromPartNo(String poNo, String partNo, Integer poRecSeq, UserProfile userProfile) {

        int recSeq = 0;
        String partno = "";
        List<Object[]> checkPartNo = purRepository.checkPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo, partNo);
        List<Object[]> checkDuplicatePartNo = purRepository.checkDuplicatePartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo, partNo, poRecSeq);
        GrnDetDTO detDTO = GrnDetDTO.builder().build();

        for (Object[] data : checkPartNo) {
            BigDecimal count = new BigDecimal(String.valueOf(data[0]));
            int countPartNo = count.intValue();
            if (countPartNo == 0) {
                detDTO.setMessage("The Part No is either invalid or qty fully received!");
            }
            for (Object[] data2 : checkDuplicatePartNo) {
                PurDetDTO dto = PurDetDTO.builder().build();
                BigDecimal recS = new BigDecimal(String.valueOf(data[0]));
                dto.setRecSeq(recS.intValue());
                dto.setPartNo((String) data2[1]);
                partno = dto.getPartNo();
                recSeq = dto.getRecSeq();
            }
            if (partNo.equals(partno) & poRecSeq == recSeq) {
                detDTO.setMessage("Duplicate Part No found!'");
            }
        }
        return detDTO;
    }

    private List<PurDetDTO> getDetailInfo(String poNo, String itemNo, String partNo, Integer poRecSeq, UserProfile userProfile) {

        List<PurDetDTO> list = new ArrayList<>();
        List<Object[]> detailInfo = purRepository.getDataFromItemAndPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo,
                itemNo, partNo, poRecSeq);
        for (Object[] data : detailInfo) {
            PurDetDTO dto = PurDetDTO.builder().build();
            BigDecimal recSeq = new BigDecimal(String.valueOf(data[1]));
            BigDecimal itemType = new BigDecimal(String.valueOf(data[5]));
            dto.setPartNo((String) data[0]);
            dto.setRecSeq(recSeq.intValue());
            dto.setItemNo((String) data[2]);
            dto.setDescription((String) data[3]);
            dto.setMslCode((String) data[4]);
            dto.setItemType(itemType.intValue());
            dto.setLoc((String) data[6]);
            dto.setUom((String) data[7]);
            dto.setProjectNo((String) data[8]);
            dto.setOrderQty((BigDecimal) data[9]);
            dto.setUnitPrice((BigDecimal) data[10]);
            dto.setDueDate((Date) data[11]);
            dto.setResvQty((BigDecimal) data[12]);
            dto.setInvUom((String) data[13]);
            dto.setStdPackQty((BigDecimal) data[14]);
            dto.setRemarks((String) data[15]);
            list.add(dto);
        }
        return list;
    }

    @Override
    public GrnDTO createGrn(GrnDTO input) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String date = sdf.format(new Date());
        try {
            UserProfile userProfile = UserProfileContext.getUserProfile();

            GrnId grnId = new GrnId();
            BeanUtils.copyProperties(input, grnId);

            Grn grn = new Grn();
            grn.setIds(grnId);
            BeanUtils.copyProperties(input, grn);

            grn.setIds(populateGrnId(input.getGrnNo(), input.getSubType()));
            grn.setRecdDate(sdf.parse(date));
            grn.setStatus(Status.ACTIVE);
            grn.setCreatedBy(userProfile.getUsername());
            grn.setCreatedAt(ZonedDateTime.now());
            grn.setUpdatedBy(userProfile.getUsername());
            grn.setUpdatedAt(ZonedDateTime.now());

            Optional<Grn> grnOptional = grnRepository.findGrnByIdsGrnNo(input.getGrnNo());
            if (grnOptional.isPresent()) {
                input.setMessage("GRN Record exists ! New GRN No: " + input.getGrnNo() + " is being assigned !");
            } else {
                Grn saved = grnRepository.save(grn);
                populateAfterSaving(input, saved);
                if (input.getGrnDetList() != null) {
                    List<GrnDet> grnDetList = new ArrayList<>();
                    for (int i = 0; i < input.getGrnDetList().size(); i++) {
                        GrnDet grnDet = new GrnDet();
                        BeanUtils.copyProperties(input, grnDet);
                        grnDet.setSeqNo(input.getGrnDetList().get(i).getSeqNo());
                        grnDet.setLoc(input.getGrnDetList().get(i).getLoc());
                        grnDet.setItemNo(input.getGrnDetList().get(i).getItemNo());
                        grnDet.setPartNo(input.getGrnDetList().get(i).getPartNo());
                        grnDet.setItemType(input.getGrnDetList().get(i).getItemType());
                        grnDet.setProjectNo(input.getGrnDetList().get(i).getProjectNo());
                        grnDet.setPoRecSeq(input.getGrnDetList().get(i).getPoRecSeq());
                        grnDet.setUom(input.getGrnDetList().get(i).getUom());
                        grnDet.setRecdQty(input.getGrnDetList().get(i).getRecdQty());
                        grnDet.setRecdPrice(input.getGrnDetList().get(i).getRecdPrice());
                        grnDet.setPoPrice(input.getGrnDetList().get(i).getPoPrice());
                        grnDet.setIssuedQty(input.getGrnDetList().get(i).getIssuedQty());
                        grnDet.setLabelQty(input.getGrnDetList().get(i).getLabelQty());
                        grnDet.setStdPackQty(input.getGrnDetList().get(i).getStdPackQty());
                        grnDet.setRemarks(input.getGrnDetList().get(i).getRemarks());
                        grnDet.setGrnId(grn.getIds());
                        grnDet.setGrn(grn);
                        grnDet.setRecdDate(sdf.parse(date));
                        grnDetList.add(grnDet);
                        grnDetRepository.save(grnDet);
                        postSaving(userProfile);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return input;
    }

    private void populateAfterSaving(GrnDTO grnDTO, Grn saved) {
        grnDTO.setVersion(saved.getVersion());
    }

    private void postSaving(UserProfile userProfile) {

        List<Object[]> generatedNoforGRN = docmNoRepository.getLastGeneratedNoforGRN(userProfile.getCompanyCode(), userProfile.getPlantNo());
        for (Object[] data : generatedNoforGRN) {
            BigDecimal lastGeneratedNo = new BigDecimal(String.valueOf(data[1]));
            docmNoRepository.updateLastGeneratedNo(lastGeneratedNo, userProfile.getCompanyCode(), userProfile.getPlantNo());
        }
    }

    @Override
    public GrnDTO findBy(String grnNo, String subType) {

        GrnId grnId = populateGrnId(grnNo, subType);
        Grn grn = checkIfRecordExist(grnId);
        List<GrnDetDTO> list = new ArrayList<>();
        GrnDetDTO grnDetDTO = GrnDetDTO.builder().build();
        for (GrnDet grnDet : grn.getGrnDetList()) {
            grnDetDTO.setId(grnDet.getId());
            grnDetDTO.setSeqNo(grnDet.getSeqNo());
            grnDetDTO.setItemNo(grnDet.getItemNo());
            grnDetDTO.setPartNo(grnDet.getPartNo());
            grnDetDTO.setLoc(grnDet.getLoc());
            grnDetDTO.setItemType(grnDet.getItemType());
            grnDetDTO.setProjectNo(grnDet.getProjectNo());
            grnDetDTO.setPoNo(grnDet.getPoNo());
            grnDetDTO.setPoRecSeq(grnDet.getPoRecSeq());
            grnDetDTO.setSivNo(grnDet.getSivNo());
            grnDetDTO.setUom(grnDet.getUom());
            grnDetDTO.setRecdDate(grnDet.getRecdDate());
            grnDetDTO.setRecdPrice(grnDet.getRecdPrice());
            grnDetDTO.setPoPrice(grnDet.getPoPrice());
            grnDetDTO.setIssuedQty(grnDet.getIssuedQty());
            grnDetDTO.setLabelQty(grnDet.getLabelQty());
            grnDetDTO.setStdPackQty(grnDet.getStdPackQty());
            grnDetDTO.setApRecdQty(grnDet.getApRecdQty());
            grnDetDTO.setRemarks(grnDet.getRemarks());
        }
        list.add(grnDetDTO);
        GrnDTO grnDTO = GrnDTO.builder().grnDetList(list).build();
        BeanUtils.copyProperties(grn, grnDTO);
        BeanUtils.copyProperties(grn.getIds(), grnDTO);

        return grnDTO;
    }

    private Grn checkIfRecordExist(GrnId grnId) {

        Optional<Grn> optionalGrn = grnRepository.findGrnByIds(grnId);

        if (!optionalGrn.isPresent()) {
            throw new NotFoundException("Record is not found");
        }
        return optionalGrn.get();
    }

    @Override
    public SearchResult<GrnDTO> searchBy(SearchRequest searchRequest) {
        Specification specs = where(queryGenerator.createDefaultSpecificationWithId());

        if (!CollectionUtils.isEmpty(searchRequest.getFilters())) {
            for (Filter filter : searchRequest.getFilters()) {
                specs = specs.and(queryGenerator.createSpecification(filter));
            }
        }

        Page<Grn> pgGrns = grnRepository.findAll(specs, queryGenerator.constructPageable(searchRequest));

        SearchResult<GrnDTO> grns = new SearchResult<>();
        grns.setTotalRows(pgGrns.getTotalElements());
        grns.setTotalPages(pgGrns.getTotalPages());
        grns.setCurrentPageNumber(pgGrns.getPageable().getPageNumber());
        grns.setCurrentPageSize(pgGrns.getNumberOfElements());
        grns.setRows(pgGrns.getContent().stream().map(grn -> {
            List<GrnDetDTO> list = new ArrayList<>();
            GrnDetDTO grnDetDTO = GrnDetDTO.builder().build();
            for (GrnDet grnDet : grn.getGrnDetList()) {
                grnDetDTO.setId(grnDet.getId());
                grnDetDTO.setSeqNo(grnDet.getSeqNo());
                grnDetDTO.setItemNo(grnDet.getItemNo());
                grnDetDTO.setPartNo(grnDet.getPartNo());
                grnDetDTO.setLoc(grnDet.getLoc());
                grnDetDTO.setItemType(grnDet.getItemType());
                grnDetDTO.setProjectNo(grnDet.getProjectNo());
                grnDetDTO.setPoNo(grnDet.getPoNo());
                grnDetDTO.setPoRecSeq(grnDet.getPoRecSeq());
                grnDetDTO.setSivNo(grnDet.getSivNo());
                grnDetDTO.setUom(grnDet.getUom());
                grnDetDTO.setRecdDate(grnDet.getRecdDate());
                grnDetDTO.setRecdPrice(grnDet.getRecdPrice());
                grnDetDTO.setPoPrice(grnDet.getPoPrice());
                grnDetDTO.setIssuedQty(grnDet.getIssuedQty());
                grnDetDTO.setLabelQty(grnDet.getLabelQty());
                grnDetDTO.setStdPackQty(grnDet.getStdPackQty());
                grnDetDTO.setApRecdQty(grnDet.getApRecdQty());
                grnDetDTO.setRemarks(grnDet.getRemarks());
            }
            list.add(grnDetDTO);
            GrnDTO grnDTO = GrnDTO.builder().grnDetList(list).build();
            BeanUtils.copyProperties(grn.getIds(), grnDTO);
            BeanUtils.copyProperties(grn, grnDTO);
            return grnDTO;
        }).collect(Collectors.toList()));

        return grns;
    }

    private GrnId populateGrnId(String grnNo, String subType) {
        UserProfile userProfile = UserProfileContext.getUserProfile();

        GrnId grnId = new GrnId();
        grnId.setCompanyCode(userProfile.getCompanyCode());
        grnId.setPlantNo(userProfile.getPlantNo());
        grnId.setGrnNo(grnNo);
        grnId.setSubType(subType);

        return grnId;
    }
}
