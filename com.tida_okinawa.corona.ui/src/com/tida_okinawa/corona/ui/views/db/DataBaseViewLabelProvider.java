/**
 * @version $Id: DataBaseViewLabelProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/23 19:51:54
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.views.db;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.TextItem;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IFlucDic;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.ISynonymDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.model.dic.PatternType;
import com.tida_okinawa.corona.io.util.CoronaIoUtils;
import com.tida_okinawa.corona.ui.Icons;
import com.tida_okinawa.corona.ui.views.db.DataBaseViewContentProvider.DBViewFolder;

/**
 * 
 * @author kyohei-miyazato
 */
public class DataBaseViewLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
        if (element instanceof ICoronaDic) {
            /* 辞書 */
            return ((ICoronaDic) element).getName();
        }
        if (element instanceof IClaimData) {
            /* 問い合わせデータ */
            IClaimData claim = (IClaimData) element;
            return Messages.bind(Messages.DataBaseViewLabelProvider_Claim_TableAndFile, CoronaIoUtils.getTableNameSuffix(claim.getTableName()),
                    claim.getFileName());
        }
        if (element instanceof ICoronaProject) {
            /* プロジェクト */
            return ((ICoronaProject) element).getName();
        }
        if (element instanceof TextItem) {
            return ((TextItem) element).getText();
        }
        if (element instanceof DBViewFolder) {
            return ((DBViewFolder) element).getFolderName();
        }

        return super.getText(element);
    }


    @Override
    public Image getImage(Object element) {
        /* 辞書 */
        if (element instanceof ICoronaDic) {
            if (element instanceof IUserDic) {
                DicType type = ((IUserDic) element).getDicType();
                switch (type) {
                case COMMON: /* 一般辞書 */
                    return Icons.INSTANCE.get(Icons.IMG_DIC_COMMON);
                case CATEGORY: /* 分野辞書 */
                    return Icons.INSTANCE.get(Icons.IMG_DIC_CATEGORY);
                case SPECIAL: /* 固有辞書 */
                    return Icons.INSTANCE.get(Icons.IMG_DIC_SPECIAL);
                case JUMAN: /* JUMAN辞書 */
                    return Icons.INSTANCE.get(Icons.IMG_DIC_JUMAN);
                }
            }
            /* ゆらぎ辞書 */
            if (element instanceof IFlucDic) {
                return Icons.INSTANCE.get(Icons.IMG_DIC_FLUC);
            }
            /* ラベル辞書 */
            if (element instanceof ILabelDic) {
                return Icons.INSTANCE.get(Icons.IMG_DIC_LABEL);
            }
            /* 構文パターン辞書 */
            if (element instanceof IPatternDic) {
                return Icons.INSTANCE.get(Icons.IMG_DIC_PATTERN);
            }
            /* 同義語辞書 */
            if (element instanceof ISynonymDic) {
                return Icons.INSTANCE.get(Icons.IMG_DIC_SYNONYM);
            }
        }

        /* プロジェクト */
        if (element instanceof ICoronaProject) {
            return Icons.INSTANCE.get(Icons.IMG_PROJECT);
        }

        if ((element instanceof PatternType) || (element instanceof TextItem)) {
            return Icons.INSTANCE.get(Icons.IMG_OBJ_ITEM);
        }

        /* 問い合わせデータ */
        if (element instanceof IClaimData) {
            return Icons.INSTANCE.get(Icons.IMG_CLAIM);
        }

        if (element instanceof DBViewFolder) {
            return Icons.INSTANCE.get(Icons.IMG_FOLDER);
        }

        return super.getImage(element);
    }
}
