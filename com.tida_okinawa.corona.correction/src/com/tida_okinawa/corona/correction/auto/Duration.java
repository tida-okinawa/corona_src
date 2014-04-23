/**
 * @version $Id: Duration.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/15 17:51:40
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.auto;

/**
 * 自動実行スケジュールを行う間隔を表す列挙クラス
 * 
 * @author kousuke-morishima
 */
public enum Duration {

    /** 毎日実行することを表す */
    DAYLY(Messages.Duration_everyDay),
    /** 毎週実行することを表す */
    WEEKLY(Messages.Duration_everyWeek),
    /** 毎月実行することを表す */
    MONTHLY(Messages.Duration_everyMonth);

    private String value;


    /**
     * @param value
     *            表示名
     */
    private Duration(String value) {
        this.value = value;
    }


    /**
     * @return 表示名
     */
    public String getValue() {
        return this.value;
    }

}
