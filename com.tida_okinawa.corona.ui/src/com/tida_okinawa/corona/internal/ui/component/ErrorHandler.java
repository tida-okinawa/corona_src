/**
 * @version $Id: ErrorHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/02/02
 * @author KMorishima
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona.internal.ui.component;

import org.eclipse.core.runtime.ListenerList;

public abstract class ErrorHandler implements IErrorHandler {
    /**
     * エラーが起きていないことを示すエラー種別。
     */
    public static final int NO_ERROR = 0;

    /**
     * このクラス内で起きたエラーを保持するフィールド
     */
    protected final ErrorInfo errorInfo = new ErrorInfo();


    @Override
    public int getError() {
        return errorInfo.getError();
    }


    @Override
    public int getListeningError() {
        return errorInfo.getListeningError();
    }


    @Override
    public void setListeningError(int errorTypes) {
        errorInfo.setListeningError(errorTypes);
    }


    @Override
    public void addListeningError(int errorTypes) {
        int error = errorInfo.getError();
        errorInfo.setListeningError(error | errorTypes);
    }


    /**
     * エラーが起きているかチェックして、保持する<br />
     * <p>
     * <strong>このメソッドは呼び出すべきではない。</strong><br />
     * ハンドリングしたエラーと保持しているエラーに整合性がなくなる恐れがある。 このメソッドは、実装されるためだけにprotectedで公開される
     * </p>
     * call {@link #check()} method instead of this
     * 
     * @return {@link #NO_ERROR} or any error type
     */
    protected abstract int handling();


    /**
     * {@link #handling}を呼び出し、エラーが起きているかチェックする<br />
     * エラーが起きている場合、イベントを投げる
     */
    public void check() {
        errorInfo.setError(handling());
        fireErrorOccurs(errorInfo.getError());
    }


    /**
     * errorTypeに指定されたエラーが起きたことを、リスナーに通知する
     * 
     * @param errorType
     */
    protected void fireErrorOccurs(int errorType) {
        IErrorListener[] ls = getListeners();
        for (IErrorListener l : ls) {
            l.errorOccurs(errorType);
        }
    }

    private ListenerList listeners = new ListenerList();


    @Override
    public void addErrorListener(IErrorListener listener) {
        listeners.add(listener);
    }


    /**
     * @return 現在登録されているリスナー
     */
    protected IErrorListener[] getListeners() {
        IErrorListener[] ary = new IErrorListener[listeners.size()];
        System.arraycopy(listeners.getListeners(), 0, ary, 0, ary.length);
        return ary;
    }

    /**
     * エラー情報保持クラス
     * 
     * @author KMorishima
     * 
     */
    protected static class ErrorInfo {
        /**
         * エラーなし、ハンドリングするエラーなしの状態でインスタンス化する
         */
        public ErrorInfo() {
            error = NO_ERROR;
            this.listeningError = NO_ERROR;
        }

        private int error;
        private int listeningError;


        /**
         * @return 保持しているエラー
         */
        public int getError() {
            return error;
        }


        /**
         * @param error
         *            保持させるエラー
         */
        public void setError(int error) {
            this.error = error;
        }


        /**
         * @return ハンドリングしているエラー
         */
        public int getListeningError() {
            return listeningError;
        }


        /**
         * @param listeningError
         *            ハンドリングするエラー。上書きする
         */
        public void setListeningError(int listeningError) {
            this.listeningError = listeningError;
        }
    }
}
