/**
 * @version $Id: WebEntryExternalControllerForTwitter.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/09/25 14:20:18
 * @author yukihiro-kinjo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.webentry;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import twitter4j.Status;

import com.tida_okinawa.corona.correction.controller.ErratumController;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.webentry.twitter.TweetConvert;
import com.tida_okinawa.corona.webentry.twitter.TwitterConnection;

/**
 * WebEntryプラグインを外部から利用するための操作クラス。<br />
 * このクラスのメソッドはWebEntryプラグイン内部で生成される例外を返却しません。
 * これは内部で持っているライブラリー固有の例外が存在する為です。
 * 最後に発生した例外を取得したい場合 {@link #getLastException()} メソッドで取得が可能です。
 * ただし、実行時例外に関してはスローされる可能性が有ります。
 * 
 * @author yukihiro-kinjo
 * 
 */
public class WebEntryExternalControllerForTwitter {

    private TwitterConnection connection;
    private WebEntryException internalLastException;


    /**
     * コンストラクター
     */
    public WebEntryExternalControllerForTwitter() {
        connection = new TwitterConnection();
        internalLastException = null;
    }


    /**
     * 自動実行用
     * 通常、<code>ResourcesPlugin.getPlugin().getStateLocation()</code>
     * で取れるパスを渡し、スタンドアロン起動時でもTwitterAccessTokenやテンポラリファイルの保存位置を取得できるようにする
     * 
     * @param stateLocation
     *            プラグインのデータ保存領域のパス
     */
    public WebEntryExternalControllerForTwitter(String stateLocation) {
        connection = new TwitterConnection();
        TwitterConnection.setStateLocation(stateLocation);
        internalLastException = null;
    }


    /**
     * このクラス内の処理で最後に発生した例外を取得する。<br />
     * 返される {@link WebEntryException} オブジェクトは
     * スローされる原因となった例外を保持している場合があり、 {@link WebEntryException#getCause()}
     * メソッドで取得できる
     * 
     * @return 最後に発生した例外。無ければnullを返却
     */
    public WebEntryException getLastException() {
        return this.internalLastException;
    }


    /**
     * このクラス内の処理で最後に発生した例外をクリアする
     */
    public void clearLastException() {
        this.internalLastException = null;
    }


    /**
     * デバッグモードの設定。<br />
     * 有効な場合、Twitter接続処理内で例外が発生した場合に
     * 標準出力にスタックトレースを出力する
     * 
     * 通信の詳細をさらに詳細に確認した場合VM引数に
     * -Dtwitter4j.debug=true
     * を追記してCoronaを起動してください
     * 
     * @param mode
     *            有効にする場合true
     */
    public void setDebugMode(boolean mode) {
        connection.setDebugMode(mode);
    }


    /**
     * Twitterの初回認証が済んでいる状態で接続する
     * 
     * @return 何らかの問題が発生した場合にfalseを返却
     */
    public boolean twitterSubsequentConnect() {
        return connection.twitterSubsequentConnect();
    }


    /**
     * アクセストークンが保存されているかを取得する。<br />
     * Twitter側の操作でCoronaのアクセス許可を解除する事が可能であり、
     * トークンが保存されている=接続可、ではない点に注意
     * 
     * @return 保存済みであればtrue
     */
    public boolean isSaveAccessToken() {
        return connection.isSaveAccessToken();
    }


