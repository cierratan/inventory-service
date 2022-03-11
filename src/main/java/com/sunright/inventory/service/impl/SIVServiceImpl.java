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
import com.sunright.inventory.entity.bombypj.BombypjProjection;
import com.sunright.inventory.entity.bomproj.BomprojProjection;
import com.sunright.inventory.entity.company.CompanyProjection;
import com.sunright.inventory.entity.coq.*;
import com.sunright.inventory.entity.docmno.DocmNoProjection;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.grn.GrnDetailProjection;
import com.sunright.inventory.entity.inaudit.InAudit;
import com.sunright.inventory.entity.item.ItemProjection;
import com.sunright.inventory.entity.itembatc.ItemBatchProjection;
import com.sunright.inventory.entity.itembatclog.ItemBatcLog;
import com.sunright.inventory.entity.itembatclog.ItemBatcLogId;
import com.sunright.inventory.entity.itembatclog.ItemBatcLogProjection;
import com.sunright.inventory.entity.itemloc.ItemLocProjection;
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
import com.sunright.inventory.entity.siv.SIVDetailSub;
import com.sunright.inventory.entity.wip.WipDirsProjection;
import com.sunright.inventory.entity.wip.WipProjProjection;
import com.sunright.inventory.exception.DuplicateException;
import com.sunright.inventory.exception.NotFoundException;
import com.sunright.inventory.exception.ServerException;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.*;
import com.sunright.inventory.service.SIVService;
import com.sunright.inventory.util.QueryGenerator;
import net.sf.jasperreports.engine.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;

@Transactional
@Service
public class SIVServiceImpl implements SIVService {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SIVRepository sivRepository;

    @Autowired
    private SIVDetailRepository sivDetailRepository;

    @Autowired
    private SIVDetailSubRepository sivDetailSubRepository;

    @Autowired
    private DocmNoRepository docmNoRepository;

