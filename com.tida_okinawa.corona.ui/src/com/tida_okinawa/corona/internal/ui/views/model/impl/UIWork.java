/**
 * @version $Id: UIWork.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/13 10:36:34
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model.impl;

import java.text.SimpleDateFormat;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import com.tida_okinawa.corona.internal.ui.views.model.IUIContainer;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.PropertyUtil;
import com.tida_okinawa.corona.io.PropertyUtil.PropertyItem;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;

/**
 * @author kousuke-morishima
 */
public class UIWork extends UIElement implements IUIWork {
    private int claimId;
    private int fieldId;
    private ClaimWorkDataType type;


    /* public */UIWork(IUIContainer parent, IClaimWorkData object, IFile resource) {
        super(parent, object, resource);
        claimId = object.getClaimId();
        fieldId = object.getFieldId();
        type = object.getClaimWorkDataType();
    }


    @Override
    public IClaimWorkData getObject() {
        IClaimWorkData ret = null;
        IUIProduct uiProduct = (IUIProduct) CoronaModel.INSTANCE.getUIContainer(IUIProduct.class, parent);
        ret = uiProduct.getObject().getClaimWorkData(claimId, type, fieldId);
        return ret;
    }


    @Override
    public IFile getResource() {
        return (IFile) resource;
    }


    /* ****************************************
     * property view
     */

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] sp = super.getPropertyDescriptors();

        int size = 3;
        boolean isMorp = false;
        boolean isMis = false;
        IClaimWorkData work = getObject();
        if (work == null) {
            return new IPropertyDescriptor[0];
        }
        ClaimWorkDataType type = work.getClaimWorkDataType();
        switch (type) {
        case MORPHOLOGICAL:
        case DEPENDENCY_STRUCTURE:
            isMorp = true;
            size += 1;
            break;
        case CORRECTION_MISTAKES:
            size -= 1;
            isMis = true;
            break;
        default:
            break;
        }
        IPropertyDescriptor[] descriptor = new IPropertyDescriptor[sp.length + size];

        int i;
        for (i = 0; i < sp.length; i++) {
            descriptor[i] = sp[i];
        }
        PropertyUtil prop = new PropertyUtil();
        if (!isMis) {
            descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_INPUT_DATATYPE);
        }
        descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_CLAIM_DATA_NAME);
        descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_FIELDS);
        if (isMorp) {
            descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_EXEC_RESULT);
        }
        return descriptor;
    }


    @Override
    public Object getPropertyValue(Object id) {

        if (PropertyItem.PROP_INPUT_DATATYPE.getKey().equals(id)) {
            IClaimWorkData work = getObject();
            ClaimWorkDataType type = work.getClaimWorkDataType();
            /* 　入力データの表示なので、誤記補正時は表示しない　 */
            if (ClaimWorkDataType.CORRECTION_MISTAKES.equals(type)) {
                return PropertyUtil.DEFAULT_VALUE;
            }
            if (work.getNote() == null) {
                return PropertyUtil.DEFAULT_VALUE;
            }
            /* 　入力データ種別取得(最後から2番目)　 */
            String[] types = work.getNote().split(",");
            String dataType = "";
            /* 　入力データ種別と自分の種別の２つはあるはず　 */
            if (types.length > 1) {
                dataType = types[types.length - 2];
            }
            /* 　形態素係り受け時はKNPフラグが付加されているので分ける　 */
            if (dataType.contains(":")) {
                dataType = dataType.substring(0, dataType.indexOf(":"));
            }
            return dataType;
        } else if (PropertyItem.PROP_CLAIM_DATA_NAME.getKey().equals(id)) {
            int claimId = getObject().getClaimId();
            List<IClaimData> list = IoActivator.getService().getClaimDatas();

            /* 　自分のclaimIDと同じIDのファイル名を取得する */
            String fileName = "";
            for (IClaimData data : list) {
                if (data.getId() == claimId) {
                    fileName = data.getName() + ",";
                    ;
                }
            }
            if (!("").equals(fileName)) {
                fileName = fileName.substring(0, fileName.lastIndexOf(","));
            }
            return fileName;
        } else if (PropertyItem.PROP_FIELDS.getKey().equals(id)) {
            List<IClaimData> list = IoActivator.getService().getClaimDatas();
            IClaimWorkData work = getObject();
            int fieldId = work.getFieldId();
            int claimId = work.getClaimId();

            /* 　fieldIDの情報から名前を取得する　 */
            String name = "";
            for (IClaimData data : list) {
                if (data.getId() == claimId) {
                    IFieldHeader header = data.getFieldInformation(fieldId);
                    name = header.getName() + ",";
                }
            }
            if (!("").equals(name)) {
                name = name.substring(0, name.lastIndexOf(","));
            }
            return name;
        } else if (PropertyItem.PROP_EXEC_RESULT.getKey().equals(id)) {
            IClaimWorkData work = getObject();
            ClaimWorkDataType type = work.getClaimWorkDataType();
            /* 形態素・係り受け解析時以外は表示しない　 */
            if (!ClaimWorkDataType.DEPENDENCY_STRUCTURE.equals(type)) {
                return PropertyUtil.DEFAULT_VALUE;
            }
            if (work.getNote() == null) {
                return PropertyUtil.DEFAULT_VALUE;
            }

            /* 　実行した処理を取得　 */
            String[] types = work.getNote().split(",");
            String dataType = types[types.length - 1];

            /* 　形態素係り受け時はKNPフラグを取得　 */
            if (dataType.contains(":")) {
                String doKnp = dataType.substring(dataType.indexOf(":") + 1);
                if (("true").equals(doKnp)) {
                    return "形態素と係り受け";
                } else {
                    return "形態素のみ";
                }
            }
        } else if (PropertyItem.PROP_LASTMODIFIED.getKey().equals(id)) {
            IClaimWorkData work = getObject();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            return sdf.format(work.getLasted());
        }

        return super.getPropertyValue(id);
    }

}
