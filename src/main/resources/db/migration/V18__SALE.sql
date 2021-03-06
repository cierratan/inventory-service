----------------------------------------------- SALE -----------------------------------------------

CREATE TABLE SALE
(
    "COMPANY_CODE"           VARCHAR2(3 BYTE)  NOT NULL ENABLE,
    "PLANT_NO"               NUMBER(3, 0)      NOT NULL ENABLE,
    "ORDER_NO"               VARCHAR2(15 BYTE) NOT NULL ENABLE,
    "ORDER_DATE"             DATE              NOT NULL ENABLE,
    "CATEGORY_CODE"          VARCHAR2(3 BYTE),
    "CATEGORY_SUB_CODE"      VARCHAR2(5 BYTE),
    "CUSTOMER_CODE"          VARCHAR2(10 BYTE),
    "ORDER_REF"              VARCHAR2(100 BYTE),
    "RLSE_DATE"              DATE,
    "CUSTOMER_PO_IND"        VARCHAR2(1 BYTE),
    "CUSTOMER_PO_NO"         VARCHAR2(30 BYTE),
    "CUSTOMER_PO_RECD_DATE"  DATE,
    "QUOTATION_NO"           VARCHAR2(15 BYTE),
    "SALESMAN_CODE"          VARCHAR2(10 BYTE),
    "PAYMENT_TERM"           NUMBER(3, 0),
    "PAYMENT_DESC"           VARCHAR2(30 BYTE),
    "SHIPPING_TERM"          VARCHAR2(30 BYTE),
    "DIV_CODE"               VARCHAR2(2 BYTE),
    "DEPT_CODE"              VARCHAR2(3 BYTE),
    "ADDRESS1"               VARCHAR2(60 BYTE),
    "ADDRESS2"               VARCHAR2(60 BYTE),
    "ADDRESS3"               VARCHAR2(60 BYTE),
    "ADDRESS4"               VARCHAR2(60 BYTE),
    "PCODE"                  VARCHAR2(30 BYTE),
    "CONTACT"                VARCHAR2(60 BYTE),
    "SHIPTO_NAME"            VARCHAR2(60 BYTE),
    "SHIPTO_ADDRESS1"        VARCHAR2(60 BYTE),
    "SHIPTO_ADDRESS2"        VARCHAR2(60 BYTE),
    "SHIPTO_ADDRESS3"        VARCHAR2(60 BYTE),
    "SHIPTO_ADDRESS4"        VARCHAR2(60 BYTE),
    "SHIPTO_PCODE"           VARCHAR2(30 BYTE),
    "SHIPTO_COUNTRY"         VARCHAR2(10 BYTE),
    "SHIPTO_CONTACT"         VARCHAR2(60 BYTE),
    "SHIPTO_TEL_NO"          VARCHAR2(30 BYTE),
    "SHIPTO_FAX_NO"          VARCHAR2(30 BYTE),
    "SHIPTO_EMAIL_ADDRESS"   VARCHAR2(100 BYTE),
    "CHARGE_TO"              VARCHAR2(30 BYTE),
    "INSTALLATION_DATE"      DATE,
    "WARRANTY_PARTS_DATE"    DATE,
    "WARRANTY_LABOR_DATE"    DATE,
    "TRAINING_REQD"          VARCHAR2(1 BYTE),
    "DELIVERY_MODE"          VARCHAR2(30 BYTE),
    "FORWARDER"              VARCHAR2(60 BYTE),
    "CURRENCY_CODE"          VARCHAR2(3 BYTE),
    "CURRENCY_RATE"          NUMBER(10, 6),
    "PRINTED_DO"             VARCHAR2(1 BYTE),
    "PRINTED_OI"             VARCHAR2(1 BYTE),
    "PRINTED_SO"             VARCHAR2(1 BYTE),
    "OPEN_CLOSE"             VARCHAR2(1 BYTE),
    "CLOSE_TYPE"             VARCHAR2(1 BYTE),
    "CLOSE_DATE"             DATE,
    "CLOSE_USER"             VARCHAR2(30 BYTE),
    "LAST_DO_NO"             VARCHAR2(15 BYTE),
    "LAST_OI_NO"             VARCHAR2(15 BYTE),
    "GROSS_AMT"              NUMBER(16, 4),
    "CARRIAGE"               NUMBER(16, 4),
    "SERVICE"                NUMBER(16, 4),
    "INSURANCE"              NUMBER(16, 4),
    "MISC_CHARGES"           NUMBER(16, 4),
    "DISCOUNT_AMOUNT"        NUMBER(16, 4),
    "DISCOUNT_PERCENT"       NUMBER(8, 4),
    "DISCOUNT_REMARKS"       VARCHAR2(60 BYTE),
    "NET_AMT"                NUMBER(16, 4),
    "REV_NO"                 VARCHAR2(5 BYTE),
    "REV_DATE"               DATE,
    "REV_RLSE"               DATE,
    "CUSTOMER_REF"           VARCHAR2(30 BYTE),
    "CONFIRM_FLAG"           VARCHAR2(1 BYTE),
    "DESIGN_APPROVAL1"       VARCHAR2(20 BYTE),
    "DESIGN_APPROVAL2"       VARCHAR2(20 BYTE),
    "DESIGN_APPROVAL_FAX1"   VARCHAR2(15 BYTE),
    "DESIGN_APPROVAL_FAX2"   VARCHAR2(15 BYTE),
    "ORIGINATOR"             VARCHAR2(45 BYTE),
    "ORIGIN_SUBMIT_DATE"     DATE,
    "AUTHORISE_DESIGNATION1" VARCHAR2(20 BYTE),
    "AUTHORISE_DESIGNATION2" VARCHAR2(20 BYTE),
    "AUTHORISE_DESIGNATION3" VARCHAR2(20 BYTE),
    "AUTHORISE_NAME1"        VARCHAR2(20 BYTE),
    "AUTHORISE_NAME2"        VARCHAR2(20 BYTE),
    "AUTHORISE_NAME3"        VARCHAR2(20 BYTE),
    "ENTRY_USER"             VARCHAR2(30 BYTE),
    "ENTRY_DATE"             DATE,
    "CONVERT_FLAG"           VARCHAR2(1 BYTE),
    "POTENTIAL"              VARCHAR2(1 BYTE),
    "SALE_TYPE"              VARCHAR2(1 BYTE),
    "PATENT_REQD"            VARCHAR2(1 BYTE),
    "ROHS_STATUS"            VARCHAR2(1 BYTE),
    "COQ_CAT_CODE"           VARCHAR2(3 BYTE),
    "COQ_REASON_CODE"        VARCHAR2(5 BYTE),
    "COQ_DIV_CODE"           VARCHAR2(2 BYTE),
    "COQ_DEPT_CODE"          VARCHAR2(3 BYTE),
    "SRA_NO"                 VARCHAR2(15 BYTE),
    "BUSINESS_UNIT"          VARCHAR2(5 BYTE),
    "REMARKS"                VARCHAR2(2000 BYTE)
);

