DROP TABLE MRVDET;
DROP TABLE MRV;

----------------------------- MRV TABLE -----------------------------

CREATE TABLE MRV
(
    "ID"            NUMBER(20, 0) GENERATED BY DEFAULT ON NULL AS IDENTITY,
    "COMPANY_CODE"  VARCHAR2(3 BYTE)                                   NOT NULL ENABLE,
    "PLANT_NO"      NUMBER(3, 0)                                       NOT NULL ENABLE,
    "MRV_NO"        VARCHAR2(15 BYTE)                                  NOT NULL ENABLE,
    "STATUS_1"      VARCHAR2(1 BYTE),
    "CURRENCY_CODE" VARCHAR2(3 BYTE),
    "CURRENCY_RATE" NUMBER(10, 6),
    "ENTRY_TIME"    VARCHAR2(10 BYTE),
    STATUS          INTEGER                  DEFAULT 1                 NOT NULL,
    CREATED_BY      VARCHAR2(200)            DEFAULT 'system'          NOT NULL,
    CREATED_AT      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UPDATED_BY      VARCHAR2(200)            DEFAULT 'system'          NOT NULL,
    UPDATED_AT      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX MRV_PK ON MRV ("ID");

ALTER TABLE MRV
    ADD CONSTRAINT MRV_PK PRIMARY KEY ("ID")
        USING INDEX MRV_PK ENABLE;

----------------------------- MRVDET TABLE -----------------------------

CREATE TABLE MRVDET
(
    "ID"           NUMBER(20, 0) GENERATED BY DEFAULT ON NULL AS IDENTITY,
    "MRV_ID"       NUMBER(20, 0)     NOT NULL ENABLE,
    "COMPANY_CODE" VARCHAR2(3 BYTE)  NOT NULL ENABLE,
    "PLANT_NO"     NUMBER(3, 0)      NOT NULL ENABLE,
    "MRV_NO"       VARCHAR2(15 BYTE) NOT NULL ENABLE,
    "SEQ_NO"       NUMBER(3, 0)      NOT NULL ENABLE,
    "ITEM_NO"      VARCHAR2(15 BYTE),
    "PART_NO"      VARCHAR2(60 BYTE),
    "LOC"          VARCHAR2(5 BYTE),
    "ITEM_TYPE"    NUMBER(2, 0),
    "BATCH_NO"     VARCHAR2(15 BYTE),
    "PROJECT_NO"   VARCHAR2(15 BYTE),
    "SIV_NO"       VARCHAR2(15 BYTE),
    "UOM"          VARCHAR2(3 BYTE),
    "RECD_QTY"     NUMBER(16, 4),
    "RECD_PRICE"   NUMBER(16, 4),
    "DOCM_NO"      VARCHAR2(15 BYTE),
    "TRAN_TYPE"    VARCHAR2(5 BYTE),
    "SALE_TYPE"    VARCHAR2(1 BYTE),
    "REPLACE"      VARCHAR2(1 BYTE),
    "MSR_STATUS"   VARCHAR2(1 BYTE),
    "LABEL_QTY"    NUMBER(16, 4),
    "REMARKS"      VARCHAR2(2000 BYTE),
    CONSTRAINT MRVDET_FK FOREIGN KEY ("MRV_ID")
        REFERENCES MRV ("ID") ENABLE
);

CREATE UNIQUE INDEX MRVDET_PK ON MRVDET ("ID");

ALTER TABLE MRVDET
    ADD CONSTRAINT MRVDET_PK PRIMARY KEY ("ID")
        USING INDEX MRVDET_PK ENABLE;