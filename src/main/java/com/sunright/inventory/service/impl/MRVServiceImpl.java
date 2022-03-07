package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.mrv.MrvDTO;
import com.sunright.inventory.dto.mrv.MrvDetailDTO;
import com.sunright.inventory.dto.search.Filter;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.entity.bombypj.BombypjProjection;
import com.sunright.inventory.entity.docmno.DocmNoProjection;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.item.ItemProjection;
import com.sunright.inventory.entity.itemloc.ItemLoc;
import com.sunright.inventory.entity.mrv.MRV;
import com.sunright.inventory.entity.mrv.MRVDetail;
import com.sunright.inventory.entity.sfcwip.SfcWipProjection;
import com.sunright.inventory.entity.sfcwip.SfcWipTranProjection;
import com.sunright.inventory.entity.siv.SIV;
import com.sunright.inventory.entity.siv.SIVDetail;
import com.sunright.inventory.entity.siv.SIVDetailSub;
import com.sunright.inventory.exception.NotFoundException;
import com.sunright.inventory.exception.ServerException;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.*;
import com.sunright.inventory.service.MRVService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;

@Service
public class MRVServiceImpl implements MRVService {

    @Autowired
    private DocmNoRepository docmNoRepository;

    @Autowired
    private SIVRepository sivRepository;

    @Autowired
    private MRVRepository mrvRepository;

    @Autowired
    private MRVDetailRepository mrvDetailRepository;

    @Autowired
    private BombypjRepository bombypjRepository;

    @Autowired
    private ItemLocRepository itemLocRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private SfcWipRepository sfcWipRepository;

    @Autowired
    private SfcWipTranRepository sfcWipTranRepository;

    @Autowired
    private QueryGenerator queryGenerator;

    @Override
    public DocmValueDTO getGeneratedNo() {
        DocmNoProjection docmNo = docmNoRepository.getLastGeneratedNo(
                UserProfileContext.getUserProfile().getCompanyCode(),
                UserProfileContext.getUserProfile().getPlantNo(),
                "MRV",
                "N");

        return DocmValueDTO.builder()
                .generatedNo(docmNo.getGeneratedNo())
                .docmNo(docmNo.getDocmNo())
                .build();
    }

    @Override
    public MrvDetailDTO findSivAndPopulateMRVDetails(String sivNo) {
        Optional<SIV> found = sivRepository.findSIVByCompanyCodeAndPlantNoAndSivNo(UserProfileContext.getUserProfile().getCompanyCode(), UserProfileContext.getUserProfile().getPlantNo(), sivNo);

        if(!found.isPresent() || CollectionUtils.isEmpty(found.get().getSivDetails())) {
            throw new NotFoundException(String.format("SIV No: %s is not found", sivNo));
        }

        SIV siv = found.get();
        SIVDetail sivDetail = new ArrayList<>(siv.getSivDetails()).get(0);

        MrvDetailDTO mrvDetail = MrvDetailDTO.builder()
                .itemType(sivDetail.getItemType())
                .itemNo(sivDetail.getItemNo())
                .partNo(sivDetail.getPartNo())
                .loc(sivDetail.getLoc())
                .uom(sivDetail.getUom())
                .batchNo(sivDetail.getBatchNo())
                .recdQty(sivDetail.getIssuedQty())
                .recdPrice(sivDetail.getIssuedPrice())
                .remarks(sivDetail.getRemarks())
                .issuedQty(sivDetail.getIssuedQty())
                .labelQty(sivDetail.getIssuedQty())
                .sivNo(sivDetail.getSivNo())
                .tranType(siv.getTranType())
                .docmNo(siv.getDocmNo())
                .replace("N")
                .msrStatus("N")
                .build();

        if(StringUtils.equals("N", siv.getSubType())) {
            mrvDetail.setSaleType("P");
        } else if(StringUtils.equals("M", siv.getSubType())) {

            switch (siv.getTranType()) {
                case "PR":
                    mrvDetail.setSaleType("P");

                    if(!CollectionUtils.isEmpty(siv.getSivDetails())) {
                        SIVDetail detail = new ArrayList<>(siv.getSivDetails()).get(0);

                        if(!CollectionUtils.isEmpty(detail.getSivDetailSub())) {
                            SIVDetailSub sivDetailSub = new ArrayList<>(detail.getSivDetailSub()).get(0);

                            if(sivDetailSub != null) {
                                mrvDetail.setProjectNo(sivDetailSub.getDocmNo());
                            }
                        }
                    }
                    break;
                case "DS":
                    mrvDetail.setSaleType("D");
                    mrvDetail.setProjectNo(siv.getDocmNo());
                    break;
                case "WD":
                    mrvDetail.setSaleType("D");
                    break;
            }
        }


        return mrvDetail;
    }

