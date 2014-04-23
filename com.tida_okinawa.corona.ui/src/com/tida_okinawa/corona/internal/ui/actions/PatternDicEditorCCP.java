/**
 * @version $Id: PatternDicEditorCCP.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/02 13:57:21
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.correction.morphem.ISyntaxStructureElement;
import com.tida_okinawa.corona.correction.parsing.model.Link;
import com.tida_okinawa.corona.correction.parsing.model.Modification;
import com.tida_okinawa.corona.correction.parsing.model.NotOperator;
import com.tida_okinawa.corona.correction.parsing.model.Pattern;
import com.tida_okinawa.corona.correction.parsing.model.PatternContainer;
import com.tida_okinawa.corona.correction.parsing.model.PatternKind;
import com.tida_okinawa.corona.correction.parsing.model.PatternRecord;
import com.tida_okinawa.corona.correction.parsing.model.SearchScopeType;
import com.tida_okinawa.corona.correction.parsing.model.Sequence;
import com.tida_okinawa.corona.correction.parsing.model.Term;
import com.tida_okinawa.corona.io.model.MorphemeElement;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.TermClass;
import com.tida_okinawa.corona.io.model.dic.TermPart;
import com.tida_okinawa.corona.ui.UIActivator;
import com.tida_okinawa.corona.ui.editors.DicEditorInput;
import com.tida_okinawa.corona.ui.editors.pattern.PatternDicEditor;
import com.tida_okinawa.corona.ui.editors.pattern.PatternDicPage;

/**
 * 構文パターン辞書エディタのコピー・カット・ペースト処理
 * 
 * @author kousuke-morishima
 */
public class PatternDicEditorCCP extends AbstractCCP {

    private static final int TYPE_MORPHEME = 0;
    private static final int TYPE_PATTERN = 1;

    private PatternDicEditor editor;


    /**
     * 　コンストラクター
     * 
     * @param editor
     *            パターン辞書エディタオブジェクト
     */
    public PatternDicEditorCCP(PatternDicEditor editor) {
        this.editor = editor;
    }


    @Override
    protected AbstractCoronaCCPAction createCopy(String name) {
        return new AbstractCoronaCCPAction(name) {
            @Override
            public void run() {
                Object[] elements = extractParents(selection, TYPE_PATTERN);
                /* コピーしてから編集した内容が貼り付けられてしまうのを防ぐためにclone */
                for (int i = 0; i < elements.length; i++) {
                    elements[i] = ((Pattern) elements[i]).clone();
                }
                setContents(new StructuredSelection(elements));

                /* コピー直後の貼り付けが有効にならないため、強制的にアクションの状態を更新 */
                if (pasteAction != null) {
                    pasteAction.selectionChanged(selection);
                }
            }
        };
    }

    /** #756 カットされたパターンを記憶しておく */
    IStructuredSelection newSelection;


    @Override
    protected AbstractCoronaCCPAction createCut(String name) {
        final PatternDicPage page = (PatternDicPage) editor.getActivePageInstance();
        return new AbstractCoronaCCPAction(name) {
            /*
             * 
             * パターン辞書の Cut アクション
             * 
             * 切り取れなかったパターン(依存している参照パターン切り取り時の変更ダイアログで cancel した時など)も出てくるので
             * selection ではなく、実際に削除できたパターン (page.doDelete() の返り値)をクリップボードに入れる。
             */
            @Override
            public void run() {
                /* 選択し直さないと、親と子をまとめて選択しているときに子が消えてしまう */
                Object[] elements = extractParents(selection, TYPE_PATTERN);

                page.reveal(elements);
                List<Pattern> cutedPattern = page.doDelete();

                newSelection = new StructuredSelection(cutedPattern);
                setContents(newSelection);

                /* カット直後の貼り付けが有効にならないため、強制的にアクションの状態を更新 */
                // 子を切り取ると、親が選択されるので、親を選択しているとみなす。ただし、最後の要素の親が選択されているな。。。
                if (pasteAction != null) {
                    if (selection.size() > 0) {
                        Object selectionParent = ((Pattern) selection.toArray()[selection.size() - 1]).getParent();
                        IStructuredSelection currentSelection;
                        if (selectionParent == null) {
                            currentSelection = new StructuredSelection();
                        } else {
                            currentSelection = new StructuredSelection(selectionParent);
                        }
                        pasteAction.selectionChanged(currentSelection);
                    }
                }
            }
        };
    }


    @Override
    protected AbstractCoronaCCPAction createPaste(String name) {
        final PatternDicPage page = (PatternDicPage) editor.getActivePageInstance();
        return new CCPActionPaste(name, page);
    }


