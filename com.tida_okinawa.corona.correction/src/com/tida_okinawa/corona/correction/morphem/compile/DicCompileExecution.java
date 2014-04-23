/**
 * @version $Id: DicCompileExecution.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/30 11:48:56
 * @author sanenori-makiya
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.morphem.compile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.tida_okinawa.corona.common.DefaultLogger;
import com.tida_okinawa.corona.common.Encoding;
import com.tida_okinawa.corona.common.ILogger;
import com.tida_okinawa.corona.correction.common.ExternalProgramExec;
import com.tida_okinawa.corona.correction.common.ExternalProgramExitException;
import com.tida_okinawa.corona.correction.common.FileUtil;
import com.tida_okinawa.corona.correction.morphem.preference.MorphemePreference;

/**
 * 形態素解析に使う辞書ファイルのコンパイルを実行するクラス
 * 
 * @author sanenori-makiya, imai
 */
public class DicCompileExecution {

    final String DICPATH = MorphemePreference.getJumanDicDir().getAbsolutePath();
    final String MAKEMAT = DICPATH + File.separator + "makemat"; //$NON-NLS-1$
    final String MAKEINT = DICPATH + File.separator + "makeint"; //$NON-NLS-1$
    final String MAKEPAT = DICPATH + File.separator + "makepat"; //$NON-NLS-1$
    final String DICSORT = DICPATH + File.separator + "dicsort"; //$NON-NLS-1$

    /**
     * コンソール
     */
    final ILogger logger;

    /**
     * プログレスモニタ
     */
    final IProgressMonitor monitor;

    /**
     * Jumanが使う辞書ファイルのリスト
     */
    final static String[] jumanDicFilenames = { "jumandic.dat", "jumandic.mat", "jumandic.pat", "jumandic.tab", }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    /**
     * JUMANの標準辞書
     */
    final static String[] defaultDics = { "Assert.dic", "AuxV.dic", "ContentW.dic", "Demonstrative.dic", "Emoticon.dic", "Noun.hukusi.dic", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
            "Noun.keishiki.dic", "Noun.koyuu.dic", "Noun.suusi.dic", "Postp.dic", "Prefix.dic", "Rengo.dic", "Special.dic", "Suffix.dic", }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$


    /**
     * コンストラクター
     * 
     * @param logger
     *            コマンドの出力先 null:標準出力
     * @param monitor
     *            プログレスモニタ null： モニタなし
     */
    public DicCompileExecution(ILogger logger, IProgressMonitor monitor) {
        if (logger == null) {
            logger = new DefaultLogger();
        }
        this.logger = logger;

        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        this.monitor = monitor;
    }


    /**
     * Jumanが使う辞書ファイルのリストを取得
     * 
     * @return 辞書ファイル一覧
     */
    public File[] getJumanDicFiles() {
        File[] juman_dic_files = new File[jumanDicFilenames.length];
        File dicDir = MorphemePreference.getJumanDicDir();
        for (int i = 0; i < jumanDicFilenames.length; i++) {
            juman_dic_files[i] = new File(dicDir, jumanDicFilenames[i]);
            assert (juman_dic_files[i].exists());
        }
        return juman_dic_files;
    }


    /**
     * juman/dic/makedic.bat の内容を実行する
     * batファイルを実行しない理由
     * - 冗長な処理をしない（同じファイルを作成しなおさない）
     * - 並列実行
     * 
     * @param dics
     *            dicファイルのリスト
     * @param force
     *            dicファイルの更新がなくても jumandic.patを作る
     * @return 辞書ファイルを作成したか
     * @throws IOException
     *             プロセスの入出力エラー
     * @throws InterruptedException
     *             プロセスの入出力同期中の割り込み
     * @throws ExternalProgramExitException
     *             異常終了
     */
    public boolean compile(File[] dics, boolean force) throws IOException, InterruptedException, ExternalProgramExitException {
        monitor.beginTask("compile JUMAN dictionary.", 5); //$NON-NLS-1$

        ExternalProgramExec epe = new ExternalProgramExec();

        File base = MorphemePreference.getJumanDicDir();

        // 標準辞書をマージの対象にいれる
        List<File> dicList = new ArrayList<File>();
        for (String default_dic : defaultDics) {
            dicList.add(new File(base, default_dic));
        }
        for (File dic : dics) {
            dicList.add(dic);
        }
        dics = dicList.toArray(dics);

        // いらないIntファイルの削除
        boolean deleteFlg = deleteIntFile(dics);

        // 更新の有無をチェック
        // Memo static内部クラスにできるかもしれない無名内部クラス。
        File[] update_dics = FileUtil.getFilterFiles(dics, new FileFilter() {
            @Override
            public boolean accept(File dicFile) {
                File intFile = FileUtil.transPathExtension(dicFile, "int"); //$NON-NLS-1$
                return FileUtil.hasUpdate(dicFile, intFile);
            }
        });

        if (update_dics.length == 0 && !deleteFlg) {
            if (!force) {
                logger.getOutStream().println(Messages.DicCompileExecution_logNoJumanUpdate);
                return false;
            }
        }

        /*
         * make mat
         */
        monitor.subTask("makemat"); //$NON-NLS-1$
        epe.exec(new String[] { MAKEMAT }, base, null, logger.getOutStream(), logger.getErrStream());
        monitor.worked(1);

        /*
         * for %%f in (*.dic) do makeint %%f
         */
        exec(update_dics, MAKEINT);
        monitor.worked(1);

        /*
         * copy /b *.int jumandic.txt
         */
        File[] int_files = new File[dics.length];
        for (int i = 0; i < dics.length; i++) {
            int_files[i] = FileUtil.transPathExtension(dics[i], "int"); //$NON-NLS-1$
        }
        // note: 他のターゲット・プロジェクトのintファイルもある
        monitor.subTask("concat"); //$NON-NLS-1$
        File jumandic_txt = new File(base, "jumandic.txt"); //$NON-NLS-1$
        FileUtil.concatFiles(jumandic_txt, int_files);
        monitor.worked(1);

        /*
         * dic sort jumandic.txt > jumandic.dat
         */
        monitor.subTask("dicsort"); //$NON-NLS-1$
        File jumandic_dat = new File(base, "jumandic.dat"); //$NON-NLS-1$
        FileOutputStream jumandic_dat_out = new FileOutputStream(jumandic_dat);
        try {
            epe.exec(new String[] { DICSORT, jumandic_txt.getAbsolutePath() }, base, null, jumandic_dat_out, logger.getErrStream());
        } catch (IOException e) {
            throw e;
        } finally {
            jumandic_dat_out.close();
        }
        monitor.worked(1);

        /*
         * make pat
         */
        monitor.subTask("makepat"); //$NON-NLS-1$
        epe.exec(new String[] { MAKEPAT }, base, null, logger.getOutStream(), logger.getErrStream());
        monitor.worked(1);

        monitor.done();

        logger.getOutStream().println(Messages.DicCompileExecution_logJumanUpdate);
        return true;
    }


