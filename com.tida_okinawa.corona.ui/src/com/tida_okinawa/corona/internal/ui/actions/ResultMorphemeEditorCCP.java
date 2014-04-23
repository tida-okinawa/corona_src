/**
 * @version $Id: ResultMorphemeEditorCCP.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/02 15:32:24
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.actions;

import org.eclipse.jface.viewers.IStructuredSelection;

import com.tida_okinawa.corona.correction.morphem.ISyntaxStructureElement;
import com.tida_okinawa.corona.io.model.MorphemeElement;

/**
 * @author kousuke-morishima
 */
public class ResultMorphemeEditorCCP extends AbstractCCP {


    public ResultMorphemeEditorCCP() {
    }


    @Override
    protected AbstractCoronaCCPAction createCopy(String name) {
        return new AbstractCoronaCCPAction(name) {
            @Override
            public void run() {
                setContents(selection);
            }


            @Override
            protected boolean updateSelection(IStructuredSelection selection) {
                if (!super.updateSelection(selection)) {
                    return false;
                }
                for (Object o : selection.toArray()) {
                    if (o instanceof ISyntaxStructureElement) {
                    } else if (o instanceof MorphemeElement) {
                    } else {
                        return false;
                    }
                }
                return true;
            }
        };
    }


    @Override
    protected AbstractCoronaCCPAction createCut(String name) {
        return null; // not support
    }


    @Override
    protected AbstractCoronaCCPAction createPaste(String name) {
        return null; // not support
    }
}