    /**
     * ssに入っている要素がツリー構造になっているとき、その最親だけの一覧を返す。
     * 
     * @param ss
     *            探索対象のセレクション
     * @param type
     *            ss が保持しているオブジェクトの種類
     * @return ツリーの最親の一覧
     * @see #TYPE_MORPHEME
     * @see #TYPE_PATTERN
     */
    Object[] extractParents(IStructuredSelection ss, int type) {
        /* ssは、選択したものがツリーやテーブルの上から順に入っているのでそれを利用する。 */
        if (ss.isEmpty()) {
            return new Object[0];
        }

        List<Object> ret = new ArrayList<Object>(ss.size());
        Object[] selectElements = ss.toArray();
        ret.add(selectElements[0]);

        int lastIndex = 0;
        for (int i = 1; i < selectElements.length; i++) {
            Object add = null;

            switch (type) {
            case TYPE_MORPHEME:
                add = extractParentMorpheme(selectElements[i], ret.get(lastIndex));
                break;
            case TYPE_PATTERN:
                add = extractParentPattern(selectElements[i], ret.get(lastIndex));
                break;
            default:
                break;
            }

            if (add != null) {
                ret.add(add);
                lastIndex++;
            }
        }
        return ret.toArray();
    }


    private static Object extractParentMorpheme(Object add, Object lastAdded) {
        if (add instanceof ISyntaxStructureElement) {
            // 最親なので格納
        } else if (add instanceof MorphemeElement) {
            if (lastAdded instanceof ISyntaxStructureElement) {
                ISyntaxStructureElement element = (ISyntaxStructureElement) lastAdded;
                if (element.getMorphemes().contains(add)) {
                    add = null;
                }
            }
        }
        return add;
    }


    private static Object extractParentPattern(Object add, Object lastAdded) {
        if ((add instanceof Pattern) && (lastAdded instanceof Pattern)) {
            if ((lastAdded instanceof PatternContainer)) {
                // 最後に追加したものの子かどうか
                PatternContainer parent = ((Pattern) add).getParent();
                while ((parent != null) && !parent.equals(lastAdded)) {
                    parent = parent.getParent();
                }
                if (parent != null) {
                    add = null;
                }
            }
        } else {
            add = null;
        }
        return add;
    }


    private final class CCPActionPaste extends AbstractCoronaCCPAction {
        private final PatternDicPage page;


        private CCPActionPaste(String text, PatternDicPage page) {
            super(text);
            this.page = page;
        }


