/**
 * @version $Id: CoronaDicPri.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/13 14:23:46
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate.hql;

import com.tida_okinawa.corona.io.model.ICoronaDicPri;


/**
 * @author yukihiro-kinjyo
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
     *            辞書のプライオリティ（#1301）
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


    /* #1301 辞書のプライオリティ取得 */
    @Override
    public int getDicPri() {
        return this.dicPri;
    }


    /* #1301 辞書IDの設定 */
    @Override
    public void setDicId(int dicId) {
        this.dicId = dicId;
    }
}
