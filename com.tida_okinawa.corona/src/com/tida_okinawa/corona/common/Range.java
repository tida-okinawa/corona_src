/**
 * @version $Id: Range.java 1841 2014-04-16 06:01:48Z yukihiro-kinjyo $
 * 
 * 2012/09/03 17:10:07
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * StyleRangeをマージするためのクラス.
 * int[] {開始位置, 終了位置}の形式の配列を渡すと、重複する範囲をマージする
 * 
 * @author kousuke-morishima
 * 
 */
public class Range {

    List<int[]> ranges = new ArrayList<int[]>();


    /**
     * @return 現在の範囲
     */
    public List<int[]> getRanges() {
        return new ArrayList<int[]>(ranges);
    }


    private boolean needMarge = false;


    /**
     * マージ対象の範囲を追加する
     * 
     * @param range
     *            追加する範囲
     */
    public void add(int[] range) {
        ranges.add(range);
        needMarge = true;
    }


    /**
     * 現在の範囲をマージする
     */
    public void marge() {
        if (!needMarge) {
            return;
        }

        Collections.sort(ranges, new PrivateComparator());

        // ####[  0  ]####
        // [1][2][3][4][5]
        //   [2]     [4]
        //     [3] [3]
        //   [    6    ]
        for (int i = 0; i < ranges.size(); i++) {
            int[] r = ranges.get(i);
            Set<int[]> removed = new HashSet<int[]>(ranges.size());
            for (int j = i + 1; j < ranges.size(); j++) {
                int[] range = ranges.get(j);

                if (r[0] <= range[0]) {
                    if (r[1] < range[0]) {
                        // [5]
                    } else if (r[1] == range[0]) {
                        r[1] = range[1]; // [4]
                        removed.add(range);
                    } else {
                        if (r[1] < range[1]) {
                            r[1] = range[1]; // [4]
                            removed.add(range);
                        } else {
                            // [3]
                            removed.add(range);
                        }
                    }
                } else {
                    if (r[0] < range[1]) {
                        if (r[1] < range[1]) {
                            r[0] = range[0];
                            r[1] = range[1]; // [6]
                            removed.add(range);
                        } else {
                            r[0] = range[0]; // [2]
                            removed.add(range);
                        }
                    } else if (r[0] == range[1]) {
                        r[0] = range[0]; // [2]
                        removed.add(range);
                    } else {
                        // [1]
                    }
                }
            }
            for (int[] remove : removed) {
                ranges.remove(remove);
            }
        }

        needMarge = false;
    }


    static class PrivateComparator implements Comparator<int[]>, Serializable {
        private static final long serialVersionUID = -6376267296023639515L;


        @Override
        public int compare(int[] o1, int[] o2) {
            if (o1[0] < o2[0]) {
                return -1;
            }
            if (o1[0] > o2[0]) {
                return 1;
            }
            if (o1[1] < o2[1]) {
                return -1;
            }
            if (o1[1] > o2[1]) {
                return 1;
            }
            return 0;
        }
    }
}
