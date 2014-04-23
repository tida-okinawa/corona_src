/**
 * @version $Id: ICoronaPatternParser.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 11:52:05
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing;

import com.tida_okinawa.corona.io.model.IResultCoronaPattern;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;

/**
 * 構文解析パーサの実装用インタフェース
 * 
 * @author shingo-takahashi
 */
public interface ICoronaPatternParser {
    /**
     * パターンと照合する
     * 
     * メソッド単位でスレッドセーフ
     * 
     * @param target
     *            係り受け解析結果
     * @return パターン照合結果オブジェクト
     */
    IResultCoronaPattern parsing(String target);


    /**
     * 辞書を登録
     * 
     * @param dic
     *            ICoronaDicを実装した辞書オブジェクト
     */
    void addDic(ICoronaDic dic);


    /**
     * パターンマップ数を取得
     * 
     * @return パターンマップ数
     */
    Integer getPatternMapSize();


    /**
     * 構文解析で複数Hitを行うかどうか
     * 
     * @param isMalti
     *            複数Hitを行うならtrue
     */
    void setMaltiHit(boolean isMalti);

}
