/**
 * @version $Id: CoronaElementLabelProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/26 21:06:47
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.views;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.tida_okinawa.corona.internal.ui.util.ClaimUtil;
import com.tida_okinawa.corona.internal.ui.views.model.ICoronaFolder;
import com.tida_okinawa.corona.internal.ui.views.model.ILibrary;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IFlucDic;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.ISynonymDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.ui.Icons;

/**
 * @author kousuke-morishima
 */
public class CoronaElementLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
        if (element instanceof ICoronaDic) {
            return ((ICoronaDic) element).getName();
        } else if (element instanceof IClaimWorkData) {
            IClaimWorkData work = (IClaimWorkData) element;
            return work.getClaimWorkDataType().getName() + "(" + ClaimUtil.getFieldName(work.getClaimId(), work.getFieldId()) + ")";
        } else if (element instanceof IClaimData) {
            return ((IClaimData) element).getName();
        } else if (element instanceof ICoronaFolder) {
            return ((ICoronaFolder) element).getName();
        } else if (element instanceof ICoronaProduct) {
            return ((ICoronaProduct) element).getName();
        }
        return super.getText(element);
    }


    @Override
    public Image getImage(Object element) {
        if (element instanceof IUserDic) {
            IUserDic udic = (IUserDic) element;
            if (DicType.JUMAN.equals(udic.getDicType())) {
                return Icons.INSTANCE.get(Icons.IMG_DIC_JUMAN);
            } else if (DicType.COMMON.equals(udic.getDicType())) {
                return Icons.INSTANCE.get(Icons.IMG_DIC_COMMON);
            } else if (DicType.CATEGORY.equals(udic.getDicType())) {
                return Icons.INSTANCE.get(Icons.IMG_DIC_CATEGORY);
            } else if (DicType.SPECIAL.equals(udic.getDicType())) {
                return Icons.INSTANCE.get(Icons.IMG_DIC_SPECIAL);
            }
        } else if (element instanceof IFlucDic) {
            return Icons.INSTANCE.get(Icons.IMG_DIC_FLUC);
        } else if (element instanceof ILabelDic) {
            return Icons.INSTANCE.get(Icons.IMG_DIC_LABEL);
        } else if (element instanceof IPatternDic) {
            return Icons.INSTANCE.get(Icons.IMG_DIC_PATTERN);
        } else if (element instanceof ISynonymDic) {
            return Icons.INSTANCE.get(Icons.IMG_DIC_SYNONYM);
        } else if (element instanceof IClaimData) {
            return Icons.INSTANCE.get(Icons.IMG_CLAIM);
        } else if (element instanceof IClaimWorkData) {
            if (((IClaimWorkData) element).getClaimWorkDataType() == ClaimWorkDataType.NONE) {
                return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE).createImage();
            } else if (((IClaimWorkData) element).getClaimWorkDataType() == ClaimWorkDataType.BASE) {
                return Icons.INSTANCE.get(Icons.IMG_RESLUT_BASE);
            } else if (((IClaimWorkData) element).getClaimWorkDataType() == ClaimWorkDataType.CORRECTION_MISTAKES) {
                return Icons.INSTANCE.get(Icons.IMG_RESLUT_CORRECTION_MISTAKES);
            } else if (((IClaimWorkData) element).getClaimWorkDataType() == ClaimWorkDataType.MORPHOLOGICAL) {
                return Icons.INSTANCE.get(Icons.IMG_RESLUT_MORPHOLOGICAL);
            } else if (((IClaimWorkData) element).getClaimWorkDataType() == ClaimWorkDataType.DEPENDENCY_STRUCTURE) {
                return Icons.INSTANCE.get(Icons.IMG_RESLUT_DEPENDENCY_STRUCTURE);
            } else if (((IClaimWorkData) element).getClaimWorkDataType() == ClaimWorkDataType.CORRECTION_FLUC) {
                return Icons.INSTANCE.get(Icons.IMG_RESLUT_CORRECTION_FLUC);
            } else if (((IClaimWorkData) element).getClaimWorkDataType() == ClaimWorkDataType.CORRECTION_SYNONYM) {
                return Icons.INSTANCE.get(Icons.IMG_RESLUT_CORRECTION_SYNONYM);
            } else if (((IClaimWorkData) element).getClaimWorkDataType() == ClaimWorkDataType.RESLUT_PATTERN) {
                return Icons.INSTANCE.get(Icons.IMG_RESLUT_RESLUT_PATTERN);
            } else if (((IClaimWorkData) element).getClaimWorkDataType() == ClaimWorkDataType.LASTED) {
                return Icons.INSTANCE.get(Icons.IMG_RESLUT_LASTED);
            } else if (((IClaimWorkData) element).getClaimWorkDataType() == ClaimWorkDataType.FREQUENTLY_APPERING) {
                return Icons.INSTANCE.get(Icons.IMG_RESLUT_FREQUENT);
            }
        } else if (element instanceof ILibrary) {
            return Icons.INSTANCE.get(Icons.IMG_LIBRARY);
        } else if (element instanceof ICoronaProduct) {
            return Icons.INSTANCE.get(Icons.IMG_PRODUCT);
        } else if (element instanceof ICoronaFolder) {
            return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER).createImage();
        }
        return null;
    }

}
