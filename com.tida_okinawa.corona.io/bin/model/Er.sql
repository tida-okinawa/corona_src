SET SESSION FOREIGN_KEY_CHECKS=0;

/* Drop Tables */

DROP TABLE REL_FLUC;
DROP TABLE DIC_FLUC;
DROP TABLE REL_SYNONYM;
DROP TABLE DIC_SYNONYM;
DROP TABLE FLUC_TBL;
DROP TABLE REL_COMMON_LABEL;
DROP TABLE SYNONYM_TBL;
DROP TABLE DIC_COMMON;
DROP TABLE LABEL_TREE;
DROP TABLE DIC_LABEL;
DROP TABLE DIC_PATTERN;
DROP TABLE REL_PRJ_DIC;
DROP TABLE REL_PRODUCT_DIC;
DROP TABLE DIC_TABLE;
DROP TABLE CATEGORY;
DROP TABLE CLAIMS;
DROP TABLE DIC_PRI;
DROP TABLE FIELDS;
DROP TABLE REL_CLM_PRODUCT;
DROP TABLE REL_PRJ_PRODUCT;
DROP TABLE PRODUCT;
DROP TABLE REL_PRJ_CLM;
DROP TABLE PROJECT;
DROP TABLE TABLES;
DROP TABLE TYPE_PATTERN;
DROP TABLE WORKDATAS;




/* Create Tables */

-- 分野辞書の分野を定義するテーブル
CREATE TABLE CATEGORY
(
	ID INT NOT NULL AUTO_INCREMENT,
	NAME VARCHAR(40) NOT NULL UNIQUE,
	PRIMARY KEY (ID)
) COMMENT = '分野辞書の分野を定義するテーブル';


CREATE TABLE CLAIMS
(
	ID INT NOT NULL AUTO_INCREMENT,
	WORK_TBL_ID INT,
	KEY_FLD_ID INT,
	PRODUCT_FLD_ID INT,
	-- 誤記補正対象フィールドを、数値カンマ区切り定義
	TGT_FLDS VARCHAR(40) COMMENT '誤記補正対象フィールドを、数値カンマ区切り定義',
	EXTERNAL_FLG BOOLEAN,
	INTERNAL_FLG BOOLEAN,
	PRIMARY KEY (ID)
);


-- 用語を定義するためのテーブル
-- どの辞書に属するかは辞書IDにより判断する
CREATE TABLE DIC_COMMON
(
	ITEM_ID INT NOT NULL AUTO_INCREMENT,
	NAME VARCHAR(255) NOT NULL,
	READING VARCHAR(255) NOT NULL,
	PART_ID INT NOT NULL,
	CLASS_ID INT,
	CFORM_ID INT,
	DIC_ID INT NOT NULL,
	INACTIVE BOOLEAN,
	JUMAN_BASE VARCHAR(4096),
	PRIMARY KEY (ITEM_ID)
) COMMENT = '用語を定義するためのテーブル
どの辞書に属するかは辞書IDにより判断する';


-- 辞書IDを跨いだ定義は許さない
CREATE TABLE DIC_FLUC
(
	FLUC_ID INT NOT NULL AUTO_INCREMENT,
	DIC_ID INT NOT NULL,
	ITEM_ID INT NOT NULL,
	INACTIVE BOOLEAN,
	PRIMARY KEY (FLUC_ID)
) COMMENT = '辞書IDを跨いだ定義は許さない';


CREATE TABLE DIC_LABEL
(
	LABEL_ID INT NOT NULL AUTO_INCREMENT,
	DIC_ID INT NOT NULL,
	LABEL_NAME VARCHAR(40),
	INACTIVE BOOLEAN,
	PRIMARY KEY (LABEL_ID)
);


CREATE TABLE DIC_PATTERN
(
	ID INT NOT NULL AUTO_INCREMENT,
	DIC_ID INT NOT NULL,
	NAME VARCHAR(80),
	PATTERN VARCHAR(4096) NOT NULL,
	TYPE_ID INT NOT NULL,
	PARTS BOOLEAN NOT NULL,
	INACTIVE BOOLEAN,
	PRIMARY KEY (ID)
);


