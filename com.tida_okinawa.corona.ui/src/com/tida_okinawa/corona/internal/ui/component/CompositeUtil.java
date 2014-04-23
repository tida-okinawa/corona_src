/**
 * @version $Id: CompositeUtil.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/03/11
 * @author KMorishima
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 * 
 */
package com.tida_okinawa.corona.internal.ui.component;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * ダイアログやウィザードを作るときに使える系のユーティリティクラス
 * 
 * @author KMorishima
 * 
 */
public class CompositeUtil {

    /**
     * ラベルを作る
     * 
     * @param parent
     * @param label
     *            ラベル文字列
     * @param width
     *            幅（GridData#widthHint)に設定される。負数なら親の幅いっぱいに広げる
     * @return {@link org.eclipse.swt.widgets.Label}
     */
    public static Label createLabel(Composite parent, String label, int width) {
        Label ret = new Label(parent, SWT.NONE);
        if (width >= 0) {
            GridData labelData = new GridData();
            labelData.widthHint = width;
            ret.setLayoutData(labelData);
        } else {
            GridData labelData = new GridData(SWT.FILL, SWT.NONE, true, false);
            ret.setLayoutData(labelData);
        }
        ret.setText(label);
        return ret;
    }


    /**
     * 区切り線ラベルを追加する
     * 
     * @param parent
     *            追加先コンポジット
     * @param orientation
     *            区切り線の向き
     * @return 区切り線ラベル
     * @see SWT#HORIZONTAL
     * @see SWT#VERTICAL
     */
    public static Label createSeparator(Composite parent, int orientation) {
        Label ret = new Label(parent, SWT.SEPARATOR | orientation);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        ret.setLayoutData(layoutData);
        return ret;
    }


    /**
     * スピナーを作る
     * 
     * @param parent
     * @return {@link org.eclipse.swt.widgets.Spinner}
     */
    public static Spinner createSpinner(Composite parent) {
        Spinner ret = new Spinner(parent, SWT.NONE);
        GridData spinData = new GridData(SWT.FILL, SWT.NONE, true, false);
        spinData.widthHint = 40;
        spinData.horizontalAlignment = SWT.RIGHT;
        ret.setLayoutData(spinData);
        return ret;
    }


    /**
     * スピナーを指定した設定で作る
     * 
     * @param parent
     * @param values
     *            {@link Spinner#setValues(int, int, int, int, int, int)}
     *            に指定する要素を順に入れた配列
     * @return {@link org.eclipse.swt.widgets.Spinner}
     */
    public static Spinner createSpinner(Composite parent, int[] values) {
        Spinner ret = new Spinner(parent, SWT.NONE);
        GridData spinData = new GridData(SWT.FILL, SWT.NONE, true, false);
        spinData.widthHint = 40;
        spinData.horizontalAlignment = SWT.RIGHT;
        ret.setLayoutData(spinData);
        ret.setValues(values[0], values[1], values[2], values[3], values[4], values[5]);
        return ret;
    }


    /**
     * コンボを({@link SWT#DROP_DOWN} | {@link SWT#READ_ONLY})で作る
     * 
     * @param parent
     * @param items
     *            リストアイテム
     * @return {@link org.eclipse.swt.widgets.Combo}
     */
    public static Combo createCombo(Composite parent, String[] items) {
        Combo ret = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        ret.setItems(items);
        return ret;
    }


    /**
     * プッシュボタンを作る
     * 
     * @param parent
     * @param style
     * @param label
     *            ボタンに表示する文字列
     * @param l
     *            nullなら何も登録しない
     * @return org.eclipse.swt.widgets.Button
     */
    public static Button createBtn(Composite parent, int style, String label, SelectionListener l) {
        Button btn = new Button(parent, style);
        btn.setText(label);
        if (l != null) {
            btn.addSelectionListener(l);
        }
        GridData layoutData = new GridData();
        btn.setLayoutData(layoutData);
        return btn;
    }


    /**
     * ツリービューアカラムを作る
     * 
     * @param parent
     * @param label
     *            カラム名
     * @param width
     *            列幅。負数を指定すると、packでラベルにあわせる
     * @return 作ったカラム
     */
    public static TreeViewerColumn createColumn(TreeViewer parent, String label, int width) {
        TreeViewerColumn col = new TreeViewerColumn(parent, SWT.NONE);
        col.getColumn().setText(label);
        if (width < 0) {
            col.getColumn().pack();
        } else {
            col.getColumn().setWidth(width);
        }
        col.getColumn().setResizable(true);
        col.getColumn().setMoveable(false);
        return col;
    }


    /**
     * @param parent
     * @param label
     * @param width
     *            負数なら幅いっぱい
     * @return
     */
    public static TableColumn createColumn(Table parent, String label, int width) {
        TableColumn ret = new TableColumn(parent, SWT.NONE);
        // カラム名
        ret.setText(label);
        // カラム幅
        if (width >= 0) {
            ret.setWidth(width);
        } else {
            ret.pack();
        }
        ret.setResizable(true); // リサイズ可能
        return ret;
    }


    /**
     * @param parent
     * @param label
     * @param width
     *            負数なら幅いっぱい
     * @return
     */
    public static TreeColumn createColumn(Tree parent, String label, int width) {
        TreeColumn ret = new TreeColumn(parent, SWT.NONE);
        ret.setText(label);
        if (width >= 0) {
            ret.setWidth(width);
        } else {
            ret.pack();
        }
        ret.setResizable(true);
        return ret;
    }


