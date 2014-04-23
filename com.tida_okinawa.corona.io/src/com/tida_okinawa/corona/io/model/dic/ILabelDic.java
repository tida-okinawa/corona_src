/**
 * @version $Id: ILabelDic.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/03
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic;

import java.util.List;


/**
 * ラベル辞書インタフェース
 * 
 * @author shingo-takahashi
 * 
 */
public interface ILabelDic extends ICoronaDic {

    /**
     * ラベル辞書アイテムのListを取得する
     * 
     * @return ラベル辞書アイテムのList
     */
    public List<ILabel> getLabels();


    /**
     * ラベルアイテムからその子になるラベルアイテムをすべて取得する
     * 
     * @param list
     *            探索するラベル辞書アイテムのList
     * @return ラベル辞書アイテムList
     */
    public List<ILabel> getLabelsRecursive(List<ILabel> list);


    /**
     * 指定したユーザー辞書とこのラベル辞書との紐づけ関係に変更があるかどうかを取得する
     * 
     * @param dicId
     *            ユーザー辞書ID
     * @return 変更がある場合true
     */
    public boolean isDicRelationDirty(int dicId);
}
