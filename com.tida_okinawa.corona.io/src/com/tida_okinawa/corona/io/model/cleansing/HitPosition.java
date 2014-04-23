/**
 * @version $Id: HitPosition.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/12/14 10:43:13
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model.cleansing;

import java.util.ArrayList;
import java.util.List;

/**
 * 構文解析のヒット位置を保持するクラス.<br/>
 * １つのヒット位置は、要素２のint[]で、
 * 0:ヒット開始位置（形態素の番号）、1:ヒットした長さ（形態素の数）を表す。
 * 
 * @author kousuke-morishima
 */
public class HitPosition {

    private List<int[]> positions;
    private int startPosition;
    private int length;
    private PatternKind kind;

    private boolean needUpdate = false;


    /**
     * @param kind
     *            このヒット位置オブジェクトが表す、パターン種別
     */
    public HitPosition(PatternKind kind) {
        this.kind = kind;
        startPosition = -1;
        length = 0;
        positions = new ArrayList<int[]>();
    }


    /**
     * このオブジェクトが保持している、実際にヒットした位置の一覧を返す.<br/>
     * 
     * @return 実際にヒットした位置の一覧
     */
    public List<int[]> getPositions() {
        return new ArrayList<int[]>(positions);
    }


    /**
     * このオブジェクトが、どのパターン種別がヒットした情報を保持しているのかを示す.
     * 
     * @return パターン種別
     */
    public PatternKind getKind() {
        return kind;
    }


    /**
     * 子パターンのヒット位置を自身に追加する.<br/>
     * 
     * @param hitPosition
     *            このオブジェクトが示すパターンの子パターンがヒットした位置
     */
    public void addHitPosition(HitPosition hitPosition) {
        if (hitPosition.isHit()) {
            addHitPosition(hitPosition.getRange());
        }
    }


    /**
     * HitPositionConverterからのみ使われるメソッド.<br/>
     * ヒット位置を追加する。
     * 
     * @param hitPosition
     *            追加するヒット位置
     */
    public void addHitPosition(int[] hitPosition) {
        positions.add(hitPosition);

        /* 情報が更新されたので、length等の再設定が必要 */
        needUpdate = true;
    }


    /**
     * このオブジェクトが示すパターンがヒットした範囲を返す.<br/>
     * 0:ヒット開始位置の形態素番号、1:ヒットした形態素の数
     * 
     * @return ヒット範囲
     */
    public int[] getRange() {
        /* 戻り値（初期値:未使用の場合の値を含む） */
        int[] retValue = new int[] { startPosition, length };
        /* 作業 */
        int[] topPosValue = new int[] { -1, 0 };
        int[] btmPosValue = new int[] { -1, 0 };

        if (!needUpdate || positions.size() <= 0) {
            /* 既に判明している値を戻り値とする。 */
            return retValue;
        }
        /* length等の再設定が完了 */
        needUpdate = false;

        /* getRangeは、typeごとに異なる範囲を返すので、それぞれの範囲計算方法を実施する */
        switch (kind) {
        case TERM:
        case OR:
        case NOT:
        case MODIFICATION:
            /* 何番目の形態素からヒットかの情報をセット */
            startPosition = positions.get(0)[0];
            /* lengthの設定 */
            length = positions.get(0)[1];
            return positions.get(0).clone();
        case SEQUENCE:
        case ORDER:
            if (positions.size() == 1) {
                /* 何番目の形態素からヒットかの情報をセット */
                startPosition = positions.get(0)[0];
                /* lengthの設定 */
                length = positions.get(0)[1];
                return positions.get(0).clone();
            } else {
                /* positionsの先頭のヒット位置から末尾のヒット位置までの範囲を返す */
                retValue = positions.get(0).clone();
                btmPosValue = positions.get(positions.size() - 1);
                retValue[1] = btmPosValue[0] + btmPosValue[1] - retValue[0];
                /* 何番目の形態素からヒットかの情報をセット */
                startPosition = retValue[0];
                /* lengthの設定 */
                length = retValue[1];
                return retValue;
            }
        case AND:
            if (positions.size() == 1) {
                /* 何番目の形態素からヒットかの情報をセット */
                startPosition = positions.get(0)[0];
                /* lengthの設定 */
                length = positions.get(0)[1];
                return new int[] { startPosition, length };
            } else {
                btmPosValue = positions.get(positions.size() - 1).clone();

                for (int i = 0; i < positions.size(); i++) {
                    retValue = positions.get(i).clone();
                    if (topPosValue[0] == -1) {
                        topPosValue = retValue;
                    } else {
                        if (topPosValue[0] > retValue[0]) {
                            topPosValue = retValue;
                        }
                    }
                    if (btmPosValue[0] < retValue[0]) {
                        btmPosValue = retValue;
                    }
                }
                /* もっとも前からヒットしたヒット位置 */
                retValue[0] = topPosValue[0];
                /* もっとも後ろまでヒットしたヒット位置までの範囲（ヒットの長さ（ヒットした形態素の数）） */
                retValue[1] = btmPosValue[0] + btmPosValue[1] - retValue[0];
                /* 何番目の形態素からヒットかの情報をセット */
                startPosition = retValue[0];
                /* lengthの設定 */
                length = retValue[1];
                return new int[] { startPosition, length };
            }
        default:
            break;
        }
        /* 既に判明している値を戻り値とする。 */
        return retValue;
    }


    /**
     * このオブジェクトがヒット位置を保持している（ヒットした）ならtrue, そうでないならfalseを返す
     * 
     * @return ヒットしたならtrue, そうでないならfalse
     */
    public boolean isHit() {
        return positions.size() > 0;
    }


    /**
     * ヒット位置オブジェクトの保存形式文字列を生成する
     */
    @Override
    public String toString() {
        return HitPositionConverter.toSaveString(this);
    }


    /**
     * HitPositionが、どのパターンのヒット情報を保持しているのかを表す列挙型
     * 
     * @author kousuke-morishima
     */
    public static enum PatternKind {
        /** 未定義状態 */
        NONE,
        /** 単語 */
        TERM,
        /** And */
        AND,
        /** Or */
        OR,
        /** Not */
        NOT,
        /** 連続 */
        SEQUENCE,
        /** 順序 */
        ORDER,
        /** 係り受け */
        MODIFICATION,
        /** 参照 */
        LINK;
    }
}
