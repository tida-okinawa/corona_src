/**
 * @version $Id: QueueDataProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/10 03:46:11
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.controllers;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * データプロバイダー
 * 
 * @author imai
 * 
 * @param <T>
 *            入力データ
 */
abstract public class QueueDataProvider<T> implements IDataProvider<T> {

    final private ArrayBlockingQueue<T> queue;
    boolean hasData = true;


    /**
     * コンストラクタ
     * 
     * @param capacity
     *            キュー容量 = 処理スレッドの数
     */
    QueueDataProvider(int capacity) {
        queue = new ArrayBlockingQueue<T>(capacity);
    }


    /**
     * データを投入
     * 
     * @param data
     *            供給するデータ
     */
    protected void put(T data) {
        try {
            queue.put(data); // キュー満杯（処理スレッドがビジー）の場合はブロック
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * データ終わり
     */
    protected void end() {
        // キューが空になるのを待ってから終了フラグを立てる
        while (!queue.isEmpty()) {
            try {
                if (hasData) {
                    Thread.sleep(10);
                } else {
                    queue.clear();
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }
        hasData = false;
    }


    @Override
    public void endQueue() {
        hasData = false;
    }


    @Override
    public T next() {
        try {
            // 待っている間にデータが終わることがあるので、タイムアウトしてポーリング
            while (hasData) {
                T data = queue.poll(5, TimeUnit.MILLISECONDS);
                if (data != null) {
                    return data;
                }
            }
            return null; // データ終わり
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
