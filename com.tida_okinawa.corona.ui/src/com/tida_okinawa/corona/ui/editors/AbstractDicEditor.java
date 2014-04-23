/**
 * @version $Id: AbstractDicEditor.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/14 9:59:23
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;

/**
 * @author kousuke-morishima
 */
public abstract class AbstractDicEditor extends EditorPart {
    private static final String INIT_EXCEPTION_MESSAGE = "この辞書エディタでは開けません";
    protected ICoronaDic coronaDic;
    protected IUIDictionary uiDic;

    private IPartListener activeActionListener;


    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        /* EditorInputチェック */
        if (input instanceof DicEditorInput) {
        } else if (input instanceof FileEditorInput) {
            input = EditorUtil.convertFrom((FileEditorInput) input);
            if (input == null) {
                throw new PartInitException(INIT_EXCEPTION_MESSAGE);
            }
        } else {
            throw new PartInitException(INIT_EXCEPTION_MESSAGE);
        }

        DicEditorInput dicInput = (DicEditorInput) input;
        uiDic = dicInput.getUIDictionary();
        if (validDictionary(dicInput.getDictionary())) {
            coronaDic = dicInput.getDictionary();
        } else {
            throw new PartInitException(INIT_EXCEPTION_MESSAGE);
        }

        /* グローバルアクション用のPartListener */
        activeActionListener = new IPartListener() {
            @Override
            public void partOpened(IWorkbenchPart part) {
            }


            @Override
            public void partDeactivated(IWorkbenchPart part) {
                if (AbstractDicEditor.this.equals(part)) {
                    setGlobalActions(false);
                }
            }


            @Override
            public void partClosed(IWorkbenchPart part) {
            }


            @Override
            public void partBroughtToTop(IWorkbenchPart part) {
            }


            @Override
            public void partActivated(IWorkbenchPart part) {
                if (AbstractDicEditor.this.equals(part)) {
                    setGlobalActions(true);
                }
            }
        };
        site.getPage().addPartListener(activeActionListener);

        actions = new HashMap<String, Action>();

        setSite(site);
        setInput(input);
    }


    @Override
    public void dispose() {
        super.dispose();
        getSite().getPage().removePartListener(activeActionListener);
    }


    @Override
    public void createPartControl(Composite parent) {
        createActions();
        setGlobalActions(true);
        hookContextMenu();
    }


    /* ****************************************
     * アクション関連
     */
    /**
     * エディタアクションを作る
     */
    abstract protected void createActions();


    /**
     * グローバルアクションを設定する
     */
    protected void setGlobalActions(boolean activate) {
        IActionBars bar = getEditorSite().getActionBars();
        if (activate) {
            for (Entry<String, Action> e : actions.entrySet()) {
                bar.setGlobalActionHandler(e.getKey(), e.getValue());
            }
        } else {
            for (Entry<String, Action> e : actions.entrySet()) {
                bar.setGlobalActionHandler(e.getKey(), null);
            }
        }
    }


    protected void hookContextMenu() {
    }

    private Map<String, Action> actions;


    protected void setAction(String actionId, Action action) {
        if (actionId != null) {
            actions.put(actionId, action);
        }
    }


    /**
     * @param dic
     * @return この辞書を開けるならtrue
     */
    abstract protected boolean validDictionary(ICoronaDic dic);


    /**
     * @return ProjExplから開いている場合trueを返す。
     *         DBビューから開いた場合、同じ辞書がプロジェクトにインポートしてあってもfalseを返す。
     */
    public boolean isLocalFile() {
        return uiDic != null;
    }


    /**
     * 辞書childの親ユーザー辞書を、searchの中から探す
     * 
     * @param child
     *            親辞書を探す子辞書
     * @param search
     *            探索する辞書リスト
     * @return 親ユーザー辞書リスト
     */
    public static List<IUserDic> searchParentDictionaries(ICoronaDic child, List<ICoronaDic> search) {
        List<IUserDic> returnDicList = new ArrayList<IUserDic>();
        for (ICoronaDic dic : search) {
            if (dic instanceof IUserDic) {
                for (Integer id : child.getParentIds()) {
                    if (dic.getId() == id) {
                        returnDicList.add((IUserDic) dic);
                        break;
                    }
                }
            }
        }
        return returnDicList;
    }


    /**
     * 辞書udicを親辞書としている辞書を、searchの中から探す
     * 
     * @param udic
     *            親辞書
     * @param search
     *            この中から、udicを親にしている辞書を探す
     * @return 検索結果 子辞書リスト
     */
    public static List<ICoronaDic> searchChildDictionaries(IUserDic udic, List<ICoronaDic> search) {
        List<ICoronaDic> returnDicList = new ArrayList<ICoronaDic>();
        final int thisId = udic.getId();
        for (ICoronaDic dic : search) {
            for (int parentId : dic.getParentIds()) {
                if (parentId == thisId) {
                    returnDicList.add(dic);
                    break;
                }
            }
        }

        return returnDicList;
    }


    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (adapter.equals(IPropertySheetPage.class)) {
            return new PropertySheetPage();
        }
        return super.getAdapter(adapter);
    }
}
