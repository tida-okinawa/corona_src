/**
 * @version $Id: CoronaModel.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/09 16:32:59
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.views.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.common.StringUtil;
import com.tida_okinawa.corona.internal.ui.CoronaConstants;
import com.tida_okinawa.corona.internal.ui.PersistentPropertyKeys;
import com.tida_okinawa.corona.internal.ui.util.ClaimUtil;
import com.tida_okinawa.corona.internal.ui.util.PreferenceUtils;
import com.tida_okinawa.corona.internal.ui.views.model.IClaimFolder;
import com.tida_okinawa.corona.internal.ui.views.model.ICorrectionFolder;
import com.tida_okinawa.corona.internal.ui.views.model.ILibrary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIClaim;
import com.tida_okinawa.corona.internal.ui.views.model.IUIClaimFolder;
import com.tida_okinawa.corona.internal.ui.views.model.IUIContainer;
import com.tida_okinawa.corona.internal.ui.views.model.IUICorrectionFolder;
import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUILibFolder;
import com.tida_okinawa.corona.internal.ui.views.model.IUILibrary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProject;
import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.ICoronaObject;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.IModelFactory;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicFactory;
import com.tida_okinawa.corona.ui.CoronaNature;
import com.tida_okinawa.corona.ui.UIActivator;
import com.tida_okinawa.corona.ui.data.ConnectionParameter;

/**
 * Coronaプロジェクトが使うモデルのユーティリティクラス。
 * 
 * @author kousuke-morishima
 */
public class CoronaModel {
    private CoronaModel() {
    }

    public final static CoronaModel INSTANCE = new CoronaModel();

    /* ****************************************
     * Model Factory
     */
    private IModelFactory mFactory = IoActivator.getModelFactory();
    private IDicFactory dFactory = IoActivator.getDicFactory();


