/**
 * @version $Id: DictionaryPriorityUtil.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/01/11 11:20:07
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.util;

import java.util.Map;
import java.util.Map.Entry;

import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.internal.ui.views.model.IUILibrary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProject;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDependDic;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;

/**
 * @author shingo-takahashi
 */
public class DictionaryPriorityUtil {
    /**
     * 辞書のプライオリティ追加
     * 
     * @param uiDic
     */
    public static void addDicPriority(IUIDictionary uiDic) {
        ICoronaDic dic = uiDic.getObject();
        int dicId = uiDic.getId();

        if (uiDic.getParent().getParent() instanceof IUIProject) {
            IUIProject prj = (IUIProject) uiDic.getParent().getParent();
            /* プロジェクトに紐づくターゲットのリストを取得 */
            Map<Integer, String> prdList = prj.getObject().getWorkData(prj.getId());
            for (Entry<Integer, String> entry : prdList.entrySet()) {
                if (entry.getKey() == 0) {
                    /* クレンジング未実行 */
                    break;
                }
                String[] strs = entry.getValue().split("/", -1); /* idとtypeを分ける */
                String[] ids = strs[0].split(",", -1); /* idを配列へ格納 */
                String[] types = strs[1].split(",", -1); /* typeを配列へ格納 */

                for (int i = 0; i < ids.length; i++) {
                    int id = Integer.parseInt(ids[i]);
                    int type = Integer.parseInt(types[i]);
                    /* IDと辞書IDをキーに既にプライオリティが存在するかチェック */
                    boolean flg = prj.getObject().getDicPriority(id, dicId);
                    if (!flg) {
                        /* IDに対する辞書がプライオリティテーブルに存在しない場合TYPEと辞書種別をもとにプライオリティを追加 */
                        if (ClaimWorkDataType.valueOf(type) == ClaimWorkDataType.DEPENDENCY_STRUCTURE && dic instanceof IUserDic) {
                            prj.getObject().addDicPriority(id, dicId);
                        } else if (ClaimWorkDataType.valueOf(type) == ClaimWorkDataType.CORRECTION_SYNONYM && dic instanceof IDependDic) {
                            prj.getObject().addDicPriority(id, dicId);
                        } else if (ClaimWorkDataType.valueOf(type) == ClaimWorkDataType.RESLUT_PATTERN
                                && (dic instanceof IUserDic || dic instanceof ILabelDic || dic instanceof IPatternDic)) {
                            prj.getObject().addDicPriority(id, dicId);
                        }
                    }
                }

            }
        } else if (uiDic.getParent().getParent() instanceof IUIProduct) {
            /* ターゲットへの辞書追加の場合 */
            IUIProduct prd = (IUIProduct) uiDic.getParent().getParent();
            /* ターゲットが紐づいているプロジェクトのリストを取得 */
            Map<Integer, String> prjList = prd.getObject().getWorkData(prd.getId());
            for (Entry<Integer, String> entry : prjList.entrySet()) {
                if (entry.getKey() == 0) {
                    /* クレンジング未実行 */
                    break;
                }
                String[] strs = entry.getValue().split("/", -1); /* idとtypeを分ける */
                String[] ids = strs[0].split(",", -1); /* idを配列へ格納 */
                String[] types = strs[1].split(",", -1); /* typeを配列へ格納 */

                for (int i = 0; i < ids.length; i++) {
                    int id = Integer.parseInt(ids[i]);
                    int type = Integer.parseInt(types[i]);

                    /* IDと辞書IDをキーに既にプライオリティが存在するかチェック */
                    boolean flg = prd.getObject().getDicPriority(id, dicId);
                    if (!flg) {
                        /* IDに対する辞書がプライオリティテーブルに存在しない場合TYPEと辞書種別をもとにプライオリティを追加 */
                        if (ClaimWorkDataType.valueOf(type) == ClaimWorkDataType.DEPENDENCY_STRUCTURE && dic instanceof IUserDic) {
                            prd.getObject().addDicPriority(id, dicId);
                        } else if (ClaimWorkDataType.valueOf(type) == ClaimWorkDataType.CORRECTION_SYNONYM && dic instanceof IDependDic) {
                            prd.getObject().addDicPriority(id, dicId);
                        } else if (ClaimWorkDataType.valueOf(type) == ClaimWorkDataType.RESLUT_PATTERN
                                && (dic instanceof IUserDic || dic instanceof ILabelDic || dic instanceof IPatternDic)) {
                            prd.getObject().addDicPriority(id, dicId);
                        }
                    }
                }
            }
        }
    }


    /**
     * 辞書のプライオリティ削除
     * 
     * @param lib
     * @param uiDic
     */
    public static void deleteDicPriority(IUILibrary lib, IUIDictionary uiDic) {
        /* IDを取得 */
        int dicId = uiDic.getId();
        int id = lib.getId();
        if (lib instanceof IUIProject) {
            IUIProject prj = (IUIProject) uiDic.getParent().getParent();
            // プロジェクトに紐づくターゲットIDとWORKDATASのIDを取得
            Map<Integer, String> prdList = prj.getObject().getWorkData(id);
            for (Entry<Integer, String> entry : prdList.entrySet()) {
                if (entry.getKey() == 0) {
                    break;
                }
                // ターゲットと辞書が紐づけられているかチェック
                boolean flg = prj.getObject().getRelation(entry.getKey(), dicId);
                if (!flg) {
                    String[] strs = entry.getValue().split("/", -1);
                    prj.getObject().delDicPriority(strs[0], dicId);
                }

            }
        } else if (lib instanceof IUIProduct) {
            IUIProduct prd = (IUIProduct) lib;
            /* ターゲットに紐づくプロジェクトIDとWORKDATASのIDを取得 */
            Map<Integer, String> prjList = prd.getObject().getWorkData(id);
            for (Entry<Integer, String> entry : prjList.entrySet()) {
                if (entry.getKey() == 0) {
                    break;
                }
                // プロジェクトと辞書が紐づけられているかチェック
                boolean flg = prd.getObject().getRelation(entry.getKey(), dicId);
                if (!flg) {
                    String[] strs = entry.getValue().split("/", -1);
                    prd.getObject().delDicPriority(strs[0], dicId);
                }
            }
        }
    }
}
