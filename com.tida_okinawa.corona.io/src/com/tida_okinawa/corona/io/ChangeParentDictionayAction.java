/**
 * @version $Id: ChangeParentDictionayAction.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/21 14:43:53
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;

import com.tida_okinawa.corona.PreviewDialog;
import com.tida_okinawa.corona.PreviewableAction;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDepend;
import com.tida_okinawa.corona.io.model.dic.IDependDic;
import com.tida_okinawa.corona.io.model.dic.IDependSub;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.ITerm;

/**
 * 親辞書変更アクション
 * 
 * @author kousuke-morishima
 */
public class ChangeParentDictionayAction extends PreviewableAction {

    private ICoronaDic child;
    private List<ICoronaDic> parents;


    /**
     * 親辞書変更アクションのコンストラクタ
     * 
     * @param child
     *            親辞書を変更する辞書
     * @param newParents
     *            新しい親辞書
     */
    public ChangeParentDictionayAction(ICoronaDic child, List<ICoronaDic> newParents) {
        this.child = child;
        this.parents = newParents;
    }


    @Override
    protected void doRun() {
        // 消えてしまった親辞書に関連する単語を削除する
        if (child instanceof ILabelDic) {
            for (Entry<ITerm, List<IDicItem>> e : effectedItems.entrySet()) {
                for (IDicItem item : e.getValue()) {
                    ((ILabel) item).removeTerm(e.getKey());
                }
            }
        } else if (child instanceof IDependDic) {
            for (Entry<ITerm, List<IDicItem>> e : effectedItems.entrySet()) {
                for (IDicItem item : e.getValue()) {
                    if (item instanceof IDependSub) {
                        ((IDependSub) item).getParent().removeSub(e.getKey());
                        IDicItem parent = ((IDependSub) item).getParent();
                        if (((IDepend) parent).getSub().size() == 0) {
                            child.removeItem(parent);
                        }
                    } else {
                        child.removeItem(item);
                    }
                }
            }
        }

        // 新しく増えた親辞書を、子辞書にセットする
        for (Integer id : deletedParents) {
            child.removeParentId(id);
        }
        for (Integer id : newParents) {
            child.addParentId(id);
        }

        // 更新
        child.commit(new NullProgressMonitor());
    }

    /* ****************************************
     * preview
     */
    /**
     * 親辞書ID、影響を受ける単語
     */
    private Map<Integer, List<IDicItem>> effectedTerms = new HashMap<Integer, List<IDicItem>>();
    /**
     * 影響を受ける単語、単語を保持しているアイテム（IDependかILabel）
     */
    private Map<ITerm, List<IDicItem>> effectedItems = new HashMap<ITerm, List<IDicItem>>();
    /**
     * 親の付け替えによって、消えてしまった親辞書
     */
    private Set<Integer> deletedParents = null;
    private Set<Integer> newParents = null;


    @Override
    protected boolean checkEffected() {
        effectedTerms.clear();
        effectedItems.clear();

        deletedParents = new HashSet<Integer>(child.getParentIds());
        newParents = new HashSet<Integer>();
        for (ICoronaDic parent : parents) {
            if (!deletedParents.remove(parent.getId())) {
                newParents.add(parent.getId());
            }
        }

        if (deletedParents.isEmpty() && newParents.isEmpty()) {
            /* 消えた親も新しい親もいなければなにもしない */
            return false;
        }

        if (child instanceof IDependDic) {
            List<IDicItem> items = child.getItems();
            for (IDicItem item : items) {
                /* 消えた親の中に代表語が含まれていれば影響アイテムに追加 */
                IDepend depend = (IDepend) item;
                if (deletedParents.contains(depend.getMain().getComprehensionDicId())) {
                    addItem(depend.getMain(), depend);
                }
                /* 消えた親の中に従属語が含まれていれば影響アイテムに追加 */
                for (Entry<Integer, IDependSub> e : depend.getSubs().entrySet()) {
                    if (deletedParents.contains(e.getValue().getTerm().getComprehensionDicId())) {
                        addItem(e.getValue().getTerm(), e.getValue());
                    }
                }
            }
        } else if (child instanceof ILabelDic) {
            List<IDicItem> items = child.getItems();
            for (IDicItem item : items) {
                checkLabelItem((ILabel) item);
            }
        }

        return true;
    }


    /**
     * 親辞書の変更によって、ラベルアイテムが影響を受けるか確認する.
     * label が影響を受けるアイテムであれば、リストに蓄えられる。
     * 
     * @param label
     *            検査対象のラベル
     */
    private void checkLabelItem(ILabel label) {
        for (ITerm term : label.getTerms()) {
            if (deletedParents.contains(term.getComprehensionDicId())) {
                addItem(term, label);
            }
        }
        for (ILabel child : label.getChildren()) {
            checkLabelItem(child);
        }
    }


    /**
     * 影響を受けたアイテムをリストに追加
     * 
     * @param term
     *            親辞書変更の影響を受けたアイテムに紐付く単語
     * @param item
     *            親辞書変更の影響を受けたアイテム
     */
    private void addItem(ITerm term, IDicItem item) {
        List<IDicItem> terms = effectedTerms.get(term.getComprehensionDicId());
        if (terms == null) {
            terms = new ArrayList<IDicItem>();
            effectedTerms.put(term.getComprehensionDicId(), terms);
        }
        if (item instanceof ILabel) {
            /* ラベルの場合 */
            if (!terms.contains(term)) {
                terms.add(term);
            }
        } else {
            /* ゆらぎ・同義語の場合 */
            terms.add(item);
        }
        List<IDicItem> items = effectedItems.get(term);
        if (items == null) {
            items = new ArrayList<IDicItem>();
            effectedItems.put(term, items);
        }
        items.add(item);
    }


