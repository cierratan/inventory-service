package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.grn.GrnSupplierDTO;
import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.msr.MsrDTO;
import com.sunright.inventory.dto.msr.MsrDetailDTO;
import com.sunright.inventory.dto.search.Filter;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.entity.inaudit.InAudit;
import com.sunright.inventory.entity.itemloc.ItemLoc;
import com.sunright.inventory.entity.item.ItemProjection;
import com.sunright.inventory.entity.bombypj.BombypjProjection;
import com.sunright.inventory.entity.docmno.DocmNoProjection;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.grn.GrnSupplierProjection;
import com.sunright.inventory.entity.itembatc.ItemBatc;
import com.sunright.inventory.entity.itembatc.ItemBatcId;
import com.sunright.inventory.entity.itembatclog.ItemBatcLog;
import com.sunright.inventory.entity.itembatclog.ItemBatcLogId;
import com.sunright.inventory.entity.msr.MSR;
import com.sunright.inventory.entity.msr.MSRDetail;
import com.sunright.inventory.exception.NotFoundException;
import com.sunright.inventory.exception.ServerException;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.*;
import com.sunright.inventory.service.MSRService;
import com.sunright.inventory.util.QueryGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;

@Service
public class MSRServiceImpl implements MSRService {

    @Autowired
    private MSRRepository msrRepository;

    @Autowired
    private MsrDetailRepository msrDetailRepository;

    @Autowired
    private DocmNoRepository docmNoRepository;

    @Autowired
    private GrnRepository grnRepository;

    @Autowired
    private BombypjRepository bombypjRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemLocRepository itemLocRepository;

    @Autowired
    private ItemBatcRepository itemBatcRepository;

    @Autowired
    private ItemBatcLogRepository itemBatcLogRepository;

    @Autowired
    private InAuditRepository inAuditRepository;

    @Autowired
    private QueryGenerator queryGenerator;

    @Override
    @Transactional
    public MsrDTO createMSR(MsrDTO input) {
        UserProfile userProfile = UserProfileContext.getUserProfile();

        MSR msr = new MSR();
        BeanUtils.copyProperties(input, msr);

        msr.setCompanyCode(userProfile.getCompanyCode());
        msr.setPlantNo(userProfile.getPlantNo());
        msr.setStatus(Status.ACTIVE);
        msr.setCreatedBy(userProfile.getUsername());
        msr.setCreatedAt(ZonedDateTime.now());
        msr.setUpdatedBy(userProfile.getUsername());
        msr.setUpdatedAt(ZonedDateTime.now());

        MSR saved = msrRepository.save(msr);

        if(!CollectionUtils.isEmpty(input.getMsrDetails())) {
            for (MsrDetailDTO detail : input.getMsrDetails()) {
                MSRDetail msrDetail = new MSRDetail();
                BeanUtils.copyProperties(detail, msrDetail);
                msrDetail.setCompanyCode(userProfile.getCompanyCode());
                msrDetail.setPlantNo(userProfile.getPlantNo());
                msrDetail.setMsr(saved);

                msrDetailRepository.save(msrDetail);

                msrDetailPostSaving(msrDetail);
            }
        }

        msrPostSaving(userProfile);

        input.setId(saved.getId());
        input.setVersion(saved.getVersion());
        return input;
    }

    @Override
    public MsrDTO findBy(Long id) {
        return convertToMsrDTO(checkIfRecordExist(id));
    }


    @Override
    public SearchResult<MsrDTO> searchBy(SearchRequest searchRequest) {
        Specification<MSR> specs = where(queryGenerator.createDefaultSpec());

        if(!CollectionUtils.isEmpty(searchRequest.getFilters())) {
            for (Filter filter : searchRequest.getFilters()) {
                specs = specs.and(queryGenerator.createSpecification(filter));
            }
        }

        Page<MSR> pgMSR = msrRepository.findAll(specs, queryGenerator.constructPageable(searchRequest));

        SearchResult<MsrDTO> result = new SearchResult<>();
        result.setTotalRows(pgMSR.getTotalElements());
        result.setTotalPages(pgMSR.getTotalPages());
        result.setCurrentPageNumber(pgMSR.getPageable().getPageNumber());
        result.setCurrentPageSize(pgMSR.getNumberOfElements());
        result.setRows(pgMSR.getContent().stream().map(msr -> convertToMsrDTO(msr)).collect(Collectors.toList()));

        return result;
    }

