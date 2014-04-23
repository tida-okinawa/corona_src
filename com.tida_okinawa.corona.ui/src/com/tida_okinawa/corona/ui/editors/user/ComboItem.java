/**
 * @version $Id: ComboItem.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/27 15:25:24
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.user;

import java.util.Arrays;

/**
 * TableViewerなどにComboBoxCellEditorをつけて編集できるようにしたとき、
 * コンボに表示するリストと元データの対応を保持するためのクラス
 * 
 * @author kousuke-morishima
 */
public abstract class ComboItem<T> {
    /**
     * 表示名のリスト
     */
    protected String[] names;

    /**
     * 元データ
     */
    protected T[] items;

    private boolean includeBlank;


    /**
     * @param items
     *            コンボボックスに表示するアイテムの元データ
     */
    public ComboItem(T[] items) {
        this(items, false);
    }


    /**
     * @param items
     * @param includeBlank
     *            アイテムの先頭に空文字を挿入するか
     */
    public ComboItem(T[] items, boolean includeBlank) {
        this.items = items;
        this.includeBlank = includeBlank;

        int blank = (includeBlank) ? 1 : 0;
        this.names = new String[items.length + blank];
        if (includeBlank) {
            this.names[0] = "";
        }
        for (int i = 0; i < items.length; i++) {
            names[i + blank] = toName(items[i]);
        }
    }


    /**
     * @param item
     * @return 指定されたアイテムが何番目の値か {@link #isIncludeBlank()}がtrueを返す場合、返る値は+1される
     */
    public int getIndex(T item) {
        int offset = (includeBlank) ? 1 : 0;
        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(item)) {
                return i + offset;
            }
        }
        return (includeBlank) ? 0 : -1;
    }


    /**
     * @param index
     * @return 指定された位置のアイテム
     *         indexが0より小さいかアイテム数を超える場合はnullを返す
     *         また、{@link #isIncludeBlank()}がtrueを返す場合、0を渡してもnullが返る
     */
    public T get(int index) {
        index = (includeBlank) ? index - 1 : index;
        if ((0 <= index) && (index < items.length)) {
            return items[index];
        }
        return null;
    }


    /**
     * @param item
     * @return 指定されたitemの表示名
     */
    protected abstract String toName(T item);


    /* ****************************************
     * getter
     */
    public String[] getNames() {
        return Arrays.copyOf(names, names.length);
    }


    public T[] getItems() {
        return Arrays.copyOf(items, items.length);
    }


    /**
     * @return {@link #names}の先頭に空アイテムを含むかどうか
     */
    public boolean isIncludeBlank() {
        return includeBlank;
    }
}