    /**
     * selectionの一つ目をIResourceにする
     * 
     * @param selection
     * @return
     */
    public static IStructuredSelection toContainer(IStructuredSelection selection) {
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


    /**
     * @param selection
     *            {@link IStructuredSelection} of {@link IUIElement} and/or
     *            {@link IResource}
     * @return selection をIResourceに変換したもの
     */
    public static IStructuredSelection toResources(IStructuredSelection selection) {
        List<IResource> items = new ArrayList<IResource>();
        for (Iterator<?> itr = selection.iterator(); itr.hasNext();) {
            Object o = itr.next();
            if (o instanceof IUIElement) {
                IResource resource = ((IUIElement) o).getResource();
                if (resource != null) {
                    items.add(resource);
                }
            } else if (o instanceof IResource) {
                items.add((IResource) o);
            }
        }
        return new StructuredSelection(items);
    }


    /* ****************************************
     * CoronaProject
     */
    /**
     * CoronaProjectかどうか判定する。プロジェクトが開いていない場合はfalse
     * 
     * @param project
     *            判定するProject
     * @return CoronaProjectNatureを持っていれば、CoronaProjectだと判断してtrueを返す
     */
    public boolean isCoronaProject(IProject project) {
        try {
            return (project.getNature(CoronaNature.ID) != null);
        } catch (CoreException e) {
            /*
             * Project is not open or not exist or nature extension point is not
             * declare.
             */
            return false;
        }
    }


    /**
     * IProjectにCoronaNatureをつける
     * 
     * @param project
     * @param monitor
     * @throws CoreException
     */
    public void setNature(IProject project, IProgressMonitor monitor) throws CoreException {
        IProjectDescription desc = project.getDescription();
        List<String> ids = new ArrayList<String>(Arrays.asList(desc.getNatureIds()));
        ids.add(CoronaNature.ID);
        desc.setNatureIds(ids.toArray(new String[ids.size()]));
        project.setDescription(desc, monitor);
    }


    /**
     * IProjectからCoronaNatureを外す
     * 
     * @param project
     * @param monitor
     * @throws CoreException
     */
    public void removeNature(IProject project, IProgressMonitor monitor) throws CoreException {
        IProjectDescription desc = project.getDescription();
        List<String> ids = new ArrayList<String>(Arrays.asList(desc.getNatureIds()));
        ids.remove(CoronaNature.ID);
        desc.setNatureIds(ids.toArray(new String[ids.size()]));
        project.setDescription(desc, monitor);
    }


    /* ****************************************
     * オブジェクト作成系
     */
    /**
     * UIオブジェクトを作成する。同じresourceですでに作成している場合、以前のキャッシュが返る。
     * <p>
     * ただし、ターゲットを作成する場合にはこのメソッドは使用できない。 {@link #create(IFolder, String)}を使用すること。
     * </p>
     * 
     * @param resource
     * @return なんかしら返る。ただし、ターゲットにしか合致しないIResourceの場合null。
     */
    public IUIElement create(IResource resource) {
        IUIElement element = adapter(resource, false);
        if (element == null) {
            element = doCreate(resource);
        }
        return element;
    }


    private IUIElement doCreate(IResource resource) {
        IUIContainer parent = (IUIContainer) adapter(resource.getParent(), true);
        ICoronaObject object = null;
        if (resource instanceof IProject) {
            object = createCoronaProject((IProject) resource);
        } else if (resource instanceof IFolder) {
            object = createCoronaContainerObject((IFolder) resource);
        } else if (resource instanceof IFile) {
            object = createCoronaElement((IFile) resource);
        }
        if (object != null) {
            return doCreate(parent, object, resource);
        }
        return null;
    }


    /**
     * UIオブジェクトを作成する。同じresourceですでに作成している場合、以前のキャッシュが返る
     * 
     * @param parent
     * @param object
     * @param resource
     * @return
     */
    public IUIElement create(IUIContainer parent, ICoronaObject object, IResource resource) {
        IUIElement ret = adapter(resource, false);
        if (ret == null) {
            ret = doCreate(parent, object, resource);
        }
        return ret;
    }


    private IUIElement doCreate(IUIContainer parent, ICoronaObject object, IResource resource) {
        IUIElement ret = null;
        if (object instanceof ICoronaDic) {
            ret = new UIDictionary(parent, (ICoronaDic) object, (IFile) resource);
        } else if (object instanceof IClaimWorkData) {
            ret = new UIWork(parent, (IClaimWorkData) object, (IFile) resource);
        } else if (object instanceof ICoronaProduct) {
            ret = new UIProduct(parent, (ICoronaProduct) object, (IFolder) resource);
        } else if (object instanceof IClaimData) {
            ret = new UIClaim(parent, (IClaimData) object, (IFile) resource);
        } else if (object instanceof ICoronaProject) {
            ret = new UIProject(parent, (ICoronaProject) object, (IProject) resource);
        } else if (object instanceof ILibrary) {
            ret = new UILibFolder((IUILibrary) parent, (ILibrary) object, (IContainer) resource);
        } else if (object instanceof ICorrectionFolder) {
            ret = new UICorrectionFolder((IUIProduct) parent, (ICorrectionFolder) object, (IContainer) resource);
        } else if (object instanceof IClaimFolder) {
            ret = new UIClaimFolder((IUIProject) parent, (IClaimFolder) object, (IContainer) resource);
        }
        if (ret != null) {
            put(ret);
            if (parent != null) {
                parent.modifiedChildren();
            }
        }
        return ret;
    }


    /* ********************
     * ICoronaObject作成(private)
     */
    /**
     * 渡されたIProjctがCoronaProjectだったら、それを返す。違ったらnullを返す
     * 
     * @param project
     * @return
     */
    private ICoronaProject createCoronaProject(IProject project) {

        ICoronaProject coronaProject = null;
        IUIProject uiProject = (IUIProject) adapter(project, false);

        if (uiProject != null) {
            coronaProject = uiProject.getObject();
            return coronaProject;
        }
        if (!(isCoronaProject(project))) {
            return coronaProject;
        }

        try {
            /* プロジェクトが正規のIDを保持していれば、そのIDでDBからプロジェクトを取得する */
            String idString = project.getPersistentProperty(PersistentPropertyKeys.PROJECT_ID);
            if (idString != null) {
                int id = Integer.parseInt(project.getPersistentProperty(PersistentPropertyKeys.PROJECT_ID));
                coronaProject = IoActivator.getService().getProject(id);
                if (coronaProject == null || (!coronaProject.getName().equals(project.getName()))) {
                    coronaProject = null;
                }
            } else {
                /* 新方式（StoreにIDを保持する）で作成されたプロジェクト */
                ConnectionParameter parameter = PreferenceUtils.getCurrentConnectionParameter();
                if (parameter != null) {
                    IPreferenceStore pStore = UIActivator.getDefault().getPreferenceStore(project);
                    String name = pStore.getString(PersistentPropertyKeys.DB_CONNECT_NAME.toString());
                    String url = pStore.getString(PersistentPropertyKeys.DB_CONNECT_URL.toString());
                    if (parameter.name.equals(name) && parameter.url.equals(url)) {
                        coronaProject = mFactory.createProject(project.getName());
                    }

                    if (coronaProject == null) {
                        /* 関係ないプロジェクトかどうか判定 */
                        for (ICoronaProject p : IoActivator.getService().getProjects()) {
                            if (p.getName().equals(project.getName())) {
                                /*
                                 * ワークスペースから一度削除されて、再度インポートされたプロジェクトであるため
                                 * 、再設定
                                 */
                                coronaProject = mFactory.createProject(project.getName());
                                pStore.setValue(PersistentPropertyKeys.DB_CONNECT_NAME.toString(), parameter.name);
                                pStore.setValue(PersistentPropertyKeys.DB_CONNECT_URL.toString(), parameter.url);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.err.println(Messages.CoronaModel_errorProjectId);
        } catch (CoreException e) {
            e.printStackTrace();
        }

        return coronaProject;
    }


    private ICoronaObject createCoronaContainerObject(IFolder resource) {
        IUIElement element = adapter(resource, false);
        if (element == null) {
            String name = resource.getName();
            if (CoronaConstants.COMMON_LIBRARY_NAME.equals(name)) {
                return new Library(CoronaConstants.COMMON_LIBRARY_NAME);
            } else if (CoronaConstants.LIBRARY_NAME.equals(name)) {
                return new Library(CoronaConstants.LIBRARY_NAME);
            } else if (CoronaConstants.CLAIM_FOLDER_NAME.equals(name)) {
                return new ClaimFolder();
            } else if (!name.isEmpty() && name.contains(CoronaConstants.CORRECTION_FOLDER_NAME)) {
                return new CorrectionFolder(name);
            } else if (existsProductFolder(resource, name)) {
                /**
                 * 直接 createCoronaProduct を呼ぶと、
                 * ターゲットとして存在しないフォルダ名が指定されていると
                 * 新たなターゲットとして作成してしまうため
                 * existsProductFolder() でチェックしてから呼び出す
                 */
                return createCoronaProduct(resource, name);
            }
            return null;
        }
        return element.getObject();
    }


    /**
     * 指定したフォルダとそのフォルダ名がターゲットに登録されているものかチェックする
     * 
     * @param resource
     *            ファイルとしての不正文字を含まないリソース
     * @param name
     *            問い合わせデータ通りのターゲット名
     * @return 登録済みのものであれば true
     */
    private boolean existsProductFolder(IFolder resource, String name) {
        IUIProject uiProject = (IUIProject) adapter(resource.getProject(), false);

        if (uiProject == null)
            return false;
        ICoronaProject project = uiProject.getObject();

        for (ICoronaProduct product : project.getProducts()) {
            if (product.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }


    /**
     * @param resource
     *            ファイルとしての不正文字を含まないリソース
     * @param name
     *            問い合わせデータ通りのターゲット名。
     * @return may be null
     */
    private ICoronaProduct createCoronaProduct(IFolder resource, String name) {
        ICoronaProduct product = null;
        IUIProduct uiProduct = (IUIProduct) adapter(resource, false);
        if (uiProduct == null) {
            IUIProject uiProject = (IUIProject) adapter(resource.getProject(), false);
            if (uiProject != null) {
                ICoronaProject project = uiProject.getObject();
                product = mFactory.createProduct(name, project);
                /*
                 * IModelFactory#createProductは、すでに存在するターゲット名の場合nullを返す。
                 * CoronaElementContentProviderよりも先にターゲットのUIElementを取得しようとすると
                 * nullが返ってしまう問題に対処。
                 */
                if (product == null) {
                    for (ICoronaProduct p : project.getProducts()) {
                        if (p.getName().equals(name)) {
                            product = p;
                            break;
                        }
                    }
                }
            }
        } else {
            product = uiProduct.getObject();
        }

        return product;
    }

    private static final int NO_TYPE = 0;
    private static final int DIC_TYPE = 1;
    private static final int CLAIM_TYPE = 2;
    private static final int CORRECTION_TYPE = 3;


    /**
     * 辞書とか問い合わせデータとか中間データを作る
     * 
     * @param resource
     *            辞書の場合、拡張子必須
     * @return may be null
     */
    private ICoronaObject createCoronaElement(IFile resource) {
        switch (getElementType(resource)) {
        case DIC_TYPE:
            return createDictionary(resource);
        case CLAIM_TYPE:
            // TODO 問い合わせデータも作れるようにする
            break;
        case CORRECTION_TYPE:
            break;
        default:
            break;
        }
        return null;
    }


    /**
     * resourceの親をたどって行って、親の種類からresourceの種別を判定する
     * 
     * @param resource
     * @return
     */
    private int getElementType(IFile iFile) {
        IContainer parent = iFile.getParent();
        while (parent != null) {
            IUIElement element = create(parent);
            if (element instanceof IUIProject) {
                return NO_TYPE;
            } else if (element instanceof IUILibFolder) {
                return DIC_TYPE;
            } else if (element instanceof IUIClaimFolder) {
                return CLAIM_TYPE;
            } else if (element instanceof IUICorrectionFolder) {
                return CORRECTION_TYPE;
            } else {
                parent = parent.getParent();
            }
        }
        return NO_TYPE;
    }


    private ICoronaDic createDictionary(IFile resource) {
        IUIDictionary uiDic = (IUIDictionary) adapter(resource, false);
        if (uiDic == null) {
            String ext = resource.getFileExtension();
            if (ext == null) {
                return null;
            }

            String name = resource.getName();
            if (ext.equalsIgnoreCase(DicType.SPECIAL.getExtension())) {
                return dFactory.createUserDic(name, name, DicType.SPECIAL);
            } else if (ext.equalsIgnoreCase(DicType.COMMON.getExtension())) {
                return dFactory.createUserDic(name, name, DicType.COMMON);
            } else if (ext.equalsIgnoreCase(DicType.CATEGORY.getExtension())) {
                return dFactory.createUserDic(name, name, DicType.CATEGORY);
            } else if (ext.equalsIgnoreCase(DicType.FLUC.getExtension())) {
                return dFactory.createFlucDic(name, new HashSet<Integer>());
            } else if (ext.equalsIgnoreCase(DicType.SYNONYM.getExtension())) {
                return dFactory.createSynonymDic(name, new HashSet<Integer>());
            } else if (ext.equalsIgnoreCase(DicType.PATTERN.getExtension())) {
                return dFactory.createPatternDic(name);
            } else if (ext.equalsIgnoreCase(DicType.LABEL.getExtension())) {
                return dFactory.createLabelDic(name);
            } else {
                return null;
            }
        }

        return uiDic.getObject();
    }


    /* ****************************************
     * Adapter的なメソッド
     */
    /**
     * 引数に指定したIUIElementが属するプロジェクトを取得する
     * 
     * @param element
     * @return
     */
    public IUIProject getProject(IUIElement element) {
        if (element instanceof IUIProject) {
            return (IUIProject) element;
        }
        if (element != null) {
            IUIContainer parent = element.getParent();
            while (parent != null) {
                if (parent instanceof IUIProject) {
                    return (IUIProject) parent;
                }
                parent = parent.getParent();
            }
        }
        return null;
    }


    /**
     * 指定した辞書を保持しているIUILibraryを返す
     * 
     * @param dic
     * @return
     */
    public IUILibrary getLibrary(IUIDictionary dic) {
        if (dic == null) {
            return null;
        }
        IUIContainer parent = dic.getParent();
        while (parent != null) {
            if (parent instanceof IUILibrary) {
                return (IUILibrary) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }


    /**
     * elementの親をたどっていき(elementも検査する)、初めに見つかったcls型のIUIContainerを返す
     * 
     * @param cls
     * @param element
     * @return
     */
    public IUIContainer getUIContainer(Class<? extends IUIContainer> cls, IUIElement element) {
        if (element == null) {
            return null;
        }

        IUIContainer parent;
        if (element instanceof IUIContainer) {
            parent = (IUIContainer) element;
        } else {
            parent = element.getParent();
        }
        while (parent != null) {
            if (cls.isAssignableFrom(parent.getClass())) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }


    public IUIProduct getProduct(IUIProject uiProject, ICoronaProduct product) {
        IFolder folder = null;
        folder = uiProject.getResource().getFolder(StringUtil.convertValidFileName(product.getName()));
        return (IUIProduct) create(uiProject, product, folder);
    }


    /**
     * ICoronaDicをUIElementにして返す
     * 
     * @param uiLib
     * @param dic
     * @return
     */
    public IUIDictionary getDic(IUILibrary uiLib, ICoronaDic dic) {
        IFolder folder = null;
        if (uiLib instanceof IUIProject) {
            folder = ((IUIProject) uiLib).getResource().getFolder(CoronaConstants.COMMON_LIBRARY_NAME);
        } else if (uiLib instanceof IUIProduct) {
            folder = ((IUIProduct) uiLib).getResource().getFolder(CoronaConstants.LIBRARY_NAME);
        }
        if (folder != null) {
            IUILibFolder parent = (IUILibFolder) adapter(folder, true);
            return (IUIDictionary) create(parent, dic, folder.getFile(dic.getName()));
        }

        return null;
    }


    public IUIClaim getClaim(IUIProject uiProject, IClaimData claim) {
        IFolder folder = uiProject.getResource().getFolder(CoronaConstants.CLAIM_FOLDER_NAME);
        IUIClaimFolder parent = (IUIClaimFolder) adapter(folder, true);
        return (IUIClaim) create(parent, claim, folder.getFile(claim.getName()));
    }


    /**
     * IClaimWorkDataをUIElementにして返す
     * 
     * @param uiProduct
     * @param work
     * @return
     */
    public IUIWork getWork(IUIProduct uiProduct, IClaimWorkData work) {
        return getWork(uiProduct, work, ""); //$NON-NLS-1$
    }


    public IUIWork getWork(IUIProduct uiProduct, IClaimWorkData work, String nameSuffix) {
        String claimName = ClaimUtil.getClaimName(work.getClaimId());
        IFolder folder = uiProduct.getResource().getFolder(CoronaConstants.createCorrectionFolderName(claimName));
        IUICorrectionFolder parent = (IUICorrectionFolder) adapter(folder, true);
        String name = work.getClaimWorkDataType().getName() + Messages.CoronaModel_leftParenthesis
                + ClaimUtil.getFieldName(work.getClaimId(), work.getFieldId()) + Messages.CoronaModel_RightParenthesis + nameSuffix;
        return (IUIWork) create(parent, work, folder.getFile(name));
    }

    /* ****************************************
     * adapter
     */
    private Map<IResource, IUIElement> adapter = new HashMap<IResource, IUIElement>();


    /**
     * keyに対応するIUElementを返す。
     * keyに対応させられるICoronaObjectがない場合はnull
     * 
     * @param key
     * @param create
     *            keyに対応する値がない場合、新しく作成するかどうか
     * @return may be null
     */
    public IUIElement adapter(IResource key, boolean create) {
        if (key == null) {
            return null;
        }

        IUIElement ret = adapter.get(key);
        if ((ret == null) && create) {
            ret = doCreate(key);
        }
        return ret;
    }


    /**
     * 
     * @param uiElement
     * @param resource
     */
    private void put(IUIElement uiElement) {
        IResource res = uiElement.getResource();
        adapter.put(res, uiElement);
    }


    /**
     * oldResourceに対応する値を、newResourceの値に差し替える。
     * 
     * @param oldResource
     * @param newResource
     */
    public void changeAdapterKey(IResource oldResource, IResource newResource) {
        IUIElement element = adapter.remove(oldResource);
        adapter.put(newResource, element);
        ((UIElement) element).setResource(newResource);
        if (element instanceof IUIContainer) {
            changeAdapterKey((IUIContainer) element);
        }
    }


    private void changeAdapterKey(IUIContainer parent) {
        IUIElement[] children = parent.getChildren();
        for (IUIElement child : children) {
            IResource oldResource = child.getResource();
            adapter.remove(oldResource);
            String name = oldResource.getName();
            if (child instanceof IUIContainer) {
                IFolder newResource = parent.getResource().getFolder(new Path(name));
                adapter.put(newResource, child);
                ((UIContainer) child).setResource(newResource);
                changeAdapterKey((IUIContainer) child);
            } else {
                IFile newResource = parent.getResource().getFile(new Path(name));
                adapter.put(newResource, child);
                ((UIElement) child).setResource(newResource);
            }
        }
    }


    public void remove(IUIElement element) {
        if (element == null) {
            return;
        }

        if (element.getParent() != null) {
            element.getParent().modifiedChildren();
        }
        if (element instanceof IUIContainer) {
            IPath removePath = element.getResource().getFullPath();
            for (Iterator<Entry<IResource, IUIElement>> itr = adapter.entrySet().iterator(); itr.hasNext();) {
                Entry<IResource, IUIElement> e = itr.next();
                IResource res = e.getKey();
                if (removePath.isPrefixOf(res.getFullPath())) {
                    itr.remove();
                }
            }
        } else {
            adapter.remove(element.getResource());
        }
    }


    /**
     * 指定したICoronaObjectを保持しているIUIElementをすべて取得する
     * 
     * @param key
     * @return
     */
    public List<IUIElement> adapter(ICoronaObject key) {
        /*
         * DBViewの変更(rename, delete)に伴ってProjExplに変更を加えるときに使うことを想定
         */
        return findUIElement(key);
    }


    private List<IUIElement> findUIElement(ICoronaObject key) {
        List<IUIElement> ret = new ArrayList<IUIElement>();
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        if (key instanceof ICoronaDic) {
            for (IUIProject uiProject : getCoronaProjects(projects)) {
                ICoronaProject coronaProject = uiProject.getObject();
                List<ICoronaDic> dics = coronaProject.getDictionarys(key.getClass());
                /* 共通辞書 */
                for (ICoronaDic dic : dics) {
                    if (dic.equals(key)) {
                        ret.add(getDic(uiProject, dic));
                        break;
                    }
                }
                /* ターゲット下の辞書 */
                for (ICoronaProduct product : coronaProject.getProducts()) {
                    for (ICoronaDic dic : product.getDictionarys(key.getClass())) {
                        if (dic.equals(key)) {
                            ret.add(getDic(getProduct(uiProject, product), dic));
                            break;
                        }
                    }
                }
            }
        } else if (key instanceof ICoronaProject) {
            for (IUIProject uiProject : getCoronaProjects(projects)) {
                if (uiProject.getObject().equals(key)) {
                    ret.add(uiProject);
                    break;
                }
            }
        } else if (key instanceof ICoronaProduct) {
            for (IUIProject uiProject : getCoronaProjects(projects)) {
                for (ICoronaProduct product : uiProject.getObject().getProducts()) {
                    if (product.equals(key)) {
                        ret.add(getProduct(uiProject, product));
                        break;
                    }
                }
            }
        } else if (key instanceof IClaimData) {
            for (IUIProject uiProject : getCoronaProjects(projects)) {
                for (IClaimData claim : uiProject.getObject().getClaimDatas()) {
                    if (claim.equals(key)) {
                        ret.add(getClaim(uiProject, claim));
                    }
                }
            }
        } else if (key instanceof IClaimWorkData) {
            IClaimWorkData work = (IClaimWorkData) key;
            int projectId = work.getProjectId();
            int productId = work.getProductId();
            int claimId = work.getClaimId();
            ClaimWorkDataType type = work.getClaimWorkDataType();
            int fieldId = work.getFieldId();
            for (IUIProject uiProject : getCoronaProjects(projects)) {
                if (projectId == uiProject.getId()) {
                    for (ICoronaProduct product : uiProject.getObject().getProducts()) {
                        // testH25 20130930 全文表示エリアに表示されない

                        /* 起動元のProductId　と合致する全文表示エリア用データを取得する。 */
                        /* （Projectに複数のターゲットが存在する場合に、起動元のProductIdで区別する） */
                        if (productId == product.getId()) {
                            work = product.getClaimWorkData(claimId, type, fieldId);
                            if (work != null) {
                                ret.add(getWork(getProduct(uiProject, product), work));
                                break;
                            }
                        }

                        //work = product.getClaimWorkData(claimId, type, fieldId);
                        //if (work != null) {
                        //    ret.add(getWork(getProduct(uiProject, product), work));
                        //}
                        // testH25 20130930 全文表示エリアに表示されない
                    }
                }
            }
        }
        return ret;
    }


    /**
     * 指定した Project に対応する UIProject を取得する
     * 
     * <p>
     * 別 DB にあってワークスペースに存在するプロジェクトの場合、{@link #create(IResource)} が NULL を返すので
     * それは破棄する。
     * </p>
     * 
     * @param projects
     * @return
     */
    private IUIProject[] getCoronaProjects(IProject[] projects) {
        List<IUIProject> ret = new ArrayList<IUIProject>(projects.length);
        for (IProject project : projects) {
            /* 開いて閉じてをすると、ProjectExplorerの処理に負荷がかかるため、閉じられているものは判定対象外にする */
            boolean isOpen = project.isOpen();
            if (isOpen && isCoronaProject(project)) {
                IUIProject uip = (IUIProject) create(project);
                if (uip != null) {
                    ret.add(uip);
                }
            }
        }
        return ret.toArray(new IUIProject[ret.size()]);
    }


    /**
     * @param productFolder
     *            ターゲットフォルダリソース。ファイルに使えない文字を含まない
     * @param name
     *            問い合わせデータの通りのターゲット名
     * @return
     */
    public IUIElement create(IFolder productFolder, String name) {
        IUIElement element = adapter(productFolder, false);
        if (element == null) {
            IUIContainer parent = (IUIContainer) adapter(productFolder.getParent(), true);
            ICoronaObject object = createCoronaProduct(productFolder, name);
            if (object != null) {
                return doCreate(parent, object, productFolder);
            }
            return null;
        }
        return element;
    }


    /** 接続先のDBに登録されているプロジェクトのみを開かせる処理 */
    public void openCoronaProject() {
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        IProgressMonitor monitor = new NullProgressMonitor();

        for (IProject project : projects) {
            try {
                boolean isOpened = project.isOpen();
                if (!isOpened) {
                    project.open(monitor);
                }
                if (CoronaModel.INSTANCE.adapter(project, true) == null) {
                    /* 非Coronaプロジェクトは対象外 */
                    if (isCoronaProject(project)) {
                        if (isOpened) {
                            CoronaActivator.getDefault().getLogger().getOutStream().println(project.getName() + Messages.CoronaModel_errorNotAvailable);
                        }
                        project.close(monitor);
                    }
                }

                /* 閉じていた非CoronaProjectは閉じたままにする */
                if (!isOpened && project.isOpen() && !isCoronaProject(project)) {
                    project.close(monitor);
                }
            } catch (CoreException e) {
                e.printStackTrace();
                IStatus error = new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, e.getLocalizedMessage());
                CoronaActivator.log(error, false);
            }
        }
    }

}