    /**
     * Twitterへ検索クエリーを実行し、結果をファイルに保存する。<br />
     * 別スレッドでの処理は行われない。<br />
     * このメソッド内で発生した問題は適宜例外が生成され、{@link #getLastException()} で取得できる。
     * 
     * @param setting
     *            設定済みのTwitter検索クエリー設定オブジェクト
     * @param csvPath
     *            検索結果CSVファイルの保存先パス。nullを指定した場合は保存されない
     * @param sqlPath
     *            検索結果定義SQLファイルの保存先パス。nullを指定した場合は保存されない
     * @param monitor
     *            進行状況を進めるIProgressMonitor。nullを渡した場合モニターを処理しない。
     *            モニターの開始と終了は呼び出し側で行う必要がある。
     * @return 最後まで処理が正常に行われた場合trueを返却
     */
    public boolean searchTwitterAndSaveFiles(TwitterQuerySettings setting, String csvPath, String sqlPath, IProgressMonitor monitor) {

        try {
            if (!(setting.isValidSettings())) {
                internalLastException = new WebEntryException(Messages.WebEntryExternalControllerForTwitter_InvalidQuerySetting);
                internalLastException.initCause(new WebEntryException(setting.getValidateErrorMessage()));
                return false;
            }

            /* 登録処理用入力データの用意 */
            final String searchWord = setting.getSearchWord();
            final int searchMax = setting.getSearchMax();
            final boolean japaneseOnly = setting.isJapaneseOnly();
            final String dataName = setting.getDataName();
            final String productName = setting.getProductName();
            final boolean useScreenName = setting.isUseScreenName();
            final boolean useName = setting.isUseName();
            final boolean useDateTime = setting.isUseDateTime();

            final boolean useFilterRt = setting.isFilterRt();
            final boolean useFilterReply = setting.isFilterReply();
            final boolean useRtCount = setting.isUseRtCount();
            final boolean useFollowers = setting.isUseFollowers();
            final boolean useFollows = setting.isUseFollows();
            final boolean useProfile = setting.isUseProfile();
            final boolean tcoExpand = setting.isTcoExpand();

            if (monitor == null) {
                monitor = new NullProgressMonitor();
            }

            monitor.beginTask(null, 15); /* その他処理１：検索４ */

            /* Twitterへ接続 */
            monitor.subTask(Messages.TwitterFinishProgress_ConnectTwitter);
            TwitterConnection twitter = new TwitterConnection();
            twitter.twitterSubsequentConnect();
            if (monitor.isCanceled()) {
                internalLastException = new WebEntryException(Messages.WebEntryExternalControllerForTwitter_UserCanceled);
                return false;
            }
            monitor.worked(1);

            /* Twitterへ検索クエリーを実行 */
            monitor.subTask(Messages.TwitterFinishProgress_SearchExecTwitter);
            List<Status> getTweet = null;
            getTweet = twitter.searchTwitter(searchWord, searchMax, japaneseOnly,
                    new SubProgressMonitor(monitor, 12, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
            if (monitor.isCanceled()) {
                internalLastException = new WebEntryException(Messages.WebEntryExternalControllerForTwitter_UserCanceled);
                return false;
            }
            if (getTweet.size() < 1) {
                internalLastException = new WebEntryException(Messages.WebEntryExternalControllerForTwitter_GetCountOfZero);
                return false;
            }

            /* 取得した投稿をCoronaで読み込める形式に変換する */
            monitor.subTask(Messages.TwitterFinishProgress_TwitterDataConvert);
            TweetConvert converter = new TweetConvert(dataName, productName, useScreenName, useName, useDateTime, useRtCount, useFollows, useFollowers,
                    useProfile, tcoExpand);
            /* スタンドアロン起動時でも、テンポラリファイルを保存する場所を特定できるように、保存パスを渡してあげる */
            TweetConvert.setStateLocation(TwitterConnection.getStateLocation());
            // CSV、SQLテンポラリファイルの削除は行わない
            if (csvPath != null) {
                if (useFilterRt) {
                    converter.filterRT(getTweet);
                }
                if (useFilterReply) {
                    converter.filterReply(getTweet);
                }
                if (!(converter.convertTweetToCsvFile(getTweet, csvPath))) {
                    internalLastException = new WebEntryException(Messages.WebEntryExternalControllerForTwitter_DataConvertFailed);
                    return false;
                }
            }
            monitor.worked(1);
            if (sqlPath != null) {
                if (!(converter.createDefineSqlFile(sqlPath))) {
                    internalLastException = new WebEntryException(Messages.WebEntryExternalControllerForTwitter_DataConvertFailed);
                    return false;
                }
            }
            monitor.worked(1);
        } finally {
            monitor.done();
        }
        return true;
    }


    /**
     * Twitterへ検索クエリーを実行し、結果をデータベースにインポートする。<br />
     * 別スレッドでの処理は行われない。
     * また、データベースビューの表示の更新は行われない。<br />
     * このメソッド内で発生した問題は適宜例外が生成され、{@link #getLastException()} で取得できる。
     * 
     * @param setting
     *            設定済みのTwitter検索クエリー設定オブジェクト
     * @param monitor
     *            進行状況を進めるIProgressMonitor。nullを渡した場合モニターを処理しない。
     *            モニターの開始と終了は呼び出し側で行う必要がある。
     * @return 最後まで処理が正常に行われた場合trueを返却
     */
    public boolean searchTwitterAndImportDataBase(TwitterQuerySettings setting, IProgressMonitor monitor) {
        registeredClaimData = null;

        if (!(setting.isValidSettings())) {
            internalLastException = new WebEntryException(Messages.WebEntryExternalControllerForTwitter_InvalidQuerySetting);
            internalLastException.initCause(new WebEntryException(setting.getValidateErrorMessage()));
            return false;
        }

        /* 登録処理用入力データの用意 */
        final String searchWord = setting.getSearchWord();
        final int searchMax = setting.getSearchMax();
        final boolean japaneseOnly = setting.isJapaneseOnly();
        final String dataName = setting.getDataName();
        final String productName = setting.getProductName();
        final boolean useScreenName = setting.isUseScreenName();
        final boolean useName = setting.isUseName();
        final boolean useDateTime = setting.isUseDateTime();

        final boolean useFilterRt = setting.isFilterRt();
        final boolean useFilterReply = setting.isFilterReply();
        final boolean useRtCount = setting.isUseRtCount();
        final boolean useFollowers = setting.isUseFollowers();
        final boolean useFollows = setting.isUseFollows();
        final boolean useProfile = setting.isUseProfile();
        final boolean tcoExpand = setting.isTcoExpand();

        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        monitor.beginTask(null, 25); /* その他処理１：検索４ */

        /* Twitterへ接続 */
        monitor.subTask(Messages.TwitterFinishProgress_ConnectTwitter);
        TwitterConnection twitter = new TwitterConnection();
        twitter.twitterSubsequentConnect();
        if (monitor.isCanceled()) {
            internalLastException = new WebEntryException(Messages.WebEntryExternalControllerForTwitter_UserCanceled);
            return false;
        }
        monitor.worked(1);

        /* Twitterへ検索クエリーを実行 */
        monitor.subTask(Messages.TwitterFinishProgress_SearchExecTwitter);
        List<Status> getTweet = null;
        getTweet = twitter.searchTwitter(searchWord, searchMax, japaneseOnly, new SubProgressMonitor(monitor, 20, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
        if (monitor.isCanceled()) {
            internalLastException = new WebEntryException(Messages.WebEntryExternalControllerForTwitter_UserCanceled);
            return false;
        }
        if (getTweet.size() < 1) {
            internalLastException = new WebEntryException(Messages.WebEntryExternalControllerForTwitter_GetCountOfZero);
            return false;
        }

        /* 取得した投稿をCoronaで読み込める形式に変換する */
        monitor.subTask(Messages.TwitterFinishProgress_TwitterDataConvert);
        TweetConvert converter = new TweetConvert(dataName, productName, useScreenName, useName, useDateTime, useRtCount, useFollows, useFollowers, useProfile,
                tcoExpand);
        /* スタンドアロン起動時でも、テンポラリファイルを保存する場所を特定できるように、保存パスを渡してあげる */
        TweetConvert.setStateLocation(TwitterConnection.getStateLocation());
        converter.deleteConvertTempFiles();
        String tempDirPath = TweetConvert.getTempStoreDirPath();
        if (tempDirPath == null) {
            internalLastException = new WebEntryException(Messages.WebEntryExternalControllerForTwitter_DataImportFailed);
            return false;
        }
        String tempCsvFilePath = tempDirPath + dataName + ".csv"; //$NON-NLS-1$
        String tempSqlFilePath = tempDirPath + dataName + ".sql"; //$NON-NLS-1$
        if (useFilterRt) {
            converter.filterRT(getTweet);
        }
        if (useFilterReply) {
            converter.filterReply(getTweet);
        }
        if (!(converter.convertTweetToCsvFile(getTweet, tempCsvFilePath))) {
            internalLastException = new WebEntryException(Messages.WebEntryExternalControllerForTwitter_DataImportFailed);
            return false;
        }
        monitor.worked(1);
        if (!(converter.createDefineSqlFile(tempSqlFilePath))) {
            internalLastException = new WebEntryException(Messages.WebEntryExternalControllerForTwitter_DataImportFailed);
            return false;
        }
        monitor.worked(1);
        if (monitor.isCanceled()) {
            internalLastException = new WebEntryException(Messages.WebEntryExternalControllerForTwitter_UserCanceled);
            return false;
        }


        /* データベースへインポート */
        IClaimData claimData;
        monitor.subTask(Messages.TwitterFinishProgress_DataBaseImport);
        try {
            tempCsvFilePath = tempCsvFilePath.replace("/", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
            claimData = IoActivator.getService().importClaimData(tempCsvFilePath, tempSqlFilePath, dataName, false);
        } catch (SQLException | IOException e) {
            internalLastException = new WebEntryException(Messages.WebEntryExternalControllerForTwitter_DataImportFailed);
            internalLastException.initCause(e);
            return false;
        }
        if (claimData == null) {
            internalLastException = new WebEntryException(Messages.WebEntryExternalControllerForTwitter_DataImportFailed);
            return false;
        }
        monitor.worked(1);

        /* 誤記補正処理の準備 */
        monitor.subTask(Messages.TwitterFinishProgress_ExecErratum);
        claimData.setDispIdField(1);
        int bodyFieldNum = 2;
        int productFieldNum = 3;
        if (useScreenName) {
            bodyFieldNum++;
            productFieldNum++;
        }
        if (useName) {
            bodyFieldNum++;
            productFieldNum++;
        }
        if (useDateTime) {
            productFieldNum++;
        }
        if (useRtCount) {
            productFieldNum++;
        }
        if (useFollows) {
            productFieldNum++;
        }
        if (useFollowers) {
            productFieldNum++;
        }
        if (useProfile) {
            productFieldNum++;
        }
        claimData.setProductField(productFieldNum);
        /* 誤記補正する前に誤記補正フィールドをcommitしてしまうと、誤記補正結果が正しくDBに書き込まれないため、コメントアウト */
        //claimData.addCorrectionMistakesField(bodyFieldNum);
        claimData.commit();

        /* 誤記補正処理の実行 */
        List<IFieldHeader> targetFields = new ArrayList<IFieldHeader>(1);
        targetFields.add(claimData.getFieldInformation(bodyFieldNum));
        ErratumController controller = new ErratumController(claimData, targetFields, null);
        try {
            controller.run(null); // IRunnableWithProgressとしては起動しない
        } catch (InvocationTargetException | InterruptedException e) {
            internalLastException = new WebEntryException(Messages.WebEntryExternalControllerForTwitter_DataErratumFailed);
            internalLastException.initCause(e);
            return false;
        }
        monitor.worked(1);
        monitor.done();

        registeredClaimData = claimData;
        return true;
    }


    private IClaimData registeredClaimData;


    /**
     * {@link #searchTwitterAndImportDataBase(TwitterQuerySettings, IProgressMonitor)}
     * が成功した時に限り、登録した問い合わせデータを取得できる。
     * 
     * 処理を実行していない場合や、処理に失敗した場合はnullを返す。
     * 
     * @return 登録した問い合わせデータ
     */
    public IClaimData getClaimData() {
        return registeredClaimData;
    }

}