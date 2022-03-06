package com.sunright.inventory.util;

import com.sunright.inventory.dto.search.DataSorting;
import com.sunright.inventory.dto.search.Filter;
import com.sunright.inventory.dto.search.SearchRequest;
import com.sunright.inventory.dto.search.SortOption;
import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.interceptor.UserProfileContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Locale;

@Component
public class QueryGenerator {

    @Deprecated
    public Specification createDefaultSpecification() {
        Specification companyCode = ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id").get("companyCode"), UserProfileContext.getUserProfile().getCompanyCode()));
        Specification plantNo = ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id").get("plantNo"), UserProfileContext.getUserProfile().getPlantNo()));
        Specification status = ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), Status.ACTIVE));

        return companyCode.and(plantNo).and(status);
    }

    public Specification createDefaultSpec() {
        Specification companyCode = ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("companyCode"), UserProfileContext.getUserProfile().getCompanyCode()));
        Specification plantNo = ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("plantNo"), UserProfileContext.getUserProfile().getPlantNo()));
        Specification status = ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), Status.ACTIVE));

        return companyCode.and(plantNo).and(status);
    }

    public Specification createSpecification(Filter input) {
        switch (input.getOperator()) {
            case EQUALS:
                return (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get(input.getField()),
                                castToRequiredType(root.get(input.getField()).getJavaType(), input.getValue()));
            case LIKE:
                final String strValue;
                if (StringUtils.contains(input.getValue(), "_")) {
                    strValue = StringUtils.replace(input.getValue(), "_", "\\_");
                } else if (StringUtils.contains(input.getValue(), "%")) {
                    strValue = StringUtils.replace(input.getValue(), "%", "\\%");
                } else {
                    strValue = input.getValue();
                }

                return (root, query, criteriaBuilder) -> // add by Arya (case-insensitive like matching anywhere)
                        criteriaBuilder.like(criteriaBuilder.lower(root.get(input.getField())), "%" + strValue.toLowerCase(Locale.ROOT) + "%", '\\');
            default:
                throw new RuntimeException("Operation not supported yet");
        }
    }

    public Pageable constructPageable(SearchRequest searchRequest) {
        Sort sort = null;

        if (!CollectionUtils.isEmpty(searchRequest.getSorts())) {
            for (DataSorting dataSort : searchRequest.getSorts()) {
                sort = Sort.by(dataSort.getSort() == SortOption.ASC ? Sort.Direction.ASC : Sort.Direction.DESC, dataSort.getField());
            }
        } else {
            sort = Sort.by(Sort.Direction.DESC, "updatedAt");
        }

        return PageRequest.of(searchRequest.getPage(), searchRequest.getLimit(), sort);
    }

    private Object castToRequiredType(Class fieldType, String value) {
        if (fieldType.isAssignableFrom(Double.class)) {
            return Double.valueOf(value);
        } else if (fieldType.isAssignableFrom(Integer.class)) {
            return Integer.valueOf(value);
        } else if (Enum.class.isAssignableFrom(fieldType)) {
            return Enum.valueOf(fieldType, value);
        } else if (fieldType.isAssignableFrom(String.class)) {
            return value;
        }

        return null;
    }
}
