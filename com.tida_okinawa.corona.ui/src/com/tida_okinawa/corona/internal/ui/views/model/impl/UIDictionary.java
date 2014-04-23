/**
 * @version $Id: UIDictionary.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/13 10:28:41
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import com.tida_okinawa.corona.internal.ui.views.model.IUIContainer;
import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUILibFolder;
import com.tida_okinawa.corona.internal.ui.views.model.IUILibrary;
import com.tida_okinawa.corona.io.DialogPropertyDescriptor;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.PropertyUtil;
import com.tida_okinawa.corona.io.PropertyUtil.PropertyItem;
import com.tida_okinawa.corona.io.model.ICoronaDics;
import com.tida_okinawa.corona.io.model.TextItem;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDependDic;
import com.tida_okinawa.corona.io.model.dic.IFlucDic;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.ISynonymDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.service.IIoService;
import com.tida_okinawa.corona.ui.editors.user.ComboItem;

/**
 * @author kousuke-morishima
 */
public class UIDictionary extends UIElement implements IUIDictionary {
    private int id;


    /* public */UIDictionary(IUIContainer parent, ICoronaDic object, IFile resource) {
        super(parent, object, resource);
        IUILibrary uiLib = (IUILibrary) CoronaModel.INSTANCE.getUIContainer(IUILibrary.class, parent);
        if (uiLib != null) {
            ICoronaDics dics = uiLib.getObject();
            if (dics == null) {
                throw new IllegalStateException("辞書を追加できません");
            }
            dics.addDictionary(object);
        }
        id = object.getId();
    }


    @Override
    public int getId() {
        return id;
    }


    @Override
    public ICoronaDic getObject() {
        IUILibrary uiLib = CoronaModel.INSTANCE.getLibrary(this);
        ICoronaDics lib = uiLib.getObject();
        if (lib == null) {
            return null;
        }
        ICoronaDic ret = lib.getDictionary(id);
        return ret;
    }


    @Override
    public IFile getResource() {
        return (IFile) resource;
    }

    /* ****************************************
     * property view
     */

