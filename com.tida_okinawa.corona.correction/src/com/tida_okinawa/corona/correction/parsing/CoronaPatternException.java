/**
 * @version $Id: CoronaPatternException.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/02/21 11:03:21
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing;

/**
 * 構文パターン例外
 * 
 * @author kousuke-morishima
 */
public class CoronaPatternException extends Exception {

    private static final long serialVersionUID = -6506478396863243963L;

    /**
     * 構文パターンのエラーメッセージ定義
     */
    public enum PatternError {
        /**
         * 解析対象の辞書の中に、解析できないパターンが含まれています
         */
        ERROR_PATTERN(8100, "解析対象の辞書の中に、解析できないパターンが含まれています。"),

        /**
         * パターンの中身がありません
         */
        NO_CONTENTS(8101, "パターンの中身がありません。"),

        /**
         * 参照先のパターンが見つかりません
         */
        BLANK_LINK(8102, "参照先のパターンが見つかりません。"),

        /**
         * 参照パターンが循環しています
         */
        LOOP_LINK(8103, "参照パターンが循環しています。"),

        /**
         * 空の単語があります
         */
        TERM_EMPTY(8104, "空の単語があります。"),

        /**
         * 子がありません
         */
        NO_CHILD(8105, "子がありません。"),

        ;


        private PatternError(int code, String message) {
            this.code = code;
            this.message = message;
        }

        private int code;


        /**
         * @return error code
         */
        public int getCode() {
            return code;
        }

        private String message;


        /**
         * @return error message
         */
        public String getMessage() {
            return message;
        }


        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder(256);
            buf.append(getCode()).append(" : ").append(getMessage());
            return buf.toString();
        }
    }


    public CoronaPatternException(PatternError errorType) {
        this(errorType, null);
    }


    public CoronaPatternException(PatternError errorType, Throwable cause) {
        super(errorType.getMessage(), cause);
        this.errorType = errorType;
    }

    private PatternError errorType;


    /**
     * パターン例外のエラータイプを取得する
     * 
     * @return エラータイプ
     */
    public PatternError getErrorType() {
        return errorType;
    }
}
