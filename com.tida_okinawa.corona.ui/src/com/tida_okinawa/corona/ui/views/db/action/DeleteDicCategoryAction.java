/**
 * @version $Id: DeleteDicCategoryAction.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/01/12 20:18:25
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.views.db.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.PreviewDialog;
import com.tida_okinawa.corona.PreviewableAction;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.TextItem;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;

/**
 * @author kousuke-morishima
 */
public class DeleteDicCategoryAction extends PreviewableAction {
    /**
     * 選択された分野
     */
    List<TextItem> categories;

    /**
     * 分野に対して、紐づいている辞書のリストマップ
     */
    private Map<TextItem, List<ICoronaDic>> dependencyItems = new HashMap<TextItem, List<ICoronaDic>>();


    public DeleteDicCategoryAction(List<TextItem> categories) {
        this.categories = categories;
    }


    public DeleteDicCategoryAction(IStructuredSelection selection) {
        categories = new ArrayList<TextItem>(selection.size());
        for (Object o : selection.toArray()) {
            if (o instanceof TextItem) {
                categories.add((TextItem) o);
            }
        }
    }


    /*
     * 
     * 辞書に紐づいている分野が一つもない場合、または紐づいている辞書一覧表示後に呼ばれる。
     * 辞書に紐づいていない分野のみ削除を行う
     */
    @Override
    protected void doRun() {
        for (TextItem category : categories) {
            if (!dependencyItems.containsKey(category)) {
                IoActivator.getService().removeCategory(category.getText());
            }
        }
    }


    /*
     * 
     * 辞書に紐付く分野がひとつでもあれば、
     * その分野に削除処理が走るように false を返す
     */
    @Override
    protected boolean checkEffected() {
        List<ICoronaDic> dics = IoActivator.getService().getDictionarys(IUserDic.class);
        for (TextItem category : categories) {
            for (ICoronaDic dic : dics) {
                if (DicType.CATEGORY.equals(((IUserDic) dic).getDicType())) {
                    if (category.equals(((IUserDic) dic).getDicCategory())) {
                        if (!dependencyItems.containsKey(category)) {
                            dependencyItems.put(category, new ArrayList<ICoronaDic>());
                        }
                        dependencyItems.get(category).add(dic);
                    }
                }
            }
        }

        return dependencyItems.keySet().size() > 0;
    }


    @Override
    protected boolean doRunWhenNoEffectedItems() {
        return true;
    }


    @Override
    protected int openPreviewDialog(Shell shell) {
        /* プレビューはする。処理はしない */
        String message = "以下の辞書は分類に属しているため、削除できませんでした。";
        PreviewDialog dialog = new PreviewDialog(shell, false) {
            @Override
            protected void createDetailArea(Composite parent) {
            }


            @Override
            protected void selectionChanged(IStructuredSelection selection) {
            }


            @Override
            protected void createButtonsForButtonBar(Composite parent) {
                createButton(parent, IDialogConstants.OK_ID, "OK", true);
            }
        };
        dialog.setTreeContentProvider(new ConPro());
        dialog.setTreeLabelProvider(new LabelProvider());
        dialog.setMessage(message);
        dialog.setInput(dependencyItems);
        dialog.setWeight(new int[] { 1 });
        dialog.open();

        /* コンソールにメッセージを表示 */
        CoronaActivator.getDefault().getLogger().getOutStream().println(message);
        for (Map.Entry<TextItem, List<ICoronaDic>> entry : dependencyItems.entrySet()) {
            CoronaActivator.getDefault().getLogger().getOutStream().print("[ " + entry.getKey().getText() + " ]\n  ");
            for (ICoronaDic dic : entry.getValue()) {
                CoronaActivator.getDefault().getLogger().getOutStream().print(dic.getName() + ", ");
            }
            CoronaActivator.getDefault().getLogger().getOutStream().println();
        }
        CoronaActivator.getDefault().getLogger().getOutStream().println(); // new
                                                                           // line

        /**
         * ダイアログ表示後、辞書に紐づいていない分野の削除も行うために OK code を返している
         */
        return dialog.getReturnCode();
    }

    private class ConPro extends PreviewDialog.ContentProvider {
        @Override
        public Object[] getElements(Object input) {
            return ((HashMap<?, ?>) input).keySet().toArray();
        }


        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof TextItem) {
                return dependencyItems.get(parentElement).toArray();
            }
            return new Object[0];
        }


        /*
         * 
         * 指定された分野に依存している辞書があるかどうかを返す。
         * 辞書がきたら無視
         * 
         * {@link #checkEffected()} で作成された dependencyItems には
         * 辞書が依存している分野しか入っていないので
         * 必ず true が返る
         * 
         * @param element
         * 
         * @return 分野に依存している辞書があるかどうか。辞書がきたら false
         */
        @Override
        public boolean hasChildren(Object element) {
            return (element instanceof TextItem) || dependencyItems.containsKey(element);
        }
    }
}
