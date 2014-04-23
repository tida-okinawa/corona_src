/**
 * @version $Id: VerifyImpl.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/04/13
 * @author KMorishima
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona.internal.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

/**
 * 入力された文字を適用した後の文字列が正しい書式になっているかチェックする
 * 
 * @author KMorishima
 * 
 */
public class VerifyImpl implements VerifyListener {
    private TextProvider provider;
    private Match match;


    /**
     * {@link #verifyText(VerifyEvent)}
     * ですでに入力されている文字列を取得したいが、何にキャストしていいか判断できないので、自前インタフェースから値を取ることにした。
     * 
     * @param provider
     *            検査対象のウィジェットの内容を返す処理を実装したクラス
     * @param regex
     *            入力を許す文字列を表す正規表現
     */
    public VerifyImpl(TextProvider provider, String regex) {
        this.provider = provider;
        match = new Match(regex);
    }


    @Override
    public void verifyText(VerifyEvent e) {
        int keyCode = e.keyCode;
        if (keyCode == SWT.BS || keyCode == SWT.DEL) {
            // nothing to do
        } else {
            // 変更後の文字列を作る
            String text = e.text; // 入力文字列
            String str = provider.getText(); // 変更前の文字列
            int start = e.start;
            int end = e.end;
            str = str.substring(0, start) + text + str.substring(end, str.length());

            if (str.length() > 0) {
                match.setTarget(str);
                e.doit = match.find();
            }
        }
    }

    public static interface TextProvider {
        /**
         * チェックするUIの内容を返す。ユーザ入力適用前の値であること。
         * 
         * @return
         */
        public String getText();
    }
}
