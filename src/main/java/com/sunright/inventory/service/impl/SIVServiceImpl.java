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
import com.sunright.inventory.entity.ItemProjection;
import com.sunright.inventory.entity.bombypj.BombypjProjection;
import com.sunright.inventory.entity.docmno.DocmNoProjection;
import com.sunright.inventory.entity.enums.Status;
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
        if (input.getProjectNo() != null) {
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
    public DocmValueDTO getGeneratedNo(SIVDTO input) {
        DocmNoProjection docmNo = docmNoRepository.getLastGeneratedNo(
                UserProfileContext.getUserProfile().getCompanyCode(),
                UserProfileContext.getUserProfile().getPlantNo(),
                "SIV",
                input.getProjectNo().substring(0, 1));

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
    public SIVDetailDTO checkNextItem(SIVDTO input) {

        UserProfile userProfile = UserProfileContext.getUserProfile();

        if (input.getProjectNo() != null) {
            checkRecValid(input);
            populateDetails(userProfile, input);
        }

        return SIVDetailDTO.builder().build();
    }

    private void populateDetails(UserProfile userProfile, SIVDTO input) {

        SaleDetailProjection sdetProjection = saleDetailRepository.getProjectType(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getProjectNo());
        List<ItemProjection> itemProjections = itemRepository.getDataByProjectNo(userProfile.getCompanyCode(), userProfile.getPlantNo(), input.getProjectNo());
        String sivType = sdetProjection.getProductType();
        if (sivType == null) {
            throw new ServerException("Project Type of " + input.getProjectNo() + " is unknown!");
        }
    }
}