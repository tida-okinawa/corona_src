/**
 * @version $Id: AbstractExtractHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/07 14:07:16
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.extract;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;

/**
 * @author s.takuro
 *         #177 パターン自動生成（係り受け抽出）
 */
public class AbstractExtractHandler extends AbstractHandler {

    protected ClaimWorkDataType targetType = ClaimWorkDataType.NONE;
    static IUIWork uiWork = null;


    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        return null;
    }


    @Override
    public boolean isEnabled() {
        targetType = getWorkDataType();
        return isEnabled(targetType);
    }


    private static ClaimWorkDataType getWorkDataType() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return ClaimWorkDataType.NONE;
        }
        IStructuredSelection ss = (IStructuredSelection) window.getSelectionService().getSelection();
        /* 複数アイテムを選択している場合はグレーアウト */
        if (ss.size() == 1) {
            for (Object element : ss.toArray()) {
                if (element instanceof IUIWork) {
                    uiWork = (IUIWork) element;
                    return uiWork.getObject().getClaimWorkDataType();
                }
            }
        }
        return ClaimWorkDataType.NONE;
    }


    /**
     * 抽出可能なタイプかどうか判定する
     * 
     * @param type
     *            タイプ
     * @return trueならば抽出可能
     */
    protected boolean isEnabled(ClaimWorkDataType type) {
        return false;
    }
}