    @Override
    @Transactional
    public MrvDTO createMrv(MrvDTO input) {
        UserProfile userProfile = UserProfileContext.getUserProfile();

        MRV mrv = new MRV();
        BeanUtils.copyProperties(input, mrv);

        mrv.setCompanyCode(userProfile.getCompanyCode());
        mrv.setPlantNo(userProfile.getPlantNo());
        mrv.setCurrencyCode("SGD"); // FIXME: this is set at global level
        mrv.setCurrencyRate(new BigDecimal(1));
        mrv.setStatus(Status.ACTIVE);
        mrv.setCreatedBy(userProfile.getUsername());
        mrv.setCreatedAt(ZonedDateTime.now());
        mrv.setUpdatedBy(userProfile.getUsername());
        mrv.setUpdatedAt(ZonedDateTime.now());

        MRV saved = mrvRepository.save(mrv);
        saved.setId(saved.getId());
        saved.setVersion(saved.getVersion());

        if(!CollectionUtils.isEmpty(input.getMrvDetails())) {
            for (MrvDetailDTO detail : input.getMrvDetails()) {
                MRVDetail mrvDetail = new MRVDetail();
                BeanUtils.copyProperties(detail, mrvDetail);

                mrvDetail.setCompanyCode(userProfile.getCompanyCode());
                mrvDetail.setPlantNo(userProfile.getPlantNo());
                mrvDetail.setMrvNo(saved.getMrvNo());
                mrvDetail.setMrv(saved);

                mrvDetailRepository.save(mrvDetail);

                mrvDetailPostSaving(mrvDetail);
            }
        }

        mrvPostSaving(userProfile);

        return input;
    }

    @Override
    public SearchResult<MrvDTO> searchBy(SearchRequest searchRequest) {
        Specification<MRV> specs = where(queryGenerator.createDefaultSpec());

        if(!CollectionUtils.isEmpty(searchRequest.getFilters())) {
            for (Filter filter : searchRequest.getFilters()) {
                specs = specs.and(queryGenerator.createSpecification(filter));
            }
        }

        Page<MRV> pgMRV = mrvRepository.findAll(specs, queryGenerator.constructPageable(searchRequest));

        SearchResult<MrvDTO> result = new SearchResult<>();
        result.setTotalRows(pgMRV.getTotalElements());
        result.setTotalPages(pgMRV.getTotalPages());
        result.setCurrentPageNumber(pgMRV.getPageable().getPageNumber());
        result.setCurrentPageSize(pgMRV.getNumberOfElements());
        result.setRows(pgMRV.getContent().stream().map(mrv -> convertToMrvDTO(mrv)).collect(Collectors.toList()));

        return result;
    }

    @Override
    public MrvDTO findBy(Long id) {
        return convertToMrvDTO(checkIfRecordExist(id));
    }

