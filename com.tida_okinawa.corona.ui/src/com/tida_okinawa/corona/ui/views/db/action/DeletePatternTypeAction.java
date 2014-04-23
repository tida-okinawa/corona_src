/**
 * @version $Id: DeletePatternTypeAction.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/18 16:57:25
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.views.db.action;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.PreviewDialog;
import com.tida_okinawa.corona.PreviewableAction;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.PatternType;
import com.tida_okinawa.corona.io.service.IIoService;
import com.tida_okinawa.corona.ui.Icons;

/**
 * @author kousuke-morishima
 */
public class DeletePatternTypeAction extends PreviewableAction {
    private List<PatternType> subjects;
    private List<EffectedItem> effectedItems = new ArrayList<EffectedItem>();


    /**
     * @param types
     *            delete subjects
     */
    public DeletePatternTypeAction(List<PatternType> types) {
        this.subjects = types;
    }


    /**
     * このアクションで削除できるものだけを抜き出して処理する
     * 
     * @param selection
     */
    public DeletePatternTypeAction(IStructuredSelection selection) {
        subjects = new ArrayList<PatternType>();
        for (Object o : selection.toArray()) {
            if (o instanceof PatternType) {
                subjects.add((PatternType) o);
            }
        }
    }


    @Override
    protected void doRun() {
        doDelete();
    }


    private void doDelete() {
        Set<ICoronaDic> commitDics = new TreeSet<ICoronaDic>(new Comparator<ICoronaDic>() {
            @Override
            public int compare(ICoronaDic o1, ICoronaDic o2) {
                return o1.getId() - o2.getId();
            }
        });

        /* パターンを修正 */
        for (EffectedItem effectedItem : effectedItems) {
            effectedItem.pattern.setPatternType(effectedItem.changed.getId());
            commitDics.add(effectedItem.dic);
        }
        /* 辞書をコミット */
        for (ICoronaDic dic : commitDics) {
            dic.commit(new NullProgressMonitor());
        }
        /* パターンタイプを削除 */
        IIoService service = IoActivator.getService();
        for (PatternType type : subjects) {
            service.removePatternType(type);
        }
        effectedItems.clear();
    }


    @Override
    protected boolean doRunWhenNoEffectedItems() {
        return true;
    }


    /* ****************************************
     * 影響のあるパターンを検索
     */
    @Override
    protected boolean checkEffected() {
        IIoService service = IoActivator.getService();
        List<ICoronaDic> pDics = service.getDictionarys(IPatternDic.class);
        for (PatternType type : subjects) {
            if (!removeFromDics(type, pDics)) {
                CoronaActivator.getDefault().getLogger().getOutStream()
                        .println(Messages.bind(Messages.DeletePatternTypeAction_logCantDel, new String[] { type.toString() }));
            }
        }
        return !effectedItems.isEmpty();
    }


    private boolean removeFromDics(PatternType type, List<ICoronaDic> patternDics) {
        for (ICoronaDic dic : patternDics) {
            List<IDicItem> items = dic.getItems();
            for (IDicItem item : items) {
                EffectedItem effectedItem = removeFromPattern(type, (IPattern) item);
                if (effectedItem != null) {
                    effectedItem.dic = dic;
                }
            }
        }
        return true;
    }


    /**
     * @param type
     * @param iPattern
     * @return 削除したら修正情報。それ以外ならnull
     */
    private EffectedItem removeFromPattern(PatternType type, IPattern iPattern) {
        if (iPattern.getPatternType() == type.getId()) {
            EffectedItem effectedItem = new EffectedItem();
            effectedItem.original = PatternType.getPatternType(type.getId());
            effectedItem.changed = PatternType.OTHER;
            effectedItem.pattern = iPattern;
            effectedItems.add(effectedItem);
            return effectedItem;
        }
        return null;
    }


    /* ****************************************
     * プレビューダイアログ
     */
    @Override
    protected int openPreviewDialog(Shell shell) {
        PreviewDialog dialog = getPreviewDialog(shell);
        return dialog.open();
    }


