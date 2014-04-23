/**
 * @version $Id: UIActivator.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/07 18:43:02
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.correction.CorrectionActivator;
import com.tida_okinawa.corona.internal.ui.PersistentPropertyKeys;
import com.tida_okinawa.corona.internal.ui.util.MessageConsoleLogger;
import com.tida_okinawa.corona.internal.ui.util.PreferenceUtils;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.license.LicenseActivator;
import com.tida_okinawa.corona.ui.data.ConnectionParameter;

/**
 * The activator class controls the plug-in life cycle
 */
public class UIActivator extends AbstractUIPlugin {

    /** The plug-in ID */
    public static final String PLUGIN_ID = "com.tida_okinawa.corona.ui"; //$NON-NLS-1$

    // The shared instance
    private static UIActivator plugin;


    /**
     * The constructor
     */
    public UIActivator() {
    }


    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        /* ライセンス確認プラグインを有効にする */
        LicenseActivator.getDefault();

        CorrectionActivator.initAutoSchedules();

        /*
         * 設定から接続パラメータを取得してくる
         * 取得できなかった場合、情報を持たないインスタンスを作って
         * 以降の処理で DB に接続できなかった時と同じ状況にしている
         */
        ConnectionParameter cp = PreferenceUtils.getCurrentConnectionParameter();
        if (cp == null) {
            cp = new ConnectionParameter("", "", "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }

        // testH25 20130806 互換性テスト
        //        /* DBへの接続 */
        //        com.tida_okinawa.corona.io.IoActivator.getService().connect(cp.url, cp.user, cp.passwd);
        //
        boolean isConnect = false;
        try {
            isConnect = com.tida_okinawa.corona.io.IoActivator.getService().connect(cp.url, cp.user, cp.passwd);
        } catch (Exception e) {
            /* コネクト失敗の場合(ユーザ/パスワードが不一致など) */
            e.getStackTrace();
        }

        // testH25 20130806 互換性テスト
        String name = cp.name;
        String dburl = cp.url;
        String dbname = cp.url;
        String lastSep = Messages.UIActivator_LastSep;
        int index = dbname.lastIndexOf(lastSep);
        if (index != -1) {
            dbname = dburl.substring(index + 1);
        }

        if (isConnect) {
            /* DBへの接続は成功 */
            int result[] = { 0 };
            String dbValue = com.tida_okinawa.corona.io.IoActivator.getService().getDbVersion(result).trim();
            LicenseActivator license = LicenseActivator.getDefault();
            String dbVersion = license.getDbVersion().trim();

            /* DBより取得のデータが空文字列 */
            if (dbValue.isEmpty()) {
                /*
                 * SQLでのエラー発生（テーブルがない、レコードがない） →DB定義で「NOT
                 * NULL」属性を付加しているので、正常時での空文字列の取得はない
                 */
                String title = Messages.UIActivator_VerErrTitle;
                String dialogMessage = Messages.UIActivator_VerErrMessage;
                Object args[] = { name, dbname };
                String dialogMessageSub0 = String.format(Messages.UIActivator_VerErrDbInfo, args);
                String dialogMessageSub1 = String.format(Messages.UIActivator_VerErrValue, dbVersion);
                String dialogMessageSub2 = String.format(Messages.UIActivator_VerErrDBValue, dbValue);
                if (result[0] == 2) {
                    /* テーブルなし */
                    dialogMessage = Messages.UIActivator_VerErrMessageNonTable;
                }
                if (result[0] == 1) {
                    /* カラムなし 等のエラー発生 */
                    dialogMessage = Messages.UIActivator_VerErrMessageSqlErr;
                }
                dialogMessage += dialogMessageSub0 + dialogMessageSub1 + dialogMessageSub2;
                // ダイアログ表示(Error)
                MessageDialog.openError(new Shell(), title, dialogMessage);
            }
            /* DBバージョンの確認（アプリ内でのDBVerとDB内のDBVer情報の比較） */
            else if (dbVersion.compareTo(dbValue) != 0) {
                /* DBバージョンが異なっている */
                String title = Messages.UIActivator_VerWarTitle;
                String dialogMessage = Messages.UIActivator_VerWarMessage;
                Object args[] = { name, dbname };
                String dialogMessageSub0 = String.format(Messages.UIActivator_VerErrDbInfo, args);
                String dialogMessageSub1 = String.format(Messages.UIActivator_VerErrValue, dbVersion);
                String dialogMessageSub2 = String.format(Messages.UIActivator_VerErrDBValue, dbValue);
                dialogMessage += dialogMessageSub0 + dialogMessageSub1 + dialogMessageSub2;
                // ダイアログ表示(Warning)
                MessageDialog.openWarning(new Shell(), title, dialogMessage);
            }
        } else {
            /* DBへの接続は失敗 */
            String title = Messages.UIActivator_connectErrTitle;
            String dialogMessage = Messages.UIActivator_connectErrMessage;
            Object args[] = { name, dbname, cp.user };
            String dialogMessageSub0 = String.format(Messages.UIActivator_connectErrDbInfo, args);
            dialogMessage += dialogMessageSub0;
            // ダイアログ表示(Error)
            MessageDialog.openError(new Shell(), title, dialogMessage);
        }

        /* コンソールの利用 */
        CoronaActivator.getDefault().setLogger(MessageConsoleLogger.getInstance());

        /* Logリスナー */
        /* 動きが不安定なので、まだ使わない */
        // CoronaActivator.getDefault().getLog().addLogListener(logListener);

        /* 有効な(DBに登録されている)CoronaProjectのみを開く */
        CoronaModel.INSTANCE.openCoronaProject();
    }


    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        /* DBから切断 */
        com.tida_okinawa.corona.io.IoActivator.getService().disConnect();

        super.stop(context);
    }


    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static UIActivator getDefault() {
        return plugin;
    }

    private Map<IProject, IPreferenceStore> stores = new HashMap<IProject, IPreferenceStore>();


    public IPreferenceStore getPreferenceStore(IProject project) {
        IPreferenceStore store = stores.get(project);
        if (store == null) {
            store = new ScopedPreferenceStore(new ProjectScope(project), PLUGIN_ID + "." + project.getName()); //$NON-NLS-1$
            store.setDefault(PersistentPropertyKeys.DB_CONNECT_NAME.toString(), ""); //$NON-NLS-1$
            store.setDefault(PersistentPropertyKeys.DB_CONNECT_URL.toString(), ""); //$NON-NLS-1$
            store.setDefault(PersistentPropertyKeys.PROJECT_ID.toString(), -1);
            stores.put(project, store);
        }
        return store;
    }


    /* ****************************************
     * Image関連
     */
    @Override
    protected void initializeImageRegistry(ImageRegistry registry) {
        Icons.INSTANCE.init(registry);
    }


    /* ****************************************
     * フラグ
     */
    public static final boolean isAlpha() {
        return true;
    }
}
