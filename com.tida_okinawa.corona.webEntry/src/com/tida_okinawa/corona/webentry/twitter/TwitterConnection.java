/**
 * @version $Id: TwitterConnection.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/09/18 18:30:52
 * @author yukihiro-kinjo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.webentry.twitter;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.webentry.Messages;
import com.tida_okinawa.corona.webentry.WebEntryActivator;

/**
 * Twitterにアクセスし認証・情報取得を行うクラス
 * 
 * @author yukihiro-kinjo
 * 
 */
public class TwitterConnection {
    
    static TwitterToken tokenSetting = new TwitterTokenSetting();
    private static final String CONSUMER_KEY = tokenSetting.getConsumerKey();
    private static final String CONSUMER_SECRET = tokenSetting.getConsumerSecret();
    private static final String TOKEN_FILE_NAME = "twitterAccessToken.sat"; //$NON-NLS-1$
    private static final String THIS_PLUGIN_CONFIG_DIR = "/.webEntry/"; //$NON-NLS-1$
    private static final String TWITTER_CREATE_ACCOUNT_URL = "https://twitter.com/signup"; //$NON-NLS-1$

    /** 検索APIの残り使用回数を取得するキー */
    private static final String Key_Get_SearchTweetRateLimit = "/search/tweets"; //$NON-NLS-1$

    private Twitter twitter;
    private RequestToken requestToken;
    private AccessToken accessToken;
    private boolean debugMode;

    private static String stateLocation;


    /**
     * 自動実行用
     * 
     * @param path
     *            プラグインのデータ保存領域のパス
     */
    public static void setStateLocation(String path) {
        stateLocation = path;
    }


    /**
     * 自動実行用
     * 
     * @return プラグインのデータ保存領域のパス
     */
    public static String getStateLocation() {
        return stateLocation;
    }


    /**
     * コンストラクター
     */
    public TwitterConnection() {
        twitter = new TwitterFactory().getInstance();
        requestToken = null;
        accessToken = null;
        debugMode = false;
        setDebugMode(false);
    }


    /**
     * デバッグモードの設定。<br />
     * 有効な場合、TwitterConnectionの処理内で例外が発生した場合に
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
        this.debugMode = mode;
    }


    /**
     * Twitter初回接続・認証 1。<br />
     * リクエストトークンを取得し、
     * PINを取得するための認証ページを開く
     * 
     * @return 何らかの問題が発生した場合にfalseを返却
     */
    public boolean twitterFistConnectOpenAuthPage() {

        twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
        try {
            requestToken = twitter.getOAuthRequestToken();
        } catch (TwitterException e) {
            if (debugMode) {
                e.printStackTrace();
            }
            return false;
        }
        return openBrowserURI(requestToken.getAuthorizationURL());
    }


    /**
     * Twitter初回接続・認証 2。<br />
     * PINとリクエストトークンからアクセストークンを取得し認証を完了させる
     * 
     * このメソッドを実行する前に {@link #twitterFistConnectOpenAuthPage} を実行しておく必要が有ります
     * 
     * @param pin
     *            Twitter認証ページで得られたPIN
     * @return 何らかの問題が発生した場合にfalseを返却
     */
    public boolean twitterFirstConnectPinInput(String pin) {

        try {
            accessToken = twitter.getOAuthAccessToken(requestToken, pin);
        } catch (TwitterException | IllegalStateException e) {
            /*
             * Twitter4jのJavaDocに記載はないが
             * 認証エラーが発生した場合にIllegalStateExceptionが出るのでキャッチする
             */
            if (debugMode) {
                e.printStackTrace();
            }
            return false;
        }
        try {
            storeAccessToken(accessToken);
            return true;
        } catch (IOException e) {
            if (debugMode) {
                e.printStackTrace();
            }
            return false;
        }
    }


