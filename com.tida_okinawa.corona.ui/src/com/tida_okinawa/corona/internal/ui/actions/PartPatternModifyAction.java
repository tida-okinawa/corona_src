/**
 * @version $Id: PartPatternModifyAction.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/27 13:15:09
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.actions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.tida_okinawa.corona.PreviewDialog;
import com.tida_okinawa.corona.PreviewableAction;
import com.tida_okinawa.corona.correction.parsing.model.Link;
import com.tida_okinawa.corona.correction.parsing.model.Pattern;
import com.tida_okinawa.corona.correction.parsing.model.PatternContainer;
import com.tida_okinawa.corona.correction.parsing.model.PatternRecord;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.ui.Icons;
import com.tida_okinawa.corona.ui.editors.pattern.PatternDicContentProvider;
import com.tida_okinawa.corona.ui.editors.pattern.PatternDicLabelProvider;

/**
 * パターンを削除したり、パターンの参照可能フラグをfalseにしたときに、影響を受けるパターンを探す。
 * 
 * @author kousuke-morishima
 */
public class PartPatternModifyAction extends PreviewableAction {
    private List<PatternRecord> patterns;
    private PatternRecord patternRecord = null;


    /**
     * @param patterns
     *            エディタに表示されているパターンすべて
     */
    public PartPatternModifyAction(List<PatternRecord> patterns) {
        this.patterns = patterns;
    }


    /**
     * @param pattern
     *            参照不可能に変更したか、削除したパターン
     */
    public void set(PatternRecord pattern) {
        this.patternRecord = pattern;
    }


    @Override
    protected void doRun() {
        for (Link link : effectedItems) {
            link.setLinkPattern(null);
        }
    }

    private List<Link> effectedItems = null;


    @Override
    protected boolean checkEffected() {
        effectedItems = new ArrayList<Link>();

        patterns.remove(patternRecord);
        for (PatternRecord rec : patterns) {
            /* recが、patternRecordを参照しているかどうか確認する */
            if (rec.hasChildren()) {
                check(rec.getChildren().get(0));
            }
        }
        return !effectedItems.isEmpty();
    }


    private void check(Pattern pattern) {
        if (pattern instanceof Link) {
            Link link = (Link) pattern;
            IPattern linkPattern = link.getLinkPattern();
            if (linkPattern != null) {
                /* リンクしているパターンのisPart()がfalseなら参照を外す */
                if (linkPattern.equals(patternRecord.getIPattern())) {
                    PatternContainer parent = link.getParent();
                    while ((parent != null) && !(parent instanceof PatternRecord)) {
                        parent = parent.getParent();
                    }
                    if (((PatternRecord) parent).getIPattern().getComprehensionDicId() != ICoronaDic.UNSAVED_ID) {
                        effectedItems.add(link);
                    }
                }
            }
        } else if (pattern instanceof PatternContainer) {
            /* 子をチェック */
            for (Pattern child : ((PatternContainer) pattern).getChildren()) {
                check(child);
            }
        }
    }


    @Override
    protected int openPreviewDialog(Shell shell) {
        // TODO open
        PreviewDialog d = new PreviewDialog(shell, false) {
            @Override
            protected void createDetailArea(Composite parent) {
                // not create
            }


            @Override
            protected void selectionChanged(IStructuredSelection selection) {
            }
        };
        d.setTreeContentProvider(new PatternDicContentProvider() {
            Object[] items = null;


            @Override
            public Object[] getElements(Object input) {
                if (input instanceof PatternRecord[]) {
                    items = (PatternRecord[]) input;
                    Set<ICoronaDic> root = new TreeSet<ICoronaDic>(new Comparator<ICoronaDic>() {
                        @Override
                        public int compare(ICoronaDic o1, ICoronaDic o2) {
                            return o1.getId() - o2.getId();
                        }
                    });

                    for (Object o : items) {
                        int dicId = ((PatternRecord) o).getIPattern().getComprehensionDicId();
                        ICoronaDic dic = IoActivator.getService().getDictionary(dicId);
                        root.add(dic);
                    }
                    return root.toArray();
                }
                return new Object[0];
            }


            @Override
            public Object[] getChildren(Object parent) {
                if (parent instanceof ICoronaDic) {

                    List<PatternRecord> recs = new ArrayList<PatternRecord>();
                    for (Object o : items) {
                        if (((PatternRecord) o).getIPattern().getComprehensionDicId() == ((ICoronaDic) parent).getId()) {
                            recs.add((PatternRecord) o);
                        }
                    }
                    return recs.toArray();
                }
                return new Object[0];
            }


            @Override
            public boolean hasChildren(Object element) {
                return (element instanceof ICoronaDic);
            }


            @Override
            public Object getParent(Object element) {
                if (element instanceof PatternRecord) {
                    int dicId = ((PatternRecord) element).getIPattern().getComprehensionDicId();
                    return IoActivator.getService().getDictionary(dicId);
                }
                return null;
            }

        });
        d.setTreeLabelProvider(new PatternDicLabelProvider() {
            @Override
            public Image getImage(Object element) {
                if (element instanceof IPatternDic) {
                    return Icons.INSTANCE.get(Icons.IMG_DIC_PATTERN);
                }
                return super.getImage(element);
            }
        });
        d.setInput(createRecords(effectedItems));
        /* #755 アイテムが単体の場合にアイテム名を表記するように対応 */
        if (effectedItems.size() == 1) {
            d.setMessage(effectedItems.get(0).getLabel() + "の" + Messages.PartPatternModifyAction_labelRefPattern);
        } else {
            d.setMessage(Messages.PartPatternModifyAction_labelRefPattern);
        }
        d.setWeight(new int[] { 1 });
        return d.open();
    }


    private static PatternRecord[] createRecords(List<Link> effectedLinks) {
        Set<PatternRecord> ret = new HashSet<PatternRecord>(effectedLinks.size());
        for (Link link : effectedLinks) {
            PatternContainer parent = link.getParent();
            while ((parent != null) && !(parent instanceof PatternRecord)) {
                parent = parent.getParent();
            }
            ret.add((PatternRecord) parent);
        }
        return ret.toArray(new PatternRecord[ret.size()]);
    }

    // TODO 関係ないパターンを除いて、参照パターンまでの最短パスだけを表示する試み。失敗中
    // private PatternContainer create(Pattern child) {
    // PatternContainer parent = child.getParent();
    // if (parent != null) {
    // parent = parent.getClass().newInstance();
    // }
    // try {
    // Pattern pattern = child.newInstance();
    // return pattern;
    // } catch (InstantiationException e) {
    // e.printStackTrace();
    // } catch (IllegalAccessException e) {
    // e.printStackTrace();
    // }
    // return parent;
    // }

}
