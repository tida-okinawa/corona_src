/**
 * @version $Id: HitPositionConverter.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/12/14 21:33:24
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.cleansing;

import java.util.ArrayList;
import java.util.List;

import com.tida_okinawa.corona.io.model.cleansing.HitPosition.PatternKind;

/**
 * @author kousuke-morishima
 * 
 */
public class HitPositionConverter {

    /**
     * @param hitPosition
     *            保存形式に変換するヒット位置情報
     * @return DBに保存する形式にしたヒット位置情報
     */
    public static final String toSaveString(HitPosition hitPosition) {
        List<int[]> positions = hitPosition.getPositions();
        PatternKind kind = hitPosition.getKind();

        StringBuilder saveString = new StringBuilder(5 + positions.size() * 25);
        saveString.append("(").append(kind); //$NON-NLS-1$
        for (int[] pos : positions) {
            saveString.append(" ").append(pos[0]).append("-").append(pos[1]); //$NON-NLS-1$ //$NON-NLS-2$
        }
        saveString.append(")"); //$NON-NLS-1$
        return saveString.toString();
    }


    /**
     * usr_relptn_xxx_yyyのhit_info列に保存されている形式の文字列を受け取り、HitPositionに変換する
     * 
     * @param hitString
     *            usr_relptn_xxx_yyyのhit_info列に保存されている形式の文字列
     * @return
     *         変換されたHitPosition
     */
    public static final HitPosition[] fromSaveString(String hitString) {
        List<String> hitStrings = split(hitString);
        List<HitPosition> hitInfos = new ArrayList<HitPosition>(hitStrings.size());
        for (String hitInfo : hitStrings) {
            convertHitPosition(hitInfo);
        }
        return hitInfos.toArray(new HitPosition[hitInfos.size()]);
    }


    /**
     * @param hitString
     *            ヒット情報ひとつ分の文字列
     * @return
     *         ヒット情報ひとつ分の文字列のデータが格納されたHitPosition
     */
    public static final HitPosition convertHitPosition(String hitString) {
        final int NONE = 0;
        final int TYPE = 1;
        final int POS = 2;
        int state = NONE;

        char[] typeArray = new char[12];
        int typeIndex = 0;

        int number = 0;
        List<Integer> numbers = new ArrayList<Integer>();

        char[] array = hitString.toCharArray();
        for (int i = 0; i < array.length; i++) {
            char ch = array[i];
            switch (state) {
            case TYPE:
                if (ch == ' ') {
                    state = POS;
                } else {
                    typeArray[typeIndex++] = ch;
                }
                break;
            case POS:
                if (ch == ')') {
                    numbers.add(number);
                    number = 0;
                    break;
                } else if (ch == '-' || (ch == ' ')) {
                    numbers.add(number);
                    number = 0;
                } else {
                    number = (number * 10) + (ch - '0');
                }
                break;
            default:
                if (ch == '(') {
                    state = TYPE;
                }
                break;
            }
        }
        PatternKind kind = PatternKind.valueOf(String.valueOf(typeArray, 0, typeIndex));
        HitPosition hitPosition = new HitPosition(kind);
        for (int i = 0; i < numbers.size(); i += 2) {
            hitPosition.addHitPosition(new int[] { numbers.get(i), numbers.get(i + 1) });
        }
        return hitPosition;
    }


    /**
     * @param hitString
     *            ヒット情報データの文字列
     * @return
     *         ヒット情報データが格納されたList
     */
    public static final List<String> split(String hitString) {
        List<String> hitInfos = new ArrayList<String>(hitString.split("\\(").length); //$NON-NLS-1$
        int fromIndex = 1;
        int indexS = 0;
        int indexE;
        while ((indexE = hitString.indexOf('(', fromIndex)) != -1) {
            hitInfos.add(hitString.substring(indexS, indexE));
            indexS = indexE;
            fromIndex = indexE + 1;
        }
        hitInfos.add(hitString.substring(indexS, hitString.length()));
        return hitInfos;
    }
}
