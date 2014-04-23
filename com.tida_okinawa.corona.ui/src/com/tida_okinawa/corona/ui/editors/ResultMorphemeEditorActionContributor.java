/**
 * @version $Id: ResultMorphemeEditorActionContributor.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/02/15 14:20:50
 * @author miyaguni
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.EditorActionBarContributor;

/**
 * 形態素・係り受け解析結果エディタの ActionContributor クラス
 * 
 * @author miyaguni
 */
public class ResultMorphemeEditorActionContributor extends EditorActionBarContributor {
    @Override
    public void setActiveEditor(IEditorPart targetEditor) {
        if (targetEditor instanceof ResultMorphemeEditor) {
            ((ResultMorphemeEditor) targetEditor).setGlobalActions();
        }
    }
}
