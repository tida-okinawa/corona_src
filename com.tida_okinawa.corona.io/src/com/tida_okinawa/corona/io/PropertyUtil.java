/**
 * @version $Id: PropertyUtil.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/16 11:04:02
 * @author takayuki-matsumoto
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.tida_okinawa.corona.IPreviewableAction;
import com.tida_okinawa.corona.io.model.ICoronaDics;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.uicomponent.SelectList;

/**
 * プロパティビューに表示する内容を定義するクラス.
 * 
 * @author takayuki-matsumoto
 */
public class PropertyUtil {

    /** プロパティの値に表示する初期値 */
    public static final String DEFAULT_VALUE = ""; //$NON-NLS-1$

    /** プロパティ値の取得に失敗した時のメッセージ */
    public static final String GET_PROPERTY_ERROR_MESSAGE = Messages.PropertyUtil_noInformation;

    /**
     * プロパティビューの項目名の列挙定数
     * 
     * @author takayuki-matsumoto
     */
    public enum PropertyItem {
        /* TODO 要日本語文字列の外部化 */

        /** UIElement: ロケーション */
        PROP_LOCATION("location", Messages.PropertyUtil_location), //$NON-NLS-1$

        /** UIElement: 作成日時 */
        PROP_CREATEDATE("createdate", Messages.PropertyUtil_createDate), //$NON-NLS-1$

        /** UIElement: 最終変更日時 */
        PROP_LASTMODIFIED("lastmodified", Messages.PropertyUtil_lastModified), //$NON-NLS-1$

        /** UIElement: 編集可能 */
        PROP_EDITABLE("editable", Messages.PropertyUtil_editable), //$NON-NLS-1$

        /** UIElement: 名前 */
        PROP_NAME("name", Messages.PropertyUtil_name), //$NON-NLS-1$

        /** UIClaim: カラム名 */
        PROP_COLUMN_NAME("column", Messages.PropertyUtil_columnName), //$NON-NLS-1$

        /**
         * UIClaim: レコード件数
         * UIDictionaryでも使用
         */
        PROP_RECORDS("records", Messages.PropertyUtil_records), // //$NON-NLS-1$

        /** UIClaim: 取り込み日時 */
        PROP_IMPORTDATE("importdate", Messages.PropertyUtil_importDate), //$NON-NLS-1$

        /** UIProduct:問い合わせデータ */
        PROP_CLAIMDATA("claimdata", Messages.PropertyUtil_claimData), //$NON-NLS-1$

        /** UIProduct: 最新処理結果 */
        PROP_RESULT("result", Messages.PropertyUtil_result), //$NON-NLS-1$

        /** UIDictionary: 辞書種別 */
        PROP_DIC_TYPE("dictype", Messages.PropertyUtil_dicType), //$NON-NLS-1$

        /** UIDictionary: 親辞書名 */
        PROP_PARENT_NAME("parentname", Messages.PropertyUtil_parentName), //$NON-NLS-1$

        /** UIDictionary: 分野 */
        PROP_CATEGORY("category", Messages.PropertyUtil_category), //$NON-NLS-1$

        /** UIDictionary: 子ラベル辞書名 */
        PROP_CHILD_LABEL("childlabel", Messages.PropertyUtil_childLabel), //$NON-NLS-1$

        /** UIDictionary: 子ゆらぎ辞書名 */
        PROP_CHILD_FLUC("childfluc", Messages.PropertyUtil_childFluc), //$NON-NLS-1$

        /** UIDictionary: 子同義語辞書名 */
        PROP_CHILD_SYNONYM("childsynonym", Messages.PropertyUtil_childSynonym), //$NON-NLS-1$

        /** UIWork: 入力データ種別 */
        PROP_INPUT_DATATYPE("datatype", Messages.PropertyUtil_dataType), //$NON-NLS-1$

        /** UIWork: 問い合わせデータ名 */
        PROP_CLAIM_DATA_NAME("claimdataname", Messages.PropertyUtil_claimDataName), //$NON-NLS-1$

        /** UIWork: 処理対象フィールド名 */
        PROP_FIELDS("field", Messages.PropertyUtil_filed), //$NON-NLS-1$

