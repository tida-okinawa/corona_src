/**
 * @version $Id: ElementListSelectionDialog1.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/02/24 22:11:19
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.component;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.FilteredList;

import com.tida_okinawa.corona.correction.erratum.Erratum;
import com.tida_okinawa.corona.internal.ui.util.DelayTimer;

/**
 * フィルタテキストのIMEモードと、文字を入力してからフィルタをかけるまでのディレイを設定できるElementListSelectionDialogの実装。
 * 
 * @author kousuke-morishima
 */
public class ElementListSelectionDialog1 extends ElementListSelectionDialog {


    /**
     * @param parent
     *            このダイアログの親Shell
     * @param renderer
     *            表示するアイテムのラベルプロバイダ
     */
    public ElementListSelectionDialog1(Shell parent, ILabelProvider renderer) {
        super(parent, renderer);
    }

    /* ****************************************
     * Interface
     */
    private int imeMode = -1;


    /**
     * <ul>
     * <li>
     * SWT#NATIVE : 全角ひらがな</li>
     * <li>
     * SWT#PHONETIC : 半角カタカナ</li>
     * <li>
     * SWT#DBCS : 全角アルファベット</li>
     * <li>
     * SWT#ALPHA : 半角アルファベット</li>
     * <li>
     * SWT#NONE : IME入力なし</li>
     * <li>
     * SWT#ROMAN : IME入力あり</li>
     * </ul>
     * <code> shell.setImeInputMode(SWT.NATIVE | SWT.PHONETIC)</code>で全角カナ
     * 
     * @param imeMode
     *            SWTのIME設定定数か、-1。-1だと、もともとの設定を変えない。
     * @see SWT#NONE
     * @see SWT#DBCS
     * @see SWT#ALPHA
     * @see SWT#NATIVE
     * @see SWT#PHONETIC
     * @see SWT#ROMAN
     */
    public void setImeMode(int imeMode) {
        this.imeMode = imeMode;
    }


    /**
     * @return 現在のIMEモード
     * @see #setImeMode(int)
     */
    public int getImeMode() {
        return imeMode;
    }

    private long delay = 300;


    /**
     * @param millis
     *            文字を確定してからフィルタをかけるまでの時間
     */
    public void setFilterDelay(long millis) {
        this.delay = millis;
    }


    /**
     * @return 文字を確定してからフィルタを適用するまでの遅延時間
     */
    public long getFilterDelay() {
        return delay;
    }

    private FilteredList filteredList;


    /** #1385 継承先で使用するため */
    protected FilteredList getFilteredList() {
        return filteredList;
    }


    /* ****************************************
     * Override
     */
    @Override
    protected FilteredList createFilteredList(Composite parent) {
        filteredList = super.createFilteredList(parent);
        return filteredList;
    }

    private boolean isConvertZenkaku = false;


    /**
     * 半角英数の全角変換を行うかどうかを設定する
     * 
     * @param setConvertFlag
     *            全角変換処理を行う場合はtrue、行わない場合はfalse
     * 
     */
    public void setConvertZenkaku(boolean setConvertFlag) {
        isConvertZenkaku = setConvertFlag;
    }


    /**
     * 全角変換処理の設定値を返却
     * 
     * @return isConvertZenkaku
     *         全角変換処理を行う設定であればTrue
     */
    public boolean isConvertZenkaku() {
        return isConvertZenkaku;
    }

    private Text filteredText;


    @Override
    protected Text createFilterText(Composite parent) {
        final Text finalText = filteredText = super.createFilterText(parent);

        /* 入力直後にフィルタを適用するリスナーがついているので、元のリスナーを外す */
        Listener[] listeners = filteredText.getListeners(SWT.Modify);
        for (Listener listener : listeners) {
            filteredText.removeListener(SWT.Modify, listener);
        }

        final DelayTimer timer = new DelayTimer("", new Runnable() { //$NON-NLS-1$
                    @Override
                    public void run() {
                        getShell().getDisplay().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                if (isConvertZenkaku()) {
                                    String zenkakuString = Erratum.convertZenkakuString(finalText.getText()).replace('＊', '*');
                                    getFilteredList().setFilter(zenkakuString);
                                } else {
                                    getFilteredList().setFilter(finalText.getText());
                                }
                            }
                        });
                    }
                });

        Listener listener = new Listener() {
            @Override
            public void handleEvent(Event e) {
                timer.run(getFilterDelay());
            }
        };
        FocusListener focusListener = new FocusListener() {
            private int oldImeMode = -1;


            @Override
            public void focusGained(FocusEvent e) {
                Shell shell = getShell();
                oldImeMode = shell.getImeInputMode();
                if (getImeMode() == -1) {
                    shell.setImeInputMode(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getImeInputMode());
                } else {
                    shell.setImeInputMode(getImeMode());
                }
            }


            @Override
            public void focusLost(FocusEvent e) {
                Shell shell = getShell();
                shell.setImeInputMode(oldImeMode);
            }
        };
        filteredText.addListener(SWT.Modify, listener);
        filteredText.addFocusListener(focusListener);

        return filteredText;
    }
}
