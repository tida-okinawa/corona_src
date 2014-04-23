/**
 * @version $Id: TwitterFinishProgress.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/09/27 17:11:03
 * @author yukihiro-kinjo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.webentry.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import twitter4j.Status;

import com.tida_okinawa.corona.correction.controller.ErratumController;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.webentry.Messages;
import com.tida_okinawa.corona.webentry.twitter.TweetConvert;
import com.tida_okinawa.corona.webentry.twitter.TwitterConnection;

class TwitterFinishProgress implements IRunnableWithProgress {

    private final String dataName;
    private final boolean useName;
    private final int searchMax;
    private final String productName;
    private final boolean japaneseOnly;
    private final boolean useDateTime;
    private final boolean useScreenName;
    private final String searchWord;
    private Shell shell;
    private IClaimData claimData;

    private final boolean useRetweet;
    private final boolean useFollow;
    private final boolean useFollower;
    private final boolean useProfile;
    private final boolean tcoExpand;
    private final boolean filterRetweet;
    private final boolean filterReply;


    public TwitterFinishProgress(String dataName, boolean useName, int searchMax, String productName, boolean japaneseOnly, boolean useDateTime,
            boolean useScreenName, String searchWord, Shell shell, boolean useRetweet, boolean useFollow, boolean useFollower, boolean useProfile,
            boolean tcoExpand, boolean filterRetweet, boolean filterReply) {
        this.dataName = dataName;
        this.useName = useName;
        this.searchMax = searchMax;
        this.productName = productName;
        this.japaneseOnly = japaneseOnly;
        this.useDateTime = useDateTime;
        this.useScreenName = useScreenName;
        this.searchWord = searchWord;
        this.shell = shell;
        this.claimData = null;

        this.useRetweet = useRetweet;
        this.useFollow = useFollow;
        this.useFollower = useFollower;
        this.useProfile = useProfile;
        this.tcoExpand = tcoExpand;
        this.filterRetweet = filterRetweet;
        this.filterReply = filterReply;
    }


    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

        /*
         * タスク数の算出
         * その他処理1：検索4の割合
         */
        final int taskSearch = 20;
        final int taskAny = 5;
        monitor.beginTask(null, taskAny + taskSearch);
        monitor.subTask(Messages.TwitterFinishProgress_ConnectTwitter);

        /* Twitterへ接続 */
        TwitterConnection twitter = new TwitterConnection();
        twitter.twitterSubsequentConnect();
        if (monitor.isCanceled()) {
            return;
        }
        monitor.worked(1);

        /* Twitterへ検索クエリーを実行 */
        monitor.subTask(Messages.TwitterFinishProgress_SearchExecTwitter);
        List<Status> getTweet = null;
        getTweet = twitter.searchTwitter(searchWord, searchMax, japaneseOnly, new SubProgressMonitor(monitor, taskSearch,
                SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
        if (monitor.isCanceled()) {
            return;
        }
        if (getTweet.size() < 1) {
            showErrorDialog(shell, Messages.TwitterFinishProgress_GetCountOfZero);
            return;
        }

        /* 取得した投稿をCoronaで読み込める形式に変換する */
        monitor.subTask(Messages.TwitterFinishProgress_TwitterDataConvert);
        TweetConvert converter = new TweetConvert(dataName, productName, useScreenName, useName, useDateTime, useRetweet, useFollow, useFollower, useProfile,
                tcoExpand);
        converter.deleteConvertTempFiles();
        String tempDirPath = TweetConvert.getTempStoreDirPath();
        if (tempDirPath == null) {
            showErrorDialog(shell, Messages.TwitterFinishProgress_DataImportFailed);
            return;
        }
        String tempCsvFilePath = tempDirPath + dataName + ".csv"; //$NON-NLS-1$
        String tempSqlFilePath = tempDirPath + dataName + ".sql"; //$NON-NLS-1$

        if (filterRetweet) {
            converter.filterRT(getTweet);
        }
        if (filterReply) {
            converter.filterReply(getTweet);
        }

        if (!(converter.convertTweetToCsvFile(getTweet, tempCsvFilePath))) {
            showErrorDialog(shell, Messages.TwitterFinishProgress_DataImportFailed);
            return;
        }
        monitor.worked(1);
        if (!(converter.createDefineSqlFile(tempSqlFilePath))) {
            showErrorDialog(shell, Messages.TwitterFinishProgress_DataImportFailed);
            return;
        }
        if (monitor.isCanceled()) {
            return;
        }
        monitor.worked(1);

        /* データベースへインポート */
        monitor.subTask(Messages.TwitterFinishProgress_DataBaseImport);
        try {
            tempCsvFilePath = tempCsvFilePath.replace("/", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
            claimData = IoActivator.getService().importClaimData(tempCsvFilePath, tempSqlFilePath, dataName, false);
        } catch (SQLException | IOException e) {
            showErrorDialog(shell, Messages.TwitterFinishProgress_DataImportFailed);
            return;
        }
        if (claimData == null) {
            showErrorDialog(shell, Messages.TwitterFinishProgress_DataImportFailed);
            return;
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
        claimData.setProductField(productFieldNum);
        /* 誤記補正する前に誤記補正フィールドをcommitしてしまうと、誤記補正結果が正しくDBに書き込まれないため、コメントアウト */
        //claimData.addCorrectionMistakesField(bodyFieldNum);
        claimData.commit();

        /* 誤記補正処理の実行 */
        List<IFieldHeader> targetFields = new ArrayList<IFieldHeader>(1);
        targetFields.add(claimData.getFieldInformation(bodyFieldNum));
        ErratumController controller = new ErratumController(claimData, targetFields, null);
        controller.run(null); // 既に別スレッドで起動した中なのでIRunnableWithProgressとしては起動しない
        monitor.worked(1);
        monitor.done();
    }


    public static void showErrorDialog(final Shell shell, final String message) {
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                MessageDialog.openError(shell, Messages.TwitterFinishProgress_Error, message);
            }
        });
    }


    public IClaimData getClaimData() {
        return claimData;
    }

}