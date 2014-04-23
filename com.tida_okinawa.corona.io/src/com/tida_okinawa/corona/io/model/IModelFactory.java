/**
 * @version $Id: IModelFactory.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/19 12:52:54
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model;

/**
 * モデル作成クラス
 * 
 * @author shingo-takahashi
 */
public interface IModelFactory {


    /**
     * 指定された名前でプロジェクトを作成する。
     * 
     * @param name
     *            作成するプロジェクト名
     * @return 同名のプロジェクトがある場合は、その既存プロジェクト。
     */
    public abstract ICoronaProject createProject(String name);


    /**
     * 指定されたプロジェクトに、ターゲットを作成する。
     * 
     * @param name
     *            作成するターゲット名
     * @param parent
     *            親プロジェクト
     * @return 作成したターゲット。同名のターゲットがある場合はnull
     */
    public abstract ICoronaProduct createProduct(String name, ICoronaProject parent);


    /**
     * 問い合わせ中間データ生成
     * 
     * @param claimId
     *            対応する問い合わせデータ
     * @param fieldId
     *            作成するフィールドのID
     * @param type
     *            作成する中間データの種別
     * @param projectId
     *            作成先のプロジェクトID
     * @param productId
     *            作成先のターゲットID
     * @return 指定された中間データオブジェクト
     */
    public abstract IClaimWorkData createClaimWorkData(int claimId, int fieldId, ClaimWorkDataType type, int projectId, int productId);


    /**
     * パターンの結果
     * TODO 構文解析結果しか作らないんだから、typeは引数にもらう必要がない
     * 
     * @param claimId
     *            対応する問い合わせデータ
     * @param fieldId
     *            作成するフィールドのID
     * @param type
     *            作成する中間データの種別
     * @param projectId
     *            作成先のプロジェクトID
     * @param productId
     *            作成先のターゲットID
     * @return 構文解析結果の中間データオブジェクト
     */
    public abstract IClaimWorkPattern createClaimWorkPattern(int claimId, int fieldId, ClaimWorkDataType type, int projectId, int productId);


    /**
     * 頻出結果データ生成
     * 
     * @param claimId
     *            対応する問い合わせデータ
     * @param fieldId
     *            作成するフィールドのID
     * @param projectId
     *            作成先のプロジェクトID
     * @param productId
     *            作成先のターゲットID
     * @return 頻出用語抽出結果の中間データオブジェクト
     */
    public abstract IClaimWorkFAData createClaimFAData(int claimId, int fieldId, int projectId, int productId);
}