    @Override
    public DocmValueDTO getGeneratedNo() {
        DocmNoProjection docmNo = docmNoRepository.getLastGeneratedNo(
                UserProfileContext.getUserProfile().getCompanyCode(),
                UserProfileContext.getUserProfile().getPlantNo(),
                "MSR",
                "N");

        return DocmValueDTO.builder()
                .generatedNo(docmNo.getGeneratedNo())
                .docmNo(docmNo.getDocmNo())
                .build();
    }

    @Override
    public GrnSupplierDTO findSupplierByGrnNo(String grnNo) {
        GrnSupplierProjection grnSupplier = grnRepository.getSupplierByGrn(UserProfileContext.getUserProfile().getCompanyCode(),
                UserProfileContext.getUserProfile().getPlantNo(), grnNo);

        if (grnSupplier == null) {
            throw new NotFoundException(String.format("GrnNo: %s is not found", grnNo));
        }

        List<MsrDetailDTO> msrDetails = msrDetailRepository.populateMSRDetailBy(UserProfileContext.getUserProfile().getCompanyCode(),
                UserProfileContext.getUserProfile().getPlantNo(),
                grnSupplier.getGrnNo()).stream().map(detail -> {
                    MsrDetailDTO msrDetail = MsrDetailDTO.builder()
                            .itemType(detail.getItemType())
                            .itemNo(detail.getItemNo())
                            .partNo(detail.getPartNo())
                            .loc(detail.getLoc())
                            .uom(detail.getUom())
                            .batchNo(detail.getBatchNo() != null ? String.valueOf(detail.getBatchNo()) : "")
                            .projectNo(detail.getProjectNo())
                            .grnType(detail.getGrnType())
                            .grnNo(detail.getGrnNo())
                            .grndetSeqNo(detail.getGrndetSeqNo())
                            .grndetRecdQty(detail.getGrndetRecdQty())
                            .retnPrice(detail.getRetnPrice())
                            .retnQty(detail.getRetnQty())
                            .build();

                    return msrDetail;
                }
        ).collect(Collectors.toList());

        return GrnSupplierDTO.builder()
                .grnId(grnSupplier.getGrnId())
                .grnNo(grnSupplier.getGrnNo())
                .supplierCode(grnSupplier.getSupplierCode())
                .name(grnSupplier.getName())
                .msrDetails(new HashSet(msrDetails))
                .build();
    }

    private MSR checkIfRecordExist(Long id) {
        Optional<MSR> optionalItem = msrRepository.findById(id);

        if (!optionalItem.isPresent()) {
            throw new NotFoundException("Record is not found");
        }
        return optionalItem.get();
    }

    private MsrDTO convertToMsrDTO(MSR msr) {
        Set<MsrDetailDTO> msrDetails = new HashSet<>();
        if(!CollectionUtils.isEmpty(msr.getMsrDetails())) {
            msrDetails = msr.getMsrDetails().stream().map(detail -> {
                MsrDetailDTO msrDetail = MsrDetailDTO.builder()
                        .msrNo(detail.getMsrNo())
                        .seqNo(detail.getSeqNo())
                        .build();

                BeanUtils.copyProperties(detail, msrDetail);

                return msrDetail;
            }).collect(Collectors.toSet());
        }

        MsrDTO msrDTO = MsrDTO.builder().build();

        BeanUtils.copyProperties(msr.getId(), msrDTO);
        BeanUtils.copyProperties(msr, msrDTO);
        msrDTO.setMsrDetails(msrDetails);
        return msrDTO;
    }

