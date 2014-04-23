/**
 * @version $Id: SynonymSubItem.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/27 10:31:32
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.dic.impl;

import com.tida_okinawa.corona.io.model.dic.IDepend;
import com.tida_okinawa.corona.io.model.dic.ITerm;

/**
 * @author shingo-takahashi
 */
public class SynonymSubItem extends DependSub {
    /**
     * @param id
     * @param term
     *            従属語
     * @param parent
     *            自身が属している代表語アイテム
     */
    public SynonymSubItem(int id, ITerm term, IDepend parent) {
        super(id, term, parent);
    }

}
