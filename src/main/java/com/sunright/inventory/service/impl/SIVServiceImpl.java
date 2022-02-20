package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.ItemDTO;
import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.search.Filter;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.dto.siv.SIVDTO;
import com.sunright.inventory.dto.siv.SIVDetailDTO;
import com.sunright.inventory.dto.wip.WipProjDTO;
import com.sunright.inventory.entity.InAudit;
import com.sunright.inventory.entity.ItemLocProjection;
import com.sunright.inventory.entity.ItemProjection;
import com.sunright.inventory.entity.bombypj.BombypjProjection;
import com.sunright.inventory.entity.bomproj.BomprojProjection;
import com.sunright.inventory.entity.company.CompanyProjection;
import com.sunright.inventory.entity.coq.*;
import com.sunright.inventory.entity.docmno.DocmNoProjection;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.grn.GrnDetailProjection;
import com.sunright.inventory.entity.itembatc.ItemBatchProjection;
import com.sunright.inventory.entity.itembatclog.ItemBatcLog;
import com.sunright.inventory.entity.itembatclog.ItemBatcLogId;
import com.sunright.inventory.entity.itembatclog.ItemBatcLogProjection;
import com.sunright.inventory.entity.pr.PRDetailProjection;
import com.sunright.inventory.entity.pr.PRProjection;
import com.sunright.inventory.entity.product.ProductProjection;
import com.sunright.inventory.entity.sale.SaleDetailProjection;
import com.sunright.inventory.entity.sale.SaleProjection;
import com.sunright.inventory.entity.sfcwip.SfcWip;
import com.sunright.inventory.entity.sfcwip.SfcWipId;
import com.sunright.inventory.entity.sfcwip.SfcWipProjection;
import com.sunright.inventory.entity.siv.SIV;
import com.sunright.inventory.entity.siv.SIVDetail;
import com.sunright.inventory.entity.wip.WipDirsProjection;
import com.sunright.inventory.entity.wip.WipProjProjection;
import com.sunright.inventory.exception.DuplicateException;
import com.sunright.inventory.exception.NotFoundException;
import com.sunright.inventory.exception.ServerException;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.*;
import com.sunright.inventory.repository.lov.LocationRepository;
import com.sunright.inventory.service.SIVService;
import com.sunright.inventory.util.QueryGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;

@Transactional
@Service
public class SIVServiceImpl implements SIVService {

    @Autowired
    private SIVRepository sivRepository;

    @Autowired
    private SIVDetailRepository sivDetailRepository;

    @Autowired
    private DocmNoRepository docmNoRepository;

    @Autowired
    private BombypjRepository bombypjRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private SaleDetailRepository saleDetailRepository;

    @Autowired
    private ItemLocRepository itemLocRepository;

    @Autowired
    private ItemBatcRepository itemBatcRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private BomprojRepository bomprojRepository;

    @Autowired
    private GrnDetRepository grnDetRepository;

    @Autowired
    private ItemBatcLogRepository itemBatcLogRepository;

    @Autowired
    private InAuditRepository inAuditRepository;

    @Autowired
    private COQRepository coqRepository;

    @Autowired
    private COQDetailRepository coqDetailRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SfcWipRepository sfcWipRepository;

    @Autowired
    private WipProjRepository wipProjRepository;

    @Autowired
    private WipDirsRepository wipDirsRepository;

    @Autowired
    private PRDetailRepository prDetailRepository;

    @Autowired
    private PRRepository prRepository;

    @Autowired
    private QueryGenerator queryGenerator;

    @Override
    public SIVDTO createSIV(SIVDTO input) {

        UserProfile userProfile = UserProfileContext.getUserProfile();

        SIV siv = new SIV();
        BeanUtils.copyProperties(input, siv);

        siv.setCompanyCode(userProfile.getCompanyCode());
        siv.setPlantNo(userProfile.getPlantNo());
        siv.setStatus(Status.ACTIVE);
        siv.setCreatedBy(userProfile.getUsername());
        siv.setCreatedAt(ZonedDateTime.now());
        siv.setUpdatedBy(userProfile.getUsername());
        siv.setUpdatedAt(ZonedDateTime.now());

        checkRecNull(input);
        checkIfSivNoExist(userProfile, input);
        checkRecValid(input);
        SIV saved = sivRepository.save(siv);
        if (!CollectionUtils.isEmpty(input.getSivDetails())) {
            for (SIVDetailDTO detail : input.getSivDetails()) {
                SIVDetail sivDetail = new SIVDetail();
                checkRecValidDetail(userProfile, input, detail);
                preSavingDetail(userProfile, input, detail);
                BeanUtils.copyProperties(detail, sivDetail);
                sivDetail.setCompanyCode(userProfile.getCompanyCode());
                sivDetail.setPlantNo(userProfile.getPlantNo());
                sivDetail.setSiv(saved);
                sivDetailRepository.save(sivDetail);
                sivDetailPostSaving(userProfile, saved, sivDetail, detail, input);
                procBatchConsolidate(sivDetail, detail);
                procUpdateSfcWip(userProfile, sivDetail, detail, input);
            }
        }

        sivPostSaving(userProfile, saved);
        populateAfterSaving(input, saved);

        return input;
    }

