/**
 * @version $Id: ControlLocalFolderAndFileUtil.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/20 16:08:00
 * @author shingo_wakamatsu
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.tida_okinawa.corona.internal.ui.CoronaConstants;
import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProject;
import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.internal.ui.views.model.impl.FileContent;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;

/**
 * @author shingo_wakamatsu
 */
public class ControlLocalFolderAndFileUtil {

    /**
     * デフォルトコンストラクター。処理なし
     */
    public ControlLocalFolderAndFileUtil() {
    }


    /**
     * ローカルに「ターゲット」以下の辞書、中間データのファイルを作成
     * 
     * @param uiProject
     * @param monitor
     *            プログレスモニター
     */
    public void createDicAndWorkData(IUIProject uiProject, IProgressMonitor monitor) {

        for (IUIElement child : uiProject.getChildren()) {
            /* 「ターゲット」 */
            if (child instanceof IUIProduct) {
                IUIProduct uiProduct = (IUIProduct) child;

                // PrjExplの「ターゲット」情報を取得
                ICoronaProduct product = uiProduct.getObject();
                if (product == null) {
                    continue;
                }
                uiProduct.update(monitor);

                /* 辞書フォルダを作成 */
                createFolder(uiProduct.getResource(), CoronaConstants.LIBRARY_NAME, monitor);
                /* 辞書データの実ファイルを作成 */
                for (ICoronaDic dic : product.getDictionarys(ICoronaDic.class)) {
                    IUIDictionary uiDic = CoronaModel.INSTANCE.getDic(uiProduct, dic);
                    uiDic.update(monitor);
                }

                /* ターゲットフォルダ,処理結果フォルダを作成 */
                List<IClaimData> claims = uiProduct.getObject().getClaimDatas();
                for (IClaimData claim : claims) {

                    createFolder(uiProduct.getResource(), CoronaConstants.createCorrectionFolderName(claim.getName()), monitor);
                    /* 中間データの実ファイルを作成 */
                    for (IClaimWorkData work : product.getClaimWorkDatas()) {
                        IUIWork uiWork = CoronaModel.INSTANCE.getWork(uiProduct, work);
                        uiWork.update(monitor);
                    }
                }
            }
        }
    }


    /**
     * ローカルに共通辞書ファイルを作成
     * 
     * @param uiProject
     * @param monitor
     *            プログレスモニター
     */
    public void createCommonDicFile(IUIProject uiProject, IProgressMonitor monitor) {
        IProject iProject = uiProject.getResource();
        /* フォルダが存在していなかったら、新規作成 */
        IFolder cmnDicFolder = uiProject.getResource().getFolder(CoronaConstants.COMMON_LIBRARY_NAME);
        if (cmnDicFolder.exists() == false) {
            createFolder(iProject, CoronaConstants.COMMON_LIBRARY_NAME, monitor);
        }

        ICoronaProject coronaProject = uiProject.getObject();
        List<ICoronaDic> dics = coronaProject.getDictionarys(ICoronaDic.class);
        for (ICoronaDic d : dics) {
            createFile(cmnDicFolder, d.getName(), monitor);
        }
    }


    /**
     * ローカルに問い合わせデータファイルを作成
     * 
     * @param uiProject
     * @param monitor
     *            プログレスモニター
     */
    public void createClaimDataFile(IUIProject uiProject, IProgressMonitor monitor) {
        IProject iProject = uiProject.getResource();
        /* フォルダが存在していなかったら、新規作成 */
        IFolder claimDataFolder = uiProject.getResource().getFolder(CoronaConstants.CLAIM_FOLDER_NAME);
        if (claimDataFolder.exists() == false) {
            createFolder(iProject, CoronaConstants.CLAIM_FOLDER_NAME, monitor);
        }

        ICoronaProject coronaProject = uiProject.getObject();
        List<IClaimData> claims = coronaProject.getClaimDatas();
        for (IClaimData c : claims) {
            createFile(claimDataFolder, c.getName(), monitor);
        }
    }


    /**
     * ローカルにフォルダーを作成
     * 
     * @param iContainer
     * @param path
     *            作成パス
     * @param monitor
     *            プログレスモニター
     * @return 作成されたフォルダーのハンドル
     */
    public IFolder createFolder(IContainer iContainer, String path, IProgressMonitor monitor) {
        IFolder folder = iContainer.getFolder(new Path(path));
        if (!folder.exists()) {
            try {
                folder.create(false, true, monitor);
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
        return folder;
    }


    /**
     * ローカルにファイルを作成
     * 
     * @param iContainer
     * @param path
     *            作成パス
     * @param monitor
     *            プログレスモニター
     * @return 作成されたファイルのハンドル
     */
    public IFile createFile(IContainer iContainer, String path, IProgressMonitor monitor) {
        IFile file = iContainer.getFile(new Path(path));
        if (!file.exists()) {
            try {
                file.create(FileContent.toStream(CoronaModel.INSTANCE.create(file)), true, monitor);
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
