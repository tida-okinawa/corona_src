/**
 * @version $Id: ProjectNameValidator.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/07 15:34:12
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.util;

import org.eclipse.jface.dialogs.IInputValidator;


/**
 * @author kousuke-morishima
 */
public class ProjectNameValidator {
    /**
     * @param name
     *            検査するプロジェクト名
     * @return 問題がなければnull。問題があればエラーメッセージを返す。
     */
    public static String isValid(String name) {
        if (!StringUtil.isValidFileName(name)) {
            return "作成できないプロジェクト名です。";
        }

        if (name.length() > ValidateUtil.MAX_LENGTH_PROJECT_NAME) {
            return "プロジェクト名称は" + ValidateUtil.MAX_LENGTH_PROJECT_NAME + "文字以内です。";
        }

        /**
         * フォルダとしては保存できるが、DB に登録できないため
         */
        if (name.indexOf("'") != -1) {
            return "シングルクォートはプロジェクト名に入力できません。";
        }

        int result = ValidateUtil.isValidProjectName(name);
        if ((result & ValidateUtil.DUPLICATE_DB) != 0) {
            return name + "はデータベースに存在します。(大文字・小文字は区別されません)";
        } else if ((result & ValidateUtil.DUPLICATE_WS) != 0) {
            return name + "はワークスペースに存在します。(大文字・小文字は区別されません)";
        }
        return null;
    }

    private static IInputValidator projectNameValidator = new IInputValidator() {
        @Override
        public String isValid(String newText) {
            return ProjectNameValidator.isValid(newText);
        }
    };


    public static IInputValidator getProjectNameValidator() {
        return projectNameValidator;
    }
}