    @Autowired
    private BombypjRepository bombypjRepository;

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
    private COQDetailSubRepository coqDetailSubRepository;

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
        String entryTime = FastDateFormat.getInstance("kkmmss").format(System.currentTimeMillis());
        input.setEntryTime(entryTime);
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
                SIVDetail savedDetail = sivDetailRepository.save(sivDetail);
                if (input.getSubType().equals("M")) {
                    procPRClosure(userProfile, input);
                } else {
                    procBatchConsolidate(sivDetail, detail);
                    procUpdateSfcWip(userProfile, sivDetail, detail, input);
                }
                sivDetailPostSaving(userProfile, saved, sivDetail, detail, input, savedDetail);
            }
        }

        sivPostSaving(userProfile, saved);
        populateAfterSaving(input, saved);

        return input;
    }

    private void procPRClosure(UserProfile userProfile, SIVDTO input) {
        PRProjection cReqItem = prRepository.cReqItem(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getDocmNo());
        PRDetailProjection cComplete = prDetailRepository.cComplete(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getDocmNo());
        if (input.getTranType().equals("PR")) {
            if (cReqItem != null) {
                if (cReqItem.getCountReqItem() == 0) {
                    return;
                }
            }
            if (cComplete.getIssReq() == 1) {
                Date closedDate = new Date(System.currentTimeMillis());
                prRepository.updateStatusClosedDate(closedDate, userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getDocmNo());
            }
        }
    }

    private void procUpdateSfcWip(UserProfile userProfile, SIVDetail sivDetail, SIVDetailDTO detail, SIVDTO input) {
        String type = input.getProjectNo().substring(0, 1);
        String subType = input.getProjectNo().substring(9, 11);
        ProductProjection wipTrackCur = productRepository.wipTrackCur(userProfile.getCompanyCode(), userProfile.getPlantNo(), type, subType);
        ItemProjection itemCat = itemRepository.itemCatCodePartNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), detail.getItemNo());
        SfcWipProjection wipCur = sfcWipRepository.wipCur(input.getProjectNo(), itemCat.getPartNo());
        if (wipTrackCur != null) {
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

    private void sivDetailPostSaving(UserProfile userProfile, SIV siv, SIVDetail sivDetail, SIVDetailDTO detail, SIVDTO input, SIVDetail savedDetail) {

        if (detail.getSubType().equals("M")) {
            if (detail.getItemType() == 0) {
                reduceItemInv(userProfile, input, detail);
                postBombypj(userProfile, siv, sivDetail, detail);
                if (input.getTranType().equals("PR")) {
                    if (StringUtils.isNotBlank(detail.getProjectNo1())) {
                        procBombProjUpdate(userProfile.getCompanyCode(), userProfile.getPlantNo(), detail.getItemNo(),
                                detail.getProjectNo1(), detail.getIssuedQty1());
                    }
                    if (StringUtils.isNotBlank(detail.getProjectNo2())) {
                        procBombProjUpdate(userProfile.getCompanyCode(), userProfile.getPlantNo(), detail.getItemNo(),
                                detail.getProjectNo1(), detail.getIssuedQty2());
                    }
                    if (StringUtils.isNotBlank(detail.getProjectNo3())) {
                        procBombProjUpdate(userProfile.getCompanyCode(), userProfile.getPlantNo(), detail.getItemNo(),
                                detail.getProjectNo1(), detail.getIssuedQty3());
                    }
                    if (StringUtils.isNotBlank(detail.getProjectNo4())) {
                        procBombProjUpdate(userProfile.getCompanyCode(), userProfile.getPlantNo(), detail.getItemNo(),
                                detail.getProjectNo1(), detail.getIssuedQty4());
                    }
                    if (StringUtils.isNotBlank(detail.getProjectNo5())) {
                        procBombProjUpdate(userProfile.getCompanyCode(), userProfile.getPlantNo(), detail.getItemNo(),
                                detail.getProjectNo1(), detail.getIssuedQty5());
                    }
                }
                postCoq(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getProjectNo(), input.getDocmNo(), detail.getItemNo(),
                        detail.getBatchNo(), input.getSivNo(), detail.getIssuedQty(), detail.getIssuedPrice(), input.getSubType());
            }

            Integer vSeqNo = 1;
            if (detail.getSaleType().equals("D") && StringUtils.isNotBlank(detail.getItemNo())) {

                vSeqNo = savedDetail.getSeqNo();
                BigDecimal vIssuedQty1 = (detail.getIssuedQty1() == null ? BigDecimal.ZERO : detail.getIssuedQty1());
                String docmNO = input.getDocmNo();

                SIVDetailSub sivDetailSub = new SIVDetailSub();
                sivDetailSub.setCompanyCode(userProfile.getCompanyCode());
                sivDetailSub.setPlantNo(userProfile.getPlantNo());
                sivDetailSub.setSubType(savedDetail.getSubType());
                sivDetailSub.setSivNo(savedDetail.getSivNo());
                sivDetailSub.setSeqNo(vSeqNo);
                sivDetailSub.setDetailSeq(savedDetail.getSeqNo());
                sivDetailSub.setItemNo(savedDetail.getItemNo());
                sivDetailSub.setSaleType(savedDetail.getSaleType());
                sivDetailSub.setDocmNo(docmNO);
                sivDetailSub.setIssuedQty(vIssuedQty1);
                sivDetailSub.setSivDetail(savedDetail);
                sivDetailSubRepository.save(sivDetailSub);
            } else if (detail.getSaleType().equals("P")) {
                if (StringUtils.isNotBlank(detail.getProjectNo1()) && StringUtils.isNotBlank(detail.getItemNo())) {
                    vSeqNo = savedDetail.getSeqNo();
                    BigDecimal vIssuedQty1 = (detail.getIssuedQty1() == null ? BigDecimal.ZERO : detail.getIssuedQty1());
                    String docmNO = detail.getProjectNo1();

                    SIVDetailSub sivDetailSub = new SIVDetailSub();
                    sivDetailSub.setCompanyCode(userProfile.getCompanyCode());
                    sivDetailSub.setPlantNo(userProfile.getPlantNo());
                    sivDetailSub.setSubType(savedDetail.getSubType());
                    sivDetailSub.setSivNo(savedDetail.getSivNo());
                    sivDetailSub.setSeqNo(vSeqNo);
                    sivDetailSub.setDetailSeq(savedDetail.getSeqNo());
                    sivDetailSub.setItemNo(savedDetail.getItemNo());
                    sivDetailSub.setSaleType(savedDetail.getSaleType());
                    sivDetailSub.setDocmNo(docmNO);
                    sivDetailSub.setIssuedQty(vIssuedQty1);
                    sivDetailSub.setSivDetail(savedDetail);
                    sivDetailSubRepository.save(sivDetailSub);
                }


                if (StringUtils.isNotBlank(detail.getProjectNo2()) && StringUtils.isNotBlank(detail.getItemNo())) {
                    vSeqNo = savedDetail.getSeqNo() + 1;
                    BigDecimal vIssuedQty2 = (detail.getIssuedQty1() == null ? BigDecimal.ZERO : detail.getIssuedQty2());
                    String docmNO = detail.getProjectNo2();

                    SIVDetailSub sivDetailSub = new SIVDetailSub();
                    sivDetailSub.setCompanyCode(userProfile.getCompanyCode());
                    sivDetailSub.setPlantNo(userProfile.getPlantNo());
                    sivDetailSub.setSubType(savedDetail.getSubType());
                    sivDetailSub.setSivNo(savedDetail.getSivNo());
                    sivDetailSub.setSeqNo(vSeqNo);
                    sivDetailSub.setDetailSeq(savedDetail.getSeqNo());
                    sivDetailSub.setItemNo(savedDetail.getItemNo());
                    sivDetailSub.setSaleType(savedDetail.getSaleType());
                    sivDetailSub.setDocmNo(docmNO);
                    sivDetailSub.setIssuedQty(vIssuedQty2);
                    sivDetailSub.setSivDetail(savedDetail);
                    sivDetailSubRepository.save(sivDetailSub);
                }

                if (StringUtils.isNotBlank(detail.getProjectNo3()) && StringUtils.isNotBlank(detail.getItemNo())) {
                    vSeqNo = savedDetail.getSeqNo() + 2;
                    BigDecimal vIssuedQty3 = (detail.getIssuedQty3() == null ? BigDecimal.ZERO : detail.getIssuedQty2());
                    String docmNO = detail.getProjectNo3();

                    SIVDetailSub sivDetailSub = new SIVDetailSub();
                    sivDetailSub.setCompanyCode(userProfile.getCompanyCode());
                    sivDetailSub.setPlantNo(userProfile.getPlantNo());
                    sivDetailSub.setSubType(savedDetail.getSubType());
                    sivDetailSub.setSivNo(savedDetail.getSivNo());
                    sivDetailSub.setSeqNo(vSeqNo);
                    sivDetailSub.setDetailSeq(savedDetail.getSeqNo());
                    sivDetailSub.setItemNo(savedDetail.getItemNo());
                    sivDetailSub.setSaleType(savedDetail.getSaleType());
                    sivDetailSub.setDocmNo(docmNO);
                    sivDetailSub.setIssuedQty(vIssuedQty3);
                    sivDetailSub.setSivDetail(savedDetail);
                    sivDetailSubRepository.save(sivDetailSub);
                }

                if (StringUtils.isNotBlank(detail.getProjectNo4()) && StringUtils.isNotBlank(detail.getItemNo())) {
                    vSeqNo = savedDetail.getSeqNo() + 3;
                    BigDecimal vIssuedQty4 = (detail.getIssuedQty3() == null ? BigDecimal.ZERO : detail.getIssuedQty4());
                    String docmNO = detail.getProjectNo4();

                    SIVDetailSub sivDetailSub = new SIVDetailSub();
                    sivDetailSub.setCompanyCode(userProfile.getCompanyCode());
                    sivDetailSub.setPlantNo(userProfile.getPlantNo());
                    sivDetailSub.setSubType(savedDetail.getSubType());
                    sivDetailSub.setSivNo(savedDetail.getSivNo());
                    sivDetailSub.setSeqNo(vSeqNo);
                    sivDetailSub.setDetailSeq(savedDetail.getSeqNo());
                    sivDetailSub.setItemNo(savedDetail.getItemNo());
                    sivDetailSub.setSaleType(savedDetail.getSaleType());
                    sivDetailSub.setDocmNo(docmNO);
                    sivDetailSub.setIssuedQty(vIssuedQty4);
                    sivDetailSub.setSivDetail(savedDetail);
                    sivDetailSubRepository.save(sivDetailSub);
                }

                if (StringUtils.isNotBlank(detail.getProjectNo5()) && StringUtils.isNotBlank(detail.getItemNo())) {
                    vSeqNo = savedDetail.getSeqNo() + 4;
                    BigDecimal vIssuedQty5 = (detail.getIssuedQty3() == null ? BigDecimal.ZERO : detail.getIssuedQty5());
                    String docmNO = detail.getProjectNo5();

                    SIVDetailSub sivDetailSub = new SIVDetailSub();
                    sivDetailSub.setCompanyCode(userProfile.getCompanyCode());
                    sivDetailSub.setPlantNo(userProfile.getPlantNo());
                    sivDetailSub.setSubType(savedDetail.getSubType());
                    sivDetailSub.setSivNo(savedDetail.getSivNo());
                    sivDetailSub.setSeqNo(vSeqNo);
                    sivDetailSub.setDetailSeq(savedDetail.getSeqNo());
                    sivDetailSub.setItemNo(savedDetail.getItemNo());
                    sivDetailSub.setSaleType(savedDetail.getSaleType());
                    sivDetailSub.setDocmNo(docmNO);
                    sivDetailSub.setIssuedQty(vIssuedQty5);
                    sivDetailSub.setSivDetail(savedDetail);
                    sivDetailSubRepository.save(sivDetailSub);
                }
            }
        } else {
            postBombypj(userProfile, siv, sivDetail, detail);
            postCoq(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getProjectNo(), input.getDocmNo(), detail.getItemNo(),
                    detail.getBatchNo(), input.getSivNo(), detail.getIssuedQty(), detail.getIssuedPrice(), input.getSubType());
        }
    }

    private void procBombProjUpdate(String companyCode, Integer plantNo, String itemNo, String projectNo, BigDecimal issuedQty) {

        BombypjProjection updateIssuedQty = bombypjRepository.bomProjCur(companyCode, plantNo, itemNo, projectNo);
        BigDecimal issuedQtyUpdt = issuedQty.add(updateIssuedQty.getIssuedQty());
        bombypjRepository.updateIssuedQty(issuedQtyUpdt, companyCode, plantNo, projectNo, projectNo, itemNo);
    }

    private void postBombypj(UserProfile userProfile, SIV siv, SIVDetail sivDetail, SIVDetailDTO detail) {

        BigDecimal shortQty = detail.getBomShortQtyL().add(detail.getBomShortQtyF());
        List<BombypjProjection> bombypjInfoByStatus = bombypjRepository.getBombypjInfoByStatus(userProfile.getCompanyCode(),
                userProfile.getPlantNo(), siv.getProjectNo(), detail.getItemNo(), shortQty);

        BigDecimal pickIss = detail.getIssuedQty();
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
                    userProfile.getCompanyCode(), userProfile.getPlantNo(), siv.getProjectNo(), detail.getItemNo());
        }

        if (pickIss.compareTo(BigDecimal.ZERO) > 0 && bombypjInfoByStatus.size() != 0) {
            BombypjProjection bombypjInfo = bombypjRepository.getBombypjInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), siv.getProjectNo(), detail.getItemNo());
            BigDecimal issuedQty = bombypjInfo.getIssuedQty().add(pickIss);
            bombypjRepository.updateIssuedQty(issuedQty, userProfile.getCompanyCode(), userProfile.getPlantNo(), bombypjInfo.getOrderNo(), siv.getProjectNo(), detail.getItemNo());
        }
    }

    private void postCoq(String companyCode, Integer plantNo, String projectNo, String docmNo, String itemNo,
                         Long batchNo, String sivNo, BigDecimal issuedQty, BigDecimal issuedPrice, String subType) {
        String WKType = "WO";
        String PRType = "PR";
        COQProjection coqRecN = null;
        COQProjection coqRecM = null;
        SaleProjection saleProj = null;
        List<SaleProjection> cCoqInfo = null;
        COQProjection coqDet = null;
        if (subType.equals("N")) {
            coqRecN = coqRepository.coqRecN(companyCode, plantNo, itemNo, projectNo, WKType);
            saleProj = saleRepository.saleCoqReasonsDet(companyCode, plantNo, projectNo);
            coqDet = coqDetailRepository.coqDet(companyCode, plantNo, projectNo);
        } else {
            coqRecM = coqRepository.coqRecM(companyCode, plantNo, itemNo, docmNo, WKType, PRType);
            cCoqInfo = saleRepository.cCoqInfo(companyCode, plantNo, docmNo, WKType, PRType);
            coqDet = coqDetailRepository.coqDet(companyCode, plantNo, docmNo);
        }

        String poNo = null;
        BigDecimal unitPrice = null;

        ItemBatcLogProjection iBatcLog = itemBatcLogRepository.getPoNoRecdPrice(companyCode,
                plantNo, sivNo, batchNo, itemNo);

        if (iBatcLog != null) {
            poNo = iBatcLog.getPoNo();
            unitPrice = iBatcLog.getRecdPrice();
        }

        ItemProjection itemInfo = itemRepository.itemInfo(companyCode, plantNo, itemNo);

        String docmNO = null;
        BigDecimal docmQty = null;
        Integer subSeq = null;
        if (subType.equals("N")) {
            if (coqRecN != null) {
                docmNO = coqRecN.getDocmNo();
                docmQty = coqRecN.getDocmQty();
                subSeq = coqRecN.getSeqNo();
            }
        } else {
            if (coqRecM != null) {
                docmNO = coqRecM.getDocmNo();
                docmQty = coqRecM.getDocmQty();
                subSeq = coqRecM.getSeqNo();
            }
        }

        if (StringUtils.isNotBlank(docmNO)) {
            if (coqRecN == null) {
                Integer maxDetRecSeq = coqDet.getRecSeq();
                Integer maxDetSeqNo = coqDet.getSeqNo();
                String partNo = itemInfo.getPartNo();
                String itemDesc = itemInfo.getDescription();
                String coqDiv = null;
                String coqDept = null;
                String reasonCode = null;
                String reasonDesc = null;
                if (subType.equals("N")) {
                    coqDiv = saleProj.getCoqDivCode();
                    coqDept = saleProj.getCoqDeptCode();
                    reasonCode = saleProj.getReasonCode();
                    reasonDesc = saleProj.getReasonDesc();
                } else {
                    for (SaleProjection rec : cCoqInfo) {
                        coqDiv = rec.getCoqDivCode();
                        coqDept = rec.getCoqDeptCode();
                        reasonCode = rec.getReasonCode();
                        reasonDesc = rec.getReasonDesc();
                    }

                }
                Integer recSeq = maxDetRecSeq + 1;

                COQDetail coqDetail = new COQDetail();
                COQDetailId idCoqDet = new COQDetailId();
                idCoqDet.setCompanyCode(companyCode);
                idCoqDet.setPlantNo(plantNo);
                idCoqDet.setDocmNo(docmNO);
                idCoqDet.setRecSeq(recSeq);
                coqDetail.setSeqNo(maxDetSeqNo + 1);
                coqDetail.setItemType(0);
                coqDetail.setPartNo(partNo);
                coqDetail.setItemNo(itemNo);
                coqDetail.setAssemblyNo("");
                coqDetail.setDescription(itemDesc);
                coqDetail.setDocmQty(issuedQty);
                coqDetail.setReasonCode(reasonCode);
                coqDetail.setReasonDesc(reasonDesc);
                coqDetail.setDivCode(coqDiv);
                coqDetail.setDeptCode(coqDept);
                coqDetail.setId(idCoqDet);
                coqDetailRepository.save(coqDetail);
            } else {
                if (subType.equals("N")) {
                    BigDecimal docmQtyUpdate = docmQty.add(issuedQty);
                    coqDetailRepository.updateDocmQty(docmQtyUpdate, companyCode, plantNo, subSeq, docmNO);
                } else {
                    BigDecimal docmQtyUpdate = docmQty.add(issuedQty);
                    coqDetailRepository.updateDocmQty(docmQtyUpdate, companyCode, plantNo, subSeq, docmNO);
                }
            }

            COQDetailSub coqDetailSub = new COQDetailSub();
            COQDetailSubId idDetSub = new COQDetailSubId();
            idDetSub.setCompanyCode(companyCode);
            idDetSub.setPlantNo(plantNo);
            idDetSub.setDocmNo(docmNO);
            if (subType.equals("N")) {
                idDetSub.setDetRecSeq(coqRecN.getRecSeq());
            } else {
                idDetSub.setDetRecSeq(coqRecM.getRecSeq());
            }
            idDetSub.setSeqNo(subSeq + 1);
            coqDetailSub.setSivNo(sivNo);
            coqDetailSub.setQty(issuedQty);
            coqDetailSub.setPoNo(poNo);
            if (unitPrice != null) {
                coqDetailSub.setUnitPrice(unitPrice);
            } else {
                coqDetailSub.setUnitPrice(issuedPrice);
            }
            Date entryDate = new Date(System.currentTimeMillis());
            coqDetailSub.setEntryDate(entryDate);
            coqDetailSub.setId(idDetSub);
            coqDetailSubRepository.save(coqDetailSub);
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
                if (input.getTranType().equals("PR") || input.getTranType().equals("WK")) {
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
                } else if (input.getTranType().equals("DS") || input.getTranType().equals("WD")) {
                    if (detail.getItemType() == 0) {
                        List<ItemProjection> itemCur = itemRepository.itemCur(input.getDocmNo(), detail.getCompanyCode(), detail.getPlantNo(), detail.getItemNo());
                        for (ItemProjection rec : itemCur) {
                            detail.setItemNo(rec.getItemNo());
                            detail.setLoc(rec.getLoc());
                            detail.setUom(rec.getUom());
                        }
                        if (StringUtils.isBlank(detail.getItemNo())) {
                            throw new ServerException("Item No : " + detail.getItemNo() + " is invalid or not found in SALEDET/ITEM Table !");
                        }
                    } else {
                        throw new ServerException("Please Enter Only Stock Item!");
                    }
                } else if (input.getTranType().equals("OTHER")) {
                    if (detail.getItemType() == 0) {
                        if (detail.getItemType() == null) {
                            throw new ServerException("Please enter 0-Stock, 1-Non Stock !");
                        } else {
                            if (!Integer.toString(detail.getItemType()).contains("0") && !Integer.toString(detail.getItemType()).contains("1")) {
                                throw new ServerException("Please enter 0-Stock, 1-Non Stock !");
                            }
                        }
                        SIVDetailDTO detailDTO = checkValidItemNo(input);
                        checkRecValidIssuedQty(userProfile, input, detailDTO);
                    }
                }
            }
        } else {
            List<GrnDetailProjection> grnDetCur = grnDetRepository.getGrndetCur(userProfile.getCompanyCode(),
                    userProfile.getPlantNo(), detail.getBatchNo(), detail.getItemNo());
            for (GrnDetailProjection prj : grnDetCur) {
                detail.setGrnNo(prj.getGrnNo());
                detail.setUom(prj.getUom());
                detail.setGrndetSeqNo(prj.getSeqNo());
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
        BigDecimal itemBatchBal = BigDecimal.ZERO;
        List<ItemBatchProjection> itemBatcPrj = itemBatcRepository.getItemBatchByBatchNo(userProfile.getCompanyCode(),
                userProfile.getPlantNo(), detail.getItemNo(), detail.getBatchNo(), detail.getLoc());
        ItemLocProjection itemLoc = itemLocRepository.itemLocByItemNo(userProfile.getCompanyCode(),
                userProfile.getPlantNo(), detail.getItemNo(), detail.getLoc());
        ItemProjection itemQoh = itemRepository.getQohByItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(),
                detail.getItemNo(), detail.getLoc());
        CompanyProjection coStkLoc = companyRepository.getStockLoc(userProfile.getCompanyCode(), userProfile.getPlantNo());
        ItemProjection itemInfo = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), detail.getItemNo());
        for (ItemBatchProjection rec : itemBatcPrj) {
            if (rec.getQoh().compareTo(detail.getIssuedQty()) < 0) {
                throw new ServerException("Item No : " + detail.getItemNo() + " Issued Qty > Batch Qty!");
            } else {
                itemBatchBal = rec.getQoh().subtract(detail.getIssuedQty());
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
                itemBatcLog.setDateCode(rec.getDateCode());
                itemBatcLog.setPoNo(rec.getPoNo());
                itemBatcLog.setPoRecSeq(rec.getPoRecSeq());
                itemBatcLog.setGrnNo(rec.getGrnNo());
                itemBatcLog.setGrnSeq(rec.getGrnSeq());
                itemBatcLog.setGrnQty(rec.getOriQoh());
                itemBatcLog.setId(id);
                itemBatcLog.setStatus(Status.ACTIVE);
                itemBatcLog.setCreatedBy(userProfile.getUsername());
                itemBatcLog.setCreatedAt(ZonedDateTime.now());
                itemBatcLog.setUpdatedBy(userProfile.getUsername());
                itemBatcLog.setUpdatedAt(ZonedDateTime.now());
                itemBatcLogRepository.save(itemBatcLog);
            }
            detail.setPoNo(rec.getPoNo());
            detail.setGrnNo(rec.getGrnNo());
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
        String tranTime = FastDateFormat.getInstance("kkmmss").format(System.currentTimeMillis());
        inAudit.setTranTime(tranTime);
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
        inAudit.setItemlocId(itemLoc.getId());
        inAuditRepository.save(inAudit);

        return detail;
    }

    private void checkRecNull(SIVDTO input) {
        if (input.getSivNo() == null) {
            throw new ServerException("SIV No Can Not be Blank!");
        }
    }

    private SIVDetailDTO checkRecValidDetail(UserProfile userProfile, SIVDTO input, SIVDetailDTO detail) {
        if (input.getSubType().equals("N")) {
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

                if (!bombypjCur.getSource().equals("B") || !bombypjCur.getSource().equals("C")) {
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
                ItemLocProjection itemsNo = itemLocRepository.itemLocCur(userProfile.getCompanyCode(),
                        userProfile.getPlantNo(), detail.getItemNo(), detail.getLoc());
                if (itemsNo == null) {
                    throw new NotFoundException("Item No : " + detail.getItemNo() + " is not found in this loc !");
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

            if (detail.getItemType() == null) {
                throw new ServerException("Please enter 0-Stock, 1-Non Stock !");
            } else {
                if (!Integer.toString(detail.getItemType()).contains("0") && !Integer.toString(detail.getItemType()).contains("1")) {
                    throw new ServerException("Please enter 0-Stock, 1-Non Stock !");
                }
            }

            if (detail.getIssuedQty().compareTo(BigDecimal.ZERO) > 0) {
                if (detail.getItemType() == 0) {
                    if (StringUtils.isNotBlank(detail.getProjectNo1())) {
                        if (detail.getIssuedQty1().compareTo(BigDecimal.ZERO) <= 0) {
                            throw new ServerException("Issued Qty Must be > 0");
                        }
                    }
                    if (StringUtils.isNotBlank(detail.getProjectNo2())) {
                        if (detail.getIssuedQty2().compareTo(BigDecimal.ZERO) <= 0) {
                            throw new ServerException("Issued Qty Must be > 0");
                        }
                    }
                    if (StringUtils.isNotBlank(detail.getProjectNo3())) {
                        if (detail.getIssuedQty3().compareTo(BigDecimal.ZERO) <= 0) {
                            throw new ServerException("Issued Qty Must be > 0");
                        }
                    }
                    if (StringUtils.isNotBlank(detail.getProjectNo4())) {
                        if (detail.getIssuedQty4().compareTo(BigDecimal.ZERO) <= 0) {
                            throw new ServerException("Issued Qty Must be > 0");
                        }
                    }
                    if (StringUtils.isNotBlank(detail.getProjectNo5())) {
                        if (detail.getIssuedQty5().compareTo(BigDecimal.ZERO) <= 0) {
                            throw new ServerException("Issued Qty Must be > 0");
                        }
                    }

                    BombypjProjection bombPickedQty;
                    if (StringUtils.isNotBlank(input.getProjectNo())) {
                        bombPickedQty = bombypjRepository.bombypjCurCaseWhen(userProfile.getCompanyCode(),
                                userProfile.getPlantNo(), input.getProjectNo(), detail.getItemNo());
                    } else {
                        bombPickedQty = bombypjRepository.bombypjCurCaseWhen(userProfile.getCompanyCode(),
                                userProfile.getPlantNo(), input.getDocmNo(), detail.getItemNo());
                    }

                    if (bombPickedQty == null) {
                        if (StringUtils.isNotBlank(input.getProjectNo())) {
                            throw new ServerException("Ref No " + input.getProjectNo() + " not found !");
                        } else {
                            throw new ServerException("Ref No " + input.getDocmNo() + " not found !");
                        }
                    } else if (bombPickedQty.getPickedQty().compareTo(BigDecimal.ZERO) == 0) {
                        throw new ServerException("Item No" + detail.getItemNo() + " has no outstanding qty !");
                    } else if (bombPickedQty.getPickedQty().compareTo(detail.getIssuedQty()) < 0) {
                        detail.setExtraQty(detail.getIssuedQty().subtract(bombPickedQty.getPickedQty()));
                    } else {
                        detail.setExtraQty(BigDecimal.ZERO);
                    }
                }
            }
        }
        return detail;
    }

    private void checkRecValid(SIVDTO input) {
        if (input.getSubType().equals("N")) {
            if (StringUtils.isNotBlank(input.getDocmNo()) || StringUtils.isNotBlank(input.getProjectNo())) {
                ItemProjection itemProjection;
                if (StringUtils.isNotBlank(input.getProjectNo())) {
                    itemProjection = itemRepository.getItemNoByProjectNo(input.getProjectNo());
                } else {
                    itemProjection = itemRepository.getItemNoByProjectNo(input.getDocmNo());
                }
                if (itemProjection == null) {
                    throw new ServerException("Invalid Project No!");
                }
            } else {
                throw new ServerException("Project No. Can Not be Blank!");
            }
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

        if (input.getSubType().equals("N")) {
            BomprojProjection pickedStatus = bomprojRepository.getPickedStatus(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getProjectNo());
            if (pickedStatus != null) {
                if (!pickedStatus.getPickedStatus().equals("P") || StringUtils.isBlank(pickedStatus.getPickedStatus())) {
                    bomprojRepository.updatePickedStatusSivNo(input.getSivNo(), input.getProjectNo());
                } else {
                    bomprojRepository.updateSivNo(input.getSivNo(), input.getProjectNo());
                }
            }
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

    private SIVDetailDTO checkValidIssuedQty(SIVDTO input) {
        UserProfile userProfile = UserProfileContext.getUserProfile();
        SIVDetailDTO detailDTO = SIVDetailDTO.builder().build();
        detailDTO = checkRecValidIssuedQty(userProfile, input, detailDTO);
        return detailDTO;
    }

    @Override
    public List<SIVDetailDTO> populateSIVManualDetails(SIVDTO input) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        List<SIVDetailDTO> sivDetailDTOList = new ArrayList<>();
        if (StringUtils.isNotBlank(input.getDocmNo())) {
            sivDetailDTOList = populateDetailsManual(userProfile, input);
        }
        return sivDetailDTOList;
    }

    private SIVDetailDTO checkValidItemNo(SIVDTO input) {
        SIVDetailDTO dto = SIVDetailDTO.builder().build();
        UserProfile userProfile = UserProfileContext.getUserProfile();
        if (input.getTranType().equals("OTHER")) {
            dto = checkValidOtherItemNo(userProfile, input);
        } else if (input.getTranType().equals("DS") || input.getTranType().equals("WD")) {
            dto = checkRecValidItemNoDSWD(userProfile, input);
        } else if (input.getTranType().equals("WK") || input.getTranType().equals("PR")) {
            dto = checkValidPRWKItemNo(userProfile, input);
        }
        if (dto.getItemType() == 0) {
            if (StringUtils.isBlank(dto.getLoc()) && StringUtils.isBlank(dto.getUom())) {
                ItemProjection itemCur = itemRepository.itemOtherCurOrItemPRCur(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getItemNo());
                dto.setLoc(itemCur.getLoc());
                dto.setUom(itemCur.getUom());
            }
            if (dto.getIssuedPrice() == null) {
                ItemLocProjection itemLocCur = itemLocRepository.itemLocCur(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getItemNo(), dto.getLoc());
                BigDecimal stdMaterial = (itemLocCur.getStdMaterial() == null ? BigDecimal.ZERO : itemLocCur.getStdMaterial());
                dto.setIssuedPrice(stdMaterial);
            }
        }
        return dto;
    }

    @Override
    public byte[] generatedLabelSIV(SIVDTO input) throws JRException, SQLException {

        Integer seqNo = 0;
        for (SIVDetailDTO dto : input.getSivDetails()) {
            seqNo = dto.getSeqNo();
        }
        UserProfile userProfile = UserProfileContext.getUserProfile();
        InputStream resource = this.getClass().getResourceAsStream("/reports/siv_label.jrxml");
        // Compile the Jasper report from .jrxml to .jasper
        JasperReport jasperReport = JasperCompileManager.compileReport(resource);
        Map<String, Object> param = new HashMap<>();
        param.put("SIV_NO", input.getSivNo());
        param.put("COMPANY_CODE", userProfile.getCompanyCode());
        param.put("PLANT_NO", userProfile.getPlantNo());
        param.put("SEQ_NO", seqNo);
        Connection source = dataSource.getConnection();
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, param, source);
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    @Override
    public byte[] generatedReportSIV(SIVDTO input) throws JRException, SQLException {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        // Fetching the .jrxml file from the resources folder.
        InputStream resourceSubReport = this.getClass().getResourceAsStream("/reports/siv_report_header_INR00009.jrxml");
        InputStream mainReport = this.getClass().getResourceAsStream("/reports/siv_report_detail_INR00009.jrxml");
        // Compile the Jasper report from .jrxml to .jasper
        JasperReport jasperSubReport = JasperCompileManager.compileReport(resourceSubReport);
        JasperReport jasperMainReport = JasperCompileManager.compileReport(mainReport);
        Map<String, Object> param = new HashMap<>();
        // Adding the additional parameters to the pdf.
        param.put("SIV_NO_START", input.getSivNo());
        param.put("SIV_NO_END", input.getSivNo());
        param.put("COMPANY_CODE", userProfile.getCompanyCode());
        param.put("PLANT_NO", userProfile.getPlantNo());
        param.put("SUB_REPORT", jasperSubReport);
        // Fetching the inventoryuser from the data source.
        Connection source = dataSource.getConnection();
        // Filling the report with the data and additional parameters information.
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperMainReport, param, source);
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    @Override
    public SIVDetailDTO checkNextItem(SIVDTO input) {
        UserProfile userProfile = UserProfileContext.getUserProfile();
        SIVDetailDTO detailDTO = SIVDetailDTO.builder().build();
        if (input.getSivDetails().size() != 0) {
            for (SIVDetailDTO rec : input.getSivDetails()) {
                if (input.getSubType().equals("M")) {
                    //check project no 1
                    if (rec.getItemType() == 0) {
                        if (input.getTranType().equals("PR")) {
                            if (StringUtils.isBlank(rec.getProjectNo1())) {
                                throw new ServerException("Project No 1 cannot be empty for PR ref type !");
                            } else {
                                SaleProjection saleTypeCur = saleRepository.saleTypeCur(userProfile.getCompanyCode(),
                                        userProfile.getPlantNo(), rec.getProjectNo1());
                                if (saleTypeCur == null) {
                                    throw new ServerException(String.format("%s, is not a valid entry !", rec.getProjectNo1()));
                                }
                            }
                        } else if (!input.getTranType().equals("PR")) {
                            detailDTO.setProjectNo1(null);
                            throw new ServerException("Project No 1 cannot be empty for PR ref type !");
                        }

                        if (!input.getTranType().equals("OTHER")) {

                        }
                    }
                }
            }
        }
        return detailDTO;
    }

    @Override
    public List<ItemDTO> getAllItemNo() {
        List<ItemDTO> list = new ArrayList<>();
        List<ItemProjection> allItemNo = itemRepository.getAllItemNo();
        for (ItemProjection rec : allItemNo) {
            list.add(ItemDTO.builder().itemNo(rec.getItemNo()).build());
        }
        return list;
    }

    private SIVDetailDTO checkValidPRWKItemNo(UserProfile userProfile, SIVDTO input) {

        SIVDetailDTO detailDTO = SIVDetailDTO.builder().build();
        for (SIVDetailDTO dto : input.getSivDetails()) {
            if (dto.getItemType() == 0) {
                ItemProjection rec = itemRepository.itemOtherCurOrItemPRCur(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getItemNo());
                if (rec == null) {
                    throw new ServerException("Item No : " + dto.getItemNo() + " is Invalid or not found in ITEM Table!");
                } else {
                    detailDTO.setLoc(rec.getLoc());
                    detailDTO.setUom(rec.getUom());
                    detailDTO.setDocmNo(input.getDocmNo());
                }
            }
        }
        return detailDTO;
    }

    private SIVDetailDTO checkRecValidItemNoDSWD(UserProfile userProfile, SIVDTO input) {

        SIVDetailDTO detailDTO = SIVDetailDTO.builder().build();
        for (SIVDetailDTO dto : input.getSivDetails()) {
            if (dto.getItemType() == 0) {
                List<ItemProjection> itemCur = itemRepository.itemCur(input.getDocmNo(), userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getItemNo());
                if (itemCur.size() == 0) {
                    throw new ServerException("Item No : " + dto.getItemNo() + " is Invalid or not found in SALEDET/ITEM Table!");
                } else {
                    for (ItemProjection rec : itemCur) {
                        detailDTO.setLoc(rec.getLoc());
                        detailDTO.setUom(rec.getUom());
                        detailDTO.setDocmNo(input.getDocmNo());
                        detailDTO.setProjectNo1(input.getDocmNo());
                        detailDTO.setIssuedQty1(dto.getIssuedQty());
                    }
                }
            }
        }
        return detailDTO;
    }

    private SIVDetailDTO checkValidOtherItemNo(UserProfile userProfile, SIVDTO input) {
        SIVDetailDTO detailDTO = SIVDetailDTO.builder().build();
        for (SIVDetailDTO dto : input.getSivDetails()) {
            if (dto.getItemType() == 0) {
                ItemProjection rec = itemRepository.itemOtherCurOrItemPRCur(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getItemNo());
                if (rec == null) {
                    throw new ServerException("Item No : " + dto.getItemNo() + " is Invalid or not found in ITEM Table!");
                } else {
                    detailDTO.setLoc(rec.getLoc());
                    detailDTO.setUom(rec.getUom());
                    detailDTO.setDocmNo(input.getDocmNo());
                }
            }
        }
        return detailDTO;
    }

    private List<SIVDetailDTO> populateDetailsManual(UserProfile userProfile, SIVDTO input) {
        List<SIVDetailDTO> list = new ArrayList<>();
        String tranType = input.getTranType();
        String prRmk = "";
        String saleType = "";
        PRProjection prRmkProj = prRepository.prRmk(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getDocmNo());
        if (input.getTranType().equals("PR") || input.getTranType().equals("WK")) {
            checkValidPRNo(userProfile, input);
            prRmk = prRmkProj.getRemarks();
            saleType = "P";
        } else if (input.getTranType().equals("DS") || input.getTranType().equals("WD")) {
            checkValidOrderNo(userProfile, input);
            saleType = "D";
        } else if (input.getTranType().equals("OTHER")) {
            String docmNo = input.getDocmNo().substring(0, 2);
            if (docmNo.equals("PR")) {
                checkValidPRNo(userProfile, input);
                tranType = "PR";
            }
        }

        List<PRDetailProjection> bombypjCurs = prDetailRepository.bombypjCur(input.getDocmNo(), prRmk, userProfile.getCompanyCode(),
                userProfile.getPlantNo(), tranType, saleType);
        int seqNo = 1;
        for (PRDetailProjection bomRec : bombypjCurs) {
            BigDecimal docmPickQty = bomRec.getPickedQty();
            if (bomRec.getItemType() == 0) {
                BigDecimal docmShortQtyL = BigDecimal.ZERO;
                BigDecimal docmShortQtyF = BigDecimal.ZERO;
                BigDecimal docmShortQty = BigDecimal.ZERO;
                BigDecimal itemAvailQty = BigDecimal.ZERO;
                if (bomRec.getShortQty().compareTo(BigDecimal.ZERO) > 0) {
                    docmShortQty = bomRec.getShortQty();
                    itemAvailQty = BigDecimal.ZERO;
                    ItemLocProjection itemAvailQohL = itemLocRepository.getItemAvailQohL(userProfile.getCompanyCode(), userProfile.getPlantNo(), bomRec.getAlternate());
                    itemAvailQty = itemAvailQohL.getAvailQty();
                    if (itemAvailQty.compareTo(BigDecimal.ZERO) > 0) {
                        if (itemAvailQty.compareTo(bomRec.getShortQty()) > 0) {
                            docmShortQtyL = bomRec.getShortQty();
                            docmShortQty = BigDecimal.ZERO;
                        } else {
                            docmShortQtyL = itemAvailQty;
                            docmShortQty = docmShortQty.subtract(itemAvailQty);
                        }
                    }

                    if (docmShortQty.compareTo(BigDecimal.ZERO) > 0) {
                        itemAvailQty = BigDecimal.ZERO;
                        ItemLocProjection itemAvailQohF = itemLocRepository.getItemAvailQohF(userProfile.getCompanyCode(), userProfile.getPlantNo(), bomRec.getAlternate());
                        itemAvailQty = itemAvailQohF.getAvailQty();

                        if (itemAvailQty.compareTo(BigDecimal.ZERO) > 0) {
                            if (itemAvailQty.compareTo(bomRec.getShortQty()) > 0) {
                                docmShortQtyF = bomRec.getShortQty();
                                docmShortQty = BigDecimal.ZERO;
                            } else {
                                docmShortQtyF = itemAvailQty;
                                docmShortQty = docmShortQty.subtract(itemAvailQty);
                            }
                        }
                    }
                }

                List<ItemBatchProjection> batchCur = itemBatcRepository.getBatchNoByItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), bomRec.getAlternate());
                for (ItemBatchProjection batRec : batchCur) {
                    BigDecimal batchQty = batRec.getQoh();
                    BigDecimal pickQty = BigDecimal.ZERO;
                    BigDecimal shortQty = BigDecimal.ZERO;
                    String projectNo1 = null;
                    BigDecimal issuedQty1 = null;
                    if (docmPickQty.compareTo(BigDecimal.ZERO) > 0) {
                        if (docmPickQty.compareTo(batchQty) > 0) {
                            pickQty = batchQty;
                            docmPickQty = docmPickQty.subtract(batchQty);
                            batchQty = BigDecimal.ZERO;
                        } else {
                            pickQty = docmPickQty;
                            batchQty = batchQty.subtract(docmPickQty);
                            docmPickQty = BigDecimal.ZERO;
                        }

                        if (input.getTranType().equals("DS") || input.getTranType().equals("WD")) {
                            projectNo1 = input.getDocmNo();
                            issuedQty1 = pickQty;
                        }

                        list.add(SIVDetailDTO.builder()
                                .seqNo(seqNo++)
                                .itemType(bomRec.getItemType())
                                .itemNo(bomRec.getAlternate())
                                .partNo(bomRec.getPartNo())
                                .loc(bomRec.getLoc())
                                .uom(bomRec.getUom())
                                .batchNo(batRec.getBatchNo())
                                .batchLoc(batRec.getBatchNo() + "/" + bomRec.getLoc())
                                .issuedQty(pickQty)
                                .extraQty(BigDecimal.ZERO)
                                .issuedPrice(bomRec.getStdMaterial())
                                .bomPickQty(bomRec.getPickedQty())
                                .bomShortQtyL(BigDecimal.ZERO)
                                .bomShortQtyF(BigDecimal.ZERO)
                                .batchQty(batRec.getQoh())
                                .saleType(bomRec.getSaleType())
                                .docmNo(bomRec.getDocmNo())
                                .remarks(bomRec.getRemarks())
                                .projectNo1(projectNo1)
                                .issuedQty1(issuedQty1).build());
                    }

                    if (docmShortQtyL.compareTo(BigDecimal.ZERO) > 0 && batchQty.compareTo(BigDecimal.ZERO) > 0) {
                        if (docmShortQtyL.compareTo(batchQty) > 0) {
                            pickQty = batchQty;
                            shortQty = batchQty;
                            docmShortQtyL = docmShortQtyL.subtract(batchQty);
                            batchQty = BigDecimal.ZERO;
                        } else {
                            pickQty = docmShortQtyL;
                            shortQty = docmShortQtyL;
                            batchQty = batchQty.subtract(docmShortQtyL);
                            docmShortQtyL = BigDecimal.ZERO;
                        }

                        if (input.getTranType().equals("DS") || input.getTranType().equals("WD")) {
                            projectNo1 = input.getDocmNo();
                            issuedQty1 = pickQty;
                        }

                        list.add(SIVDetailDTO.builder()
                                .seqNo(seqNo++)
                                .itemType(bomRec.getItemType())
                                .itemNo(bomRec.getAlternate())
                                .partNo(bomRec.getPartNo())
                                .loc(bomRec.getLoc())
                                .uom(bomRec.getUom())
                                .batchNo(null)
                                .batchLoc(null)
                                .issuedQty(pickQty)
                                .extraQty(BigDecimal.ZERO)
                                .issuedPrice(bomRec.getStdMaterial())
                                .bomPickQty(pickQty)
                                .bomShortQtyL(shortQty)
                                .bomShortQtyF(BigDecimal.ZERO)
                                .batchQty(batRec.getQoh())
                                .saleType(bomRec.getSaleType())
                                .docmNo(bomRec.getDocmNo())
                                .remarks(bomRec.getPartNo())
                                .projectNo1(projectNo1)
                                .issuedQty1(issuedQty1).build());
                    }

                    CompanyProjection companyStockLoc = companyRepository.getStockLoc(userProfile.getCompanyCode(), userProfile.getPlantNo()); // get stock loc company
                    if (docmShortQtyF.compareTo(BigDecimal.ZERO) > 0 && batchQty.compareTo(BigDecimal.ZERO) > 0 && !batRec.getLoc().equals(companyStockLoc.getStockLoc())) {
                        if (docmShortQtyF.compareTo(batchQty) > 0) {
                            pickQty = batchQty;
                            shortQty = batchQty;
                            docmShortQtyF = docmShortQtyF.subtract(batchQty);
                            batchQty = BigDecimal.ZERO;
                        } else {
                            pickQty = docmShortQtyF;
                            shortQty = docmShortQtyF;
                            batchQty = batchQty.subtract(docmShortQtyF);
                            docmShortQtyF = BigDecimal.ZERO;
                        }

                        if (input.getTranType().equals("DS") || input.getTranType().equals("WD")) {
                            projectNo1 = input.getDocmNo();
                            issuedQty1 = pickQty;
                        }

                        list.add(SIVDetailDTO.builder()
                                .seqNo(seqNo++)
                                .itemType(bomRec.getItemType())
                                .itemNo(bomRec.getAlternate())
                                .partNo(bomRec.getPartNo())
                                .loc(bomRec.getLoc())
                                .uom(bomRec.getUom())
                                .batchNo(null)
                                .batchLoc(null)
                                .issuedQty(pickQty)
                                .extraQty(BigDecimal.ZERO)
                                .issuedPrice(bomRec.getStdMaterial())
                                .bomPickQty(pickQty)
                                .bomShortQtyL(BigDecimal.ZERO)
                                .bomShortQtyF(shortQty)
                                .batchQty(batRec.getQoh())
                                .saleType(bomRec.getSaleType())
                                .docmNo(bomRec.getDocmNo())
                                .remarks(bomRec.getPartNo())
                                .projectNo1(projectNo1)
                                .issuedQty1(issuedQty1).build());
                    }
                }

                if (docmPickQty.compareTo(BigDecimal.ZERO) != 0) {
                    throw new ServerException("Error in Batch QOH Distribution!");
                }
            } else {
                String projectNo1 = null;
                BigDecimal issuedQty1 = null;
                if (input.getTranType().equals("DS") || input.getTranType().equals("WD")) {
                    projectNo1 = input.getDocmNo();
                    issuedQty1 = bomRec.getPickedQty();
                }

                list.add(SIVDetailDTO.builder()
                        .seqNo(seqNo++)
                        .itemType(bomRec.getItemType())
                        .itemNo(bomRec.getAlternate())
                        .partNo(bomRec.getPartNo())
                        .loc(bomRec.getLoc())
                        .uom(bomRec.getUom())
                        .issuedQty(bomRec.getPickedQty())
                        .extraQty(BigDecimal.ZERO)
                        .issuedPrice(bomRec.getStdMaterial())
                        .bomPickQty(bomRec.getPickedQty())
                        .saleType(bomRec.getSaleType())
                        .docmNo(bomRec.getDocmNo())
                        .remarks(bomRec.getRemarks())
                        .projectNo1(projectNo1)
                        .issuedQty1(issuedQty1)
                        .build());
            }
        }

        return list;
    }

    private void checkValidOrderNo(UserProfile userProfile, SIVDTO input) {
        SaleProjection cOrder = saleRepository.cOrder(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getDocmNo());
        if (cOrder == null) {
            throw new NotFoundException("" + input.getDocmNo() + " is Invalid or not found!");
        } else if (cOrder.getOpenClose().equals("C") || cOrder.getOpenClose().equals("V")) {
            throw new NotFoundException("" + input.getDocmNo() + " is CLOSED/VOIDED !");
        }
    }

    private void checkValidPRNo(UserProfile userProfile, SIVDTO input) {
        PRProjection cPr = prRepository.cPr(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getDocmNo());
        if (StringUtils.isBlank(cPr.getDocmNo())) {
            throw new NotFoundException("" + input.getDocmNo() + " is Invalid or not found!");
        } else if (!cPr.getStatus().equals("A")) {
            throw new NotFoundException("" + input.getDocmNo() + " is yet to be approved!");
        } else if (cPr.getStatus().equals("C") || cPr.getStatus().equals("V")) {
            throw new NotFoundException("" + input.getDocmNo() + " is CLOSED/VOIDED !");
        }
    }

    @Override
    public SIVDTO getDefaultValueSIV(String subType) {
        UserProfile userProfile = UserProfileContext.getUserProfile();
        String entryTime = FastDateFormat.getInstance("kkmmss").format(System.currentTimeMillis());
        // subType (N : for Entry, M : for Manual)
        return SIVDTO.builder().currencyCode("USD").currencyRate(BigDecimal.ONE)
                .entryUser(userProfile.getUsername()).subType(subType).statuz("O")
                .entryDate(new Timestamp(System.currentTimeMillis())).entryTime(entryTime).build();
    }


    @Override
    public List<SIVDetailDTO> populateBatchList(String subType, String projectNo, String itemNo) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        List<SIVDetailDTO> populateDetails = populateDetails(userProfile, projectNo);
        List<SIVDetailDTO> listPopulateBatch = new ArrayList<>();

        int countSeqNo = 1;
        int countGrnDetSeqNo = 1;

        if (subType.equals("N")) {
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
                            iBatchFloc.setGrndetSeqNo(countGrnDetSeqNo++);
                            iBatchFloc.setItemNo(dto.getItemNo());
                            iBatchFloc.setBatchDesc(ib.getBatchDesc());
                            iBatchFloc.setBatchNoLoc(ib.getBatchNoLoc());
                            listPopulateBatch.add(iBatchFloc);
                        }
                    } else {
                        for (ItemBatchProjection ib : itemBatch) {
                            SIVDetailDTO iBatch = SIVDetailDTO.builder().build();
                            iBatch.setSeqNo(countSeqNo++);
                            iBatch.setGrndetSeqNo(countGrnDetSeqNo++);
                            iBatch.setItemNo(dto.getItemNo());
                            iBatch.setBatchDesc(ib.getBatchDesc());
                            iBatch.setBatchNoLoc(ib.getBatchNoLoc());
                            listPopulateBatch.add(iBatch);
                        }
                    }
                }
            }
        } else {
            if (StringUtils.isNotBlank(itemNo)) {
                List<ItemBatchProjection> itemBatch = itemBatcRepository.getItemBatchByItemNo(userProfile.getCompanyCode(),
                        userProfile.getPlantNo(), itemNo);
                for (ItemBatchProjection ib : itemBatch) {
                    SIVDetailDTO iBatch = SIVDetailDTO.builder().build();
                    iBatch.setSeqNo(countSeqNo++);
                    iBatch.setGrndetSeqNo(countGrnDetSeqNo++);
                    iBatch.setItemNo(itemNo);
                    iBatch.setBatchDesc(ib.getBatchDesc());
                    iBatch.setBatchNoLoc(ib.getBatchNoLoc());
                    listPopulateBatch.add(iBatch);
                }
            }
        }

        return listPopulateBatch;
    }

    private SIVDetailDTO checkRecValidIssuedQty(UserProfile userProfile, SIVDTO input, SIVDetailDTO detailDTO) {

        SIVDetailDTO dto = SIVDetailDTO.builder().build();
        if (input.getSubType().equals("N")) {
            List<SIVDetailDTO> populateDetails = populateDetails(userProfile, input.getProjectNo());
            for (SIVDetailDTO rec : populateDetails) {
                for (SIVDetailDTO dtos : input.getSivDetails()) {
                    if (dtos.getItemNo().equals(rec.getItemNo()) && dtos.getSeqNo() == rec.getSeqNo()) {
                        if (dtos.getIssuedQty().compareTo(BigDecimal.ZERO) != 0) {
                            if (dtos.getIssuedQty().compareTo(rec.getBomPickQty()) > 0) {
                                dto.setExtraQty(rec.getIssuedQty().subtract(rec.getBomPickQty()));
                            }
                            if (dtos.getBatchLoc() != null) {
                                if (dtos.getIssuedQty().compareTo(BigDecimal.ZERO) > 0) {
                                    if (rec.getBomPickQty() == null) {
                                        throw new NotFoundException("Item No : " + rec.getItemNo() + " not found !");
                                    } else if (rec.getBomPickQty().compareTo(BigDecimal.ZERO) == 0) {
                                        throw new ServerException("Item No : " + rec.getItemNo() + " has no pick qty !");
                                    }

                                    if (dtos.getIssuedQty().compareTo(rec.getBatchQty()) > 0) {
                                        qtyReset(rec);
                                        throw new ServerException("Item No : " + rec.getItemNo() + " Issued > Batch Qty !");
                                    }

                                    ItemLocProjection qohCur = itemLocRepository.getQohCur(userProfile.getCompanyCode(), userProfile.getPlantNo(), rec.getItemNo(), rec.getLoc());
                                    if (qohCur.getQoh().compareTo(dtos.getIssuedQty()) < 0) {
                                        qtyReset(rec);
                                        throw new ServerException("Item No : " + rec.getItemNo() + " Issued > Qty-On Hand !");
                                    }

                                    if (dtos.getIssuedQty().subtract(rec.getBomPickQty()).compareTo(BigDecimal.ZERO) > 0
                                            && qohCur.getEoh().compareTo(dtos.getIssuedQty().subtract(rec.getBomPickQty())) < 0) {
                                        qtyReset(rec);
                                        throw new ServerException("Issued Qty of " + rec.getItemNo() + " Cannot be > EOH, EOH now is : " + qohCur.getEoh() + " !");
                                    }

                                    ItemLocProjection resv = itemLocRepository.getResv(userProfile.getCompanyCode(), userProfile.getPlantNo(), dtos.getItemNo());
                                    if (resv.getProdnResv().subtract(resv.getPoResvQty()).compareTo(resv.getResvQty()) != 0) {
                                        throw new ServerException("" + dtos.getItemNo() + " Item Resv does not match Bom total Resv ! " +
                                                "Prodn Resv : " + resv.getProdnResv() + ", Resv Qty : " + resv.getResvQty() + " Inform MIS");
                                    }
                                } else {
                                    qtyReset(rec);
                                    throw new ServerException("Issued Qty Must be > 0");
                                }
                            }
                        } else {
                            qtyReset(rec);
                            throw new ServerException("Issued Qty Must be > 0");
                        }
                    }

                    dto.setItemNo(dtos.getItemNo());
                    dto.setSeqNo(dto.getSeqNo());
                    dto.setIssuedQty(dtos.getIssuedQty());
                    dto.setGrndetSeqNo(dto.getSeqNo());
                }
            }
        } else {
            if (input.getSubType().equals("M")) {
                for (SIVDetailDTO detail : input.getSivDetails()) {
                    if (detail.getIssuedQty().compareTo(BigDecimal.ZERO) > 0) {
                        if (detail.getItemType() == 0) {
                            if (StringUtils.isNotBlank(detail.getProjectNo1())) {
                                if (detail.getIssuedQty1().compareTo(BigDecimal.ZERO) <= 0) {
                                    throw new ServerException("Issued Qty Must be > 0");
                                }
                            }
                            if (StringUtils.isNotBlank(detail.getProjectNo2())) {
                                if (detail.getIssuedQty2().compareTo(BigDecimal.ZERO) <= 0) {
                                    throw new ServerException("Issued Qty Must be > 0");
                                }
                            }
                            if (StringUtils.isNotBlank(detail.getProjectNo3())) {
                                if (detail.getIssuedQty3().compareTo(BigDecimal.ZERO) <= 0) {
                                    throw new ServerException("Issued Qty Must be > 0");
                                }
                            }
                            if (StringUtils.isNotBlank(detail.getProjectNo4())) {
                                if (detail.getIssuedQty4().compareTo(BigDecimal.ZERO) <= 0) {
                                    throw new ServerException("Issued Qty Must be > 0");
                                }
                            }
                            if (StringUtils.isNotBlank(detail.getProjectNo5())) {
                                if (detail.getIssuedQty5().compareTo(BigDecimal.ZERO) <= 0) {
                                    throw new ServerException("Issued Qty Must be > 0");
                                }
                            }

                            BombypjProjection bombPickedQty;
                            if (StringUtils.isNotBlank(input.getProjectNo())) {
                                bombPickedQty = bombypjRepository.bombypjCurCaseWhen(userProfile.getCompanyCode(),
                                        userProfile.getPlantNo(), input.getProjectNo(), detail.getItemNo());
                            } else {
                                bombPickedQty = bombypjRepository.bombypjCurCaseWhen(userProfile.getCompanyCode(),
                                        userProfile.getPlantNo(), input.getDocmNo(), detail.getItemNo());
                            }

                            if (bombPickedQty == null) {
                                if (StringUtils.isNotBlank(input.getProjectNo())) {
                                    throw new ServerException("Ref No " + input.getProjectNo() + " not found !");
                                } else {
                                    throw new ServerException("Ref No " + input.getDocmNo() + " not found !");
                                }
                            } else if (bombPickedQty.getPickedQty().compareTo(BigDecimal.ZERO) == 0) {
                                throw new ServerException("Item No" + detail.getItemNo() + " has no outstanding qty !");
                            } else if (bombPickedQty.getPickedQty().compareTo(detail.getIssuedQty()) < 0) {
                                dto.setExtraQty(detail.getIssuedQty().subtract(bombPickedQty.getPickedQty()));
                            } else {
                                dto.setExtraQty(BigDecimal.ZERO);
                            }

                            List<ItemLocProjection> qohCur = itemLocRepository.qohCur(userProfile.getCompanyCode(),
                                    userProfile.getPlantNo(), detailDTO.getItemNo(), detailDTO.getLoc());
                            for (ItemLocProjection rec : qohCur) {
                                BigDecimal vQoh = (rec.getQoh() == null ? BigDecimal.ZERO : rec.getQoh());
                                BigDecimal vProdnResv = rec.getProdnResv();
                                BigDecimal vStdMat = (rec.getStdMaterial() == null ? BigDecimal.ZERO : rec.getStdMaterial());
                                if (vQoh.compareTo(detail.getIssuedQty()) < 0) {
                                    throw new ServerException("Item No : " + detail.getItemNo() + " Issued > Qty-On Hand !");
                                }

                                BigDecimal vEOH = vQoh.subtract(vProdnResv);
                                if (dto.getExtraQty().compareTo(BigDecimal.ZERO) > 0 && vEOH.compareTo(dto.getExtraQty()) < 0) {
                                    dto.setIssuedQty(bombPickedQty.getPickedQty());
                                    dto.setIssuedQty1(null);
                                    dto.setIssuedQty2(null);
                                    dto.setIssuedQty3(null);
                                    dto.setIssuedQty4(null);
                                    dto.setIssuedQty5(null);
                                    throw new ServerException("Issued Qty of " + rec.getItemNo() + " Cannot be > EOH, EOH now is : " + vEOH + " !");
                                } else {
                                    dto.setIssuedPrice(vStdMat);
                                }
                            }
                        } else {
                            dto.setIssuedPrice(BigDecimal.ZERO);
                        }
                    } else {
                        throw new ServerException("Issued Qty MUST be > 0 !");
                    }
                }
            }
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
                            .grndetSeqNo(countGrnDetSeqNo++)
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
                            .grndetSeqNo(countGrnDetSeqNo++)
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
                            .grndetSeqNo(countGrnDetSeqNo++)
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