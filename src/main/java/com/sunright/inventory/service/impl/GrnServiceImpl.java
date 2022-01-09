package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.*;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.grn.Grn;
import com.sunright.inventory.entity.grn.GrnDet;
import com.sunright.inventory.entity.grn.GrnId;
import com.sunright.inventory.exception.ErrorMessage;
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
    public List<GrnDTO> getGrnHeader(String poNo) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String date = sdf.format(new Date());
        UserProfile userProfile = UserProfileContext.getUserProfile();
        List<GrnDTO> list = new ArrayList<>();
        checkStatusPoNo(poNo, userProfile);
        List<DocmNoDTO> lastNoForGrnList = getLastGeneratedNoforGRN(userProfile);
        List<PurDTO> purInfoList = getPurInfo(poNo, userProfile);
        for (DocmNoDTO dtoDocmNo : lastNoForGrnList) {
            GrnDTO grnDTO = GrnDTO.builder().build();
            grnDTO.setGrnNo(dtoDocmNo.getPrefix());
            if (grnDTO.getGrnNo() == null) {
                ErrorMessage.builder().message("Grn No. must not be empty").build();
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
                    List<SupplierDTO> supplierList = supplierName(grnDTO.getSupplierCode(), userProfile);
                    for (SupplierDTO dtoSupplier : supplierList) {
                        grnDTO.setSupplierName(dtoSupplier.getName());
                        if (grnDTO.getSupplierName() == null) {
                            ErrorMessage.builder().message("Supplier Code not found").build();
                        } else {
                            list.add(grnDTO);
                        }
                    }
                }
            }
        }

        return list;
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

    private void checkStatusPoNo(String poNo, UserProfile userProfile) {

        try {
            List<Object[]> pur = purRepository.checkStatusPoNoPur(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo);
            List<Object[]> draftPur = draftPurRepository.checkStatusPoNoDraftPur(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo);
            for (Object[] dataDraftPur : draftPur) {
                PurDTO dto = PurDTO.builder().build();
                dto.setPoNo((String) dataDraftPur[0]);
                dto.setOpenClose((String) dataDraftPur[1]);
                if (dto.getOpenClose().equalsIgnoreCase("C")) {
                    ErrorMessage.builder().message("PO already Closed, Purchase Receipt not allowed.").build();
                } else if (!dto.getOpenClose().equalsIgnoreCase("A")) {
                    ErrorMessage.builder().message("PO is yet to be Approved, Purchase Receipt not allowed.").build();
                } else if (dto.getOpenClose().equalsIgnoreCase("V")) {
                    ErrorMessage.builder().message("PO already Voided, Purchase Receipt not allowed.").build();
                } else if (dto.getPoNo() == null) {
                    ErrorMessage.builder().message("Invalid PO No!").build();
                } else {
                    if (dto.getPoNo() != null && dto.getOpenClose() != null) {
                        for (Object[] data : pur) {
                            dto.setPoNo((String) data[0]);
                            dto.setOpenClose((String) data[1]);
                            if (dto.getOpenClose().equalsIgnoreCase("C")) {
                                ErrorMessage.builder().message("PO already Closed, Purchase Receipt not allowed.").build();
                            } else if (!dto.getOpenClose().equalsIgnoreCase("A")) {
                                ErrorMessage.builder().message("PO is yet to be Approved, Purchase Receipt not allowed.").build();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public List<GrnDetDTO> getAllPartNo(String poNo) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        List<GrnDetDTO> list = new ArrayList<>();
        List<Object[]> getDataFromPartNo = purDetRepository.getDataFromPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo);
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
    public List<GrnDetDTO> getGrnDetail(String poNo, String itemNo, String partNo, Integer poRecSeq) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        List<GrnDetDTO> list = new ArrayList<>();
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
            GrnDetDTO grnDetDTO = GrnDetDTO.builder().build();
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

        return list;
    }

    private void checkDataFromItemNo(String poNo, String itemNo, UserProfile userProfile) {

        int countItemNo = 0;
        List<Object[]> checkItemNo = purDetRepository.checkItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo, itemNo);
        for (Object[] data : checkItemNo) {
            BigDecimal count = new BigDecimal(String.valueOf(data[0]));
            countItemNo = count.intValue();
        }
        if (countItemNo == 0) {
            ErrorMessage.builder().message("The Part No is either invalid or qty fully received!").build();
        }
    }

    private void checkDataFromPartNo(String poNo, String partNo, Integer poRecSeq, UserProfile userProfile) {

        int recSeq = 0;
        String partno = "";
        List<Object[]> checkPartNo = purDetRepository.checkPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo, partNo);
        List<Object[]> checkDuplicatePartNo = purDetRepository.checkDuplicatePartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo, partNo, poRecSeq);

        for (Object[] data : checkPartNo) {
            BigDecimal count = new BigDecimal(String.valueOf(data[0]));
            int countPartNo = count.intValue();
            if (countPartNo == 0) {
                ErrorMessage.builder().message("The Part No is either invalid or qty fully received!").build();
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
                ErrorMessage.builder().message("Duplicate Part No found!'").build();
            }
        }
    }

    private List<PurDetDTO> getDetailInfo(String poNo, String itemNo, String partNo, Integer poRecSeq, UserProfile userProfile) {

        List<PurDetDTO> list = new ArrayList<>();
        List<Object[]> detailInfo = purDetRepository.getDataFromItemAndPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo,
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
                ErrorMessage.builder().message("GRN Record exists ! New GRN No: " + input.getGrnNo() + " is being assigned !").build();
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
                        // pre insert
                        if (grnDet.getPartNo() != null) {
                            checkReceivedQty(grnDetList, input.getPoNo());
                            checkLabelQty(grnDetList);
                        }
                        checkRecdQty(grnDetList);
                        checkUnitPrice(grnDetList);
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

    private void checkLabelQty(List<GrnDet> grnDetList) {

        for (GrnDet grnDet : grnDetList) {
            if (grnDet.getLabelQty().intValue() > 0 && grnDet.getLabelQty().intValue() > grnDet.getRecdQty().intValue()) {
                ErrorMessage.builder().message("Qty per label is more than Received Qty!").build();
            }
        }
    }

    private void checkReceivedQty(List<GrnDet> grnDetList, String poNo) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        List<Object[]> purDetInfo = purDetRepository.getPurDetInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo);
        for (GrnDet grnDet : grnDetList) {
            for (Object[] data : purDetInfo) {
                PurDetDTO dto = PurDetDTO.builder().build();
                dto.setOrderQty((BigDecimal) data[0]);
                if (grnDet.getRecdQty().intValue() == 0) {
                    ErrorMessage.builder().message("Received Qty cannot be empty or zero!").build();
                } else if (grnDet.getRecdQty().intValue() > 0 && grnDet.getRecdQty().intValue() > dto.getOrderQty().intValue()) {
                    ErrorMessage.builder().message("Receiving more than Ordered is not allowed!").build();
                }
            }
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

    private void checkUnitPrice(List<GrnDet> grnDetList) {

        for (GrnDet grnDet : grnDetList) {
            if (grnDet.getRecdQty().intValue() > 0) {
                if (!grnDet.getPoPrice().equals(grnDet.getRecdPrice())) {
                    ErrorMessage.builder().message("Unit price is not equal to receiving price !").build();
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
                    }
                }
            }
        }
    }
}