CREATE UNIQUE INDEX SALE_PK ON SALE ("COMPANY_CODE", "PLANT_NO", "ORDER_NO");

ALTER TABLE SALE
    ADD CONSTRAINT SALE_PK PRIMARY KEY ("COMPANY_CODE", "PLANT_NO", "ORDER_NO")
        USING INDEX SALE_PK ENABLE;

----------------------------------------------- SALEDET -----------------------------------------------

CREATE TABLE SALEDET
(
    "COMPANY_CODE"        VARCHAR2(3 BYTE)  NOT NULL ENABLE,
    "PLANT_NO"            NUMBER(3, 0)      NOT NULL ENABLE,
    "ORDER_NO"            VARCHAR2(15 BYTE) NOT NULL ENABLE,
    "REC_SEQ"             NUMBER(3, 0)      NOT NULL ENABLE,
    "SEQ_NO"              NUMBER(3, 0),
    "LEVEL_NO"            NUMBER(3, 0),
    "LINE_NO"             NUMBER(7, 2),
    "PROJECT_NO"          VARCHAR2(15 BYTE),
    "PROJECT_NO_SUB"      VARCHAR2(15 BYTE),
    "SUB_ASSEMBLY_SEQ"    NUMBER(3, 0),
    "ITEM_NO"             VARCHAR2(15 BYTE),
    "LOC"                 VARCHAR2(5 BYTE),
    "PART_NO"             VARCHAR2(60 BYTE),
    "SPEC_NO"             VARCHAR2(30 BYTE),
    "SOCKET_PART_NO"      VARCHAR2(60 BYTE),
    "SOCKET_REF_QUOTE"    VARCHAR2(60 BYTE),
    "DENSITY"             NUMBER(5, 0),
    "DENSITY_DESC"        VARCHAR2(30 BYTE),
    "UOM"                 VARCHAR2(3 BYTE),
    "STOCK_STATUS"        VARCHAR2(1 BYTE),
    "DUE_DATE"            DATE,
    "ORDER_QTY"           NUMBER(16, 4),
    "UNIT_PRICE"          NUMBER(16, 4),
    "AMORTISE_AW_ENG"     VARCHAR2(1 BYTE),
    "AW_CHARGE"           NUMBER(16, 4),
    "ENG_CHARGE"          NUMBER(16, 4),
    "SHIPPED_QTY"         NUMBER(16, 4),
    "SHIPPED_DATE"        DATE,
    "MBI_DATE"            DATE,
    "AW_PCB_DATE"         DATE,
    "STD_MATERIAL"        NUMBER(16, 4),
    "FREIGHT"             NUMBER(16, 4),
    "HANDLING"            NUMBER(16, 4),
    "CUSTOM_DUTY"         NUMBER(16, 4),
    "ANY_DISCOUNT"        VARCHAR2(1 BYTE),
    "DISCOUNT_AMOUNT"     NUMBER(16, 4),
    "DISCOUNT_PERCENT"    NUMBER(8, 4),
    "TAXABLE"             VARCHAR2(1 BYTE),
    "TAX_CAT"             VARCHAR2(10 BYTE),
    "TAX_TYPE"            VARCHAR2(10 BYTE),
    "TAX_PERCENT"         NUMBER(8, 4),
    "TAX_AMOUNT"          NUMBER(16, 4),
    "PICKED_LOC"          VARCHAR2(5 BYTE),
    "PICKED_QTY"          NUMBER(16, 4),
    "FREIGHT_IND"         VARCHAR2(1 BYTE),
    "PATENT_FEE"          NUMBER(16, 4),
    "PATENT_DESC"         VARCHAR2(60 BYTE),
    "INSTALL_CHARGE"      NUMBER(16, 4),
    "INSTALL_DESC"        VARCHAR2(60 BYTE),
    "INSTALL_UOM"         VARCHAR2(3 BYTE),
    "RETEST_CHARGE"       NUMBER(16, 4),
    "RETEST_DESC"         VARCHAR2(60 BYTE),
    "RETEST_UOM"          VARCHAR2(3 BYTE),
    "AW_DESC"             VARCHAR2(60 BYTE),
    "AW_UOM"              VARCHAR2(3 BYTE),
    "ENG_DESC"            VARCHAR2(60 BYTE),
    "ENG_UOM"             VARCHAR2(3 BYTE),
    "OPEN_CLOSE"          VARCHAR2(1 BYTE),
    "CLOSE_TYPE"          VARCHAR2(1 BYTE),
    "CLOSE_DATE"          DATE,
    "CLOSE_USER"          VARCHAR2(30 BYTE),
    "CONVERT_FLAG"        VARCHAR2(1 BYTE),
    "ORDER_REF"           VARCHAR2(15 BYTE),
    "ORDER_SEQ_REF"       NUMBER(3, 0),
    "ORDER_REC_SEQ_REF"   NUMBER(3, 0),
    "LEAD_COUNT"          NUMBER(5, 0),
    "PACKAGING"           VARCHAR2(15 BYTE),
    "SOCKET_VDR"          VARCHAR2(15 BYTE),
    "CUST_PO_LINE_NO"     NUMBER(7, 2),
    "AW_LINE_NO"          NUMBER(7, 2),
    "INSTALL_LINE_NO"     NUMBER(7, 2),
    "RETEST_LINE_NO"      NUMBER(7, 2),
    "ENG_LINE_NO"         NUMBER(7, 2),
    "PATENT_LINE_NO"      NUMBER(7, 2),
    "CUST_PART_NO"        VARCHAR2(60 BYTE),
    "ROHS_STATUS"         VARCHAR2(1 BYTE),
    "PLATFORM"            VARCHAR2(15 BYTE),
    "PROCESSOR_TYPE"      VARCHAR2(15 BYTE),
    "PRODUCT_TYPE"        VARCHAR2(10 BYTE),
    "PRODUCT_SUB_TYPE"    VARCHAR2(10 BYTE),
    "PRODUCT_DESC"        VARCHAR2(120 BYTE),
    "INV_UOM"             VARCHAR2(3 BYTE),
    "STD_PACK_QTY"        NUMBER(12, 6),
    "REMARKS"             VARCHAR2(2000 BYTE),
    "BOARD_STAGE"         VARCHAR2(3 BYTE),
    "BOARD_STAGE_QTY"     NUMBER(16, 4),
    "BOARD_STAGE_APPROVE" VARCHAR2(1 BYTE),
    CONSTRAINT "SALEDET_FK" FOREIGN KEY ("COMPANY_CODE", "PLANT_NO", "ORDER_NO")
        REFERENCES SALE ("COMPANY_CODE", "PLANT_NO", "ORDER_NO") ENABLE
);