    /**
     * @param shell
     * @return ダイアログのreturnCode
     */
    private PreviewDialog getPreviewDialog(Shell shell) {
        /* ダイアログ表示 */
        PreviewDialog dialog = new PreviewDialog((shell == null) ? new Shell() : shell, false);
        StringBuilder names = new StringBuilder(15 * subjects.size());
        for (PatternType type : subjects) {
            names.append(Messages.DeletePatternTypeAction_labelCategory1).append(type.getPatternName()).append(Messages.DeletePatternTypeAction_labelCategory2);
        }
        names.append(Messages.DeletePatternTypeAction_labelCategory3);
        dialog.setMessage(names.substring(1));
        dialog.setWeight(new int[] { 6, 3 });
        dialog.setDetailLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof EffectedItem) {
                    String str = ((EffectedItem) element).original.getPatternName();
                    if (str == null || str.trim().length() == 0) {
                        str = Messages.DeletePatternTypeAction_labelNoValue;
                    }
                    return Messages.DeletePatternTypeAction_labelPatternCategory + str;
                }
                return ""; //$NON-NLS-1$
            }
        });

        dialog.setTreeContentProvider(new ITreeContentProvider() {
            Object[] items = null;


            @Override
            public Object[] getElements(Object input) {
                if (input instanceof List<?>) {
                    items = ((List<?>) input).toArray();
                    Set<ICoronaDic> root = new TreeSet<ICoronaDic>(new Comparator<ICoronaDic>() {
                        @Override
                        public int compare(ICoronaDic o1, ICoronaDic o2) {
                            return o1.getId() - o2.getId();
                        }
                    });
                    for (Object o : items) {
                        EffectedItem item = (EffectedItem) o;
                        root.add(item.dic);
                    }
                    return root.toArray();
                }

                return new Object[0];
            }


            @Override
            public Object[] getChildren(Object parent) {
                if (parent instanceof ICoronaDic) {
                    List<EffectedItem> ret = new ArrayList<EffectedItem>();
                    for (Object o : items) {
                        if (((EffectedItem) o).dic.equals(parent)) {
                            ret.add(((EffectedItem) o));
                        }
                    }
                    return ret.toArray();
                }
                return new Object[0];
            }


            @Override
            public boolean hasChildren(Object element) {
                return (element instanceof ICoronaDic);
            }


            @Override
            public Object getParent(Object element) {
                if (element instanceof EffectedItem) {
                    return ((EffectedItem) element).dic;
                }
                return null;
            }


            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }


            @Override
            public void dispose() {
            }
        });

        dialog.setTreeLabelProvider(new LabelProvider() {
            @Override
            public Image getImage(Object element) {
                if (element instanceof EffectedItem) {
                    if (((EffectedItem) element).pattern.isParts()) {
                        return Icons.INSTANCE.get(Icons.IMG_PATTERN_PART);
                    }
                    return Icons.INSTANCE.get(Icons.IMG_PATTERN_RECORD);
                } else if (element instanceof IPatternDic) {
                    return Icons.INSTANCE.get(Icons.IMG_DIC_PATTERN);
                }
                return null;
            }
        });

        dialog.setInput(effectedItems);
        return dialog;
    }


    /* ****************************************
     * other
     */

    @Override
    public boolean isEnabled() {
        return subjects.size() > 0;
    }


    @Override
    public String getText() {
        return Messages.DeletePatternTypeAction_labelDelPatternCategory;
    }


    @Override
    public String getToolTipText() {
        return ""; //$NON-NLS-1$
    }

    static class EffectedItem {
        public EffectedItem() {
        }

        ICoronaDic dic;
        IPattern pattern;
        PatternType original;
        PatternType changed;


        @Override
        public String toString() {
            String str = pattern.getLabel();
            if (str.trim().length() == 0) {
                str = Messages.DeletePatternTypeAction_labelId + pattern.getId();
            }
            return str;
        }
    }
}
