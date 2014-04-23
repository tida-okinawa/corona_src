/**
 * @version $Id: LabelNameInputValidator.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/15 16:04:27
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import com.tida_okinawa.corona.AbstractInputValidator;

/**
 * 
 * @author kyohei-miyazato
 */
public class LabelNameInputValidator extends AbstractInputValidator {
    @Override
    public String isValid(String newText) {
        String message = super.isValid(newText);
        if (message != null) {
            return message;
        }

        if (!isValidString(newText)) {
            return "'(シングルクォート)、/(半角スラッシュ)、\\(バックスラッシュ)、|(パイプ)は入力できません。";
        }

        return null;
    }


    /* ********************
     * 文字数制限
     */

    @Override
    public int getLimit() {
        /* DB定義は40文字だけど */
        return 30;
    }


    /** @return '(シングルクォート)が入力されていない時　true */
    private static boolean isValidString(String str) {
        return (str.matches("[^'/\\\\|]*"));
    }
}