CREATE UNIQUE INDEX SALEDET_PK ON SALEDET ("COMPANY_CODE", "PLANT_NO", "ORDER_NO", "REC_SEQ");

ALTER TABLE SALEDET
    ADD CONSTRAINT SALEDET_PK PRIMARY KEY ("COMPANY_CODE", "PLANT_NO", "ORDER_NO", "REC_SEQ")
        USING INDEX SALEDET_PK ENABLE;

----------------------------------------------- SALE_REMARKS -----------------------------------------------

CREATE TABLE SALE_REMARKS
(
    "COMPANY_CODE" VARCHAR2(3 BYTE)  NOT NULL ENABLE,
    "PLANT_NO"     NUMBER(3, 0)      NOT NULL ENABLE,
    "ORDER_NO"     VARCHAR2(15 BYTE) NOT NULL ENABLE,
    "SEQ_NO"       NUMBER(3, 0)      NOT NULL ENABLE,
    "REMARKS"      VARCHAR2(2000 BYTE),
    CONSTRAINT "SALE_REMARKS_FK" FOREIGN KEY ("COMPANY_CODE", "PLANT_NO", "ORDER_NO")
        REFERENCES SALE ("COMPANY_CODE", "PLANT_NO", "ORDER_NO") ENABLE
);

CREATE UNIQUE INDEX SALE_REMARKS_PK ON SALE_REMARKS ("COMPANY_CODE", "PLANT_NO", "ORDER_NO", "SEQ_NO");

