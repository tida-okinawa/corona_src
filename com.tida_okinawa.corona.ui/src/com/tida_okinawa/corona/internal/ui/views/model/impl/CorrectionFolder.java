/**
 * @version $Id: CorrectionFolder.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/22 15:40:15
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model.impl;

import com.tida_okinawa.corona.internal.ui.views.model.ICorrectionFolder;

/**
 * 中間データをまとめておくフォルダ
 * 
 * @author kousuke-morishima
 */
public class CorrectionFolder extends CoronaFolder implements ICorrectionFolder {

    CorrectionFolder(String name) {
        super(name);
    }

}
