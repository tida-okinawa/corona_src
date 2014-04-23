/**
 * @version $Id: Frequent.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/08 14:40:18
 * @author kenta-uechi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.frequent;

import java.util.Collection;

import com.tida_okinawa.corona.correction.morphem.SyntaxStructure;
import com.tida_okinawa.corona.io.model.MorphemeElement;

/**
 * 頻出後カウント処理
 * 
 * @author kenta-uechi, imai
 */
public class Frequent {
    /**
     * 頻出カウンタ
     */
    FrequentCounter ft;


    /**
     * コンストラクター
     */
    public Frequent() {
        ft = new FrequentCounter();
    }


    /**
     * @param text
     *            Juman/KNPの結果(同義語補正
     */
    public void count(String text) {
        // 形態素ごとに分割
        SyntaxStructure ss = new SyntaxStructure(text);
        for (MorphemeElement me : ss.getMorphemeElemsnts()) {
            ft.count(me);
        }
    }


    /**
     * @return 頻出用語カウント結果
     */
    public Collection<FrequentRecord> getRecords() {
        return ft.getRecords();
    }

}
