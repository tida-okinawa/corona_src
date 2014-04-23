/**
 * @version $Id: StringUtil.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/07 16:05:59
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.util;

import java.io.File;
import java.io.IOException;


/**
 * @author kousuke-morishima
 */
public class StringUtil {

    public static String removeExtension(String fileName) {
        isValidFileName(fileName);

        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            return fileName.substring(0, index);
        }
        return fileName;
    }


    public static String getExtension(String fileName) {
        isValidFileName(fileName);

        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            return fileName.substring(index + 1);
        }
        return "";
    }

    private static final char[] ILLEGAL_CHARACTERS = { '`', '?', '*', '\\', '<', '>', '|', '\"', ':', '/', '\n', '\r', '\t', '\0', '\f' };


    /**
     * デバイス名などの予約語、ディレクトリ名など
     * ファイルとして保存できないパスが指定されてる場合は例外を投げる
     * 
     * TODO 総パス長が OS の制限にかかる場合は何も対策していない。
     * 
     * @param fileName
     * @return
     */
    public static boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.length() == 0) {
            return false;
        }

        for (char c : ILLEGAL_CHARACTERS) {
            if (fileName.indexOf(c) != -1) {
                return false;
            }
        }
        File f = new File(fileName);
        try {
            f.getCanonicalPath();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
