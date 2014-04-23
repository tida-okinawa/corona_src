/**
 * @version $Id: IVariableTemplate.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/12/26 11:32:09
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.template;

/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 *         要素の状態の判定（固定or可変）
 *         ※単語（Word）か参照（Label）のみ有効
 */
public interface IVariableTemplate {

    /**
     * 固定かどうかを設定
     * 
     * @param check
     *            true: 固定<br/>
     *            false: 可変
     */
    public void setFixCheck(boolean check);


    /**
     * 固定かどうかを取得
     * 
     * @return true: 固定<br/>
     *         false: 可変
     */
    public boolean getFixCheck();
}
