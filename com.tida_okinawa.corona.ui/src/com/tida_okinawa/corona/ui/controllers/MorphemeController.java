/**
 * @version $Id: MorphemeController.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 21:30:11
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.controllers;

import static com.tida_okinawa.corona.common.CleansingNameVariable.MORPH_DEPEND;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.common.Encoding;
import com.tida_okinawa.corona.common.ILogger;
import com.tida_okinawa.corona.correction.controller.IListener;
import com.tida_okinawa.corona.correction.morphem.Morpheme;
import com.tida_okinawa.corona.correction.morphem.MorphemeRelationProcessor;
import com.tida_okinawa.corona.correction.morphem.preference.MorphemePreference;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.ui.UIActivator;
import com.tida_okinawa.corona.ui.data.ErratumCorrectionRecord;
import com.tida_okinawa.corona.ui.data.MorphemeRecord;

/**
 * UI->形態素・係り受け解析処理
 * 
 * @author imai
 * 
 */
public class MorphemeController extends ClaimWorkDataController<ErratumCorrectionRecord, MorphemeRecord> {
    /**
     * KNPを実行するかどうか
     */
    final boolean doKnp;

    private String bundleLocation;
    private boolean convSJIS;


    /**
     * ターゲットに紐づいている問合せデータ（誤記補正済み）に対して、形態素・係り受け解析を実行するインスタンスのコンストラクタ。
     * 
     * @param product
     *            形態素・係り受け解析に使用する辞書の最終更新日
     * @param doKnp
     *            KNPを実行するかどうか
     * @param listener
     *            処理結果を受け取るリスナー (EditorInput)
     */
    public MorphemeController(ICoronaProduct product, boolean doKnp, IListener<MorphemeRecord> listener) {
        super(MORPH_DEPEND, product, ClaimWorkDataType.CORRECTION_MISTAKES, ClaimWorkDataType.DEPENDENCY_STRUCTURE, listener);
        this.doKnp = doKnp;
    }


    /**
     * @param product
     *            解析対象のターゲット
     * @param workS
     *            入力データ
     * @param doKnp
     *            KNPを実行するかどうか
     * @param listener
     *            処理結果を受け取るリスナー (EditorInput)
     */
    public MorphemeController(ICoronaProduct product, IClaimWorkData workS, boolean doKnp, IListener<MorphemeRecord> listener) {
        super(MORPH_DEPEND, product, workS, ClaimWorkDataType.DEPENDENCY_STRUCTURE, listener);
        this.doKnp = doKnp;
    }


    /**
     * @param product
     *            解析対象のターゲット
     * @param inputWorks
     *            入力データ
     * @param doKnp
     *            KNPを実行するかどうか
     * @param listener
     *            処理結果を受け取るリスナー (EditorInput)
     */
    public MorphemeController(ICoronaProduct product, List<IClaimWorkData> inputWorks, boolean doKnp, IListener<MorphemeRecord> listener) {
        super(MORPH_DEPEND, product, inputWorks, ClaimWorkDataType.DEPENDENCY_STRUCTURE, listener);
        this.doKnp = doKnp;
    }


    /**
     * プラグイン以外から呼び出す場合のコンストラクタ。juman, knpの場所を特定するために、bundleLocationが必要
     * 
     * @param product
     *            形態素・係り受け解析に使用する辞書の最終更新日
     * @param doKnp
     *            KNPを実行するかどうか
     * @param bundleLocation
     *            correction.externalプラグインがバンドルされているパス。
     * @param convSJIS
     * @param listener
     *            処理結果を受け取るリスナー (EditorInput)
     */
    public MorphemeController(ICoronaProduct product, boolean doKnp, String bundleLocation, boolean convSJIS, IListener<MorphemeRecord> listener) {
        this(product, doKnp, listener);
        this.bundleLocation = bundleLocation;
        this.convSJIS = convSJIS;
    }

    /**
     * juman / knp の実行用
     * <p>
     * サーバー利用時は、スレッドごとに引数が違うので、スレッド数分を用意する
     * </p>
     */
    MorphemeRelationProcessor[] processors;


    @Override
    protected Controller createController(String title) {
        Controller controller;

        // スタンドアロン: プリファレンスのスレッド数, サーバー利用: プリファレンスのサーバー設定
        int nThread = MorphemePreference.getKnpServerNumber();
        if (nThread <= 0) {
            // スタンドアローン
            // プリファレンスのスレッド数に従う
            controller = new Controller(title, Runtime.getRuntime().availableProcessors());
        } else {
            controller = new Controller(title, nThread);
        }

        // Juman, KNP 実行用
        int n = controller.getThreadNum(); // スレッド数(=サーバーのプロセス数の合計)
        processors = new MorphemeRelationProcessor[n];
        if (bundleLocation != null) {
            for (int threadId = 0; threadId < n; threadId++) {
                processors[threadId] = new MorphemeRelationProcessor(threadId, bundleLocation);
            }
        } else {
            for (int threadId = 0; threadId < n; threadId++) {
                processors[threadId] = new MorphemeRelationProcessor(threadId);
            }
        }

        return controller;
    }


    @Override
    ErratumCorrectionRecord createRecordImpl(int claimId, int fieldId, int recordId, String text) {
        return new ErratumCorrectionRecord(claimId, fieldId, recordId, text);
    }


