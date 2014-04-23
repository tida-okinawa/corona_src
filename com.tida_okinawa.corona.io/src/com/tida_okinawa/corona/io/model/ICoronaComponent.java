/**
 * @version $Id: ICoronaComponent.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/01 18:21:54
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author shingo-takahashi
 */
public interface ICoronaComponent extends ICoronaObject {

    /**
     * 更新を行う。
     * 
     * @return 更新処理に成功したら、true
     */
    public abstract boolean update();


    /**
     * コミットを行う
     * 
     * @param monitor
     *            進捗確認用モニター
     * 
     * @return コミット処理に成功したら、true
     */
    public abstract boolean commit(IProgressMonitor monitor);


    /**
     * コミットを行う
     * 
     * @param bRecords
     *            レコード更新フラグ(true:レコードのコミットも行う/false:レコード以外のコミットを行う)
     * @param monitor
     *            進捗確認用モニター
     * @return コミット処理に成功したら、true
     */
    public abstract boolean commit(boolean bRecords, IProgressMonitor monitor);

}
