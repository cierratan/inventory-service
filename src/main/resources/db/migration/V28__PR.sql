----------------------------------------------- PR -----------------------------------------------

CREATE TABLE PR
(
    "COMPANY_CODE"        VARCHAR2(3 BYTE)  NOT NULL ENABLE,
    "PLANT_NO"            NUMBER(3, 0)      NOT NULL ENABLE,
    "DOCM_NO"             VARCHAR2(15 BYTE) NOT NULL ENABLE,
    "DOCM_TYPE"           VARCHAR2(3 BYTE),
    "REQUESTOR"           VARCHAR2(30 BYTE),
    "CURRENCY_CODE"       VARCHAR2(3 BYTE),
    "CURRENCY_RATE"       NUMBER(10, 6),
    "CUSTOMER_CODE"       VARCHAR2(10 BYTE),
    "PRODUCT"             VARCHAR2(60 BYTE),
    "PCB_PART_NO"         VARCHAR2(60 BYTE),
    "TTL_SALE_AMT"        NUMBER(16, 4),
    "POTENTIAL_QTY"       NUMBER(16, 4),
    "PROB_SALES"          VARCHAR2(1 BYTE),
    "EST_PO_DATE"         DATE,
    "PROJECT_NO"          VARCHAR2(15 BYTE),
    "DIV_CODE"            VARCHAR2(2 BYTE),
    "DEPT_CODE"           VARCHAR2(3 BYTE),
    "ACCOUNTABILITY"      VARCHAR2(1 BYTE),
    "COQ_CAT_CODE"        VARCHAR2(3 BYTE),
    "SRA_NO"              VARCHAR2(15 BYTE),
    "CARRIAGE"            NUMBER(16, 4),
    "INSURANCE"           NUMBER(16, 4),
    "MISC_CHARGES"        NUMBER(16, 4),
    "DISCOUNT_AMT"        NUMBER(16, 4),
    "DISCOUNT_PCT"        NUMBER(8, 4),
    "DISCOUNT_DESC"       VARCHAR2(60 BYTE),
    "GST_PCT"             NUMBER(5, 2),
    "GST_AMT"             NUMBER(16, 4),
    "APPROVER"            VARCHAR2(30 BYTE),
    "APPROVAL_DATE"       DATE,
    "FINAL_APPROVER"      VARCHAR2(30 BYTE),
    "FINAL_APPROVAL_DATE" DATE,
    "REV_NO"              VARCHAR2(5 BYTE),
    "REV_DATE"            DATE,
    "STATUS"              VARCHAR2(1 BYTE),
    "ENTRY_USER"          VARCHAR2(30 BYTE),
    "ENTRY_DATE"          DATE,
    "CLOSED_DATE"         DATE,
    "REMARKS"             VARCHAR2(2000 BYTE)
);

CREATE UNIQUE INDEX PR_PK ON PR ("COMPANY_CODE", "PLANT_NO", "DOCM_NO");

ALTER TABLE PR
    ADD CONSTRAINT PR_PK PRIMARY KEY ("COMPANY_CODE", "PLANT_NO", "DOCM_NO")
        USING INDEX PR_PK ENABLE;

----------------------------------------------- PRDET -----------------------------------------------

CREATE TABLE PRDET
(
    "COMPANY_CODE" VARCHAR2(3 BYTE)  NOT NULL ENABLE,
    "PLANT_NO"     NUMBER(3, 0)      NOT NULL ENABLE,
    "DOCM_NO"      VARCHAR2(15 BYTE) NOT NULL ENABLE,
    "REC_SEQ"      NUMBER(3, 0)      NOT NULL ENABLE,
    "SEQ_NO"       NUMBER(3, 0),
    "ITEM_TYPE"    NUMBER(2, 0),
    "ITEM_NO"      VARCHAR2(15 BYTE),
    "PART_NO"      VARCHAR2(60 BYTE),
    "DESCRIPTION"  VARCHAR2(100 BYTE),
    "DUE_DATE"     DATE,
    "SOURCE"       VARCHAR2(60 BYTE),
    "UOM"          VARCHAR2(3 BYTE),
    "LOC"          VARCHAR2(5 BYTE),
    "QTY"          NUMBER(16, 4),
    "UNIT_PRICE"   NUMBER(16, 4),
    "REASON_CODE"  VARCHAR2(5 BYTE),
    "REASON_DESC"  VARCHAR2(60 BYTE),
    CONSTRAINT PRDET_FK FOREIGN KEY ("COMPANY_CODE", "PLANT_NO", "DOCM_NO")
        REFERENCES PR ("COMPANY_CODE", "PLANT_NO", "DOCM_NO") ENABLE
);

CREATE UNIQUE INDEX PRDET_PK ON PRDET ("COMPANY_CODE", "PLANT_NO", "DOCM_NO", "REC_SEQ");

ALTER TABLE PRDET
    ADD CONSTRAINT PRDET_PK PRIMARY KEY ("COMPANY_CODE", "PLANT_NO", "DOCM_NO", "REC_SEQ")
        USING INDEX PRDET_PK ENABLE;
