/**
 * @version $Id: IMorphemeDetailTableLabelProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/17 13:44:24
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.extract;

import java.util.Map;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;

/**
 * @author s.takuro
 *         #177 パターン自動生成（係り受け抽出）
 */
public interface IMorphemeDetailTableLabelProvider extends ITableLabelProvider, IColorProvider {
    /**
     * 登録済みアイテムのマップの設定
     * 
     * @param prevDetailMap
     *            登録済みアイテムのマップ
     */
    void setPrevDetailMap(Map<String, IExtractRelationElement> prevDetailMap);
}
