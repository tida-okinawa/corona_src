/**
 * @version $Id: IPatternListener.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/30 15:18:47
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;


/**
 * @author kousuke-morishima
 */
public interface IPatternListener {
    public static final int ADDED = 1 << 0;
    public static final int REMOVED = 1 << 1;


    /**
     * 子が追加された通知を受ける<br/>
     * parent, child, positionが有効
     * 
     * @param event
     */
    public void patternAdded(PatternEvent event);


    /**
     * 子が削除された通知を受ける<br/>
     * parent, childが有効
     * 
     * @param event
     */
    public void patternRemoved(PatternEvent event);


    /**
     * 変更されたパターン<br/>
     * childが有効
     * 
     * @param event
     */
    public void patternChanged(PatternEvent event);

    /**
     * パターンに変更があったときの通知に使用されるクラス
     * 
     * @author KMorishima
     */
    public static class PatternEvent {
        /**
         * 位置が指定されていない
         */
        public static final int NO_POS = -1;

        /**
         * このパターンの子が「追加」「削除」された
         */
        public PatternContainer parent;
        /**
         * このパターンが「追加」「削除」「変更」された
         */
        public Pattern child;
        /**
         * 追加されたとき、親のどこに追加されたのかを示す。追加位置が指定されていないときは{@link #NO_POS}。
         */
        public int position;


        /**
         * @param parent
         * @param child
         * @param position
         */
        public PatternEvent(PatternContainer parent, Pattern child, int position) {
            this.parent = parent;
            this.child = child;
            this.position = position;
        }


        /**
         * @param parent
         * @param child
         */
        public PatternEvent(PatternContainer parent, Pattern child) {
            this(parent, child, -1);
        }
    }
}
