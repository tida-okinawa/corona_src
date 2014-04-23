/**
 * @version $Id: ExtractCooccurrenceHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/07 13:07:23
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.extract;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.tida_okinawa.corona.correction.collocation.CollocationExtractSequence;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicName;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.table.ITextRecord;
import com.tida_okinawa.corona.io.service.IDicService;
import com.tida_okinawa.corona.ui.editors.DicEditorInput;

/**
 * @author s.takuro
 *         #173 パターン自動生成（共起抽出）
 */
public class ExtractCooccurrenceHandler extends AbstractExtractHandler {
    private static final int BTN_CLOSE_ID = 100;
    private int cooccurrenceNum = 2;
    private boolean isOrderCooccurrence = false;


    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell myShell = HandlerUtil.getActiveShell(event);
        /* パターン辞書が開かれているかどうかの判定 */
        boolean openPatternDic = false;
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (page != null) {
            IEditorReference[] editors = page.getEditorReferences();
            for (IEditorReference editor : editors) {
                try {
                    if (editor.getEditorInput() instanceof DicEditorInput) {
                        DicEditorInput dicInput = (DicEditorInput) editor.getEditorInput();
                        ICoronaDic coronaDic = dicInput.getDictionary();
                        if (coronaDic instanceof IPatternDic) {
                            openPatternDic = true;
                            break;
                        }
                    }
                } catch (PartInitException e) {
                    e.printStackTrace();
                }
            }
        }
        if (openPatternDic != true) {
            if (openExtractCooccurrenceDialogSetting(myShell)) {
                openExtractCooccurrenceDialog(myShell);
            }
        } else {
            errorMessageBox(myShell, Messages.EXTRACT_HANDLER_ERROR_PATTERNDIC_OPENED);
        }
        return null;
    }


    @Override
    protected boolean isEnabled(ClaimWorkDataType type) {
        switch (type) {
        case CORRECTION_MISTAKES:
            return true;
        default:
            return false;
        }
    }


    /**
     * 共起条件のダイアログを表示する
     * 
     * @param myShell
     *            自身のシェル
     * 
     * @return true:[OK]押下、false:[キャンセル]押下
     */
    private boolean openExtractCooccurrenceDialogSetting(Shell myShell) {
        ExtractCooccurrenceDialogSetting dialog = new ExtractCooccurrenceDialogSetting(myShell);
        if (dialog.open() == IDialogConstants.OK_ID) {
            cooccurrenceNum = dialog.getExtractCount();
            isOrderCooccurrence = dialog.getOrderCooccurrence();
            return true;
        }
        return false;
    }


    /**
     * 共起抽出結果のダイアログを表示する
     * 
     * @param myShell
     *            自身のシェル
     */
    private void openExtractCooccurrenceDialog(Shell myShell) {
        IDicService service = IoActivator.getDicUtil();
        ExtractCooccurrenceDialog dialog = new ExtractCooccurrenceDialog(myShell);
        dialog.setTableContentProvider(new ArrayContentProvider());
        dialog.setTableLabelProvider(new CooccurrenceListTableLabelProvider());

        /* 誤記補正後の問い合わせデータの有無 */
        if (uiWork == null) {
            errorMessageBox(myShell, Messages.EXTRACT_HANDLER_ERROR_CLAIMDATA_NOTHING);
            return;
        }

        /* 辞書名の設定 */
        List<IDicName> dicNames = null;
        if (service.getDicName(DicType.PATTERN) != null) {
            Object[] names = service.getDicName(DicType.PATTERN);
            dicNames = new ArrayList<IDicName>(names.length);
            for (Object name : names) {
                if (name instanceof IDicName) {
                    dicNames.add((IDicName) name);
                }
            }
        }
        if (dicNames.size() > 0) {
            dialog.setDicName(dicNames);
        } else {
            errorMessageBox(myShell, Messages.EXTRACT_HANDLER_ERROR_PATTERNDIC_NOTHING);
            return;
        }

        /* 共起抽出 */
        List<ExtractCooccurrenceElement> result = createTmtList();
        if (result.size() > 0) {
            dialog.setExtractCooccurrenceElement(result);
            dialog.setExtractCount(cooccurrenceNum);
        } else {
            /* CollocationExtractSequenceにてエラーメッセージを出力しているので、ここでのエラーメッセージは不要 */
            return;
        }

        /* ダイアログオープン */
        if (dialog.open() == BTN_CLOSE_ID) {
            // not to do
        }
    }


    /**
     * 共起語抽出（TMT）
     * 
     * @return 共起語リスト
     */
    private List<ExtractCooccurrenceElement> createTmtList() {
        List<ExtractCooccurrenceElement> extractCooccurrenceElement = null;
        IClaimWorkData claimWorkData = uiWork.getObject();
        List<String> misstakesList = new ArrayList<String>();
        List<ITextRecord> workDatas = claimWorkData.getClaimWorkDatas();
        for (ITextRecord workData : workDatas) {
            misstakesList.add(workData.getText());
        }
        try {
            /* TMT実行 */
            CollocationExtractSequence collocationExtract = new CollocationExtractSequence();
            List<String> tmtResult = collocationExtract.exec(misstakesList, cooccurrenceNum, isOrderCooccurrence);
            /* 実行結果の保存 */
            extractCooccurrenceElement = new ArrayList<ExtractCooccurrenceElement>(tmtResult.size());
            for (String text : tmtResult) {
                extractCooccurrenceElement.add(new ExtractCooccurrenceElement(text));
            }
            /* ソート */
            Collections.sort(extractCooccurrenceElement, new Comparator<ExtractCooccurrenceElement>() {
                @Override
                public int compare(ExtractCooccurrenceElement o1, ExtractCooccurrenceElement o2) {
                    int n1 = Integer.parseInt(o1.getCount());
                    int n2 = Integer.parseInt(o2.getCount());
                    /* 降順で並び替え（抽出数が多い方が上に来るように） */
                    if (n1 < n2) {
                        return 1;
                    } else if (n1 == n2) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            });
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return new ArrayList<ExtractCooccurrenceElement>(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new ArrayList<ExtractCooccurrenceElement>(0);
        }
        return extractCooccurrenceElement;
    }


    private static void errorMessageBox(Shell shell, String message) {
        MessageBox messageEnd = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        messageEnd.setText(Messages.EXTRACT_MESSAGEBOX_TEXT_ERROR);
        messageEnd.setMessage(message);
        messageEnd.open();
    }
}
