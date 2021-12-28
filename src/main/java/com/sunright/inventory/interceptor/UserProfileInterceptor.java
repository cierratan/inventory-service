package com.sunright.inventory.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunright.inventory.dto.UserProfile;
import com.sunright.inventory.exception.ErrorMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserProfileInterceptor implements HandlerInterceptor {
    private static final String USERNAME_HEADER = "X-USERNAME";
    private static final String COMPANY_CODE_HEADER = "X-COMPANYCODE";
    private static final String PLANT_NO_HEADER = "X-PLANTNO";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String username = request.getHeader(USERNAME_HEADER);
        String companyCode = request.getHeader(COMPANY_CODE_HEADER);
        String plantNo = request.getHeader(PLANT_NO_HEADER);

        if(StringUtils.isAnyBlank(username, companyCode, plantNo)) {
            ErrorMessage errorMessage = ErrorMessage.builder()
                    .message("X-USERNAME, X-COMPANYCODE and X-PLANTNO request headers can't be empty")
                    .build();

            ObjectMapper mapper = new ObjectMapper();

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(mapper.writeValueAsString(errorMessage));
            response.getWriter().flush();

            return false;
        }

        UserProfileContext.setUserProfileContext(UserProfile.builder()
                .username(username)
                .companyCode(companyCode)
                .plantNo(Integer.parseInt(plantNo))
                .build());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserProfileContext.clear();
    }
}
