/**
 * @version $Id: FlucDicEditor.java 1840 2014-04-16 05:38:34Z yukihiro-kinjyo $
 * 
 * 2011/10/04 16:08:36
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.internal.OverlayIcon;

import com.tida_okinawa.corona.internal.ui.component.ElementListSelectionDialog1;
import com.tida_okinawa.corona.internal.ui.views.model.IUILibrary;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ICoronaDics;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDepend;
import com.tida_okinawa.corona.io.model.dic.IDependDic;
import com.tida_okinawa.corona.io.model.dic.IDicFactory;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.ui.Icons;
import com.tida_okinawa.corona.ui.UIActivator;

/**
 * ゆらぎ辞書エディターオブジェクト
 * 
 * @author kousuke-morishima
 */
public class FlucDicEditor extends MasterServantEditor2 {

    /** エディターID */
    public static final String EDITOR_ID = "com.tida_okinawa.corona.ui.editor.fluctuationdic"; //$NON-NLS-1$


    /**
     * ゆらぎ辞書エディターオブジェクトの初期化
     */
    public FlucDicEditor() {
        super(new int[] { 1, 1 });
    }

    private IDependDic dependDic;
    private DicEditorDisposer disposer;


    @Override
    protected void update() {
        dependDic.update();
        /* looper初期化の為 */
        registeredMasterItems = null;
    }