    /**
     * @param parent
     * @param style
     * @param width
     *            -1なら横幅いっぱい
     * @return
     * @see SWT#SINGLE
     * @see SWT#MULTI
     * @see SWT#READ_ONLY
     * @see SWT#WRAP
     * @see SWT#LEFT
     * @see SWT#RIGHT
     * @see SWT#CENTER
     * @see SWT#PASSWORD
     */
    public static Text createText(Composite parent, int style, int width) {
        Text ret = new Text(parent, style);
        GridData layoutData = new GridData();
        if (width == -1) {
            layoutData.horizontalAlignment = SWT.FILL;
            layoutData.verticalAlignment = SWT.None;
            layoutData.grabExcessHorizontalSpace = true;
            layoutData.grabExcessVerticalSpace = false;
        } else {
            layoutData.widthHint = width;
        }
        ret.setLayoutData(layoutData);

        return ret;
    }


    /**
     * グループを作る
     * 
     * @param parent
     * @param label
     *            グループ名
     * @param columns
     *            中にアイテムを何列で表示するか指定する
     * @return org.eclipse.swt.widgets.Group<br />
     *         GrieData(SWT.FILL, SWT.NONE, true, false)を適用
     */
    public static Group defaultGroup(Composite parent, String label, int columns) {
        Group ret = new Group(parent, SWT.NONE);
        ret.setLayout(new GridLayout(columns, false));
        ret.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        ret.setText(label);
        return ret;
    }


    /**
     * org.eclipse.swt.widgets.GridLayoutを適用したComposite
     * 
     * @param parent
     * @param columns
     *            中にアイテムを何列で表示するか指定する
     * @return org.eclipse.swt.widgets.Combosite<br />
     *         GrieData(SWT.FILL, SWT.FILL, true, true)を適用
     */
    public static Composite defaultComposite(Composite parent, int columns) {
        GridLayout layout = new GridLayout(columns, false);
        return createComposite(parent, layout, null);
    }


    /**
     * @param parent
     * @param columns
     * @param layout
     *            may be null
     * @param layoutData
     *            may be null
     * @return
     */
    public static Composite createComposite(Composite parent, GridLayout layout, GridData layoutData) {
        Composite ret = new Composite(parent, SWT.NONE);
        if (layout == null) {
            layout = new GridLayout(1, false);
        }
        ret.setLayout(layout);
        if (layoutData == null) {
            layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        }
        ret.setLayoutData(layoutData);
        return ret;
    }


    /**
     * @param parent
     * @param orientation
     *            {@link SWT#HORIZONTAL} or {@link SWT#VERTICAL}
     * @return
     */
    public static SashForm defaultSashForm(Composite parent, int orientation) {
        SashForm ret = new SashForm(parent, SWT.NONE);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        ret.setLayoutData(layoutData);
        ret.setOrientation(orientation);
        return ret;
    }


    /**
     * ボタンタイプのToolItemを作る
     * 
     * @param bar
     *            親ツールバー
     * @param text
     *            ボタンの表示文字。not null
     * @param tooltip
     *            マウスホバー時に表示する文字列。
     * @param icon
     *            不要ならnull
     * @param listener
     *            ボタンが押されたときの処理を行うリスナー
     * @return
     */
    public static ToolItem createToolItem(ToolBar bar, String text, String tooltip, Image icon, SelectionListener listener) {
        final ToolItem ret = new ToolItem(bar, SWT.PUSH);
        ret.setText(text);
        ret.setToolTipText(tooltip);
        ret.setImage(icon);
        ret.addSelectionListener(listener);
        return ret;
    }


    /**
     * @param bar
     *            親ツールバー
     * @param style
     *            ボタン種別
     * @param text
     *            ボタンの表示文字。
     * @param tooltip
     *            マウスホバー時に表示する文字列。
     * @param icon
     *            不要ならnull
     * @param listener
     *            ボタンが押されたときの処理を行うリスナー
     * @return
     * @see SWT#PUSH
     * @see SWT#CHECK
     * @see SWT#RADIO
     * @see SWT#SEPARATOR
     * @see SWT#DROP_DOWN
     */
    public static ToolItem createToolItem(ToolBar bar, int style, String text, String tooltip, Image icon, SelectionListener listener) {
        final ToolItem ret = new ToolItem(bar, style);
        if (text != null) {
            ret.setText(text);
        }
        ret.setToolTipText(tooltip);
        ret.setImage(icon);
        if (listener != null) {
            ret.addSelectionListener(listener);
        }
        return ret;
    }


    /* ****************************************
     * layout
     */
    /**
     * GridLayoutを作る。
     * 
     * @param numColumns
     * @param marginLeft
     * @param marginRight
     * @param marginTop
     * @param marginBottom
     * @return
     */
    public static GridLayout gridLayout(int numColumns, int marginLeft, int marginRight, int marginTop, int marginBottom) {
        GridLayout layout = new GridLayout(numColumns, false);
        layout.marginLeft = marginLeft;
        layout.marginRight = marginRight;
        if (marginLeft == 0 && marginRight == 0) {
            layout.marginWidth = 0;
        }
        layout.marginTop = marginTop;
        layout.marginBottom = marginBottom;
        if (marginTop == 0 && marginBottom == 0) {
            layout.marginHeight = 0;
        }
        return layout;
    }


    public static GridData gridData(boolean fillWidth, boolean fillHeight, int hSpan, int vSpan) {
        int width = (fillWidth) ? SWT.FILL : SWT.NONE;
        int height = (fillHeight) ? SWT.FILL : SWT.NONE;
        GridData layoutData = new GridData(width, height, fillWidth, fillHeight);
        layoutData.horizontalSpan = hSpan;
        layoutData.verticalSpan = vSpan;
        return layoutData;
    }
}
