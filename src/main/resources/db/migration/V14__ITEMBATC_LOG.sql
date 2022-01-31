CREATE TABLE ITEMBATC_LOG
(
    "COMPANY_CODE" VARCHAR2(3 BYTE)                                   NOT NULL ENABLE,
    "PLANT_NO"     NUMBER(3, 0)                                       NOT NULL ENABLE,
    "ITEM_NO"      VARCHAR2(15 BYTE)                                  NOT NULL ENABLE,
    "LOC"          VARCHAR2(5 BYTE)                                   NOT NULL ENABLE,
    "BATCH_NO"     NUMBER(8, 0)                                       NOT NULL ENABLE,
    "SIV_NO"       VARCHAR2(15 BYTE)                                  NOT NULL ENABLE,
    "SIV_QTY"      NUMBER(16, 4),
    "DATE_CODE"    NUMBER(4, 0),
    "PO_NO"        VARCHAR2(15 BYTE),
    "PO_REC_SEQ"   NUMBER(3, 0),
    "GRN_NO"       VARCHAR2(15 BYTE),
    "GRN_SEQ"      NUMBER(3, 0),
    "GRN_QTY"      NUMBER(16, 4),
    STATUS         INTEGER                  DEFAULT 1                 NOT NULL,
    CREATED_BY     VARCHAR2(200)            DEFAULT 'system'          NOT NULL,
    CREATED_AT     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UPDATED_BY     VARCHAR2(200)            DEFAULT 'system'          NOT NULL,
    UPDATED_AT     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX ITEMBATC_LOG_PK ON ITEMBATC_LOG ("COMPANY_CODE", "PLANT_NO", "ITEM_NO", "LOC", "BATCH_NO", "SIV_NO");
ALTER TABLE ITEMBATC_LOG
    ADD CONSTRAINT ITEMBATC_LOG_PK PRIMARY KEY ("COMPANY_CODE", "PLANT_NO", "ITEM_NO", "LOC", "BATCH_NO", "SIV_NO")
        USING INDEX ITEMBATC_LOG_PK ENABLE;