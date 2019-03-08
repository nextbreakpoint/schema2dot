CREATE TABLE COUNTRY (
    COUNTRY_ID UUID NOT NULL,
    COUNTRY_NAME VARCHAR(255) NOT NULL,
    CURRENCY_ID UUID NOT NULL,
    ISO_2_CODE CHAR(2) NOT NULL,
    ISO_3_CODE CHAR(3) NOT NULL,
    PRIMARY KEY (COUNTRY_ID)
);

CREATE TABLE CURRENCY (
    CURRENCY_ID UUID NOT NULL,
    CURRENCY_NAME VARCHAR(255) NOT NULL,
    ISO_3_CODE CHAR(3) NOT NULL,
    PRIMARY KEY (CURRENCY_ID)
);

CREATE TABLE EXCHANGE_RATE (
    EXCHANGE_RATE_ID UUID NOT NULL,
    EXCHANGE_RATE_VALUE NUMERIC(10,6) NOT NULL,
    FROM_CURRENCY_ID UUID NOT NULL,
    TO_CURRENCY_ID UUID NOT NULL,
    START_DATE DATE NOT NULL,
    END_DATE DATE NOT NULL,
    PRIMARY KEY (EXCHANGE_RATE_ID)
);

ALTER TABLE COUNTRY
   ADD CONSTRAINT UK_10001 UNIQUE (COUNTRY_NAME);

ALTER TABLE COUNTRY
   ADD CONSTRAINT UK_10002 UNIQUE (ISO_2_CODE);

ALTER TABLE COUNTRY
   ADD CONSTRAINT UK_10003 UNIQUE (ISO_3_CODE);

ALTER TABLE CURRENCY
   ADD CONSTRAINT UK_10010 UNIQUE (CURRENCY_NAME);

ALTER TABLE CURRENCY
   ADD CONSTRAINT UK_10011 UNIQUE (ISO_3_CODE);

ALTER TABLE COUNTRY
   ADD CONSTRAINT FK_10001
   FOREIGN KEY (CURRENCY_ID)
   REFERENCES CURRENCY;

ALTER TABLE EXCHANGE_RATE
   ADD CONSTRAINT FK_10010
   FOREIGN KEY (FROM_CURRENCY_ID)
   REFERENCES CURRENCY;

ALTER TABLE EXCHANGE_RATE
   ADD CONSTRAINT FK_10011
   FOREIGN KEY (TO_CURRENCY_ID)
   REFERENCES CURRENCY;