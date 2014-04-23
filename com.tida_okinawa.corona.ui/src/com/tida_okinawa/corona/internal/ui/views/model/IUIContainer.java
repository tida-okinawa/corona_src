/**
 * @version $Id: IUIContainer.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/12 16:47:33
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model;

import org.eclipse.core.resources.IContainer;

/**
 * @author kousuke-morishima
 */
public interface IUIContainer extends IUIElement {

    @Override
    public IContainer getResource();


    /**
     * UI(ProjExplツリー)上の子を返す。
     * 
     * @return UI上の子。
     */
    IUIElement[] getChildren();


    /**
     * @return 子がいるか
     */
    boolean hasChildren();


    /**
     * 子になんかしらの変更があったことの通知を受ける
     */
    void modifiedChildren();
}
