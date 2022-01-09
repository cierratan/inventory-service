package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.*;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.grn.Grn;
import com.sunright.inventory.entity.grn.GrnDet;
import com.sunright.inventory.entity.grn.GrnId;
import com.sunright.inventory.exception.ErrorMessage;
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
    private DefaultCodeDetailRepository defaultCodeDetailRepository;

    @Autowired
    private QueryGenerator queryGenerator;

    @Override
    public Map<String, Object> getAllPoNo(UserProfile userProfile) {

        Map<String, Object> map = new HashMap<>();
        try {
            List<PurDTO> list = new ArrayList<>();
            List<Object[]> getAllPONo = purRepository.getAllPoNo(userProfile.getCompanyCode(), userProfile.getPlantNo());
            for (Object[] data : getAllPONo) {
                PurDTO dto = new PurDTO();
                dto.setPoNo((String) data[0]);
                list.add(dto);
            }
            map.put("contentData", list);
            map.put("totalRecords", (long) list.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public Map<String, Object> getGrnHeader(GrnDTO grnDTO) {

        Map<String, Object> map = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String date = sdf.format(new Date());
        try {
            UserProfile userProfile = UserProfileContext.getUserProfile();
            List<GrnDTO> list = new ArrayList<>();
            Map<String, Object> checkStatusPONo = checkStatusPoNo(grnDTO, userProfile);
            if (checkStatusPONo.get("statusPONo") != null) {
                map.put("contentData", checkStatusPONo.get("statusPONo"));
            } else {
                List<DocmNoDTO> lastNoForGrnList = getLastGeneratedNoforGRN(userProfile);
                List<PurDTO> purInfoList = getPurInfo(grnDTO, userProfile);
                for (DocmNoDTO dtoDocmNo : lastNoForGrnList) {
                    grnDTO.setGrnNo(dtoDocmNo.getPrefix());
                    if (grnDTO.getGrnNo() == null) {
                        map.put("contentData", "Grn No. must not be empty");
                    } else {
                        for (PurDTO dtoPur : purInfoList) {
                            grnDTO.setSupplierCode(dtoPur.getSupplierCode());
                            grnDTO.setCurrencyCode(dtoPur.getCurrencyCode());
                            grnDTO.setCurrencyRate(dtoPur.getCurrencyRate());
                            grnDTO.setBuyer(dtoPur.getBuyer());
                            grnDTO.setRlseDate(dtoPur.getRlseDate());
                            grnDTO.setPoRemarks(dtoPur.getRemarks());
                            grnDTO.setRecdDate(sdf.parse(date));
                            List<SupplierDTO> supplierList = supplierName(grnDTO.getSupplierCode(), userProfile);
                            for (SupplierDTO dtoSupplier : supplierList) {
                                grnDTO.setSupplierName(dtoSupplier.getName());
                                if (grnDTO.getSupplierName() == null) {
                                    map.put("contentData", "Supplier Code not found");
                                } else {
                                    list.add(grnDTO);
                                }
                            }
                        }
                        map.put("contentData", list);
                    }
                }
            }
            map.put("totalRecords", (long) list.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    private List<DocmNoDTO> getLastGeneratedNoforGRN(UserProfile userProfile) {

        List<DocmNoDTO> list = new ArrayList<>();
        List<Object[]> generatedNoforGRN = docmNoRepository.getLastGeneratedNoforGRN(userProfile.getCompanyCode(), userProfile.getPlantNo());
        for (Object[] data : generatedNoforGRN) {
            DocmNoDTO dto = new DocmNoDTO();
            BigDecimal d = new BigDecimal(String.valueOf(data[1]));
            dto.setPrefix((String) data[0]);
            dto.setLastGeneratedNo(d.intValue());
            list.add(dto);
        }
        return list;
    }

    private Map<String, Object> checkStatusPoNo(GrnDTO grnDTO, UserProfile userProfile) {

        Map<String, Object> map = new HashMap<>();
        try {
            List<Object[]> pur = purRepository.checkStatusPoNoPur(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDTO.getPoNo());
            List<Object[]> draftPur = draftPurRepository.checkStatusPoNoDraftPur(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDTO.getPoNo());
            for (Object[] dataDraftPur : draftPur) {
                PurDTO dto = new PurDTO();
                dto.setPoNo((String) dataDraftPur[0]);
                dto.setOpenClose((String) dataDraftPur[1]);
                if (dto.getOpenClose().equalsIgnoreCase("C")) {
                    map.put("statusPONo", "PO already Closed, Purchase Receipt not allowed.");
                } else if (!dto.getOpenClose().equalsIgnoreCase("A")) {
                    map.put("statusPONo", "PO is yet to be Approved, Purchase Receipt not allowed.");
                } else if (dto.getOpenClose().equalsIgnoreCase("V")) {
                    map.put("statusPONo", "PO already Voided, Purchase Receipt not allowed.");
                } else if (dto.getPoNo() == null) {
                    map.put("statusPONo", "Invalid PO No!" + dto.getPoNo() + " ");
                } else {
                    if (dto.getPoNo() != null && dto.getOpenClose() != null) {
                        for (Object[] data : pur) {
                            dto.setPoNo((String) data[0]);
                            dto.setOpenClose((String) data[1]);
                            if (dto.getOpenClose().equalsIgnoreCase("C")) {
                                map.put("statusPONo", "PO already Closed, Purchase Receipt not allowed.");
                            } else if (!dto.getOpenClose().equalsIgnoreCase("A")) {
                                map.put("statusPONo", "PO is yet to be Approved, Purchase Receipt not allowed.");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    private List<PurDTO> getPurInfo(GrnDTO grnDTO, UserProfile userProfile) {

        List<PurDTO> list = new ArrayList<>();
        List<Object[]> purInfo = purRepository.getPurInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDTO.getPoNo());
        for (Object[] data : purInfo) {
            PurDTO dto = new PurDTO();
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

    private List<SupplierDTO> supplierName(String supplierCode, UserProfile userProfile) {

        List<SupplierDTO> list = new ArrayList<>();
        List<Object[]> supplierNameInfo = supplierRepository.getSupplierName(userProfile.getCompanyCode(), userProfile.getPlantNo(), supplierCode);
        for (Object[] data : supplierNameInfo) {
            SupplierDTO dto = new SupplierDTO();
            dto.setName((String) data[0]);
            list.add(dto);
        }

        return list;
    }

    @Override
    public Map<String, Object> getAllPartNo(GrnDTO grnDTO, UserProfile userProfile) {

        Map<String, Object> map = new HashMap<>();
        try {
            List<PurDetDTO> list = new ArrayList<>();
            List<Object[]> getDataFromPartNo = purDetRepository.getDataFromPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDTO.getPoNo());
            for (Object[] data : getDataFromPartNo) {
                PurDetDTO dto = new PurDetDTO();
                BigDecimal seqNo = new BigDecimal(String.valueOf(data[0]));
                dto.setSeqNo(seqNo.intValue());
                dto.setPartNo((String) data[1]);
                dto.setItemNo((String) data[2]);
                BigDecimal recSeq = new BigDecimal(String.valueOf(data[3]));
                dto.setRecSeq(recSeq.intValue());
                list.add(dto);
            }
            map.put("contentData", list);
            map.put("totalRecords", (long) list.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public Map<String, Object> getGrnDetail(GrnDTO grnDTO, GrnDetDTO grnDetDTO) {

        Map<String, Object> map = new HashMap<>();
        try {
            UserProfile userProfile = UserProfileContext.getUserProfile();
            List<GrnDetDTO> list = new ArrayList<>();
            List<PurDetDTO> getDataDetail = getDetailInfo(grnDTO, grnDTO.getGrnDetList(), userProfile);
            if (grnDTO.getGrnDetList().get(0).getPartNo() != null) {
                map = checkDataFromPartNo(grnDTO, grnDTO.getGrnDetList(), userProfile);
                if (map.get("partNoMsgError") != null) {
                    map.put("contentData", map.get("partNoMsgError"));
                }
            } else {
                map = checkDataFromItemNo(grnDTO, grnDTO.getGrnDetList(), userProfile);
                if (map.get("itemNoMsgError") != null) {
                    map.put("contentData", map.get("itemNoMsgError"));
                }
            }

            if (grnDTO.getGrnDetList().get(0).getPartNo() == null && grnDTO.getGrnDetList().get(0).getItemNo() == null) {
                map = checkDataFromPartNo(grnDTO, grnDTO.getGrnDetList(), userProfile);
                if (map.get("partNoMsgError") != null) {
                    map.put("contentData", map.get("partNoMsgError"));
                }
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
                list.add(grnDetDTO);
            }
            map.put("contentData", list);
            map.put("totalRecords", (long) list.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    private Map<String, Object> checkDataFromItemNo(GrnDTO grnDTO, List<GrnDetDTO> grnDetDTO, UserProfile userProfile) {

        Map<String, Object> map = new HashMap<>();
        try {
            int countItemNo = 0;
            List<Object[]> checkItemNo = purDetRepository.checkItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDTO.getPoNo(), grnDetDTO.get(0).getItemNo());
            for (Object[] data : checkItemNo) {
                BigDecimal count = new BigDecimal(String.valueOf(data[0]));
                countItemNo = count.intValue();
            }
            if (countItemNo == 0) {
                map.put("itemNoMsgError", "The Part No is either invalid or qty fully received!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;

    }

    private Map<String, Object> checkDataFromPartNo(GrnDTO grnDTO, List<GrnDetDTO> grnDetDTO, UserProfile userProfile) {

        Map<String, Object> map = new HashMap<>();
        try {
            int recSeq = 0;
            String partNo = "";
            List<Object[]> checkPartNo = purDetRepository.checkPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDTO.getPoNo(), grnDetDTO.get(0).getPartNo());
            List<Object[]> checkDuplicatePartNo = purDetRepository.checkDuplicatePartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDTO.getPoNo(), grnDetDTO.get(0).getPartNo(), grnDetDTO.get(0).getPoRecSeq());

            for (Object[] data : checkPartNo) {
                BigDecimal count = new BigDecimal(String.valueOf(data[0]));
                int countPartNo = count.intValue();
                if (countPartNo == 0) {
                    map.put("partNoMsgError", "The Part No is either invalid or qty fully received!");
                }
                for (Object[] data2 : checkDuplicatePartNo) {
                    PurDetDTO dto = new PurDetDTO();
                    BigDecimal recS = new BigDecimal(String.valueOf(data[0]));
                    dto.setRecSeq(recS.intValue());
                    dto.setPartNo((String) data2[1]);
                    partNo = dto.getPartNo();
                    recSeq = dto.getRecSeq();
                }
                if (grnDetDTO.get(0).getPartNo().equals(partNo) & grnDetDTO.get(0).getPoRecSeq() == recSeq) {
                    map.put("partNoMsgError", "Duplicate Part No found!'");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    private List<PurDetDTO> getDetailInfo(GrnDTO grnDTO, List<GrnDetDTO> grnDetDTO, UserProfile userProfile) {

        List<PurDetDTO> list = new ArrayList<>();
        List<Object[]> getDetailInfo = purDetRepository.getDataFromItemAndPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDTO.getPoNo(),
                grnDetDTO.get(0).getItemNo(), grnDetDTO.get(0).getPartNo(), grnDetDTO.get(0).getPoRecSeq());
        for (Object[] data : getDetailInfo) {
            PurDetDTO dto = new PurDetDTO();
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
    public Map<String, Object> create(GrnDTO grnDTO) {

        Map<String, Object> map = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String date = sdf.format(new Date());

        try {
            UserProfile userProfile = UserProfileContext.getUserProfile();

            GrnId grnId = new GrnId();
            BeanUtils.copyProperties(grnDTO, grnId);

            Grn grn = new Grn();
            grn.setIds(grnId);
            BeanUtils.copyProperties(grnDTO, grn);

            grn.setIds(populateGrnId(grnDTO.getGrnNo(), grnDTO.getSubType()));
            grn.setRecdDate(sdf.parse(date));
            grn.setStatus(Status.ACTIVE);
            grn.setCreatedBy(userProfile.getUsername());
            grn.setCreatedAt(ZonedDateTime.now());
            grn.setUpdatedBy(userProfile.getUsername());
            grn.setUpdatedAt(ZonedDateTime.now());

            Optional<Grn> grnOptional = grnRepository.findGrnByIdsGrnNo(grnDTO.getGrnNo());
            if (grnOptional.isPresent()) {
                map.put("grnExists", "GRN Record exists ! New GRN No: " + grnDTO.getGrnNo() + " is being assigned !");
            } else {
                log.info("Saving new GRN : {}", grn);
                Grn saved = grnRepository.save(grn);
                populateAfterSaving(grnDTO, saved);
                if (grnDTO.getGrnDetList() != null) {
                    List<GrnDet> grnDetList = new ArrayList<>();
                    for (int i = 0; i < grnDTO.getGrnDetList().size(); i++) {
                        GrnDet grnDet = new GrnDet();
                        BeanUtils.copyProperties(grnDTO, grnDet);
                        grnDet.setSeqNo(grnDTO.getGrnDetList().get(i).getSeqNo());
                        grnDet.setLoc(grnDTO.getGrnDetList().get(i).getLoc());
                        grnDet.setItemNo(grnDTO.getGrnDetList().get(i).getItemNo());
                        grnDet.setPartNo(grnDTO.getGrnDetList().get(i).getPartNo());
                        grnDet.setItemType(grnDTO.getGrnDetList().get(i).getItemType());
                        grnDet.setProjectNo(grnDTO.getGrnDetList().get(i).getProjectNo());
                        grnDet.setPoRecSeq(grnDTO.getGrnDetList().get(i).getPoRecSeq());
                        grnDet.setUom(grnDTO.getGrnDetList().get(i).getUom());
                        grnDet.setRecdQty(grnDTO.getGrnDetList().get(i).getRecdQty());
                        grnDet.setRecdPrice(grnDTO.getGrnDetList().get(i).getRecdPrice());
                        grnDet.setPoPrice(grnDTO.getGrnDetList().get(i).getPoPrice());
                        grnDet.setIssuedQty(grnDTO.getGrnDetList().get(i).getIssuedQty());
                        grnDet.setLabelQty(grnDTO.getGrnDetList().get(i).getLabelQty());
                        grnDet.setStdPackQty(grnDTO.getGrnDetList().get(i).getStdPackQty());
                        grnDet.setRemarks(grnDTO.getGrnDetList().get(i).getRemarks());
                        grnDet.setGrnId(grn.getIds());
                        grnDet.setGrn(grn);
                        grnDet.setRecdDate(sdf.parse(date));
                        grnDetList.add(grnDet);
                        map.put("contentData", grnDetList);
                        map.put("totalRecords", (long) grnDetList.size());
                        // pre insert
                        if (grnDet.getPartNo() != null) {
                            checkReceivedQty(grnDetList, grnDTO.getPoNo());
                            checkLabelQty(grnDetList);
                        }
                        checkRecdQty(grnDetList);
                        checkUnitPrice(grnDetList);
                        grnDetRepository.save(grnDet);
                        postSaving(userProfile);

                        map.put("successSave", "Transaction Complete: " + (long) grnDetList.size() + " records applied and saved");
                    }
                    log.info("{} GRN Detail records saved. {}", grnDetList.size(), grnDetList);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
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

    private void checkLabelQty(List<GrnDet> grnDetList) {

        for (GrnDet grnDet : grnDetList) {
            if (grnDet.getLabelQty().intValue() > 0 && grnDet.getLabelQty().intValue() > grnDet.getRecdQty().intValue()) {
                ErrorMessage.builder().message("Qty per label is more than Received Qty!").build();
                return;
            }
        }
    }

    private void checkReceivedQty(List<GrnDet> grnDetList, String poNo) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        List<Object[]> purDetInfo = purDetRepository.getPurDetInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo);
        for (GrnDet grnDet : grnDetList) {
            for (Object[] data : purDetInfo) {
                PurDetDTO dto = new PurDetDTO();
                dto.setOrderQty((BigDecimal) data[0]);
                if (grnDet.getRecdQty().intValue() == 0) {
                    ErrorMessage.builder().message("Received Qty cannot be empty or zero!").build();
                    return;
                } else if (grnDet.getRecdQty().intValue() > 0 && grnDet.getRecdQty().intValue() > dto.getOrderQty().intValue()) {
                    ErrorMessage.builder().message("Receiving more than Ordered is not allowed!").build();
                    return;
                }
            }
        }

    }

    @Override
    public List<Grn> get() {
        return (List<Grn>) grnRepository.findAll();
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
            GrnDTO grnDTO = GrnDTO.builder().build();
            BeanUtils.copyProperties(grn.getId(), grnDTO);
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

    private void checkUnitPrice(List<GrnDet> grnDetList) {

        for (GrnDet grnDet : grnDetList) {
            if (grnDet.getRecdQty().intValue() > 0) {
                if (!grnDet.getPoPrice().equals(grnDet.getRecdPrice())) {
                    ErrorMessage.builder().message("Unit price is not equal to receiving price !").build();
                    return;
                }
            }
        }
    }

    private void checkRecdQty(List<GrnDet> grnDetList) {

        for (GrnDet grnDet : grnDetList) {
            int poRecSeq = 0;
            if (poRecSeq != grnDet.getPoRecSeq()) {
                poRecSeq = grnDet.getPoRecSeq();
                BigDecimal recQty = grnDet.getRecdQty();
                if (grnDet.getPoRecSeq() == poRecSeq) {
                    recQty = recQty.add(grnDet.getRecdQty());
                    if (recQty.intValue() > grnDet.getRecdQty().intValue()) {
                        ErrorMessage.builder().message("Receiving more than Ordered is not allowed!").build();
                        return;
                    }
                }

            }
        }
    }
}
