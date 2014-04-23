/**
 * @version $Id$
 * 
 * 2013/11/01 20:14:34
 * @author wataru-higa
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate.hql;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.tida_okinawa.corona.io.bean.RelClmProductBean;
import com.tida_okinawa.corona.io.bean.TablesBean;
import com.tida_okinawa.corona.io.dam.hibernate.IoService;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.model.dic.TermCForm;
import com.tida_okinawa.corona.io.model.dic.TermClass;
import com.tida_okinawa.corona.io.model.dic.TermPart;
import com.tida_okinawa.corona.io.model.table.TableType;

/**
 * @author wataru-higa
 * 
 */
public class CommonCreateQuery {

    // *****************************************//
    // categoryテーブル
    // *****************************************//

    /**
     * categoryテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    public static Query getCategoryQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM CategoryBean"); //$NON-NLS-1$
    }


    /**
     * categoryテーブル取得Query(NAME)
     * 
     * @param name
     *            NAME
     * @return Query
     */
    public static Query getCategoryQuery(String name) {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM CategoryBean WHERE name=:NAME").setString("NAME", name); //$NON-NLS-1$ //$NON-NLS-2$
    }


    /**
     * categoryテーブル取得Query(ID)
     * 
     * @param id
     *            ID
     * @return Query
     */
    public static Query getCategoryQuery(int id) {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM CategoryBean WHERE id=:ID").setInteger("ID", id); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // *****************************************//
    // claimsテーブル
    // *****************************************//

    /**
     * calimsテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getClaimsQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM ClaimsBean"); //$NON-NLS-1$
    }


    // *****************************************//
    // corona_dbversionテーブル
    // *****************************************//

    /**
     * corona_dbversionテーブル取得Query(条件なし)
     * 
     * @param session
     * 
     * @return Query
     */
    public static Query getCoronaDbVersionQuery(Session session) {
        return session.createQuery("FROM CoronaDbVersionBean"); //$NON-NLS-1$
    }


    // *****************************************//
    // dic_commonテーブル
    // *****************************************//

    /**
     * dic_commonテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getDicCommonQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM DicCommonBean"); //$NON-NLS-1$
    }


    /**
     * dic_commonテーブル取得Query(DIC_ID、NAME、READING、PART_ID、CLASS_ID、CFORM_ID)
     * 
     * @param dics
     *            List<IUserDic>
     * @param name
     *            String
     * @param reading
     *            String
     * @param part
     *            TermPart
     * @param cls
     *            TermClass
     * @param cform
     *            TermCForm
     * @return Query
     */
    public static Query getDicCommonQuery(List<IUserDic> dics, String name, String reading, TermPart part, TermClass cls, TermCForm cform) {
        Session session = IoService.getInstance().getSession();
        StringBuilder strHQL = new StringBuilder();
        strHQL.append("FROM DicCommonBean WHERE inactive=:INACTIVE AND dicId IN (:DIC_ID_LIST)"); //$NON-NLS-1$

        List<Integer> dicIds = new ArrayList<>();
        for (IUserDic dic : dics) {
            dicIds.add(dic.getId());
        }

        if (name != null)
            strHQL.append(" AND name=:NAME"); //$NON-NLS-1$
        if (reading != null)
            strHQL.append(" AND reading=:READING"); //$NON-NLS-1$
        if (part != TermPart.NONE)
            strHQL.append(" AND partId=:PART_ID"); //$NON-NLS-1$
        if (cls != TermClass.NONE)
            strHQL.append(" AND classId=:CLASS_ID"); //$NON-NLS-1$
        if (cform != TermCForm.NONE)
            strHQL.append(" AND cformId=:CFORM_ID"); //$NON-NLS-1$

        Query query = session.createQuery(strHQL.toString());
        query.setBoolean("INACTIVE", false); //$NON-NLS-1$
        query.setParameterList("DIC_ID_LIST", dicIds); //$NON-NLS-1$

        if (name != null)
            query.setString("NAME", name); //$NON-NLS-1$
        if (reading != null)
            query.setSerializable("READING", reading); //$NON-NLS-1$
        if (part != TermPart.NONE)
            query.setInteger("PART_ID", part.getIntValue()); //$NON-NLS-1$
        if (cls != TermClass.NONE)
            query.setInteger("CLASS_ID", cls.getIntValue()); //$NON-NLS-1$
        if (cform != TermCForm.NONE)
            query.setInteger("CFORM_ID", cform.getIntValue()); //$NON-NLS-1$

        return query;
    }


    // *****************************************//
    // dic_flucテーブル
    // *****************************************//

    /**
     * dic_flucテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getdicFlucQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM DicFlucBean"); //$NON-NLS-1$
    }


    // *****************************************//
    // dic_labelテーブル
    // *****************************************//

    /**
     * (条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getDicLabelQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM DicLabelBean"); //$NON-NLS-1$
    }


    /**
     * dic_labelテーブル取得Query(LABEL_ID)
     * 
     * @param itemId
     * @return Query
     */
    public static Query getDicLabelQuery(int labelId) {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM DicLabelBean WHERE labelId=:LBL_ID").setInteger("LBL_ID", labelId); //$NON-NLS-1$ //$NON-NLS-2$
    }


    /**
     * dic_table、label_treeのJoin結果取得用Query(DIC_ID)
     * 
     * @param dicId
     *            DIC_ID
     * @return Query(単一レコード)
     */
    public static Query getDicLabelJoinLabelTreeQuery(int dicId) {
        Session session = IoService.getInstance().getSession();
        //        String strSQL = "SELECT dic.*,tree.PARENT_ID FROM dic_label dic LEFT JOIN LABEL_TREE tree ON dic.LABEL_ID = tree.CHILD_ID WHERE dic.INACTIVE = false AND dic.DIC_ID = " //$NON-NLS-1$
        //                + dicId;
        StringBuilder sql = new StringBuilder(256);
        sql.append("SELECT dic.LABEL_ID,dic.DIC_ID,dic.LABEL_NAME,dic.INACTIVE,tree.PARENT_ID "); //$NON-NLS-1$
        sql.append("FROM dic_label dic LEFT JOIN LABEL_TREE tree ON dic.LABEL_ID = tree.CHILD_ID "); //$NON-NLS-1$
        sql.append("WHERE dic.INACTIVE = false AND dic.DIC_ID = ").append(dicId); //$NON-NLS-1$

        Query query = session.createSQLQuery(sql.toString());
        return query;
    }


    // *****************************************//
    // dic_patternテーブル
    // *****************************************//

    /**
     * dic_patternテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getDicPatternQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM DicPatternBean"); //$NON-NLS-1$
    }


    // *****************************************//
    // dic_priテーブル
    // *****************************************//

    /**
     * dic_priテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getDicPriQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM DicPriorityBean"); //$NON-NLS-1$
    }


    /**
     * dic_priテーブル取得Query(ID)
     * 
     * @param id
     *            ID
     * @return Query
     */
    public static Query getDicPriQuery(int id) {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM DicPriorityBean WHERE primaryKey.id=:ID").setInteger("ID", id); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // *****************************************//
    // dic_synonymテーブル
    // *****************************************//

    /**
     * dic_synonymテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getDicSynonymQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM DicSynonymBean"); //$NON-NLS-1$
    }


    // *****************************************//
    // dic_tableテーブル
    // *****************************************//

    /**
     * dic_tableテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getDicTableQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM DicTableBean"); //$NON-NLS-1$
    }


    /**
     * dic_tableテーブル取得Query(DIC_ID)
     * 
     * @param dicId
     *            DIC_ID
     * @return Query(単一レコード)
     */
    public static Query getDicTableQuery(int dicId) {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM DicTableBean WHERE dicId=:DIC_ID").setInteger("DIC_ID", dicId); //$NON-NLS-1$ //$NON-NLS-2$
    }


    /**
     * dic_tableテーブル取得Query(INACTIVE)
     * 
     * @param inactive
     *            INACTIVE
     * @return Query
     */
    public static Query getDicTableQuery(boolean inactive) {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("From DicTableBean WHERE inactive=:INACTIVE").setBoolean("INACTIVE", inactive); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // *****************************************//
    // fieldsテーブル
    // *****************************************//

    /**
     * fieldsテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getFieldsQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM FieldsBean"); //$NON-NLS-1$
    }


    // *****************************************//
    // fluc_tblテーブル
    // *****************************************//

    /**
     * fluc_tblテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getFlucTblQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM FlucTblBean"); //$NON-NLS-1$
    }


    // *****************************************//
    // label_treeテーブル
    // *****************************************//

    /**
     * label_treeテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getLabelTreeQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM LabelTreeBean"); //$NON-NLS-1$
    }


    /**
     * label_treeテーブル取得Query(PARENT_ID、CHILD_ID)
     * 
     * @param parentId
     *            PARENT_ID
     * @param childId
     *            CHILD_ID
     * @return Query(単一レコード)
     */
    @Deprecated
    public static Query getLabelTreeQuery(int parentId, int childId) {
        Session session = IoService.getInstance().getSession();
        // return
        // session.createQuery("FROM LabelTreeBean WHERE primaryKey.parentId=:PARENT_ID AND primaryKey.childId=:CHILD_ID").setInteger("PARENT_ID",
        // parentId)
        // .setInteger("CHILD_ID", childId);
        return session.createQuery("FROM LabelTreeBean WHERE parentId=:PARENT_ID AND childId=:CHILD_ID").setInteger("PARENT_ID", parentId) //$NON-NLS-1$ //$NON-NLS-2$
                .setInteger("CHILD_ID", childId); //$NON-NLS-1$
    }


    // *****************************************//
    // productテーブル
    // *****************************************//

    /**
     * productテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getProductQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM ProductBean"); //$NON-NLS-1$
    }


    /**
     * productテーブル取得Query(ID)
     * 
     * @return Query
     */
    public static Query getProductQuery(int productId) {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM ProductBean WHERE productId=:PRD_ID").setInteger("PRD_ID", productId); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // *****************************************//
    // projectテーブル
    // *****************************************//

    /**
     * projectテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    public static Query getProjectQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM ProjectBean"); //$NON-NLS-1$
    }


    /**
     * projectテーブル取得Query(PROJECT_NAME)
     * 
     * @param projectName
     *            PROJECT_NAME
     * @return String Query
     *         セット値：PRJ_NAME(String)
     */
    public static Query getProjectQuery(String projectName) {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM ProjectBean WHERE projectName=:PRJ_NAME").setString("PRJ_NAME", projectName); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // *****************************************//
    // recent_dic_priテーブル
    // *****************************************//

    /**
     * recent_dic_priテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getRecentDicPriQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM RecentDicPriBean"); //$NON-NLS-1$
    }


    // *****************************************//
    // rel_clm_productテーブル
    // *****************************************//

    /**
     * rel_clm_productテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getRelClmProductQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM RelClmProductBean"); //$NON-NLS-1$
    }


    /**
     * rel_clm_productテーブル取得Query(REL_TBL_ID)
     * 
     * @param relTblId
     *            REL_TBL_ID
     * @return Query
     */
    public static Query getRelClmProductQuery(int relTblId) {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM RelClmProductBean WHERE relTableId=:REL_TBL_ID").setInteger("REL_TBL_ID", relTblId); //$NON-NLS-1$ //$NON-NLS-2$
    }


    /**
     * projectテーブル取得Query(TBL_ID、PRJ_ID)
     * 
     * @param tableId
     *            TBL_ID
     * @param projectId
     *            PRJ_ID
     * @return Query
     */
    public static Query getRelClmProductQuery(int tableId, int projectId) {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM RelClmProductBean WHERE primaryKey.tableId=:TBL_ID AND primaryKey.projectId=:PRJ_ID").setInteger("TBL_ID", tableId) //$NON-NLS-1$ //$NON-NLS-2$
                .setInteger("PRJ_ID", projectId); //$NON-NLS-1$
    }


    // *****************************************//
    // rel_common_labelテーブル
    // *****************************************//

    /**
     * rel_common_labelテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getRelCommonLabelQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM RelCommonLabelBean"); //$NON-NLS-1$
    }


    /**
     * rel_common_labelテーブル取得Query(LABEL_ID、DIC_ID、LABEL_ID)
     * 
     * @param labelId
     *            LABEL_ID
     * @return Query(単一レコード)
     */
    @Deprecated
    public static Query getRelCommonLabelQuery(int labelId) {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM RelCommonLabelBean WHERE labelId=:LBL_ID").setInteger("LBL_ID", labelId); //$NON-NLS-1$ //$NON-NLS-2$
    }


    /**
     * rel_common_labelテーブル取得Query
     * (DIC_ID、ITEM_ID=dic_common.ITEM_ID、INACTIVE=dic_common.ITEM_ID)
     * 
     * @param dicId
     *            DIC_ID
     * @return Query(複数レコード)
     */
    @Deprecated
    public static Query getRelCommonLabelQueryIncludeDicCommon(int dicId) {
        Session session = IoService.getInstance().getSession();
        Query query = session
                .createQuery("SELECT a.* FROM RelCommonLabelBean AS a,DicCommonBean AS b WHERE a.dicId=:DIC_ID AND a.itemId=b.itemId AND b.inactive=false"); //$NON-NLS-1$
        return query.setInteger("DIC_ID", dicId); //$NON-NLS-1$
    }


    /**
     * rel_common_labelテーブル取得Query(LABEL_ID、DIC_ID、LABEL_ID)
     * 
     * @param labelId
     *            LABEL_ID
     * @param itemId
     *            ITEM_ID
     * @return Query(単一レコード)
     */
    public static Query getRelCommonLabelQuery(int labelId, int itemId) {
        Session session = IoService.getInstance().getSession();
        return session
                .createQuery("FROM RelCommonLabelBean WHERE labelId=:LBL_ID AND itemId=:ITEM_ID").setInteger("LBL_ID", labelId).setInteger("ITEM_ID", itemId); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }


    /**
     * rel_common_labelテーブル取得Query(LABEL_ID、DIC_ID、LABEL_ID)
     * 
     * @param labelId
     *            LABEL_ID
     * @param dicId
     *            DIC_ID
     * @param itemId
     *            ITEM_ID
     * @return Query(単一レコード)
     */
    public static Query getRelCommonLabelQuery(int labelId, int dicId, int itemId) {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM RelCommonLabelBean WHERE labelId=:LBL_ID AND dicId=:DIC_ID AND itemId=:ITEM_ID").setInteger("LBL_ID", labelId) //$NON-NLS-1$ //$NON-NLS-2$
                .setInteger("DIC_ID", dicId).setInteger("ITEM_ID", itemId); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // *****************************************//
    // rel_flucテーブル
    // *****************************************//

    /**
     * rel_flucテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getRelFlucQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM RelFlucBean"); //$NON-NLS-1$
    }


    // *****************************************//
    // rel_prj_clmテーブル
    // *****************************************//

    /**
     * rel_prj_clmテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getRelPrjClmQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM RelPrjClmBean"); //$NON-NLS-1$
    }


    /**
     * rel_prj_clmテーブル取得Query(TABLE_ID)
     * 
     * @param tableId
     *            TABLE_ID
     * 
     * @return Query
     */
    public static Query getRelPrjClmQuery(int tableId) {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM RelPrjClmBean WHERE primaryKey.tableId=:TBL_ID").setInteger("TBL_ID", tableId); //$NON-NLS-1$ //$NON-NLS-2$
    }


    /**
     * rel_prj_clmテーブル取得Query(TABLE_ID)
     * 
     * @param tableId
     * 
     * @param projectId
     * @return Query
     */
    public static Query getRelPrjClmQuery(int tableId, int projectId) {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM RelPrjClmBean WHERE primaryKey.tableId=:TBL_ID AND primaryKey.projectId=:PRJ_ID").setInteger("TBL_ID", tableId) //$NON-NLS-1$ //$NON-NLS-2$
                .setInteger("PRJ_ID", projectId); //$NON-NLS-1$
    }


    // *****************************************//
    // rel_prj_dicテーブル
    // *****************************************//

    /**
     * rel_prj_dicテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getRelPrjDicQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM RelPrjDicBean"); //$NON-NLS-1$
    }


    /**
     * projectテーブル取得Query(PROJECT_ID)
     * 
     * @param projectId
     *            PROJECT_ID
     * @return Query
     */
    @Deprecated
    public static Query getRelPrjDicQuery(int projectId) {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM RelPrjDicBean WHERE primaryKey.projectId=:PRJ_ID").setInteger("PRJ_ID", projectId); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // *****************************************//
    // rel_prj_productテーブル
    // *****************************************//

    /**
     * rel_prj_productテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getRelPrjProductQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM RelPrjProductBean"); //$NON-NLS-1$
    }


    /**
     * rel_prj_productテーブル取得Query(PROJECT_ID、PRODUCT_ID)
     * 
     * @param projectId
     *            PROJECT_ID
     * @param productId
     *            PRODUCT_ID
     * @return Query
     */
    public static Query getRelPrjProductQuery(int projectId, int productId) {
        Session session = IoService.getInstance().getSession();
        return session
                .createQuery("FROM RelPrjProductBean WHERE primaryKey.projectId=:PRJ_ID AND primaryKey.productId=:PRD_ID").setInteger("PRJ_ID", projectId) //$NON-NLS-1$ //$NON-NLS-2$
                .setInteger("PRD_ID", productId); //$NON-NLS-1$
    }


    // *****************************************//
    // rel_product_dicテーブル
    // *****************************************//

    /**
     * rel_product_dicテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getRelProductDicQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM RelProductDicBean"); //$NON-NLS-1$
    }


    // *****************************************//
    // rel_synonymテーブル
    // *****************************************//

    /**
     * rel_synonymテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getRelSynonymQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM RelSynonymBean"); //$NON-NLS-1$
    }


    // *****************************************//
    // synonym_tblテーブル
    // *****************************************//

    /**
     * synonym_tblテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getSynonymTblQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM RelSynonymBean"); //$NON-NLS-1$
    }


    // *****************************************//
    // tablesテーブル
    // *****************************************//

    /**
     * tablesテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getTablesQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM TablesBean"); //$NON-NLS-1$
    }


    /**
     * tablesテーブル取得Query(DBNAME)
     * 
     * @param tableName
     *            DBNAME
     * @return Query
     */
    public static Query getTablesQuery(String tableName) {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM TablesBean WHERE dbname=:DB_NAME").setString("DB_NAME", tableName); //$NON-NLS-1$ //$NON-NLS-2$
    }


    /**
     * tablesテーブル取得Query(TYPE)
     * 
     * @param type
     *            TYPE
     * @return Query
     */
    public static Query getTablesQuery(int type) {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM TablesBean WHERE type=:TYPE").setInteger("TYPE", type); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // *****************************************//
    // template_patternテーブル
    // *****************************************//

    /**
     * tamplate_patternテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getTemplatePatternQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM TemplatePatternBean"); //$NON-NLS-1$
    }


    // *****************************************//
    // type_patternテーブル
    // *****************************************//

    /**
     * type_patternテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    public static Query getTypePatternQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM TypePatternBean"); //$NON-NLS-1$
    }


    /**
     * type_patternテーブル取得Query(NAME)
     * 
     * @param name
     *            NAME
     * @return Query
     */
    public static Query getTypePatternQuery(String name) {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM TypePatternBean WHERE name=:NAME").setString("NAME", name); //$NON-NLS-1$ //$NON-NLS-2$
    }


    /**
     * type_patternテーブル取得Query(ID)
     * 
     * @param id
     *            ID
     * @return Query
     */
    public static Query getTypePatternQuery(int id) {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM TypePatternBean WHERE id=:ID").setInteger("ID", id); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // *****************************************//
    // usr_claim_xxxテーブル
    // *****************************************//

    /**
     * usr_claim_xxxテーブル作成用SQLQuery
     * 
     * @param definePath
     *            xxx.sqlファイルパス
     * @param tableName
     *            作成テーブル名
     * @return String SQL
     */
    public static SQLQuery createUsrClaimTableSQLQuery(String definePath, String tableName) {
        BufferedReader br = null;
        String line;
        StringBuilder strSQL = new StringBuilder(200);
        try {
            br = new BufferedReader(new FileReader(definePath));

            while ((line = br.readLine()) != null) {
                strSQL.append(line);
            }

            /* テーブル名を置換 */
            int idx = strSQL.indexOf("("); //$NON-NLS-1$
            if (idx > -1) {
                strSQL.delete(0, idx);
                strSQL.insert(0, " ").insert(0, tableName).insert(0, ' ').insert(0, "CREATE TABLE"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            SQLQuery retValue = IoService.getInstance().getSession().createSQLQuery(strSQL.toString());
            return retValue;
            // return
            // IoService.getInstance().getSession().createSQLQuery(strSQL.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            /* ファイルを閉じる */
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * usr_claim_xxxテーブルに問い合わせファイルの内容をLoadするSQLQuery
     * 
     * @param path
     *            問い合わせデータファイルパス
     * @param tableName
     *            テーブル名
     * @param strColumns
     *            カラム
     * @param headFlg
     *            先頭行見出し
     * @return String SQL
     */
    public static SQLQuery loadDataInUsrClaimTableSQLQuery(String path, String tableName, String strColumns, boolean headFlg) {
        /* ファイルパスの"\"を"\\"へ変更 */
        path = path.replace("\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$

        /* SQL生成 */
        StringBuilder strSQL = new StringBuilder(250);

        strSQL.append("INSERT INTO ").append(tableName); //$NON-NLS-1$
        strSQL.append(" (").append(strColumns).append(") "); //$NON-NLS-1$ //$NON-NLS-2$
        strSQL.append(" SELECT ").append(strColumns).append(" FROM CSVREAD('").append(path).append("', "); //$NON-NLS-1$ //$NON-NLS-2$
        if (!headFlg) {
            // 先頭行を見出しとしない場合
            strSQL.append("'").append(strColumns).append("', "); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            // 先頭行を見出しとする場合
            strSQL.append("null, "); //$NON-NLS-1$
        }
        strSQL.append("null, null, ").append(" '\"\"' ").append(")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        SQLQuery retValue = null;
        retValue = IoService.getInstance().getSession().createSQLQuery(strSQL.toString());
        return retValue;
    }


    // *****************************************//
    // usr_cm_xxxテーブル
    // *****************************************//

    /**
     * usr_cm_xxxテーブル作成用SQLQuery
     * teblesテーブルにもレコードを追加
     * 
     * @param tableName
     *            登録テーブル
     * @param type
     *            クレンジングタイプ
     * @param date
     *            更新日時
     * @return int
     */
    @Deprecated
    public static int createUsrCmTableSQLQuery(String tableName, TableType type, Date date) {
        /* トランザクション開始 */
        Session session = IoService.getInstance().getSession();
        session.beginTransaction();
        try {
            /* テーブルリストに登録(誤記補正) */
            TablesBean table = new TablesBean();
            table.setName(TableType.CORRECTION_MISTAKES_DATA.toString());
            table.setDbname(tableName);
            table.setType(TableType.CORRECTION_MISTAKES_DATA.getIntValue());
            table.setLasted(date);
            IoService.getInstance().getSession().save(table);
            IoService.getInstance().getSession().flush();
            table = (TablesBean) getTablesQuery(tableName).uniqueResult();
            if (table == null) {
                throw new HibernateException("Failed to add '" + tableName + "' table."); //$NON-NLS-1$ //$NON-NLS-2$
            }
            /* usr_cm_xxxテーブル作成 */
            StringBuilder strSQL = new StringBuilder().append("CREATE TABLE ").append(tableName) //$NON-NLS-1$
                    .append("WORK_ID INT NOT NULL, FLD_ID INT NOT NULL, REC_ID INT NOT NULL, "); //$NON-NLS-1$
            if (TableType.CORRECTION_MISTAKES_DATA.equals(type)) {
                // USR_CM_の場合
                strSQL.append("DATA MEDIUMTEXT, ").append("PRIMARY KEY (WORK_ID, FLD_ID, REC_ID));"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                // USR_WORK_を作成する場合、履歴IDフィールドを持たせる。
                strSQL.append("HISTORY_ID INT, "); //$NON-NLS-1$
                strSQL.append("DATA MEDIUMTEXT, ").append("PRIMARY KEY (WORK_ID, FLD_ID, REC_ID, HISTORY_ID));"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            int result = IoService.getInstance().getSession().createSQLQuery(strSQL.toString()).executeUpdate();
            if (result == 0)
                return result;

            // 　usr_cm_xxxテーブルへIndexを追加
            result = addIndexToUsrCmTableSQLQuery(tableName).executeUpdate();
            if (result == 0)
                return result;

            /* トランザクションコミット */
            session.getTransaction().commit();

            return result;
        } finally {
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
        }
    }


    /**
     * usr_claim_xxxテーブルへIndex追加するSQLQueryを作成
     * 
     * @param tableName
     *            対象テーブル名
     * @return SQLQuery
     */
    private static SQLQuery addIndexToUsrCmTableSQLQuery(String tableName) {
        StringBuilder strCreateIndex = new StringBuilder().append("CREATE INDEX "); //$NON-NLS-1$
        strCreateIndex.append(tableName).append("_INDEX ON ").append(tableName).append("(WORK_ID, HISTORY_ID, REC_ID ASC);"); //$NON-NLS-1$ //$NON-NLS-2$
        return IoService.getInstance().getSession().createSQLQuery(strCreateIndex.toString());
    }


    // *****************************************//
    // usr_work_xxxテーブル
    // *****************************************//

    /**
     * usr_work_xxxテーブル作成用SQLQuery
     * 
     * @param tableName
     *            作成テーブル名
     * @param date
     *            更新日時
     * @param projectId
     * @param productId
     * @param claimId
     * @param relId
     * @param tgts
     * @return String SQL
     */
    @Deprecated
    public static int createUsrWorkTableQuery(String tableName, Date date, int projectId, int productId, int claimId, int workId, int relId, Set<Integer> tgts) {
        int result = 0;
        /* トランザクション開始 */
        Session session = IoService.getInstance().getSession();
        session.beginTransaction();
        try {
            /* テーブルリストに登録(誤記補正) */
            TablesBean table = new TablesBean();
            table.setName(TableType.RESULT_DATA.toString());
            table.setDbname(tableName);
            table.setType(TableType.RESULT_DATA.getIntValue());
            table.setLasted(date);
            IoService.getInstance().getSession().save(table);
            IoService.getInstance().getSession().flush();
            table = (TablesBean) getTablesQuery(tableName).uniqueResult();
            if (table == null) {
                throw new HibernateException("Failed to add '" + tableName + "' table."); //$NON-NLS-1$ //$NON-NLS-2$
            }

            /* usr_work_xxxテーブル作成 */
            StringBuilder createSql = new StringBuilder(128);
            createSql.append("CREATE TABLE ").append(tableName); //$NON-NLS-1$
            createSql.append("(WORK_ID INT NOT NULL,").append("FLD_ID INT NOT NULL,"); //$NON-NLS-1$ //$NON-NLS-2$
            createSql.append("HISTORY INT NOT NULL,").append("REC_ID INT NOT NULL,"); //$NON-NLS-1$ //$NON-NLS-2$
            createSql.append("PATTERN_ID INT NOT NULL,").append("HIT_INFO MEDIUMTEXT NOT NULL)"); //$NON-NLS-1$ //$NON-NLS-2$
            result = IoService.getInstance().getSession().createSQLQuery(createSql.toString()).executeUpdate();
            if (result == 0)
                return result;

            result = addIndexToUsrWorkTableSQLQuery(tableName).executeUpdate();
            if (result == 0)
                return result;

            /* rel_clm_productへ紐付けを追加 */
            RelClmProductBean relClmProduct = new RelClmProductBean();
            relClmProduct.getPrimaryKey().setProjectId(projectId);
            relClmProduct.getPrimaryKey().setProductId(productId);
            relClmProduct.getPrimaryKey().setTableId(claimId);
            if (workId != 0)
                relClmProduct.setWorkTableId(workId);
            if (relId != 0)
                relClmProduct.setRelTableId(relId);

            IoService.getInstance().getSession().save(relClmProduct);
            IoService.getInstance().getSession().flush();

            /* トランザクションコミット */
            session.getTransaction().commit();
            return result;
        } finally {
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
        }
    }


    /**
     * usr_work_xxxテーブルへIndex追加するSQLQueryを作成
     * 
     * @param tableName
     *            対象テーブル名
     * @return SQLQuery
     */
    private static SQLQuery addIndexToUsrWorkTableSQLQuery(String tableName) {
        StringBuilder strCreateIndex = new StringBuilder().append("CREATE INDEX "); //$NON-NLS-1$
        strCreateIndex.append(tableName).append("_INDEX ON ").append(tableName).append("(WORK_ID, HISTORY, REC_ID ASC);"); //$NON-NLS-1$ //$NON-NLS-2$
        return IoService.getInstance().getSession().createSQLQuery(strCreateIndex.toString());
    }


    // *****************************************//
    // workdatasテーブル
    // *****************************************//

    /**
     * workdatasテーブル取得Query(条件なし)
     * 
     * @return Query
     */
    @Deprecated
    public static Query getWorkdatasQuery() {
        Session session = IoService.getInstance().getSession();
        return session.createQuery("FROM WorkdatasBean"); //$NON-NLS-1$
    }


    /**
     * workdatasテーブル取得Query(条件なし)
     * 
     * @param projectId
     * @param productId
     * @param claimId
     * @param type
     * @return Query
     */
    public static Query getWorkdatasQuery(int projectId, int productId, int claimId, ClaimWorkDataType type) {
        StringBuilder selectSql = new StringBuilder(128);
        selectSql.append("FROM WorkdatasBean WHERE projectId=:PRJ_ID AND inputTableId=:INP_TBL_ID"); //$NON-NLS-1$

        if (productId > 0) {
            selectSql.append(" AND productId=:PRD_ID"); //$NON-NLS-1$
        }
        if (ClaimWorkDataType.NONE.equals(type)) {
            selectSql.append(" AND type<>:TYPE"); //$NON-NLS-1$
        } else {
            selectSql.append(" AND type=:TYPE"); //$NON-NLS-1$
        }
        Session session = IoService.getInstance().getSession();
        Query query = session.createQuery(selectSql.toString());
        query.setInteger("PRJ_ID", projectId).setInteger("INP_TBL_ID", claimId); //$NON-NLS-1$ //$NON-NLS-2$
        if (productId > 0) {
            query.setInteger("PRD_ID", productId); //$NON-NLS-1$
        }
        if (ClaimWorkDataType.NONE.equals(type)) {
            query.setInteger("TYPE", ClaimWorkDataType.CORRECTION_MISTAKES.getIntValue()); //$NON-NLS-1$
        } else {
            query.setInteger("TYPE", type.getIntValue()); //$NON-NLS-1$
        }
        return query;
    }
}
