/**
 * @version $Id: AutoCleansingUtil.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/28 14:19:02
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.script;

import java.util.List;

import com.tida_okinawa.corona.common.StringUtil;
import com.tida_okinawa.corona.io.dam.hibernate.ModelFactory;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;

/**
 * @author kousuke-morishima
 * 
 */
public class AutoCleansingUtil {

    /**
     * 問い合わせデータのターゲット列にあるすべてのターゲットをプロジェクトに登録する。<br/>
     * 問い合わせデータを新規登録することを前提とする。
     * 
     * @param project
     *            登録先プロジェクト
     * @param claim
     *            登録する問い合わせデータ
     * @param miningFields
     *            マイニング対象フィールド
     */
    public static void registerProject(ICoronaProject project, IClaimData claim, List<IFieldHeader> miningFields) {
        project.addClaimData(claim);
        List<String> targetNames = claim.getProducts();
        for (String targetName : targetNames) {
            String safeTargetName = StringUtil.convertValidFileName(targetName);
            ICoronaProduct target = ModelFactory.getInstance().createProduct(safeTargetName, project);
            if (target == null) {
                /* 新規登録が前提だが、デバッグ用に登録済みの場合の処理を入れておく */
                for (ICoronaProduct t : project.getProducts()) {
                    if (t.getName().equals(targetName)) {
                        target = t;
                        break;
                    }
                }
            }
            project.addProduct(target);
            for (IFieldHeader field : miningFields) {
                target.addMiningField(claim.getId(), field.getId());
            }
        }
    }
}
