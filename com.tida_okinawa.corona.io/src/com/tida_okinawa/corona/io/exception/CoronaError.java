/**
 * @version $Id: CoronaError.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/02 14:48:33
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.exception;

/**
 * Coronaで発生したエラーを扱う.
 * 
 * @author shingo-takahashi
 */
public class CoronaError extends Error {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private int id;


    /**
     * コンストラクタ
     * 
     * @param id
     *            エラーID
     */
    public CoronaError(int id) {
        super();
        this.id = id;
    }


    /**
     * コンストラクタ
     * 
     * @param message
     *            エラーメッセージ
     * @param cause
     *            exception
     * @param id
     *            エラーID
     */
    public CoronaError(String message, Throwable cause, int id) {
        super(message, cause);
        this.id = id;
    }


    /**
     * コンストラクタ
     * 
     * @param message
     *            エラーメッセージ
     * @param id
     *            エラーID
     */
    public CoronaError(String message, int id) {
        super(message);
        this.id = id;
    }


    /**
     * コンストラクタ
     * 
     * @param cause
     *            exception
     * @param id
     *            エラーID
     */
    public CoronaError(Throwable cause, int id) {
        super(cause);
        this.id = id;
    }


    /**
     * このエラーのエラーIDを返す
     * 
     * @return エラーID
     */
    public int getId() {
        return id;
    }
}
