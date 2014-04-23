/**
 * @version $Id: CreateHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/30 14:49:30
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.tida_okinawa.corona.correction.parsing.model.Pattern;
import com.tida_okinawa.corona.correction.parsing.model.PatternContainer;
import com.tida_okinawa.corona.correction.parsing.model.PatternKind;
import com.tida_okinawa.corona.ui.editors.pattern.PatternDicEditor;

/**
 * @author kousuke-morishima
 */
public abstract class CreateHandler extends AbstractHandler {
    protected PatternContainer selectedPattern = null;
    protected Pattern own = null;


    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        String type = event.getParameter("com.tida_okinawa.corona.ui.commandParameter.patternType");
        if (type != null) {
            if (type.equals("change")) {
                /* 変更のときは、isEnabledが呼ばれないので、ここでcreatePattern()している */
                changePattern(selectedPattern, createPattern(selectedPattern.getParent()));
                return null;
            }
        }
        selectedPattern.addChild(createPattern(selectedPattern));
        return null;
    }


    @Override
    public boolean isEnabled() {
        if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) {
            return false;
        }
        /*
         * パターン辞書エディタでPatternContainerが選択されていたら、
         */
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorPart editor = page.getActiveEditor();
        if (editor instanceof PatternDicEditor) {
            IStructuredSelection ss = (IStructuredSelection) page.getSelection();
            Object selection = ss.getFirstElement();
            if (selection instanceof PatternContainer) {
                selectedPattern = (PatternContainer) selection;
                if (selectedPattern.canHaveChild(getCreatePatternKind())) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }


    /**
     * 新しく作成するパターン（自分自身）を作成する
     * 
     * @param parent
     *            作成するパターンの親
     * @return 作成したパターン（自分自身）
     */
    protected abstract Pattern createPattern(PatternContainer parent);


    /**
     * 子パターンとして作成できる種別かどうか判定するために、子パターンの種別を返す。
     * 
     * @return 作成するパターンの種別
     */
    protected abstract PatternKind getCreatePatternKind();


    /**
     * パターンを置き換える
     * 
     * @param from
     *            変更前パターン
     * @param to
     *            変更後パターン
     */
    protected void changePattern(Pattern from, Pattern to) {
        PatternContainer parent = from.getParent();
        parent.setIgnoreChange(true);
        /* 元の位置を保持するため、まず挿入位置を取得 */
        int index = parent.getChildren().indexOf(from);
        /* 親とfromの関連を除去 */
        parent.removeChild(from);
        from.setParent(null);
        /* 親とtoを紐づけ */
        parent.addChild(index, to);
        // to.setParent(parent); /* Memo to作成時に親が指定されているので、冗長 */

        if ((from instanceof PatternContainer) && (to instanceof PatternContainer)) {
            // ((PatternContainer) to).getChildren().clear(); // Memo verbose
            for (Pattern child : ((PatternContainer) from).getChildren()) {
                ((PatternContainer) to).addChild(child);
                child.setParent((PatternContainer) to);
            }
            ((PatternContainer) from).getChildren().clear();
        }
        parent.setIgnoreChange(false);
    }
}
