/**
 * @version $Id: UserDicLabelProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/16 16:41:46
 * @author takayuki-matsumoto
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.user;

import java.util.List;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.ui.editors.IPagingDataProvider;

/**
 * @author takayuki-matsumoto
 */
public class UserDicLabelProvider extends LabelProvider implements ITableLabelProvider {
    private static final int STATUS = 0;
    private static final int NUMBER = 1;
    private static final int WORD = 2;
    private static final int PART = 3;
    private static final int CLASS = 4;
    private static final int LABEL = 5;

    private UserDicEditor editor;
    private TableViewer viewer;
    private IPagingDataProvider provider;


    public UserDicLabelProvider(UserDicEditor editor, TableViewer viewer, IPagingDataProvider provider) {
        this.editor = editor;
        this.viewer = viewer;
        this.provider = provider;
    }


    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }


    @Override
    public String getColumnText(Object element, int columnIndex) {
        String result = "";

        ITerm term = (ITerm) element;
        switch (columnIndex) {
        case STATUS:
            /* エラーチェック */
            if (term.isError()) {
                result = "E";
            }
            /* エラーのときは"*"を表示しない */
            else if (term.isDirty()) {
                result += "*";
            }
            break;
        case NUMBER:
            Widget wi = viewer.testFindItem(term);
            if (wi instanceof TableItem) {
                int currentIndex = provider.currentIndex();
                int itemIndex = viewer.getTable().indexOf((TableItem) wi);
                result = String.valueOf(currentIndex + itemIndex + 1);
            }
            break;
        case WORD:
            result = term.getValue();
            break;
        case PART:
            if (term.getTermPart() != null) {
                /* ”なし”の場合は空白表示 */
                if (term.getTermPart().getIntValue() != -1) {
                    result = term.getTermPart().getName();
                }
            }
            break;
        case CLASS:
            if (term.getTermClass() != null) {
                result = term.getTermClass().getName();
            }
            break;
        case LABEL:
            StringBuilder buf = new StringBuilder();
            List<ILabel> labels = editor.getLabels(term);
            for (ILabel l : labels) {
                buf.append(l.getName() + ", ");
            }
            if (buf.length() > 0) {
                result = buf.substring(0, buf.length() - 2);
            }
            break;
        default:
            break;
        }

        return result;
    }
}
