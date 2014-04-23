/**
 * @version $Id: ClaimWorkFAData.java 968 2013-03-05 12:25:25Z kousuke-morishima $
 * 
 * 2011/10/06 10:51:48
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate;

import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.CorrectionMistakesType;
import com.tida_okinawa.corona.io.model.IClaimWorkFAData;

/**
 * @author shingo-takahashi, imai
 */
public class ClaimWorkFAData extends ClaimWorkData implements IClaimWorkFAData {


    /**
     * @param claimId
     *            問い合わせデータID
     * @param fieldId
     *            フィールドID
     * @param projectId
     *            プロジェクトID
     * @param productId
     *            ターゲットID
     */
    public ClaimWorkFAData(int claimId, int fieldId, int projectId, int productId) {
        super(claimId, fieldId, ClaimWorkDataType.FREQUENTLY_APPERING, projectId, productId);
    }


    /**
     * @deprecated use {@link #getClaimWorkDatas()}
     * @see com.tida_okinawa.corona.io.dam.mysql.ClaimWorkData#getClaimWorkDatas()
     */
    @Deprecated
    @Override
    public String getClaimWorkData(int recordId) {
        throw new UnsupportedOperationException("未サポートメソッド"); //$NON-NLS-1$
    }


    @Deprecated
    @Override
    public boolean isExternalCorrectionMistakes() {
        throw new UnsupportedOperationException("未サポートメソッド"); //$NON-NLS-1$
    }


    @Deprecated
    @Override
    public boolean isInternalCorrectionMistakes() {
        throw new UnsupportedOperationException("未サポートメソッド"); //$NON-NLS-1$
    }


    @Deprecated
    @Override
    public boolean setCorrectionMistakesType(CorrectionMistakesType type) {
        throw new UnsupportedOperationException("未サポートメソッド"); //$NON-NLS-1$
    }


    @Override
    public void clear() {

        _records.clear();
    }
}
