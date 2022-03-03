package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.InvCtlDTO;
import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.search.Filter;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SearchResult;
import com.sunright.inventory.entity.base.BaseIdEntity;
import com.sunright.inventory.entity.invctl.InvCtl;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.exception.NotFoundException;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.InvCtlRepository;
import com.sunright.inventory.service.InvCtlService;
import com.sunright.inventory.util.QueryGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;

@Service
public class InvCtlServiceImpl implements InvCtlService {

    @Autowired
    private QueryGenerator queryGenerator;

    @Autowired
    private InvCtlRepository invCtlRepository;

    @Override
    public InvCtlDTO createInvCtl(InvCtlDTO input) {
        UserProfile userProfile = UserProfileContext.getUserProfile();

        InvCtl invCtl = new InvCtl();
        BeanUtils.copyProperties(input, invCtl);
        invCtl.setId(populateId());
        invCtl.setStatus(Status.ACTIVE);
        invCtl.setCreatedBy(userProfile.getUsername());
        invCtl.setCreatedAt(ZonedDateTime.now());
        invCtl.setUpdatedBy(userProfile.getUsername());
        invCtl.setUpdatedAt(ZonedDateTime.now());

        InvCtl saved = invCtlRepository.save(invCtl);
        input.setVersion(saved.getVersion());

        return input;
    }

    @Override
    public InvCtlDTO editInvCtl(InvCtlDTO input) {
        BaseIdEntity id = populateId();

        InvCtl found = checkIfRecordExist(id);

        InvCtl invCtl = new InvCtl();
        BeanUtils.copyProperties(input, invCtl, "status");
        invCtl.setId(id);
        invCtl.setStatus(found.getStatus());
        invCtl.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
        invCtl.setUpdatedAt(ZonedDateTime.now());

        InvCtl saved = invCtlRepository.save(invCtl);

        input.setVersion(saved.getVersion());
        return input;
    }

    @Override
    public InvCtlDTO findBy() {
        BaseIdEntity id = populateId();

        InvCtl found = checkIfRecordExist(id);

        InvCtlDTO invCtlDTO = InvCtlDTO.builder().build();
        BeanUtils.copyProperties(found, invCtlDTO);

        return invCtlDTO;
    }

    @Override
    public void deleteInvCtl() {
        BaseIdEntity id = populateId();

        InvCtl invCtl = checkIfRecordExist(id);
        invCtl.setStatus(Status.DELETED);
        invCtl.setUpdatedBy(UserProfileContext.getUserProfile().getUsername());
        invCtl.setUpdatedAt(ZonedDateTime.now());

        invCtlRepository.save(invCtl);
    }

    @Override
    public SearchResult<InvCtlDTO> searchBy(SearchRequest searchRequest) {

        Specification activeStatus = ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), Status.ACTIVE));

        Specification<InvCtl> specs = where(activeStatus);

        if(!CollectionUtils.isEmpty(searchRequest.getFilters())) {
            for (Filter filter : searchRequest.getFilters()) {
                specs = specs.and(queryGenerator.createSpecification(filter));
            }
        }

        Page<InvCtl> pgInvCtl = invCtlRepository.findAll(specs, queryGenerator.constructPageable(searchRequest));

        SearchResult<InvCtlDTO> invcCtl = new SearchResult<>();
        invcCtl.setTotalRows(pgInvCtl.getTotalElements());
        invcCtl.setTotalPages(pgInvCtl.getTotalPages());
        invcCtl.setCurrentPageNumber(pgInvCtl.getPageable().getPageNumber());
        invcCtl.setCurrentPageSize(pgInvCtl.getNumberOfElements());
        invcCtl.setRows(pgInvCtl.getContent().stream().map(item -> InvCtlDTO.builder()
                .stockDepn(item.getStockDepn())
                .provAge(item.getProvAge())
                .build()
        ).collect(Collectors.toList()));

        return invcCtl;
    }

    private BaseIdEntity populateId() {
        UserProfile userProfile = UserProfileContext.getUserProfile();

        BaseIdEntity id = new BaseIdEntity();
        id.setCompanyCode(userProfile.getCompanyCode());
        id.setPlantNo(userProfile.getPlantNo());

        return id;
    }

    private InvCtl checkIfRecordExist(BaseIdEntity id) {
        Optional<InvCtl> optionalInvCtl = invCtlRepository.findById(id);

        if (!optionalInvCtl.isPresent()) {
            throw new NotFoundException("Record is not found");
        }
        return optionalInvCtl.get();
    }
}
