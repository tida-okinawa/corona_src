/**
 * @version $Id: TwitterStatus.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/09/20 15:59:12
 * @author yukihiro-kinjo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.webentry.twitter;

import com.tida_okinawa.corona.webentry.Messages;

/**
 * Twitterの接続ステータスを定義する
 * 
 * @author yukihiro-kinjo
 * 
 */
public class TwitterStatus {

    /** サービス名：Twitter */
    public static final String TWITTER = Messages.TwitterStatus_Twitter;

    /** ログイン状態:アカウント未設定 */
    public static final String NO_SETTING_ACCOUNT = Messages.TwitterStatus_NoSettingAccount;

    /** ログイン状態:アカウント設定済み */
    public static final String SETTING_ACCOUNT = Messages.TwitterStatus_SettingAccount;

    /** ログイン状態:ログイン失敗 */
    public static final String FAILED_LOGIN = Messages.TwitterStatus_FailedLogin;

    /** ログイン状態:ログイン成功 */
    public static final String OK_LOGIN = Messages.TwitterStatus_OkLogin;
}
