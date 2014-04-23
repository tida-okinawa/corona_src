/**
 * @version $Id: LabelRelationGroup.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/20 10:12:48
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.PageBook;

import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.ITerm;

/**
 * @author kousuke-morishima
 */
public class LabelRelationGroup extends LabelTreeGroup {

    private int userDicId;


    public LabelRelationGroup(Composite parent, int style) {
        super(parent, style);
    }

    /*
     * TODO pageBook = null にすると、super経由で初期化された pageBook がここで再び null にされてしまう。
     * クラス構造が悪いように思うが、今は直す工数がないので null初期化をコメントアウトする
     */
    private PageBook pageBook/* = null */;
    private Label noRelationLabel;
    private Control relatedLabelContent;


    @Override
    protected Control createContents(Composite parent, int style) {
        pageBook = new PageBook(parent, SWT.NONE);
        noRelationLabel = new Label(pageBook, SWT.LEFT | SWT.TOP | SWT.WRAP);
        noRelationLabel.setText(Messages.LabelRelationGroup_textFailShowLabel);

        relatedLabelContent = super.createContents(pageBook, style);
        /* #767 ラベルツリーを展開した状態で表示 */
        viewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
        viewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                Object element = event.getElement();

                if (element instanceof ILabel) {
                    ILabel label = (ILabel) event.getElement();

                    /* #767 複数選択のチェック */

                    // 用語に紐付くラベルを外すかどうかをチェックするフラグ(true:ラベルを外す)
                    boolean labelRemoveFlag = true;
                    List<ITerm> checkTerms = label.getTerms();

                    // 選択した用語が複数ある場合、選択した用語すべてにラベルが紐づいているかチェックする
                    // 紐づいてい無いものが選択されている場合、既存のラベルを外さないようフラグをfalseにする。
                    if (sourceTerms.size() > 1) {
                        for (ITerm term : sourceTerms) {
                            if (!checkTerms.contains(term)) {
                                labelRemoveFlag = false;
                                break;
                            }
                        }
                    }

                    for (ITerm sourceTerm : sourceTerms) {
                        List<ILabel> labels = termToLabelMap.get(sourceTerm);
                        if (labels == null) {
                            labels = new ArrayList<ILabel>();
                            termToLabelMap.put(sourceTerm, labels);
                        }
                        /* #767 ラベルが紐付いているかどうかで判定するように変更 */
                        if (labels.contains(label)) {
                            // ラベルが紐づいている場合、、紐づけを外すかどうかのフラグをチェックする
                            if (labelRemoveFlag) {
                                labels.remove(label);
                            }
                        } else {
                            // ラベルが用語に紐づくように追加する(半チェック状態または未チェック状態)
                            labels.add(label);
                        }
                    }
                } else if (element instanceof ILabelDic) {
                    // 辞書はチェックさせない
                    ((CheckboxTreeViewer) event.getSource()).setChecked(element, false);
                }
            }
        });

        return pageBook;
    }

    /* ****************************************
     * 関連するラベル辞書の管理
     */
    private List<ILabelDic> relatedLabelDics = new ArrayList<ILabelDic>();


    public List<ILabelDic> getRelatedlabelDics() {
        return relatedLabelDics;
    }


    /**
     * @param relatedLabelDics
     *            must not null. 表示するラベル辞書
     * @param userDicId
     *            表示するラベル辞書の親ユーザ辞書ID
     */
    public void setRelatedLabelDics(List<ILabelDic> relatedLabelDics, int userDicId) {
        /* ほかで開かれたラベル辞書が編集された場合の同期設定 */
        final ObjectSyncManager sync = ObjectSyncManager.INSTANCE;
        for (ILabelDic dic : this.relatedLabelDics) {
            sync.removeListener(dic, this);
        }
        if (relatedLabelDics == null) {
            this.relatedLabelDics.clear();
        } else {
            this.relatedLabelDics = relatedLabelDics;
        }
        reCreateContent();

        for (ILabelDic dic : this.relatedLabelDics) {
            sync.addListener(dic, this);
        }

        super.setInput(this.relatedLabelDics);
        this.userDicId = userDicId;
    }


    /**
     * @deprecated このクラスでは、ユーザ辞書IDが必要なので、
     *             {@link #setRelatedLabelDics(List, int)}を使ってください
     */
    @Deprecated
    @Override
    public void setInput(List<ILabelDic> inputDics) {
        super.setInput(inputDics);
    }


    /**
     * このユーザ辞書と関連するラベル辞書を増やす<br />
     * ユーザ辞書を開いた後で作成されたラベル辞書が認識されないので、このメソッドで追加する
     * 
     * @param labelDic
     */
    public void addRelated(ILabelDic labelDic) {
        Assert.isNotNull(labelDic); // Assert

        for (int parentId : labelDic.getParentIds()) {
            if (parentId == userDicId) {
                if (!relatedLabelDics.contains(labelDic)) {
                    relatedLabelDics.add(labelDic);
                    reCreateContent();
                    ObjectSyncManager.INSTANCE.addListener(labelDic, this);
                    refresh();
                }
                break;
            }
        }
    }


    /**
     * このユーザ辞書と関連しているラベル辞書を減らす<br />
     * ラベル辞書そのものが削除された場合に使用する
     * 
     * @param labelDic
     */
    public void removeRelated(ILabelDic labelDic) {
        Assert.isNotNull(labelDic); // Assert

        if (relatedLabelDics.remove(labelDic)) {
            ObjectSyncManager.INSTANCE.removeListener(labelDic, this);
            if (relatedLabelDics.size() == 0) {
                reCreateContent();
            }
            refresh();
        }
    }

    /*
     * 単語とラベルの紐づけ処理
     */
    Map<ITerm, List<ILabel>> termToLabelMap = new HashMap<ITerm, List<ILabel>>();


    /**
     * @param term
     * @return termに紐づいているラベルの一覧。
     */
    public List<ILabel> getRelatedLabels(ITerm term) {
        /* ラベル列がなくなればやらなくていい処理 */
        /* LabelDicが保持しているITermが、今開いているITermと違うので、動的に作る */
        /* TODO ラベル辞書で削除したとき、ユーザ辞書でもラベル列の値が消えるといい */
        List<ILabel> ret = termToLabelMap.get(term);
        if (ret == null) {
            ret = new ArrayList<ILabel>();
            termToLabelMap.put(term, ret);

            List<ILabel> topLabels = new ArrayList<ILabel>();
            for (ILabelDic dic : this.relatedLabelDics) {
                List<IDicItem> items = dic.getItems();
                for (IDicItem item : items) {
                    topLabels.add((ILabel) item);
                }
            }
            mapping(topLabels, term, ret);
        }
        return ret;
    }


    private void mapping(List<ILabel> children, ITerm key, List<ILabel> result) {
        for (ILabel l : children) {
            List<ITerm> terms = l.getTerms();
            for (ITerm t : terms) {
                if (key.equals(t)) {
                    result.add(l);
                    break;
                }
            }
            mapping(l.getChildren(), key, result);
        }
    }


    private void reCreateContent() {
        assert pageBook != null;
        if (relatedLabelDics.size() == 0) {
            pageBook.showPage(noRelationLabel);
        } else {
            pageBook.showPage(relatedLabelContent);
        }
    }


    /**
     * ラベルと単語の紐づけを保持しているキャッシュをクリアする。
     */
    public void clearCache() {
        termToLabelMap.clear();
    }


    public void dispose() {
        ObjectSyncManager sync = ObjectSyncManager.INSTANCE;
        for (ILabelDic dic : relatedLabelDics) {
            sync.removeListener(dic, this);
        }
    }
}
