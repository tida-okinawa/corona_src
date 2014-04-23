/**
 * @version $Id: CreateClaimTableValidator.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/21 10:23:01
 * @author yukihiro-kinjo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.views.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IInputValidator;

import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.table.TableType;
import com.tida_okinawa.corona.io.util.CoronaIoUtils;

/**
 * データベースに登録するテーブルをチェックする
 * 
 * @author yukihiro-kinjo
 */
public class CreateClaimTableValidator implements IInputValidator {

    private Map<String, IClaimData> tableNameMap;
    private static final int TABLE_NAME_LIMIT = 30;


    /**
     * コンストラクター
     */
    public CreateClaimTableValidator() {

        tableNameMap = new HashMap<String, IClaimData>();
        List<IClaimData> registeredClaims = IoActivator.getService().getClaimDatas();
        for (IClaimData claim : registeredClaims) {
            tableNameMap.put(claim.getTableName().toUpperCase(), claim);
        }
    }


    @Override
    public String isValid(String testTableName) {
        if (testTableName != null) {
            if (!blankCheck(testTableName)) {
                if (limitCheck(testTableName)) {
                    if (stringCheck(testTableName)) {
                        return null;
                    }
                    return "記号は[ \"_\" ]のみ登録できます。その他記号、空白は登録できません。 ";
                }
                return "問い合わせデータ名は" + TABLE_NAME_LIMIT + "文字以内です。";
            }
            return "問い合わせデータ名を入力してください。";
        }
        return null;
    }


    private static boolean blankCheck(String check) {
        /* 入力文字が空文字だったらtrue */
        return (check.equals(""));
    }


    private static boolean limitCheck(String check) {
        /* テーブル長は64バイト。prefixを考慮して30文字まで */
        return (check.length() <= TABLE_NAME_LIMIT);
    }


    private static boolean stringCheck(String check) {
        /* 入力文字に、「かな,カナ,英数字,漢字,_　」が入るとtrue */
        return (check.matches("[[ぁ-ゖ][ァ-ヺ][ａ-ｚＡ-Ｚ][一-龻][0-9０-９][a-zA-Z][_ー]]*"));
    }


    /**
     * 指定したテーブル名がデーターベースに存在するか確認する
     * 
     * @param tableName
     *            確認するテーブル名
     * @return 存在している場合false
     */
    public boolean exists(String tableName) {
        if (tableNameMap.get(CoronaIoUtils.createWorkTableName(tableName, TableType.CLAIM_DATA, 0).toUpperCase()) != null) {
            return true;
        }
        return false;
    }
}
