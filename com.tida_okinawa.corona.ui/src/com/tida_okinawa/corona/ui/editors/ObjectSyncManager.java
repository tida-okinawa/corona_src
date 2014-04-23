/**
 * @version $Id: ObjectSyncManager.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/15 19:11:19
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author kousuke-morishima
 */
public class ObjectSyncManager {
    private ObjectSyncManager() {
    }

    public static final ObjectSyncManager INSTANCE = new ObjectSyncManager();

    private Map<Object, List<IObjectSyncListener>> targetMap = new HashMap<Object, List<IObjectSyncListener>>();


    /**
     * 監視対象オブジェクトの変更通知を受け取るリスナーを登録する
     * 
     * @param target
     *            監視対象オブジェクト
     * @param listener
     *            通知を受け取るリスナー
     */
    public void addListener(Object target, IObjectSyncListener listener) {
        if (target != null) {
            List<IObjectSyncListener> listeners = targetMap.get(target);
            if (listeners == null) {
                listeners = new ArrayList<IObjectSyncListener>();
                targetMap.put(target, listeners);
            }
            listeners.add(listener);
        }
    }


    /**
     * 指定したリスナーへ監視対象オブジェクトからの変更通知がこないようにする<br />
     * 監視対象オブジェクトが登録されていないとき、このメソッドは何も行わない
     * 
     * @param target
     *            監視対象オブジェクト
     * @param listener
     *            変更通知受け取りをやめるリスナー
     */
    public void removeListener(Object target, IObjectSyncListener listener) {
        if (target != null) {
            List<IObjectSyncListener> listeners = targetMap.get(target);
            if (listeners != null) {
                listeners.remove(listener);
                if (listeners.size() == 0) {
                    targetMap.remove(target);
                }
            }
        }
    }


    /**
     * 監視対象オブジェクトの変更を通知する
     * 
     * @param target
     *            監視対象オブジェクト
     * @param modifiedItems
     *            変更されたオブジェクト。監視対象の内包物かもしれないし、監視対象オブジェクトそのものかもしれない
     * @param eventType
     * @see ObjectSyncEvent#ADDED
     * @see ObjectSyncEvent#REMOVED
     * @see ObjectSyncEvent#MODIFIED
     */
    public void modified(Object target, Object[] modifiedItems, int eventType) {
        if (target != null) {
            List<IObjectSyncListener> listeners = targetMap.get(target);
            if (listeners != null) {
                ObjectSyncEvent event = new ObjectSyncEvent(target, modifiedItems, eventType);
                for (IObjectSyncListener l : listeners) {
                    l.modified(event);
                }
            }
        }
    }


    /**
     * オブジェクトが誰かの監視対象になっているかを返す
     * 
     * @param target
     * @return 監視対象ならtrue
     */
    public boolean isListening(Object target) {
        return targetMap.containsKey(target);
    }
}
