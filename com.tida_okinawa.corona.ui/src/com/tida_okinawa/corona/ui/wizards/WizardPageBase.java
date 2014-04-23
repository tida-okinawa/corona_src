/**
 * @version $Id: WizardPageBase.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 9:55:45
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

/**
 * @author kousuke-morishima
 */
public abstract class WizardPageBase extends WizardPage {

    protected WizardPageBase(String pageName) {
        super(pageName);
    }

    /**
     * 入力内容を保持するためのインスタンス
     */
    protected IDialogSettings dialogSetting;


    @Override
    public void createControl(Composite parent) {
        dialogSetting = getDialogSettings();

        initializeDialogUnits(parent);
    }


    /**
     * 指定されたsectionNameに紐づいたIDialogSettingsを返す。
     * 
     * @param sectionName
     * @param create
     *            なにも紐づいていないとき、新規に作成するか
     * @return
     */
    protected IDialogSettings getSection(String sectionName, boolean create) {
        if (dialogSetting != null) {
            IDialogSettings section = dialogSetting.getSection(sectionName);
            if (section == null && create) {
                section = dialogSetting.addNewSection(sectionName);
            }
            return section;
        }
        return null;
    }

    /**
     * 実際にページの内容を記憶するインスタンス
     */
    protected IDialogSettings section;
    private static final int historyNum = 5;


    /**
     * 最大で５件まで、コンボボックスの履歴を追加する<br />
     * currentValueが履歴の先頭になるように追加される
     * 
     * @param currentValue
     *            履歴に追加したい値
     * @param key
     *            履歴を管理するキーワード
     */
    protected void saveCombo(String currentValue, String key) {
        saveComboHistory(section, currentValue, key, historyNum);
    }


    /**
     * コンボボックスの履歴を追加する。
     * 
     * @param section
     *            履歴を保存する先
     * @param currentValue
     *            今回追加する値
     * @param key
     *            保存キー
     * @param historyNum
     *            最大履歴数
     */
    public static void saveComboHistory(IDialogSettings section, String currentValue, String key, int historyNum) {
        /* 入力値を先頭に */
        if (currentValue.trim().length() == 0) {
            return;
        }

        /* 履歴をそのうしろに */
        /* 最大５個まで記憶 */
        String[] history = section.getArray(key);
        String[] newHistory;
        if (history != null) {
            if (history.length == historyNum) {
                newHistory = new String[historyNum];
                newHistory[0] = currentValue;
                arraycopy(history, 0, newHistory, 1, historyNum - 1);
            } else {
                newHistory = new String[history.length + 1];
                newHistory[0] = currentValue;
                arraycopy(history, 0, newHistory, 1, history.length);
            }
            /* arraycopyによって、末尾にnullが含まれていたら除去する */
            int cnt = 0;
            for (int i = newHistory.length - 1; i >= 0; i--) {
                if (newHistory[i] == null) {
                    cnt++;
                }
            }
            String[] tmp = new String[newHistory.length - cnt];
            System.arraycopy(newHistory, 0, tmp, 0, tmp.length);
            newHistory = tmp;
        } else {
            newHistory = new String[1];
            newHistory[0] = currentValue;
        }
        section.put(key, newHistory);
    }


    /**
     * srcにdst[0]と同じ値があったら、それはコピーしない<br />
     * この場合、dstの末尾がnullになる<br />
     * 引数は、System.arraycopyと同じ
     * 
     * @param src
     * @param srcPos
     * @param dst
     * @param dstPos
     * @param length
     */
    private static void arraycopy(String[] src, int srcPos, String[] dst, int dstPos, int length) {
        int j = srcPos;
        int k = dstPos;
        for (int i = 0; i < length; i++) {
            if (dst[0].equals(src[j])) {
                j++;
            } else {
                dst[k++] = src[j++];
            }
        }
    }


    /**
     * ページのコンポーネントにフォーカスを当てる
     */
    public abstract void setFocus();

}