    private void mrvPostSaving(UserProfile userProfile) {
        String type = "MRV";
        String subType = "N";

        DocmNoProjection docmNo = docmNoRepository.getLastGeneratedNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), type, subType);
        docmNoRepository.updateLastGeneratedNo(docmNo.getDocmNo(), userProfile.getCompanyCode(), userProfile.getPlantNo(), subType, type);
    }

    private void mrvDetailPostSaving(MRVDetail mrvDetail) {
        UserProfile userProfile = UserProfileContext.getUserProfile();

        BombypjProjection bombypj = bombypjRepository.getBombypjInfo(userProfile.getCompanyCode(),
                userProfile.getPlantNo(), mrvDetail.getProjectNo(), mrvDetail.getItemNo());

        BigDecimal projReturn = BigDecimal.ZERO;

        BigDecimal mrvQty = bombypj.getMrvQty() == null ? BigDecimal.ZERO : bombypj.getMrvQty();
        BigDecimal issuedQty = bombypj.getIssuedQty();
        BigDecimal recdQty = mrvDetail.getRecdQty();

        if(issuedQty.compareTo(recdQty) >= 0) {
            projReturn = recdQty;
            mrvQty = mrvQty.add(recdQty);
            recdQty = BigDecimal.ZERO;
        } else {
            projReturn = issuedQty;
            mrvQty = mrvQty.add(issuedQty);
            recdQty = recdQty.subtract(issuedQty);
        }

        // check if MSR selected
        List<ItemLoc> itemLocs = itemLocRepository.findByCompanyCodeAndPlantNoAndItemNoAndLoc(userProfile.getCompanyCode(),
                userProfile.getPlantNo(), mrvDetail.getItemNo(), mrvDetail.getLoc());

        if(!CollectionUtils.isEmpty(itemLocs)) {
            ItemLoc itemLoc = itemLocs.get(0);

            BigDecimal mrvResv = bombypj.getMrvResv();
            BigDecimal itemLocMrvResv = itemLoc.getMrvResv();
            BigDecimal itemLocProdnResv = itemLoc.getProdnResv();
            BigDecimal itemLocPickedQty = itemLoc.getPickedQty();
            BigDecimal shortQty = bombypj.getShortQty() == null ? BigDecimal.ZERO : bombypj.getShortQty();
            BigDecimal resvQty = bombypj.getResvQty() == null ? BigDecimal.ZERO : bombypj.getResvQty();
            BigDecimal pickedQty = bombypj.getPickedQty() == null ? BigDecimal.ZERO : bombypj.getPickedQty();
            if(StringUtils.equals("Y", mrvDetail.getMsrStatus())) {
                mrvResv = mrvResv.add(projReturn);
                itemLocMrvResv = itemLocMrvResv.add(projReturn);

                if(StringUtils.equals("N", mrvDetail.getReplace())) {
                    shortQty = shortQty.add(projReturn);
                    resvQty = resvQty.add(projReturn);
                    itemLocProdnResv = itemLocProdnResv.add(projReturn);
                }
            }

            ItemProjection item = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), mrvDetail.getItemNo());
            BigDecimal itemMrvResv = item.getMrvResv();

            if(item != null) {
                BigDecimal currentItemEoh = item.getQoh().add(item.getOrderQty()).subtract(item.getProdnResv()).subtract(item.getRpcResv()).subtract(item.getMrvResv());

                if(StringUtils.equals("Y", mrvDetail.getReplace())) {
                    if(currentItemEoh.compareTo(projReturn) >= 0) {
                        pickedQty = pickedQty.add(projReturn);
                        itemLocPickedQty = itemLocPickedQty.add(projReturn);
                    } else {
                        throw new ServerException(String.format("Not enough item %s for replacement", mrvDetail.getItemNo()));
                    }

                    resvQty = resvQty.add(projReturn);
                    itemLocProdnResv = itemLocProdnResv.add(projReturn);
                }

                if(StringUtils.equals("C", item.getSource())) {
                    shortQty = BigDecimal.ZERO;
                }

                if(shortQty.compareTo(BigDecimal.ZERO) < 0) {
                    shortQty = BigDecimal.ZERO;
                }

                bombypjRepository.updateResvQtyAndPickedQtyAndShortQtyAndMrvQtyAndMrvResv(resvQty, pickedQty, shortQty, mrvQty, mrvResv,
                        mrvDetail.getCompanyCode(), mrvDetail.getPlantNo(), bombypj.getComponent(), bombypj.getOrderNo(), bombypj.getAlternate(), bombypj.getProjectNo(), bombypj.getAssemblyNo());

                // update spare
                if(StringUtils.equals("Y", mrvDetail.getMsrStatus())) {
                    itemLocMrvResv = itemLocMrvResv.add(recdQty);
                }

                if(itemLocMrvResv.compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal finalMrvResv = itemMrvResv.add(itemLocMrvResv);
                    itemRepository.updateMrvResv(finalMrvResv, UserProfileContext.getUserProfile().getCompanyCode(), UserProfileContext.getUserProfile().getPlantNo(), mrvDetail.getItemNo());

                    itemLocRepository.updateMrvResv(finalMrvResv, UserProfileContext.getUserProfile().getCompanyCode(), UserProfileContext.getUserProfile().getPlantNo(), mrvDetail.getItemNo(), mrvDetail.getLoc());
                }

                if(itemLocPickedQty.compareTo(BigDecimal.ZERO) != 0
                | itemLocProdnResv.compareTo(BigDecimal.ZERO) != 0
                | itemLocMrvResv.compareTo(BigDecimal.ZERO) != 0) {
                    ItemProjection itemFound = itemRepository.itemInfo(userProfile.getCompanyCode(), userProfile.getPlantNo(), mrvDetail.getItemNo());

                    BigDecimal finalPickedQty = itemFound.getPickedQty().add(itemLocPickedQty);
                    BigDecimal finalProdnResv = itemFound.getProdnResv().add(itemLocProdnResv);
                    BigDecimal finalMrvResv = itemFound.getMrvResv().add(itemLocMrvResv);

                    itemRepository.updatePickedQtyMrvResvProdnResv(finalPickedQty, finalMrvResv, finalProdnResv, mrvDetail.getCompanyCode(), mrvDetail.getPlantNo(), mrvDetail.getItemNo());
                    itemLocRepository.updatePickedQtyMrvResvProdnResv(finalPickedQty, finalMrvResv, finalProdnResv, mrvDetail.getCompanyCode(), mrvDetail.getPlantNo(), mrvDetail.getItemNo(), mrvDetail.getLoc());
                }
            }
        }

        // update sfc wip
        if(StringUtils.startsWith(mrvDetail.getItemNo(), "01-") && mrvDetail.getProjectNo() != null) {
            SfcWipProjection sfcWip = sfcWipRepository.wipCurWithStatusCheck(mrvDetail.getProjectNo(), mrvDetail.getPartNo());
            if(sfcWip != null) {
                BigDecimal pcbQty = sfcWip.getPcbQty();
                BigDecimal sfcRtnQty = mrvDetail.getRecdQty();

                if(pcbQty.compareTo(sfcRtnQty) >= 0) {
                    pcbQty = pcbQty.subtract(sfcRtnQty);

                    sfcWipRepository.updatePcbQty(pcbQty, mrvDetail.getProjectNo(), mrvDetail.getPartNo());
                    sfcRtnQty = BigDecimal.ZERO;

                    List<SfcWipTranProjection> sfcWipTran = sfcWipTranRepository.getSfcWipTran(mrvDetail.getProjectNo(), mrvDetail.getPartNo());
                } else {

                }
            }
        }
    }

    private MRV checkIfRecordExist(Long id) {
        Optional<MRV> optionalItem = mrvRepository.findById(id);

        if (!optionalItem.isPresent()) {
            throw new NotFoundException("Record is not found");
        }
        return optionalItem.get();
    }

    private MrvDTO convertToMrvDTO(MRV mrv) {
        MrvDTO mrvDTO = MrvDTO.builder()
                .mrvNo(mrv.getMrvNo())
                .currencyCode(mrv.getCurrencyCode())
                .currencyRate(mrv.getCurrencyRate())
                .build();
        mrvDTO.setId(mrv.getId());

        return mrvDTO;
    }
}