    private static boolean deleteIntFile(File[] dics) {

        boolean delete = false;
        File dir = MorphemePreference.getJumanDicDir();
        /* 全intファイル */
        File[] intFiles = FileUtil.getExtensionSelectFiles(dir, "int"); //$NON-NLS-1$

        List<File> intList = new ArrayList<File>();
        List<File> dicList = new ArrayList<File>();
        /* リストに移す */
        for (File f : intFiles) {
            intList.add(f);

        }
        for (File f : dics) {
            dicList.add(f);
        }

        /* 同じものを省く */
        for (Iterator<File> ietr = intList.iterator(); ietr.hasNext();) {
            File intFile = ietr.next();
            for (File dicFile : dicList) {
                /* intファイル名取得 */
                File f = FileUtil.transPathExtension(dicFile, "int"); //$NON-NLS-1$
                if (intFile.equals(f)) {
                    ietr.remove();
                }
            }
        }
        /* 残ったファイルを削除 */
        for (File f : intList) {
            if (f.delete()) {
                delete = true;
            }
        }
        return delete;
    }

    /**
     * コマンド失敗の有無
     */
    boolean isFailed = false;


    /**
     * ファイルを処理するコマンドを実行
     * コマンド [引数 ...] ファイル
     * 
     * @param files
     * @param cmd
     *            コマンド
     * @throws ExternalProgramExitException
     * @throws InterruptedException
     */
    private void exec(File[] files, String... cmd) throws ExternalProgramExitException, InterruptedException {
        int n_thr = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(n_thr);
        final File dicDir = MorphemePreference.getJumanDicDir();

        for (final File file : files) {
            final String[] args = new String[cmd.length + 1];
            System.arraycopy(cmd, 0, args, 0, cmd.length);
            args[cmd.length] = file.getAbsolutePath();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    ByteArrayOutputStream err = new ByteArrayOutputStream();
                    ExternalProgramExec epe = new ExternalProgramExec();
                    try {
                        monitor.subTask("" + file); //$NON-NLS-1$
                        epe.exec(args, dicDir, null, out, err);

                    } catch (ExternalProgramExitException e) {
                        isFailed = true;
                        logger.getErrStream().println(args[0] + " " + file); //$NON-NLS-1$
                        logger.getErrStream().println(Messages.DicCompileExecution_logEndCode + e.process.exitValue());
                    } catch (Exception e) {
                        logger.getErrStream().println(e + ":" + Arrays.toString(args)); //$NON-NLS-1$
                        e.printStackTrace(logger.getErrStream());
                    }
                    sjisConvertWrite(logger.getOutStream(), new ByteArrayInputStream(out.toByteArray()));
                    sjisConvertWrite(logger.getErrStream(), new ByteArrayInputStream(err.toByteArray()));
                }
            });
        }

        /* 終了を待つ */
        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(1);
            if (monitor.isCanceled()) {
                // 直ちに終了
                // 実行中のタスクは完了されるので、後始末は考慮しない
                executor.shutdownNow();
                throw new InterruptedException();
            }
        }

        if (isFailed) {
            throw new ExternalProgramExitException(cmd, null);
        }
    }


    /**
     * コマンドの出力(Shift_JIS(※MS932)) を変換してコンソールへ出力
     * 
     * @param out
     * @param in
     */
    private static void sjisConvertWrite(PrintStream out, InputStream in) {
        InputStreamReader isr = null;
        try {
            if (MorphemePreference.convSJIS()) {
                isr = new InputStreamReader(in, Encoding.MS932.toString());
            } else {
                isr = new InputStreamReader(in);
            }
        } catch (UnsupportedEncodingException e) {
            isr = new InputStreamReader(in);
        }
        try {
            OutputStreamWriter osw = new OutputStreamWriter(out);
            char[] buf = new char[1024];
            int n;
            while ((n = isr.read(buf)) > 0) {
                String msg = new String(buf, 0, n);
                osw.write(msg);
            }
            osw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