        @Override
        public void run() {
            ISelection contents = getContents();
            if ((contents == null) || contents.isEmpty()) {
                return;
            }
            if (!(contents instanceof IStructuredSelection)) {
                return;
            }

            IStructuredSelection copyItem = (IStructuredSelection) contents;
            Object firstElement = selection.getFirstElement();
            /* 貼り付け先を特定する */
            PatternContainer parent = null;
            if (firstElement instanceof PatternContainer) {
                /* Containerには貼り付けられるので、処理を実行する。 */
                parent = (PatternContainer) firstElement;
            } else if (firstElement == null) {
                /* null(何も選択していない) ならルートに貼り付けるので、parent == null のまま処理を続行する */
            } else {
                /* 子を持てないパターンが選択されていた場合、parent == null のまま処理を続行する */
            }
            Object firstCopyItem = copyItem.getFirstElement();
            /* 作成したパターンを、ツリー上で選択するための配列 */
            Object[] reveals = null;

            if ((firstCopyItem instanceof ISyntaxStructureElement) || (firstCopyItem instanceof MorphemeElement)) {
                /* 形態素・係り受け解析結果からのコピー */
                if (parent == null) {
                    parent = new PatternRecord(null);
                    page.doCreate((PatternRecord) parent);
                }
                parent.setIgnoreChange(true);

                Object[] elements = extractParents(copyItem, TYPE_MORPHEME);
                if (elements.length > 1) {
                    if (parent.canHaveChild(PatternKind.SEQUENCE)) {
                        Sequence innerParent = new Sequence(parent);
                        parent.addChild(innerParent);
                        parent = innerParent;
                    }
                }
                reveals = new Object[elements.length];
                int i = 0;
                for (Object o : elements) {
                    if (o instanceof ISyntaxStructureElement) {
                        /* 連続(文節)の下にTerm複数 */
                        if (parent.canHaveChild(PatternKind.SEQUENCE)) {
                            Sequence seq = new Sequence(parent);
                            seq.setScope(SearchScopeType.SEARCH_SEGMENT);
                            parent.addChild(seq);
                            for (MorphemeElement element : ((ISyntaxStructureElement) o).getMorphemes()) {
                                createTerm(seq, element);
                            }
                            reveals[i++] = seq;
                        }
                    } else if (o instanceof MorphemeElement) {
                        Term term = createTerm(parent, (MorphemeElement) o);
                        if (term != null) {
                            reveals[i++] = term;
                        }
                    }
                }
            } else if (firstCopyItem instanceof Pattern) {
                /* パターン辞書からのコピー */
                /*
                 * Memo 現状、パターン辞書エディタでしかコピーされていないので冗長
                 * パターン辞書でコピーするときにも extractParentsしているので、ここでしなくてもいいという意味。
                 * ほかでもコピーするようにしたら意味が出てくる
                 */
                Object[] elements = extractParents(copyItem, TYPE_PATTERN);
                /* 貼り付けたアイテムにさらに貼り付けても大丈夫なようにclone */
                reveals = new Object[elements.length];
                for (int i = 0; i < elements.length; i++) {
                    reveals[i] = elements[i] = ((Pattern) elements[i]).clone();
                }
                if (parent != null) {
                    parent.setIgnoreChange(true);
                }
                int count = 0;
                for (Object o : elements) {
                    if (o instanceof PatternRecord) {
                        /* #755 部品パターンを参照として追加するかの判定 */
                        DicEditorInput dicInput = (DicEditorInput) editor.getEditorInput();
                        ICoronaDic coronaDic = dicInput.getDictionary();
                        /* 参照として追加可能かどうかの判定における比較対象は、クローン前のオリジナルでないと意味がない */
                        if ((coronaDic instanceof IPatternDic) && checkPartPattern(parent, (PatternRecord) copyItem.toArray()[count])) {
                            insertPartPattern(parent, (PatternRecord) o, coronaDic);
                        }
                        /* 条件を満たさない場合は従来のルートに張り付ける処理を実施 */
                        else {
                            /*
                             * Memo parent == null問題は、
                             * コピーするときに先頭にあるPatternRecordをコピーしないようにすれば解決すると思われ
                             */
                            if (parent != null) {
                                parent.setIgnoreChange(false); // 前回までの親が更新されるように
                            }
                            parent = (PatternRecord) o;
                            parent.setIgnoreChange(true);
                            page.doCreate((PatternRecord) parent);
                        }
                    } else {
                        if (parent == null) {
                            parent = new PatternRecord(null);
                            parent.setIgnoreChange(true);
                            page.doCreate((PatternRecord) parent);
                        }
                        if (parent.canHaveChild(((Pattern) o).getKind())) {
                            /* #1081 制限のあるパターンが生成されるのを防止する */
                            if (patternInputCheck(parent, o)) {
                                ((Pattern) o).setParent(parent);
                                parent.addChild((Pattern) o);
                            } else {
                                log("単語以外を含む" + ((Pattern) o).getKind() + "は" + parent.getKind() + "配下に貼り付けできません");
                            }
                        } else {
                            log(((Pattern) o).getKind() + "は" + parent.getKind() + "配下に貼り付けできません");
                        }
                    }
                    count++;
                }
            } else if (firstCopyItem instanceof ITerm) {
                /* ユーザ辞書からのコピー */
                Object[] elements = copyItem.toArray();
                reveals = new Object[copyItem.size()];
                for (int i = 0; i < reveals.length; i++) {
                    reveals[i] = ((ITerm) elements[i]).clone();
                }

                if (parent == null) {
                    parent = new PatternRecord(null);
                    page.doCreate((PatternRecord) parent);
                }
                parent.setIgnoreChange(true);

                for (Object o : reveals) {
                    ITerm t = (ITerm) o;
                    if (parent.canHaveChild(PatternKind.TERM)) {
                        Term term = new Term(null); /* 通知処理呼び出しを減らすため、親は後でつける */
                        term.setWord(t.getValue());
                        term.setPart(t.getTermPart());
                        term.setWordClass(t.getTermClass());

                        term.setParent(parent);
                        parent.addChild(term);
                    }
                }
            }

            parent.setIgnoreChange(false);
            if (reveals != null) {
                page.reveal(reveals);
            }
        }


        /**
         * 単語パターンを作成する.
         * 
         * @param parent
         *            作成する単語パターンの親パターン
         * @param morph
         *            作成する単語の情報を保持しているオブジェクト
         * @return parentの下に作成できなかったらnull
         */
        private Term createTerm(PatternContainer parent, MorphemeElement morph) {
            if (parent.canHaveChild(PatternKind.TERM)) {
                Term term = new Term(null); /* 通知処理呼び出しを減らすため、親は後でつける */
                term.setWord(morph.getGenkei());
                term.setPart(TermPart.valueOfName(morph.getHinshi()));
                term.setWordClass(TermClass.valueOfName(morph.getHinshiSaibunrui()));

                term.setParent(parent);
                parent.addChild(term);
                return term;
            }
            return null;
        }


