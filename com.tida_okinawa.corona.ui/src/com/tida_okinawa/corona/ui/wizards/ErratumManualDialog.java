/**
 * @version $Id: ErratumManualDialog.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/26 10:09:40
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.tida_okinawa.corona.common.StringUtil;
import com.tida_okinawa.corona.correction.erratum.IllegalWordRecord;
import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.ui.data.ErratumCorrectionRecord;

/**
 * 
 * @author shingo-wakamatsu
 */
public class ErratumManualDialog extends Dialog {

    /**
     * 誤記補正対象レコードリスト
     */
    private List<ErratumCorrectionRecord> illegalResultList;
    /**
     * 誤記補正対象レコード
     */
    private ErratumCorrectionRecord illegalResult;
    /**
     * 誤記補正対象レコードの文章
     */
    private String illegalText;
    /**
     * 誤記補正対象レコードの誤記文字列
     */
    private List<IllegalWordRecord> illegalWordList;
    /**
     * リターン用　誤記補正結果レコードリスト
     */
    private List<ErratumCorrectionRecord> correctResultList;

    private int total;
    /**
     * 次の表示データ管理用iterator
     */
    private Iterator<ErratumCorrectionRecord> itr;


    public ErratumManualDialog(Shell parentShell, List<ErratumCorrectionRecord> illegalResultList) {
        super(parentShell);
        total = illegalResultList.size();
        itr = illegalResultList.iterator();
        this.illegalResultList = illegalResultList;

        correctResultList = illegalResultList;
    }


