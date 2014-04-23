/**
 * @version $Id: ExtractRelationHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/07 13:07:23
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.extract;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicName;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.service.IDicService;
import com.tida_okinawa.corona.ui.editors.DicEditorInput;

/**
 * @author s.takuro
 *         #177 パターン自動生成（係り受け抽出）
 */
public class ExtractRelationHandler extends AbstractExtractHandler {
    private static final int BTN_CLOSE_ID = 100;


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
            openExtractRelationDialog(myShell);
        } else {
            errorMessageBox(myShell, Messages.EXTRACT_HANDLER_ERROR_PATTERNDIC_OPENED);
        }
        return null;
    }


    @Override
    protected boolean isEnabled(ClaimWorkDataType type) {
        switch (type) {
        case DEPENDENCY_STRUCTURE:
            return true;
        case CORRECTION_SYNONYM:
            return true;
        default:
            return false;
        }
    }


    /**
     * 係り受け抽出結果のダイアログを表示する
     * 
     * @param myShell
     *            自身のシェル
     */
    private void openExtractRelationDialog(Shell myShell) {
        IDicService service = IoActivator.getDicUtil();
        ExtractRelationDialog dialog = new ExtractRelationDialog(myShell);
        dialog.setMorphemeListTableContentProvider(new ArrayContentProvider());
        dialog.setMorphemeListTableLabelProvider(new MorphemeListTableLabelProvider());
        dialog.setMorphemeDetailTreeContentProvider(new ArrayContentProvider());
        dialog.setMorphemeDetailTableLabelProvider(new MorphemeDetailTableLabelProvider());

        if (uiWork == null) {
            errorMessageBox(myShell, Messages.EXTRACT_HANDLER_ERROR_CLAIMDATA_NOTHING);
            return;
        }
        /* 係り受け解析後のクレームデータの設定 */
        IClaimWorkData claimWorkData = uiWork.getObject();
        if (claimWorkData.getNote().indexOf(Messages.EXTRACT_RELATION_TRUE) == -1) {
            errorMessageBox(myShell, Messages.EXTRACT_HANDLER_ERROR_RELATION);
            return;
        }
        IExtractRelationElement[] ereList = createTmtList(claimWorkData);
        if (ereList.length > 0) {
            dialog.setExtractRelationList(ereList);
        } else {
            errorMessageBox(myShell, Messages.EXTRACT_HANDLER_ERROR_ONCE_ONLY_RELATION);
            return;
        }

        /* 辞書名の設定 */
        List<IDicName> dicNames = null;
        if (service.getDicName(DicType.PATTERN) != null) {
            IDicName[] names = service.getDicName(DicType.PATTERN);
            dicNames = new ArrayList<IDicName>(names.length);
            for (IDicName name : names) {
                dicNames.add(name);
            }
        }
        if (dicNames.size() > 0) {
            dialog.setDicName(dicNames);
        } else {
            errorMessageBox(myShell, Messages.EXTRACT_HANDLER_ERROR_PATTERNDIC_NOTHING);
            return;
        }

        /* ダイアログオープン */
        if (dialog.open() == BTN_CLOSE_ID) {
            // not to do
        }
    }


    @SuppressWarnings("static-method")
    private IExtractRelationElement[] createTmtList(IClaimWorkData claimWorkData) {
        ExtractRelation extractRelation = new ExtractRelation();
        try {
            return extractRelation.execRelation(claimWorkData);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new IExtractRelationElement[0];
    }


    private static void errorMessageBox(Shell shell, String message) {
        MessageBox messageEnd = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        messageEnd.setText(Messages.EXTRACT_MESSAGEBOX_TEXT_ERROR);
        messageEnd.setMessage(message);
        messageEnd.open();
    }
}
