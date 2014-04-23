/**
 * @version $Id: AbstractCleansing.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/09/11 10:53:23
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;


/**
 * @author kousuke-morishima
 * 
 */
public abstract class AbstractCleansing {
    private boolean isVoid;
    private Object returnObject;


    /**
     * 
     * @param monitor
     *            進捗ダイアログ
     * @param method
     *            処理するメソッドを指定する文字列
     * @param args
     *            処理するメソッドに渡す引数
     * @throws InvocationTargetException
     *             何らかの実行例外
     * @throws InterruptedException
     *             割り込み
     */
    public abstract void run(IProgressMonitor monitor, String method, String... args) throws InvocationTargetException, InterruptedException;


    /**
     * 指定されたメソッド名がこのクラスで処理できるかどうか判定する.
     * 
     * @param method
     *            判定したいメソッド名
     * @param args
     *            引数
     * @return true:処理可能、false:処理不可
     */
    public abstract boolean isProcessable(String method, String... args);


    /**
     * {@link #run(IProgressMonitor, String, String...)}で実行したメソッドの戻り値を返す。
     * {@link #isVoid()} がtrueを返すとき、このメソッドは正確な値を返さない。
     * 
     * @return {@link #run(IProgressMonitor, String, String...)}の実行結果
     */
    public Object getReturnObject() {
        return returnObject;
    }


    /**
     * {@link #run(IProgressMonitor, String, String...)}で実行したメソッドの戻り値を設定する。
     * 
     * @param returnObject
     *            実行結果
     */
    protected void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }


    /**
     * {@link #run(IProgressMonitor, String, String...)}
     * で実行したメソッドの戻り値がvoidかどうかを返す。
     * 
     * @return voidならtrue
     */
    public boolean isVoid() {
        return isVoid;
    }


    /**
     * {@link #run(IProgressMonitor, String, String...)}の実行結果がvoidかどうかを設定する
     * 
     * @param isVoid
     *            voidであればtrue
     */
    protected void setVoid(boolean isVoid) {
        this.isVoid = isVoid;
    }
}
