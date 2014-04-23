/**
 * @version $Id: IResultPatternEditorInput.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/02/23 9:27:00
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import com.tida_okinawa.corona.io.model.IClaimWorkPattern;


/**
 * @author shingo-takahashi
 */
public interface IResultPatternEditorInput {

    public abstract IClaimWorkPattern getClaimWorkPattern();


    public abstract int getClaimId();


    public abstract int getFieldId();

}