    @Override
    IListener<MorphemeRecord> createCommitter() {
        return new ClaimWorkDataRecordCommitter<MorphemeRecord>(product, typeR) {
            @Override
            public void end(IProgressMonitor monitor) {
                StringBuffer note = new StringBuffer(claimWorkData.getNote());
                if (note.length() > 0) {
                    note.append(","); //$NON-NLS-1$
                }

                ClaimWorkDataType type = claimWorkData.getClaimWorkDataType();
                note.append(type.getName());
                note.append(":").append(doKnp); //$NON-NLS-1$

                /* DBでの文字数制限 */
                if (note.length() > 255) {
                    note.delete(0, note.length() - 255);
                }
                claimWorkData.setNote(note.toString());

                claimWorkData.commit(monitor);
            }
        };
    }


    @Override
    MorphemeRecord execImpl(ErratumCorrectionRecord record) {

        try {
            /* 抽出・補正に、解析処理を要求 */
            final Morpheme m = new Morpheme(processors[controller.getThreadId()]);

            String text = record.getResult();
            if (text == null) {
                System.err.println(Messages.MorphemeController_errNullRecord + record.getRecordId());
                text = ""; //$NON-NLS-1$
            }
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            List<String> morphemeResult;
            if (bundleLocation != null) {
                morphemeResult = m.process(text, doKnp, err, convSJIS, Messages.MorphemeController_PERIOD, Messages.MorphemeController_QUESTION); /* 句点で区切って処理する */
            } else {
                morphemeResult = m.process(text, doKnp, err, Messages.MorphemeController_PERIOD, Messages.MorphemeController_QUESTION); /* 句点で区切って処理する */
            }
            err(record, err);

            MorphemeRecord resultRecord = new MorphemeRecord(record.getClaimId(), record.getFieldId(), record.getRecordId(), text, morphemeResult);
            return resultRecord;
        } catch (RuntimeException e) {
            e.printStackTrace();
            if (CoronaActivator.getDefault() != null) {
                CoronaActivator.getDefault().getLogger().getErrStream().println(e.getLocalizedMessage());
            }
            // return dummy result
            return new MorphemeRecord(record.getClaimId(), record.getFieldId(), record.getRecordId(), record.getResult(), new ArrayList<String>(0));
        }
    }


    /**
     * KNPのエラー原文を日本語にして返す.
     * エラー扱いしないものは、nullを返す.
     * 
     * @param errmsg
     *            エラー原文
     * @return 日本語訳したエラー文
     */
    private static String getDetailErrorMessage(String errmsg) {
        /* その他の理由がわかったら随時追加する */
        if (errmsg.contains("Too many mrph")) { //$NON-NLS-1$
            return (Messages.MorphemeController_tooManyMrph);
        } else if (errmsg.contains("bunsetsu")) { //$NON-NLS-1$
            return (Messages.MorphemeController_tooManyBunsetsu);
        } else if (errmsg.contains("older than rule file")) { //$NON-NLS-1$
            /* 解析はできているので */
            return null;
        } else if (errmsg.contains("init_case_frame")) { //$NON-NLS-1$
            /* Memo 詳しい理由を出したい */
        } else if (errmsg.contains("new_cky_data")) { //$NON-NLS-1$
            /* Memo 詳しい理由を出したい */
        } else if (errmsg.contains("pathopen") | errmsg.contains("tie")) { //$NON-NLS-1$ //$NON-NLS-2$
            /* KNP4にした途端色々出始めたのでスルーする　#1145 */
            return null;
        }
        return ""; //$NON-NLS-1$
    }


    /**
     * コンソールとLogViewにエラー出力する
     * 
     * @param record
     * @param err
     */
    private static void err(ErratumCorrectionRecord record, ByteArrayOutputStream err) {
        if (CoronaActivator.getDefault() == null) {
            return;
        }

        ILogger logger = CoronaActivator.getDefault().getLogger();
        synchronized (logger) {
            if (err.size() == 0) {
                return;
            }

            /*
             * 解析に失敗した原文...(###文字)
             * _ 解析失敗。[詳細なメッセージ(メッセージ原文)], ...
             * _ Claim:### Field:### Rec:###
             */
            /* 解析に失敗した原文 */
            StringBuffer textBuf = new StringBuffer(64).append(Messages.MorphemeController_body);
            String text = record.getResult();
            if (text.length() < 25) {
                textBuf.append(text);
            } else {
                textBuf.append(text.substring(0, 25)).append(Messages.MorphemeController_ellipsis).append(text.length())
                        .append(Messages.MorphemeController_stringCount);
            }

            StringBuffer errBuf = new StringBuffer(256);
            // 詳細なメッセージ(メッセージ原文)
            try {
                // ShiftJIS -> UTF-8 にして出力
                InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(err.toByteArray()), Encoding.MS932.toString());
                BufferedReader reader = new BufferedReader(isr);
                /* エラーメッセージ作成 */
                String errmsg;
                while ((errmsg = reader.readLine()) != null) {
                    String detail = getDetailErrorMessage(errmsg);
                    if (detail != null) {
                        errBuf.append("[").append(detail).append("(").append(errmsg).append(")] "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (errBuf.length() == 0) {
                return;
            }
            errBuf.append("\n\tClaim:").append(record.getClaimId()).append(" Field:").append(record.getFieldId()).append(" Rec:").append(record.getRecordId()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            /* コンソールにエラーメッセージを出力 */
            logger.getOutStream().println(textBuf.toString());
            logger.getErrStream().println(errBuf.toString());
            /* 解析失敗の情報をLogViewに書きだす */
            errBuf.insert(0, textBuf.toString());
            CoronaActivator.log(new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, errBuf.toString()), false);

            /* エラーログビューを表示する */
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    try {
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$
                    } catch (PartInitException e) {
                    }
                }
            });
        }
    }
}
