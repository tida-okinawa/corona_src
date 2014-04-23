/**
 * @version $Id: TwitterQuerySettings.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/09/28 15:36:53
 * @author yukihiro-kinjo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.webentry;

import com.tida_okinawa.corona.webentry.util.CreateClaimTableValidator;

/**
 * Twitter検索クエリー設定オブジェクト
 * 
 * @author yukihiro-kinjo
 * 
 */
public class TwitterQuerySettings {
    /** 検索で取得できる最大件数(本来は件数制限なんて意味がないんだけどね) */
    public static final int TWITTER_SEARCH_MAX = 15000;

    private String dataName;
    private boolean useName;
    private int searchMax;
    private String productName;
    private boolean japaneseOnly;
    private boolean useDateTime;
    private boolean useScreenName;
    private String searchWord;

    private boolean isFilterRt;
    private boolean isFilterReply;
    private boolean isUseRtCount;
    private boolean isUseFollowers;
    private boolean isUseFollows;
    private boolean isUseProfile;
    private boolean isTcoExpand;

    private String validateError;


    /**
     * Twitter検索クエリ設定オブジェクトをインスタンス化します。<br />
     * 
     * 引数で設定する値以外の設定値はデフォルト値が適用されます。
     * 変更が必要な値は適切なsetterメソッドで設定してください。
     * また、設定値が有効な状態であるかは{@link #isValidSettings()} メソッドで確認可能です。
     * 
     * @param dataName
     *            問い合わせデータ名
     * @param productName
     *            ターゲット名
     * @param searchWord
     *            検索クエリー
     */
    public TwitterQuerySettings(String dataName, String productName, String searchWord) {
        this.dataName = dataName;
        this.useName = true;
        this.searchMax = TWITTER_SEARCH_MAX;
        this.productName = productName;
        this.japaneseOnly = false;
        this.useDateTime = true;
        this.useScreenName = true;
        this.searchWord = searchWord;

        this.isFilterRt = false;
        this.isFilterReply = false;
        this.isUseRtCount = false;
        this.isUseFollowers = false;
        this.isUseFollows = false;
        this.isUseProfile = false;
        this.isTcoExpand = false;

        this.validateError = ""; //$NON-NLS-1$
    }


    /**
     * 設定値が有効な状態であるかを取得。<br />
     * 詳細なエラー内容は {@link #getValidateErrorMessage()} メソッドで文字列表現を取得できます。
     * 
     * @return 設定内容に問題が無ければtrue
     */
    public boolean isValidSettings() {
        /* nullチェック */
        if (dataName == null) {
            validateError = Messages.TwitterQuerySettings_DataNameIsNull;
            return false;
        }
        if (productName == null) {
            validateError = Messages.TwitterQuerySettings_ProductNameIsNull;
            return false;
        }
        if (searchWord == null) {
            validateError = Messages.TwitterQuerySettings_QueryIsNull;
            return false;
        }

        /* Table名チェック */
        /* 重複 */
        CreateClaimTableValidator tableValidator = new CreateClaimTableValidator();
        if (tableValidator.exists(dataName)) {
            validateError = Messages.TwitterQuerySettings_DataBaseNameExists;
            return false;
        }
        /* 正しい形式の名前か */
        String warning = tableValidator.isValid(dataName);
        if (dataName.length() == 0 || (warning != null)) {
            /* textDataNameに入力なし(length==0) */
            validateError = warning;
            return false;
        }
        /* ターゲット名チェック */
        if (productName.isEmpty()) {
            validateError = Messages.TwitterQuerySettings_PleaseInputProductName;
            return false;
        }
        if (productName.length() > 128) {
            validateError = Messages.TwitterQuerySettings_ProductNameLengthError;
            return false;
        }
        if (productName.indexOf(",") != -1) { //$NON-NLS-1$
            validateError = Messages.TwitterQuerySettings_ProductNameCharacterError;
            return false;
        }
        /* 検索ワードチェック */
        if (searchWord.isEmpty()) {
            validateError = Messages.TwitterQuerySettings_PleaseInputQuery;
            return false;
        }
        if (searchWord.length() > 256) {
            validateError = Messages.TwitterQuerySettings_QueryLengthError;
            return false;
        }
        /* 取得件数チェック */
        if ((searchMax < 1) || (searchMax > TWITTER_SEARCH_MAX)) {
            validateError = Messages.TwitterQuerySettings_GetCountLengthError;
            return false;
        }
        validateError = ""; //$NON-NLS-1$
        return true;
    }


    /**
     * 設定値のエラー内容を文字列表現として取得します。<br />
     * このメソッドで適切な値を取得する為には、
     * 先に {@link #isValidSettings()} メソッドを呼び出す必要が有ります。
     * 
     * @return 設定値のエラー内容文字列表現。エラーが見つからなかった場合、空文字を返却します。
     */
    public String getValidateErrorMessage() {
        return this.validateError;
    }


    /**
     * 問い合わせデータ名を取得
     * 
     * @return 問い合わせデータ名
     */
    public String getDataName() {
        return dataName;
    }


    /**
     * 問い合わせデータ名を設定
     * 
     * @param dataName
     *            問い合わせデータ名
     */
    public void setDataName(String dataName) {
        this.dataName = dataName;
    }


    /**
     * Name(ユーザーID)を利用するかを取得
     * 
     * @return ユーザーIDを利用する場合true
     */
    public boolean isUseName() {
        return useName;
    }


