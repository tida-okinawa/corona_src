/**
 * @file TermElementListSelectionDialog.java
 * @version $Id$
 * 
 * 2013/08/05 16:26:12
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.tida_okinawa.corona.correction.erratum.Erratum;
import com.tida_okinawa.corona.internal.ui.component.ElementListSelectionDialog1;
import com.tida_okinawa.corona.internal.ui.util.DelayTimer;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicItem;

/**
 * #1385 単語選択ダイアログ表示の高速化の為に、一部機能をOverrideしてカスタマイズ
 * 
 * @author s.takuro
 */
public class TermElementListSelectionDialog extends ElementListSelectionDialog1 {

    List<ICoronaDic> ICoronaDics;


    /**
     * @param parent
     *            このダイアログの親Shell
     * @param renderer
     *            表示するアイテムのラベルプロバイダ
     */
    public TermElementListSelectionDialog(Shell parent, ILabelProvider renderer) {
        super(parent, renderer);
        isSettingElements = true;
    }


    /**
     * @param input
     *            辞書
     */
    public void setDicList(List<ICoronaDic> input) {
        ICoronaDics = input;
    }

    /* ****************************************
     * Override
     */
    private Text filteredText;
    private boolean isSettingElements;


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
                getShell().setCursor(new Cursor(null, SWT.CURSOR_WAIT));
                /* ユーザ辞書から単語を取得してダイアログに設定 */
                if (isSettingElements) {
                    /* 2度目以降に単語を取得する際、メモリに保管した場合と比較しても速度にほぼ差はない */
                    List<IDicItem> viewItems = new ArrayList<IDicItem>();
                    for (ICoronaDic dic : ICoronaDics) {
                        viewItems.addAll(dic.getItems());
                    }
                    setListElements(viewItems.toArray());
                    isSettingElements = false;
                }
                /* テキストボックスが空欄となった場合 */
                else if (finalText.getText().isEmpty()) {
                    setListElements(null);
                    isSettingElements = true;
                }
                getShell().setCursor(new Cursor(null, SWT.CURSOR_ARROW));
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


    @Override
    protected void handleEmptyList() {
        /* リストが空でもコントロールを無効にしない */
        updateOkState();
    }
}