CREATE TABLE DIC_PRI
(
	ID INT NOT NULL,
	FLD_ID INT NOT NULL,
	DIC_ID INT NOT NULL,
	PRIORITY INT,
	INACTIVE BOOLEAN,
	PRIMARY KEY (ID, FLD_ID, DIC_ID)
);


-- 辞書IDを跨いだ定義は許さない
CREATE TABLE DIC_SYNONYM
(
	SYNONYM_ID INT NOT NULL AUTO_INCREMENT,
	DIC_ID INT NOT NULL,
	ITEM_ID INT NOT NULL,
	INACTIVE BOOLEAN,
	PRIMARY KEY (SYNONYM_ID)
) COMMENT = '辞書IDを跨いだ定義は許さない';


-- 辞書を管理するテーブル
-- 
-- 辞書に登録するのは用語辞書（一般、専門など）、ゆらぎ辞書、同義語辞書、ラベル辞書、共起辞書などそれぞれの辞書を管理する
CREATE TABLE DIC_TABLE
(
	DIC_ID INT NOT NULL AUTO_INCREMENT,
	PARENT_ID VARCHAR(256) DEFAULT '-1',
	DIC_NAME VARCHAR(255) NOT NULL,
	DIC_FILE_NAME VARCHAR(255) NOT NULL,
	DIC_TYPE INT NOT NULL,
	-- 辞書タイプが分野のときのみ有効
	CATEGORY_ID INT COMMENT '辞書タイプが分野のときのみ有効',
	DATE DATETIME,
	INACTIVE BOOLEAN,
	PRIMARY KEY (DIC_ID)
) COMMENT = '辞書を管理するテーブル

辞書に登録するのは用語辞書（一般、専門など）、ゆらぎ辞書、同義語辞書、ラベル辞書、共起辞書';


CREATE TABLE FIELDS
(
	ID INT NOT NULL AUTO_INCREMENT,
	TBL_ID INT NOT NULL,
	FLD_ID INT,
	NAME VARCHAR(256),
	DBNAME VARCHAR(128),
	PRIMARY KEY (ID)
);


CREATE TABLE FLUC_TBL
(
	ID INT NOT NULL AUTO_INCREMENT,
	DIC_ID INT NOT NULL,
	ITEM_ID INT NOT NULL,
	INACTIVE BOOLEAN,
	PRIMARY KEY (ID)
);


CREATE TABLE LABEL_TREE
(
	PARENT_ID INT NOT NULL,
	CHILD_ID INT NOT NULL,
	PRIMARY KEY (PARENT_ID, CHILD_ID)
);


-- ターゲットを管理するテーブル
CREATE TABLE PRODUCT
(
	PRODUCT_ID INT NOT NULL AUTO_INCREMENT,
	PRODUCT_NAME VARCHAR(100) NOT NULL UNIQUE,
	PRIMARY KEY (PRODUCT_ID)
) COMMENT = 'ターゲットを管理するテーブル';


-- プロジェクトに対する情報テーブル
CREATE TABLE PROJECT
(
	PROJECT_ID INT NOT NULL AUTO_INCREMENT,
	PROJECT_NAME VARCHAR(80) NOT NULL UNIQUE,
	GUID VARCHAR(0),
	PRIMARY KEY (PROJECT_ID)
) COMMENT = 'プロジェクトに対する情報テーブル';


CREATE TABLE REL_CLM_PRODUCT
(
	PRJ_ID INT NOT NULL,
	PRODUCT_ID INT NOT NULL,
	TBL_ID INT NOT NULL,
	WORK_TBL_ID INT,
	-- マイニング結果を出力するためのテーブル情報を記載する。
	-- 最終出力を行う際に出力テーブルを作成し、本フィールドにIDをセットする予定
	REL_TBL_ID INT COMMENT 'マイニング結果を出力するためのテーブル情報を記載する。
最終出力を行う際に出力テーブルを作成し、本フィールドにIDをセットする予定',
	TGT_FLDS VARCHAR(40),
	PRIMARY KEY (PRJ_ID, PRODUCT_ID, TBL_ID)
);


