/**
 * @version $Id: FileUtil.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/09 19:01:11
 * @author imai-yoshikazu
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * ファイル操作ユーティリティクラス
 * 
 * @author imai-yoshikazu
 */
public class FileUtil {

    /**
     * ファイル名の配列からFileオブジェクトの配列を作成する
     * String[] -> File[]
     * 
     * @param filenames
     *            ファイル名の一覧
     * @param base
     *            ベースディレクトリ
     * @return filenamesのそれぞれにbaseをくっつけてFileにしたもの
     */
    public static File[] createFilesFromFileNames(String[] filenames, File base) {
        File[] files = new File[filenames.length];
        for (int i = 0; i < filenames.length; i++) {
            files[i] = new File(base, filenames[i]);
        }
        return files;
    }


    /**
     * 指定した拡張子に変える。
     * （ファイル名を変更するのではなく、新しい拡張子のファイルインスタンスを作成して返す）
     * 
     * @param file
     *            元のファイル
     * @param oldExtension
     *            変更される、古い拡張子
     * @param newExtension
     *            新しい拡張子
     * @return 新しい拡張子のファイル
     */
    public static File transPathExtension(File file, String oldExtension, String newExtension) {
        String str = file.getAbsolutePath();
        int pos = str.lastIndexOf("." + oldExtension); //$NON-NLS-1$
        if (pos + oldExtension.length() + 1 != str.length()) {
            System.err.println(file + " no ext:" + oldExtension); //$NON-NLS-1$
            return null;
        }
        String newStr = str.substring(0, pos) + "." + newExtension; //$NON-NLS-1$
        return new File(newStr);
    }


    /**
     * 指定した拡張子に変える
     * （ファイル名を変更するのではなく、拡張子を変更したファイルを新しく作成する）
     * 
     * @param file
     *            対象ファイル
     * @param newExtension
     *            新しい拡張子
     * @return 拡張子を変えたファイル
     */
    public static File transPathExtension(File file, String newExtension) {
        String oldExt = getExtension(file);
        return transPathExtension(file, oldExt, newExtension);
    }


    /**
     * 拡張子を指定してディレクトリ内のファイル一覧を取得
     * 
     * @param dir
     *            ファイルの一覧を取得するディレクトリ
     * @param extension
     *            取得する拡張子を指定する。nullならすべての拡張子に一致
     * @return 指定された拡張子を持つファイル一覧
     */
    public static File[] getExtensionSelectFiles(File dir, final String extension) {
        final FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File file) {
                String ext1 = getExtension(file);
                return extension == null || extension.equals(ext1);
            }
        };

        if (!dir.isDirectory()) {
            System.err.println(dir + " is not directory."); //$NON-NLS-1$
            return new File[0];
        }
        return dir.listFiles(filter);
    }


    /**
     * 条件を満たすファイルを抽出
     * 
     * @param files
     *            検査対象のファイル一覧
     * @param filter
     *            マッチ条件
     * @return 条件を満たすファイル一覧
     */
    public static File[] getFilterFiles(File[] files, FileFilter filter) {
        List<File> filteredFiles = new ArrayList<File>();
        for (File file : files) {
            if (filter.accept(file)) {
                filteredFiles.add(file);
            }
        }
        return filteredFiles.toArray(new File[filteredFiles.size()]);
    }


    /**
     * ファイルの拡張子を取得する
     * 
     * @param file
     *            対象のファイル
     * @return fileの拡張子
     */
    public static final String getExtension(File file) {
        String path = file.getPath();
        int pos = path.lastIndexOf("."); //$NON-NLS-1$
        if (pos != -1) {
            return path.substring(pos + 1);
        }
        return ""; //$NON-NLS-1$
    }


    /**
     * 2つのファイルの最終更新時間を比較して、更新が必要か判断する。
     * 
     * @param file1
     *            検査対象１
     * @param file2
     *            検査対象２
     * @return file1 の方が新しければ true
     */
    public static boolean hasUpdate(File file1, File file2) {
        if (file1.exists()) {
            if (file2.exists()) {
                long d1 = file1.lastModified();
                long d2 = file2.lastModified();
                return d1 > d2;
            } else {
                return true;
            }
        } else {
            // ソースがないので更新不要に判定するように
            return false;
        }
    }


    /**
     * ファイルをコピーする.
     * OutputStream, InputStreamのいずれもこのメソッドではクローズされない。
     * 
     * @param to
     *            出力先ストリーム
     * @param from
     *            コピー元ストリーム
     * @throws IOException
     *             IOエラー
     */
    public static void copy(OutputStream to, InputStream from) throws IOException {
        byte[] buf = new byte[1024 * 1024];
        int n;
        while ((n = from.read(buf)) > 0) {
            to.write(buf, 0, n);
        }
    }


    /**
     * ファイルをコピーする.
     * InputStreamは、このメソッドではクローズされない。
     * 
     * @param to
     *            出力先ファイル
     * @param from
     *            コピー元ストリーム
     * @throws IOException
     *             IOエラー
     */
    public static void copy(File to, InputStream from) throws IOException {
        OutputStream os = new FileOutputStream(to);
        try {
            copy(os, from);
        } finally {
            os.close();
        }
    }


    /**
     * ファイルをコピーする
     * 
     * @param to
     *            出力先ファイル
     * @param from
     *            コピー元ファイル
     * @throws IOException
     *             IOエラー
     */
    public static void copy(File to, File from) throws IOException {
        if (to.equals(from)) {
            return;
        }
        OutputStream os = null;
        InputStream is = null;
        try {
            os = new FileOutputStream(to);
            is = new FileInputStream(from);
            copy(os, is);
        } finally {
            if (os != null) {
                os.close();
            }
            if (is != null) {
                is.close();
            }
        }
    }


    /**
     * ファイルコピー（複数ファイル）
     * 
     * @param toDir
     *            出力先ディレクトリ
     * @param fromFiles
     *            コピー元ファイル群
     * @throws IOException
     *             IOエラー
     */
    public static void copy(File toDir, File[] fromFiles) throws IOException {
        for (File from : fromFiles) {
            File to = new File(toDir, from.getName());
            copy(to, from);
        }
    }


    /**
     * ハッシュ(MD5)算出
     * 
     * @param file
     *            ハッシュを取得するファイル
     * @return ファイルのMD5ハッシュ値
     * @throws IOException
     *             IOエラー
     */
    public static byte[] calcMD5(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        try {
            return calcMD5(is);
        } finally {
            is.close();
        }
    }


    /**
     * ハッシュ(MD5)算出
     * 
     * @param inputStream
     *            ハッシュを取得するストリーム
     * @return MD5ハッシュ値
     * @throws IOException
     *             IOエラー
     */
    public static byte[] calcMD5(InputStream inputStream) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
            DigestInputStream dis = new DigestInputStream(inputStream, md);

            byte[] buf = new byte[1024 * 1024];
            while (dis.read(buf) > 0) {
                //
            }
            byte[] digest = md.digest();
            return digest;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace(System.err);
            return new byte[0];
        }
    }


    /**
     * ファイルを結合する
     * 
     * @param target
     *            出力先ファイル
     * @param files
     *            結合元ファイルの一覧
     * @throws IOException
     *             IOエラー
     */
    public static void concatFiles(File target, File[] files) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(target));
        try {
            byte[] buf = new byte[1024 * 1024];
            for (File file : files) {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                try {
                    int n;
                    while ((n = bis.read(buf)) > 0) {
                        bos.write(buf, 0, n);
                    }
                } catch (IOException e) {
                    throw e;
                } finally {
                    bis.close();
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            bos.close();
        }
    }
}
