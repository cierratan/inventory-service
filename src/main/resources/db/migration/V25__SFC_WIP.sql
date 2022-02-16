----------------------------------------------- SFC_WIP -----------------------------------------------

CREATE TABLE SFC_WIP
(
    "PROJECT_NO_SUB" VARCHAR2(15 BYTE)  NOT NULL ENABLE,
    "PCB_PART_NO"    VARCHAR2(100 BYTE) NOT NULL ENABLE,
    "PCB_QTY"        NUMBER(16, 4),
    "FLOW_ID"        VARCHAR2(10 BYTE),
    "STATUS"         VARCHAR2(1 BYTE),
    "CTN_QTY"        NUMBER(3, 0),
    "REMARKS"        VARCHAR2(2000 BYTE)
);

CREATE UNIQUE INDEX SFC_WIP_PK ON SFC_WIP ("PROJECT_NO_SUB", "PCB_PART_NO");

ALTER TABLE SFC_WIP
    ADD CONSTRAINT SFC_WIP_PK PRIMARY KEY ("PROJECT_NO_SUB", "PCB_PART_NO")
        USING INDEX SFC_WIP_PK ENABLE;

----------------------------------------------- SFC_WIP_TRAN -----------------------------------------------

CREATE TABLE SFC_WIP_TRAN
(
    "PRODUCT_ID"     VARCHAR2(30 BYTE)  NOT NULL ENABLE,
    "PROJECT_NO_SUB" VARCHAR2(15 BYTE)  NOT NULL ENABLE,
    "PCB_PART_NO"    VARCHAR2(100 BYTE) NOT NULL ENABLE,
    "PROJECT_CYCLE"  VARCHAR2(15 BYTE),
    "TRAN_ID"        VARCHAR2(20 BYTE),
    "FLOW_ID"        VARCHAR2(10 BYTE),
    "SEQ_NO"         NUMBER(3, 0),
    "LEVEL_NO"       NUMBER(3, 0),
    "TIME_IN"        DATE,
    "TIME_OUT"       DATE,
    "STATUS"         VARCHAR2(1 BYTE),
    "REMARKS"        VARCHAR2(2000 BYTE),
    CONSTRAINT SFC_WIP_TRAN_SFC_WIP_FK FOREIGN KEY ("PROJECT_NO_SUB", "PCB_PART_NO")
        REFERENCES SFC_WIP ("PROJECT_NO_SUB", "PCB_PART_NO") ENABLE
);

CREATE UNIQUE INDEX SFC_WIPTRAN_PK ON SFC_WIP_TRAN ("PRODUCT_ID");

ALTER TABLE SFC_WIP_TRAN
    ADD CONSTRAINT SFC_WIPTRAN_PK PRIMARY KEY ("PRODUCT_ID")
        USING INDEX SFC_WIPTRAN_PK ENABLE;

