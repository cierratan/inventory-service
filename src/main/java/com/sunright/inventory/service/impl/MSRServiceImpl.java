package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.grn.GrnSupplierDTO;
import com.sunright.inventory.dto.lov.DocmValueDTO;
import com.sunright.inventory.dto.msr.MsrDTO;
import com.sunright.inventory.dto.msr.MsrDetailDTO;
import com.sunright.inventory.dto.search.Filter;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.entity.ItemProjection;
import com.sunright.inventory.entity.bombypj.BombypjProjection;
import com.sunright.inventory.entity.docmno.DocmNoProjection;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.grn.GrnSupplierProjection;
import com.sunright.inventory.entity.msr.MSR;
import com.sunright.inventory.entity.msr.MSRDetail;
import com.sunright.inventory.exception.NotFoundException;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

                // post saving
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
                        userProfile.getCompanyCode(), userProfile.getPlantNo(), msrDetail.getItemNo());
            }
        }

        // post saving
        String type = "MSR";
        String subType = "N";

        DocmNoProjection docmNo = docmNoRepository.getLastGeneratedNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), type, subType);
        docmNoRepository.updateLastGeneratedNo(docmNo.getDocmNo(), userProfile.getCompanyCode(), userProfile.getPlantNo(), subType, type);

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
}
