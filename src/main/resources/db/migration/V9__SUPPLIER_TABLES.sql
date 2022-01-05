------------------ COMPANY TABLE ------------------

CREATE TABLE COMPANY
(
    "COMPANY_CODE"     VARCHAR2(3 BYTE) NOT NULL ENABLE,
    "PLANT_NO"         NUMBER(3, 0)     NOT NULL ENABLE,
    "COMPANY_NAME"     VARCHAR2(45 BYTE),
    "PLANT_NAME"       VARCHAR2(45 BYTE),
    "NAME_SHORT"       VARCHAR2(10 BYTE),
    "ADDRESS1"         VARCHAR2(30 BYTE),
    "ADDRESS2"         VARCHAR2(30 BYTE),
    "ADDRESS3"         VARCHAR2(30 BYTE),
    "ADDRESS4"         VARCHAR2(30 BYTE),
    "PCODE"            VARCHAR2(15 BYTE),
    "TEL_NO"           VARCHAR2(15 BYTE),
    "TEL_NO2"          VARCHAR2(15 BYTE),
    "FAX_NO"           VARCHAR2(15 BYTE),
    "FAX_NO2"          VARCHAR2(15 BYTE),
    "CURRENCY_CODE"    VARCHAR2(3 BYTE) NOT NULL ENABLE,
    "BUSINESS_NATURE"  VARCHAR2(20 BYTE),
    "COUNTRY_CODE"     VARCHAR2(10 BYTE),
    "COUNTRY_NAME"     VARCHAR2(45 BYTE),
    "REGION_CODE"      VARCHAR2(10 BYTE),
    "REGION_NAME"      VARCHAR2(45 BYTE),
    "STATE_CODE"       VARCHAR2(10 BYTE),
    "STATE_NAME"       VARCHAR2(45 BYTE),
    "STOCK_LOC"        VARCHAR2(5 BYTE),
    "DATE_FORMAT"      VARCHAR2(2 BYTE),
    "ACCESS_IN"        VARCHAR2(2 BYTE),
    "ACCESS_BOM"       VARCHAR2(2 BYTE),
    "ACCESS_MRP"       VARCHAR2(2 BYTE),
    "ACCESS_WC"        VARCHAR2(2 BYTE),
    "ACCESS_CRP"       VARCHAR2(2 BYTE),
    "ACCESS_MPS"       VARCHAR2(2 BYTE),
    "ACCESS_SFC"       VARCHAR2(2 BYTE),
    "ACCESS_CA"        VARCHAR2(2 BYTE),
    "ACCESS_SOP"       VARCHAR2(2 BYTE),
    "ACCESS_PUR"       VARCHAR2(2 BYTE),
    "ACCESS_NL"        VARCHAR2(2 BYTE),
    "ACCESS_SL"        VARCHAR2(2 BYTE),
    "ACCESS_PL"        VARCHAR2(2 BYTE),
    "ACCESS_PR"        VARCHAR2(2 BYTE),
    "ACCESS_CAD"       VARCHAR2(2 BYTE),
    "ACCESS_ST"        VARCHAR2(2 BYTE),
    "ACCESS_LOT"       VARCHAR2(2 BYTE),
    "ACCESS_EX"        VARCHAR2(2 BYTE),
    "ACCESS_FA"        VARCHAR2(2 BYTE),
    "ACCESS_PER"       VARCHAR2(2 BYTE),
    "ACCESS_UTIL"      VARCHAR2(2 BYTE),
    "TRADER_CODE"      VARCHAR2(20 BYTE),
    "TAX_REGN_NO"      VARCHAR2(20 BYTE),
    "FREIGHT_PERCENT"  NUMBER(8, 4),
    "HANDLING_PERCENT" NUMBER(8, 4),
    "INVOICE_TITLE"    VARCHAR2(20 BYTE),
    "PASSWORD_CHANGE"  NUMBER(3, 0),
    "BUSINESS_REGN_NO" VARCHAR2(20 BYTE),
    "ACCESS_CP"        VARCHAR2(2 BYTE),
    "TAX_CAT"          VARCHAR2(10 BYTE),
    "PRODUCT_VERSION"  VARCHAR2(10 BYTE),
    "GAF_VERSION"      VARCHAR2(10 BYTE),
    "CURRENCY_DECIMAL" NUMBER(3, 0),
    "TZNAME"           VARCHAR2(64 BYTE),
    "SYSTEM_EMAIL"     VARCHAR2(100 BYTE)
);

CREATE UNIQUE INDEX COMPANY_PK ON COMPANY ("COMPANY_CODE", "PLANT_NO");

ALTER TABLE COMPANY
    ADD CONSTRAINT COMPANY_PK PRIMARY KEY ("COMPANY_CODE", "PLANT_NO")
        USING INDEX COMPANY_PK ENABLE;

------------------ SUPPLIER TABLE ------------------

