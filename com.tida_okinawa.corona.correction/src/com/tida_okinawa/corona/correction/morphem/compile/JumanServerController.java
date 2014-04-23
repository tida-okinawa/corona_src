/**
 * @version $Id: JumanServerController.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/21 20:00:08
 * @author yoshikazu-imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.morphem.compile;

import com.tida_okinawa.corona.correction.morphem.preference.MorphemePreference;

/**
 * @author yoshikazu-imai
 * 
 */
public class JumanServerController extends ServerController {
    /**
     * サーバーススクリプト
     */
    final static String SCRIPT = "jumanserver";


    @Override
    protected String[] getHostNames() {
        return MorphemePreference.getJumanHostNames();
    }


    @Override
    protected String getStartCmd() {
        return SCRIPT + " start";
    }


    @Override
    protected String getStopCmd() {
        return SCRIPT + " stop";
    }


    @Override
    protected String getRestartCmd() {
        return SCRIPT + " restart";
    }
}
