
------- Country ---------------------------------------
CREATE TABLE COUNTRY
(	"COUNTRY_CODE" VARCHAR2(10 BYTE) NOT NULL ENABLE,
     "DESCRIPTION" VARCHAR2(60 BYTE),
     "LETTER_2" VARCHAR2(2 BYTE),
     "DIGIT_3" VARCHAR2(3 BYTE),
     "WT_PCT" NUMBER(8,4),
     "REMARKS" VARCHAR2(2000 BYTE)
);

CREATE UNIQUE INDEX COUNTRY_PK ON COUNTRY ("COUNTRY_CODE");
ALTER TABLE COUNTRY ADD CONSTRAINT COUNTRY_PK PRIMARY KEY ("COUNTRY_CODE")
    USING INDEX COUNTRY_PK ENABLE;

insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('ITA','ITALY','IT','380',null,null);
insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('SGP','SINGAPORE','SG','702',null,null);
insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('JPN','JAPAN','JP','392',null,null);
insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('USA','UNITED STATES OF AMERICA','US','840',null,null);
insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('CHN','CHINA','CN','156',null,null);
insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('THA','THAILAND','TH','764',null,null);
insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('TWN','TAIWAN','TW','158',null,'ASIA');
insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('KOR','KOREA','KR','410',null,null);
insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('ISR','ISRAEL','IL','376',null,null);
insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('EUR','EUROPE',null,null,null,null);
insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('IND','INDIA','IN','356',null,null);
insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('PRT','PORTUGAL','PT','620',null,null);
insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('CRI','COSTA RICA','CR','188',null,null);
insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('MYS','MALAYSIA','MY','458',null,null);
insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('GBR','UNITED KINGDOM','GB','826',null,null);
insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('HKG','HONG KONG','HK','344',null,null);
insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('PHL','PHILIPPINES','PH','608',null,null);
insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('DEU','GERMANY','DE','276',null,null);
insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('VNM','VIET NAM','VN','704',null,null);
insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('FRA','FRANCE','FR','250',null,null);
insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('MLT','MALTA','MT','470',null,null);
insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('CAN','CANADA','CA','124',null,null);
insert into COUNTRY (COUNTRY_CODE,DESCRIPTION,LETTER_2,DIGIT_3,WT_PCT,REMARKS) values ('NOR','NORWAY','NO','578',null,null);

-------------Location --------------
CREATE TABLE LOC
(	"COMPANY_CODE" VARCHAR2(3 BYTE) NOT NULL ENABLE,
     "PLANT_NO" NUMBER(3,0) NOT NULL ENABLE,
     "LOC" VARCHAR2(5 BYTE) NOT NULL ENABLE,
     "VERSION" NUMBER NOT NULL,
     "DESCRIPTION" VARCHAR2(60 BYTE),
     "ADDRESS1" VARCHAR2(30 BYTE),
     "ADDRESS2" VARCHAR2(30 BYTE),
     "ADDRESS3" VARCHAR2(30 BYTE),
     "ADDRESS4" VARCHAR2(30 BYTE),
     "PCODE" VARCHAR2(15 BYTE),
     "TEL_NO" VARCHAR2(15 BYTE),
     "FAX_NO" VARCHAR2(15 BYTE),
     "COUNTRY_CODE" VARCHAR2(10 BYTE),
     "REGION_CODE" VARCHAR2(10 BYTE),
     "STATE_CODE" VARCHAR2(10 BYTE),
     "CITY_CODE" VARCHAR2(30 BYTE),
     "PERSON_IN_CHARGE" VARCHAR2(45 BYTE),
     "REMARKS" VARCHAR2(2000 BYTE)
);

CREATE UNIQUE INDEX LOC_PK ON LOC ("COMPANY_CODE", "PLANT_NO", "LOC");
ALTER TABLE LOC ADD CONSTRAINT LOC_PK PRIMARY KEY ("COMPANY_CODE", "PLANT_NO", "LOC")
    USING INDEX LOC_PK ENABLE;



