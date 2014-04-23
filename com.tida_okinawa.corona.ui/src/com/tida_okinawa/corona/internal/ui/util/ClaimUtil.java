/**
 * @version $Id: ClaimUtil.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/15 14:14:34
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.util;

import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;

/**
 * @author kousuke-morishima
 */
public class ClaimUtil {

    /**
     * 該当する問合せデータのフィールド名を取得する
     * 
     * @param claimId
     * @param fieldId
     * @return 取得できなかったら空文字
     */
    public static String getFieldName(int claimId, int fieldId) {
        String ret = "";
        IClaimData claim = IoActivator.getService().getClaimData(claimId);
        if (claim != null) {
            IFieldHeader field = claim.getFieldInformation(fieldId);
            if (field != null) {
                ret = field.getDispName();
            }
        }
        return ret;
    }


    /**
     * @param claimId
     * @return
     */
    public static String getClaimName(int claimId) {
        IClaimData claim = IoActivator.getService().getClaimData(claimId);
        return claim.getName();
    }
}
