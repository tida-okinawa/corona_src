/**
 * @version $Id: DbViewExportHandler.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/16 14:48:10
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.internal.ui.actions.DictionaryExportOperation;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.ui.UIActivator;

/**
 * @author shingo-takahashi
 */
public class DbViewExportHandler extends AbstractHandler {
    private IStructuredSelection selection;
    private IWorkbenchWindow window;
    private Shell shell;
    File exportDir;
    final Map<ICoronaDic, Set<ILabelDic>> map = new HashMap<ICoronaDic, Set<ILabelDic>>();
    List<ICoronaDic> coronaDics = new ArrayList<ICoronaDic>();


    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            shell = HandlerUtil.getActiveShell(event);
            DirectoryDialog dialog = new DirectoryDialog(shell);
            dialog.setMessage("エクスポート先を指定してください");
            String path = dialog.open();
            // ファイルパスがnullの場合は処理を中止します。
            if (path == null) {
                return false;
            }
            exportDir = new File(path);
            coronaDics = new ArrayList<ICoronaDic>();

            Object appContextObj = event.getApplicationContext();
            if (appContextObj instanceof IEvaluationContext) {
                IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
                Object var = context.getDefaultVariable();
                // 選択されている辞書リスト
                if (var instanceof List<?>) {
                    for (Object o : (List<?>) var) {
                        if (o instanceof ICoronaDic) {
                            if (o instanceof IUserDic) {
                                List<ICoronaDic> ldics = IoActivator.getService().getDictionarys(ILabelDic.class);
                                for (ICoronaDic dic : ldics) {
                                    Set<Integer> id = dic.getParentIds();
                                    if (id.contains(((ICoronaDic) o).getId())) {
                                        Set<ILabelDic> list = map.get(o);
                                        if (list == null) {
                                            list = new HashSet<ILabelDic>();
                                            map.put((ICoronaDic) o, list);
                                        }
                                        list.add((ILabelDic) dic);
                                    }
                                }
                            }
                            coronaDics.add((ICoronaDic) o);
                        }
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }

        final DictionaryExportOperation op = new DictionaryExportOperation();

        Job exportJob = new Job("エクスポート") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                IStatus status = null;
                try {
                    status = op.execute(coronaDics.toArray(new ICoronaDic[coronaDics.size()]), exportDir, map, monitor);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    status = new Status(Status.ERROR, UIActivator.PLUGIN_ID, e.getLocalizedMessage());
                }

                if (status.getSeverity() == IStatus.CANCEL) {
                    CoronaActivator.getDefault().getLogger().getOutStream().println("処理がキャンセルされました。");
                }
                return status;
            }


            @Override
            public boolean belongsTo(Object family) {
                return false;
            }
        };
        exportJob.setUser(true);
        exportJob.setSystem(false);
        exportJob.schedule();

        return true;
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
         * 辞書（ラベル辞書以外）を選択している場合true
         */
        selection = (IStructuredSelection) window.getActivePage().getSelection();
        if (selection != null && selection.size() > 0) {
            for (Object item : selection.toArray()) {
                if (item instanceof ICoronaDic) {
                    // ラベル辞書のエクスポートは対象外
                    if (item instanceof ILabelDic) {
                        return false;
                    }
                } else if (item instanceof ICoronaProject) {
                    return false;
                } else if (item instanceof IClaimData) {
                    return false;
                } else {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
