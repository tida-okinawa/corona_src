/**
 * @version $Id: ISyntaxStructureElement.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/02 20:34:11
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.morphem;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;

import com.tida_okinawa.corona.io.model.MorphemeElement;

/**
 * 文節/句による文構造
 * 
 */
public interface ISyntaxStructureElement extends IAdaptable {
    /**
     * IDを取得
     * 
     * @return この文節/句の文節番号
     */
    int getIndex();


    /**
     * この文節/句を構成する形態素リストを取得
     * 
     * @return この文節/句を形成する形態素リスト
     */
    List<MorphemeElement> getMorphemes();


    /**
     * この文節/句の表記を取得
     * 
     * @return この文節/句の表記
     */
    String getHyouki();


    /**
     * この文節/句の読みを取得
     * 
     * @return この文節/句の読み
     */
    String getYomi();


    /**
     * この文節/句の係り先を取得
     * 
     * @return この文節/句の係り先
     */
    ISyntaxStructureElement getDependDestination();


    /**
     * この文節/句の係り先を取得
     * 
     * @param type
     *            "D", "P", null 両方
     * @return この文節/句の係り先
     */
    ISyntaxStructureElement getDependDestination(String type);


    /**
     * この文節/句の係り元を取得
     * 
     * @return この文節/句の係り元リスト
     */
    List<ISyntaxStructureElement> getDependSources();


    /**
     * この文節/句の係り元を取得
     * 
     * @param type
     *            "D", "P", null 両方
     * @return この文節/句の係り元リスト
     */
    List<ISyntaxStructureElement> getDependSources(String type);

}
