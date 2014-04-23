/**
 * @version $Id: ObjectSyncEvent.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/15 19:02:31
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Event;

/**
 * @author kousuke-morishima
 */
public class ObjectSyncEvent extends Event {
    /**
     * イベント種別
     */
    public static final int ADDED = 1;
    /**
     * イベント種別
     */
    public static final int REMOVED = 2;
    /**
     * イベント種別
     */
    public static final int MODIFIED = 3;

    private Object listeningResource;
    private Object[] changedItems;
    private int eventType;


    /**
     * @param listeningTarget
     * @param changedItem
     * @param eventType
     * @see #ADDED
     * @see #REMOVED
     * @see #MODIFIED
     */
    public ObjectSyncEvent(Object listeningTarget, Object changedItem, int eventType) {
        this(listeningTarget, new Object[] { changedItem }, eventType);
    }


    public ObjectSyncEvent(Object listeningTarget, List<Object> changedItems, int eventType) {
        this(listeningTarget, changedItems.toArray(), eventType);
    }


    public ObjectSyncEvent(Object listeningTarget, Object[] changedItems, int eventType) {
        Assert.isNotNull(listeningTarget);
        this.listeningResource = listeningTarget;
        Assert.isNotNull(changedItems);
        this.changedItems = changedItems;
        checkType(eventType);
        this.eventType = eventType;
    }


    public Object getListeningResource() {
        return listeningResource;
    }


    public Object[] getChangedItems() {
        return Arrays.copyOf(changedItems, changedItems.length);
    }


    /**
     * @return {@link #getChangedItems()}
     *         で取得できるアイテムが、追加されたものなのか、削除されたものなのか、変更されたものなのか
     * @see #ADDED
     * @see #REMOVED
     * @see #MODIFIED
     */
    public int getEventType() {
        return eventType;
    }


    private static void checkType(int eventType) {
        if (!(eventType >= 1) && (eventType <= 3)) {
            throw new IllegalArgumentException("eventType: " + eventType + " が想定外の値です");
        }
    }

}