    @Override
    protected int openPreviewDialog(Shell shell) {
        /* 影響を受けるアイテムがあったか確認する */
        boolean openDialog = false;
        for (Entry<ITerm, List<IDicItem>> e : effectedItems.entrySet()) {
            if (!e.getValue().isEmpty()) {
                openDialog = true;
            }
        }

        if (!openDialog) {
            return Dialog.OK;
        }

        PreviewDialog dialog = new PreviewDialog(shell, false);
        dialog.setTreeContentProvider(new ITreeContentProvider() {
            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }


            @Override
            public void dispose() {
            }


            @Override
            public boolean hasChildren(Object element) {
                return (element instanceof Integer);
            }


            @Override
            public Object getParent(Object element) {
                return null;
            }


            @Override
            public Object[] getElements(Object input) {
                if (input instanceof Map<?, ?>) {
                    return ((Map<?, ?>) input).keySet().toArray();
                }
                return new Object[0];
            }


            @Override
            public Object[] getChildren(Object parent) {
                if (parent instanceof Integer) {
                    return getItems((Integer) parent).toArray();
                }
                return new Object[0];
            }
        });
        dialog.setTreeLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof IDepend) {
                    /* 代表語を返す */
                    return Messages.ChangeParentDictionayAction_labelParentWord + ((IDepend) element).getMain().getValue();
                } else if (element instanceof IDependSub) {
                    /* 従属語を返す */
                    return Messages.ChangeParentDictionayAction_labelChildWord + ((IDependSub) element).getTerm().getValue();
                } else if (element instanceof ITerm) {
                    /* 用語を返す */
                    return ((ITerm) element).getValue();
                } else if (element instanceof Integer) {
                    /* 辞書名を返す */
                    ICoronaDic dic = IoActivator.getService().getDictionary((Integer) element);
                    if (dic != null) {
                        return dic.getName();
                    }
                }
                return "ERROR"; //$NON-NLS-1$
            }
        });
        dialog.setDetailLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                StringBuffer buf = new StringBuffer(128);
                if (element instanceof IDepend) {
                    /* 対象が代表語の場合、従属語を設定する */
                    List<ITerm> details = ((IDepend) element).getSub();
                    for (ITerm d : details) {
                        buf.append(Messages.ChangeParentDictionayAction_labelChildWord).append(d.getValue())
                                .append(Messages.ChangeParentDictionayAction_newLine);
                    }
                } else if (element instanceof IDependSub) {
                    /* 対象が従属語の場合、代表語を設定する */
                    buf.append(Messages.ChangeParentDictionayAction_labelParentWord).append(((IDependSub) element).getParent().getMain().getValue())
                            .append(Messages.ChangeParentDictionayAction_newLine);
                } else if (element instanceof ITerm) {
                    /* ラベル名を設定する */
                    List<IDicItem> details = getDetails((ITerm) element);
                    if (!details.isEmpty()) {
                        for (IDicItem item : details) {
                            buf.append(Messages.ChangeParentDictionayAction_labelLabel).append(((ILabel) item).getName())
                                    .append(Messages.ChangeParentDictionayAction_newLine);
                        }
                    }
                } else if (element instanceof Integer) {
                    /* 対象すべてを設定する */
                    buf = new StringBuffer(1024 * 1024);
                    Set<IDicItem> only = new HashSet<IDicItem>();
                    for (IDicItem item : getItems((Integer) element)) {
                        ITerm term = null;
                        if (item instanceof ITerm) {
                            term = (ITerm) item;
                        } else if (item instanceof IDepend) {
                            term = ((IDepend) item).getMain();
                        } else if (item instanceof IDependSub) {
                            term = ((IDependSub) item).getTerm();
                        }
                        only.addAll(getDetails(term));
                    }
                    for (IDicItem item : only) {
                        if (item instanceof IDepend) {
                            buf.append(Messages.ChangeParentDictionayAction_labelParentWord).append(((IDepend) item).getMain().getValue())
                                    .append(Messages.ChangeParentDictionayAction_newLine);
                        } else if (item instanceof IDependSub) {
                            buf.append(Messages.ChangeParentDictionayAction_labelChildWord).append(((IDependSub) item).getTerm().getValue())
                                    .append(Messages.ChangeParentDictionayAction_newLine);
                        } else if (item instanceof ILabel) {
                            buf.append(Messages.ChangeParentDictionayAction_labelLabel).append(((ILabel) item).getName())
                                    .append(Messages.ChangeParentDictionayAction_newLine);
                        }
                    }
                }
                return buf.toString();
            }
        });
        if (child instanceof ILabelDic) {
            dialog.setMessage(Messages.ChangeParentDictionayAction_dialogDelLabel);
            dialog.setDetailLabelText(Messages.ChangeParentDictionayAction_dialogDelLabelWord);
        } else {
            dialog.setMessage(Messages.ChangeParentDictionayAction_dialogDelRelation);
            dialog.setDetailLabelText(Messages.ChangeParentDictionayAction_dialogDelRelationWord);
        }
        dialog.setInput(effectedTerms);
        return dialog.open();
    }


    List<IDicItem> getItems(Integer dicId) {
        return effectedTerms.get(dicId);
    }


    List<IDicItem> getDetails(ITerm term) {
        return effectedItems.get(term);
    }
}
