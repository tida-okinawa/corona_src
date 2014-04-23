/**
 * @version $Id: ComparePreviewDialog.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/21 16:05:45
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.actions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.tida_okinawa.corona.PreviewDialog;
import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;

/**
 * @author kousuke-morishima
 */
public class ComparePreviewDialog extends PreviewDialog {

    /**
     * @param shell
     * @param check
     *            アイテム一覧にチェックボックスをつけるか
     */
    ComparePreviewDialog(Shell shell, boolean check) {
        super(shell, check);
        beforeLabelProvider = afterLabelProvider = new LabelProvider(); // set
                                                                        // default
    }


    /* ****************************************
     * UI
     */
    @Override
    protected void selectionChanged(SelectionChangedEvent event) {
        /* 選択したアイテムのbefore/afterを表示する */
        IStructuredSelection selection = (IStructuredSelection) itemViewer.getSelection();
        String before = beforeLabelProvider.getText(selection.getFirstElement());
        beforeText.setText(before);
        String after = afterLabelProvider.getText(selection.getFirstElement());
        afterText.setText(after);
    };

    Text beforeText = null;
    Text afterText = null;


    @Override
    protected void createDetailArea(Composite parent) {
        SashForm root = CompositeUtil.defaultSashForm(parent, SWT.HORIZONTAL);
        Composite beforeGroup = CompositeUtil.defaultComposite(root, 1);
        CompositeUtil.createLabel(beforeGroup, "変更前", -1);
        beforeText = createText(beforeGroup);
        Composite afterGroup = CompositeUtil.defaultComposite(root, 1);
        CompositeUtil.createLabel(afterGroup, "変更後", -1);
        afterText = createText(afterGroup);
        root.setWeights(new int[] { 1, 1 });
    }


    private static Text createText(Composite parent) {
        Text text = new Text(parent, SWT.BORDER | SWT.MULTI);
        text.setEditable(false);
        text.setLayoutData(CompositeUtil.gridData(true, true, 1, 1));
        return text;
    }


    /**
     * @deprecated use {@link #setBeforeLabelProvider(LabelProvider)} and
     *             {@link #setAfterLabelProvider(LabelProvider)}
     */
    @Deprecated
    @Override
    public void setDetailLabelProvider(LabelProvider labelProvider) {
    };

    LabelProvider beforeLabelProvider;


    public void setBeforeLabelProvider(LabelProvider labelProvider) {
        beforeLabelProvider = labelProvider;
    }

    LabelProvider afterLabelProvider;


    public void setAfterLabelProvider(LabelProvider labelProvider) {
        afterLabelProvider = labelProvider;
    }

}
