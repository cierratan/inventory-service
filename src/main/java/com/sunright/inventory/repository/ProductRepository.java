package com.sunright.inventory.repository;

import com.sunright.inventory.entity.product.Product;
import com.sunright.inventory.entity.product.ProductId;
import com.sunright.inventory.entity.product.ProductProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, ProductId>, JpaSpecificationExecutor<Product> {

    @Query("SELECT p.wipTracking as wipTracking FROM PRODUCT p WHERE p.id.companyCode = :companyCode " +
            "AND p.id.plantNo = :plantNo AND p.id.type = :type AND p.id.subType = :subType")
    ProductProjection wipTrackCur(String companyCode, Integer plantNo, String type, String subType);
}
