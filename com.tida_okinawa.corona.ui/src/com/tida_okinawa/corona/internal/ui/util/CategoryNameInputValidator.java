/**
 * @version $Id: CategoryNameInputValidator.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * @author miyaguni
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.util;

import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.TextItem;
import com.tida_okinawa.corona.ui.editors.LabelNameInputValidator;

/**
 * 分野名バリデーションクラス
 * 
 * @author miyaguni
 * 
 */
public class CategoryNameInputValidator extends LabelNameInputValidator {
    @Override
    public String isValid(String newText) {
        String message = super.isValid(newText);
        if (message != null) {
            return message;
        }
        for (TextItem category : IoActivator.getService().getCategorys()) {
            if (category.getText().equals(newText)) {
                return newText + "はすでに存在します。";
            }
        }
        return null;
    }


    @Override
    public int getLimit() {
        return 40;
    }
}