    private void procUpdateSfcWip(UserProfile userProfile, SIVDetail sivDetail, SIVDetailDTO detail, SIVDTO input) {
        String type = input.getProjectNo().substring(0, 1);
        String subType = input.getProjectNo().substring(9, 11);
        ProductProjection wipTrackCur = productRepository.wipTrackCur(userProfile.getCompanyCode(), userProfile.getPlantNo(), type, subType);
        ItemProjection itemCat = itemRepository.itemCatCodePartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), detail.getItemNo());
        SfcWipProjection wipCur = sfcWipRepository.wipCur(input.getProjectNo(), itemCat.getPartNo());
        if (wipTrackCur.getWipTracking().equals("Y")) {
            if (itemCat.getCategoryCode().equals("01")) {
                if (wipCur == null) {
                    SfcWip sfcWip = new SfcWip();
                    SfcWipId id = new SfcWipId();
                    id.setProjectNoSub(input.getProjectNo());
                    id.setPcbPartNo(itemCat.getPartNo());
                    sfcWip.setPcbQty(sivDetail.getIssuedQty());
                    sfcWip.setFlowId(null);
                    sfcWip.setStatus("O");
                    sfcWip.setId(id);
                    sfcWipRepository.save(sfcWip);
                } else {
                    BigDecimal pcbQty = wipCur.getPcbQty().add(sivDetail.getIssuedQty());
                    sfcWipRepository.updatePcbQty(pcbQty, input.getProjectNo(), itemCat.getPartNo());
                }
            }
        }
    }

    private SIVDetailDTO procBatchConsolidate(SIVDetail sivDetail, SIVDetailDTO detail) {
        String itemNo = sivDetail.getItemNo();
        Long batchNo = sivDetail.getBatchNo();
        String loc = sivDetail.getLoc();
        BigDecimal issuedQty = BigDecimal.ZERO;
        BigDecimal bomPickQty = BigDecimal.ZERO;
        BigDecimal exIssued = BigDecimal.ZERO;
        BigDecimal bomShortL = BigDecimal.ZERO;
        BigDecimal bomShortF = BigDecimal.ZERO;

        if (itemNo.equals(sivDetail.getItemNo()) && batchNo.equals(sivDetail.getBatchNo()) && loc.equals(sivDetail.getLoc())) {
            issuedQty = issuedQty.add(sivDetail.getIssuedQty());
            exIssued = sivDetail.getIssuedQty().add(sivDetail.getExtraQty());
            bomPickQty = bomPickQty.add(detail.getBomPickQty());
            bomShortL = sivDetail.getIssuedQty().add(detail.getBomShortQtyL());
            bomShortF = sivDetail.getIssuedQty().add(detail.getBomShortQtyF());
        }
        detail.setIssuedQty(detail.getIssuedQty().add(issuedQty));
        detail.setExtraQty(detail.getExtraQty().add(exIssued));
        detail.setBomPickQty(detail.getBomPickQty().add(bomPickQty));
        detail.setBomShortQtyL(detail.getBomShortQtyL().add(bomShortL));
        detail.setBomShortQtyF(detail.getBomShortQtyF().add(bomShortF));

        return detail;
    }

    private void sivDetailPostSaving(UserProfile userProfile, SIV siv, SIVDetail sivDetail, SIVDetailDTO detail, SIVDTO input) {

        if (detail.getSubType().equals("M")) {
            if (detail.getItemType() == 0) {
                reduceItemInv(userProfile, input, detail);
                postBombypj(userProfile, siv, sivDetail, detail);
            }
        } else {
            postBombypj(userProfile, siv, sivDetail, detail);
            postCoq(userProfile, siv, sivDetail, detail);
        }
    }

    private void postBombypj(UserProfile userProfile, SIV siv, SIVDetail sivDetail, SIVDetailDTO detail) {

        BigDecimal shortQty = detail.getBomShortQtyL().add(detail.getBomShortQtyF());
        List<BombypjProjection> bombypjInfoByStatus = bombypjRepository.getBombypjInfoByStatus(userProfile.getCompanyCode(),
                userProfile.getPlantNo(), siv.getProjectNo(), sivDetail.getItemNo(), shortQty);

        BigDecimal pickIss = sivDetail.getIssuedQty();
        BigDecimal shortIss = shortQty;
        for (BombypjProjection bProj : bombypjInfoByStatus) {
            BigDecimal resvQty = bProj.getResvQty() == null ? BigDecimal.ZERO : bProj.getResvQty();
            BigDecimal issuedQty = bProj.getIssuedQty() == null ? BigDecimal.ZERO : bProj.getIssuedQty();
            BigDecimal pickedQty = bProj.getPickedQty() == null ? BigDecimal.ZERO : bProj.getPickedQty();
            BigDecimal vShortQty = bProj.getShortQty() == null ? BigDecimal.ZERO : bProj.getShortQty();
            if (shortIss.compareTo(BigDecimal.ZERO) > 0) {
                if (detail.getSubType().equals("M")) {
                    if (pickIss.compareTo(vShortQty) > 0) {
                        resvQty = resvQty.subtract(vShortQty);
                        issuedQty = issuedQty.add(vShortQty);
                        shortIss = shortIss.subtract(vShortQty);
                        pickIss = pickIss.subtract(vShortQty);
                        vShortQty = BigDecimal.ZERO;
                    } else {
                        resvQty = resvQty.subtract(pickIss);
                        issuedQty = issuedQty.add(pickIss);
                        vShortQty = vShortQty.subtract(pickIss);
                        shortIss = shortIss.subtract(pickIss);
                        pickIss = BigDecimal.ZERO;
                    }
                } else {
                    if (shortIss.compareTo(vShortQty) > 0) {
                        resvQty = resvQty.subtract(vShortQty);
                        issuedQty = issuedQty.add(vShortQty);
                        shortIss = shortIss.subtract(vShortQty);
                        pickIss = pickIss.subtract(vShortQty);
                        vShortQty = BigDecimal.ZERO;
                    } else {
                        resvQty = resvQty.subtract(shortIss);
                        issuedQty = issuedQty.add(shortIss);
                        vShortQty = vShortQty.subtract(shortIss);
                        pickIss = BigDecimal.ZERO;
                        shortIss = BigDecimal.ZERO;
                    }
                }
            } else {
                if (pickIss.compareTo(pickedQty) > 0) {
                    resvQty = resvQty.subtract(pickedQty);
                    issuedQty = issuedQty.add(pickedQty);
                    pickIss = pickIss.subtract(pickedQty);
                    pickedQty = BigDecimal.ZERO;
                } else {
                    resvQty = resvQty.subtract(pickIss);
                    issuedQty = issuedQty.add(pickIss);
                    pickedQty = pickedQty.subtract(pickIss);
                    pickIss = BigDecimal.ZERO;
                }
            }

            if (resvQty.compareTo(BigDecimal.ZERO) < 0) {
                resvQty = BigDecimal.ZERO;
            }

            bombypjRepository.updateResvIssuedPickedShortQty(resvQty, issuedQty, pickedQty, vShortQty,
                    userProfile.getCompanyCode(), userProfile.getPlantNo(), siv.getProjectNo(), sivDetail.getItemNo());
        }

        if (pickIss.compareTo(BigDecimal.ZERO) > 0 && bombypjInfoByStatus.size() != 0) {
            BombypjProjection bombypjInfo = bombypjRepository.getBombypjInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), siv.getProjectNo(), sivDetail.getItemNo());
            BigDecimal issuedQty = bombypjInfo.getIssuedQty().add(pickIss);
            bombypjRepository.updateIssuedQty(issuedQty, userProfile.getCompanyCode(), userProfile.getPlantNo(), bombypjInfo.getOrderNo(), siv.getProjectNo(), sivDetail.getItemNo());
        }
    }

    private void postCoq(UserProfile userProfile, SIV siv, SIVDetail sivDetail, SIVDetailDTO detail) {
        COQProjection coqRec = coqRepository.coqRec(userProfile.getCompanyCode(), userProfile.getPlantNo(),
                sivDetail.getItemNo(), siv.getProjectNo());
        COQProjection coqDet = coqDetailRepository.coqDet(userProfile.getCompanyCode(), userProfile.getPlantNo(), siv.getDocmNo());
        ItemProjection itemInfo = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), sivDetail.getItemNo());
        SaleProjection saleProj = saleRepository.saleCoqReasonsDet(userProfile.getCompanyCode(), userProfile.getPlantNo(), siv.getDocmNo());
        ItemBatcLogProjection iBatcLog = itemBatcLogRepository.getPoNoRecdPrice(userProfile.getCompanyCode(),
                userProfile.getPlantNo(), siv.getSivNo(), sivDetail.getBatchNo(), sivDetail.getItemNo());

        String docmNo = null;
        BigDecimal docmQty = null;
        Integer subSeq = null;
        String poNo = null;
        BigDecimal unitPrice = null;

        docmNo = coqRec.getDocmNo();
        docmQty = coqRec.getDocmQty();
        subSeq = coqRec.getSeqNo();
        poNo = iBatcLog.getPoNo();
        unitPrice = iBatcLog.getRecdPrice();

        if (StringUtils.isNotBlank(coqRec.getDocmNo())) {
            if (coqRec == null) {
                Integer maxDetRecSeq = null;
                Integer maxDetSeqNo = null;
                maxDetRecSeq = coqDet.getRecSeq();
                maxDetSeqNo = coqDet.getSeqNo();
                String partNo = null;
                String itemDesc = null;
                partNo = itemInfo.getPartNo();
                itemDesc = itemInfo.getDescription();
                String coqDiv = null;
                String coqDept = null;
                String reasonCode = null;
                String reasonDesc = null;
                coqDiv = saleProj.getCoqDivCode();
                coqDept = saleProj.getCoqDeptCode();
                reasonCode = saleProj.getReasonCode();
                reasonDesc = saleProj.getReasonDesc();
                Integer recSeq = maxDetRecSeq + 1;

                COQDetail coqDetail = new COQDetail();
                COQDetailId id = new COQDetailId();
                id.setCompanyCode(sivDetail.getCompanyCode());
                id.setPlantNo(sivDetail.getPlantNo());
                id.setDocmNo(docmNo);
                id.setRecSeq(recSeq);
                coqDetail.setSeqNo(maxDetSeqNo + 1);
                coqDetail.setItemType(0);
                coqDetail.setPartNo(partNo);
                coqDetail.setItemNo(sivDetail.getItemNo());
                coqDetail.setAssemblyNo("");
                coqDetail.setDescription(itemDesc);
                coqDetail.setDocmQty(sivDetail.getIssuedQty());
                coqDetail.setReasonCode(reasonCode);
                coqDetail.setReasonDesc(reasonDesc);
                coqDetail.setDivCode(coqDiv);
                coqDetail.setDeptCode(coqDept);
                coqDetail.setId(id);
                coqDetailRepository.save(coqDetail);
            } else {
                BigDecimal docmQtyUpdate = coqRec.getDocmQty().add(sivDetail.getIssuedQty());
                coqDetailRepository.updateDocmQty(docmQtyUpdate, userProfile.getCompanyCode(), userProfile.getPlantNo(), coqRec.getRecSeq(), coqRec.getDocmNo());
            }

            ZonedDateTime now = ZonedDateTime.now();
            Date entryDate = new Date(now.toLocalDate().toEpochDay());

            COQDetailSub coqDetailSub = new COQDetailSub();
            COQDetailSubId id = new COQDetailSubId();
            id.setCompanyCode(sivDetail.getCompanyCode());
            id.setPlantNo(sivDetail.getPlantNo());
            id.setDocmNo(docmNo);
            id.setDetRecSeq(coqRec.getRecSeq());
            id.setSeqNo(subSeq + 1);
            coqDetailSub.setSivNo(sivDetail.getSivNo());
            coqDetailSub.setQty(sivDetail.getIssuedQty());
            coqDetailSub.setPoNo(poNo);
            coqDetailSub.setUnitPrice(unitPrice);
            coqDetailSub.setEntryDate(entryDate);
        }
    }

    private SIVDetailDTO preSavingDetail(UserProfile userProfile, SIVDTO input, SIVDetailDTO detail) {

        if (input.getSubType().equals("M")) {
            detail.setSivNo(input.getSivNo());
            detail.setSubType("M");
            if (!input.getTranType().equals("OTHER")) {
                computeIssuedQtyTotal(detail);
            }

            if (detail.getItemType() == 0) {
                if (input.getTranType().equals("PR") && input.getTranType().equals("WK")) {
                    String recValidSub;
                    if (detail.getSaleType().equals("P")) {
                        recValidSub = "PROJECT_NO";
                        if (StringUtils.isBlank(detail.getProjectNo1())) {
                            throw new ServerException("A Project No is needed!");
                        } else {
                            checkRecValidSub(userProfile, detail, detail.getProjectNo1(), recValidSub);
                        }

                        if (StringUtils.isNotBlank(detail.getProjectNo2())) {
                            checkRecValidSub(userProfile, detail, detail.getProjectNo2(), recValidSub);
                        }

                        if (StringUtils.isNotBlank(detail.getProjectNo3())) {
                            checkRecValidSub(userProfile, detail, detail.getProjectNo3(), recValidSub);
                        }

                        if (StringUtils.isNotBlank(detail.getProjectNo4())) {
                            checkRecValidSub(userProfile, detail, detail.getProjectNo4(), recValidSub);
                        }

                        if (StringUtils.isNotBlank(detail.getProjectNo5())) {
                            checkRecValidSub(userProfile, detail, detail.getProjectNo5(), recValidSub);
                        }
                    } else {
                        recValidSub = "ORDER_NO";
                        if (StringUtils.isNotBlank(detail.getProjectNo1())) {
                            checkRecValidSub(userProfile, detail, detail.getProjectNo1(), recValidSub);
                        }

                        if (StringUtils.isNotBlank(detail.getProjectNo2())) {
                            checkRecValidSub(userProfile, detail, detail.getProjectNo2(), recValidSub);
                        }

                        if (StringUtils.isNotBlank(detail.getProjectNo3())) {
                            checkRecValidSub(userProfile, detail, detail.getProjectNo3(), recValidSub);
                        }

                        if (StringUtils.isNotBlank(detail.getProjectNo4())) {
                            checkRecValidSub(userProfile, detail, detail.getProjectNo4(), recValidSub);
                        }

                        if (StringUtils.isNotBlank(detail.getProjectNo5())) {
                            checkRecValidSub(userProfile, detail, detail.getProjectNo5(), recValidSub);
                        }
                    }
                } else if (input.getTranType().equals("DS") && input.getTranType().equals("WD")) {
                    if (detail.getItemType() == 0) {
                        List<ItemProjection> itemCur = itemRepository.itemCur(input.getDocmNo(), detail.getCompanyCode(), detail.getPlantNo(), detail.getItemNo());
                        ItemDTO dto = ItemDTO.builder().build();
                        for (ItemProjection rec : itemCur) {
                            dto.setItemNo(rec.getItemNo());
                            dto.setLoc(rec.getLoc());
                            dto.setUom(rec.getUom());
                        }
                        if (StringUtils.isBlank(dto.getItemNo())) {
                            throw new ServerException("Item No : " + detail.getItemNo() + " is invalid or not found in SALEDET/ITEM Table !");
                        }
                    } else {
                        throw new ServerException("Please Enter Only Stock Item!");
                    }
                }
            }
        } else {
            List<GrnDetailProjection> grnDetCur = grnDetRepository.getGrndetCur(userProfile.getCompanyCode(),
                    userProfile.getPlantNo(), detail.getBatchNo(), detail.getItemNo());
            for (GrnDetailProjection prj : grnDetCur) {
                detail.setGrnNo(prj.getGrnNo());
                detail.setUom(prj.getUom());
                detail.setGrnDetSeqNo(prj.getSeqNo());
                if (prj.getIssuedQty().compareTo(BigDecimal.ZERO) == 0) {
                    BigDecimal issuedQty = (prj.getIssuedQty() == null ? BigDecimal.ZERO : prj.getIssuedQty()).add(detail.getIssuedQty());
                    grnDetRepository.updateSivNoIssuedQty(input.getSivNo(), issuedQty, prj.getGrnNo(), prj.getSeqNo());
                }
            }

            reduceItemInv(userProfile, input, detail);
        }

        return detail;
    }

    private void checkRecValidSub(UserProfile userProfile, SIVDetailDTO detail, String projectNo, String recValidSub) {

        if (recValidSub.equals("PROJECT_NO")) {
            List<WipProjProjection> projectCurs = wipProjRepository.projectCur(userProfile.getCompanyCode(), userProfile.getPlantNo(), detail.getDocmNo(), projectNo);
            WipProjDTO dto = WipProjDTO.builder().build();
            for (WipProjProjection prjCur : projectCurs) {
                dto.setProjectNoSub(prjCur.getProjectNoSub());
            }
            if (StringUtils.isBlank(dto.getProjectNoSub())) {
                throw new ServerException("Project No is Invalid or not found in WIPPROJ/SALEDET Table !");
            }
        }

        if (recValidSub.equals("ORDER_NO")) {
            List<WipDirsProjection> orderCurs = wipDirsRepository.orderCur(userProfile.getCompanyCode(), userProfile.getPlantNo(), projectNo);
            WipProjDTO dto = WipProjDTO.builder().build();
            for (WipDirsProjection orderCur : orderCurs) {
                dto.setProjectNoSub(orderCur.getOrderNo());
            }
            if (StringUtils.isBlank(dto.getProjectNoSub())) {
                throw new ServerException("Project No is Invalid or not found in WIPPROJ/SALEDET Table !");
            }
        }
    }

    private void computeIssuedQtyTotal(SIVDetailDTO detail) {
        detail.setIssuedQty(detail.getIssuedQty1().add(detail.getIssuedQty2().add(detail.getIssuedQty3().add(detail.getIssuedQty4().add(detail.getIssuedQty5())))));
    }

    private SIVDetailDTO reduceItemInv(UserProfile userProfile, SIVDTO input, SIVDetailDTO detail) {

        BigDecimal itemPickQty = detail.getIssuedQty().subtract(detail.getExtraQty())
                .subtract(detail.getBomShortQtyL().add(detail.getBomShortQtyF()));
        BigDecimal itemBatchBal = null;
        List<ItemBatchProjection> itemBatcPrj = itemBatcRepository.getItemBatchByBatchNo(userProfile.getCompanyCode(),
                userProfile.getPlantNo(), detail.getItemNo(), detail.getBatchNo(), detail.getLoc());
        ItemLocProjection itemLoc = itemLocRepository.itemLocByItemNo(userProfile.getCompanyCode(),
                userProfile.getPlantNo(), detail.getItemNo(), detail.getLoc());
        ItemProjection itemQoh = itemRepository.getQohByItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(),
                detail.getItemNo(), detail.getLoc());
        CompanyProjection coStkLoc = companyRepository.getStockLoc(userProfile.getCompanyCode(), userProfile.getPlantNo());
        ItemProjection itemInfo = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), detail.getItemNo());
        for (ItemBatchProjection itemBatc : itemBatcPrj) {
            if (itemBatc.getQoh().compareTo(detail.getIssuedQty()) < 0) {
                throw new ServerException("Item No : " + detail.getItemNo() + " Issued Qty > Batch Qty!");
            } else {
                itemBatchBal = itemBatc.getQoh().subtract(detail.getIssuedQty());
                if (itemBatchBal.compareTo(BigDecimal.ZERO) == 0) {
                    itemBatcRepository.deleteItemBatcBal(detail.getItemNo(), detail.getBatchNo());
                } else {
                    itemBatcRepository.updateItemBatcBal(itemBatchBal, detail.getBatchNo());
                }

                ItemBatcLog itemBatcLog = new ItemBatcLog();
                ItemBatcLogId id = new ItemBatcLogId();
                id.setCompanyCode(userProfile.getCompanyCode());
                id.setPlantNo(userProfile.getPlantNo());
                id.setItemNo(detail.getItemNo());
                id.setBatchNo(detail.getBatchNo());
                id.setSivNo(detail.getDocmNo());
                id.setLoc(detail.getLoc());
                itemBatcLog.setSivQty(detail.getIssuedQty());
                itemBatcLog.setDateCode(itemBatc.getDateCode());
                itemBatcLog.setPoNo(itemBatc.getPoNo());
                itemBatcLog.setPoRecSeq(itemBatc.getPoRecSeq());
                itemBatcLog.setGrnNo(itemBatc.getGrnNo());
                itemBatcLog.setGrnSeq(itemBatc.getGrnSeq());
                itemBatcLog.setGrnQty(itemBatc.getOriQoh());
                itemBatcLog.setId(id);
                itemBatcLogRepository.save(itemBatcLog);
            }
            detail.setPoNo(itemBatc.getPoNo());
            detail.setGrnNo(itemBatc.getGrnNo());
        }
        if (itemBatchBal == null) {
            throw new NotFoundException("Batch No : " + detail.getBatchNo() + " not found!");
        }

        BigDecimal itemLocBal = itemLoc.getQoh().subtract(detail.getIssuedQty());
        BigDecimal prodnResv = (itemLoc.getProdnResv() == null ? BigDecimal.ZERO : itemLoc.getProdnResv())
                .subtract(detail.getIssuedQty().subtract(detail.getExtraQty()));
        BigDecimal pickedQty = (itemLoc.getPickedQty() == null ? BigDecimal.ZERO : itemLoc.getPickedQty())
                .subtract(itemPickQty);
        BigDecimal qoh = (itemLoc.getQoh() == null ? BigDecimal.ZERO : itemLoc.getQoh())
                .subtract(detail.getIssuedQty());
        BigDecimal ytdProd = (itemLoc.getYtdProd() == null ? BigDecimal.ZERO : itemLoc.getYtdProd())
                .add(detail.getIssuedQty());
        BigDecimal ytdIssue = (itemLoc.getYtdIssue() == null ? BigDecimal.ZERO : itemLoc.getYtdIssue())
                .add(detail.getIssuedQty());
        ZonedDateTime now = ZonedDateTime.now();
        Date lastTranDate = new Date(now.toLocalDate().toEpochDay());
        if (itemLocBal == null) {
            throw new NotFoundException("Item No : " + detail.getItemNo() + " not found in ITEMLOC!");
        } else if (itemLocBal.compareTo(BigDecimal.ZERO) < 0) {
            throw new ServerException("Item No : " + detail.getItemNo() + " Issued Qty > Qty-On-Hand!");
        } else {
            if (detail.getLoc().equals(coStkLoc.getStockLoc())) {
                itemLocRepository.updateProdnResvPickedQtyQohYtdProdTydIssueLTranDate(prodnResv, pickedQty, qoh, ytdProd, ytdIssue,
                        lastTranDate, userProfile.getCompanyCode(), userProfile.getPlantNo(), detail.getItemNo(), detail.getLoc());
            } else {
                itemLocRepository.updateProdnResvPickedQty(prodnResv, pickedQty, userProfile.getCompanyCode(),
                        userProfile.getPlantNo(), detail.getItemNo(), detail.getLoc());
                itemLocRepository.updateQohYtdProdTydIssueLTranDate(qoh, ytdProd, ytdIssue,
                        lastTranDate, userProfile.getCompanyCode(),
                        userProfile.getPlantNo(), detail.getItemNo(), detail.getLoc());
            }
        }

        BigDecimal prodnResvItem = (itemInfo.getProdnResv() == null ? BigDecimal.ZERO : itemInfo.getProdnResv())
                .subtract(detail.getIssuedQty().subtract(detail.getExtraQty()));
        BigDecimal pickedQtyItem = (itemInfo.getPickedQty() == null ? BigDecimal.ZERO : itemInfo.getPickedQty())
                .subtract(itemPickQty);
        BigDecimal qohItem = (itemInfo.getQoh() == null ? BigDecimal.ZERO : itemInfo.getQoh())
                .subtract(detail.getIssuedQty());
        BigDecimal ytdProdItem = (itemInfo.getYtdProd() == null ? BigDecimal.ZERO : itemInfo.getYtdProd())
                .add(detail.getIssuedQty());
        BigDecimal ytdIssueItem = (itemInfo.getYtdIssue() == null ? BigDecimal.ZERO : itemInfo.getYtdIssue())
                .add(detail.getIssuedQty());

        if (itemQoh == null) {
            throw new NotFoundException("Item No : " + detail.getItemNo() + " not found in ITEM!");
        } else {
            itemRepository.updateProdnResvPickedQtyQohYtdProdTydIssueLTranDate(prodnResvItem, pickedQtyItem, qohItem, ytdProdItem, ytdIssueItem,
                    lastTranDate, userProfile.getCompanyCode(), userProfile.getPlantNo(), detail.getItemNo(), detail.getLoc());
        }

        InAudit inAudit = new InAudit();
        inAudit.setCompanyCode(userProfile.getCompanyCode());
        inAudit.setPlantNo(userProfile.getPlantNo());
        inAudit.setItemNo(detail.getItemNo());
        inAudit.setLoc(detail.getLoc());
        inAudit.setTranDate(lastTranDate);
        inAudit.setTranTime(new Timestamp(System.currentTimeMillis()).toString());
        inAudit.setTranType("IM");
        inAudit.setDocmNo(detail.getDocmNo());
        inAudit.setOutQty(detail.getIssuedQty());
        inAudit.setOrderQty(detail.getIssuedQty());
        inAudit.setBalQty(itemLocBal);
        inAudit.setProjectNo(input.getProjectNo());
        inAudit.setCurrencyCode(input.getCurrencyCode());
        inAudit.setCurrencyRate(input.getCurrencyRate());
        inAudit.setActualCost(itemLoc.getStdMaterial());
        inAudit.setGrnNo(detail.getGrnNo());
        inAudit.setPoNo(detail.getPoNo());
        inAudit.setDoNo(null);
        if (detail.getSubType().equals("M")) {
            inAudit.setRemarks("Issued Thru SIV-M, Prg : INM00005");
        } else {
            inAudit.setRemarks("Issued Thru SIV-N, Prg : INM00004");
        }
        inAudit.setStatus(Status.ACTIVE);
        inAudit.setCreatedBy(userProfile.getUsername());
        inAudit.setCreatedAt(ZonedDateTime.now());
        inAudit.setUpdatedBy(userProfile.getUsername());
        inAudit.setUpdatedAt(ZonedDateTime.now());
        inAuditRepository.save(inAudit);

        return detail;
    }

    private void checkRecNull(SIVDTO input) {
        if (input.getSivNo() == null) {
            throw new ServerException(String.format("SIV No: %s Can Not be Blank!", input.getSivNo()));
        }
    }

    private SIVDetailDTO checkRecValidDetail(UserProfile userProfile, SIVDTO input, SIVDetailDTO detail) {
        if (StringUtils.isBlank(input.getProjectNo())) {
            throw new ServerException("Project No. Cannot be blank!");
        } else if (StringUtils.isBlank(detail.getItemNo())) {
            throw new ServerException("Item No Can Not be Blank!");
        } else {
            BombypjProjection bombypjCur = bombypjRepository.bombypjCur(userProfile.getCompanyCode(),
                    userProfile.getPlantNo(), input.getProjectNo(), detail.getItemNo());
            if (bombypjCur == null) {
                throw new ServerException("Item No : " + detail.getItemNo() + " not found in " + input.getProjectNo() + " !");
            }

            if (!bombypjCur.getSource().equals("B") && !bombypjCur.getSource().equals("C")) {
                throw new ServerException("Invalid Item Source, Item is not Buy or Consigned !");
            }

            if (detail.getItemType() == null) {
                detail.setItemType(0);
                detail.setPartNo(bombypjCur.getPartNo());
                detail.setLoc(bombypjCur.getLoc());
                detail.setUom(bombypjCur.getUom());
                detail.setRemarks(bombypjCur.getPartNo());
                detail.setIssuedQty(bombypjCur.getPickedQty());
                detail.setExtraQty(BigDecimal.ZERO);
                detail.setIssuedPrice(bombypjCur.getStdMaterial());
            }
        }

        if (detail.getItemType() == 0) {
            List<ItemLocProjection> itemsNo = itemLocRepository.itemLocCur(userProfile.getCompanyCode(),
                    userProfile.getPlantNo(), detail.getItemNo(), detail.getLoc());
            for (ItemLocProjection iLocPrj : itemsNo) {
                if (iLocPrj == null) {
                    throw new NotFoundException("Item No : " + detail.getItemNo() + " is not found in this loc !");
                }
            }
        }

        if (detail.getIssuedQty().compareTo(BigDecimal.ZERO) > 0) {
            if (detail.getBomPickQty() == null) {
                throw new NotFoundException("Item No : " + detail.getItemNo() + " not found !");
            } else if (detail.getBomPickQty().compareTo(BigDecimal.ZERO) == 0) {
                throw new ServerException("Item No : " + detail.getItemNo() + " has no pick qty !");
            }

            if (detail.getIssuedQty().compareTo(detail.getBatchQty()) > 0) {
                qtyReset(detail);
                throw new ServerException("Item No : " + detail.getItemNo() + " Issued > Batch Qty !");
            }

            ItemLocProjection qohCur = itemLocRepository.getQohCur(userProfile.getCompanyCode(), userProfile.getPlantNo(),
                    detail.getItemNo(), detail.getLoc());
            if (qohCur.getQoh().compareTo(BigDecimal.ZERO) < detail.getIssuedQty().compareTo(BigDecimal.ZERO)) {
                qtyReset(detail);
                throw new ServerException("Item No : " + detail.getItemNo() + " Issued > Qty-On-Hand !");
            }

            if ((detail.getIssuedQty().subtract(detail.getBomPickQty())).compareTo(BigDecimal.ZERO) > 0
                    && qohCur.getEoh().compareTo(detail.getIssuedQty()
                    .subtract(detail.getBomPickQty())) < 0) {
                qtyReset(detail);
                throw new ServerException("Issued Qty of " + detail.getItemNo() + " Cannot be > EOH, EOH now is : " + qohCur.getEoh() + " !");
            }

            ItemLocProjection resv = itemLocRepository.getResv(userProfile.getCompanyCode(), userProfile.getPlantNo(), detail.getItemNo());
            if (resv.getProdnResv().subtract(resv.getPoResvQty()).compareTo(resv.getResvQty()) != 0) {
                throw new ServerException("" + detail.getItemNo() + " Item Resv does not match Bom total Resv ! " +
                        "Prodn Resv : " + resv.getProdnResv() + ", Resv Qty : " + resv.getResvQty() + " Inform MIS");
            }
        } else {
            qtyReset(detail);
            throw new ServerException("Issued Qty Must be > 0 !");
        }

        return detail;
    }

    private void checkRecValid(SIVDTO input) {
        if (StringUtils.isNotBlank(input.getProjectNo())) {
            ItemProjection itemProjection = itemRepository.getItemNoByProjectNo(input.getProjectNo());
            if (itemProjection == null) {
                throw new ServerException("Invalid Project No!");
            }
        } else {
            throw new ServerException("Project No. Can Not be Blank!");
        }
    }

    private void checkRecValidProjectNo(String projectNo) {
        if (StringUtils.isNotBlank(projectNo)) {
            ItemProjection itemProjection = itemRepository.getItemNoByProjectNo(projectNo);
            if (itemProjection == null) {
                throw new ServerException("Project Type of " + projectNo + " is unknown!");
            }
        } else {
            throw new ServerException("Project No. Can Not be Blank!");
        }
    }

    private void checkIfSivNoExist(UserProfile userProfile, SIVDTO input) {
        Optional<SIV> sivOptional = sivRepository.findSIVByCompanyCodeAndPlantNoAndSivNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getSivNo());
        String type = "SIV";
        String subType;
        if (input.getSubType().equals("M")) {
            subType = "M";
        } else {
            subType = input.getProjectNo().substring(0, 1);
        }

        DocmNoProjection docmNo = docmNoRepository.getLastGeneratedNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), type, subType);
        if (docmNo.getGeneratedNo() == null) {
            throw new NotFoundException("Not found in DOCM_NO table for type SIV !");
        }

        if (sivOptional.isPresent()) {
            throw new DuplicateException("SIV No. exists ! SIV NO change from " + sivOptional.get().getSivNo() +
                    " to " + docmNo.getGeneratedNo() + " Please add the record again to confirm the change.");
        }
    }

    private void sivPostSaving(UserProfile userProfile, SIV input) {

        BomprojProjection pickedStatus = bomprojRepository.getPickedStatus(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getProjectNo());
        if (!pickedStatus.getPickedStatus().equals("P") || StringUtils.isBlank(pickedStatus.getPickedStatus())) {
            bomprojRepository.updatePickedStatusSivNo(input.getSivNo(), input.getProjectNo());
        } else {
            bomprojRepository.updateSivNo(input.getSivNo(), input.getProjectNo());
        }

        String type = "SIV";
        String subType;
        if (input.getSubType().equals("M")) {
            subType = "M";
        } else {
            subType = input.getProjectNo().substring(0, 1);
        }
        DocmNoProjection docmNo = docmNoRepository.getLastGeneratedNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), type, subType);
        docmNoRepository.updateLastGeneratedNo(docmNo.getDocmNo(), userProfile.getCompanyCode(), userProfile.getPlantNo(), subType, type);
    }

    private void populateAfterSaving(SIVDTO input, SIV saved) {
        input.setId(saved.getId());
        input.setVersion(saved.getVersion());
    }

    @Override
    public SIVDTO findBy(Long id) {
        return convertToSIVDTO(checkIfRecordExist(id));
    }

    private SIVDTO convertToSIVDTO(SIV siv) {
        Set<SIVDetailDTO> sivDetails = new HashSet<>();
        if (!CollectionUtils.isEmpty(siv.getSivDetails())) {
            sivDetails = siv.getSivDetails().stream().map(detail -> {
                SIVDetailDTO sivDetail = SIVDetailDTO.builder()
                        .sivNo(detail.getSivNo())
                        .seqNo(detail.getSeqNo())
                        .build();

                BeanUtils.copyProperties(detail, sivDetail);

                return sivDetail;
            }).collect(Collectors.toSet());
        }

        SIVDTO sivDTO = SIVDTO.builder().build();

        BeanUtils.copyProperties(siv.getId(), sivDTO);
        BeanUtils.copyProperties(siv, sivDTO);
        sivDTO.setSivDetails(sivDetails);
        return sivDTO;
    }

    private SIV checkIfRecordExist(Long id) {
        Optional<SIV> optionalSIV = sivRepository.findById(id);

        if (!optionalSIV.isPresent()) {
            throw new NotFoundException("Record is not found");
        }
        return optionalSIV.get();
    }

    @Override
    public SearchResult<SIVDTO> searchBy(SearchRequest searchRequest) {
        Specification<SIV> specs = where(queryGenerator.createDefaultSpec());

        if (!CollectionUtils.isEmpty(searchRequest.getFilters())) {
            for (Filter filter : searchRequest.getFilters()) {
                specs = specs.and(queryGenerator.createSpecification(filter));
            }
        }

        Page<SIV> pgSIV = sivRepository.findAll(specs, queryGenerator.constructPageable(searchRequest));

        SearchResult<SIVDTO> result = new SearchResult<>();
        result.setTotalRows(pgSIV.getTotalElements());
        result.setTotalPages(pgSIV.getTotalPages());
        result.setCurrentPageNumber(pgSIV.getPageable().getPageNumber());
        result.setCurrentPageSize(pgSIV.getNumberOfElements());
        result.setRows(pgSIV.getContent().stream().map(siv -> convertToSIVDTO(siv)).collect(Collectors.toList()));

        return result;
    }

    @Override
    public DocmValueDTO getGeneratedNoSIV(SIVDTO input) {
        String subType;
        if (input.getSubType().equals("M")) {
            subType = "M";
        } else {
            subType = input.getProjectNo().substring(0, 1);
        }

        DocmNoProjection docmNo = docmNoRepository.getLastGeneratedNo(
                UserProfileContext.getUserProfile().getCompanyCode(),
                UserProfileContext.getUserProfile().getPlantNo(),
                "SIV",
                subType);

        return DocmValueDTO.builder()
                .generatedNo(docmNo.getGeneratedNo())
                .docmNo(docmNo.getDocmNo())
                .build();
    }

    @Override
    public List<SIVDTO> getProjectNoByStatus() {
        List<BombypjProjection> prjNoProjection = bombypjRepository.getPrjNoByStatus(
                UserProfileContext.getUserProfile().getCompanyCode(),
                UserProfileContext.getUserProfile().getPlantNo());
        List<SIVDTO> list = new ArrayList<>();
        for (BombypjProjection bProj : prjNoProjection) {
            list.add(SIVDTO.builder().projectNo(bProj.getProjectNo()).build());
        }

        return list;
    }

    @Override
    public List<SIVDetailDTO> populateSivDetail(String projectNo) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        List<SIVDetailDTO> sivDetailDTOList;

        if (StringUtils.isNotBlank(projectNo)) {
            checkRecValidProjectNo(projectNo);
            sivDetailDTOList = populateDetails(userProfile, projectNo);
        } else {
            throw new ServerException("Project No. Can Not be Blank!");
        }

        return sivDetailDTOList;
    }

    @Override
    public SIVDetailDTO checkValidIssuedQty(String projectNo, String itemNo, BigDecimal issuedQty, int seqNo) {
        UserProfile userProfile = UserProfileContext.getUserProfile();
        SIVDetailDTO dto = checkRecValidIssuedQty(userProfile, projectNo, itemNo, issuedQty, seqNo);
        return dto;
    }

    @Override
    public List<SIVDetailDTO> getSIVManualDetails(SIVDTO input) {
        UserProfile userProfile = UserProfileContext.getUserProfile();
        String saleType = "";
        String prRemarks = "";
        List<SIVDetailDTO> sivDetailDTOList = new ArrayList<>();
        List<PRDetailProjection> bombypjCur = prDetailRepository.bombypjCur(saleType, input.getDocmNo(),
                prRemarks, userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getTranType());
        for (SIVDetailDTO dto : input.getSivDetails()) {
            ItemLocProjection itemAvailQohL = itemLocRepository.getItemAvailQohL(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getItemNo());
            ItemLocProjection itemAvailQohF = itemLocRepository.getItemAvailQohF(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getItemNo());
        }
        PRProjection prRmk = prRepository.prRmk(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getDocmNo());


        return sivDetailDTOList;
    }

    @Override
    public SIVDTO getDefaultValueSIV(String subType) {
        UserProfile userProfile = UserProfileContext.getUserProfile();
        String entryTime = FastDateFormat.getInstance("kkmmss").format(System.currentTimeMillis());
        ZonedDateTime now = ZonedDateTime.now();
        // subType (N : for Entry, M : for Manual)
        return SIVDTO.builder().currencyCode("USD").currencyRate(BigDecimal.ONE)
                .entryUser(userProfile.getUsername()).subType(subType).statuz("O")
                .entryDate(now).entryTime(entryTime).build();
    }


    @Override
    public List<SIVDetailDTO> populateBatchList(String projectNo) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        List<SIVDetailDTO> populateDetails = populateDetails(userProfile, projectNo);
        List<SIVDetailDTO> listPopulateBatch = new ArrayList<>();

        int countSeqNo = 1;
        int countGrnDetSeqNo = 1;

        for (SIVDetailDTO dto : populateDetails) {
            if (StringUtils.isNotBlank(dto.getItemNo())) {
                List<ItemBatchProjection> itemBatchFLoc = itemBatcRepository.getItemBatchFLocByItemNo(userProfile.getCompanyCode(),
                        userProfile.getPlantNo(), dto.getItemNo());
                List<ItemBatchProjection> itemBatch = itemBatcRepository.getItemBatchByItemNo(userProfile.getCompanyCode(),
                        userProfile.getPlantNo(), dto.getItemNo());

                if (dto.getBomShortQtyF().compareTo(BigDecimal.ZERO) > 0) {
                    for (ItemBatchProjection ib : itemBatchFLoc) {
                        SIVDetailDTO iBatchFloc = SIVDetailDTO.builder().build();
                        iBatchFloc.setSeqNo(countSeqNo++);
                        iBatchFloc.setGrnDetSeqNo(countGrnDetSeqNo++);
                        iBatchFloc.setItemNo(dto.getItemNo());
                        iBatchFloc.setBatchDesc(ib.getBatchDesc());
                        iBatchFloc.setBatchNoLoc(ib.getBatchNoLoc());
                        listPopulateBatch.add(iBatchFloc);
                    }
                } else {
                    for (ItemBatchProjection ib : itemBatch) {
                        SIVDetailDTO iBatch = SIVDetailDTO.builder().build();
                        iBatch.setSeqNo(countSeqNo++);
                        iBatch.setGrnDetSeqNo(countGrnDetSeqNo++);
                        iBatch.setItemNo(dto.getItemNo());
                        iBatch.setBatchDesc(ib.getBatchDesc());
                        iBatch.setBatchNoLoc(ib.getBatchNoLoc());
                        listPopulateBatch.add(iBatch);
                    }
                }
            }
        }

        return listPopulateBatch;
    }

    private SIVDetailDTO checkRecValidIssuedQty(UserProfile userProfile, String projectNo, String itemNo, BigDecimal issuedQty, int seqNo) {

        List<SIVDetailDTO> populateDetails = populateDetails(userProfile, projectNo);

        SIVDetailDTO dto = SIVDetailDTO.builder().build();

        for (SIVDetailDTO rec : populateDetails) {
            if (itemNo.equals(rec.getItemNo()) && seqNo == rec.getSeqNo()) {
                if (issuedQty.compareTo(BigDecimal.ZERO) != 0) {
                    if (issuedQty.compareTo(rec.getBomPickQty()) > 0) {
                        dto.setExtraQty(rec.getIssuedQty().subtract(rec.getBomPickQty()));
                    }
                    if (rec.getBatchLoc() != null) {
                        if (issuedQty.compareTo(BigDecimal.ZERO) > 0) {
                            if (rec.getBomPickQty() == null) {
                                throw new NotFoundException("Item No " + rec.getItemNo() + " not found !");
                            } else if (rec.getBomPickQty().compareTo(BigDecimal.ZERO) == 0) {
                                throw new ServerException("Item No " + rec.getItemNo() + " has no pick qty !");
                            }

                            if (issuedQty.compareTo(rec.getBatchQty()) > 0) {
                                qtyReset(rec);
                                throw new ServerException("Item No : " + rec.getItemNo() + " Issued > Batch Qty !");
                            }

                            ItemLocProjection qohCur = itemLocRepository.getQohCur(userProfile.getCompanyCode(), userProfile.getPlantNo(), rec.getItemNo(), rec.getLoc());
                            if (qohCur.getQoh().compareTo(issuedQty) < 0) {
                                qtyReset(rec);
                                throw new ServerException("Item No " + rec.getItemNo() + " Issued > Qty-On Hand !");
                            }

                            if (issuedQty.subtract(rec.getBomPickQty()).compareTo(BigDecimal.ZERO) > 0
                                    && qohCur.getEoh().compareTo(issuedQty.subtract(rec.getBomPickQty())) < 0) {
                                qtyReset(rec);
                                throw new ServerException("Issued Qty of " + rec.getItemNo() + " Cannot be > EOH, EOH now is : " + qohCur.getEoh() + " !");
                            }

                            ItemLocProjection resv = itemLocRepository.getResv(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo);
                            if (resv.getProdnResv().subtract(resv.getPoResvQty()).compareTo(resv.getResvQty()) != 0) {
                                throw new ServerException("" + itemNo + " Item Resv does not match Bom total Resv ! " +
                                        "Prodn Resv : " + resv.getProdnResv() + ", Resv Qty : " + resv.getResvQty() + " Inform MIS");
                            }
                        } else {
                            qtyReset(rec);
                            throw new ServerException("Issued Qty Must be > 0");
                        }
                    }
                }
            }
            dto.setItemNo(itemNo);
            dto.setSeqNo(seqNo);
            dto.setIssuedQty(issuedQty);
            dto.setGrnDetSeqNo(seqNo);
        }
        return dto;
    }

    private void qtyReset(SIVDetailDTO dto) {
        dto.setIssuedQty(dto.getBomPickQty());
        dto.setExtraQty(BigDecimal.ZERO);
    }

    private List<SIVDetailDTO> populateDetails(UserProfile userProfile, String projectNo) {

        List<SIVDetailDTO> list = new ArrayList<>();

        SaleDetailProjection sdetProjection = saleDetailRepository.getProjectType(userProfile.getCompanyCode(), userProfile.getPlantNo(), projectNo);
        String sivType = sdetProjection.getProductType();
        if (StringUtils.isBlank(sivType)) {
            throw new ServerException("Project Type of " + projectNo + " is unknown!");
        }

        List<BombypjProjection> bombProjections = bombypjRepository.getDataByProjectNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), projectNo);

        int countSeqNo = 1;
        int countGrnDetSeqNo = 1;

        // bombypj loop
        for (BombypjProjection bombRec : bombProjections) {
            List<ItemBatchProjection> batchProjection = itemBatcRepository.getBatchNoByItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), bombRec.getAlternate());
            BigDecimal projPickQty = bombRec.getPickedQty();
            BigDecimal projShortQtyL = BigDecimal.ZERO;
            BigDecimal projShortQtyF = BigDecimal.ZERO;
            if (bombRec.getShortQty().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal projShortQty = bombRec.getShortQty();
                BigDecimal itemAvailQty = BigDecimal.ZERO;
                ItemLocProjection itemAvailQohL = itemLocRepository.getItemAvailQohL(userProfile.getCompanyCode(), userProfile.getPlantNo(), bombRec.getAlternate());
                if (itemAvailQohL != null) {
                    itemAvailQty = itemAvailQohL.getAvailQty();
                    if (itemAvailQty.compareTo(BigDecimal.ZERO) > 0) {
                        if (itemAvailQty.compareTo(bombRec.getShortQty()) > 0) {
                            projShortQtyL = bombRec.getShortQty();
                            projShortQty = BigDecimal.ZERO;
                        } else {
                            projShortQtyL = itemAvailQty;
                            projShortQty = projShortQty.subtract(itemAvailQty);
                        }
                    }

                    if (projShortQty.compareTo(BigDecimal.ZERO) > 0) {
                        itemAvailQty = BigDecimal.ZERO;
                        ItemLocProjection itemAvailQohF = itemLocRepository.getItemAvailQohF(userProfile.getCompanyCode(), userProfile.getPlantNo(), bombRec.getAlternate());
                        if (itemAvailQohF != null) {
                            itemAvailQty = itemAvailQohF.getAvailQty();
                            if (itemAvailQty.compareTo(BigDecimal.ZERO) > 0) {
                                if (itemAvailQty.compareTo(bombRec.getShortQty()) > 0) {
                                    projShortQtyF = bombRec.getShortQty();
                                    projShortQty = BigDecimal.ZERO;
                                } else {
                                    projShortQtyF = itemAvailQty;
                                    projShortQty = projShortQty.subtract(itemAvailQty);
                                }
                            }
                        }
                    }
                }
            }

            // batch loop
            for (ItemBatchProjection batRec : batchProjection) {
                BigDecimal batchQty = batRec.getQoh();
                BigDecimal pickQty = bombRec.getPickedQty();
                BigDecimal shortQty = bombRec.getShortQty();
                BigDecimal issPrice = bombRec.getStdMaterial();
                String itemNo = bombRec.getAlternate();
                String partNo = bombRec.getPartNo();
                String loc = bombRec.getLoc();
                String uom = bombRec.getUom();
                Long batchNo = batRec.getBatchNo();
                String batchLoc = batRec.getBatchNo() + "/" + bombRec.getLoc();

                if (projPickQty.compareTo(BigDecimal.ZERO) > 0) {
                    if (projPickQty.compareTo(batchQty) > 0) {
                        pickQty = batchQty;
                        projPickQty = projPickQty.subtract(batchQty);
                        batchQty = BigDecimal.ZERO;
                    } else {
                        pickQty = projPickQty;
                        batchQty = batchQty.subtract(projPickQty);
                        projPickQty = BigDecimal.ZERO;
                    }

                    list.add(SIVDetailDTO.builder()
                            .seqNo(countSeqNo++)
                            .grnDetSeqNo(countGrnDetSeqNo++)
                            .itemType(0)
                            .itemNo(itemNo)
                            .partNo(partNo)
                            .loc(loc)
                            .uom(uom)
                            .batchNo(batchNo)
                            .batchLoc(batchLoc)
                            .issuedQty(pickQty)
                            .extraQty(BigDecimal.ZERO)
                            .issuedPrice(issPrice)
                            .bomPickQty(pickQty)
                            .bomShortQtyL(BigDecimal.ZERO)
                            .bomShortQtyF(BigDecimal.ZERO)
                            .batchQty(batchQty)
                            .remarks(partNo)
                            .build());
                }

                if (projShortQtyL.compareTo(BigDecimal.ZERO) > 0 && batchQty.compareTo(BigDecimal.ZERO) > 0) {
                    if (projShortQtyL.compareTo(batchQty) > 0) {
                        pickQty = batchQty;
                        shortQty = batchQty;
                        projShortQtyL = projShortQtyL.subtract(batchQty);
                        batchQty = BigDecimal.ZERO;
                    } else {
                        pickQty = projShortQtyL;
                        shortQty = projShortQtyL;
                        batchQty = batchQty.subtract(projShortQtyL);
                        projShortQtyL = BigDecimal.ZERO;
                    }

                    list.add(SIVDetailDTO.builder()
                            .seqNo(countSeqNo++)
                            .grnDetSeqNo(countGrnDetSeqNo++)
                            .itemType(0)
                            .itemNo(itemNo)
                            .partNo(partNo)
                            .loc(loc)
                            .uom(uom)
                            .batchNo(null)
                            .batchLoc(null)
                            .issuedQty(pickQty)
                            .extraQty(BigDecimal.ZERO)
                            .issuedPrice(issPrice)
                            .bomPickQty(pickQty)
                            .bomShortQtyL(shortQty)
                            .bomShortQtyF(BigDecimal.ZERO)
                            .batchQty(batchQty)
                            .remarks(partNo)
                            .build());
                }

                CompanyProjection companyStockLoc = companyRepository.getStockLoc(userProfile.getCompanyCode(), userProfile.getPlantNo()); // get stock loc company
                if (projShortQtyF.compareTo(BigDecimal.ZERO) > 0 && batchQty.compareTo(BigDecimal.ZERO) > 0 && !batRec.getLoc().equals(companyStockLoc.getStockLoc())) {
                    if (projShortQtyF.compareTo(batchQty) > 0) {
                        pickQty = batchQty;
                        shortQty = batchQty;
                        projShortQtyF = projShortQtyF.subtract(batchQty);
                        batchQty = BigDecimal.ZERO;
                    } else {
                        pickQty = projShortQtyF;
                        shortQty = projShortQtyF;
                        batchQty = batchQty.subtract(projShortQtyF);
                        projShortQtyF = BigDecimal.ZERO;
                    }

                    list.add(SIVDetailDTO.builder()
                            .seqNo(countSeqNo++)
                            .grnDetSeqNo(countGrnDetSeqNo++)
                            .itemType(0)
                            .itemNo(itemNo)
                            .partNo(partNo)
                            .loc(loc)
                            .uom(uom)
                            .batchNo(null)
                            .batchLoc(null)
                            .issuedQty(pickQty)
                            .extraQty(BigDecimal.ZERO)
                            .issuedPrice(issPrice)
                            .bomPickQty(pickQty)
                            .bomShortQtyL(BigDecimal.ZERO)
                            .bomShortQtyF(shortQty)
                            .batchQty(batRec.getQoh())
                            .remarks(partNo)
                            .build());
                }

            }

            if (projPickQty.compareTo(BigDecimal.ZERO) != 0) {
                throw new ServerException("Error in Batch QOH distribution!");
            }
        }

        return list;
    }
}