    /**
     * 指定したURIをブラウザーで開く。<br />
     * このメソッド内部での例外の発生はデバッグモードの設定対象外
     * 
     * @param uri
     *            ブラウザーで開くURI
     * @return 何らかの問題が発生した場合にfalseを返却
     */
    private static boolean openBrowserURI(String uri) {

        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.browse(new URI(uri));
        } catch (IOException | URISyntaxException e) {
            return false;
        }
        return true;
    }


    /**
     * Twitterのアカウントを新規作成するページを開く
     * 
     * @return 何らかの問題が発生した場合にfalseを返却
     */
    public static boolean openCreateTwitterAccountPage() {
        return openBrowserURI(TWITTER_CREATE_ACCOUNT_URL);
    }


    /**
     * Twitterの初回認証が済んでいる状態で接続する
     * 
     * @return 何らかの問題が発生した場合にfalseを返却
     */
    public boolean twitterSubsequentConnect() {
        try {
            accessToken = loadAccessToken();
        } catch (ClassNotFoundException | IOException e) {
            if (debugMode) {
                e.printStackTrace();
            }
            return false;
        }
        twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
        twitter.setOAuthAccessToken(accessToken);
        try {
            if (twitter.getScreenName().isEmpty()) {
                return false;
            }
        } catch (IllegalStateException | TwitterException e) {
            if (debugMode) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }


    /**
     * 2回目以降の接続を行うためのアクセストークンを保存する。<br />
     * 例外は呼び出し元でキャッチする必要が有る
     * 
     * @param accessToken
     *            アクセストークン
     * @throws FileNotFoundException
     *             アクセストークンを作成できない。
     * @throws IOException
     *             IOエラー
     */
    private static void storeAccessToken(AccessToken accessToken) throws FileNotFoundException, IOException {

        ObjectOutputStream outputStream = null;
        if (getAccessTokenSavePath() == null) {
            throw new IOException();
        }
        String tokenFilePath = getAccessTokenSavePath() + TOKEN_FILE_NAME;

        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(tokenFilePath));
            outputStream.writeObject(accessToken);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }


    /**
     * 作成済みのアクセストークンを削除する
     * 
     * @return 削除に成功した場合true
     */
    public static boolean deleteAccessToken() {

        String tokenFilePath = getAccessTokenSavePath() + TOKEN_FILE_NAME;
        File tokenFile = new File(tokenFilePath);
        if (tokenFile.exists()) {
            return tokenFile.delete();
        } else {
            return false;
        }
    }


    /**
     * APIリクエスト数の残量と総量をフォーマット済み文字列で返す
     * 
     * @return ( API残量 / API総量 )
     * @throws TwitterException
     *             API量取得に問題が発生した場合にスローされる
     */
    public String getApiRateLimitStatusFormat() throws TwitterException {
        try {
            Map<String, RateLimitStatus> rateLimits = twitter.getRateLimitStatus();
            RateLimitStatus rateLimit = rateLimits.get(Key_Get_SearchTweetRateLimit);
            StringBuilder outBuffer = new StringBuilder(32);
            if (rateLimit == null) {
                outBuffer.append(Messages.TwitterConnection_ErrorMessage_Failed_GetSearchRateLimit);
            } else {
                outBuffer.append(Messages.bind("( {0} / {1} )", rateLimit.getRemaining(), rateLimit.getLimit())); //$NON-NLS-1$
            }
            return outBuffer.toString();
        } catch (TwitterException e) {
            CoronaActivator
                    .log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, WebEntryActivator.PLUGIN_ID, e.getLocalizedMessage(), e),
                            false);
            throw e;
        }
    }


    /**
     * 2回目以降の接続を行うための保存されたアクセストークンを取得する。<br />
     * 例外は呼び出し元でキャッチする必要が有る
     * 
     * @return アクセストークン
     * @throws FileNotFoundException
     *             アクセストークンが存在しない。
     * @throws IOException
     *             IOエラー
     * @throws ClassNotFoundException
     *             シリアライズしたクラスに該当するファイルをロードできない
     */
    private static AccessToken loadAccessToken() throws FileNotFoundException, IOException, ClassNotFoundException {

        ObjectInputStream inputStream = null;
        if (getAccessTokenSavePath() == null) {
            throw new IOException();
        }
        String tokenFilePath = getAccessTokenSavePath() + TOKEN_FILE_NAME;

        try {
            inputStream = new ObjectInputStream(new FileInputStream(tokenFilePath));
            AccessToken token = (AccessToken) inputStream.readObject();
            return token;

        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }


    /**
     * Twitter検索クエリーを実行し結果を取得する
     * 
     * @param queryString
     *            クエリー
     * @param searchMax
     *            最大取得件数。
     * @param jpOnly
     *            trueを指定した場合日本語での投稿のみを取得
     * @param monitor
     *            進行状況を進めるIProgressMonitor
     * @return 取得した投稿
     */
    public List<Status> searchTwitter(String queryString, int searchMax, boolean jpOnly, IProgressMonitor monitor) {
        /*
         * TwitterException発生時は、取得したツイートを返すようにしたので throws TwitterException 節を削除
         */

        /* ページあたりのツイート取得件数 */
        final int TweetPerPage = 100;
        /* 検索リトライ回数 */
        final int MaxRetryCount = 3;

        /* キャンセルやエラーで中断した時に、進捗を進めるためのカウンタ */
        int workRest = searchMax / TweetPerPage;
        if ((searchMax % TweetPerPage) > 0) {
            workRest++;
        }
        /* +1は、最後の並べ替え処理分 */
        monitor.beginTask(Messages.TwitterFinishProgress_SearchExecTwitter, workRest + 1);

        Query query = new Query(queryString);
        query.setCount(TweetPerPage);
        if (jpOnly) {
            query.setLang("ja"); //$NON-NLS-1$
        }

        TreeSet<Status> getTweets = new TreeSet<Status>();
        LinkedList<Status> resultGetTweets = new LinkedList<Status>();

        int searchRetry = 0;
        while (getTweets.size() < searchMax) {
            QueryResult result = null;
            try {
                result = twitter.search(query);
                monitor.worked(1);
                workRest--;
                /* 正常に処理を終えたら、リトライカウントはクリア */
                searchRetry = 0;

                List<Status> tweets = result.getTweets();
                for (Status tweet : tweets) {
                    getTweets.add(tweet);
                }

                if (!result.hasNext()) {
                    monitor.worked(workRest);
                    break;
                }

                if (monitor.isCanceled()) {
                    monitor.worked(workRest);
                    return new LinkedList<Status>();
                }
                query = result.nextQuery();
            } catch (TwitterException e) {
                /*
                 * 検索クエリーの処理に失敗した場合はここに来る.
                 * エラーが続けて起きたら、処理を中断
                 */
                if ((searchRetry++ > MaxRetryCount) || (e.getErrorCode() == TwitterException.TOO_MANY_REQUESTS)) {
                    /*
                     * ・エラーで中断した場合は、そこまでの結果を返す
                     * ・ErrorCode 429 rate limit だったらリトライしない
                     */
                    e.printStackTrace();
                    CoronaActivator.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.WARNING, WebEntryActivator.PLUGIN_ID,
                            e.toString(), e), true);
                    monitor.worked(workRest);
                    break;
                }

                /* 少し待って検索を試みる */
                try {
                    Thread.sleep(500L * searchRetry);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                if (monitor.isCanceled()) {
                    monitor.worked(workRest);
                    return new LinkedList<Status>();
                }
            }
        }


        /*
         * 検索結果の件数切りつめと再ソート。
         * TreeSetによってツイートIDの昇順に自動ソートされている状態を逆順で(新しい物から)必要な分取得し
         * LinkedListの先頭に追加していく(古い物を手前にして降順にする)
         */
        Iterator<Status> it = getTweets.descendingIterator();
        while (it.hasNext()) {
            resultGetTweets.addFirst(it.next());
            if (resultGetTweets.size() >= searchMax) {
                break;
            }
        }
        monitor.worked(1);
        monitor.done();
        return resultGetTweets;
    }


    /**
     * アクセストークンの保存先パスを取得する
     * 
     * @return トークン保存先ディレクトリへのパス
     */
    private static String getAccessTokenSavePath() {
        /* ワークスペースの設定として保存する */
        String tokenStoreDirPath;
        if (ResourcesPlugin.getPlugin() == null) {
            tokenStoreDirPath = stateLocation + THIS_PLUGIN_CONFIG_DIR;
        } else {
            /* スタンドアロン（自動実行）用の対策 */
            tokenStoreDirPath = ResourcesPlugin.getPlugin().getStateLocation() + THIS_PLUGIN_CONFIG_DIR;
        }
        File directory = new File(tokenStoreDirPath);
        if (!(directory.exists())) {
            if (!(directory.mkdirs())) {
                return null;
            }
        }
        return tokenStoreDirPath;
    }


    /**
     * アクセストークンが保存されているかを取得する.
     * Twitter側の操作でCoronaのアクセス許可を解除する事が可能であり、
     * トークンが保存されている=接続可、ではない点に注意
     * 
     * @return 保存済みであればtrue
     */
    public boolean isSaveAccessToken() {
        /* Memo アクセストークンを、Coronaで複数持てるようにする（ユーザ管理する）のでなければ、staticでかまわない */
        if (getAccessTokenSavePath() == null) {
            return false;
        }
        String tokenFilePath = getAccessTokenSavePath() + TOKEN_FILE_NAME;
        File tokenFile = new File(tokenFilePath);
        return tokenFile.exists();
    }
}