CREATE TABLE REL_COMMON_LABEL
(
	REL_LABEL_ID INT NOT NULL AUTO_INCREMENT,
	LABEL_ID INT NOT NULL,
	DIC_ID INT NOT NULL,
	ITEM_ID INT NOT NULL,
	VALUE INT,
	MATH_FLG BOOLEAN,
	PRIMARY KEY (REL_LABEL_ID)
);


CREATE TABLE REL_FLUC
(
	ID INT NOT NULL,
	FLUC_ID INT NOT NULL,
	STATUS INT,
	PRIMARY KEY (ID, FLUC_ID)
);


-- プロジェクトに属するターゲットをどのクレーム情報を使ってマイニングするのかを定義するテーブル。
CREATE TABLE REL_PRJ_CLM
(
	PRJ_ID INT NOT NULL,
	TBL_ID INT NOT NULL,
	PRIMARY KEY (PRJ_ID, TBL_ID)
) COMMENT = 'プロジェクトに属するターゲットをどのクレーム情報を使ってマイニングするのかを定義するテーブル。';


-- プロジェクト単位で、管理する辞書を定義するためのテーブル
CREATE TABLE REL_PRJ_DIC
(
	PROJECT_ID INT NOT NULL,
	DIC_ID INT NOT NULL,
	PRIMARY KEY (PROJECT_ID, DIC_ID)
) COMMENT = 'プロジェクト単位で、管理する辞書を定義するためのテーブル';


CREATE TABLE REL_PRJ_PRODUCT
(
	PROJECT_ID INT NOT NULL,
	PRODUCT_ID INT NOT NULL,
	PRIMARY KEY (PROJECT_ID, PRODUCT_ID)
);


-- ターゲット単位で指定する辞書を管理するためのテーブル
CREATE TABLE REL_PRODUCT_DIC
(
	DIC_ID INT NOT NULL,
	PRODUCT_ID INT NOT NULL,
	PRIMARY KEY (DIC_ID, PRODUCT_ID)
) COMMENT = 'ターゲット単位で指定する辞書を管理するためのテーブル';


CREATE TABLE REL_SYNONYM
(
	ID INT NOT NULL,
	SYNONYM_ID INT NOT NULL,
	STATUS INT,
	PRIMARY KEY (ID, SYNONYM_ID)
);


CREATE TABLE SYNONYM_TBL
(
	ID INT NOT NULL AUTO_INCREMENT,
	DIC_ID INT NOT NULL,
	ITEM_ID INT NOT NULL,
	INACTIVE BOOLEAN,
	PRIMARY KEY (ID)
);


CREATE TABLE TABLES
(
	ID INT NOT NULL AUTO_INCREMENT,
	NAME VARCHAR(255) NOT NULL,
	DBNAME VARCHAR(255) NOT NULL UNIQUE,
	TYPE INT NOT NULL,
	COMMENT VARCHAR(255),
	LASTED DATETIME,
	PRIMARY KEY (ID)
);


CREATE TABLE TYPE_PATTERN
(
	ID INT NOT NULL AUTO_INCREMENT,
	NAME VARCHAR(20) NOT NULL UNIQUE,
	PRIMARY KEY (ID)
);


CREATE TABLE WORKDATAS
(
	ID INT NOT NULL AUTO_INCREMENT,
	PROJECT_ID INT,
	PRODUCT_ID INT,
	INPUT_TABLE_ID INT NOT NULL,
	TYPE INT,
	LINK VARCHAR(255),
	LASTED DATETIME,
	PRIMARY KEY (ID)
);



/* Create Foreign Keys */

