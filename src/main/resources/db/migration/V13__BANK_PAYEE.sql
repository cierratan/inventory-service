------------------ BANK_PAYEE TABLE ------------------

CREATE TABLE BANK_PAYEE
(
    "SUPPLIER_CODE"          VARCHAR2(10 BYTE) NOT NULL ENABLE,
    "COMPANY_CODE"           VARCHAR2(3 BYTE)  NOT NULL ENABLE,
    "PLANT_NO"               NUMBER(3, 0)      NOT NULL ENABLE,
    "BANK_ACCOUNT_NO"        VARCHAR2(40 BYTE) NOT NULL ENABLE,
    "BANK_ACCOUNT_NAME"      VARCHAR2(45 BYTE),
    "BANK_CODE_PAYEE"        VARCHAR2(15 BYTE),
    "BRANCH_CODE"            VARCHAR2(10 BYTE),
    "CLEARING_CODE_FOR_TT"   VARCHAR2(40 BYTE),
    "INTERMEDIARY_BANK_CODE" VARCHAR2(15 BYTE),
    "NAME1"                  VARCHAR2(45 BYTE),
    "NAME2"                  VARCHAR2(45 BYTE),
    "ADDRESS1"               VARCHAR2(30 BYTE),
    "ADDRESS2"               VARCHAR2(30 BYTE),
    "ADDRESS3"               VARCHAR2(30 BYTE),
    "ADDRESS4"               VARCHAR2(30 BYTE),
    "REMARKS1"               VARCHAR2(70 BYTE),
    "REMARKS2"               VARCHAR2(70 BYTE),
    CONSTRAINT BP_SUPPLIER_FK FOREIGN KEY ("COMPANY_CODE", "PLANT_NO", "SUPPLIER_CODE")
        REFERENCES SUPPLIER ("COMPANY_CODE", "PLANT_NO", "SUPPLIER_CODE") ENABLE
);

CREATE UNIQUE INDEX BANK_PAYEE_PK ON BANK_PAYEE ("SUPPLIER_CODE", "COMPANY_CODE", "PLANT_NO", "BANK_ACCOUNT_NO");

ALTER TABLE BANK_PAYEE
    ADD CONSTRAINT BANK_PAYEE_PK PRIMARY KEY ("SUPPLIER_CODE", "COMPANY_CODE", "PLANT_NO", "BANK_ACCOUNT_NO")
        USING INDEX BANK_PAYEE_PK ENABLE;