/**
 * @version $Id: UserDicInputCheck.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/22 14:10:36
 * @author takayuki-matsumoto
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.user;

import com.tida_okinawa.corona.correction.erratum.Erratum;
import com.tida_okinawa.corona.io.model.dic.ITerm;

/**
 * @author takayuki-matsumoto
 */
public class UserDicInputCheck {

    public static boolean isError(ITerm term) {
        /* 単語　空欄チェック　 */
        if (term.getValue().equals("")) {
            return true;
        }
        /* 単語　64文字チェック　 */
        if (term.getValue().length() >= 64) {
            return true;
        }

        /* 品詞　空欄チェック　 */
        // 品詞は空欄にできない

        /* 品詞詳細　空欄チェック　 */
        // 品詞に対する品詞詳細がある場合は空欄にできない
        // 品詞によっては空欄の場合がある

        Erratum erratum = new Erratum();
        /* 単語　全角変換時のIllegalWordあり */
        erratum.convert(term.getValue());
        if (erratum.hasIllegalWords()) {
            return true;
        }
        /* 読み　全角変換時のIllegalWordあり */
        erratum.convert(term.getReading());
        if (erratum.hasIllegalWords()) {
            return true;
        }

        return false;
    }
}
