/**
 * @version $Id: TweetConvert.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/09/19 19:53:59
 * @author yukihiro-kinjo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.webentry.twitter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.ResourcesPlugin;

import twitter4j.Status;
import twitter4j.URLEntity;

import com.tida_okinawa.corona.common.Encoding;

/**
 * Twitter投稿をCoronaで読み込める形式に変換するクラス
 * 
 * @author yukihiro-kinjo
 * 
 */
public class TweetConvert {

    private static final String THIS_PLUGIN_TEMP_DIR = "/.webEntry/temp/"; //$NON-NLS-1$
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"; //$NON-NLS-1$

    private String dataName;
    private String productName;
    private boolean screanNameVisible;
    private boolean nameVisible;
    private boolean dateVisible;

    private boolean useRetweet;
    private boolean tcoExpand;
    private boolean followVisible;
    private boolean followerVisible;
    private boolean profileVisible;


    /**
     * コンストラクター
     * 
     * @param dataName
     *            問い合わせデータ名
     * @param productName
     *            ターゲット名
     * @param screanNameVisible
     *            ユーザー表示名を取得する場合true
     * @param nameVisible
     *            ユーザーIDを取得する場合true
     * @param dateVisible
     *            投稿日時を取得する場合true
     * @param useRetweet
     *            リツイート数を取得する場合はtrue
     * @param followVisible
     *            ユーザーのフォロー数を取得する場合はtrue
     * @param followerVisible
     *            ユーザーのフォロワー数を取得する場合はtrue
     * @param profileVisible
     *            ユーザーのプロフィールを取得する場合はtrue
     * @param tcoExpand
     *            t.co短縮URLを展開する場合はtrue
     */
    public TweetConvert(String dataName, String productName, boolean screanNameVisible, boolean nameVisible, boolean dateVisible, boolean useRetweet,
            boolean followVisible, boolean followerVisible, boolean profileVisible, boolean tcoExpand) {
        super();
        this.dataName = dataName;
        this.productName = convertStringToCsvSafe(productName);
        this.screanNameVisible = screanNameVisible;
        this.nameVisible = nameVisible;
        this.dateVisible = dateVisible;
        this.useRetweet = useRetweet;
        this.followVisible = followVisible;
        this.followerVisible = followerVisible;
        this.profileVisible = profileVisible;
        this.tcoExpand = tcoExpand;
    }


    /**
     * 文字列をCSVに取り込める状態に変換する
     * 
     * @param source
     *            変換する文字列
     * @return 変換済み文字列
     */
    private static String convertStringToCsvSafe(String source) {

        if ((source == null) || source.isEmpty()) {
            return ""; //$NON-NLS-1$
        }

        String converted = source;
        /* 半角カンマとダブルクォーテーションと円記号を安全な文字に置き換える */
        converted = converted.replace(',', '，').replace('\"', '”').replace('\\', '￥');
        if (converted.contains("\n")) { //$NON-NLS-1$
            return new StringBuilder(converted.length() + 2).append('"').append(converted).append('"').toString();
        }
        return converted;
    }


    /**
     * ツイート群からリツイートを除去します
     * 
     * @param tweets
     *            Twitter投稿
     */
    public void filterRT(List<Status> tweets) {
        Iterator<Status> it = tweets.iterator();
        while (it.hasNext()) {
            Status item = it.next();
            if (item.isRetweet()) {
                it.remove();
            }
        }
    }


    /**
     * ツイート群からリプライを除去します
     * 
     * @param tweets
     *            Twitter投稿
     */
    public void filterReply(List<Status> tweets) {
        Iterator<Status> it = tweets.iterator();
        while (it.hasNext()) {
            Status item = it.next();
            long userid = item.getInReplyToUserId();
            if (userid > 1) {
                it.remove();
            }
        }
    }


