/**
 * @version $Id: DictionaryNameValidator.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/07 15:35:58
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.util;

import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.dialogs.IInputValidator;

/**
 * @author kousuke-morishima
 */
public class DictionaryNameValidator {

    /**
     * @param parent
     *            辞書作成先の親。データベース内など親がない場合はnull
     * @param name
     *            検査する辞書名。
     * @return 問題なければnull。問題があればエラーメッセージを返す。
     */
    public static String isValid(IContainer parent, String name) {
        if (!StringUtil.isValidFileName(name)) {
            return "作成できないファイル名です。";
        }

        if (name.length() > ValidateUtil.MAX_LENGTH_DICTIONARY_NAME) {
            return ("辞書名は" + ValidateUtil.MAX_LENGTH_DICTIONARY_NAME + "文字以内です。");
        }

        /**
         * ファイルとしては保存できるが、DB に登録できないため
         */
        if (name.indexOf("'") != -1) {
            return "シングルクォートは辞書名に入力できません。";
        }

        int result = ValidateUtil.isValidDictionaryName(parent, name);
        if ((result & ValidateUtil.DUPLICATE_DB) != 0) {
            return name + "はデータベースにすでに存在します。";
        } else if ((result & ValidateUtil.DUPLICATE_WS) != 0) {
            return name + "はワークスペースにすでに存在します。";
        }
        return null;
    }


    /**
     * @param parent
     *            辞書作成先の親。データベース内など親がない場合はnull
     * @return
     */
    public static IInputValidator getDictionaryNameValidator(final IContainer parent) {
        return new IInputValidator() {
            @Override
            public String isValid(String newText) {
                return DictionaryNameValidator.isValid(parent, newText);
            }
        };
    }
}
