package com.sunright.inventory.util;

import com.sunright.inventory.entity.nlctl.NLCTLProjection;
import com.sunright.inventory.exception.NotFoundException;
import com.sunright.inventory.exception.ServerException;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.repository.NLCTLRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.YearMonth;

@Component
public class IdentifierGeneratorUtil {

    @Autowired
    private NLCTLRepository nlctlRepository;

    public Long getNewBatchNo(Long objBatchNo) {
        NLCTLProjection batchYear = nlctlRepository.getBatchYear(UserProfileContext.getUserProfile().getCompanyCode(),
                UserProfileContext.getUserProfile().getPlantNo());

        Long newBatchNo;

        if (objBatchNo == null) {
            newBatchNo = (batchYear.getBatchNo().multiply(BigDecimal.valueOf(10000))).add(BigDecimal.valueOf(1)).longValue();
        } else {
            String batchYr = String.valueOf(objBatchNo).substring(0, 4);
            String batchNo = String.valueOf(objBatchNo).substring(7);
            BigDecimal btchYr = BigDecimal.valueOf(Double.parseDouble(batchYr));
            BigDecimal btchNo = BigDecimal.valueOf(Double.parseDouble(batchNo)).add(BigDecimal.valueOf(1));

            if (BigDecimal.valueOf(Double.parseDouble(batchYr)).intValue() < batchYear.getBatchNo().intValue()) {
                btchYr = batchYear.getBatchNo();
                btchNo = BigDecimal.ZERO;
            }

            newBatchNo = (btchYr.multiply(BigDecimal.valueOf(10000))).add(btchNo).longValue();

            if (newBatchNo == null) {
                throw new NotFoundException("New Batch No not found!");
            }
        }

        return newBatchNo;
    }

    public Boolean checkInventoryPeriod(String companyCode, Integer plantNo) {

        NLCTLProjection checkInvPeriod = nlctlRepository.checkInvPeriod(UserProfileContext.getUserProfile().getCompanyCode(),
                UserProfileContext.getUserProfile().getPlantNo());
        if (StringUtils.equals(checkInvPeriod.getInventoryEnabled(), "N")) {
            throw new ServerException("Inventory Operation is Disabled !");
        } else {
            YearMonth thisMonth = YearMonth.now();
            Integer currentMonth = thisMonth.getMonthValue();
            Long currentYear = Long.valueOf(thisMonth.getYear());
            if (!checkInvPeriod.getInventoryMonth().equals(currentMonth) || !checkInvPeriod.getInventoryYear().equals(currentYear)) {
                throw new ServerException("Inventory Period in NLCTL is not Current Month !");
            }
        }
        return true;
    }
}