    ComboItem<TextItem> categoryItems = null;


    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] sp = super.getPropertyDescriptors();

        int size = 3;
        ICoronaDic dic = getObject();
        boolean isUserDic = (dic instanceof IUserDic);
        boolean isCategoryDic = isUserDic && (((IUserDic) dic).getDicType()).equals(DicType.CATEGORY);
        boolean canHaveParentDic = (dic instanceof ILabelDic) || (dic instanceof IDependDic);
        size += (isUserDic) ? 3 : 0;
        size += (isCategoryDic) ? 1 : 0;
        size += (canHaveParentDic) ? 1 : 0;
        IPropertyDescriptor[] descriptor = new IPropertyDescriptor[sp.length + size];

        int i;
        for (i = 0; i < sp.length; i++) {
            descriptor[i] = sp[i];
        }
        PropertyUtil prop = new PropertyUtil();
        descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_DIC_TYPE);
        descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_RECORDS);
        descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_CREATEDATE);

        /* ユーザ辞書特有 */
        if (isUserDic) {
            descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_CHILD_LABEL);
            descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_CHILD_FLUC);
            descriptor[i++] = prop.getDescriptor(PropertyItem.PROP_CHILD_SYNONYM);
        }
        /* 分野辞書特有 */
        if (isCategoryDic) {
            categoryItems = new ComboItem<TextItem>(IoActivator.getService().getCategorys()
                    .toArray(new TextItem[IoActivator.getService().getCategorys().size()])) {
                @Override
                protected String toName(TextItem item) {
                    return item.getText();
                }
            };
            descriptor[i++] = PropertyUtil.getComboDescriptor(PropertyItem.PROP_CATEGORY, categoryItems.getNames());
        }
        /* ラベル辞書、ゆらぎ・同義語辞書特有 */
        if (canHaveParentDic) {
            IUILibrary uiLib = CoronaModel.INSTANCE.getLibrary(this);
            DialogPropertyDescriptor dProp = PropertyUtil.getDialogDescriptor(uiLib.getObject(), dic);
            List<Object> defaultValues = new ArrayList<Object>();
            IIoService service = IoActivator.getService();
            for (Integer id : dic.getParentIds()) {
                ICoronaDic parentDic = service.getDictionary(id);
                if (parentDic != null) {
                    defaultValues.add(parentDic);
                }
            }
            dProp.setDefaultValueList(defaultValues);
            descriptor[i++] = dProp;
        }

        return descriptor;
    }


    @Override
    public Object getPropertyValue(Object id) {
        if (PropertyItem.PROP_DIC_TYPE.getKey().equals(id)) {
            ICoronaDic object = getObject();
            if (object != null) {
                Object ret = object.getPropertyValue(id);
                if (ret == null) {
                    ret = "";
                }
                return ret;
            }
        } else if (PropertyItem.PROP_PARENT_NAME.getKey().equals(id)) {
            IUILibrary uiLib = CoronaModel.INSTANCE.getLibrary(this);
            if (uiLib == null) {
                return PropertyUtil.DEFAULT_VALUE;
            }
            ICoronaDics lib = uiLib.getObject();
            if (lib == null) {
                return PropertyUtil.DEFAULT_VALUE;
            }
            ICoronaDic dic = getObject();
            if (dic != null) {
                StringBuilder parentName = new StringBuilder(128);
                for (int pId : dic.getParentIds()) {
                    ICoronaDic parentDic = lib.getDictionary(pId);
                    if (parentDic != null) {
                        /* 　親辞書名取得　 */
                        parentName.append(", ").append(parentDic.getName());
                    }
                }
                if (parentName.length() > 0) {
                    return parentName.substring(2);
                }
            }
            return PropertyUtil.DEFAULT_VALUE;

        } else if (PropertyItem.PROP_CATEGORY.getKey().equals(id)) {
            ICoronaDic dic = getObject();
            if ((dic instanceof IUserDic) && (DicType.CATEGORY.equals(((IUserDic) dic).getDicType()))) {
                return categoryItems.getIndex(((IUserDic) dic).getDicCategory());
            }
            return PropertyUtil.DEFAULT_VALUE;

        } else if (PropertyItem.PROP_CHILD_LABEL.getKey().equals(id)) {
            List<ICoronaDic> dics = IoActivator.getService().getDictionarys(ILabelDic.class);
            return getChildName(dics);

        } else if (PropertyItem.PROP_CHILD_FLUC.getKey().equals(id)) {
            List<ICoronaDic> dics = IoActivator.getService().getDictionarys(IFlucDic.class);
            return getChildName(dics);

        } else if (PropertyItem.PROP_CHILD_SYNONYM.getKey().equals(id)) {
            List<ICoronaDic> dics = IoActivator.getService().getDictionarys(ISynonymDic.class);
            return getChildName(dics);

        } else if (PropertyItem.PROP_RECORDS.getKey().equals(id)) {
            ICoronaDic dic = IoActivator.getService().getDictionary(getId());
            if (dic != null) {
                return Integer.toString(dic.getItemCount());
            } else {
                return "件数を取得できません";
            }

        } else if (PropertyItem.PROP_CREATEDATE.getKey().equals(id)) {
            ICoronaDic dic = getObject();
            if (dic != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                Date date = dic.getCreationTime();
                if (date == null) {
                    return PropertyUtil.DEFAULT_VALUE;
                }
                return sdf.format(date);
            }
        }
        return super.getPropertyValue(id);
    }

    private TextItem changedCategory;


    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PropertyItem.PROP_CATEGORY.getKey().equals(id)) {
            IUserDic dic = (IUserDic) getObject();
            if (dic != null) {
                changedCategory = categoryItems.get((Integer) value);
                dic.setDicCategory(changedCategory);
                dic.commit(false, new NullProgressMonitor());
            }
        } else if (PropertyItem.PROP_PARENT_NAME.getKey().equals(id)) {
            /*
             * ここで処理を実行する（プレビューダイアログを開く）と、プレビューダイアログが複数回開いてしまったので、
             * DialogPropertyDescriptor内で処理している
             */

            // UI上でこの辞書を保持しているターゲットがいれば、新しい親辞書を同じ場所に辞書を追加する
            ICoronaDic dic = getObject();
            if (dic != null) {
                // 親辞書一覧を作る
                Set<Integer> parentIds = dic.getParentIds();
                List<ICoronaDic> parentDics = new ArrayList<ICoronaDic>(parentIds.size());
                for (Integer parentId : parentIds) {
                    ICoronaDic parentDic = IoActivator.getService().getDictionary(parentId);
                    if (parentDic != null) {
                        parentDics.add(parentDic);
                    }
                }
                // 追加する場所を特定（自分と同じ辞書がいる位置を全部見つける
                List<IUIElement> uiElements = CoronaModel.INSTANCE.adapter(dic);
                uiElements.remove(this);
                for (IUIElement uiElement : uiElements) {
                    // こいつらの親フォルダ（ICoronaDics）に、自分の親辞書を作る
                    IUILibrary uiLib = CoronaModel.INSTANCE.getLibrary(((IUIDictionary) uiElement));
                    IUIContainer uiParent = CoronaModel.INSTANCE.getUIContainer(IUILibFolder.class, uiElement.getParent());
                    ICoronaDics lib = uiLib.getObject();
                    for (ICoronaDic parentDic : parentDics) {
                        lib.addDictionary(parentDic);
                        createUIDic(uiParent, parentDic);
                    }
                }
            }
        }
    }


    private static IUIDictionary createUIDic(IUIContainer uiParent, ICoronaDic dic) {
        IFile newParentIFile = uiParent.getResource().getFile(new Path(dic.getName()));
        IUIDictionary uiDic = (IUIDictionary) CoronaModel.INSTANCE.create(uiParent, dic, newParentIFile);
        uiDic.update(null);
        return uiDic;
    }


    private String getChildName(List<ICoronaDic> dics) {
        StringBuffer childName = new StringBuffer(50);
        Integer myId = getId();
        /* 自分を親に持つ辞書取得　 */
        for (ICoronaDic dic : dics) {
            for (int pId : dic.getParentIds()) {
                if (pId == myId) {
                    /* 子辞書名取得　 */
                    childName.append(", ").append(dic.getName());
                }
            }
        }
        if (childName.length() > 0) {
            return childName.substring(2);
        }
        return PropertyUtil.DEFAULT_VALUE;
    }
}