ALTER TABLE DIC_TABLE
	ADD FOREIGN KEY (CATEGORY_ID)
	REFERENCES CATEGORY (ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE DIC_FLUC
	ADD FOREIGN KEY (ITEM_ID)
	REFERENCES DIC_COMMON (ITEM_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE DIC_SYNONYM
	ADD FOREIGN KEY (ITEM_ID)
	REFERENCES DIC_COMMON (ITEM_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE FLUC_TBL
	ADD FOREIGN KEY (ITEM_ID)
	REFERENCES DIC_COMMON (ITEM_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE REL_COMMON_LABEL
	ADD FOREIGN KEY (ITEM_ID)
	REFERENCES DIC_COMMON (ITEM_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE SYNONYM_TBL
	ADD FOREIGN KEY (ITEM_ID)
	REFERENCES DIC_COMMON (ITEM_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE REL_FLUC
	ADD FOREIGN KEY (FLUC_ID)
	REFERENCES DIC_FLUC (FLUC_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE LABEL_TREE
	ADD FOREIGN KEY (CHILD_ID)
	REFERENCES DIC_LABEL (LABEL_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE LABEL_TREE
	ADD FOREIGN KEY (PARENT_ID)
	REFERENCES DIC_LABEL (LABEL_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE REL_COMMON_LABEL
	ADD FOREIGN KEY (LABEL_ID)
	REFERENCES DIC_LABEL (LABEL_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE REL_SYNONYM
	ADD FOREIGN KEY (SYNONYM_ID)
	REFERENCES DIC_SYNONYM (SYNONYM_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE DIC_COMMON
	ADD FOREIGN KEY (DIC_ID)
	REFERENCES DIC_TABLE (DIC_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE DIC_FLUC
	ADD FOREIGN KEY (DIC_ID)
	REFERENCES DIC_TABLE (DIC_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE DIC_LABEL
	ADD FOREIGN KEY (DIC_ID)
	REFERENCES DIC_TABLE (DIC_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE DIC_PATTERN
	ADD FOREIGN KEY (DIC_ID)
	REFERENCES DIC_TABLE (DIC_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE DIC_SYNONYM
	ADD FOREIGN KEY (DIC_ID)
	REFERENCES DIC_TABLE (DIC_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE FLUC_TBL
	ADD FOREIGN KEY (DIC_ID)
	REFERENCES DIC_TABLE (DIC_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE REL_COMMON_LABEL
	ADD FOREIGN KEY (DIC_ID)
	REFERENCES DIC_TABLE (DIC_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE REL_PRJ_DIC
	ADD FOREIGN KEY (DIC_ID)
	REFERENCES DIC_TABLE (DIC_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE REL_PRODUCT_DIC
	ADD FOREIGN KEY (DIC_ID)
	REFERENCES DIC_TABLE (DIC_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE SYNONYM_TBL
	ADD FOREIGN KEY (DIC_ID)
	REFERENCES DIC_TABLE (DIC_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE REL_FLUC
	ADD FOREIGN KEY (ID)
	REFERENCES FLUC_TBL (ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE REL_CLM_PRODUCT
	ADD FOREIGN KEY (PRODUCT_ID)
	REFERENCES PRODUCT (PRODUCT_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE REL_PRJ_PRODUCT
	ADD FOREIGN KEY (PRODUCT_ID)
	REFERENCES PRODUCT (PRODUCT_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE REL_PRODUCT_DIC
	ADD FOREIGN KEY (PRODUCT_ID)
	REFERENCES PRODUCT (PRODUCT_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE REL_PRJ_CLM
	ADD FOREIGN KEY (PRJ_ID)
	REFERENCES PROJECT (PROJECT_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE REL_PRJ_DIC
	ADD FOREIGN KEY (PROJECT_ID)
	REFERENCES PROJECT (PROJECT_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE REL_PRJ_PRODUCT
	ADD FOREIGN KEY (PROJECT_ID)
	REFERENCES PROJECT (PROJECT_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE REL_CLM_PRODUCT
	ADD FOREIGN KEY (PRJ_ID, TBL_ID)
	REFERENCES REL_PRJ_CLM (PRJ_ID, TBL_ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE REL_SYNONYM
	ADD FOREIGN KEY (ID)
	REFERENCES SYNONYM_TBL (ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE CLAIMS
	ADD FOREIGN KEY (ID)
	REFERENCES TABLES (ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE FIELDS
	ADD FOREIGN KEY (TBL_ID)
	REFERENCES TABLES (ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE REL_PRJ_CLM
	ADD FOREIGN KEY (TBL_ID)
	REFERENCES TABLES (ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE DIC_PRI
	ADD FOREIGN KEY (ID)
	REFERENCES WORKDATAS (ID)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;