        /** UIWork: 実行処理(形態素・係り受け) */
        PROP_EXEC_RESULT("execresult", Messages.PropertyUtil_execResult), //$NON-NLS-1$

        /** UIProject: 係り受け解析 */
        PROP_DOKNP("doknp", Messages.PropertyUtil_doKnp), ; //$NON-NLS-1$

        private String key;
        private String disp;


        private PropertyItem(String key, String disp) {
            this.key = key;
            this.disp = disp;
        }


        /**
         * 項目名を返す
         * 
         * @return 項目名
         */
        public String getKey() {
            return this.key;
        }


        /**
         * プロパティビューに表示する項目名（日本語）を返す
         * 
         * @return 表示する項目名
         */
        public String getDisp() {
            return this.disp;
        }
    }


    /**
     * 指定された項目の、標準的なプロパティデスクリプタを返す
     * 
     * @param item
     *            プロパティの種類
     * @return プロパティデスクリプタ
     */
    public PropertyDescriptor getDescriptor(PropertyItem item) {
        return new PropertyDescriptor(item.getKey(), item.getDisp());
    }


    /**
     * 指定された項目の、コンボボックスで編集できるプロパティデスクリプタを返す
     * 
     * @param item
     *            プロパティの種類
     * @param items
     *            コンボボックスに表示するアイテムの一覧
     * @return コンボボックスプロパティデスクリプタ
     */
    public static ComboBoxPropertyDescriptor getComboDescriptor(PropertyItem item, String[] items) {
        return new ComboBoxPropertyDescriptor(item.getKey(), item.getDisp(), items);
    }


    /**
     * 親辞書をダイアログで編集できるプロパティデスクリプタを返す
     * 
     * @param lib
     *            選択可能な親辞書を保持しているインスタンス
     * @param source
     *            親辞書を変更される子辞書
     * @return ダイアログプロパティデスクリプタ
     */
    public static DialogPropertyDescriptor getDialogDescriptor(final ICoronaDics lib, final ICoronaDic source) {
        PropertyItem item = PropertyItem.PROP_PARENT_NAME;
        return new DialogPropertyDescriptor(item.getKey(), item.getDisp()) {
            @Override
            protected ChangePropertyDialog getDialog(Shell shell) {
                return new ChangePropertyDialog(shell) {
                    SelectList selectList = null;


                    @Override
                    protected Point getInitialSize() {
                        return new Point(500, 300);
                    }


                    @Override
                    protected Control createDialogArea(Composite parent) {
                        parent = (Composite) super.createDialogArea(parent);

                        Label message = new Label(parent, SWT.NONE);
                        message.setText(Messages.PropertyUtil_messageSelNewParentDic);

                        selectList = new SelectList(parent);
                        List<Object> input = new ArrayList<Object>(lib.getDictionarys(IUserDic.class));
                        selectList.setSourceInput(input);
                        selectList.setInitialValues(initialValues);
                        selectList.addListChangedListener(new Listener() {
                            @Override
                            public void handleEvent(Event event) {
                                listChanged(event);
                            }
                        });
                        selectList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
                        return parent;
                    }


                    @Override
                    protected void okPressed() {
                        List<ICoronaDic> newParents = new ArrayList<ICoronaDic>();
                        for (Object o : getResults()) {
                            if (o instanceof ICoronaDic) {
                                newParents.add((ICoronaDic) o);
                            }
                        }
                        IPreviewableAction action = new ChangeParentDictionayAction(source, newParents);
                        action.preview(Display.getCurrent().getActiveShell());
                        super.okPressed();
                    }


                    @Override
                    public Object[] getResults() {
                        return selectList.getSelected();
                    }


                    @Override
                    protected String getTitle() {
                        return Messages.PropertyUtil_changeParentDic;
                    }


                    void listChanged(Event event) {
                        if (event.detail == SelectList.REMOVED) {
                            if (selectList.getSelected().length == 0) {
                                getButton(OK).setEnabled(false);
                                return;
                            }
                        }
                        getButton(OK).setEnabled(true);
                    }
                };
            }
        };
    }
}
