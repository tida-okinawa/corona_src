/**
 * @version $Id: ClaimWorkDataTagAlignedExportHandler.java 1842 2014-04-21 04:50:18Z yukihiro-kinjyo $
 * 
 * 2012/02/02 17:01:09
 * @author Shingo-Takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.resultpattern.export;

import com.tida_okinawa.corona.internal.ui.actions.AbstractResultPatternExportActionDelegate;
import com.tida_okinawa.corona.ui.handlers.AbstractClaimWorkDataExportHandler;


public class ClaimWorkDataTagAlignedExportHandler extends AbstractClaimWorkDataExportHandler {

    /**
     * 構文解析結果を出力するアクションクラスを取得する。
     * 
     * @return 構文解析結果出力アクションクラス
     */
    @Override
    protected AbstractResultPatternExportActionDelegate getResultPatternExportAction() {
        return new ResultPatternTagAlignedExportActionDelegate();
    }
}
