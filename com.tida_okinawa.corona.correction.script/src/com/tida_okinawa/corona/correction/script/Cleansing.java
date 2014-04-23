/**
 * @version $Id: Cleansing.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/02 13:07:27
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.script;

import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.dam.hibernate.IoService;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;

/**
 * クレンジング実行をコンソールから行うためのクラスの基底クラス
 * 
 * @author kousuke-morishima
 */
public abstract class Cleansing {
    /** 正常終了 */
    public static final int CODE_OK = 0;
    /** InterruptedException が発生したときのエラーコード */
    public static final int ERROR_CODE_INTERRUPTED = 2;
    /** その他の Exception が発生した時のエラーコード */
    public static final int ERROR_CODE_ANY_EXCEPTION = 3;
    /** データベースへの接続ができない時のエラーコード */
    public static final int ERROR_CODE_NO_DATABASE_CONNECTION = 4;

    protected IoService service;

    private final boolean allowSystemExit;


    /**
     * @return エラー発生時、<code>System.exit(int)</code>を呼び出す場合、true
     */
    public final boolean isAllowSystemExit() {
        return this.allowSystemExit;
    }


    /**
     * スタンドアロンで起動するプログラムの場合のコンストラクタ。
     * 引数のチェックをする。
     * <p>
     * エラー発生時、<code>System.exit(int)</code>を呼び出す
     * </p>
     * 
     * @param args
     *            引数
     * @throws IllegalArgumentException
     *             渡された引数が検査に引っかかった
     */
    public Cleansing(String[] args) {
        this(true);
        int error = check(args);
        if (error != CODE_OK) {
            errorExit(error, getErrorMessage(error));
        }
    }


    /**
     * Coronaから呼び出す場合のコンストラクタ。
     * 何もしない。
     * 
     * @param allowSystemExit
     *            エラーが起きた時、<code>System.exit(int)</code>を呼び出すかどうか
     */
    public Cleansing(boolean allowSystemExit) {
        this.allowSystemExit = allowSystemExit;
    }


    /**
     * {@link #isAllowSystemExit()}がtrueを返す場合、エラーメッセージをコンソールに出力して、JavaVMを落とす。<br/>
     * falseを返す場合、エラーメッセージをコンソールに出力して、{@link IllegalStateException}を投げる。
     * 
     * @param errorCode
     *            エラーコード
     * @param errorMessage
     *            エラーメッセージ
     */
    protected final void errorExit(int errorCode, String errorMessage) {
        System.err.println(errorMessage);
        if (allowSystemExit) {
            System.exit(errorCode);
        } else {
            throw new IllegalStateException(errorCode + ":" + errorMessage); //$NON-NLS-1$
        }
    }


    /**
     * 引数の検査を行い、エラーコードを返す。
     * 正常な引数であれば 0 を返し、エラーがあれば 1 ～ 255 のいずれかを返す
     * 
     * @param args
     *            コマンド実行時引数
     * @return エラーコード
     */
    abstract int check(String[] args);


    /**
     * 処理を中断した理由を返却する
     * 
     * @param errorCode
     *            エラーコード
     * @return 処理の中断理由のメッセージ。該当するエラーメッセージがない場合はnull
     */
    String getErrorMessage(int errorCode) {
        switch (errorCode) {
        case ERROR_CODE_ANY_EXCEPTION:
            return Messages.AutoCleansing_ErrorMessage_UnExpectedErrorOccurred;
        case ERROR_CODE_INTERRUPTED:
            return Messages.AutoCleansing_ErrorMessage_InterruptedOccurred;
        case ERROR_CODE_NO_DATABASE_CONNECTION:
            return Messages.AutoCleansing_ErrorMessage_NoDatabaseConnection;
        default:
            return null;
        }
    }


    /**
     * DB接続しているIoServiceを返す。
     * 
     * @param url
     *            DB接続URL
     * @param user
     *            DB接続ユーザ名
     * @param pass
     *            DB接続パスワード
     * @return DB接続しているIoService。接続に失敗したらnull
     */
    protected IoService createService(String url, String user, String pass) {
        if (service == null) {
            service = (IoService) IoActivator.getService();
        }
        if (!service.isConnect()) {
            if (!service.connect(url, user, pass)) {
                return null;
            }
        }
        return service;
    }


    /**
     * 該当するICoronaProductを返す。
     * 
     * @param project
     *            プロジェクト
     * @param targetName
     *            ターゲット名
     * @return ターゲットのインスタンス。一致するものがなければnull
     */
    static ICoronaProduct searchProduct(ICoronaProject project, String targetName) {
        for (ICoronaProduct product : project.getProducts()) {
            if (product.getName().equals(targetName)) {
                return product;
            }
        }
        return null;
    }


    /**
     * 該当するICoronaProjectを返す。
     * 
     * @param projectName
     *            プロジェクト名
     * @return プロジェクトのインスタンス。一致するものがなければnull
     */
    ICoronaProject searchProject(String projectName) {
        for (ICoronaProject project : service.getProjects()) {
            if (project.getName().equals(projectName)) {
                return project;
            }
        }
        return null;
    }
}
