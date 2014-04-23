/**
 * @version $Id: CoronaDicPri.java 1141 2013-09-17 07:43:56Z yukihiro-kinjo $
 *
 * 2013/10/28 10:21:30
 * @author yukihiro-kinjo
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate;

import com.tida_okinawa.corona.io.model.ICoronaDicPri;

/**
 * 辞書優先度の操作クラス。
 * 
 * @author yukihiro-kinjo
 */
public class CoronaDicPri implements ICoronaDicPri {

    private int dicId;
    private boolean inactive;
    private int dicPri;


    /**
     * @param dicId
     */
    public CoronaDicPri(int dicId) {
        this.dicId = dicId;
    }


    /**
     * @param dicId
     * @param inactive
     * @param dicPri
     *            辞書のプライオリティ
     */
    public CoronaDicPri(int dicId, boolean inactive, int dicPri) {
        this.dicId = dicId;
        this.inactive = inactive;
        this.dicPri = dicPri;
    }


    @Override
    public int getDicId() {
        return dicId;
    }


    @Override
    public boolean isInActive() {
        return inactive;
    }


    @Override
    public void setInActive(boolean inactive) {
        this.inactive = inactive;
    }


    /* 辞書のプライオリティ取得 */
    @Override
    public int getDicPri() {
        return this.dicPri;
    }


    /* 辞書IDの設定 */
    @Override
    public void setDicId(int dicId) {
        this.dicId = dicId;
    }
}
