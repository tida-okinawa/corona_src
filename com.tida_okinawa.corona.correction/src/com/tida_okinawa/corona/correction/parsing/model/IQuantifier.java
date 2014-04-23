/**
 * @version $Id: IQuantifier.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/02/24 19:31:52
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;

import com.tida_okinawa.corona.io.model.MorphemeElement;


/**
 * @author kousuke-morishima
 */
public interface IQuantifier {

    /**
     * @return 設定されている数量子。not null
     */
    public QuantifierType getQuant();


    /**
     * @param quant
     *            設定する数量子
     */
    public void setQuant(QuantifierType quant);


    /**
     * @return ヒットした単語。may be null
     */
    public MorphemeElement getHitElement();


    /**
     * @param hitElement
     *            ヒットした単語。
     */
    public void setHitElement(MorphemeElement hitElement);
}
