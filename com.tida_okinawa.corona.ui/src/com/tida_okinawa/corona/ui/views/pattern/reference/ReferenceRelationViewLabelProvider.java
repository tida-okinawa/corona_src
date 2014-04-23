/**
 * @version $Id: ReferenceRelationViewLabelProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/07/24 14:58:12
 * @author wataru-higa
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.views.pattern.reference;

import static com.tida_okinawa.corona.ui.views.pattern.reference.ReferenceRelationViewModel.ICON_TYPE_NONE;
import static com.tida_okinawa.corona.ui.views.pattern.reference.ReferenceRelationViewModel.ICON_TYPE_PARTS;
import static com.tida_okinawa.corona.ui.views.pattern.reference.ReferenceRelationViewModel.ICON_TYPE_PUBLIC;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.swt.graphics.Image;

import com.tida_okinawa.corona.ui.Icons;

/**
 * 参照関係ビューのラベルプロバイダ
 * 
 * @author wataru-higa
 * 
 */
public class ReferenceRelationViewLabelProvider extends LabelProvider {

    /**
     * ツリー表示の文言を設定
     */
    @Override
    public String getText(Object obj) {
        if (obj instanceof TreeNode) {
            ReferenceRelationViewModel refRelViewModel = (ReferenceRelationViewModel) ((TreeNode) obj).getValue();
            return refRelViewModel.getReferenceWord();
        }
        return "";
    }


    /**
     * ツリー表示のアイコンを設定
     */
    @Override
    public Image getImage(Object obj) {
        if (obj instanceof TreeNode) {
            ReferenceRelationViewModel refRelViewModel = (ReferenceRelationViewModel) ((TreeNode) obj).getValue();
            // 参照先、参照元のアイコンを設定
            if (refRelViewModel.getIconTypeId() == ICON_TYPE_NONE) {
                return null;
            } else if (refRelViewModel.getIconTypeId() == ICON_TYPE_PARTS) {
                return Icons.INSTANCE.get(Icons.IMG_PATTERN_PART);
            } else if (refRelViewModel.getIconTypeId() == ICON_TYPE_PUBLIC) {
                return Icons.INSTANCE.get(Icons.IMG_PATTERN_RECORD);
            }
        }
        return null;
    }
}