    /**
     * Twitter投稿をCSVファイルに変換し出力する
     * 
     * @param tweets
     *            Twitter投稿
     * @param path
     *            保存ファイルパス
     * @return 何らかの問題が発生した場合にfalseを返却
     */
    public boolean convertTweetToCsvFile(List<Status> tweets, String path) {

        File tempCsv = new File(path);
        BufferedWriter writer = null;
        try {
            if (tempCsv.exists()) {
                return false;
            }
            if (!(tempCsv.createNewFile())) {
                return false;
            }
            if (!(tempCsv.canWrite())) {
                return false;
            }
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempCsv), Encoding.UTF_8.toString()));
            /* Tweet 140 + any */
            StringBuilder outputBuffer = new StringBuilder(256);
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

            for (Status item : tweets) {
                /* Memo getUser()がnullを返却する場合、Exceptionとなるのでnullチェック */
                if (item.getUser() != null) {
                    if (screanNameVisible) {
                        /* Memo getNameに表示名が入っている。APIのバグか? */
                        outputBuffer.append(convertStringToCsvSafe(item.getUser().getName())).append(',');
                    }
                    if (nameVisible) {
                        /* Memo getScreenNameにユーザ名が入っている。APIのバグか? */
                        outputBuffer.append(convertStringToCsvSafe(item.getUser().getScreenName())).append(',');
                    }
                } else {
                    outputBuffer.append(",,");
                }
                String tempBody = item.getText();
                if (tcoExpand) {
                    tempBody = tcoExpand(tempBody, item);
                }
                outputBuffer.append(convertStringToCsvSafe(tempBody)).append(',');
                if (dateVisible) {
                    outputBuffer.append(dateFormat.format(item.getCreatedAt())).append(',');
                }
                if (useRetweet) {
                    outputBuffer.append(item.getRetweetCount()).append(',');
                }
                if (followerVisible) {
                    outputBuffer.append(item.getUser().getFollowersCount()).append(',');
                }
                if (followVisible) {
                    outputBuffer.append(item.getUser().getFriendsCount()).append(',');
                }
                if (profileVisible) {
                    outputBuffer.append(convertStringToCsvSafe(item.getUser().getDescription())).append(',');
                }
                outputBuffer.append(productName);
                writer.write(outputBuffer.toString());
                writer.write('\n');
                outputBuffer.setLength(0);
            }

        } catch (IOException e) {
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    return false;
                }
            }
        }
        return true;
    }


    private static String tcoExpand(String body, Status status) {
        URLEntity[] entities = status.getURLEntities();//entity取得
        for (URLEntity entity : entities) {

            String ex_url = entity.getExpandedURL();//展開後のURL
            String tco = entity.getURL();//t.co

            Pattern p = Pattern.compile(tco);
            Matcher m = p.matcher(body);
            body = m.replaceAll(ex_url);//置換
        }
        return body;
    }


    /**
     * Twitter投稿をデーターベースに取り込むための定義SQLを作成する
     * 
     * @param path
     *            保存ファイルパス
     * @return 何らかの問題が発生した場合にfalseを返却
     */
    public boolean createDefineSqlFile(String path) {

        File tempSql = new File(path);
        BufferedWriter writer = null;
        try {
            if (tempSql.exists()) {
                return false;
            }
            if (!(tempSql.createNewFile())) {
                return false;
            }
            if (!(tempSql.canWrite())) {
                return false;
            }
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempSql), Encoding.UTF_8.toString()));
            StringBuilder outputBuffer = new StringBuilder(250);
            outputBuffer.append("CREATE TABLE "); //$NON-NLS-1$
            outputBuffer.append(dataName);
            outputBuffer.append("\n(\n\tID INT NOT NULL AUTO_INCREMENT,\n"); //$NON-NLS-1$
            if (screanNameVisible) {
                outputBuffer.append("\tUSERNAME VARCHAR(64),\n"); //$NON-NLS-1$
            }
            if (nameVisible) {
                outputBuffer.append("\tUSER VARCHAR(64),\n"); //$NON-NLS-1$
            }
            if (tcoExpand) {
                outputBuffer.append("\tBODY TEXT,\n"); //$NON-NLS-1$
            } else {
                outputBuffer.append("\tBODY VARCHAR(256),\n"); //$NON-NLS-1$
            }
            if (dateVisible) {
                outputBuffer.append("\tDATETIME DATETIME,\n"); //$NON-NLS-1$
            }
            if (useRetweet) {
                outputBuffer.append("\tRETWEET MEDIUMINT,\n");
            }
            if (followerVisible) {
                outputBuffer.append("\tFOLLOW MEDIUMINT,\n");
            }
            if (followVisible) {
                outputBuffer.append("\tFOLLOWER MEDIUMINT,\n");
            }
            if (profileVisible) {
                outputBuffer.append("\tPROFILE TEXT,\n");
            }
            outputBuffer.append("\tTARGET VARCHAR(128),\n\tPRIMARY KEY (ID)\n);"); //$NON-NLS-1$
            writer.write(outputBuffer.toString());

        } catch (IOException e) {
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * 変換テンポラリディレクトリ内のファイルを削除する。<br />
     * 削除に失敗したファイルは、Java仮想マシン終了時に削除されるようスケジュールされる。
     */
    public void deleteConvertTempFiles() {
        /* Memo 複数保存できるようにするのでなければ、staticでかまわない */
        String tempDirPath = getTempStoreDirPath();
        if (tempDirPath == null) {
            return;
        }
        File tempDir = new File(tempDirPath);
        if (tempDir.exists()) {
            File[] files = tempDir.listFiles();
            for (File item : files) {
                if (item.exists() && item.isFile()) {
                    if (!(item.delete())) {
                        item.deleteOnExit();
                    }
                }
            }
        }
    }

    private static String stateLocation;


    /**
     * 自動実行用
     * 通常、<code>ResourcesPlugin.getPlugin().getStateLocation()</code>
     * で取れるパスを渡し、スタンドアロン起動時でもTwitterAccessTokenやテンポラリファイルの保存位置を取得できるようにする
     * 
     * @param path
     *            アクセストークンの格納場所
     */
    public static void setStateLocation(String path) {
        stateLocation = path;
    }


    /**
     * テンポラリファイルを保存するディレクトリへのパスを取得する
     * 
     * @return テンポラリファイルを保存するパスを示す文字列
     */
    public static String getTempStoreDirPath() {

        String tempStoreDirPath;
        if (ResourcesPlugin.getPlugin() != null) {
            tempStoreDirPath = ResourcesPlugin.getPlugin().getStateLocation() + THIS_PLUGIN_TEMP_DIR;
        } else {
            /* スタンドアロン（自動実行）用の対策 */
            tempStoreDirPath = stateLocation + THIS_PLUGIN_TEMP_DIR;
        }
        File directory = new File(tempStoreDirPath);
        if (!(directory.exists())) {
            if (!(directory.mkdirs())) {
                return null;
            }
        }
        return tempStoreDirPath;
    }
}