CREATE TABLE SUPPLIER
(
    "SUPPLIER_CODE"    VARCHAR2(10 BYTE) NOT NULL ENABLE,
    "COMPANY_CODE"     VARCHAR2(3 BYTE)  NOT NULL ENABLE,
    "PLANT_NO"         NUMBER(3, 0)      NOT NULL ENABLE,
    "NAME"             VARCHAR2(45 BYTE),
    "ADDRESS1"         VARCHAR2(30 BYTE),
    "ADDRESS2"         VARCHAR2(30 BYTE),
    "ADDRESS3"         VARCHAR2(30 BYTE),
    "ADDRESS4"         VARCHAR2(30 BYTE),
    "PCODE"            VARCHAR2(15 BYTE),
    "TEL_NO"           VARCHAR2(15 BYTE),
    "FAX_NO"           VARCHAR2(15 BYTE),
    "CONTACT"          VARCHAR2(45 BYTE),
    "CONTACT_POSITION" VARCHAR2(30 BYTE),
    "BUYER"            VARCHAR2(20 BYTE),
    "CURRENCY_CODE"    VARCHAR2(3 BYTE),
    "CREDIT_LIMIT"     NUMBER(14, 2),
    "PAYMENT_TERM"     NUMBER(3, 0),
    "SHIPPING_TERM"    VARCHAR2(30 BYTE),
    "LAST_TRAN_AMT"    NUMBER(14, 2),
    "LAST_TRAN_DATE"   DATE,
    "BALBF"            NUMBER(14, 2),
    "DIV_CODE"         VARCHAR2(2 BYTE),
    "DEPT_CODE"        VARCHAR2(3 BYTE),
    "LOCAL_OR_FOREIGN" VARCHAR2(1 BYTE),
    "GST_NO"           VARCHAR2(20 BYTE),
    "REMARKS"          VARCHAR2(2000 BYTE),
    "PUR_AMT1"         NUMBER(14, 2),
    "PUR_AMT2"         NUMBER(14, 2),
    "PUR_AMT3"         NUMBER(14, 2),
    "PUR_AMT4"         NUMBER(14, 2),
    "PUR_AMT5"         NUMBER(14, 2),
    "PUR_AMT6"         NUMBER(14, 2),
    "PUR_AMT7"         NUMBER(14, 2),
    "PUR_AMT8"         NUMBER(14, 2),
    "PUR_AMT9"         NUMBER(14, 2),
    "PUR_AMT10"        NUMBER(14, 2),
    "PUR_AMT11"        NUMBER(14, 2),
    "PUR_AMT12"        NUMBER(14, 2),
    "PAY_AMT1"         NUMBER(14, 2),
    "PAY_AMT2"         NUMBER(14, 2),
    "PAY_AMT3"         NUMBER(14, 2),
    "PAY_AMT4"         NUMBER(14, 2),
    "PAY_AMT5"         NUMBER(14, 2),
    "PAY_AMT6"         NUMBER(14, 2),
    "PAY_AMT7"         NUMBER(14, 2),
    "PAY_AMT8"         NUMBER(14, 2),
    "PAY_AMT9"         NUMBER(14, 2),
    "PAY_AMT10"        NUMBER(14, 2),
    "PAY_AMT11"        NUMBER(14, 2),
    "PAY_AMT12"        NUMBER(14, 2),
    "SUPPLIER_TYPE"    VARCHAR2(1 BYTE),
    "COUNTRY_CODE"     VARCHAR2(10 BYTE),
    "AREA_CODE"        VARCHAR2(3 BYTE),
    "SUPPLIER_CAT"     VARCHAR2(1 BYTE),
    "COMPANY_CODE_SUB" VARCHAR2(3 BYTE),
    "PLANT_NO_SUB"     NUMBER(3, 0),
    "MOBILE_NO"        VARCHAR2(30 BYTE),
    "EMAIL_ADDRESS"    VARCHAR2(100 BYTE),
    "SUPPLIER_BRN"     VARCHAR2(30 BYTE),
    "TAX_TYPE"         VARCHAR2(10 BYTE),
    "PAYTO_ADDRESS1"   VARCHAR2(30 BYTE),
    "PAYTO_ADDRESS2"   VARCHAR2(30 BYTE),
    "PAYTO_ADDRESS3"   VARCHAR2(30 BYTE),
    "PAYTO_ADDRESS4"   VARCHAR2(30 BYTE),
    "PAYTO_PCODE"      VARCHAR2(15 BYTE),
    "SHIPTO_CODE"      VARCHAR2(15 BYTE),
    CONSTRAINT SUPPLIER_COMPANY_FK FOREIGN KEY ("COMPANY_CODE", "PLANT_NO")
        REFERENCES COMPANY ("COMPANY_CODE", "PLANT_NO") ENABLE
);

CREATE UNIQUE INDEX SUPPLIER_PK ON SUPPLIER ("COMPANY_CODE", "PLANT_NO", "SUPPLIER_CODE");

ALTER TABLE SUPPLIER
    ADD CONSTRAINT SUPPLIER_PK PRIMARY KEY ("COMPANY_CODE", "PLANT_NO", "SUPPLIER_CODE")
        USING INDEX SUPPLIER_PK ENABLE;