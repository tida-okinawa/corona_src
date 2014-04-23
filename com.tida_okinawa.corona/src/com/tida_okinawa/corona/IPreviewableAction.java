/**
 * @version $Id: IPreviewableAction.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/18 17:24:22
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Shell;

/**
 * 処理を実施する前にプレビューを表示するアクションのインタフェース
 * 
 * @author kousuke-morishima
 */
public interface IPreviewableAction extends IAction {

    @Override
    public void run();


    /**
     * {@link #run()}のプレビューを実行する
     * 
     * @param shell
     *            ダイアログを開くためのshell。may be null
     */
    void preview(Shell shell);

}
