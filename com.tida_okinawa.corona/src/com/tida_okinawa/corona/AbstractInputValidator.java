/**
 * @version $Id: AbstractInputValidator.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/01/18 19:45:49
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona;

import java.io.UnsupportedEncodingException;

import org.eclipse.jface.dialogs.IInputValidator;

/**
 * InputDialogの入力チェックを行う
 * 
 * @author kousuke-morishima
 */
public abstract class AbstractInputValidator implements IInputValidator {
    /**
     * 半角換算するときに使用する文字コードをMS932（全角1文字で2バイト）とする。
     */
    public AbstractInputValidator() {
        setCharset(Messages.AbstractInputValidator_encodeType);
    }


    @Override
    public String isValid(String newText) {
        if (newText == null) {
            return ""; //$NON-NLS-1$
        }

        String message = null;
        if (!allowBlank) {
            message = isBlank(newText);
            if (message != null) {
                return message;
            }
        }

        message = isLimitOver(newText);
        if (message != null) {
            return message;
        }

        return null;
    }

    /* ****************************************
     * 空文字を許すか
     */
    private boolean allowBlank = false;


    /**
     * 空文字を許すかどうかを設定
     * 
     * @param allow
     *            空文字を許容するならtrue
     */
    public void setAllowBlank(boolean allow) {
        this.allowBlank = allow;
    }


    /**
     * 空文字を許すかどうかを取得
     * 
     * @return 空文字を許容しているならtrue
     */
    public boolean isAllowBlank() {
        return allowBlank;
    }


    /**
     * @param str
     *            入力文字列
     * @return 入力文字が空文字だったらメッセージ
     */
    protected String isBlank(String str) {
        if ("".equals(str)) { //$NON-NLS-1$
            return Messages.AbstractInputValidator_labelNonValue;
        }
        return null;
    }


    /* ****************************************
     * 文字列長の制限
     */
    /**
     * textが指定の文字列長を超えているか検査する
     * 
     * @param text
     *            検査する文字列
     * @return 制限文字数を超えていればメッセージ。そうでなければnull
     */
    protected String isLimitOver(String text) {
        boolean invalid = false;
        int limit = getLimit();
        if (limit > 0) {
            if (text.length() > limit) {
                invalid = true;
            }
        }

        boolean halfInvalid = false;
        int limitOfHalf = getLimitOfHalf();
        if (limitOfHalf > 0) {
            try {
                if (text.getBytes(getCharset()).length > limitOfHalf) {
                    halfInvalid = true;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                halfInvalid = true;
            }
        }

        if (invalid || halfInvalid) {
            StringBuilder message = new StringBuilder(128).append(Messages.AbstractInputValidator_labelLengthOver);
            if (limit > 0) {
                message.append(getLimit()).append(Messages.AbstractInputValidator_labelLengthLimit);
            }
            if (limitOfHalf > 0) {
                message.append(Messages.AbstractInputValidator_labelByteLengthLimit).append(getLimitOfHalf())
                        .append(Messages.AbstractInputValidator_labelLengthLimit);
            }
            return message.toString();
        }
        return null;
    }

    private int limit = 0;


    /**
     * @return 最大文字列長。0なら、制限してない
     */
    public int getLimit() {
        return limit;
    }


    /**
     * @param limit
     *            最大文字列長。0にすると文字列長を制限しない。
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    private int limitOfHalf = 0;


    /**
     * @return 半角文字換算での最大文字列長。0なら、制限していない。
     */
    public int getLimitOfHalf() {
        return limitOfHalf;
    }


    /**
     * @param limitOfHalf
     *            最大半角文字列長。0にすると文字列長を制限しない。
     */
    public void setLimitOfHalf(int limitOfHalf) {
        this.limitOfHalf = limitOfHalf;
    }

    private String charset;


    /**
     * @return 判定に使用している文字コード
     */
    public String getCharset() {
        return charset;
    }


    /**
     * @param charset
     *            入力文字列を半角換算するときに使用する文字コード
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }
}
