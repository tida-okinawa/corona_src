/**
 * @version $Id: DbViewDeleteHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/09 19:19:23
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.TextItem;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.model.dic.PatternType;
import com.tida_okinawa.corona.ui.views.db.DataBaseView;

/**
 * 
 * @author kyohei-miyazato
 */
public class DbViewDeleteHandler extends AbstractHandler {
    private IWorkbenchWindow window = null;
    private IStructuredSelection selection = null;


    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        /*
         * 削除処理
         */
        IWorkbenchPart part = window.getActivePage().getActivePart();
        Shell shell = HandlerUtil.getActiveShell(event);
        Boolean refFlg = false;
        HashMap<String, String> regMap = new HashMap<String, String>();
        HashMap<String, String> referDics = new HashMap<String, String>();
        if (part instanceof DataBaseView) {
            /* 削除する辞書の部品パターンが他の辞書から参照されていないか確認する */
            /* 複数辞書が選択されている場合ループで回す */
            for (Object obj : selection.toArray()) {
                if (obj instanceof IPatternDic) {

                    /* TODO DeleteActionのパターン辞書削除処理と共通クラス化したい */
                    StringBuilder items = new StringBuilder();
                    List<ICoronaDic> patternDics = IoActivator.getService().getDictionarys(IPatternDic.class);
                    patternDics.remove(obj);
                    /* パターン辞書のtext部分を結合 */
                    for (ICoronaDic patternDic : patternDics) {
                        for (IDicItem item : patternDic.getItems()) {
                            items.append(((IPattern) item).getText());
                        }
                        regMap.put(patternDic.getName(), items.toString());
                        /* StringBuilderの初期化 */
                        items.setLength(0);
                    }

                    /* 選択された辞書がほかの辞書から参照されていないか確認 */
                    List<IDicItem> selItems = ((IPatternDic) obj).getItems();
                    String regular = ".*LINK ID="; //$NON-NLS-1$
                    for (IDicItem selItem : selItems) {
                        StringBuilder p = new StringBuilder();
                        p.append(regular).append("\"").append(selItem.getId()).append("\".*"); //$NON-NLS-1$ //$NON-NLS-2$
                        for (Map.Entry<String, String> e : regMap.entrySet()) {
                            if (Pattern.matches(p.toString(), e.getValue())) {
                                referDics.put(e.getKey(), ((IPattern) selItem).getLabel());
                                refFlg = true;
                            }
                        }
                    }
                }
            }

            if (refFlg) {
                /* 他辞書から参照されている場合 */
                StringBuilder message = new StringBuilder();
                message.append(Messages.DbViewDeleteHandler_labelRefPatternDic);
                for (Map.Entry<String, String> e : referDics.entrySet()) {
                    message.append(Messages.DbViewDeleteHandler_labelPatternDic + e.getKey() + "\n"); //$NON-NLS-1$
                }
                MessageDialog.openError(shell, Messages.DbViewDeleteHandler_titleRefPatternDic, message.toString());

            } else {
                /* 他辞書から参照されていない場合 */
                if (MessageDialog.openConfirm(shell, Messages.DbViewDeleteHandler_dialogDelData, DataBaseView.deleteMessage)) {
                    DataBaseView dbView = (DataBaseView) part;
                    dbView.remove(selection.toArray());
                    CoronaActivator.debugLog(Messages.DbViewDeleteHandler_logEndOfDelJob);
                }
            }
        }

        return null;
    }


    @Override
    public boolean isEnabled() {
        window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return false;
        }

        /*
         * 何も選択されていなければfalse
         * 単/複数選択時、フォルダを含んで選択している場合はfalse
         * プロジェクト、辞書、問い合わせデータを選択している場合true
         */
        selection = (IStructuredSelection) window.getActivePage().getSelection();
        if (selection != null && selection.size() > 0) {
            for (Object item : selection.toArray()) {
                if (item instanceof String) {
                    return false;
                } else if (item instanceof ICoronaDic) {
                    if (item instanceof IUserDic) {
                        if (DicType.JUMAN.equals(((IUserDic) item).getDicType())) {
                            return false;
                        }
                    }
                } else if (item instanceof ICoronaProject) {
                } else if (item instanceof IClaimData) {
                } else if (item instanceof PatternType) {
                } else if (item instanceof TextItem) {
                } else {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
