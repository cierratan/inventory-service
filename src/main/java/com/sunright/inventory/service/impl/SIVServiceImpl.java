package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.ItemDTO;
import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.lov.LocationDTO;
import com.sunright.inventory.dto.search.Filter;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.dto.siv.SIVDTO;
import com.sunright.inventory.dto.siv.SIVDetailDTO;
import com.sunright.inventory.entity.ItemLocProjection;
import com.sunright.inventory.entity.ItemProjection;
import com.sunright.inventory.entity.bombypj.BombypjProjection;
import com.sunright.inventory.entity.company.CompanyProjection;
import com.sunright.inventory.entity.docmno.DocmNoProjection;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.itembatc.ItemBatchProjection;
import com.sunright.inventory.entity.lov.LocationProjection;
import com.sunright.inventory.entity.sale.SaleDetailProjection;
import com.sunright.inventory.entity.siv.SIV;
import com.sunright.inventory.entity.siv.SIVDetail;
import com.sunright.inventory.exception.DuplicateException;
import com.sunright.inventory.exception.NotFoundException;
import com.sunright.inventory.exception.ServerException;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.*;
import com.sunright.inventory.repository.lov.LocationRepository;
import com.sunright.inventory.service.SIVService;
import com.sunright.inventory.util.QueryGenerator;
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
                BeanUtils.copyProperties(detail, sivDetail);
                sivDetail.setCompanyCode(userProfile.getCompanyCode());
                sivDetail.setPlantNo(userProfile.getPlantNo());
                sivDetail.setSiv(saved);

                checkRecValidDetail(userProfile, input, detail);
                sivDetailRepository.save(sivDetail);
            }
        }

        postSaving(userProfile, input);
        populateAfterSaving(input, saved);

        return input;
    }

    private void checkRecNull(SIVDTO input) {
        if (input.getSivNo() == null) {
            throw new ServerException(String.format("SIV No: %s Can Not be Blank!", input.getSivNo()));
        }
    }

    private void checkRecValidDetail(UserProfile userProfile, SIVDTO input, SIVDetailDTO detail) {
        if (detail.getItemNo() == null) {
            throw new ServerException("Item No Can Not be Blank!");
        } else {
            ItemProjection itemProjection = itemRepository.getDataByProjectNoAndItemNo(userProfile.getCompanyCode(),
                    userProfile.getPlantNo(), input.getProjectNo(), detail.getItemNo());
            if (itemProjection.getPartNo() == null) {
                throw new ServerException("Item No " + detail.getItemNo() + " not found in " + input.getProjectNo() + "!");
            } else if (!Objects.equals(itemProjection.getSource(), "B") || !Objects.equals(itemProjection.getSource(), "C")) {
                throw new ServerException("Invalid Item Source, Item is not Buy or Consigned !");
            } else if (detail.getItemType() == null) {
                detail.setItemType(0);
                detail.setPartNo(itemProjection.getPartNo());
                detail.setLoc(itemProjection.getLoc());
                detail.setUom(itemProjection.getUom());
                detail.setRemarks(itemProjection.getPartNo());
                detail.setIssuedQty(itemProjection.getPickedQty());
                detail.setExtraQty(BigDecimal.ZERO);
                detail.setIssuedPrice(itemProjection.getStdMaterial());
            }
        }
    }

    private void checkRecValid(SIVDTO input) {
        if (input.getProjectNo() != null && !Objects.equals(input.getProjectNo(), "")) {
            ItemProjection itemProjection = itemRepository.getItemNoByProjectNo(input.getProjectNo());
            String projectNo = itemProjection.getItemNo();
            if (projectNo == null) {
                throw new ServerException("Invalid Project No!");
            }
        } else {
            throw new ServerException("Project No. Can Not be Blank!");
        }
    }

    private void checkIfSivNoExist(UserProfile userProfile, SIVDTO input) {
        Optional<SIV> sivOptional = sivRepository.findSIVByCompanyCodeAndPlantNoAndSivNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getSivNo());
        String type = "SIV";
        String subType = input.getProjectNo().substring(0, 1);
        DocmNoProjection docmNo = docmNoRepository.getLastGeneratedNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), type, subType);
        if (docmNo.getGeneratedNo() == null) {
            throw new NotFoundException("Not found in DOCM_NO table for type SIV !");
        }

        if (sivOptional.isPresent()) {
            throw new DuplicateException("SIV No. exists ! SIV NO change from " + sivOptional.get().getSivNo() +
                    " to " + docmNo.getGeneratedNo() + " Please add the record again to confirm the change.");
        }
    }

    private void postSaving(UserProfile userProfile, SIVDTO input) {
        String type = "SIV";
        String subType = input.getProjectNo().substring(0, 1);
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
        String subType = input.getProjectNo().substring(0, 1);
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
    public List<LocationDTO> getLocAndDesc() {
        List<LocationDTO> list = new ArrayList<>();
        List<LocationProjection> loc = locationRepository.getLocAndDescription();
        for (LocationProjection locProjection : loc) {
            LocationDTO dto = new LocationDTO();
            dto.setLoc(locProjection.getLoc());
            dto.setDescription(locProjection.getDescription());
            list.add(dto);
        }
        return list;
    }

    @Override
    public List<ItemDTO> getItemNo() {
        UserProfile userProfile = UserProfileContext.getUserProfile();
        List<ItemProjection> getItems = itemRepository.getItemNoByCompanyCodeAndPlantNo(userProfile.getCompanyCode(), userProfile.getPlantNo());
        List<ItemDTO> list = new ArrayList<>();
        for (ItemProjection itemProjection : getItems) {
            list.add(ItemDTO.builder().itemNo(itemProjection.getItemNo()).build());
        }
        return list;
    }

    @Override
    public List<SIVDetailDTO> checkNextItem(SIVDTO input) {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        List<SIVDetailDTO> sivDetailDTOList;

        if (input.getProjectNo() != null && !Objects.equals(input.getProjectNo(), "")) {
            checkRecValid(input);
            sivDetailDTOList = populateDetails(userProfile, input);
        } else {
            throw new ServerException("Project No. Can Not be Blank!");
        }

        return sivDetailDTOList;
    }

    @Override
    public SIVDetailDTO checkValidIssuedQty(SIVDetailDTO input) {
        UserProfile userProfile = UserProfileContext.getUserProfile();
        checkRecValidIssuedQty(userProfile, input);
        return SIVDetailDTO.builder().build();
    }

    private List<SIVDetailDTO> populateBatchList(UserProfile userProfile, List<SIVDetailDTO> sivDetailDTOList) {

        List<SIVDetailDTO> listPopulateBatch = new ArrayList<>();

        for (SIVDetailDTO dto : sivDetailDTOList) {
            if (dto.getItemNo() != null) {
                List<ItemBatchProjection> itemBatch = itemBatcRepository.getItemBatchByItemNo(userProfile.getCompanyCode(),
                        userProfile.getPlantNo(), dto.getItemNo());
                List<ItemBatchProjection> itemBatchFLoc = itemBatcRepository.getItemBatchFLocByItemNo(userProfile.getCompanyCode(),
                        userProfile.getPlantNo(), dto.getItemNo());

                if (itemBatch == null) {
                    throw new ServerException("No Such Item : " + dto.getItemNo());
                }

                if (itemBatchFLoc == null) {
                    throw new ServerException("No Such Item : " + dto.getItemNo());
                }
                if (dto.getBomShortQtyF().compareTo(BigDecimal.ZERO) > 0) {
                    for (ItemBatchProjection ib : itemBatch) {
                        dto.setBatchDesc(ib.getBatchDesc());
                        dto.setBatchNoLoc(ib.getBatchNoLoc());
                    }
                } else {
                    for (ItemBatchProjection ib : itemBatchFLoc) {
                        dto.setBatchDesc(ib.getBatchDesc());
                        dto.setBatchNoLoc(ib.getBatchNoLoc());
                    }
                }

                if (dto.getIssuedQty().compareTo(BigDecimal.ZERO) != 0) {
                    if (dto.getIssuedQty().compareTo(BigDecimal.ZERO) > dto.getBomPickQty().compareTo(BigDecimal.ZERO)) {
                        dto.setExtraQty(dto.getIssuedQty().subtract(dto.getBomPickQty()));
                    }
                    if (dto.getBatchLoc() != null) {
                        checkRecValidIssuedQty(userProfile, dto);
                    }
                }
                listPopulateBatch.add(dto);
            }
        }

        sivDetailDTOList.addAll(listPopulateBatch);

        return sivDetailDTOList;
    }

    private SIVDetailDTO checkRecValidIssuedQty(UserProfile userProfile, SIVDetailDTO dto) {

        if (dto.getIssuedQty().compareTo(BigDecimal.ZERO) > 0) {
            if (dto.getBomPickQty() == null) {
                throw new NotFoundException("Item No " + dto.getItemNo() + " not found !");
            } else if (dto.getBomPickQty().compareTo(BigDecimal.ZERO) == 0) {
                throw new ServerException("Item No " + dto.getItemNo() + " has no pick qty !");
            }

            if (dto.getIssuedQty().compareTo(BigDecimal.ZERO) > dto.getBatchQty().compareTo(BigDecimal.ZERO)) {
                qtyReset(dto);
                throw new ServerException("Item No " + dto.getItemNo() + " Issued > Batch Qty !");
            }

            ItemLocProjection qohCur = itemLocRepository.getQohCur(userProfile.getCompanyCode(), userProfile.getPlantNo(), dto.getItemNo(), dto.getLoc());
            if (qohCur.getQoh().compareTo(BigDecimal.ZERO) < dto.getIssuedQty().compareTo(BigDecimal.ZERO)) {
                qtyReset(dto);
                throw new ServerException("Item No " + dto.getItemNo() + " Issued > Qty-On Hand !");
            }

            if (dto.getIssuedQty().subtract(dto.getBomPickQty()).compareTo(BigDecimal.ZERO) > 0
                    && qohCur.getEoh().compareTo(BigDecimal.ZERO) < (dto.getIssuedQty().subtract(dto.getBomPickQty()).compareTo(BigDecimal.ZERO))) {
                qtyReset(dto);
                throw new ServerException("Issued Qty of " + dto.getItemNo() + " Cannot be > EOH, EOH now is : " + qohCur.getEoh() + " !");
            }
        }
        return dto;
    }

    private void qtyReset(SIVDetailDTO dto) {
        dto.setIssuedQty(dto.getBomPickQty());
        dto.setExtraQty(BigDecimal.ZERO);
    }

    private List<SIVDetailDTO> populateDetails(UserProfile userProfile, SIVDTO input) {

        List<SIVDetailDTO> list = new ArrayList<>();

        SaleDetailProjection sdetProjection = saleDetailRepository.getProjectType(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getProjectNo());
        String sivType = sdetProjection.getProductType();
        if (sivType == null) {
            throw new ServerException("Project Type of " + input.getProjectNo() + " is unknown!");
        }

        List<BombypjProjection> bombProjections = bombypjRepository.getDataByProjectNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getProjectNo());
        // bombypj loop
        for (BombypjProjection bombRec : bombProjections) {
            ItemLocProjection itemAvailQohL = itemLocRepository.getItemAvailQohL(userProfile.getCompanyCode(), userProfile.getPlantNo(), bombRec.getAlternate());
            ItemLocProjection ItemAvailQohF = itemLocRepository.getItemAvailQohF(userProfile.getCompanyCode(), userProfile.getPlantNo(), bombRec.getAlternate());
            List<ItemBatchProjection> batchProjection = itemBatcRepository.getBatchNoByItemNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), bombRec.getAlternate());
            BigDecimal projShortQtyL = BigDecimal.valueOf(0);
            BigDecimal projShortQtyF = BigDecimal.valueOf(0);
            BigDecimal projPickQty = bombRec.getPickedQty();
            if (bombRec.getShortQty().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal projShortQty = bombRec.getShortQty();
                BigDecimal itemAvailQty = BigDecimal.valueOf(0);
                if (itemAvailQohL.getAvailQty().compareTo(BigDecimal.ZERO) > 0) {
                    if (itemAvailQohL.getAvailQty().intValue() > bombRec.getShortQty().intValue()) {
                        projShortQtyL = bombRec.getShortQty();
                        projShortQty = BigDecimal.valueOf(0);
                    } else {
                        projShortQtyL = itemAvailQohL.getAvailQty();
                        projShortQty = BigDecimal.valueOf(projShortQty.intValue() - itemAvailQty.intValue());
                    }

                    if (projShortQty.compareTo(BigDecimal.ZERO) > 0) {
                        if (ItemAvailQohF != null) {
                            itemAvailQty = ItemAvailQohF.getAvailQty();
                            if (itemAvailQty.compareTo(BigDecimal.ZERO) > 0) {
                                if (itemAvailQty.intValue() > bombRec.getShortQty().intValue()) {
                                    projShortQtyF = bombRec.getShortQty();
                                    projShortQty = BigDecimal.valueOf(0);
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
                if (projPickQty.compareTo(BigDecimal.ZERO) > 0) {
                    if (projPickQty.intValue() > batchQty.intValue()) {
                        pickQty = batchQty;
                        projPickQty = projPickQty.subtract(batchQty);
                        batchQty = BigDecimal.valueOf(0);
                    } else {
                        pickQty = projPickQty;
                        batchQty = batchQty.subtract(projPickQty);
                        projPickQty = BigDecimal.valueOf(0);
                    }

                    list.add(SIVDetailDTO.builder()
                            .itemType(0)
                            .itemNo(bombRec.getAlternate())
                            .partNo(bombRec.getPartNo())
                            .loc(bombRec.getLoc())
                            .uom(bombRec.getUom())
                            .batchNo(batRec.getBatchNo())
                            .batchLoc(batRec.getBatchNo() + "/" + bombRec.getLoc())
                            .issuedQty(pickQty)
                            .extraQty(BigDecimal.ZERO)
                            .issuedPrice(bombRec.getStdMaterial())
                            .bomPickQty(pickQty)
                            .bomShortQtyL(BigDecimal.ZERO)
                            .bomShortQtyF(BigDecimal.ZERO)
                            .batchQty(batRec.getQoh())
                            .remarks(bombRec.getPartNo())
                            .build());
                }

                if (projShortQtyL.compareTo(BigDecimal.ZERO) > 0 && batchQty.compareTo(BigDecimal.ZERO) > 0) {
                    if (projShortQtyL.intValue() > batchQty.intValue()) {
                        pickQty = batchQty;
                        shortQty = batchQty;
                        projShortQtyL = projShortQtyF.subtract(batchQty);
                        batchQty = BigDecimal.valueOf(0);
                    } else {
                        pickQty = projShortQtyL;
                        shortQty = projShortQtyL;
                        batchQty = batchQty.subtract(projShortQtyL);
                        projShortQtyL = BigDecimal.valueOf(0);
                    }

                    list.add(SIVDetailDTO.builder()
                            .itemType(0)
                            .itemNo(bombRec.getAlternate())
                            .partNo(bombRec.getPartNo())
                            .loc(bombRec.getLoc())
                            .uom(bombRec.getUom())
                            .batchNo(null)
                            .batchLoc(null)
                            .issuedQty(pickQty)
                            .extraQty(BigDecimal.ZERO)
                            .issuedPrice(bombRec.getStdMaterial())
                            .bomPickQty(pickQty)
                            .bomShortQtyL(shortQty)
                            .bomShortQtyF(BigDecimal.ZERO)
                            .batchQty(batRec.getQoh())
                            .remarks(bombRec.getPartNo())
                            .build());
                }


                CompanyProjection companyStockLoc = companyRepository.getStockLoc(userProfile.getCompanyCode(), userProfile.getPlantNo()); // get stock loc company
                if (projShortQtyF.compareTo(BigDecimal.ZERO) > 0 && batchQty.compareTo(BigDecimal.ZERO) > 0 && !batRec.getLoc().equals(companyStockLoc.getStockLoc())) {
                    if (projShortQtyF.intValue() > batchQty.intValue()) {
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
                            .itemType(0)
                            .itemNo(bombRec.getAlternate())
                            .partNo(bombRec.getPartNo())
                            .loc(bombRec.getLoc())
                            .uom(bombRec.getUom())
                            .batchNo(null)
                            .batchLoc(null)
                            .issuedQty(pickQty)
                            .extraQty(BigDecimal.ZERO)
                            .issuedPrice(bombRec.getStdMaterial())
                            .bomPickQty(pickQty)
                            .bomShortQtyL(BigDecimal.ZERO)
                            .bomShortQtyF(shortQty)
                            .batchQty(batRec.getQoh())
                            .remarks(bombRec.getPartNo())
                            .build());
                }

            }

            if (projPickQty.compareTo(BigDecimal.ZERO) != 0) {
                throw new ServerException("Error in Batch QOH distribution!");
            }
        }

        populateBatchList(userProfile, list);

        return list;
    }
}