    /**
     * Name(ユーザーID)を利用するかを設定
     * 
     * @param useName
     *            ユーザーIDを利用する場合true
     */
    public void setUseName(boolean useName) {
        this.useName = useName;
    }


    /**
     * 最大取得件数を取得
     * 
     * @return 最大取得件数
     */
    public int getSearchMax() {
        return searchMax;
    }


    /**
     * 最大取得件数を設定
     * 
     * @param getCount
     *            最大取得件数
     */
    public void setSearchMax(int getCount) {
        if ((getCount < 1) || (getCount > TWITTER_SEARCH_MAX)) {
            validateError = Messages.TwitterQuerySettings_GetCountLengthError;
            return;
        }
        this.searchMax = getCount;
    }


    /**
     * ターゲット名を取得
     * 
     * @return ターゲット名
     */
    public String getProductName() {
        return productName;
    }


    /**
     * ターゲット名を設定
     * 
     * @param productName
     *            ターゲット名
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }


    /**
     * 日本語の投稿のみを取得するかを取得
     * 
     * @return 日本語の投稿のみを取得する場合true
     */
    public boolean isJapaneseOnly() {
        return japaneseOnly;
    }


    /**
     * 日本語の投稿のみを取得するかを設定
     * 
     * @param japaneseOnly
     *            日本語の投稿のみを取得する場合true
     */
    public void setJapaneseOnly(boolean japaneseOnly) {
        this.japaneseOnly = japaneseOnly;
    }


    /**
     * 投稿日時を利用するかを取得
     * 
     * @return 投稿日時を利用する場合true
     */
    public boolean isUseDateTime() {
        return useDateTime;
    }


    /**
     * 投稿日時を利用するかを設定
     * 
     * @param useDateTime
     *            投稿日時を利用する場合true
     */
    public void setUseDateTime(boolean useDateTime) {
        this.useDateTime = useDateTime;
    }


    /**
     * ScreenName(ユーザー表示名)を利用するかを取得
     * 
     * @return ユーザー表示名を利用する場合true
     */
    public boolean isUseScreenName() {
        return useScreenName;
    }


    /**
     * ScreenName(ユーザー表示名)を利用するかを設定
     * 
     * @param useScreenName
     *            ユーザー表示名を利用する場合true
     */
    public void setUseScreenName(boolean useScreenName) {
        this.useScreenName = useScreenName;
    }


    /**
     * 検索クエリーを取得
     * 
     * @return 検索クエリー
     */
    public String getSearchWord() {
        return searchWord;
    }


    /**
     * 検索クエリーを設定
     * 
     * @param searchWord
     *            検索クエリー
     */
    public void setSearchWord(String searchWord) {
        this.searchWord = searchWord;
    }


    /**
     * リツイートフィルタリング設定を取得
     * 
     * @return the isFilterRt 有効の場合true
     */
    public boolean isFilterRt() {
        return isFilterRt;
    }


    /**
     * リツイートフィルタリングを設定
     * 
     * @param isFilterRt
     *            有効にする場合true
     */
    public void setFilterRt(boolean isFilterRt) {
        this.isFilterRt = isFilterRt;
    }


    /**
     * リプライフィルタリング設定を取得
     * 
     * @return 有効の場合true
     */
    public boolean isFilterReply() {
        return isFilterReply;
    }


    /**
     * リプライフィルタリングを設定
     * 
     * @param isFilterReply
     *            有効にする場合true
     */
    public void setFilterReply(boolean isFilterReply) {
        this.isFilterReply = isFilterReply;
    }


    /**
     * リツイート数を利用するか
     * 
     * @return 利用する場合true
     */
    public boolean isUseRtCount() {
        return isUseRtCount;
    }


    /**
     * リツイート数を利用するか設定
     * 
     * @param isUseRtCount
     *            利用する場合true
     */
    public void setUseRtCount(boolean isUseRtCount) {
        this.isUseRtCount = isUseRtCount;
    }


    /**
     * フォロワー数を利用するか
     * 
     * @return 利用する場合true
     */
    public boolean isUseFollowers() {
        return isUseFollowers;
    }


    /**
     * フォロワー数を利用するか設定
     * 
     * @param isUseFollowers
     *            利用する場合true
     */
    public void setUseFollowers(boolean isUseFollowers) {
        this.isUseFollowers = isUseFollowers;
    }


    /**
     * フォロー数を利用するか
     * 
     * @return 利用する場合true
     */
    public boolean isUseFollows() {
        return isUseFollows;
    }


    /**
     * フォロー数を利用するか設定
     * 
     * @param isUseFollows
     *            利用する場合true
     */
    public void setUseFollows(boolean isUseFollows) {
        this.isUseFollows = isUseFollows;
    }


    /**
     * プロフィールを利用するか
     * 
     * @return 利用する場合true
     */
    public boolean isUseProfile() {
        return isUseProfile;
    }


    /**
     * プロフィールを利用するか設定
     * 
     * @param isUseProfile
     *            利用する場合true
     */
    public void setUseProfile(boolean isUseProfile) {
        this.isUseProfile = isUseProfile;
    }


    /**
     * t.co URL短縮を展開するか
     * 
     * @return 有効にする場合true
     */
    public boolean isTcoExpand() {
        return isTcoExpand;
    }


    /**
     * t.co URL短縮を展開するか設定
     * 
     * @param isTcoExpand
     *            有効にする場合true
     */
    public void setTcoExpand(boolean isTcoExpand) {
        this.isTcoExpand = isTcoExpand;
    }
}
