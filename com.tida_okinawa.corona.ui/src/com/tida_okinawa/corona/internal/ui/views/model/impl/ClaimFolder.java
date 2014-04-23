/**
 * @version $Id: ClaimFolder.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/22 15:44:41
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model.impl;

import com.tida_okinawa.corona.internal.ui.CoronaConstants;
import com.tida_okinawa.corona.internal.ui.views.model.IClaimFolder;

/**
 * 問い合わせデータをまとめておくフォルダ。プロジェクトの直下にいる
 * 
 * @author kousuke-morishima
 */
public class ClaimFolder extends CoronaFolder implements IClaimFolder {

    public ClaimFolder() {
        super(CoronaConstants.CLAIM_FOLDER_NAME);
    }
}
