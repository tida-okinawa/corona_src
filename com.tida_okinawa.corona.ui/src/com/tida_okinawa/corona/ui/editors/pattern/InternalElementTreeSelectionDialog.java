/**
 * @version $Id: InternalElementTreeSelectionDialog.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/07 13:46:11
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.dialogs.PatternFilter;

import com.tida_okinawa.corona.ui.UIActivator;

/**
 * フィルタつきツリー選択ダイアログ
 * 
 * @author kousuke-morishima
 */
public class InternalElementTreeSelectionDialog extends ElementTreeSelectionDialog {
    private LabelProvider labelProvider;
    private ITreeContentProvider contentProvider;


    /**
     * @param parent
     *            このダイアログの親Shell
     * @param labelProvider
     *            表示するアイテムのラベルプロバイダ
     * @param contentProvider
     *            表示するアイテムのコンテントプロバイダ
     */
    public InternalElementTreeSelectionDialog(Shell parent, LabelProvider labelProvider, ITreeContentProvider contentProvider) {
        super(parent, labelProvider, contentProvider);
        this.labelProvider = labelProvider;
        this.contentProvider = contentProvider;
    }

    private FilteredTree tree;


    @Override
    protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
        tree = new FilteredTree(parent, SWT.BORDER | SWT.SINGLE, new PatternFilter(), true);
        tree.getViewer().setContentProvider(contentProvider);
        tree.getViewer().setLabelProvider(labelProvider);
        if (initialInput != null) {
            tree.getViewer().setInput(initialInput);
        }
        return tree.getViewer();
    }

    private Object initialInput;


    @Override
    public void setInput(Object input) {
        super.setInput(input);
        if (tree != null) {
            tree.getViewer().setInput(input);
        } else {
            this.initialInput = input;
        }
    }

    private boolean allowNoSelection;


    /**
     * @param allow
     *            何も選択していない状態でもOKを押せるようにするか。
     */
    public void setAllowNoSelection(boolean allow) {
        this.allowNoSelection = allow;
    }


    /**
     * @return 何も選択していない状態を許容するか
     */
    public boolean isAllowNoSelection() {
        return allowNoSelection;
    }


    /**
     * 選択されたものが指定されたクラスの場合にOKを押せるようにするValidatorをセットする
     * 
     * <pre>
     * NG な条件
     * 1. 未選択
     * 2. 選択中のアイテムが、setValidator で指定したクラスオブジェクトでない場合
     * </pre>
     * 
     * @param classes
     *            選択可能なクラス一覧
     */
    public void setValidator(final Class<?>[] classes) {
        setValidator(new ISelectionStatusValidator() {
            @Override
            public IStatus validate(Object[] selection) {
                if ((selection.length == 0) && !isAllowNoSelection()) {
                    return new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, "アイテムが未選択です");
                }

                for (Object o : selection) {
                    for (Class<?> cls : classes) {
                        if (!cls.isAssignableFrom(o.getClass())) {
                            return new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, "選択できないアイテムが含まれています");
                        }
                    }
                }

                return Status.OK_STATUS;
            }
        });
    }
}
