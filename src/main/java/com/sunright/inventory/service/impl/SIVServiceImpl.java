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
import com.sunright.inventory.entity.pur.PurDetProjection;
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
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
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
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
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
    private PurDetRepository purDetRepository;

    @Autowired
    private QueryGenerator queryGenerator;

    @Override
    public SIVDTO createSIV(SIVDTO input) {

        UserProfile userProfile = UserProfileContext.getUserProfile();

        SIV siv = new SIV();
        String entryTime = FastDateFormat.getInstance("kkmmssss").format(System.currentTimeMillis());
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
        checkRecValid(input);
        checkIfSivNoExist(userProfile, input);
        SIV saved = null, savedA = null, savedB = null, savedC = null, savedD = null, savedE = null;
        // Insert SIV Combine
        if (StringUtils.equals(input.getSivType(), "Combine")) {
            if (StringUtils.isNotBlank(input.getProjNoA())) {
                if (StringUtils.isNotBlank(input.getSivNoA())) {
                    postBomproj(userProfile, input.getSivNoA(), input.getProjNoA());
                    siv.setSivNo(input.getSivNoA());
                    siv.setProjectNo(input.getProjNoA());
                    savedA = sivRepository.save(siv);
                    input.setId(savedA.getId());
                    input.setVersion(savedA.getVersion());
                }
                if (StringUtils.isNotBlank(input.getSivNoB())) {
                    postBomproj(userProfile, input.getSivNoB(), input.getProjNoB());
                    siv.setSivNo(input.getSivNoB());
                    siv.setProjectNo(input.getProjNoB());
                    savedB = sivRepository.save(siv);
                    input.setId(savedB.getId());
                    input.setVersion(savedB.getVersion());
                }
                if (StringUtils.isNotBlank(input.getSivNoC())) {
                    postBomproj(userProfile, input.getSivNoC(), input.getProjNoC());
                    siv.setSivNo(input.getSivNoC());
                    siv.setProjectNo(input.getProjNoC());
                    savedC = sivRepository.save(siv);
                    input.setId(savedC.getId());
                    input.setVersion(savedC.getVersion());
                }
                if (StringUtils.isNotBlank(input.getSivNoD())) {
                    postBomproj(userProfile, input.getSivNoD(), input.getProjNoD());
                    siv.setSivNo(input.getSivNoD());
                    siv.setProjectNo(input.getProjNoD());
                    savedD = sivRepository.save(siv);
                    input.setId(savedD.getId());
                    input.setVersion(savedD.getVersion());
                }
                if (StringUtils.isNotBlank(input.getSivNoE())) {
                    postBomproj(userProfile, input.getSivNoE(), input.getProjNoE());
                    siv.setSivNo(input.getSivNoE());
                    siv.setProjectNo(input.getProjNoE());
                    savedE = sivRepository.save(siv);
                    input.setId(savedE.getId());
                    input.setVersion(savedE.getVersion());
                }
            }
        } else {
            // Insert SIV Entry & Manual
            saved = sivRepository.save(siv);
        }
        if (!CollectionUtils.isEmpty(input.getSivDetails())) {
            for (SIVDetailDTO detail : input.getSivDetails()) {
                SIVDetail sivDetail = new SIVDetail();
                checkRecValidDetail(userProfile, input, detail);
                preSavingDetail(userProfile, input, detail);
                BeanUtils.copyProperties(detail, sivDetail);
                sivDetail.setCompanyCode(userProfile.getCompanyCode());
                sivDetail.setPlantNo(userProfile.getPlantNo());
                String remarksCombine = "Issued Thru SIV-N, Prg : INM00024";
                if (StringUtils.isNotBlank(input.getSivNoA()) && detail.getIssuedQtyA().compareTo(BigDecimal.ZERO) > 0) {
                    postGrn(userProfile, detail.getSivNoA(), detail.getIssuedQtyA(), detail);
                    postSfc(userProfile, detail.getProjNoA(), detail.getItemNo(), detail.getIssuedQtyA());
                    postItemInv(userProfile, detail.getItemNo(), detail.getLoc(), detail.getIssuedQtyA(),
                            detail.getIssuedQtyA().subtract(detail.getBomQtyA()), detail.getBatchNo(),
                            detail.getSivNoA(), detail.getProjNoA(), remarksCombine, detail);
                    postBombypjCombine(userProfile, detail.getProjNoA(), detail.getItemNo(), detail.getIssuedQtyA());
                    postCoqCombine(userProfile, detail.getProjNoA(), detail.getItemNo(), detail.getBatchNo(),
                            detail.getSivNoA(), detail.getIssuedQtyA(), detail.getIssuedPrice());
                    sivDetail.setSivNo(detail.getSivNoA());
                    sivDetail.setIssuedQty(detail.getIssuedQtyA());
                    sivDetail.setSiv(savedA);
                    SIVDetail savedDetailA = sivDetailRepository.save(sivDetail);
                }
                if (StringUtils.isNotBlank(input.getSivNoB())) {
                    postGrn(userProfile, detail.getSivNoB(), detail.getIssuedQtyB(), detail);
                    postSfc(userProfile, detail.getProjNoB(), detail.getItemNo(), detail.getIssuedQtyB());
                    postItemInv(userProfile, detail.getItemNo(), detail.getLoc(), detail.getIssuedQtyB(),
                            detail.getIssuedQtyB().subtract(detail.getBomQtyB()), detail.getBatchNo(),
                            detail.getSivNoB(), detail.getProjNoB(), remarksCombine, detail);
                    postBombypjCombine(userProfile, detail.getProjNoB(), detail.getItemNo(), detail.getIssuedQtyB());
                    postCoqCombine(userProfile, detail.getProjNoB(), detail.getItemNo(), detail.getBatchNo(),
                            detail.getSivNoB(), detail.getIssuedQtyB(), detail.getIssuedPrice());
                    sivDetail.setSivNo(detail.getSivNoB());
                    sivDetail.setIssuedQty(detail.getIssuedQtyB());
                    sivDetail.setSiv(savedB);
                    SIVDetail savedDetailB = sivDetailRepository.save(sivDetail);
                }
                if (StringUtils.isNotBlank(input.getSivNoC())) {
                    postGrn(userProfile, detail.getSivNoC(), detail.getIssuedQtyC(), detail);
                    postSfc(userProfile, detail.getProjNoC(), detail.getItemNo(), detail.getIssuedQtyC());
                    postItemInv(userProfile, detail.getItemNo(), detail.getLoc(), detail.getIssuedQtyC(),
                            detail.getIssuedQtyC().subtract(detail.getBomQtyC()), detail.getBatchNo(),
                            detail.getSivNoC(), detail.getProjNoC(), remarksCombine, detail);
                    postBombypjCombine(userProfile, detail.getProjNoC(), detail.getItemNo(), detail.getIssuedQtyC());
                    postCoqCombine(userProfile, detail.getProjNoC(), detail.getItemNo(), detail.getBatchNo(),
                            detail.getSivNoC(), detail.getIssuedQtyC(), detail.getIssuedPrice());
                    sivDetail.setSivNo(detail.getSivNoC());
                    sivDetail.setIssuedQty(detail.getIssuedQtyC());
                    sivDetail.setSiv(savedC);
                    SIVDetail savedDetailC = sivDetailRepository.save(sivDetail);
                }
                if (StringUtils.isNotBlank(input.getSivNoD())) {
                    postGrn(userProfile, detail.getSivNoD(), detail.getIssuedQtyD(), detail);
                    postSfc(userProfile, detail.getProjNoD(), detail.getItemNo(), detail.getIssuedQtyD());
                    postItemInv(userProfile, detail.getItemNo(), detail.getLoc(), detail.getIssuedQtyD(),
                            detail.getIssuedQtyD().subtract(detail.getBomQtyD()), detail.getBatchNo(),
                            detail.getSivNoD(), detail.getProjNoD(), remarksCombine, detail);
                    postBombypjCombine(userProfile, detail.getProjNoD(), detail.getItemNo(), detail.getIssuedQtyD());
                    postCoqCombine(userProfile, detail.getProjNoD(), detail.getItemNo(), detail.getBatchNo(),
                            detail.getSivNoD(), detail.getIssuedQtyD(), detail.getIssuedPrice());
                    sivDetail.setSivNo(detail.getSivNoD());
                    sivDetail.setIssuedQty(detail.getIssuedQtyD());
                    sivDetail.setSiv(savedD);
                    SIVDetail savedDetailD = sivDetailRepository.save(sivDetail);
                }
                if (StringUtils.isNotBlank(input.getSivNoE())) {
                    postGrn(userProfile, detail.getSivNoE(), detail.getIssuedQtyE(), detail);
                    postSfc(userProfile, detail.getProjNoE(), detail.getItemNo(), detail.getIssuedQtyE());
                    postItemInv(userProfile, detail.getItemNo(), detail.getLoc(), detail.getIssuedQtyE(),
                            detail.getIssuedQtyE().subtract(detail.getBomQtyE()), detail.getBatchNo(),
                            detail.getSivNoE(), detail.getProjNoE(), remarksCombine, detail);
                    postBombypjCombine(userProfile, detail.getProjNoE(), detail.getItemNo(), detail.getIssuedQtyE());
                    postCoqCombine(userProfile, detail.getProjNoE(), detail.getItemNo(), detail.getBatchNo(),
                            detail.getSivNoE(), detail.getIssuedQtyE(), detail.getIssuedPrice());
                    sivDetail.setSivNo(detail.getSivNoE());
                    sivDetail.setIssuedQty(detail.getIssuedQtyE());
                    sivDetail.setSiv(savedE);
                    SIVDetail savedDetailE = sivDetailRepository.save(sivDetail);
                }
                if (StringUtils.isBlank(input.getSivNoA()) && StringUtils.isBlank(input.getSivNoB()) && StringUtils.isBlank(input.getSivNoC())
                        && StringUtils.isBlank(input.getSivNoD()) && StringUtils.isBlank(input.getSivNoE())) {
                    sivDetail.setSiv(saved);
                    SIVDetail savedDetail = sivDetailRepository.save(sivDetail);
                    sivDetailPostSaving(userProfile, saved, sivDetail, detail, input, savedDetail);
                }
                if (input.getSubType().equals("M")) {
                    procPRClosure(userProfile, input);
                } else {
                    if (!StringUtils.equals(input.getSivType(), "Combine")) {
                        procBatchConsolidate(sivDetail, detail);
                        procUpdateSfcWip(userProfile, sivDetail, detail, input);
                    }
                }
            }
        }

        sivPostSaving(userProfile, saved, input);
        populateAfterSaving(input, saved);

        return input;
    }

    private void postBombypjCombine(UserProfile userProfile, String projNo, String itemNo, BigDecimal issuedQty) {

        BigDecimal sivIssue = issuedQty;
        BigDecimal shortQty = BigDecimal.ZERO;
        BigDecimal pickIss = issuedQty;
        BigDecimal shortIss = shortQty;

        List<BombypjProjection> resBombypj = bombypjRepository.getBombypjInfoByStatus(userProfile.getCompanyCode(),
                userProfile.getPlantNo(), projNo, itemNo, shortQty);

        if (resBombypj.size() != 0) {
            for (BombypjProjection bProj : resBombypj) {
                BigDecimal resvQty = bProj.getResvQty() == null ? BigDecimal.ZERO : bProj.getResvQty();
                BigDecimal issuedQtyBomb = bProj.getIssuedQty() == null ? BigDecimal.ZERO : bProj.getIssuedQty();
                BigDecimal pickedQty = bProj.getPickedQty() == null ? BigDecimal.ZERO : bProj.getPickedQty();
                if (sivIssue.compareTo(pickedQty) > 0) {
                    resvQty = resvQty.subtract(pickedQty);
                    issuedQty = issuedQty.add(pickedQty);
                    sivIssue = sivIssue.subtract(pickedQty);
                    pickedQty = BigDecimal.ZERO;
                } else {
                    resvQty = resvQty.subtract(sivIssue);
                    issuedQty = issuedQty.add(sivIssue);
                    pickedQty = pickedQty.subtract(sivIssue);
                    sivIssue = BigDecimal.ZERO;
                }

                if (resvQty.compareTo(BigDecimal.ZERO) < 0) {
                    resvQty = BigDecimal.ZERO;
                }

                bombypjRepository.updateResvIssuedPickedQty(resvQty, issuedQty, pickedQty,
                        userProfile.getCompanyCode(), userProfile.getPlantNo(), projNo, itemNo);

                if (sivIssue.compareTo(BigDecimal.ZERO) == 0) {
                    break;
                }
            }

            if (sivIssue.compareTo(BigDecimal.ZERO) > 0 && resBombypj.size() != 0) {
                /** extra will be assign to the last retrieved record **/
                BombypjProjection bombypjInfo = bombypjRepository.getBombypjInfo(userProfile.getCompanyCode(),
                        userProfile.getPlantNo(), projNo, itemNo);
                BigDecimal issuedQtyBomb = bombypjInfo.getIssuedQty().add(sivIssue);
                bombypjRepository.updateIssuedQty(issuedQtyBomb, userProfile.getCompanyCode(),
                        userProfile.getPlantNo(), bombypjInfo.getOrderNo(), projNo, itemNo);
            }

            /** to catch over deduction in ITEMLOC.PRODN_RESV **/
            ItemLocProjection prodnResv = itemLocRepository.prodnResv(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo);
            PurDetProjection poResv = purDetRepository.poResv(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo);
            BigDecimal resProdnResv = prodnResv.getProdnResv() == null ? BigDecimal.ZERO : prodnResv.getProdnResv();
            BigDecimal resBombypjResv = prodnResv.getResvQty() == null ? BigDecimal.ZERO : prodnResv.getResvQty();
            BigDecimal resPoResv = poResv.getResvQty() == null ? BigDecimal.ZERO : poResv.getResvQty();

            if (resProdnResv.subtract(resPoResv).compareTo(resBombypjResv) != 0) {
                throw new ServerException(String.format("%s Item Resv does not match Bom total Resv ! " +
                        "prodnResv : %s, bombypjResv : %s, Inform MIS", itemNo, resProdnResv, resBombypjResv));
            }
        }
    }

    private void postCoqCombine(UserProfile userProfile, String projNo, String itemNo, Long batchNo,
                                String sivNo, BigDecimal issuedQty, BigDecimal issuedPrice) {

        String docmNo = null;
        BigDecimal docmQty = null;
        Integer subSeq = null;
        Integer recSeq = null;
        String WKType = "WO";
        COQProjection coqRec = coqRepository.coqRecN(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo, projNo, WKType);

        if (StringUtils.isNotBlank(coqRec.getDocmNo())) {
            docmNo = coqRec.getDocmNo();
            recSeq = coqRec.getRecSeq();
            docmQty = coqRec.getDocmQty();
            subSeq = coqRec.getSeqNo();
            String poNoCoqDetSub = null;
            BigDecimal unitPriceCoqDetSub = null;

            Integer maxDetRecSeq = null;
            Integer maxDetSeqNo = null;
            String partNo = null;
            String itemDesc = null;

            ItemBatcLogProjection iBatcLog = itemBatcLogRepository.getPoNoRecdPrice(userProfile.getCompanyCode(), userProfile.getPlantNo(),
                    sivNo, batchNo, itemNo);

            if (iBatcLog != null) {
                poNoCoqDetSub = iBatcLog.getPoNo();
                unitPriceCoqDetSub = iBatcLog.getRecdPrice();
            }

            if (coqRec == null) {
                COQProjection coqDet = coqDetailRepository.coqDet(userProfile.getCompanyCode(), userProfile.getPlantNo(), projNo);
                maxDetRecSeq = coqDet.getRecSeq();
                maxDetSeqNo = coqDet.getSeqNo();
                ItemProjection itemInfo = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo);
                partNo = itemInfo.getPartNo();
                itemDesc = itemInfo.getDescription();
                String coqDiv = null;
                String coqDept = null;
                String reasonCode = null;
                String reasonDesc = null;
                SaleProjection saleProj = saleRepository.saleCoqReasonsDet(userProfile.getCompanyCode(), userProfile.getPlantNo(), projNo);
                coqDiv = saleProj.getCoqDivCode();
                coqDept = saleProj.getCoqDeptCode();
                reasonCode = saleProj.getReasonCode();
                reasonDesc = saleProj.getReasonDesc();
                recSeq = (maxDetRecSeq == null ? 0 : maxDetRecSeq) + 1;

                /** INSERT COQ_DET**/
                COQDetail coqDetail = new COQDetail();
                COQDetailId idCoqDet = new COQDetailId();
                idCoqDet.setCompanyCode(userProfile.getCompanyCode());
                idCoqDet.setPlantNo(userProfile.getPlantNo());
                idCoqDet.setDocmNo(docmNo);
                idCoqDet.setRecSeq(recSeq);
                coqDetail.setSeqNo((maxDetRecSeq == null ? 0 : maxDetRecSeq) + 1);
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
                BigDecimal docmQtyUpdate = docmQty.add(issuedQty);
                coqDetailRepository.updateDocmQty(docmQtyUpdate, userProfile.getCompanyCode(), userProfile.getPlantNo(), subSeq, docmNo);
            }

            COQDetailSub coqDetailSub = new COQDetailSub();
            COQDetailSubId idDetSub = new COQDetailSubId();
            idDetSub.setCompanyCode(userProfile.getCompanyCode());
            idDetSub.setPlantNo(userProfile.getPlantNo());
            idDetSub.setDocmNo(docmNo);
            idDetSub.setDetRecSeq(recSeq);
            idDetSub.setSeqNo((subSeq == null ? 0 : subSeq) + 1);
            coqDetailSub.setSivNo(sivNo);
            coqDetailSub.setQty(issuedQty);
            coqDetailSub.setPoNo(poNoCoqDetSub);
            coqDetailSub.setUnitPrice(unitPriceCoqDetSub == null ? issuedPrice : unitPriceCoqDetSub);
            Date entryDate = new Date(System.currentTimeMillis());
            coqDetailSub.setEntryDate(entryDate);
            coqDetailSub.setId(idDetSub);
            coqDetailSubRepository.save(coqDetailSub);
        }
    }

    private void postItemInv(UserProfile userProfile, String itemNo, String loc, BigDecimal issuedQty,
                             BigDecimal issQtySubtractBomQty, Long batchNo, String sivNo, String projNo,
                             String remarks, SIVDetailDTO detail) {

        /** When the SIV record is created.
         Mainly to update 3 essential tables, REDUCE INVENTORY of ITEMLOC, ITEM, ITEMBATC
         insert into INAUDIT and ITEMBATC_LOG **/
        BigDecimal itemBatchBal = null;
        List<ItemBatchProjection> itemBatcPrj = itemBatcRepository.getItemBatchByBatchNo(userProfile.getCompanyCode(),
                userProfile.getPlantNo(), itemNo, batchNo, loc);
        CompanyProjection coStkLoc = companyRepository.getStockLoc(userProfile.getCompanyCode(), userProfile.getPlantNo());
        String poNoInAudit = null, grnNoInAudit = null;

        /** will delete the itembatc transaction if the qoh for that item in that batch = 0. **/
        for (ItemBatchProjection rec : itemBatcPrj) {
            if (rec.getQoh().compareTo(issuedQty) < 0) {
                throw new ServerException(String.format("Item No : %s Issued Qty > Batch Qty!", itemNo));
            } else {
                itemBatchBal = rec.getQoh().subtract(issuedQty);
                if (itemBatchBal.compareTo(BigDecimal.ZERO) == 0) {
                    itemBatcRepository.deleteItemBatcBal(itemNo, batchNo);
                } else {
                    itemBatcRepository.updateItemBatcBal(itemBatchBal, batchNo);
                }

                /** INSERT ITEMBATC_LOG **/
                ItemBatcLog itemBatcLog = new ItemBatcLog();
                ItemBatcLogId id = new ItemBatcLogId();
                id.setCompanyCode(userProfile.getCompanyCode());
                id.setPlantNo(userProfile.getPlantNo());
                id.setItemNo(itemNo);
                id.setBatchNo(batchNo);
                id.setSivNo(sivNo);
                id.setLoc(loc);
                itemBatcLog.setSivQty(issuedQty);
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
            poNoInAudit = rec.getPoNo();
            grnNoInAudit = rec.getGrnNo();
        }

        if (itemBatchBal == null) {
            throw new NotFoundException(String.format("Batch No : %s for item %s not found!", batchNo, itemNo));
        }

        /** Check the stock level of itemloc, if QOH > OUT Qty,
         update itemloc, insert records into inaudit under
         tran_code = 'IS' : for MSR
         tran_code = 'IM' : for SIV N/M **/

        /** ITEMLOC **/
        ItemLocProjection itemLoc = itemLocRepository.itemLocByItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo, loc);

        BigDecimal itemLocBal = itemLoc.getQoh().subtract(issuedQty);
        BigDecimal prodnResv = (itemLoc.getProdnResv() == null ? BigDecimal.ZERO : itemLoc.getProdnResv())
                .subtract(issuedQty.subtract(issQtySubtractBomQty));
        BigDecimal pickedQty = (itemLoc.getPickedQty() == null ? BigDecimal.ZERO : itemLoc.getPickedQty())
                .subtract(issuedQty.subtract(issQtySubtractBomQty));
        BigDecimal qoh = (itemLoc.getQoh() == null ? BigDecimal.ZERO : itemLoc.getQoh())
                .subtract(issuedQty);
        BigDecimal ytdProd = (itemLoc.getYtdProd() == null ? BigDecimal.ZERO : itemLoc.getYtdProd())
                .add(issuedQty);
        BigDecimal ytdIssue = (itemLoc.getYtdIssue() == null ? BigDecimal.ZERO : itemLoc.getYtdIssue())
                .add(issuedQty);
        Date lastTranDate = new Date(ZonedDateTime.now().toLocalDate().toEpochDay());
        Long locBatchNo = itemLoc.getBatchNo();

        if (itemLocBal == null) {
            throw new NotFoundException(String.format("Item No : %s not found in ITEMLOC!", itemNo));
        } else if (itemLocBal.compareTo(BigDecimal.ZERO) < 0) {
            throw new ServerException(String.format("Item No : %s Issued Qty > Qty-On-Hand!", itemNo));
        } else {
            /** to catch over deduction in ITEMLOC.PRODN_RESV **/
            BombypjProjection bombypjResv = bombypjRepository.bombypjResv(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo);
            PurDetProjection poResv = purDetRepository.poResv(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo);
            BigDecimal resBombypjResv = bombypjResv.getResvQty() == null ? BigDecimal.ZERO : bombypjResv.getResvQty();
            BigDecimal resPoResv = poResv.getResvQty() == null ? BigDecimal.ZERO : poResv.getResvQty();

            if (poResv.getResvQty().subtract(issuedQty.subtract(issQtySubtractBomQty))
                    .compareTo(itemLoc.getProdnResv().subtract(issuedQty.subtract(issQtySubtractBomQty))) != 0) {
                throw new ServerException(String.format("%s Item Resv does not match Bom total Resv ! bombypjResv : %s, " +
                                "prodnResv : %s, outQty : %s, exOutQty : %s, Inform MIS", itemNo, resBombypjResv,
                        itemLoc.getProdnResv(), issuedQty, issQtySubtractBomQty));
            }

            itemLocRepository.updateProdnResvPickedQtyQohYtdProdTydIssueLTranDate(prodnResv, pickedQty, qoh, ytdProd, ytdIssue,
                    lastTranDate, userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo, loc);

            itemLocRepository.updateProdnResv(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo, loc);
        }

        /** ITEM **/
        /** No checking for the QOH as the first pass in ITEMLOC
         will determine whether the QOH is enough
         Thus, just update the required fields to take effect of the MSR Qty **/

        ItemProjection itemInfo = itemRepository.getQohByItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo, loc);

        BigDecimal prodnResvItem = (itemInfo.getProdnResv() == null ? BigDecimal.ZERO : itemInfo.getProdnResv())
                .subtract(issuedQty.subtract(issQtySubtractBomQty));
        BigDecimal pickedQtyItem = (itemInfo.getPickedQty() == null ? BigDecimal.ZERO : itemInfo.getPickedQty())
                .subtract(issuedQty.subtract(issQtySubtractBomQty));
        BigDecimal qohItem = (itemInfo.getQoh() == null ? BigDecimal.ZERO : itemInfo.getQoh())
                .subtract(issuedQty);
        BigDecimal ytdProdItem = (itemInfo.getYtdProd() == null ? BigDecimal.ZERO : itemInfo.getYtdProd())
                .add(issuedQty);
        BigDecimal ytdIssueItem = (itemInfo.getYtdIssue() == null ? BigDecimal.ZERO : itemInfo.getYtdIssue())
                .add(issuedQty);

        itemRepository.updateProdnResvPickedQtyQohYtdProdTydIssueLTranDate(prodnResvItem, pickedQtyItem, qohItem, ytdProdItem, ytdIssueItem,
                lastTranDate, userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo, loc);

        itemRepository.updateProdnResv(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo, loc);

        /** Update Batch No in ITEM and ITEMLOC with lastest Batch No **/
        ItemBatchProjection resMaxBatchNo = itemBatcRepository.getMaxBatchNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo);
        if (resMaxBatchNo.getMaxBatchNo().compareTo(locBatchNo) > 0) {
            itemRepository.updateBatchNo(new BigDecimal(resMaxBatchNo.getMaxBatchNo()), userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo);
            itemLocRepository.updateBatchNo(new BigDecimal(resMaxBatchNo.getMaxBatchNo()), userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo, loc);
        }

        /** INSERT INAUDIT **/
        InAudit inAudit = new InAudit();
        inAudit.setCompanyCode(userProfile.getCompanyCode());
        inAudit.setPlantNo(userProfile.getPlantNo());
        inAudit.setItemNo(itemNo);
        inAudit.setLoc(loc);
        inAudit.setTranDate(lastTranDate);
        String tranTime = FastDateFormat.getInstance("kkmmsssss").format(System.currentTimeMillis());
        inAudit.setTranTime(tranTime);
        inAudit.setTranType("IM");
        inAudit.setDocmNo(sivNo);
        inAudit.setOutQty(issuedQty);
        inAudit.setOrderQty(issuedQty);
        inAudit.setBalQty(itemLocBal);
        inAudit.setProjectNo(projNo);
        inAudit.setCurrencyCode("SGD");
        inAudit.setCurrencyRate(BigDecimal.ONE);
        inAudit.setActualCost(itemLoc.getStdMaterial());
        inAudit.setGrnNo(grnNoInAudit);
        inAudit.setPoNo(poNoInAudit);
        inAudit.setDoNo(null);
        inAudit.setRemarks(remarks);
        inAudit.setStatus(Status.ACTIVE);
        inAudit.setCreatedBy(userProfile.getUsername());
        inAudit.setCreatedAt(ZonedDateTime.now());
        inAudit.setUpdatedBy(userProfile.getUsername());
        inAudit.setUpdatedAt(ZonedDateTime.now());
        inAudit.setItemlocId(itemLoc.getId());
        inAuditRepository.save(inAudit);
    }

    private void postSfc(UserProfile userProfile, String projNo, String itemNo, BigDecimal issuedQty) {
        String subStrItemNo = itemNo.substring(0, 2);
        if (StringUtils.equals(subStrItemNo, "01")) {
            ProductProjection wipTrack = productRepository.wipTrack(userProfile.getCompanyCode(), userProfile.getPlantNo(), projNo);
            String resWipTrack = wipTrack.getWipTracking() == null ? "N" : wipTrack.getWipTracking();
            if (StringUtils.equals(resWipTrack, "Y")) {
                ItemProjection foundPartNo = itemRepository.foundPartNoByItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), itemNo);
                SfcWipProjection getWip = sfcWipRepository.wipCur(projNo, foundPartNo.getPartNo());
                if (getWip == null) {
                    SfcWip sfcWip = new SfcWip();
                    SfcWipId id = new SfcWipId();
                    id.setProjectNoSub(projNo);
                    id.setPcbPartNo(foundPartNo.getPartNo());
                    sfcWip.setPcbQty(issuedQty);
                    sfcWip.setFlowId(null);
                    sfcWip.setStatus("O");
                    sfcWip.setId(id);
                    sfcWipRepository.save(sfcWip);
                } else {
                    BigDecimal pcbQty = (getWip.getPcbQty() == null ? BigDecimal.ZERO : getWip.getPcbQty()).add(issuedQty);
                    sfcWipRepository.updatePcbQty(pcbQty, projNo, foundPartNo.getPartNo());
                }
            }
        }
    }

    private SIVDetailDTO postGrn(UserProfile userProfile, String sivNo, BigDecimal issuedQty, SIVDetailDTO detail) {
        List<GrnDetailProjection> grnDetCurs = grnDetRepository.getGrndetCur(userProfile.getCompanyCode(),
                userProfile.getPlantNo(), detail.getBatchNo(), detail.getItemNo());
        if (grnDetCurs.size() != 0) {
            for (GrnDetailProjection rec : grnDetCurs) {
                detail.setGrnNo(rec.getGrnNo());
                detail.setGrndetSeqNo(rec.getSeqNo());
                if (rec.getIssuedQty().compareTo(BigDecimal.ZERO) == 0) {
                    grnDetRepository.updateSivNoIssuedQty(sivNo, issuedQty, rec.getGrnNo(), rec.getSeqNo());
                }
            }
        }
        return detail;
    }

    private void procPRClosure(UserProfile userProfile, SIVDTO input) {
        PRProjection cReqItem = prRepository.cReqItem(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getDocmNo());
        PRDetailProjection cComplete = prDetailRepository.cComplete(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getDocmNo());
        if (StringUtils.equals(input.getTranType(), "PR")) {
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
        String resWipTrack = wipTrackCur.getWipTracking() == null ? "N" : wipTrackCur.getWipTracking();
        if (StringUtils.equals(resWipTrack, "Y")) {
            if (StringUtils.equals(itemCat.getCategoryCode(), "01")) {
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

        if (StringUtils.equals(itemNo, sivDetail.getItemNo()) && batchNo.equals(sivDetail.getBatchNo()) && StringUtils.equals(loc, sivDetail.getLoc())) {
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

        if (StringUtils.equals(detail.getSubType(), "M")) {
            if (detail.getItemType() == 0) {
                reduceItemInv(userProfile, input, detail);
                postBombypj(userProfile, siv, detail);
                if (StringUtils.equals(input.getTranType(), "PR")) {
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
            if (StringUtils.equals(detail.getSaleType(), "D") && StringUtils.isNotBlank(detail.getItemNo())) {

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
            } else if (StringUtils.equals(detail.getSaleType(), "P")) {
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
            postBombypj(userProfile, siv, detail);
            postCoq(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getProjectNo(), input.getDocmNo(), detail.getItemNo(),
                    detail.getBatchNo(), input.getSivNo(), detail.getIssuedQty(), detail.getIssuedPrice(), input.getSubType());
        }
    }

    private void postBomproj(UserProfile userProfile, String sivNo, String projectNo) {
        BomprojProjection pickedStatus = bomprojRepository.getPickedStatus(userProfile.getCompanyCode(), userProfile.getPlantNo(), projectNo);
        if (pickedStatus != null) {
            if (!StringUtils.equals(pickedStatus.getPickedStatus(), "P") || StringUtils.isBlank(pickedStatus.getPickedStatus())) {
                bomprojRepository.updatePickedStatusSivNo(sivNo, projectNo);
            } else {
                bomprojRepository.updateSivNo(sivNo, projectNo);
            }
        }
    }

    private void procBombProjUpdate(String companyCode, Integer plantNo, String itemNo, String projectNo, BigDecimal issuedQty) {

        BombypjProjection updateIssuedQty = bombypjRepository.bomProjCur(companyCode, plantNo, itemNo, projectNo);
        BigDecimal issuedQtyUpdt = issuedQty.add(updateIssuedQty.getIssuedQty());
        bombypjRepository.updateIssuedQty(issuedQtyUpdt, companyCode, plantNo, projectNo, projectNo, itemNo);
    }

    private void postBombypj(UserProfile userProfile, SIV siv, SIVDetailDTO detail) {

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
            if (!input.getSivType().equalsIgnoreCase("Combine")) {
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
        String poNoInAudit = null, grnNoInAudit = null;
        for (ItemBatchProjection rec : itemBatcPrj) {
            if (rec.getQoh().compareTo(detail.getIssuedQty()) < 0) {
                throw new ServerException(String.format("Item No : %s Issued Qty > Batch Qty!", detail.getItemNo()));
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
            poNoInAudit = rec.getPoNo();
            grnNoInAudit = rec.getGrnNo();
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
        String tranTime = FastDateFormat.getInstance("kkmmssss").format(System.currentTimeMillis());
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
        inAudit.setGrnNo(grnNoInAudit);
        inAudit.setPoNo(poNoInAudit);
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
        if (input.getSivType().equalsIgnoreCase("Combine")) {
            if (StringUtils.isBlank(input.getProjNoA())) {
                throw new ServerException("Project No A Can Not be Blank!");
            }
            if (StringUtils.isBlank(input.getProjNoB())) {
                throw new ServerException("Project No B Can Not be Blank!");
            }
        } else {
            if (StringUtils.isBlank(input.getSivNo())) {
                throw new ServerException("SIV No Can Not be Blank!");
            }
            if (StringUtils.equals(input.getSivType(), "Consigned")) {
                if (StringUtils.isBlank(input.getSivNo())) {
                    throw new ServerException("SIV No Can Not be Blank!");
                }
                if (StringUtils.isBlank(input.getSivNo())) {
                    throw new ServerException("Project No Can Not be Blank!");
                }
            }
        }
    }

    private SIVDetailDTO checkRecValidDetail(UserProfile userProfile, SIVDTO input, SIVDetailDTO detail) {

        if (input.getSubType().equals("N")) {
            if (input.getSivType().equalsIgnoreCase("Combine")) {
                if (StringUtils.isNotBlank(input.getProjNoA())) {
                    if (StringUtils.isBlank(detail.getItemNo())) {
                        throw new ServerException("No SIV Detail, CANNOT create SIV !");
                    }

                    if (detail.getIssuedQty() == null) {
                        qtyResetCombine(detail);
                        throw new ServerException("Issued Qty Can Not be Blank !");
                    }

                    if (detail.getIssuedQty().compareTo(BigDecimal.ZERO) <= 0) {
                        qtyResetCombine(detail);
                        throw new ServerException("Issued Qty MUST be > 0 !");
                    }

                    if (detail.getBomPickQty() == null) {
                        throw new ServerException(String.format("Item No %s not found !", detail.getItemNo()));
                    } else if (detail.getBomPickQty().compareTo(BigDecimal.ZERO) == 0) {
                        throw new ServerException(String.format("Item No %s has no pick qty !", detail.getItemNo()));
                    } else if (detail.getBomPickQty().compareTo(detail.getIssuedQty()) > 0) {
                        qtyResetCombine(detail);
                        throw new ServerException("Partial issue is not allow in this program !");
                    } else if (detail.getIssuedQty().compareTo(detail.getBatchQty()) > 0) {
                        qtyResetCombine(detail);
                        throw new ServerException(String.format("Item No : %s ,Issued > Batch Qty !", detail.getItemNo()));
                    } else if (detail.getIssuedQty().compareTo(detail.getBomQtyA().add(detail.getBomQtyB())
                            .add(detail.getBomQtyC()).add(detail.getBomQtyD()).add(detail.getBomQtyE())) < 0) {
                        qtyResetCombine(detail);
                        throw new ServerException(String.format("Item No : %s ,Issued < Total Project Issue Qty !", detail.getItemNo()));
                    }

                    List<ItemLocProjection> qohCur = itemLocRepository.qohCur(userProfile.getCompanyCode(),
                            userProfile.getPlantNo(), detail.getItemNo(), detail.getLoc());
                    for (ItemLocProjection rec : qohCur) {
                        BigDecimal vQoh = (rec.getQoh() == null ? BigDecimal.ZERO : rec.getQoh());
                        BigDecimal vEOH = rec.getEoh();
                        BigDecimal issuedPrice = (rec.getStdMaterial() == null ? BigDecimal.ZERO : rec.getStdMaterial());
                        if (vQoh.compareTo(detail.getIssuedQty()) < 0) {
                            throw new ServerException(String.format("Item No : %s ,Issued > Qty-On-Hand !", detail.getItemNo()));
                        }

                        if (detail.getIssuedQty().compareTo(detail.getBomPickQty()) > 0 &&
                                vEOH.compareTo(detail.getIssuedQty().subtract(detail.getBomPickQty())) < 0) {
                            qtyResetCombine(detail);
                            throw new ServerException(String.format("Issued Qty of %s CANNOT be > EOH, EOH now is : %s !", detail.getItemNo(), vEOH));
                        } else {
                            detail.setIssuedPrice(issuedPrice);
                        }
                    }

                    Long newBatchNo = null;
                    String newLoc = null;
                    if (StringUtils.isBlank(detail.getBatchDesc())) {
                        detail.setBatchDesc(detail.getBatchLoc());
                        detail.setMessage("Batch No Can Not be Blank !");
                    } else if (!StringUtils.equals(detail.getBatchLoc(), detail.getBatchDesc())) {
                        newBatchNo = Long.valueOf(detail.getBatchNo().toString().substring(0, 8));
                        newLoc = detail.getBatchLoc().substring(9, 11);
                        ItemBatchProjection batchQty = itemBatcRepository.cBatchQoh(userProfile.getCompanyCode(),
                                userProfile.getPlantNo(), detail.getItemNo(), newBatchNo, newLoc);
                        if (batchQty.getQoh().compareTo(detail.getIssuedQty()) < 0) {
                            detail.setBatchDesc(detail.getBatchLoc());
                            throw new ServerException(String.format("Batch No / Loc : %s / %s Batch Qty < Item Issued !", newBatchNo, newLoc));
                        }

                        BigDecimal ttlIssQty = detail.getIssuedQty();

                        if (batchQty.getQoh().compareTo(ttlIssQty) < 0) {
                            detail.setBatchDesc(detail.getBatchLoc());
                            throw new ServerException(String.format("Batch No / Loc : %s / %s Batch Qty < Total Issued !", newBatchNo, newLoc));
                        } else {
                            detail.setBatchLoc(detail.getBatchDesc());
                            detail.setBatchNo(newBatchNo);
                            detail.setLoc(newLoc);
                            detail.setBatchQty(batchQty.getQoh());
                        }
                    }

                    if (detail.getIssuedQty().compareTo(detail.getBomPickQty()) != 0) {
                        detail.setExtraQty(detail.getIssuedQty().subtract(detail.getBomPickQty()));
                        extraAloc(detail);
                    }
                }
            } else {
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
                    if (qohCur.getQoh().compareTo(detail.getIssuedQty()) < 0) {
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
        }
        return detail;
    }

    private void extraAloc(SIVDetailDTO detail) {

        if ((detail.getExtraQty() == null ? BigDecimal.ZERO : detail.getExtraQty()).compareTo(BigDecimal.ZERO) == 0) {
            detail.setIssuedQtyA(detail.getBomQtyA() == null ? BigDecimal.ZERO : detail.getBomQtyA());
            detail.setIssuedQtyB(detail.getBomQtyB() == null ? BigDecimal.ZERO : detail.getBomQtyB());
            detail.setIssuedQtyC(detail.getBomQtyC() == null ? BigDecimal.ZERO : detail.getBomQtyC());
            detail.setIssuedQtyD(detail.getBomQtyD() == null ? BigDecimal.ZERO : detail.getBomQtyD());
            detail.setIssuedQtyE(detail.getBomQtyE() == null ? BigDecimal.ZERO : detail.getBomQtyE());
        }

        BigDecimal ttlExt = detail.getExtraQty() == null ? BigDecimal.ZERO : detail.getExtraQty();
        BigDecimal ttlQty = detail.getBomQtyA().add(detail.getBomQtyB()).add(detail.getBomQtyC())
                .add(detail.getBomQtyD()).add(detail.getBomQtyE());

        BigDecimal extA = (ttlExt.multiply(detail.getBomQtyA().divide(ttlQty, 2, RoundingMode.FLOOR)));
        BigDecimal extB = (ttlExt.multiply(detail.getBomQtyB().divide(ttlQty, 2, RoundingMode.FLOOR)));
        BigDecimal extC = (ttlExt.multiply(detail.getBomQtyC().divide(ttlQty, 2, RoundingMode.FLOOR)));
        BigDecimal extD = (ttlExt.multiply(detail.getBomQtyD().divide(ttlQty, 2, RoundingMode.FLOOR)));
        BigDecimal extE = (ttlExt.multiply(detail.getBomQtyE().divide(ttlQty, 2, RoundingMode.FLOOR)));

        ttlExt = ttlExt.subtract(extA).subtract(extB).subtract(extC).subtract(extD).subtract(extE);
        BigDecimal topQty = BigDecimal.ZERO;

        if (ttlExt.compareTo(BigDecimal.ZERO) < 0) {
            throw new ServerException("Error in Extra distribution!");
        } else {
            BigDecimal bomQtyA = detail.getBomQtyA() == null ? BigDecimal.ZERO : detail.getBomQtyA();
            BigDecimal bomQtyB = detail.getBomQtyB() == null ? BigDecimal.ZERO : detail.getBomQtyB();
            BigDecimal bomQtyC = detail.getBomQtyC() == null ? BigDecimal.ZERO : detail.getBomQtyC();
            BigDecimal bomQtyD = detail.getBomQtyD() == null ? BigDecimal.ZERO : detail.getBomQtyD();
            BigDecimal bomQtyE = detail.getBomQtyE() == null ? BigDecimal.ZERO : detail.getBomQtyE();
            BombypjProjection greatestQty = sivDetailRepository.findGreatestQty(bomQtyA, bomQtyB, bomQtyC, bomQtyD, bomQtyE);
            topQty = greatestQty.getGreatestQty();
            if (bomQtyA.compareTo(topQty) == 0) {
                detail.setIssuedQtyA(bomQtyA.add(extA).add(ttlExt));
            } else {
                detail.setIssuedQtyA(bomQtyA.add(extA));
            }
            if (bomQtyB.compareTo(topQty) == 0) {
                detail.setIssuedQtyB(bomQtyB.add(extB).add(ttlExt));
            } else {
                detail.setIssuedQtyB(bomQtyB.add(extB));
            }
            if (bomQtyC.compareTo(topQty) == 0) {
                detail.setIssuedQtyC(bomQtyC.add(extC).add(ttlExt));
            } else {
                detail.setIssuedQtyC(bomQtyC.add(extC));
            }
            if (bomQtyD.compareTo(topQty) == 0) {
                detail.setIssuedQtyD(bomQtyD.add(extD).add(ttlExt));
            } else {
                detail.setIssuedQtyD(bomQtyD.add(extD));
            }
            if (bomQtyE.compareTo(topQty) == 0) {
                detail.setIssuedQtyE(bomQtyE.add(extE).add(ttlExt));
            } else {
                detail.setIssuedQtyE(bomQtyE.add(extE));
            }
        }
    }

    private SIVDetailDTO qtyResetCombine(SIVDetailDTO detail) {
        detail.setIssuedQty(detail.getBomPickQty() == null ? BigDecimal.ZERO : detail.getBomPickQty());
        detail.setExtraQty(BigDecimal.ZERO);
        detail.setIssuedQtyA(detail.getBomQtyA() == null ? BigDecimal.ZERO : detail.getBomQtyA());
        detail.setIssuedQtyB(detail.getBomQtyB() == null ? BigDecimal.ZERO : detail.getBomQtyB());
        detail.setIssuedQtyC(detail.getBomQtyC() == null ? BigDecimal.ZERO : detail.getBomQtyC());
        detail.setIssuedQtyD(detail.getBomQtyD() == null ? BigDecimal.ZERO : detail.getBomQtyD());
        detail.setIssuedQtyE(detail.getBomQtyE() == null ? BigDecimal.ZERO : detail.getBomQtyE());
        return detail;
    }

    private void checkRecValid(SIVDTO input) {
        if (StringUtils.equals(input.getSubType(), "N")) {
            if (StringUtils.equals(input.getSivType(), "Combine")) {
                if (StringUtils.isBlank(input.getProjNoA()) && StringUtils.isBlank(input.getProjNoB())) {
                    throw new ServerException("At least two Project No are needed !");
                }
            }
            if (StringUtils.equals(input.getSivType(), "Consigned")) {
                checkRecValidProjectNo(input.getProjectNo());
            }
        } else {
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
                //throw new ServerException(String.format("Project Type of %s is unknown!", projectNo));
                throw new ServerException("Invalid Project No!");
            }
        } else {
            throw new ServerException("Project No. Can Not be Blank!");
        }
    }

    private void checkIfSivNoExist(UserProfile userProfile, SIVDTO input) {

        Optional<SIV> sivOptional = sivRepository.findSIVByCompanyCodeAndPlantNoAndSivNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getSivNo());
        if (StringUtils.equals(input.getSubType(), "N") && StringUtils.isNotBlank(input.getSivNoA())) {
            if (sivOptional.isPresent()) {
                throw new DuplicateException("SIV No. exists ! List of SIV NOs are updated. Please add the record again to confirm the change.");
            }
        } else {
            String type = "SIV";
            String subType;
            if (StringUtils.equals(input.getSubType(), "M")) {
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
    }

    private void sivPostSaving(UserProfile userProfile, SIV input, SIVDTO sivDto) {

        if (StringUtils.equals(input.getSubType(), "N")) {
            if (!StringUtils.equals(sivDto.getSivType(), "Combine")) {
                BomprojProjection pickedStatus = bomprojRepository.getPickedStatus(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getProjectNo());
                if (pickedStatus != null) {
                    if (!StringUtils.equals(pickedStatus.getPickedStatus(), "P") || StringUtils.isBlank(pickedStatus.getPickedStatus())) {
                        bomprojRepository.updatePickedStatusSivNo(input.getSivNo(), input.getProjectNo());
                    } else {
                        bomprojRepository.updateSivNo(input.getSivNo(), input.getProjectNo());
                    }
                }
            }
        }

        String type = "SIV";
        String subType = null;
        DocmNoProjection updateDocmNo = null;
        if (StringUtils.equals(input.getSubType(), "M")) {
            subType = input.getSubType();
            updateDocmNo = docmNoRepository.getLastGeneratedNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), type, subType);
        } else {
            if (StringUtils.equals(sivDto.getSivType(), "Combine")) {
                if (StringUtils.isNotBlank(sivDto.getSivNoA()) || StringUtils.isNotBlank(sivDto.getSivNoB())) {
                    subType = sivDto.getProjNoA().substring(0, 1);
                    updateDocmNo = docmNoRepository.getSivGeneratedNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), type, subType);
                }
            } else {
                subType = input.getProjectNo().substring(0, 1);
                updateDocmNo = docmNoRepository.getLastGeneratedNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), type, subType);
            }
        }
        docmNoRepository.updateLastGeneratedNo(updateDocmNo.getDocmNo(), userProfile.getCompanyCode(), userProfile.getPlantNo(), subType, type);
    }

    private void populateAfterSaving(SIVDTO input, SIV saved) {
        if (!StringUtils.equals(input.getSivType(), "Combine")) {
            input.setId(saved.getId());
            input.setVersion(saved.getVersion());
        }
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
        if (StringUtils.equals(input.getSubType(), "M")) {
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
        if (StringUtils.equals(input.getTranType(), "OTHER")) {
            dto = checkValidOtherItemNo(userProfile, input);
        } else if (StringUtils.equals(input.getTranType(), "DS") || StringUtils.equals(input.getTranType(), "WD")) {
            dto = checkRecValidItemNoDSWD(userProfile, input);
        } else if (StringUtils.equals(input.getTranType(), "WK") || StringUtils.equals(input.getTranType(), "PR")) {
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
    public byte[] generatedLabelSIV(SIVDTO input) {

        try {
            Integer seqNo = 0;
            for (SIVDetailDTO dto : input.getSivDetails()) {
                seqNo = dto.getSeqNo();
            }
            UserProfile userProfile = UserProfileContext.getUserProfile();
            InputStream resource = this.getClass().getResourceAsStream("/reports/siv_label.jrxml");
            // Compile the Jasper report from .jrxml to .jasper
            JasperReport jasperReport = JasperCompileManager.compileReport(resource);
            Map<String, Object> param = new HashMap<>();
            List<JasperPrint> jasperPrintList = new ArrayList<JasperPrint>();
            // Fetching the inventoryuser from the data source.
            Connection source = dataSource.getConnection();
            // Adding the additional parameters to the pdf.
            if (StringUtils.isNotBlank(input.getSivNoA())) {
                param.put("SIV_NO", input.getSivNoA());
                param.put("COMPANY_CODE", userProfile.getCompanyCode());
                param.put("PLANT_NO", userProfile.getPlantNo());
                param.put("SEQ_NO", seqNo);
                // Filling the report with the data and additional parameters information.
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, param, source);
                jasperPrintList.add(jasperPrint);
            }
            if (StringUtils.isNotBlank(input.getSivNoB())) {
                param.put("SIV_NO", input.getSivNoB());
                param.put("COMPANY_CODE", userProfile.getCompanyCode());
                param.put("PLANT_NO", userProfile.getPlantNo());
                param.put("SEQ_NO", seqNo);
                // Filling the report with the data and additional parameters information.
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, param, source);
                jasperPrintList.add(jasperPrint);
            }
            if (StringUtils.isNotBlank(input.getSivNoC())) {
                param.put("SIV_NO", input.getSivNoC());
                param.put("COMPANY_CODE", userProfile.getCompanyCode());
                param.put("PLANT_NO", userProfile.getPlantNo());
                param.put("SEQ_NO", seqNo);
                // Filling the report with the data and additional parameters information.
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, param, source);
                jasperPrintList.add(jasperPrint);
            }
            if (StringUtils.isNotBlank(input.getSivNoD())) {
                param.put("SIV_NO", input.getSivNoD());
                param.put("COMPANY_CODE", userProfile.getCompanyCode());
                param.put("PLANT_NO", userProfile.getPlantNo());
                param.put("SEQ_NO", seqNo);
                // Filling the report with the data and additional parameters information.
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, param, source);
                jasperPrintList.add(jasperPrint);
            }
            if (StringUtils.isNotBlank(input.getSivNoE())) {
                param.put("SIV_NO", input.getSivNoE());
                param.put("COMPANY_CODE", userProfile.getCompanyCode());
                param.put("PLANT_NO", userProfile.getPlantNo());
                param.put("SEQ_NO", seqNo);
                // Filling the report with the data and additional parameters information.
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, param, source);
                jasperPrintList.add(jasperPrint);
            }
            if (StringUtils.isBlank(input.getSivNoA()) && StringUtils.isBlank(input.getSivNoB()) && StringUtils.isBlank(input.getSivNoC())
                    && StringUtils.isBlank(input.getSivNoD()) && StringUtils.isBlank(input.getSivNoE())) {
                param.put("SIV_NO", input.getSivNoA());
                param.put("COMPANY_CODE", userProfile.getCompanyCode());
                param.put("PLANT_NO", userProfile.getPlantNo());
                param.put("SEQ_NO", seqNo);
                // Filling the report with the data and additional parameters information.
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, param, source);
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
            byte[] jasperReports = pdfOutputStream.toByteArray();
            return jasperReports;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServerException("Error Generated Label SIV");
        }
    }

    @Override
    public byte[] generatedReportSIV(SIVDTO input) {

        try {
            UserProfile userProfile = UserProfileContext.getUserProfile();
            // Fetching the .jrxml file from the resources folder.
            InputStream resourceSubReport = this.getClass().getResourceAsStream("/reports/siv_report_header_INR00009.jrxml");
            InputStream mainReport = this.getClass().getResourceAsStream("/reports/siv_report_detail_INR00009.jrxml");
            // Compile the Jasper report from .jrxml to .jasper
            JasperReport jasperSubReport = JasperCompileManager.compileReport(resourceSubReport);
            JasperReport jasperMainReport = JasperCompileManager.compileReport(mainReport);
            Map<String, Object> param = new HashMap<>();
            List<JasperPrint> jasperPrintList = new ArrayList<JasperPrint>();
            // Fetching the inventoryuser from the data source.
            Connection source = dataSource.getConnection();
            // Adding the additional parameters to the pdf.
            if (StringUtils.isNotBlank(input.getSivNoA())) {
                param.put("SIV_NO_START", input.getSivNoA());
                param.put("SIV_NO_END", input.getSivNoA());
                param.put("COMPANY_CODE", userProfile.getCompanyCode());
                param.put("PLANT_NO", userProfile.getPlantNo());
                param.put("SUB_REPORT", jasperSubReport);
                // Filling the report with the data and additional parameters information.
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperMainReport, param, source);
                jasperPrintList.add(jasperPrint);
            }
            if (StringUtils.isNotBlank(input.getSivNoB())) {
                param.put("SIV_NO_START", input.getSivNoB());
                param.put("SIV_NO_END", input.getSivNoB());
                param.put("COMPANY_CODE", userProfile.getCompanyCode());
                param.put("PLANT_NO", userProfile.getPlantNo());
                param.put("SUB_REPORT", jasperSubReport);
                // Filling the report with the data and additional parameters information.
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperMainReport, param, source);
                jasperPrintList.add(jasperPrint);
            }
            if (StringUtils.isNotBlank(input.getSivNoC())) {
                param.put("SIV_NO_START", input.getSivNoC());
                param.put("SIV_NO_END", input.getSivNoC());
                param.put("COMPANY_CODE", userProfile.getCompanyCode());
                param.put("PLANT_NO", userProfile.getPlantNo());
                param.put("SUB_REPORT", jasperSubReport);
                // Filling the report with the data and additional parameters information.
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperMainReport, param, source);
                jasperPrintList.add(jasperPrint);
            }
            if (StringUtils.isNotBlank(input.getSivNoD())) {
                param.put("SIV_NO_START", input.getSivNoD());
                param.put("SIV_NO_END", input.getSivNoD());
                param.put("COMPANY_CODE", userProfile.getCompanyCode());
                param.put("PLANT_NO", userProfile.getPlantNo());
                param.put("SUB_REPORT", jasperSubReport);
                // Filling the report with the data and additional parameters information.
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperMainReport, param, source);
                jasperPrintList.add(jasperPrint);
            }
            if (StringUtils.isNotBlank(input.getSivNoE())) {
                param.put("SIV_NO_START", input.getSivNoE());
                param.put("SIV_NO_END", input.getSivNoE());
                param.put("COMPANY_CODE", userProfile.getCompanyCode());
                param.put("PLANT_NO", userProfile.getPlantNo());
                param.put("SUB_REPORT", jasperSubReport);
                // Filling the report with the data and additional parameters information.
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperMainReport, param, source);
                jasperPrintList.add(jasperPrint);
            }
            if (StringUtils.isBlank(input.getSivNoA()) && StringUtils.isBlank(input.getSivNoB()) && StringUtils.isBlank(input.getSivNoC())
                    && StringUtils.isBlank(input.getSivNoD()) && StringUtils.isBlank(input.getSivNoE())) {
                param.put("SIV_NO_START", input.getSivNo());
                param.put("SIV_NO_END", input.getSivNo());
                param.put("COMPANY_CODE", userProfile.getCompanyCode());
                param.put("PLANT_NO", userProfile.getPlantNo());
                param.put("SUB_REPORT", jasperSubReport);
                // Filling the report with the data and additional parameters information.
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
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServerException("Error Generated Report SIV");
        }
    }

    @Override
    public SIVDetailDTO checkNextItem(SIVDTO input) {
        UserProfile userProfile = UserProfileContext.getUserProfile();
        SIVDetailDTO detailDTO = SIVDetailDTO.builder().build();
        if (input.getSivDetails().size() != 0) {
            for (SIVDetailDTO rec : input.getSivDetails()) {
                if (StringUtils.equals(input.getSubType(), "M")) {
                    //check project no 1
                    if (rec.getItemType() == 0) {
                        if (StringUtils.equals(input.getTranType(), "PR")) {
                            if (StringUtils.isBlank(rec.getProjectNo1())) {
                                throw new ServerException("Project No 1 cannot be empty for PR ref type !");
                            } else {
                                SaleProjection saleTypeCur = saleRepository.saleTypeCur(userProfile.getCompanyCode(),
                                        userProfile.getPlantNo(), rec.getProjectNo1());
                                if (saleTypeCur == null) {
                                    throw new ServerException(String.format("%s, is not a valid entry !", rec.getProjectNo1()));
                                }
                            }
                        } else if (!StringUtils.equals(input.getTranType(), "PR")) {
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

    @Override
    public List<SIVDTO> populateSIVCombineDetails(SIVDTO input) {
        UserProfile userProfile = UserProfileContext.getUserProfile();
        List<SIVDTO> list = new ArrayList<>();
        SIVDTO dto = projValidate(userProfile, input);
        list.add(dto);
        return list;
    }

    private SIVDTO projValidate(UserProfile userProfile, SIVDTO input) {

        String projectNoA = input.getProjNoA();
        String projectNoB = input.getProjNoB();
        String projectNoC = input.getProjNoC();
        String projectNoD = input.getProjNoD();
        String projectNoE = input.getProjNoE();
        BombypjProjection cBombypj = null;
        String sivNoA = null, sivNoB = null, sivNoC = null, sivNoD = null, sivNoE = null;
        Set<SIVDetailDTO> listDet = null;

        if (StringUtils.isNotBlank(projectNoA)) {
            cBombypj = bombypjRepository.projValidate(userProfile.getCompanyCode(), userProfile.getPlantNo(), projectNoA);
            if (cBombypj == null) {
                throw new ServerException(String.format("%s is either an invaild project or no outstanding/enough picked qty!", projectNoA));
            }
        }
        if (StringUtils.isNotBlank(projectNoB)) {
            cBombypj = bombypjRepository.projValidate(userProfile.getCompanyCode(), userProfile.getPlantNo(), projectNoB);
            if (cBombypj == null) {
                if (StringUtils.isNotBlank(projectNoB)) {
                    throw new ServerException(String.format("%s is either an invaild project or no outstanding/enough picked qty!", projectNoB));
                }
            }
        }
        if (StringUtils.isNotBlank(projectNoC)) {
            cBombypj = bombypjRepository.projValidate(userProfile.getCompanyCode(), userProfile.getPlantNo(), projectNoC);
            if (cBombypj == null) {
                if (StringUtils.isNotBlank(projectNoC)) {
                    throw new ServerException(String.format("%s is either an invaild project or no outstanding/enough picked qty!", projectNoC));
                }
            }
        }
        if (StringUtils.isNotBlank(projectNoD)) {
            cBombypj = bombypjRepository.projValidate(userProfile.getCompanyCode(), userProfile.getPlantNo(), projectNoD);
            if (cBombypj == null) {
                if (StringUtils.isNotBlank(projectNoD)) {
                    throw new ServerException(String.format("%s is either an invaild project or no outstanding/enough picked qty!", projectNoD));
                }
            }
        }
        if (StringUtils.isNotBlank(projectNoE)) {
            cBombypj = bombypjRepository.projValidate(userProfile.getCompanyCode(), userProfile.getPlantNo(), projectNoE);
            if (cBombypj == null) {
                if (StringUtils.isNotBlank(projectNoE)) {
                    throw new ServerException(String.format("%s is either an invaild project or no outstanding/enough picked qty!", projectNoE));
                }
            }
        }

        if (StringUtils.isNotBlank(projectNoA)) {
            if (StringUtils.isNotBlank(projectNoB) && projectNoB != projectNoA && !projectNoA.substring(0, 1).equals(projectNoB.substring(0, 1))) {
                throw new ServerException(String.format("%s is not same project type with %s", projectNoB, projectNoA));
            }
            if (StringUtils.isNotBlank(projectNoC) && projectNoC != projectNoA && !projectNoA.substring(0, 1).equals(projectNoC.substring(0, 1))) {
                throw new ServerException(String.format("%s is not same project type with %s", projectNoC, projectNoA));
            }
            if (StringUtils.isNotBlank(projectNoC) && projectNoC != projectNoB && !projectNoB.substring(0, 1).equals(projectNoC.substring(0, 1))) {
                throw new ServerException(String.format("%s is not same project type with %s", projectNoC, projectNoB));
            }
            if (StringUtils.isNotBlank(projectNoD) && projectNoD != projectNoA && !projectNoA.substring(0, 1).equals(projectNoD.substring(0, 1))) {
                throw new ServerException(String.format("%s is not same project type with %s", projectNoD, projectNoA));
            }
            if (StringUtils.isNotBlank(projectNoD) && projectNoD != projectNoC && !projectNoC.substring(0, 1).equals(projectNoD.substring(0, 1))) {
                throw new ServerException(String.format("%s is not same project type with %s", projectNoD, projectNoC));
            }
            if (StringUtils.isNotBlank(projectNoD) && projectNoD != projectNoB && !projectNoB.substring(0, 1).equals(projectNoD.substring(0, 1))) {
                throw new ServerException(String.format("%s is not same project type with %s", projectNoD, projectNoB));
            }
            if (StringUtils.isNotBlank(projectNoE) && projectNoE != projectNoA && !projectNoA.substring(0, 1).equals(projectNoE.substring(0, 1))) {
                throw new ServerException(String.format("%s is not same project type with %s", projectNoE, projectNoA));
            }
            if (StringUtils.isNotBlank(projectNoE) && projectNoE != projectNoD && !projectNoD.substring(0, 1).equals(projectNoE.substring(0, 1))) {
                throw new ServerException(String.format("%s is not same project type with %s", projectNoE, projectNoD));
            }
            if (StringUtils.isNotBlank(projectNoE) && projectNoE != projectNoB && !projectNoB.substring(0, 1).equals(projectNoE.substring(0, 1))) {
                throw new ServerException(String.format("%s is not same project type with %s", projectNoE, projectNoB));
            }
            if (StringUtils.isNotBlank(projectNoE) && projectNoE != projectNoC && !projectNoC.substring(0, 1).equals(projectNoE.substring(0, 1))) {
                throw new ServerException(String.format("%s is not same project type with %s", projectNoE, projectNoC));
            }
        }

        if (StringUtils.isNotBlank(projectNoA)) {

            if (StringUtils.isNotBlank(projectNoB)) {
                if (StringUtils.equals(projectNoA, projectNoB)) {
                    throw new ServerException(String.format("Duplicate project, %s , found!", projectNoB));
                }
            }
            if (StringUtils.isNotBlank(projectNoC)) {
                if (StringUtils.equals(projectNoC, projectNoB)) {
                    throw new ServerException(String.format("Duplicate project, %s , found!", projectNoC));
                } else if (StringUtils.equals(projectNoC, projectNoA)) {
                    throw new ServerException(String.format("Duplicate project, %s , found!", projectNoC));
                }
            }
            if (StringUtils.isNotBlank(projectNoD)) {
                if (StringUtils.equals(projectNoD, projectNoC)) {
                    throw new ServerException(String.format("Duplicate project, %s , found!", projectNoD));
                } else if (StringUtils.equals(projectNoD, projectNoB)) {
                    throw new ServerException(String.format("Duplicate project, %s , found!", projectNoD));
                } else if (StringUtils.equals(projectNoD, projectNoA)) {
                    throw new ServerException(String.format("Duplicate project, %s , found!", projectNoD));
                }
            }
            if (StringUtils.isNotBlank(projectNoE)) {
                if (StringUtils.equals(projectNoE, projectNoD)) {
                    throw new ServerException(String.format("Duplicate project, %s , found!", projectNoE));
                } else if (StringUtils.equals(projectNoE, projectNoC)) {
                    throw new ServerException(String.format("Duplicate project, %s , found!", projectNoE));
                } else if (StringUtils.equals(projectNoE, projectNoB)) {
                    throw new ServerException(String.format("Duplicate project, %s , found!", projectNoE));
                } else if (StringUtils.equals(projectNoE, projectNoA)) {
                    throw new ServerException(String.format("Duplicate project, %s , found!", projectNoE));
                }
            }

            if (StringUtils.isNotBlank(projectNoB)) {
                projectNoB = projectNoB;
            } else if (StringUtils.isNotBlank(projectNoC)) {
                projectNoB = projectNoC;
            } else if (StringUtils.isNotBlank(projectNoD)) {
                projectNoB = projectNoD;
            } else if (StringUtils.isNotBlank(projectNoE)) {
                projectNoB = projectNoE;
            }

            BombypjProjection countA = bombypjRepository.bomCountAltrnt(userProfile.getCompanyCode(), userProfile.getPlantNo(), projectNoA);
            BombypjProjection countB = bombypjRepository.bomCountAltrnt(userProfile.getCompanyCode(), userProfile.getPlantNo(), projectNoB);
            List<BombypjProjection> alternateA = bombypjRepository.bomAltrnt(userProfile.getCompanyCode(), userProfile.getPlantNo(), projectNoA);
            List<BombypjProjection> alternateB = bombypjRepository.bomAltrnt(userProfile.getCompanyCode(), userProfile.getPlantNo(), projectNoB);
            BombypjProjection commonItem = bombypjRepository.bomComp(userProfile.getCompanyCode(), userProfile.getPlantNo(), projectNoA, projectNoB);
            if (countA.getCountAlternate() > countB.getCountAlternate()) {
                for (BombypjProjection recA : alternateA) {
                    for (BombypjProjection recB : alternateB) {
                        if (!StringUtils.equals(recA.getAlternate(), recB.getAlternate())) {
                            if (commonItem.getCountAlternate() == 0) {
                                throw new ServerException(String.format("%s has no common item with other project!", projectNoB));
                            }
                        }
                    }
                }
            } else {
                for (BombypjProjection recB : alternateA) {
                    for (BombypjProjection recA : alternateB) {
                        if (!StringUtils.equals(recA.getAlternate(), recB.getAlternate())) {
                            if (commonItem.getCountAlternate() == 0) {
                                throw new ServerException(String.format("%s has no common item with other project!", projectNoA));
                            }
                        }
                    }
                }
            }

            if (StringUtils.isBlank(projectNoA)) {
                if (StringUtils.isNotBlank(projectNoB)) {
                    projectNoA = projectNoB;
                    projectNoB = null;
                } else if (StringUtils.isNotBlank(projectNoC)) {
                    projectNoA = projectNoC;
                    projectNoC = null;
                } else if (StringUtils.isNotBlank(projectNoD)) {
                    projectNoA = projectNoD;
                    projectNoD = null;
                } else if (StringUtils.isNotBlank(projectNoE)) {
                    projectNoA = projectNoE;
                    projectNoE = null;
                }
            }

            String type = "SIV";
            String subType = null;
            DocmNoProjection sivGeneratedNo = null;
            Integer docmNo = 1;

            if (StringUtils.isNotBlank(input.getProjNoA())) {
                subType = input.getProjNoA().substring(0, 1);
                sivGeneratedNo = docmNoRepository.getSivGeneratedNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), type, subType);
                docmNo = sivGeneratedNo.getDocmNo() + 1;
                sivNoA = (sivGeneratedNo.getGeneratedNo().substring(0, 12) + docmNo + sivGeneratedNo.getPostfix());
            } else {
                projectNoA = null;
                input.setProjNoA("");
            }

            if (StringUtils.isNotBlank(input.getProjNoB())) {
                subType = input.getProjNoB().substring(0, 1);
                sivGeneratedNo = docmNoRepository.getSivGeneratedNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), type, subType);
                if (docmNo != null) {
                    docmNo = docmNo + 1;
                    sivNoB = (sivGeneratedNo.getGeneratedNo().substring(0, 12) + docmNo + sivGeneratedNo.getPostfix());
                }
            } else {
                projectNoB = null;
                input.setProjNoB("");
            }

            if (StringUtils.isNotBlank(input.getProjNoC())) {
                subType = input.getProjNoC().substring(0, 1);
                sivGeneratedNo = docmNoRepository.getSivGeneratedNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), type, subType);
                if (docmNo != null) {
                    docmNo = docmNo + 1;
                    sivNoC = (sivGeneratedNo.getGeneratedNo().substring(0, 12) + docmNo + sivGeneratedNo.getPostfix());
                }
            } else {
                projectNoC = null;
                input.setProjNoC("");
            }

            if (StringUtils.isNotBlank(input.getProjNoD())) {
                subType = input.getProjNoD().substring(0, 1);
                sivGeneratedNo = docmNoRepository.getSivGeneratedNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), type, subType);
                if (docmNo != null) {
                    docmNo = docmNo + 1;
                    sivNoD = (sivGeneratedNo.getGeneratedNo().substring(0, 12) + docmNo + sivGeneratedNo.getPostfix());
                }
            } else {
                projectNoD = null;
                input.setProjNoD("");
            }

            if (StringUtils.isNotBlank(input.getProjNoE())) {
                subType = input.getProjNoE().substring(0, 1);
                sivGeneratedNo = docmNoRepository.getSivGeneratedNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), type, subType);
                if (docmNo != null) {
                    docmNo = docmNo + 1;
                    sivNoE = (sivGeneratedNo.getGeneratedNo().substring(0, 12) + docmNo + sivGeneratedNo.getPostfix());
                }
            } else {
                projectNoE = null;
                input.setProjNoE("");
            }

            listDet = populateSivDetCombine(userProfile, input);

        }

        return SIVDTO.builder().projNoA(projectNoA).projNoB(projectNoB)
                .projNoC(projectNoC).projNoD(projectNoD)
                .projNoE(projectNoE)
                .sivNoA(sivNoA)
                .sivNoB(sivNoB)
                .sivNoC(sivNoC)
                .sivNoD(sivNoD)
                .sivNoE(sivNoE)
                .sivDetails(listDet).build();

    }

    private Set<SIVDetailDTO> populateSivDetCombine(UserProfile userProfile, SIVDTO input) {

        Set<SIVDetailDTO> sivDetails = new HashSet<>();
        List<BombypjProjection> populateDetComb = bombypjRepository.sivCur(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getProjNoA(),
                input.getProjNoB(), input.getProjNoC(), input.getProjNoD(), input.getProjNoE());

        Integer seqNo = 1;
        for (BombypjProjection rec : populateDetComb) {
            ItemBatchProjection checkBatcQoh = itemBatcRepository.cBatch(userProfile.getCompanyCode(), userProfile.getPlantNo(),
                    rec.getAlternate(), rec.getTtlSivQty());
            if (checkBatcQoh != null) {
                sivDetails.add(SIVDetailDTO.builder()
                        .seqNo(seqNo++)
                        .itemType(0)
                        .itemNo(rec.getAlternate())
                        .partNo(rec.getPartNo())
                        .loc(rec.getLoc())
                        .uom(rec.getUom())
                        /*.batchNo(recIBatc.getBatchNo())
                        .batchLoc(recIBatc.getBatchNo() + "/" + rec.getLoc())
                        .batchQty(recIBatc.getQoh())*/
                        .issuedQty(rec.getTtlSivQty())
                        .extraQty(BigDecimal.ZERO)
                        .issuedPrice(rec.getStdMaterial())
                        .issuedQtyA(rec.getPaQty())
                        .issuedQtyB(rec.getPbQty())
                        .issuedQtyC(rec.getPcQty())
                        .issuedQtyD(rec.getPdQty())
                        .issuedQtyE(rec.getPeQty())
                        .bomPickQty(rec.getTtlSivQty())
                        .bomQtyA(rec.getPaQty())
                        .bomQtyB(rec.getPbQty())
                        .bomQtyC(rec.getPcQty())
                        .bomQtyD(rec.getPdQty())
                        .bomQtyE(rec.getPeQty())
                        .remarks(rec.getPartNo()).build());
            }

        }

        return sivDetails;
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
        if (StringUtils.equals(input.getTranType(), "PR") || StringUtils.equals(input.getTranType(), "WK")) {
            checkValidPRNo(userProfile, input);
            prRmk = prRmkProj.getRemarks();
            saleType = "P";
        } else if (StringUtils.equals(input.getTranType(), "DS") || StringUtils.equals(input.getTranType(), "WD")) {
            checkValidOrderNo(userProfile, input);
            saleType = "D";
        } else if (StringUtils.equals(input.getTranType(), "OTHER")) {
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

                        if (StringUtils.equals(input.getTranType(), "DS") || StringUtils.equals(input.getTranType(), "WD")) {
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

                        if (StringUtils.equals(input.getTranType(), "DS") || StringUtils.equals(input.getTranType(), "WD")) {
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

                        if (StringUtils.equals(input.getTranType(), "DS") || StringUtils.equals(input.getTranType(), "WD")) {
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
                if (StringUtils.equals(input.getTranType(), "DS") || StringUtils.equals(input.getTranType(), "WD")) {
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
        } else if (StringUtils.equals(cOrder.getOpenClose(), "C") || StringUtils.equals(cOrder.getOpenClose(), "V")) {
            throw new NotFoundException("" + input.getDocmNo() + " is CLOSED/VOIDED !");
        }
    }

    private void checkValidPRNo(UserProfile userProfile, SIVDTO input) {
        PRProjection cPr = prRepository.cPr(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getDocmNo());
        if (StringUtils.isBlank(cPr.getDocmNo())) {
            throw new NotFoundException("" + input.getDocmNo() + " is Invalid or not found!");
        } else if (!StringUtils.equals(cPr.getStatus(), "A")) {
            throw new NotFoundException("" + input.getDocmNo() + " is yet to be approved!");
        } else if (StringUtils.equals(cPr.getStatus(), "C") || StringUtils.equals(cPr.getStatus(), "V")) {
            throw new NotFoundException("" + input.getDocmNo() + " is CLOSED/VOIDED !");
        }
    }

    @Override
    public SIVDTO getDefaultValueSIV(String subType, String sivType) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        String entryTime = FastDateFormat.getInstance("kkmmssss").format(System.currentTimeMillis());
        String currencyCode = "USD";
        String statuz = "O";
        // subType (N : for Entry, M : for Manual)
        if (StringUtils.equals(sivType, "Entry") || StringUtils.equals(sivType, "Manual")) {
            currencyCode = "USD";
            statuz = "O";
        } else {
            currencyCode = "SGD";
            statuz = "O";
        }
        return SIVDTO.builder().currencyCode(currencyCode).currencyRate(BigDecimal.valueOf(1.0000))
                .entryUser(userProfile.getUsername()).subType(subType).statuz(statuz)
                .entryDate(new Timestamp(System.currentTimeMillis())).entryTime(entryTime).build();
    }

    @Override
    public List<SIVDetailDTO> populateBatchList(String subType, String projectNo, String itemNo, String sivType) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        List<SIVDetailDTO> populateDetails = populateDetails(userProfile, projectNo);
        List<SIVDetailDTO> listPopulateBatch = new ArrayList<>();

        int countSeqNo = 1;
        int countGrnDetSeqNo = 1;

        if (StringUtils.equals(subType, "N") && StringUtils.equals(sivType, "Entry")) {
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
        } else if (StringUtils.equals(subType, "N") && StringUtils.equals(sivType, "Combine")) {
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
                    iBatch.setBatchNo(ib.getBatchNo());
                    listPopulateBatch.add(iBatch);
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
        if (StringUtils.equals(input.getSubType(), "N")) {
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
        } else if (StringUtils.equals(input.getSubType(), "M")) {
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

        return dto;
    }

    private void qtyReset(SIVDetailDTO dto) {
        dto.setIssuedQty(dto.getBomPickQty());
        dto.setExtraQty(BigDecimal.ZERO);
    }

    private List<SIVDetailDTO> populateDetails(UserProfile userProfile, String projectNo) {

        List<SIVDetailDTO> list = new ArrayList<>();

        /** SIV NO **/
        SaleDetailProjection sdetProjection = saleDetailRepository.getProjectType(userProfile.getCompanyCode(), userProfile.getPlantNo(), projectNo);
        String sivType = sdetProjection.getProductType();
        if (StringUtils.isBlank(sivType)) {
            throw new ServerException(String.format("Project Type of %s is unknown!", projectNo));
        }

        List<BombypjProjection> bombypjs = bombypjRepository.getDataByProjectNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), projectNo);

        int countSeqNo = 1;
        int countGrnDetSeqNo = 1;

        // bombypj loop
        /** SIV DETAIL **/
        for (BombypjProjection bombRec : bombypjs) {
            List<ItemBatchProjection> batchProjection = itemBatcRepository.getBatchNoByItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), bombRec.getAlternate());
            BigDecimal projPickQty = bombRec.getPickedQty();

            /** when there is short qty and QOH available
             Allocate when local stk if available
             Else assign to foreign stk if available **/
            BigDecimal projShortQtyL = BigDecimal.ZERO; // Short Qty for Local assignment
            BigDecimal projShortQtyF = BigDecimal.ZERO; // Short Qty for foreign assignment
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

            /** individaul record will be created for PICK and SHORT qty
             no mixing of PICK and SHORT qty under one batch
             one batch can have PICK and SHORT in two separate records
             SHORT qty record will have null BATCH NO, BATCH LIST and BATCH_LOC
             when there is short qty, can only offer stk loc if avail and 2nd loc **/
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
                    } else { // projPickQty <= batRec.qoh
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
                    } else { // projShortQtyL <= batRec.qoh
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
                if (projShortQtyF.compareTo(BigDecimal.ZERO) > 0 && batchQty.compareTo(BigDecimal.ZERO) > 0 && !StringUtils.equals(batRec.getLoc(), companyStockLoc.getStockLoc())) {
                    if (projShortQtyF.compareTo(batchQty) > 0) {
                        pickQty = batchQty;
                        shortQty = batchQty;
                        projShortQtyF = projShortQtyF.subtract(batchQty);
                        batchQty = BigDecimal.ZERO;
                    } else { // projShortQtyF <= batRec.qoh
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

                if (projPickQty.compareTo(BigDecimal.ZERO) == 0 && projShortQtyL.compareTo(BigDecimal.ZERO) == 0
                        && projShortQtyF.compareTo(BigDecimal.ZERO) == 0) {
                    break;
                }
            } // end batch loop

            if (projPickQty.compareTo(BigDecimal.ZERO) != 0) {
                throw new ServerException("Error in Batch QOH distribution!");
            }
        } // end bombypj loop

        return list;
    }
}