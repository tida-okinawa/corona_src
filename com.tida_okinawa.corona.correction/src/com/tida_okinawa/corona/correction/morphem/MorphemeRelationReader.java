/**
 * @version $Id: MorphemeRelationReader.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/03 13:31:23
 * @author sanenori-makiya
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.morphem;


import java.util.ArrayList;
import java.util.List;

import com.tida_okinawa.corona.correction.common.TextReader;

/**
 * 
 * 形態素解析・係り受け解析結果を読み取る
 * 
 * @author sanenori-makiya
 */
public class MorphemeRelationReader extends TextReader {
    /**
     * 処理結果
     */
    List<String> resultList = new ArrayList<String>();


    /**
     * 処理結果を取得
     * 
     * @return 解析結果
     */
    public List<String> getResultList() {
        return resultList;
    }


    @Override
    protected void process(String line) {
        // コメント行は無視
        if (line.startsWith(";")) //$NON-NLS-1$
            return;

        /* 文字列を" "で分割 */
        String[] lineDivision = line.split(" ", 2); //$NON-NLS-1$

        /* コメント ";" , 係り受け(格解析)の判別⇒1カラム目が「*」のレコードは無視する */
        if (lineDivision[0].equals("*")) { //$NON-NLS-1$
            return;
        }

        /* Knp解析で未定義語が自動で変換された場合、未定義語に差し戻す処理 */
        if (line.contains(Messages.MorphemeRelationReader_PART_CHANGE) && line.contains("-15-")) { //$NON-NLS-1$
            String[] marge = line.split(" ", 12); //$NON-NLS-1$
            marge[3] = Messages.MorphemeRelationReader_UNDEFINED_WORD;
            marge[4] = "15"; //$NON-NLS-1$
            /* 未定義語の品詞詳細を判定 */
            if (line.contains("15-1-0-0")) { //$NON-NLS-1$
                marge[5] = Messages.MorphemeRelationReader_OTHER_WORD;
                marge[6] = "1"; //$NON-NLS-1$
            } else if (line.contains("15-2-0-0")) { //$NON-NLS-1$
                marge[5] = Messages.MorphemeRelationReader_KATAKANA;
                marge[6] = "2"; //$NON-NLS-1$
            } else if (line.contains("15-3-0-0")) { //$NON-NLS-1$
                marge[5] = Messages.MorphemeRelationReader_ALPHABET;
                marge[6] = "3"; //$NON-NLS-1$
            }
            marge[7] = "*"; //$NON-NLS-1$
            marge[8] = "0"; //$NON-NLS-1$
            marge[9] = "*"; //$NON-NLS-1$
            marge[10] = "0"; //$NON-NLS-1$

            StringBuilder builder = new StringBuilder();
            /* Splitで分割した部分を元に戻す処理 */
            for (String str : marge) {
                builder.append(str).append(" "); //$NON-NLS-1$
            }
            resultList.add(builder.substring(0));
        } else {
            resultList.add(line);
        }
    }
}
