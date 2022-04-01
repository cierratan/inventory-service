package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.interceptor.UserProfileContext;
import com.sunright.inventory.service.InvPeriodService;
import com.sunright.inventory.util.IdentifierGeneratorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvPeriodServiceImpl implements InvPeriodService {

    @Autowired
    private IdentifierGeneratorUtil identifierGeneratorUtil;

    @Override
    public Boolean checkInvPeriod() {

        UserProfile userProfile = UserProfileContext.getUserProfile();
        Boolean invPeriod = identifierGeneratorUtil.checkInventoryPeriod(userProfile.getCompanyCode(), userProfile.getPlantNo());
        return invPeriod;
    }
}
