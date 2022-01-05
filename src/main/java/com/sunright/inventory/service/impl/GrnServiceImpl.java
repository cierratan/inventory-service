package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.*;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.grn.Grn;
import com.sunright.inventory.entity.grn.GrnDet;
import com.sunright.inventory.entity.grn.GrnId;
import com.sunright.inventory.exception.ErrorMessage;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.*;
import com.sunright.inventory.service.GrnService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;

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
            grn.setGrnId(grnId);
            BeanUtils.copyProperties(grnDTO, grn);

            grn.setGrnId(populateGrnId(grnDTO.getGrnNo(), grnDTO.getSubType()));
            grn.setRecdDate(sdf.parse(date));

            grn.setStatus(Status.ACTIVE);
            grn.setCreatedBy(userProfile.getUsername());
            grn.setCreatedAt(ZonedDateTime.now());
            grn.setUpdatedBy(userProfile.getUsername());
            grn.setUpdatedAt(ZonedDateTime.now());

            Optional<Grn> grnOptional = grnRepository.findGrnByGrnIdGrnNo(grnDTO.getGrnNo());
            if (grnOptional.isPresent()) {
                map.put("grnExists", "GRN Record exists ! New GRN No:" + grnDTO.getGrnNo() + "is being assigned !");
            } else {
                log.info("Saving new GRN : {}", grn);
                Grn saved = grnRepository.save(grn);
                if (grnDTO.getGrnDetList() != null) {
                    List<GrnDet> grnDetList = new ArrayList<>();
                    for (int i = 0; i < grnDTO.getGrnDetList().size(); i++) {
                        GrnDet grnDet = new GrnDet();
                        BeanUtils.copyProperties(grnDTO, grnDet);
                        grnDet.setSeqNo(i + 1);
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
                        grnDet.setGrnId(grnId);
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
                        postSaving(grnDTO.getSubType(), userProfile);
                        populateAfterSaving(grnDTO, saved);
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

    private void postSaving(String subType, UserProfile userProfile) {

        List<Object[]> generatedNoforGRN = docmNoRepository.getLastGeneratedNoforGRN(userProfile.getCompanyCode(), userProfile.getPlantNo(), subType);
        for (Object[] data : generatedNoforGRN) {
            DocmNoDTO dto = new DocmNoDTO();
            BigDecimal d = new BigDecimal(String.valueOf(data[1]));
            dto.setLastGeneratedNo(d.intValue());
            docmNoRepository.updateLastGeneratedNo(dto.getLastGeneratedNo(), userProfile.getCompanyCode(), userProfile.getPlantNo(), subType);
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

        String msg;
        UserProfile userProfile = UserProfileContext.getUserProfile();
        List<Object[]> purDetInfo = purDetRepository.getPurDetInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), poNo);
        for (GrnDet grnDet : grnDetList) {
            for (Object[] data : purDetInfo) {
                PurDetDTO dto = new PurDetDTO();
                dto.setOrderQty((BigDecimal) data[0]);
                if (grnDet.getRecdQty().intValue() == 0) {
                    msg = "Received Qty cannot be empty or zero!";
                    ErrorMessage.builder().message(msg).build();
                } else if (grnDet.getRecdQty().intValue() > 0 && grnDet.getRecdQty().intValue() > dto.getOrderQty().intValue()) {
                    msg = "Receiving more than Ordered is not allowed!";
                    ErrorMessage.builder().message(msg).build();
                }
            }
        }
    }

    @Override
    public List<Grn> get() {
        return (List<Grn>) grnRepository.findAll();
    }

    @Override
    public Map<String, Object> getLastGeneratedNoforGRN(DocmNoDTO docmNoDTO) {

        Map<String, Object> map = new HashMap<>();
        try {
            UserProfile userProfile = UserProfileContext.getUserProfile();
            List<DocmNoDTO> list = new ArrayList<>();
            List<Object[]> generatedNoforGRN = docmNoRepository.getLastGeneratedNoforGRN(userProfile.getCompanyCode(), userProfile.getPlantNo(), docmNoDTO.getSubType());
            for (Object[] data : generatedNoforGRN) {
                DocmNoDTO dto = new DocmNoDTO();
                BigDecimal d = new BigDecimal(String.valueOf(data[1]));
                dto.setPrefix((String) data[0]);
                dto.setLastGeneratedNo(d.intValue());
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
    public ErrorMessage checkStatusPoNo(GrnDTO grnDTO) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        String msg = "";
        List<Object[]> poNoPur = purRepository.checkStatusPoNoPur(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDTO.getPoNo());
        List<Object[]> draftPur = draftPurRepository.checkStatusPoNoDraftPur(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDTO.getPoNo());
        for (Object[] data : poNoPur) {
            PurDTO dto = new PurDTO();
            dto.setPoNo((String) data[0]);
            dto.setOpenClose((String) data[1]);
            for (Object[] dataDraftPur : draftPur) {
                dto.setPoNo((String) dataDraftPur[0]);
                dto.setOpenClose((String) dataDraftPur[1]);
                if (dto.getOpenClose().equalsIgnoreCase("C")) {
                    msg = "PO already Closed, Purchase Receipt not allowed.";
                } else if (dto.getOpenClose().equalsIgnoreCase("A")) {
                    msg = "PO is yet to be Approved, Purchase Receipt not allowed.";
                } else if (dto.getOpenClose().equalsIgnoreCase("V")) {
                    msg = "PO already Voided, Purchase Receipt not allowed.";
                } else if (dto.getPoNo() == null) {
                    msg = "Invalid PO No!" + dto.getPoNo();
                }
            }
        }
        return ErrorMessage.builder().message(msg).build();
    }

    @Override
    public Map<String, Object> getPurInfo(GrnDTO grnDTO) {

        Map<String, Object> map = new HashMap<>();
        try {
            UserProfile userProfile = UserProfileContext.getUserProfile();
            List<PurDTO> list = new ArrayList<>();
            List<Object[]> purInfo = purRepository.getPurInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDTO.getPoNo());
            for (Object[] data : purInfo) {
                PurDTO dto = new PurDTO();
                dto.setSupplierCode((String) data[0]);
                dto.setCurrencyCode((String) data[1]);
                dto.setBuyer((String) data[2]);
                dto.setRlseDate((Date) data[3]);
                dto.setRemarks((String) data[4]);
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
    public Map<String, Object> getPurDetInfo(GrnDTO grnDTO) {

        Map<String, Object> map = new HashMap<>();
        try {
            UserProfile userProfile = UserProfileContext.getUserProfile();
            List<PurDetDTO> list = new ArrayList<>();
            List<Object[]> purDetInfo = purDetRepository.getPurDetInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDTO.getPoNo());
            for (Object[] data : purDetInfo) {
                PurDetDTO dto = new PurDetDTO();
                dto.setOrderQty((BigDecimal) data[0]);
                dto.setRemarks((String) data[1]);
                dto.setDueDate((Date) data[2]);
                dto.setMslCode((String) data[3]);
                dto.setOrderQty((BigDecimal) data[4]);
                dto.setInvUom((String) data[5]);
                dto.setStdPackQty((BigDecimal) data[6]);
                List<Object[]> uomDesc = purDetRepository.getUomDesc(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getInvUom());
                for (Object[] dataUomDesc : uomDesc) {
                    dto.setCodeDesc((String) dataUomDesc[0]);
                }
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
    public Map<String, Object> checkItemNoAndPartNo(GrnDTO grnDTO, GrnDetDTO grnDetDTO) {

        Map<String, Object> map = new HashMap<>();
        try {
            UserProfile userProfile = UserProfileContext.getUserProfile();
            List<PurDetDTO> list = new ArrayList<>();
            List<Object[]> checkItemNo = purDetRepository.checkItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDTO.getPoNo(), grnDetDTO.getItemNo());
            List<Object[]> checkPartNo = purDetRepository.checkPartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDTO.getPoNo(), grnDetDTO.getPartNo());
            List<Object[]> checkDuplicatePartNo = purDetRepository.checkDuplicatePartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDTO.getPoNo(), grnDetDTO.getPartNo(), grnDetDTO.getPoRecSeq());
            List<Object[]> getItemInfo = purDetRepository.getItemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), grnDTO.getPoNo(),
                    grnDetDTO.getItemNo(), grnDetDTO.getPartNo(), grnDetDTO.getPoRecSeq());
            int countItemNo = 0;
            int countPartNo = 0;
            String partNo = "";
            int recSeq = 0;
            for (Object[] data : checkDuplicatePartNo) {
                PurDetDTO dto = new PurDetDTO();
                dto.setRecSeq((Integer) data[0]);
                dto.setPartNo((String) data[1]);
                partNo = dto.getPartNo();
                recSeq = dto.getRecSeq();
            }
            for (Object[] data : checkItemNo) {
                countItemNo = (int) data[0];
            }
            for (Object[] data : checkPartNo) {
                countPartNo = (int) data[0];
            }
            if (grnDetDTO.getPartNo().equals(partNo) & grnDetDTO.getPoRecSeq() == recSeq) {
                map.put("contentData", "Duplicate Part No found!'");
                map.put("totalRecords", 0L);
            } else if (countItemNo == 0 && countPartNo == 0) {
                map.put("contentData", "The Part No is either invalid or qty fully received!");
                map.put("totalRecords", 0L);
            } else {
                for (Object[] dataItemInfo : getItemInfo) {
                    PurDetDTO dto = new PurDetDTO();
                    dto.setPartNo((String) dataItemInfo[0]);
                    dto.setRecSeq((Integer) dataItemInfo[1]);
                    dto.setItemNo((String) dataItemInfo[2]);
                    dto.setRemarks((String) dataItemInfo[3]);
                    dto.setMslCode((String) dataItemInfo[4]);
                    dto.setItemType((Integer) dataItemInfo[5]);
                    dto.setLoc((String) dataItemInfo[6]);
                    dto.setUom((String) dataItemInfo[7]);
                    dto.setProjectNo((String) dataItemInfo[8]);
                    dto.setOrderQty((BigDecimal) dataItemInfo[9]);
                    dto.setUnitPrice((BigDecimal) dataItemInfo[10]);
                    dto.setDueDate((Date) dataItemInfo[11]);
                    dto.setResvQty((BigDecimal) dataItemInfo[12]);
                    dto.setInvUom((String) dataItemInfo[13]);
                    dto.setStdPackQty((BigDecimal) dataItemInfo[14]);
                    dto.setRemarks((String) dataItemInfo[15]);
                    list.add(dto);
                }
                map.put("contentData", list);
                map.put("totalRecords", (long) list.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
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

        String msg;
        for (GrnDet grnDet : grnDetList) {
            int poRecSeq = 0;
            if (poRecSeq != grnDet.getPoRecSeq()) {
                poRecSeq = grnDet.getPoRecSeq();
                BigDecimal recQty = grnDet.getRecdQty();
                if (grnDet.getPoRecSeq() == poRecSeq) {
                    recQty = recQty.add(grnDet.getRecdQty());
                    if (recQty.intValue() > grnDet.getRecdQty().intValue()) {
                        msg = "Receiving more than Ordered is not allowed!";
                        ErrorMessage.builder().message(msg).build();
                    }
                }

            }
        }

    }
}
