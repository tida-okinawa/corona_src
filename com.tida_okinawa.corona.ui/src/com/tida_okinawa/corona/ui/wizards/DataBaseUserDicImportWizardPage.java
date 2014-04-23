/**
 * @version $Id: DataBaseUserDicImportWizardPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/05 10:08:26
 * @author kenta-uechi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.internal.ui.util.DictionaryPriorityUtil;
import com.tida_okinawa.corona.internal.ui.views.model.IUIContainer;
import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUILibFolder;
import com.tida_okinawa.corona.internal.ui.views.model.IUILibrary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ICoronaDics;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.ui.UIActivator;
import com.tida_okinawa.corona.ui.views.CoronaDicComparator;
import com.tida_okinawa.corona.ui.views.CoronaElementLabelProvider;

/**
 * @author kenta-uechi
 */
public class DataBaseUserDicImportWizardPage extends WizardNewFileCreationPage {

    /**
     * @param pageName
     * @param selection
     */
    public DataBaseUserDicImportWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName, toContainer(selection));
        setDescription(Messages.DataBaseUserDicImportWizardPage_labelDicImportFromDB);
    }


    private static IStructuredSelection toContainer(IStructuredSelection selection) {
        Object item = selection.getFirstElement();
        if (item instanceof IUIElement) {
            item = ((IUIElement) item).getResource();
        }

        if (item instanceof IFile) {
            item = ((IFile) item).getParent();
        } else if (item instanceof IContainer) {
            // nothing to do
        } else {
            item = null;
        }

        if (item == null) {
            return new StructuredSelection();
        }
        return new StructuredSelection(item);
    }

    private boolean isUiCreated;


    @Override
    protected void createAdvancedControls(Composite parent) {
        createFieldList(parent);
        isUiCreated = true;
        updateImportDicList();

        if (dicListViewer.getTable().getItemCount() > 0) {
            setFileName(((ICoronaDic) dicListViewer.getTable().getItem(0).getData()).getName());
            dicListViewer.getTable().select(0);
        }

        setPageComplete(validatePage());
        setErrorMessage(null);
    }

    TableViewer dicListViewer;


    private void createFieldList(Composite parent) {
        Composite composite = CompositeUtil.defaultComposite(parent, 1);
        GridData gd = (GridData) composite.getLayoutData();
        gd.heightHint = 200;
        CompositeUtil.createLabel(composite, Messages.DataBaseUserDicImportWizardPage_labelDics, -1).pack();

        dicListViewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
        dicListViewer.setContentProvider(new ArrayContentProvider());
        dicListViewer.setLabelProvider(new CoronaElementLabelProvider());
        dicListViewer.addSelectionChangedListener(tableItemSelectionChangedListener);
        dicListViewer.setSorter(new ViewerSorter() {
            CoronaDicComparator comparator = new CoronaDicComparator();


            @Override
            public int category(Object element) {
                return comparator.category(element);
            }
        });

        Table t = dicListViewer.getTable();
        t.setLayout(new GridLayout());
        t.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    /* ****************************************
     * 選択された辞書の取得とインポート
     */
    private Set<IUIDictionary> importedDics = new HashSet<IUIDictionary>();


    /**
     * 選択した辞書を返却
     * 
     * @return
     *         辞書リスト
     */
    public Collection<IUIDictionary> getImportedDics() {
        return importedDics;
    }


    /**
     * @deprecated use {@link #importDics()}
     */
    @Deprecated
    @Override
    public IFile createNewFile() {
        return null;
    }


    /**
     * 辞書のインポート処理
     */
    public void importDics() {
        final Object[] importDics = ((IStructuredSelection) dicListViewer.getSelection()).toArray();
        final IContainer destDir = getSelectedContainer();
        importedDics.clear();

        Job importJob = new Job(Messages.DataBaseUserDicImportWizardPage_jobImportFromDB) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                CoronaModel model = CoronaModel.INSTANCE;
                IUIElement targetLibrary = null;
                if (model.adapter(destDir, false) instanceof IUILibFolder) {
                    targetLibrary = ((IUILibFolder) model.adapter(destDir, false)).getParent();
                }

                if (targetLibrary != null) {
                    /* ターゲット下に作るならば、他の同ターゲットの下にも作る */
                    IUIElement[] uiElements;
                    if (targetLibrary instanceof IUIProduct) {
                        uiElements = model.adapter(targetLibrary.getObject()).toArray(new IUIElement[model.adapter(targetLibrary.getObject()).size()]);
                    } else {
                        uiElements = new IUIElement[] { targetLibrary };
                    }

                    monitor.beginTask(Messages.DataBaseUserDicImportWizardPage_monitorImportFromDB, (uiElements.length * importDics.length) + 1);
                    monitor.subTask(Messages.DataBaseUserDicImportWizardPage_monitorPrepare);

                    /* 同時にインポートする親辞書を特定するためのマップを作成 */
                    Map<Integer, ICoronaDic> allDics = new HashMap<Integer, ICoronaDic>();
                    for (ICoronaDic dic : IoActivator.getService().getDictionarys(ICoronaDic.class)) {
                        allDics.put(dic.getId(), dic);
                    }

                    IPath libFolderName = new Path(destDir.getName());
                    /* ファイル作成時の並びを整えるためにSortする */
                    Set<ICoronaDic> importingDics = new TreeSet<ICoronaDic>(new CoronaDicComparator());
                    for (Object o : importDics) {
                        ICoronaDic importDic = (ICoronaDic) o;
                        importingDics.add(importDic);

                        /* パターン辞書の場合は参照関係を確認する */
                        if (importDic instanceof IPatternDic) {
                            checkParentLink(importingDics, importDic);
                        }

                        /* 親辞書を特定する */
                        for (int parentId : importDic.getParentIds()) {
                            if (parentId != ICoronaDic.UNSAVED_ID) {
                                ICoronaDic parentDic = allDics.get(parentId);
                                if (parentDic != null) {
                                    importingDics.add(parentDic);
                                }
                            }
                        }
                    }

                    monitor.worked(1);
                    if (monitor.isCanceled()) {
                        monitor.done();
                        return Status.OK_STATUS;
                    }

                    /*
                     * 選択された辞書をインポートする
                     * 同じターゲットの下の辞書はまとめてインポートしないといけないので、辞書ごとにループを回す
                     */
                    for (ICoronaDic importDic : importingDics) {
                        monitor.subTask(importDic.getName());
                        for (IUIElement uiElement : uiElements) {
                            IUILibrary uiLib = (IUILibrary) uiElement;
                            IResource key = uiLib.getResource().getFolder(libFolderName);
                            IUIContainer uiParent = (IUIContainer) CoronaModel.INSTANCE.adapter(key, true);
                            ICoronaDics dics = uiLib.getObject();

                            dics.addDictionary(importDic);
                            IUIDictionary uiDic = createUIDic(uiParent, importDic);
                            DictionaryPriorityUtil.addDicPriority(uiDic);
                            monitor.worked(1);
                        }
                        if (monitor.isCanceled()) {
                            break;
                        }
                    }
                    if (getImportedDics().size() > 0) {
                        monitor.done();
                    }
                }

                monitor.done();
                return Status.OK_STATUS;
            }


            public void checkParentLink(Set<ICoronaDic> importingDics, ICoronaDic importDic) {
                /* 部品パターンを共有してる辞書も同時インポートする */
                List<IDicItem> items = importDic.getItems();
                /* パターン辞書のXML要素を抽出 */
                StringBuilder xmlString = new StringBuilder();
                for (IDicItem item : items) {
                    xmlString.append(((IPattern) item).getText());
                }

                /* XML要素からLINKIDを抽出 */
                Pattern linkReg = Pattern.compile("<LINK ID=\""); //$NON-NLS-1$
                Matcher match = linkReg.matcher(xmlString);
                HashSet<Integer> linkItems = new HashSet<Integer>();
                while (match.find()) {
                    int idLength = xmlString.substring(match.end()).indexOf("\""); //$NON-NLS-1$
                    String linkid = xmlString.substring(match.end(), match.end() + idLength);
                    linkItems.add(Integer.parseInt(linkid));
                }

                /* 自辞書以外を参照している場合、参照先の辞書をインポート対象へ追加 */
                for (Integer link : linkItems) {
                    int dicId = 0;
                    try {
                        dicId = IoActivator.getDicUtil().getItem(link, DicType.PATTERN).getComprehensionDicId();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        dicId = importDic.getId();
                    }
                    if (importDic.getId() != dicId) {
                        ICoronaDic addDic = IoActivator.getService().getDictionary(dicId);
                        if (!importingDics.contains(addDic)) {
                            importingDics.add(addDic);
                            /* 再起呼び出しにより多重リンクに対応 */
                            checkParentLink(importingDics, addDic);
                        }
                    }
                }
            }
        };

        importJob.setUser(true);
        importJob.schedule();
    }


    IUIDictionary createUIDic(IUIContainer uiParent, ICoronaDic dic) {
        IFile newParentIFile = uiParent.getResource().getFile(new Path(dic.getName()));
        IUIDictionary uiDic = (IUIDictionary) CoronaModel.INSTANCE.create(uiParent, dic, newParentIFile);
        uiDic.update(null);
        importedDics.add(uiDic);
        return uiDic;
    }


    /* ****************************************
     */
    @Override
    public void handleEvent(Event event) {
        super.handleEvent(event);
        updateImportDicList();
        setPageComplete(validatePage());
    }


    protected IContainer getSelectedContainer() {
        IPath path = getContainerFullPath();
        if (path != null) {
            return createFileHandle(path.append(Messages.DataBaseUserDicImportWizardPage_dummy)).getParent();
        }
        return null;
    }

    private List<ICoronaDic> listDics = new ArrayList<ICoronaDic>();

    private IUIContainer oldSelectedContainer;


    private void updateImportDicList() {
        if (!isUiCreated) {
            return;
        }

        /*
         * 表示用の辞書一覧作成
         */
        /* 表示する辞書リストの初期化 */
        listDics.clear();
        listDics.addAll(IoActivator.getService().getDictionarys(ICoronaDic.class));

        /* 現在選択されているフォルダを取得 */
        IUIContainer uiContainer = (IUIContainer) CoronaModel.INSTANCE.adapter(getSelectedContainer(), false);
        if ((uiContainer != null) && !uiContainer.equals(oldSelectedContainer)) {
            if (uiContainer instanceof IUILibFolder) {
                IUILibrary uiLib = ((IUILibFolder) uiContainer).getParent();
                if (uiLib != null) {
                    /* 選択されているフォルダの辞書を除去 */
                    ICoronaDics lib = uiLib.getObject();
                    if (lib != null) {
                        listDics.removeAll(lib.getDictionarys(ICoronaDic.class));
                    }

                    /* 辞書一覧の表示 */
                    dicListViewer.setInput(listDics);
                }
            }
        }
        oldSelectedContainer = uiContainer;
    }

    /* ****************************************
     * Listeners
     */
    private final ISelectionChangedListener tableItemSelectionChangedListener = new ISelectionChangedListener() {
        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            IStructuredSelection selection = (IStructuredSelection) dicListViewer.getSelection();
            if (selection.size() > 0) {
                /* 選択された辞書名を取得 */
                StringBuilder setName = new StringBuilder(128);
                for (Iterator<?> itr = selection.iterator(); itr.hasNext();) {
                    Object item = itr.next();
                    setName.append(", ").append(((ICoronaDic) item).getName()); //$NON-NLS-1$
                }

                /* 選択された辞書の名前をファイル名にセット */
                if (!getFileName().equals(setName.substring(2))) {
                    setFileName(setName.substring(2));
                }
            } else {
                /* 入力したファイル名は消さない */
                // setFileName("");
            }
            setPageComplete(validatePage());
        }
    };


    /* ****************************************
     * ページチェック
     */
    @Override
    protected boolean validatePage() {
        if (!super.validatePage()) {
            return false;
        }

        IContainer selectedContainer = getSelectedContainer();
        if (selectedContainer == null) {
            setErrorMessage(Messages.DataBaseUserDicImportWizardPage_errSelParentFolder);
        }

        if (!selectedContainer.exists()) {
            setErrorMessage(Messages.DataBaseUserDicImportWizardPage_errNonExistParentFolder);
            return false;
        }

        if (!NewDictionaryCreationPage.isLibrary(getSelectedContainer())) {
            setErrorMessage(Messages.DataBaseUserDicImportWizardPage_errSelDicFolder);
            return false;
        }

        /*
         * 入力されたファイル名の辞書をリストから選択する
         * 間違えているファイル名は無視して、あってる奴だけリストから選択する。
         * 間違えているのか入力中なのか判断つかないから。
         */
        String[] fileNames = getFileName().split(Messages.DataBaseUserDicImportWizardPage_10, -1);

        Collection<Object> select = new ArrayList<Object>(fileNames.length);
        for (ICoronaDic dic : listDics) {
            for (String fileName : fileNames) {
                if (dic.getName().equals(fileName.trim())) {
                    select.add(dic);
                    break;
                }
            }
        }
        if (select.size() == 0) {
            setErrorMessage(Messages.DataBaseUserDicImportWizardPage_errNonDics);
            return false;
        }

        /* 無限ループするので、いったん外す */
        dicListViewer.removeSelectionChangedListener(tableItemSelectionChangedListener);
        dicListViewer.setSelection(new StructuredSelection(select.toArray()));
        dicListViewer.addSelectionChangedListener(tableItemSelectionChangedListener);

        setErrorMessage(null);
        return true;
    }


    @Override
    protected IStatus validateLinkedResource() {
        return new Status(IStatus.OK, UIActivator.PLUGIN_ID, IStatus.OK, "", null); //$NON-NLS-1$
    }


    @Override
    protected void createLinkTarget() {
    }
}