        @Override
        public boolean updateSelection(IStructuredSelection selection) {
            this.selection = selection;

            ISelection contents = getContents();
            if (contents == null) {
                return false;
            }

            if (selection.size() == 1) {
                /*
                 * 貼り付け先が複数選択されているときは、ルートへの貼り付けでも不許可にする。
                 * ルートのときだけ、複数選択していても貼り付けができる状態でチグハグだったので、統一した
                 */
                if (contents instanceof IStructuredSelection) {
                    if (((IStructuredSelection) contents).getFirstElement() instanceof PatternRecord) {
                        return true;
                    }
                }

                if (selection.getFirstElement() instanceof PatternContainer) {
                    PatternContainer parent = (PatternContainer) selection.getFirstElement();
                    /* 子の数に制限があるのは、この３つだけだった */
                    /* 子として新しく作るものを持てるかどうかを判断すると負荷が増えるので、とりあえず今はやらない */
                    /*
                     * TODO Objectを生成しなくても使えるようにしたい
                     * canHaveChildが使い勝手が悪い
                     */
                    if (parent instanceof PatternRecord) {
                        return parent.getChildren().isEmpty();
                    } else if (parent instanceof Modification) {
                        return ((Modification) parent).getSource() == null || ((Modification) parent).getDestination() == null;
                    } else if (parent instanceof NotOperator) {
                        return parent.getChildren().size() < 1;
                    }
                    return true;
                }
            } else if (selection.isEmpty()) {
                return true;
            }
            return false;
        }


        /**
         * #756 部品パターンをコピーして参照として挿入するかどうかの判定
         * 
         * @param parent
         *            コピー先
         * @param part
         *            コピー元
         * @return 参照として挿入可能かどうか
         *         true : 参照として挿入
         *         false : パターンとして挿入
         */
        private boolean checkPartPattern(PatternContainer parent, PatternRecord part) {

            /* コピー先がnullならばfalse */
            if (parent == null) {
                return false;
            }
            /* コピー元が部品パターンでなければfalse */
            if (!part.isPart()) {
                return false;
            }
            /* コピー先が参照を子として持つことができなければfalse */
            if (!parent.canHaveChild(PatternKind.LINK)) {
                return false;
            }
            /* コピー元がカットされたパターンと一致する場合はfalse */
            if (newSelection != null) {
                for (Object o : newSelection.toArray()) {
                    PatternRecord p = (PatternRecord) o;
                    if (part.getId() == p.getId() && part.getLabel().equals(p.getLabel())) {
                        return false;
                    }
                }
            }
            return true;
        }


        /**
         * #756 部品パターンをコピーして参照として挿入
         * 
         * @param parent
         *            コピー先
         * @param part
         *            コピー元
         * @param coronaDic
         *            辞書
         */
        private void insertPartPattern(PatternContainer parent, PatternRecord part, ICoronaDic coronaDic) {

            /* 部品パターンを辞書から検索 */
            for (IDicItem item : coronaDic.getItems()) {
                IPattern p = (IPattern) item;
                /* 参照でないパターンと、IDがUNSAVED_IDのパターンは省く */
                if (p.isParts() && (p.getId() != IDicItem.UNSAVED_ID)) {
                    /* ラベルのオブジェクトが等しい場合 */
                    if (p.getLabel() == part.getIPattern().getLabel()) {
                        Link link = new Link();
                        link.setLinkPattern(p);
                        link.setParent(parent);
                        parent.addChild(link);
                        return;
                    }
                }
            }
            /* 辞書未保存によるペーストの失敗 */
            if (coronaDic.isDirty()) {
                /* 辞書に保存されないと参照はIDに割り当てられないので */
                log("辞書が未保存の為に" + part.getLabel() + "の参照が見つからない可能性があります。辞書を保存して再度実行してください。");
            }
        }


        /**
         * #1081 制限のあるパターンが生成されるのを防止する
         * 
         * @param parent
         *            追加先となる親要素
         * @param o
         *            追加元の子要素
         * @return 処理結果
         */
        private boolean patternInputCheck(PatternContainer parent, Object o) {
            if (!(o instanceof PatternContainer)) {
                return true;
            }
            PatternContainer pattern = (PatternContainer) o;
            if (parent instanceof Sequence) {
                switch (pattern.getKind()) {
                case OR:
                    for (Pattern tmp : pattern.getChildren()) {
                        if (tmp.getKind() != com.tida_okinawa.corona.correction.parsing.model.PatternKind.TERM) {
                            /* 単語以外を持つOrを連続の下に張り付けることはできない */
                            return false;
                        }
                    }
                    return true;
                default:
                    return true;
                }
            }
            return true;
        }
    }


    static void log(String message) {
        CoronaActivator.getDefault().getLog().log(new Status(IStatus.WARNING, UIActivator.PLUGIN_ID, message));

        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("org.eclipse.pde.runtime.LogView");
                } catch (PartInitException e) {
                }
            }
        });
    }
}