    @Override
    protected boolean validDictionary(ICoronaDic dic) {
        if (dic instanceof IDependDic) {
            dependDic = (IDependDic) dic;
            return true;
        }
        return false;
    }


    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);

        setPartName(input.getName());

        List<ICoronaDic> dics = new ArrayList<ICoronaDic>(1);
        dics.add(dependDic);
        disposer = new DicEditorDisposer(this, dics);
        getSite().getPage().addPartListener(disposer);
    }


    @Override
    public void dispose() {
        super.dispose();
        getSite().getPage().removePartListener(disposer);
    }


    /* ****************************************
     * 保存
     */
    @Override
    public void doSave(IProgressMonitor monitor) {
        /* エラーアイテムを保存しない */
        Map<IDepend, Integer> errorItemMap = new TreeMap<IDepend, Integer>(new Comparator<IDepend>() {
            @Override
            public int compare(IDepend o1, IDepend o2) {
                /* putした順に追加する */
                if (o1.equals(o2)) {
                    return 0;
                }
                return 1;
            }
        });

        List<IDicItem> currentItems = dependDic.getItems();
        // Memo 今は、FlucDicが持っているitemsをそのままもらっているのでこれでOK
        int i = 0;
        for (IDicItem item : currentItems) {
            if (isError((IDepend) item)) {
                // 直接消しているので、ICoronaDicの削除フラグは立たない */
                errorItemMap.put((IDepend) item, i--);
            }
            i++;
        }
        for (Entry<IDepend, Integer> e : errorItemMap.entrySet()) {
            currentItems.remove((int) e.getValue());
        }

        dependDic.commit(monitor);

        /* Memo 削除フラグを立てていないので、またこっそり戻す */
        if (errorItemMap.size() > 0) {
            StringBuilder message = new StringBuilder(30 + (15 * errorItemMap.size()));
            message.append(Messages.FlucDicEditor_messageErrorNotTerm);
            int cnt = 0;
            for (Entry<IDepend, Integer> e : errorItemMap.entrySet()) {
                currentItems.add(cnt + e.getValue(), e.getKey());
                if ((cnt % 5) == 0) {
                    message.append("\n"); //$NON-NLS-1$
                }
                message.append(e.getKey().getMain().getValue()).append(", "); //$NON-NLS-1$
                cnt++;
            }
            MessageDialog.openWarning(getSite().getShell(), Messages.FlucDicEditor_unsavedItem, message.toString());
            dependDic.setDirty(true);
        }

        dirtyChanged();
        getMasterViewer().update(errorItemMap.keySet().toArray(), null);
    }


    boolean isError(IDepend depend) {
        boolean ret = depend.getSub().isEmpty();
        return ret;
    }


    @Override
    public boolean isDirty() {
        return dependDic.isDirty();
    }


    @Override
    public void doSaveAs() {
    }


    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }


    /* ****************************************
     * Override
     */
    @Override
    protected Control createMasterArea(Composite parent) {
        Control c = super.createMasterArea(parent);
        getMasterViewer().setSorter(sorter);
        return c;
    }


    @Override
    protected Control createServantArea(Composite parent) {
        Control c = super.createServantArea(parent);
        getServantViewer().setSorter(sorter);
        return c;
    }

    private ViewerSorter sorter = new ViewerSorter() {
        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            ITerm t1 = null;
            ITerm t2 = null;
            if (e1 instanceof IDepend) {
                t1 = ((IDepend) e1).getMain();
                t2 = ((IDepend) e2).getMain();
            } else if (e1 instanceof ITerm) {
                t1 = (ITerm) e1;
                t2 = (ITerm) e2;
            } else {
                return 0;
            }

            return t1.getValue().compareTo(t2.getValue());
        }
    };

    private Set<ITerm> registeredMasterItems;
    /**
     * 従属語、親アイテム
     */
    Map<ITerm, IDepend> registeredSubItems;


    @Override
    protected Object getMasterInput() {
        /* 登録済みの代表語と従属語のリストを作成 */
        List<IDicItem> items = dependDic.getItems();

        if (registeredMasterItems == null) {
            Comparator<ITerm> comparator = new Comparator<ITerm>() {
                @Override
                public int compare(ITerm o1, ITerm o2) {
                    return o1.getKeyword().compareTo(o2.getKeyword());
                }
            };
            registeredMasterItems = new TreeSet<ITerm>(comparator);
            registeredSubItems = new TreeMap<ITerm, IDepend>(comparator);

            looper = new LoopDetector<ITerm>();

            for (Iterator<IDicItem> itr = items.iterator(); itr.hasNext();) {
                IDepend item = (IDepend) itr.next();
                ITerm masterTerm = item.getMain();
                if (masterTerm != null) {
                    registeredMasterItems.add(masterTerm);
                    looper.add(masterTerm);

                    List<ITerm> subs = item.getSub();
                    for (Iterator<ITerm> itr2 = subs.iterator(); itr2.hasNext();) {
                        ITerm subTerm = itr2.next();
                        if (subTerm != null) {
                            registeredSubItems.put(subTerm, item);
                            looper.add(masterTerm, subTerm);
                        } else {
                            itr2.remove(); /* nullアイテムは除去 */
                        }
                    }
                } else {
                    itr.remove(); /* nullアイテムは除去 */
                }
            }
        }
        return items;
    }

    ILabelProvider masterLabelProvider = new LabelProvider() {
        @Override
        public String getText(Object element) {
            if (element instanceof IDepend) {
                ITerm term = ((IDepend) element).getMain();
                return servantLabelProvider.getText(term);
            }
            return super.getText(element);
        }


        @SuppressWarnings("restriction")
        @Override
        public Image getImage(Object element) {
            if (element instanceof IDepend) {
                if (isError((IDepend) element)) {
                    // error item
                    ImageDescriptor base = Icons.INSTANCE.getDescriptor(Icons.IMG_PATTERN_RECORD);
                    ImageDescriptor overlay = Icons.INSTANCE.getDescriptor(Icons.IMG_OVR_ERROR);
                    return new OverlayIcon(base, overlay, new Point(16, 16)).createImage();
                }
            }
            return super.getImage(element);
        }
    };


    @Override
    protected ILabelProvider getMasterLabelProvider() {
        return masterLabelProvider;
    }


    @Override
    protected IStructuredContentProvider getServantContentProvider() {
        return new IStructuredContentProvider() {
            private final Object[] EMPTY_ARRAY = new Object[0];


            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }


            @Override
            public void dispose() {
            }


            @Override
            public Object[] getElements(Object input) {
                if (input instanceof IDepend) {
                    return ((IDepend) input).getSub().toArray();
                }
                return EMPTY_ARRAY;
            }
        };
    }


    @Override
    protected ILabelProvider getServantLabelProvider() {
        return servantLabelProvider;
    }

    LabelProvider servantLabelProvider = new LabelProvider() {
        @Override
        public String getText(Object element) {
            if (element instanceof ITerm) {
                ITerm term = (ITerm) element;
                return getString(50, ":", term.getValue(), term.getTermPart().getName(), term.getTermClass().getName()); //$NON-NLS-1$
            }
            return super.getText(element);
        }


        private final String getString(int initCapa, String sepa, Object... args) {
            StringBuilder buf = new StringBuilder(initCapa);
            for (Object arg : args) {
                buf.append(sepa).append(arg);
            }
            buf.delete(0, sepa.length());
            return buf.toString();
        }
    };

    private static final IDicFactory factory = IoActivator.getDicFactory();
    LoopDetector<ITerm> looper = new LoopDetector<ITerm>();


    @Override
    protected void addButtonSelected(String masterOrServant) {
        /* 表示アイテムを親辞書から取得 */
        List<IUserDic> parentDics = getParentDics();
        List<IDicItem> viewItems = new ArrayList<IDicItem>();
        for (IUserDic udic : parentDics) {
            viewItems.addAll(udic.getItems());
        }
        final ElementListSelectionDialog1 d = new ElementListSelectionDialog1(getSite().getShell(), new LabelProvider() {
            @Override
            public String getText(Object element) {
                StringBuilder base = new StringBuilder(50).append(servantLabelProvider.getText(element));
                if (registeredSubItems.containsKey(element)) {
                    base.append(" -> ").append(masterLabelProvider.getText(registeredSubItems.get(element))); //$NON-NLS-1$
                }
                return base.toString();
            }
        });
        d.setConvertZenkaku(true);
        d.setImeMode(SWT.NATIVE);
        d.setMultipleSelection(true);

        if (MASTER.equals(masterOrServant)) {
            /* 単語を選択してもらう */
            d.setTitle(Messages.FlucDicEditor_titleSelectMainTerm);
            d.setMessage(Messages.FlucDicEditor_messageSelectMainTerm);

            /* 登録済み単語を消す */
            viewItems.removeAll(registeredMasterItems);
            d.setElements(viewItems.toArray());

            if (d.open() == Dialog.OK) {
                IDepend addedItem = null;
                for (Object r : d.getResult()) {
                    /* 辞書に */
                    addedItem = factory.createFluc((ITerm) r);
                    dependDic.addItem(addedItem);
                    /* キャッシュに */
                    if (addedItem.getMain() != null) {
                        registeredMasterItems.add(addedItem.getMain());
                    }
                    looper.add(addedItem.getMain());
                }

                getMasterViewer().refresh();
                if (addedItem != null) {
                    getMasterViewer().setSelection(new StructuredSelection(addedItem));
                }

                dirtyChanged();
            }
        } else {
            List<IDepend> elements = getSelectedMasterItems();

            if (elements.size() != 1) {
                /* マスターが選択されていないか、複数選択されているので、なにもしない */
                return;
            }
            final IDepend parent = elements.get(0);
            /* 登録済み単語と親を消す */
            viewItems.remove(parent.getMain());
            d.setElements(viewItems.toArray());

            d.setTitle(Messages.FlucDicEditor_titleSelectDependentTerm);
            d.setMessage(Messages.FlucDicEditor_messageSelectDependentTerm);
            d.setValidator(new ISelectionStatusValidator() {
                @Override
                public IStatus validate(Object[] selection) {
                    /* 代表語と従属語でループしないか確認する */
                    StringBuilder message = new StringBuilder();
                    for (Object select : selection) {
                        if (select instanceof ITerm) {
                            ITerm t = (ITerm) select;
                            if (!looper.check(parent.getMain(), t)) {
                                message.append(", ").append(t.getValue()); //$NON-NLS-1$
                            }
                        }
                    }
                    if (message.length() > 0) {
                        return new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, message.substring(2) + Messages.FlucDicEditor_messageCycle);
                    }

                    for (Object select : selection) {
                        if (select instanceof ITerm) {
                            ITerm t = (ITerm) select;
                            IDepend parent = registeredSubItems.get(t);
                            if (parent != null) {
                                message.append(", ").append(t.getValue()); //$NON-NLS-1$
                            }
                        }
                    }
                    if (message.length() > 0) {
                        return new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, message.substring(2) + Messages.FlucDicEditor_messageRegistered);
                    }
                    return Status.OK_STATUS;
                }
            });

            if (d.open() == Dialog.OK) {
                ITerm addedChild = null;
                for (Object r : d.getResult()) {
                    addedChild = (ITerm) r;
                    parent.addSub(addedChild);
                    registeredSubItems.put(addedChild, parent);
                    looper.add(parent.getMain(), addedChild);
                }

                getMasterViewer().update(parent, null);
                getServantViewer().refresh();
                if (addedChild != null) {
                    getServantViewer().setSelection(new StructuredSelection(addedChild));
                }

                dirtyChanged();
            }
        }
    }


    @Override
    protected void removeButtonSelected(String masterOrServant) {
        /* 選択されているアイテムを全部消す */
        if (MASTER.equals(masterOrServant)) {
            List<IDepend> depends = getSelectedMasterItems();
            if (!depends.isEmpty()) {
                for (IDepend depend : depends) {
                    /* 辞書から */
                    dependDic.removeItem(depend);
                    /* キャッシュから */
                    if (depend.getMain() != null) {
                        registeredMasterItems.remove(depend.getMain());
                        for (ITerm sub : depend.getSub()) {
                            registeredSubItems.remove(sub);
                        }
                    }
                    /* ループ検出から */
                    for (ITerm sub : depend.getSub()) {
                        looper.remove(sub);
                    }
                    looper.remove(depend.getMain());
                }

                setNewSelection(getMasterViewer());
                dirtyChanged();
            }
        } else {
            /* マスターで選択されているアイテムが１つのときだけ処理する */
            List<IDepend> masterItems = getSelectedMasterItems();
            if (masterItems.size() == 1) {
                IDepend master = masterItems.get(0);

                /* サブで選択されているアイテムすべてを削除する */
                TableViewer v = getServantViewer();
                IStructuredSelection selection = (IStructuredSelection) v.getSelection();
                List<?> elements = selection.toList();
                if (!elements.isEmpty()) {
                    for (Object element : elements) {
                        if (element instanceof ITerm) {
                            ITerm term = (ITerm) element;
                            master.removeSub(term);
                            registeredSubItems.remove(term);
                            looper.remove(term);
                        }
                    }

                    setNewSelection(v);
                    dirtyChanged();
                }
            }
            getMasterViewer().refresh();
        }
    }


    /**
     * @return 選択されているアイテムをすべて返す。
     */
    protected List<IDepend> getSelectedMasterItems() {
        TableViewer v = getMasterViewer();
        int itemNum = v.getTable().getItemCount();
        List<IDepend> ret = new ArrayList<IDepend>(itemNum);

        IStructuredSelection selection = (IStructuredSelection) v.getSelection();
        for (Object o : selection.toArray()) {
            ret.add((IDepend) o);
        }

        return ret;
    }

    /* ****************************************
     * 親辞書
     */
    private List<IUserDic> parentDics;


    private List<IUserDic> getParentDics() {
        if (parentDics == null) {
            parentDics = new ArrayList<IUserDic>();
            if (isLocalFile()) {
                /* ローカルファイルの場合、同じLibraryが保持しているはず */
                IUILibrary lib = CoronaModel.INSTANCE.getLibrary(uiDic);
                ICoronaDics dics = lib.getObject();
                if (dics == null) {
                    parentDics = new ArrayList<IUserDic>(0);
                } else {
                    parentDics = searchParentDictionaries(dependDic, dics.getDictionarys(IUserDic.class));
                }
            } else {
                /* 全辞書走査 */
                parentDics = searchParentDictionaries(dependDic, IoActivator.getService().getDictionarys(IUserDic.class));
            }
        }
        return parentDics;
    }


}
