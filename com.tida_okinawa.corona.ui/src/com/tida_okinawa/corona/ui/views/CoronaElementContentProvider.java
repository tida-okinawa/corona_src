/**
 * @version $Id: CoronaElementContentProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/09 16:00:52
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;

import com.tida_okinawa.corona.internal.ui.views.model.IUIContainer;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;

/**
 * @author kousuke-morishima
 */
public class CoronaElementContentProvider implements IPipelinedTreeContentProvider {
    private static final Object[] EMPTY_ARRAY = new Object[0];
    private static final CoronaModel model = CoronaModel.INSTANCE;


    @Override
    public Object[] getElements(Object input) {
        return getChildren(input);
    }


    @Override
    public Object[] getChildren(Object parent) {
        if (parent instanceof IUIContainer) {
            /* Memo getChildrenで作成している段階でAdapter登録されるはず */
            IUIElement[] children = ((IUIContainer) parent).getChildren();
            return children;
        }
        return EMPTY_ARRAY;
    }


    @Override
    public Object getParent(Object element) {
        if (element instanceof IUIElement) {
            return ((IUIElement) element).getParent();
        }
        return null;
    }


    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof IUIContainer) {
            return ((IUIContainer) element).hasChildren();
        }
        if (element instanceof IUIElement) {
            return false;
        }
        return true;
    }


    @Override
    public void dispose() {
    }


    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }


    @Override
    public void init(ICommonContentExtensionSite aConfig) {
        // TODO Auto-generated method stub

    }


    @Override
    public void restoreState(IMemento aMemento) {
        // nothing to do
    }


    @Override
    public void saveState(IMemento aMemento) {
        // nothing to do
    }


    @Override
    public void getPipelinedChildren(Object aParent, @SuppressWarnings("rawtypes") Set theCurrentChildren) {
        customize(getChildren(aParent), theCurrentChildren);
    }


    @Override
    public void getPipelinedElements(Object anInput, @SuppressWarnings("rawtypes") Set theCurrentElements) {
        customize(getElements(anInput), theCurrentElements);
    }


    private static void customize(Object[] coronaObjects, @SuppressWarnings("rawtypes") Set currentChildren) {
        /* オリジナルの子供たちからIResourceを除去する(非表示にするため) */
        List<Object> children = Arrays.asList(coronaObjects);
        for (Object o : currentChildren) {
            IResource resource = null;
            if (o instanceof IResource) {
                resource = (IResource) o;
            } else if (o instanceof IAdaptable) {
                resource = (IResource) ((IAdaptable) o).getAdapter(IResource.class);
            }
            if (resource != null) {
                int i = children.indexOf(resource);
                if (i >= 0) {
                    coronaObjects[i] = null;
                }
            }
        }

        /* 残った子供たちをcurrentChildrenに追加する */
        /* 子供たちがAdapterしているIResourceがcurrentChildrenにいたら置き換える */
        for (Object o : coronaObjects) {
            if (o instanceof IUIElement) {
                IUIElement element = (IUIElement) o;
                currentChildren.remove(element.getResource());
                currentChildren.add(element);
            }
        }
    }


    @Override
    public Object getPipelinedParent(Object anObject, Object aSuggestedParent) {
        return getParent(anObject);
    }


    @Override
    public PipelinedShapeModification interceptAdd(PipelinedShapeModification anAddModification) {
        /* Memo IResourceを作成した時にくる */
        /*
         * 共通辞書フォルダ、辞書フォルダ、問い合わせデータフォルダ、辞書、ターゲットが登録されたときに来る(処理結果、
         * 問い合わせデータのIResourceは作っていないのでこない)
         */
        convertToCoronaModel(anAddModification);
        return anAddModification;
    }


    private static void convertToCoronaModel(PipelinedShapeModification modification) {
        // 親をCoronaModelにする試み
        Object parent = modification.getParent();
        if (parent instanceof IProject) {
            convertToCoronaModel(modification.getChildren());
        } else if (parent instanceof IContainer) {
            IUIElement element = model.adapter((IContainer) parent, false);
            if (element != null) {
                modification.setParent(element);
                Set<?> children = modification.getChildren();
                convertToCoronaModel(children);
            }
        }
    }


    private static void convertToCoronaModel(@SuppressWarnings("rawtypes") Set children) {
        /* 子をCoronaModelにする試み */
        List<IUIElement> newChildren = new ArrayList<IUIElement>();
        for (Iterator<?> itr = children.iterator(); itr.hasNext();) {
            Object child = itr.next();
            if (child instanceof IProject) {
            } else if (child instanceof IResource) {
                IUIElement element = model.adapter((IResource) child, false);
                if (element != null) {
                    itr.remove();
                    newChildren.add(element);
                } else if (((IResource) child).getName().startsWith("jumandic")) {
                    itr.remove();
                }
            }
        }

        if (newChildren.size() > 0) {
            children.addAll(newChildren);
        }
    }


    @Override
    public PipelinedShapeModification interceptRemove(PipelinedShapeModification aRemoveModification) {
        // TODO ツリーに表示しているアイテム（ICoronaObjectに変換できるならICoronaObject）に変換する
        // deconvertProject(aRemoveModification); /*
        // convertToCoronaModelで正常に処理できるように、戻す */
        convertToCoronaModel(aRemoveModification.getChildren());
        Set<?> children = aRemoveModification.getChildren();
        for (Object child : children) {
            if (child instanceof IProject) {
                CoronaModel.INSTANCE.remove(CoronaModel.INSTANCE.adapter((IProject) child, false));
            }
        }
        return aRemoveModification;
    }


    @Override
    public boolean interceptRefresh(PipelinedViewerUpdate aRefreshSynchronization) {
        // TODO 名称変更すると、ここにくる
        // return
        // convertToJavaElements(refreshSynchronization.getRefreshTargets());
        return true;
    }


    @Override
    public boolean interceptUpdate(PipelinedViewerUpdate anUpdateSynchronization) {
        // TODO Auto-generated method stub
        return false;
    }

}
