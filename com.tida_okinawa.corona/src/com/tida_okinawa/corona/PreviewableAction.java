/**
 * @version $Id: PreviewableAction.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/21 15:21:23
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

/**
 * IPreviewableActionの抽象実装クラス。
 * 
 * @author kousuke-morishima
 */
public abstract class PreviewableAction extends Action implements IPreviewableAction {

    @Override
    public void run() {
        if (checkEffected()) {
            doRun();
        }
    }


    @Override
    public void preview(Shell shell) {
        if (checkEffected()) {
            int retCode = openPreviewDialog(shell);
            if (retCode == Dialog.OK) {
                doRun();
            } else if (retCode == Dialog.CANCEL) {
                doCancel();
            }
        } else {
            if (doRunWhenNoEffectedItems()) {
                doRun();
            }
        }
    }


    /**
     * プレビューの結果、影響を受けるアイテムがなかったとき、処理を続行するか、何もしないか。
     * このメソッドは、オーバーライドするためにprotectedになっている。通常、サブクラスで呼び出す必要はない。
     * 
     * @return 処理を続行する(doRunを呼び出す)ならtrue
     */
    protected boolean doRunWhenNoEffectedItems() {
        return false;
    }


    /**
     * 変更を伴う処理を実際に行う
     */
    protected abstract void doRun();


    /**
     * プレビューでキャンセルを押されたときの処理
     */
    protected void doCancel() {
        setResult(Status.CANCEL_STATUS);
    }

    private IStatus result = Status.OK_STATUS;


    /**
     * @param result
     *            処理結果
     */
    protected void setResult(IStatus result) {
        this.result = result;
    }


    /**
     * @return 処理結果
     */
    public final IStatus getResult() {
        return result;
    }


    /**
     * プレビューで表示するアイテムを探したり、実際に処理するアイテムを探したりする。
     * 
     * @return 影響を受けるアイテムが見つかったらtrue
     */
    protected abstract boolean checkEffected();


    /**
     * プレビュー画面を開いて、処理を実行するかユーザに確認する
     * 
     * @param shell
     *            親Shell
     * @return 処理を実行するなら {@link Window#OK}。処理しないならそれ以外。
     */
    protected abstract int openPreviewDialog(Shell shell);

}
