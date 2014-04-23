/**
 * @version $Id: IRefreshable.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/09/28 11:12:41
 * @author yukihiro-kinjo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.uicomponent;

/**
 * 表示のリフレッシュが可能なUIを実装するためのインタフェース。<br />
 * <br />
 * データベースビューの更新をUIプラグイン以外(webEntry)から行うために作成した。<br />
 * DataBaseViewがこのインタフェースを実装する。<br />
 * 本来、DataBaseViewにリスナーを実装し、更新が必要なタイミングで更新されるようにするのが適切だが、
 * 影響範囲が大きい為、暫定的にこのインタフェースでDataBaseViewを操作する。
 * 
 * @author yukihiro-kinjo
 * 
 */
public interface IRefreshable {

    /**
     * UIの表示を更新する
     */
    public void refreshView();
}
