/**
 * @version $Id: AbstractWidget.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/02/02
 * @author KMorishima
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona.internal.ui.component;


/**
 * エラー検出と通知も行ってくれるコンポーネントを作るときの親クラス
 * 
 * @author KMorishima
 * 
 */
public abstract class AbstractWidget extends ErrorHandler {

    /**
     * どこにフォーカスを当てるか
     */
    public abstract void setFocus();


    /**
     * 必要なdispose処理をする
     */
    public abstract void dispose();

}