    /* ****************************************
     * ボタン処理
     */
    @Override
    protected void okPressed() {
        // 今の修正内容を補正結果リストに反映
        int prevIndex = illegalResultList.indexOf(illegalResult);
        correctResultList.get(prevIndex).setResult(correctTextBox.getText());

        super.okPressed();
    }


    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == NEXT_BTN_ID) {
            if (itr.hasNext()) {
                // ユーザ修正用テキストボックスで補正した内容をリターン用リストに登録
                int prevIndex = illegalResultList.indexOf(illegalResult);
                correctResultList.get(prevIndex).setResult(correctTextBox.getText());

                // 次の誤記補正対象レコードを取得
                illegalResult = itr.next();

                /* ウィンドウのタイトル変更 */
                int illegalResultIndex = illegalResultList.indexOf(illegalResult) + 1;
                updateTitle(illegalResultIndex, total);

                /* 次の誤記補正対象レコードをダイアログで表示 */
                illegalText = illegalResult.getResult();
                illegalWordList = illegalResult.getIllegalWordList();
                illegalTextBox.setText(illegalText);
                paintIllegalWord(illegalWordList, illegalWordColor, illegalTextBox);
                correctTextBox.setText(illegalText);

                selectIllegalWord(correctTextBox, illegalWordList);

                // 誤記補正対象レコードリストの要素が無くなったら、次へボタンをイネーヴル
                if (!itr.hasNext()) {
                    nextBtn.setEnabled(false);
                }
            }
        } else {
            super.buttonPressed(buttonId);
        }
    }


    /* 誤記補正結果を返す */
    public List<ErratumCorrectionRecord> getCorrectResultList() {
        return correctResultList;
    }

    /* ****************************************
     * UI
     */
    /**
     * 間違いを含むテキスト
     */
    StyledText illegalTextBox;
    /**
     * ユーザが修正する用のテキスト
     */
    StyledText correctTextBox;

    // 背景色
    final Color backGroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
    // 補正対象文字色
    final Color illegalWordColor = new Color(Display.getCurrent(), 0xFF, 0x00, 0x00);


    @Override
    protected Control createDialogArea(Composite parent) {
        updateTitle(1, total);

        illegalResult = itr.next();
        /* 重複している修正候補を削除 */
        illegalWordList = correctRepeatIllegalWord(illegalResult.getIllegalWordList());
        // 誤記補正対象レコードの文章を取得
        illegalText = illegalResult.getResult();

        /* ウィンドウのレイアウト設定 */
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout windowLayout = new GridLayout();
        windowLayout.marginHeight = 5;
        composite.setLayout(windowLayout);

        CompositeUtil.createLabel(composite, "原文（赤文字は修正候補）：", -1);
        // 誤記原文用テキストボックス
        illegalTextBox = new StyledText(composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
        illegalTextBox.setText(illegalText);
        illegalTextBox.setEditable(false);
        illegalTextBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        illegalTextBox.setBackground(backGroundColor);
        /* テキストボックスの誤記を色付け */
        paintIllegalWord(illegalWordList, illegalWordColor, illegalTextBox);

        CompositeUtil.createLabel(composite, "誤記を修正してください：", -1);
        // ユーザ修正用テキストボックス
        correctTextBox = new StyledText(composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
        correctTextBox.setText(illegalText);
        correctTextBox.setEditable(true);
        correctTextBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // ユーザ修正用テキストボックスにフォーカスを合わせる
        correctTextBox.setFocus();
        // 最初の補正対象の文字列を選択状態にする
        selectIllegalWord(correctTextBox, illegalWordList);

        return composite;
    }


    private void updateTitle(int current, int total) {
        StringBuilder title = new StringBuilder(100).append("誤記補正（").append(current).append(" / ").append(total).append("）");
        getShell().setText(title.toString());
    }

    /* ********************
     * button
     */
    // ボタンのID。0はOK、1はキャンセルにデフォルトで割り振られているのでそれ以外を指定。
    public static final int NEXT_BTN_ID = 10;
    Button nextBtn;
    Button okBtn;


    /* Button作成 */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        nextBtn = createButton(parent, NEXT_BTN_ID, "次へ", true);
        if (total == 1) {
            nextBtn.setEnabled(false);
        }

        okBtn = createButton(parent, OK, "OK", true);
        /* Button cnclBtn = */createButton(parent, CANCEL, "キャンセル", false);
    }


    /**
     * 与えられたリストから、重複した誤記を除去する
     * 
     * @param illegalWordList
     * @return 重複なしのリスト（引数と同じインスタンス）
     */
    private static List<IllegalWordRecord> correctRepeatIllegalWord(List<IllegalWordRecord> illegalWordList) {
        Set<IllegalWordRecord> bufIllegalWordList = new HashSet<IllegalWordRecord>();
        for (IllegalWordRecord illegalWord : illegalWordList) {
            // 重複を許可しないリストに追加
            bufIllegalWordList.add(illegalWord);
        }
        // 重複を除去した結果を戻す
        illegalWordList.clear();
        illegalWordList.addAll(bufIllegalWordList);

        return illegalWordList;
    }


    /* *****************************
     * テキストボックスの誤記を色付け
     */
    private void paintIllegalWord(List<IllegalWordRecord> illegalWordList, Color illegalWordColor, StyledText illegalTextBox) {
        for (IllegalWordRecord illegalWord : illegalWordList) {
            int startIndex = 0;
            int length = illegalWord.getIllegalWord().length();
            int prevStartIndex = 0;

            // 1文章中のillegalWordの出現回数
            int strCount = StringUtil.countStringInString(illegalText, illegalWord.getIllegalWord());
            // 1文章中の重複したillegalWord全てに色づけ
            for (int i = 0; i < strCount; i++) {
                startIndex = illegalText.indexOf(illegalWord.getIllegalWord(), prevStartIndex);
                // 部分的に色づけ
                StyleRange styleRange = new StyleRange();
                styleRange.start = startIndex;
                styleRange.length = length;
                styleRange.fontStyle = SWT.BOLD;
                styleRange.foreground = illegalWordColor;
                illegalTextBox.setStyleRange(styleRange);
                // 色づけした文字の位置以降から、また重複したillegalWordを探す
                prevStartIndex = startIndex + length;
            }
        }
    }


    /**
     * 最初の補正対象の文字列を選択状態にする<br/>
     * <p>
     * illegalWordListは、誤記の出現順に格納されていない。 ランダムに格納されているので、最初に出現する誤記を探して選択状態にする
     * </p>
     * 
     * @param textBox
     * @param wordList
     */
    private static void selectIllegalWord(StyledText textBox, List<IllegalWordRecord> wordList) {
        // Memo TreeSetに格納し直して、0番目を取得すればいい

        int firstIllegalWordIndex = Integer.MAX_VALUE;
        int firstIllegalWordLength = 0;

        if (wordList.size() > 0) {
            for (IllegalWordRecord rec : wordList) {
                // 誤記文字列の開始位置を取得
                int bufIndex = rec.getStartId();
                if (bufIndex < firstIllegalWordIndex) {
                    firstIllegalWordIndex = bufIndex;
                    // 誤記文字列の長さを取得
                    firstIllegalWordLength = rec.getIllegalWord().length();
                }
            }
        } else {
            firstIllegalWordIndex = 0;
        }
        textBox.setSelection(firstIllegalWordIndex, firstIllegalWordIndex + firstIllegalWordLength);
    }


    /* ****************************************
     * ダイアログ設定
     */
    @Override
    protected Point getInitialSize() {
        return new Point(700, 500);
    }


    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(newShellStyle | SWT.RESIZE);
    }

}
