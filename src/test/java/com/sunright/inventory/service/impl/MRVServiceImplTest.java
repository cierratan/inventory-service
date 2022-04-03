package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.dto.mrv.MrvDetailDTO;
import com.sunright.inventory.entity.siv.SIV;
import com.sunright.inventory.entity.siv.SIVDetail;
import com.sunright.inventory.entity.siv.SIVDetailSub;
import com.sunright.inventory.exception.NotFoundException;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.SIVRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MRVServiceImplTest {

    public static final String COMPANY_CODE = "050";
    public static final Integer PLANT_NO = 0;

    @Mock
    private SIVRepository sivRepository;

    @InjectMocks
    private MRVServiceImpl mrvService;

    @BeforeEach
    public void init() {
        UserProfile userProfile = UserProfile.builder()
                .companyCode(COMPANY_CODE)
                .plantNo(PLANT_NO)
                .build();

        UserProfileContext.setUserProfileContext(userProfile);
    }

    @Test
    void findSivAndPopulateMRVDetails_NotFound() {
        Optional<SIV> sivFound = Optional.empty();

        when(sivRepository.findSIVByCompanyCodeAndPlantNoAndSivNo(anyString(), anyInt(), anyString())).thenReturn(sivFound);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> mrvService.findSivAndPopulateMRVDetails("siv001"));
        assertEquals("SIV No: siv001 is not found", exception.getMessage());
    }

    @Test
    void findSivAndPopulateMRVDetails_WithSubTypeN() {
        SIVDetail testSivDetail = new SIVDetail();
        testSivDetail.setBatchNo(1l);

        Set<SIVDetail> testSivDetails = new HashSet<>();
        testSivDetails.add(testSivDetail);

        // sale type should return P
        SIV testSiv = new SIV();
        testSiv.setSivDetails(testSivDetails);
        testSiv.setSubType("N");

        Optional<SIV> sivFound = Optional.of(testSiv);

        when(sivRepository.findSIVByCompanyCodeAndPlantNoAndSivNo(anyString(), anyInt(), anyString())).thenReturn(sivFound);

        MrvDetailDTO result = mrvService.findSivAndPopulateMRVDetails("");
        assertEquals("P", result.getSaleType());
        assertEquals(1l, result.getBatchNo());
    }

    @Test
    void findSivAndPopulateMRVDetails_WithSubTypeM() {
        String testDocmNo = "testDocmNo";

        SIVDetailSub sivDetailSub = new SIVDetailSub();
        sivDetailSub.setDocmNo(testDocmNo);
        Set<SIVDetailSub> sivDetailSubs = new HashSet<>();
        sivDetailSubs.add(sivDetailSub);

        SIVDetail testSivDetail = new SIVDetail();
        testSivDetail.setSivDetailSub(sivDetailSubs);

        Set<SIVDetail> testSivDetails = new HashSet<>();
        testSivDetails.add(testSivDetail);

        // tranType == "PR"
        SIV testSiv = new SIV();
        testSiv.setSivDetails(testSivDetails);
        testSiv.setSubType("M");
        testSiv.setTranType("PR");

        Optional<SIV> sivFound = Optional.of(testSiv);

        when(sivRepository.findSIVByCompanyCodeAndPlantNoAndSivNo(anyString(), anyInt(), anyString())).thenReturn(sivFound);

        MrvDetailDTO result = mrvService.findSivAndPopulateMRVDetails("");
        assertEquals("P", result.getSaleType());
        assertEquals(testDocmNo, result.getProjectNo());

        // tranType == "DS"
        testSiv = new SIV();
        testSiv.setSivDetails(testSivDetails);
        testSiv.setSubType("M");
        testSiv.setTranType("DS");
        testSiv.setDocmNo(testDocmNo);

        sivFound = Optional.of(testSiv);

        when(sivRepository.findSIVByCompanyCodeAndPlantNoAndSivNo(anyString(), anyInt(), anyString())).thenReturn(sivFound);

        result = mrvService.findSivAndPopulateMRVDetails("");
        assertEquals("D", result.getSaleType());
        assertEquals(testDocmNo, result.getProjectNo());

        // tranType == "WD"
        testSiv = new SIV();
        testSiv.setSivDetails(testSivDetails);
        testSiv.setSubType("M");
        testSiv.setTranType("WD");
        testSiv.setDocmNo(testDocmNo);

        sivFound = Optional.of(testSiv);

        when(sivRepository.findSIVByCompanyCodeAndPlantNoAndSivNo(anyString(), anyInt(), anyString())).thenReturn(sivFound);

        result = mrvService.findSivAndPopulateMRVDetails("");
        assertEquals("D", result.getSaleType());
        assertEquals(testDocmNo, result.getDocmNo());
        assertNull(result.getProjectNo());
    }
}