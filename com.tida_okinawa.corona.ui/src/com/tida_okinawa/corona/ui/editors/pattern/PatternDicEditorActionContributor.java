/**
 * @version $Id: PatternDicEditorActionContributor.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/11 14:30:09
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorActionBarContributor;

/**
 * @author kousuke-morishima
 */
public class PatternDicEditorActionContributor extends EditorActionBarContributor {
    static final String DELETE = ActionFactory.DELETE.getId();

    PatternDicEditor activeEditor = null;


    @Override
    public void init(IActionBars bars) {
        super.init(bars);

        bars.setGlobalActionHandler(DELETE, createAction(DELETE, "削除(&D)"));
    }


    @Override
    public void setActiveEditor(IEditorPart targetEditor) {
        if (targetEditor instanceof PatternDicEditor) {
            this.activeEditor = (PatternDicEditor) targetEditor;
            ((PatternDicPage) this.activeEditor.getActivePageInstance()).setGlobalActions();
        } else {
            this.activeEditor = null;
        }
    }


    private Action createAction(final String actionId, String label) {
        Action a = new Action(label) {
            @Override
            public void run() {
                activeEditor.performAction(actionId);
            }
        };
        a.setId(actionId);
        return a;
    }
}
