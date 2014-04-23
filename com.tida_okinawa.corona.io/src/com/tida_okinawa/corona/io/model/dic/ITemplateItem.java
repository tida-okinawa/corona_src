/**
 * @version $Id: ITemplateItem.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/22 11:49:20
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic;

import com.tida_okinawa.corona.io.model.ICoronaObject;

/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 */
public interface ITemplateItem extends ICoronaObject {
    /**
     * 未保存のひな型のID
     */
    public static int UNSAVED_ID = 0;

    /**
     * ひな型のTEMPLATE_IDのデフォルト値<br/>
     * TEMPLATE_ID>0の場合、パターンIDが格納される（参照の要素）
     */
    public static int DEFAULT_ID = 0;


    /**
     * @param id
     *            ひな型管理用ID
     */
    public void setId(int id);


    /**
     * @return _id ひな型管理用ID
     */
    public int getId();


    /**
     * @param templateId
     *            ひな型ID
     */
    public void setTemplateId(int templateId);


    /**
     * @return ひな型ID
     */
    public int getTemplateId();


    /**
     * @param text
     *            ひな型名
     */
    public void setName(String text);


    /**
     * @return _name ひな型名
     */
    public String getName();


    /**
     * @param text
     *            ひな型情報
     */
    public void setText(String text);


    /**
     * @return _text ひな型情報
     */
    public String getText();


    /**
     * @param patternType
     *            パターン分類.パターン分類ID
     */
    public void setPatternType(int patternType);


    /**
     * @return _patternType パターン分類.パターン分類ID
     */
    public int getPatternType();


    /**
     * @param parts
     *            参照可否フラグ
     */
    public void setParts(boolean parts);


    /**
     * @return _parts 参照可否フラグ
     */
    public boolean isParts();


    /**
     * @param inActive
     *            アクティブかどうか
     */
    public void setInActive(boolean inActive);


    /**
     * @return _inActive アクティブかどうか
     */
    public boolean isInActive();


    /**
     * @param dirty
     *            ダーティ・フラグ（更新が必要かどうかを判定）
     */
    public void setDirty(boolean dirty);


    /**
     * @return dirty ダーティ・フラグ（更新が必要かどうかを判定）
     */
    public boolean isDirty();
}
