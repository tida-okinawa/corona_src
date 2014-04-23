/**
 * @version $Id: PersistentPropertyKeys.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/09 20:11:44
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui;

import org.eclipse.core.runtime.QualifiedName;

import com.tida_okinawa.corona.ui.UIActivator;

/**
 * プロジェクトが永続的に値を保持するときのキーをまとめたクラス
 * 
 * @author kousuke-morishima
 */
public class PersistentPropertyKeys {

    /**
     * IProjectがDBに登録されているときのIDを取得する
     */
    public static final QualifiedName PROJECT_ID = new QualifiedName(UIActivator.PLUGIN_ID, "project id");

    /**
     * IProjectが登録されているDBへの接続名を取得するキー
     */
    public static final QualifiedName DB_CONNECT_NAME = new QualifiedName(UIActivator.PLUGIN_ID, "db connect name");

    /**
     * IProjectが登録されているDBへ接続するURLを取得するキー
     */
    public static final QualifiedName DB_CONNECT_URL = new QualifiedName(UIActivator.PLUGIN_ID, "db connect url");
}
