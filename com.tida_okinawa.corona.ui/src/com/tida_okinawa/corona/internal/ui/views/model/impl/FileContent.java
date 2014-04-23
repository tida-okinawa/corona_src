/**
 * @version $Id: FileContent.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/14 18:27:56
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.tida_okinawa.corona.internal.ui.views.model.IUIClaim;
import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;

/**
 * @author kousuke-morishima
 */
public class FileContent {
    private static final String NL = System.getProperty("line.separator"); //$NON-NLS-1$


    public static InputStream toStream(IUIElement element) {
        if (element instanceof IUIDictionary) {
            return toStream((IUIDictionary) element);
        } else if (element instanceof IUIWork) {
            return toStream((IUIWork) element);
        } else if (element instanceof IUIClaim) {
            return toStream((IUIClaim) element);
        } else {
            return new ByteArrayInputStream(new byte[0]);
        }
    }


    public static InputStream toStream(IUIDictionary uiDic) {
        return new ByteArrayInputStream(toContent(uiDic).getBytes());
    }


    private static String toContent(IUIDictionary uiDic) {
        /* 辞書ファイルの中身を作る */
        ICoronaDic dic = uiDic.getObject();
        if (dic == null) {
            throw new IllegalStateException(Messages.FileContent_errorGetDic);
        }
        StringBuffer buf = new StringBuffer();
        append(buf, "Id", dic.getId()); //$NON-NLS-1$
        append(buf, "Name", dic.getName()); //$NON-NLS-1$
        append(buf, "UpdateDate", dic.getLasted()); //$NON-NLS-1$
        return buf.toString();
    }


    public static InputStream toStream(IUIWork uiWork) {
        return new ByteArrayInputStream(toContent(uiWork).getBytes());
    }


    private static String toContent(IUIWork uiWork) {
        IClaimWorkData work = uiWork.getObject();
        if (work == null) {
            throw new IllegalStateException(Messages.FileContent_errorGetResult);
        }
        StringBuffer buf = new StringBuffer();
        append(buf, "ClaimId", work.getClaimId()); //$NON-NLS-1$
        append(buf, "FieldId", work.getFieldId()); //$NON-NLS-1$
        append(buf, "Type", work.getClaimWorkDataType()); //$NON-NLS-1$
        append(buf, "UpdateDate", work.getLasted()); //$NON-NLS-1$
        return buf.toString();
    }


    public static InputStream toStream(IUIClaim uiClaim) {
        return new ByteArrayInputStream(toContent(uiClaim).getBytes());
    }


    private static String toContent(IUIClaim uiClaim) {
        IClaimData claim = uiClaim.getObject();
        if (claim == null) {
            throw new IllegalStateException(Messages.FileContent_errorGetDataFile);
        }
        StringBuffer buf = new StringBuffer();
        append(buf, "Id", claim.getId()); //$NON-NLS-1$
        append(buf, "Product", claim.getProductField()); //$NON-NLS-1$
        return buf.toString();
    }


    private static StringBuffer append(StringBuffer buf, String label, Object value) {
        if (label.length() > 0) {
            buf.append(label).append("="); //$NON-NLS-1$
        }
        return buf.append(value).append(NL);
    }
}