    private void msrPostSaving(UserProfile userProfile) {
        String type = "MSR";
        String subType = "N";

        DocmNoProjection docmNo = docmNoRepository.getLastGeneratedNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), type, subType);
        docmNoRepository.updateLastGeneratedNo(docmNo.getDocmNo(), userProfile.getCompanyCode(), userProfile.getPlantNo(), subType, type);
    }

    private void msrDetailPostSaving(MSRDetail msrDetail) {
        UserProfile userProfile = UserProfileContext.getUserProfile();

        BombypjProjection bombypj = bombypjRepository.getBombypjInfo(userProfile.getCompanyCode(),
                userProfile.getPlantNo(), msrDetail.getProjectNo(), msrDetail.getItemNo());

        // FIXME: need to revisit once MRV is done
        BigDecimal projReturn = BigDecimal.ZERO;
        BigDecimal returnQty = msrDetail.getRetnQty();
        BigDecimal pickedQty = bombypj.getPickedQty() == null ? BigDecimal.ZERO : bombypj.getPickedQty();
        BigDecimal shortQty = bombypj.getShortQty() == null ? BigDecimal.ZERO : bombypj.getShortQty();
        BigDecimal itemPickQty = BigDecimal.ZERO;
        BigDecimal itemMrvResv = BigDecimal.ZERO;
        BigDecimal itemProdnResv = BigDecimal.ZERO;

        if(StringUtils.isNotBlank(msrDetail.getProjectNo())) {
            while(returnQty.compareTo(BigDecimal.ZERO) > 0) {
                if(pickedQty.compareTo(BigDecimal.ZERO) > 0) {
                    if(returnQty.compareTo(pickedQty) > 0) {
                        projReturn = pickedQty;
                        returnQty = returnQty.subtract(pickedQty);
                    } else {
                        projReturn = returnQty;
                        returnQty = BigDecimal.ZERO;
                    }

                    shortQty = shortQty.add(projReturn);
                    pickedQty = pickedQty.subtract(projReturn);
                    itemPickQty = itemPickQty.subtract(projReturn);

                    bombypjRepository.updatePickedQtyAndShortQty(pickedQty, shortQty,
                            userProfile.getCompanyCode(), userProfile.getPlantNo(),
                            bombypj.getComponent(), bombypj.getOrderNo(), bombypj.getAlternate(),
                            bombypj.getProjectNo(), bombypj.getAssemblyNo());
                }
            }
        }

        ItemProjection item = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), msrDetail.getItemNo());
        BigDecimal iPickedQty = (item.getPickedQty() == null ? BigDecimal.ZERO : item.getPickedQty()).add(itemPickQty);
        BigDecimal iMrvResv = (item.getMrvResv() == null ? BigDecimal.ZERO : item.getMrvResv()).add(itemMrvResv);
        BigDecimal iProdnResv = (item.getProdnResv() == null ? BigDecimal.ZERO : item.getProdnResv()).add(itemProdnResv);

        itemRepository.updatePickedQtyMrvResvProdnResv(iPickedQty, iMrvResv, iProdnResv,
                userProfile.getCompanyCode(), userProfile.getPlantNo(), msrDetail.getItemNo());

        itemLocRepository.updatePickedQtyMrvResvProdnResv(iPickedQty, iMrvResv, iProdnResv,
                userProfile.getCompanyCode(), userProfile.getPlantNo(), msrDetail.getItemNo(), msrDetail.getLoc());

        // update itembatc
        ItemBatcId itemBatcId = new ItemBatcId();
        itemBatcId.setCompanyCode(userProfile.getCompanyCode());
        itemBatcId.setPlantNo(userProfile.getPlantNo());
        itemBatcId.setItemNo(msrDetail.getItemNo());
        itemBatcId.setBatchNo(Long.parseLong(msrDetail.getBatchNo()));
        itemBatcId.setLoc(msrDetail.getLoc());

        Optional<ItemBatc> itemBatcFound = itemBatcRepository.findById(itemBatcId);

        if(!itemBatcFound.isPresent()) {
            throw new NotFoundException(String.format("Itembatc is not found for %s", itemBatcId));
        }

        ItemBatc itemBatc = itemBatcFound.get();
        if(itemBatc.getQoh().compareTo(msrDetail.getRetnQty()) < 0) {
            throw new ServerException(String.format("ItemNo: %s Issued Qty > Batch Qty", msrDetail.getItemNo()));
        }

        BigDecimal itembatcBal = itemBatc.getQoh().subtract(msrDetail.getRetnQty());

        if(itembatcBal.compareTo(BigDecimal.ZERO) == 0) {
            itemBatcRepository.deleteById(itemBatcId);
        } else {
            itemBatcRepository.updateQoh(itembatcBal, userProfile.getCompanyCode(), userProfile.getPlantNo(),
                    Long.parseLong(msrDetail.getBatchNo()), msrDetail.getItemNo(), msrDetail.getLoc());
        }

        ItemBatcLogId itemBatcLogId = new ItemBatcLogId();
        itemBatcLogId.setCompanyCode(userProfile.getCompanyCode());
        itemBatcLogId.setPlantNo(userProfile.getPlantNo());
        itemBatcLogId.setItemNo(msrDetail.getItemNo());
        itemBatcLogId.setBatchNo(Long.parseLong(msrDetail.getBatchNo()));
        itemBatcLogId.setSivNo(msrDetail.getMsrNo());
        itemBatcLogId.setLoc(msrDetail.getLoc());

        ItemBatcLog itemBatcLog = new ItemBatcLog();
        itemBatcLog.setId(itemBatcLogId);
        itemBatcLog.setSivQty(msrDetail.getRetnQty());
        itemBatcLog.setDateCode(itemBatc.getDateCode());
        itemBatcLog.setPoNo(itemBatc.getPoNo());
        itemBatcLog.setPoRecSeq(itemBatc.getPoRecSeq());
        itemBatcLog.setGrnNo(itemBatc.getGrnNo());
        itemBatcLog.setGrnSeq(itemBatc.getGrnSeq());
        itemBatcLog.setGrnQty(itemBatc.getOriQoh());
        itemBatcLog.setStatus(Status.ACTIVE);
        itemBatcLog.setCreatedBy(userProfile.getUsername());
        itemBatcLog.setCreatedAt(ZonedDateTime.now());
        itemBatcLog.setUpdatedBy(userProfile.getUsername());
        itemBatcLog.setUpdatedAt(ZonedDateTime.now());

        itemBatcLogRepository.save(itemBatcLog);

        // itemloc
        List<ItemLoc> itemLocs = itemLocRepository.findByCompanyCodeAndPlantNoAndItemNoAndLoc(userProfile.getCompanyCode(),
                userProfile.getPlantNo(), msrDetail.getItemNo(), msrDetail.getLoc());

        ZonedDateTime now = ZonedDateTime.now();
        if(!CollectionUtils.isEmpty(itemLocs)) {
            ItemLoc itemLoc = itemLocs.get(0);

            BigDecimal qoh = itemLoc.getQoh() == null ? BigDecimal.ZERO : itemLoc.getQoh();
            BigDecimal ytdProd = itemLoc.getYtdProd() == null ? BigDecimal.ZERO : itemLoc.getYtdProd();
            BigDecimal ytdIssue = itemLoc.getYtdIssue() == null ? BigDecimal.ZERO : itemLoc.getYtdIssue();

            BigDecimal itemLocBal = qoh.subtract(msrDetail.getRetnQty());

            if(itemLocBal.compareTo(BigDecimal.ZERO) < 0) {
                throw new ServerException(String.format("ItemNo: %s Return Quantity > Quantity on Hand", msrDetail.getItemNo()));
            }

            BigDecimal ytdProdBal = ytdProd.add(msrDetail.getRetnQty());
            BigDecimal ytdIssueBal = ytdIssue.add(msrDetail.getRetnQty());
            Date lastTranDate = new Date(now.toLocalDate().toEpochDay());

            itemLocRepository.updateQohYtdProdYtdIssueLastTranDate(itemLocBal,
                    ytdProdBal,
                    ytdIssueBal,
                    lastTranDate,
                    userProfile.getCompanyCode(), userProfile.getPlantNo(), msrDetail.getItemNo(), msrDetail.getLoc());

            itemRepository.updateQohYtdProdYtdIssueLastTranDate(itemLocBal, ytdProdBal,
                    ytdIssueBal,
                    lastTranDate,
                    userProfile.getCompanyCode(), userProfile.getPlantNo(), msrDetail.getItemNo());

            // inaudit
            InAudit inAudit = new InAudit();
            inAudit.setCompanyCode(userProfile.getCompanyCode());
            inAudit.setPlantNo(userProfile.getPlantNo());
            inAudit.setItemlocId(itemLoc.getId());
            inAudit.setItemNo(msrDetail.getItemNo());
            inAudit.setTranDate(new Date(now.toLocalDate().toEpochDay()));
            inAudit.setTranTime("" + now.getHour() + now.getMinute() + now.getSecond());
            inAudit.setLoc(msrDetail.getLoc());
            inAudit.setTranType("IS");
            inAudit.setDocmNo(msrDetail.getMsrNo());
            inAudit.setOutQty(msrDetail.getRetnQty());
            inAudit.setOrderQty(msrDetail.getRetnQty());
            inAudit.setBalQty(itemLocBal);
            inAudit.setProjectNo(msrDetail.getProjectNo());
            inAudit.setCurrencyCode("SGD"); // FIXME: this is set at global level
            inAudit.setCurrencyRate(new BigDecimal(1));
            inAudit.setActualCost(itemLoc.getStdMaterial());
            inAudit.setGrnNo(itemBatc.getGrnNo());
            inAudit.setPoNo(itemBatc.getPoNo());
            inAudit.setDoNo("");
            inAudit.setRemarks(msrDetail.getRemarks());
            inAudit.setCreatedBy(userProfile.getUsername());
            inAudit.setCreatedAt(ZonedDateTime.now());
            inAudit.setUpdatedBy(userProfile.getUsername());
            inAudit.setUpdatedAt(ZonedDateTime.now());
            inAudit.setStatus(Status.ACTIVE);
            inAudit.setCreatedBy(userProfile.getUsername());
            inAudit.setCreatedAt(ZonedDateTime.now());
            inAudit.setUpdatedBy(userProfile.getUsername());
            inAudit.setUpdatedAt(ZonedDateTime.now());

            inAuditRepository.save(inAudit);
        }

    }
}
