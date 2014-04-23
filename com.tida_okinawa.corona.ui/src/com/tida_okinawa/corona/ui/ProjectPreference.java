/**
 * @version $Id: ProjectPreference.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/02/21 10:17:46
 * @author miyaguni
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui;

import com.tida_okinawa.corona.internal.ui.util.PreferenceUtils;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ICoronaProject.ProjectKnpConfig;


/**
 * Project と Preference を繋ぐクラス
 * 
 * @author miyaguni
 */
public class ProjectPreference {
    /**
     * 指定したプロジェクトが係り受け解析を行うかどうか調べる
     * 
     * <p>
     * 以下の要素を鑑みて決定される
     * </p>
     * 
     * <ol>
     * <li>プロジェクト毎の設定 (project テーブルの knpconfig)</li>
     * <li>[設定]-[Corona]-[係り受け解析(KNP)を行う]の設定</li>
     * </ol>
     * 
     * <pre>
     * プロジェクト毎の設定で
     * 1. ワークスペース設定を引き継ぐ(INHERIT)場合は PreferenceUtils.isDoKnp() を
     * 2. そうでない場合は、project.getKnpConfig() を使用する
     * </pre>
     * 
     * @param projectId
     *            プロジェクトID
     * @return 係り受け解析を行うのであれば TRUE
     */
    public static boolean isDoKnp(int projectId) {
        ProjectKnpConfig projectConfig = IoActivator.getService().getProject(projectId).getKnpConfig();
        boolean parentConfig = PreferenceUtils.isDoKnp();

        if (projectConfig.equals(ProjectKnpConfig.INHERIT)) {
            return parentConfig;
        }

        return projectConfig.equals(ProjectKnpConfig.DO);
    }
}
