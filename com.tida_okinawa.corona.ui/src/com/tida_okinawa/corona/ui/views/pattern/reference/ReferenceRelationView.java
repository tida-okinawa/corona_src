/**
 * @version $Id: ReferenceRelationView.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/07/26 15:25:28
 * @author wataru-higa
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.views.pattern.reference;

import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

/**
 * 参照関係ビューを設定するクラス
 * 
 * @author wataru-higa
 * 
 */
public class ReferenceRelationView extends ViewPart {
    /**
     * 参照関係ビューのVIEW_ID
     */
    public static final String VIEW_ID = "com.tida_okinawa.corona.ui.view.reference";
    private static TreeViewer viewer;


    @Override
    public void createPartControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label refRelViewLabel = new Label(composite, SWT.NONE);
        refRelViewLabel.setText("部品と部品を参照している構文パターンの関係を表示します。");
        viewer = new TreeViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.setContentProvider(new TreeNodeContentProvider());
        viewer.setLabelProvider(new ReferenceRelationViewLabelProvider());
        viewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }


    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }


    /**
     * 参照関係ビューのツリー表示を設定するメソッド
     * 
     * @param referenceNodes
     *            参照関係データ
     */
    public void setTree(TreeNode[] referenceNodes) {
        viewer.setInput(referenceNodes);
    }
}