ALTER TABLE SALE_REMARKS
    ADD CONSTRAINT SALE_REMARKS_PK PRIMARY KEY ("COMPANY_CODE", "PLANT_NO", "ORDER_NO", "SEQ_NO")
        USING INDEX SALE_REMARKS_PK ENABLE;

----------------------------------------------- SALE_CHARGES -----------------------------------------------

CREATE TABLE SALE_CHARGES
(
    "COMPANY_CODE"      VARCHAR2(3 BYTE)  NOT NULL ENABLE,
    "PLANT_NO"          NUMBER(3, 0)      NOT NULL ENABLE,
    "ORDER_NO"          VARCHAR2(15 BYTE) NOT NULL ENABLE,
    "ORDER_REC_SEQ"     NUMBER(3, 0)      NOT NULL ENABLE,
    "SEQ_NO"            NUMBER(3, 0)      NOT NULL ENABLE,
    "NAME"              VARCHAR2(30 BYTE),
    "LINE_NO"           VARCHAR2(10 BYTE),
    "UOM"               VARCHAR2(3 BYTE),
    "QTY"               NUMBER(16, 4),
    "UNIT_PRICE"        NUMBER(16, 4),
    "SHIPPED_QTY"       NUMBER(16, 4),
    "STATUS"            VARCHAR2(1 BYTE),
    "ORDER_REF"         VARCHAR2(15 BYTE),
    "ORDER_REC_SEQ_REF" NUMBER(3, 0),
    "CHARGE_SEQ_REF"    NUMBER(3, 0),
    "DESCRIPTION"       VARCHAR2(150 BYTE),
    CONSTRAINT "SALE_CHARGES_FK" FOREIGN KEY ("COMPANY_CODE", "PLANT_NO", "ORDER_NO", "ORDER_REC_SEQ")
        REFERENCES SALEDET ("COMPANY_CODE", "PLANT_NO", "ORDER_NO", "REC_SEQ") ENABLE
);

CREATE UNIQUE INDEX SALE_CHARGES_PK ON SALE_CHARGES ("COMPANY_CODE", "PLANT_NO", "ORDER_NO", "ORDER_REC_SEQ", "SEQ_NO");

ALTER TABLE SALE_CHARGES
    ADD CONSTRAINT SALE_CHARGES_PK PRIMARY KEY ("COMPANY_CODE", "PLANT_NO", "ORDER_NO", "ORDER_REC_SEQ", "SEQ_NO")
        USING INDEX SALE_CHARGES_PK ENABLE;
