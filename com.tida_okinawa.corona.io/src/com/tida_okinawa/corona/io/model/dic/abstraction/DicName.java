/**
 * @version $Id: DicName.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/09 16:51:05
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic.abstraction;

import com.tida_okinawa.corona.io.model.dic.IDicName;

/**
 * @author s.takuro
 *         #177 パターン自動化（係り受け抽出）
 */
public class DicName implements IDicName {

    private int dicId = 0;
    private String dicName = null;
    private boolean inActive = true;


    /**
     * 辞書名を管理する
     * 
     * @param id
     *            辞書ID
     * @param name
     *            辞書名
     */
    public DicName(int id, String name, boolean inActive) {
        this.dicId = id;
        this.dicName = name;
        this.inActive = inActive;
    }


    @Override
    public int getDicId() {
        return dicId;
    }


    @Override
    public String getDicName() {
        return dicName;
    }


    @Override
    public boolean isInActive() {
        return inActive;
    }
}
