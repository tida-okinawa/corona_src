/**
 * @version $Id: ClaimWorkDataExportHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/02/02 17:01:09
 * @author miyaguni
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.handlers;

import com.tida_okinawa.corona.internal.ui.actions.AbstractResultPatternExportActionDelegate;
import com.tida_okinawa.corona.internal.ui.actions.ResultPatternEditorActionDelegate;

/**
 * @author miyaguni
 */
public class ClaimWorkDataExportHandler extends AbstractClaimWorkDataExportHandler {

    /**
     * 構文解析結果をCSV出力するアクションクラスを取得する。
     * 
     * @return 構文解析結果出力アクションクラス
     */
    @Override
    protected AbstractResultPatternExportActionDelegate getResultPatternExportAction() {
        return new ResultPatternEditorActionDelegate();
    }
}
