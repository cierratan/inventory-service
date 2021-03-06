----------------------------------------------- ITEMEQL -----------------------------------------------

CREATE TABLE ITEMEQL
(
    "COMPANY_CODE"   VARCHAR2(3 BYTE) NOT NULL ENABLE,
    "PLANT_NO"       NUMBER(3, 0)     NOT NULL ENABLE,
    "ITEM_NO"        VARCHAR2(15 BYTE),
    "ALTERNATE"      VARCHAR2(15 BYTE),
    "ASSEMBLY_NO"    VARCHAR2(15 BYTE),
    "RPC_NO"         VARCHAR2(15 BYTE),
    "ENTRY_DATE"     DATE,
    "ENTRY_USER"     VARCHAR2(30 BYTE),
    "OPEN_CLOSE"     VARCHAR2(1 BYTE),
    "CLOSE_DATE"     DATE,
    "CUSTOMER_CODE"  VARCHAR2(10 BYTE),
    "CUSTOMER_GROUP" VARCHAR2(10 BYTE),
    "MNC_NAME"       VARCHAR2(30 BYTE)
);

CREATE INDEX ITEMEQL_IDX1 ON ITEMEQL ("COMPANY_CODE", "PLANT_NO", "ITEM_NO", "ALTERNATE");

CREATE INDEX ITEMEQL_IDX2 ON ITEMEQL ("COMPANY_CODE", "PLANT_NO", "ALTERNATE", "ITEM_NO");

CREATE INDEX ITEMEQL_IDX3 ON ITEMEQL ("COMPANY_CODE", "PLANT_NO", "ASSEMBLY_NO", "ITEM_NO", "ALTERNATE");

CREATE INDEX ITEMEQL_IDX4 ON ITEMEQL ("COMPANY_CODE", "PLANT_NO", "ASSEMBLY_NO", "ALTERNATE", "ITEM_NO");

CREATE INDEX ITEMEQL_IDX5 ON ITEMEQL ("COMPANY_CODE", "PLANT_NO", "ITEM_NO");

CREATE INDEX ITEMEQL_IDX6 ON ITEMEQL ("COMPANY_CODE", "PLANT_NO", "ALTERNATE");