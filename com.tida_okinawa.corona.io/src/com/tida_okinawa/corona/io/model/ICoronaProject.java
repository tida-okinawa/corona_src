/**
 * @version $Id: ICoronaProject.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/08/03
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * プロジェクトインターフェース
 * 
 * @author shingo-takahashi
 * 
 */
public interface ICoronaProject extends ICoronaDics {

    /**
     * 係り受け解析を行うかどうかをプロジェクトごとに指定する列挙型
     */
    public enum ProjectKnpConfig {

        /** ワークスペースの値を引き継ぐ設定 */
        INHERIT(0, "ワークスペースの設定を引き継ぐ"),

        /** KNP解析を必ず行う設定 */
        DO(1, "解析を行う"),

        /** KNP解析は行わない設定 */
        NOT_DO(2, "解析を行わない");

        private int key;
        private String disp;


        private ProjectKnpConfig(int key, String disp) {
            this.key = key;
            this.disp = disp;
        }


        /**
         * @return この設定の数値
         */
        public int getKey() {
            return this.key;
        }


        /**
         * この設定値を指定した時に表示する文字列
         * 
         * @return 表示文字列
         */
        public String getDisp() {
            return this.disp;
        }


        /**
         * 設定可能な値の一覧を返す.
         * 
         * @return 設定可能な一覧
         */
        public static List<ProjectKnpConfig> getConfigList() {
            List<ProjectKnpConfig> list = new ArrayList<ProjectKnpConfig>();
            list.add(INHERIT);
            list.add(DO);
            list.add(NOT_DO);
            return list;
        }


        /**
         * 設定可能な値の表示文字列の一覧を返す.
         * 
         * @return 設定可能な値の表示文字列の一覧
         */
        public static String[] getConfigDescriptions() {
            List<ProjectKnpConfig> list = getConfigList();
            String[] ret = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                ret[i] = list.get(i).getDisp();
            }
            return ret;
        }


        /**
         * 指定した key を持つ設定(INHERIT,DO,NOT_DO) を返す
         * 
         * @param key
         * @return INHERIT,DO,NOT_DO のいずれか。key がどの値も示していない場合、INHERIT を返す
         */
        public static ProjectKnpConfig get(int key) {
            for (ProjectKnpConfig config : getConfigList()) {
                if (config.getKey() == key) {
                    return config;
                }
            }
            return INHERIT;
        }
    }


    /**
     * ID取得を取得する
     * 
     * @return ID
     */
    public abstract int getId();


    /**
     * プロジェクト名を取得する
     * 
     * @return プロジェクト名
     */
    public abstract String getName();


    /**
     * このプロジェクトに属するターゲットの一覧を返す.
     * 
     * @return ターゲット一覧
     */
    public abstract List<ICoronaProduct> getProducts();


    /**
     * このプロジェクトにターゲットを追加する
     * 
     * @param product
     *            追加するターゲット
     * @return 追加に成功したらtrue
     */
    public abstract boolean addProduct(ICoronaProduct product);


    /**
     * このプロジェクトからターゲットを除去する
     * TODO 戻り値をIStatusにする
     * 
     * @param product
     *            除去するターゲット
     */
    public abstract void removeProduct(ICoronaProduct product);


    /**
     * このプロジェクトに関連している問い合わせデータを取得する
     * 
     * @return 問い合わせデータの一覧
     */
    public abstract List<IClaimData> getClaimDatas();


    /**
     * 問い合わせデータ追加
     * 
     * @param id
     */
    public abstract void addClaimData(int id);


    /**
     * 問い合わせデータ破棄
     * 
     * @param id
     */
    public abstract void removeClaimData(int id);


    /**
     * @param iClaimData
     */
    public abstract void addClaimData(IClaimData iClaimData);


    /**
     * プロジェクト名称設定
     * 
     * @param name
     */
    public abstract void setName(String name);


    /**
     * プロジェクトID設定
     * 
     * @param id
     */
    public abstract void setId(int id);


    /**
     * プロジェクトに対する係り受け解析設定を保存
     * 
     * @param config
     */
    public abstract void saveKnpConfig(ProjectKnpConfig config);


    /**
     * プロジェクトに対する係り受け解析設定を取得
     * 
     * @return Knp指定情報を返す
     */
    public abstract ProjectKnpConfig getKnpConfig();


    /**
     * ターゲットと辞書の紐づけチェック
     * 
     * @param id
     *            プライオリティID
     * @param dicId
     *            辞書ID
     * @return ターゲットと辞書の紐付けが取得できたら、true
     */
    public abstract boolean getRelation(int id, int dicId);


    /**
     * プライオリティ削除
     * 
     * @param strIds
     *            プライオリティID
     * @param dicId
     *            辞書ID
     * @return プライオリティの削除ができたら、true
     */
    public abstract boolean delDicPriority(String strIds, int dicId);


    /**
     * 中間データ取得
     * 
     * @param id
     *            プライオリティID
     * @return 中間データ取得結果のマップ
     */
    public abstract Map<Integer, String> getWorkData(int id);


    /**
     * 辞書プライオリティ取得
     * 
     * @param id
     *            プライオリティID
     * @param dicId
     *            辞書ID
     * @return プライオリティテーブルからデータが取得できたら、true
     */
    public abstract boolean getDicPriority(int id, int dicId);


    /**
     * 辞書プライオリティ追加
     * 
     * @param id
     *            プライオリティID
     * @param dicId
     *            辞書ID
     */
    public abstract void addDicPriority(int id, int dicId);

}
