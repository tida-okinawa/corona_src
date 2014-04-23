/**
 * @version $Id: IResultCoronaPattern.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 13:42:03
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;

import com.tida_okinawa.corona.io.model.dic.IPattern;

/**
 * 構文解析結果（レコードID、解析対象の原文、ヒットしたパターン、ヒットした位置）を保持する
 * 
 * @author shingo-takahashi
 */
public interface IResultCoronaPattern extends IAdaptable {
    /**
     * @return レコードID
     */
    public int getRecordId();


    /**
     * @return 解析対象の原文
     */
    public String getText();


    /**
     * このレコードに関連するすべてのラベルを返す。
     * TODO ヒットしたパターンに関連するものだけを返すのが正しいのでは？
     * 
     * @return ラベルの一覧
     */
    public String[] getLabels();


    /**
     * usr_work_xxx_yyyのデータ列に保存されている全文を返す.<br/>
     * 解析対象の原文は {@link #getText()}から取得できる。
     * 
     * @return 解析結果のデータ
     */
    public String getData();


    /**
     * 履歴を指定して、レコードにヒットしたパターンと、そのヒット位置を返す.<br/>
     * 指定した履歴で、該当のレコードにヒット情報がない場合、nullを返す。
     * 最新の情報を取得する場合、0を指定する。
     * 
     * @param history
     *            この履歴のヒット情報を返す。最新の情報を取得したい場合、0を指定する。
     * 
     * @return ヒットしたパターンと、そのヒット位置
     */
    Map<IPattern, List<String>> getHitPositions(int history);
}
