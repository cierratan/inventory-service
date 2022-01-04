package com.sunright.inventory.util;

import com.sunright.inventory.dto.Filter;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.interceptor.UserProfileContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class QueryGenerator {

    public Specification createDefaultSpecification() {
        Specification companyCode = ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id").get("companyCode"), UserProfileContext.getUserProfile().getCompanyCode()));
        Specification plantNo = ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id").get("plantNo"), UserProfileContext.getUserProfile().getPlantNo()));
        Specification status = ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), Status.ACTIVE));

        return companyCode.and(plantNo).and(status);
    }

    public Specification createSpecification(Filter input) {
        switch (input.getOperator()){
            case EQUALS:
                return (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get(input.getField()),
                                castToRequiredType(root.get(input.getField()).getJavaType(), input.getValue()));
            case LIKE:
                return (root, query, criteriaBuilder) ->
                        criteriaBuilder.like(root.get(input.getField()), "%" + input.getValue() + "%");
            default:
                throw new RuntimeException("Operation not supported yet");
        }
    }

    private Object castToRequiredType(Class fieldType, String value) {
        if(fieldType.isAssignableFrom(Double.class)) {
            return Double.valueOf(value);
        } else if(fieldType.isAssignableFrom(Integer.class)) {
            return Integer.valueOf(value);
        } else if(Enum.class.isAssignableFrom(fieldType)) {
            return Enum.valueOf(fieldType, value);
        } else if(fieldType.isAssignableFrom(String.class)) {
            return value;
        }

        return null;
    }
}
