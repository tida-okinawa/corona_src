/**
 * @version $Id: IUIElement.java 1840 2014-04-16 05:38:34Z yukihiro-kinjyo $
 * 
 * 2011/10/12 16:09:39
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.views.properties.IPropertySource;

import com.tida_okinawa.corona.io.model.ICoronaObject;

/**
 * @author kousuke-morishima
 */
public interface IUIElement extends IPropertySource, IAdaptable {
    /**
     * idを持たない場合に返される値
     */
    int NO_ID = -1;


    /**
     * @return UI上の親。may be null
     */
    IUIContainer getParent();


    /**
     * @return 自身のIResource。never not null
     */
    IResource getResource();


    /**
     * 自身と関連づくICoronaObjectを取得する。DBからすでに削除されているなどの理由で取得できない場合はnullを返す。
     * インスタンスをアプリケーションで一意にするためにキャッシュは保持していないので、この処理は時間がかかる場合がある。
     * そのため、何度も呼び出すべきではない。
     * 
     * @return 自身のICoronaObject。may be null
     */
    ICoronaObject getObject();


    /**
     * 自身のICoronaObjectを特定するIDがあればそれを返す。なければ {@link #NO_ID}を返す
     * 
     * @return 自身のIDか {@link #NO_ID}
     */
    int getId();


    /**
     * @return UIElementのルート
     */
    IUIElement getRoot();


    /**
     * ファイルの実体を更新して最新にする。IResourceが実在しない場合、作る。
     * 
     * @param monitor
     */
    void update(IProgressMonitor monitor);
}
