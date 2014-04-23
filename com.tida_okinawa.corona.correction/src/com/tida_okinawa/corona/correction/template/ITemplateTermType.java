/**
 * @version $Id: ITemplateTermType.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/12/26 15:31:04
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.template;

/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 *         単語（Word）のひな型用
 */
public interface ITemplateTermType {

    /** 単語（Word）の場合 */
    public final String TYPE_WORD = Messages.TEMPLATE_TERM_TYPE_WORD;
    /** ラベル（Label）の場合 */
    public final String TYPE_LABEL = Messages.TEMPLATE_TERM_TYPE_LABEL;
    /** 未設定の場合 */
    public final String TYPE_NULL = Messages.TEMPLATE_TERM_TYPE_NULL;


    /**
     * 単語かどうかを設定
     * 
     * @param type
     *            状態が可変で「単語」（Word）の場合はISWORD、<br/>
     *            状態が可変で「ラベル」（Label）の場合はISLABEL、<br/>
     *            それ以外ではnull<br/>
     *            ※状態が可変の場合のみ有効
     */
    public void setState(String type);


    /**
     * 単語かどうかを取得
     * 
     * @return 状態が可変で「単語」（Word）の場合はISWORD、<br/>
     *         状態が可変で「ラベル」（Label）の場合はISLABEL、<br/>
     *         それ以外ではnull<br/>
     *         ※状態が可変の場合のみ有効
     */
    public String getState();

}
