/**
 * @version $Id: PatternRecords.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/30 18:10:42
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;

import com.tida_okinawa.corona.correction.parsing.model.IPatternListener;
import com.tida_okinawa.corona.correction.parsing.model.Link;
import com.tida_okinawa.corona.correction.parsing.model.Pattern;
import com.tida_okinawa.corona.correction.parsing.model.PatternContainer;
import com.tida_okinawa.corona.correction.parsing.model.PatternRecord;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;


/**
 * @author kousuke-morishima
 */
public class PatternRecords implements IPatternListener {
    private static final String PREFIX = "パターン";
    private int no;

    private IPatternDic ptnDic;
    private List<PatternRecord> patternRecords;

    private Map<Integer, String> patternName = new HashMap<Integer, String>();


    /**
     * @param ptnDic
     *            初期データ
     */
    public PatternRecords(IPatternDic ptnDic) {
        no = 0;
        this.ptnDic = ptnDic;
        List<IDicItem> items = ptnDic.getItems();

        for (IDicItem item : items) {
            patternName.put(((IPattern) item).getId(), ((IPattern) item).getLabel());
        }
        patternRecords = new ArrayList<PatternRecord>(items.size() + 10);
        for (IDicItem item : items) {
            PatternRecord rec = new PatternRecord((IPattern) item);
            initListener(rec);
            patternRecords.add(rec);
        }

        for (PatternRecord rec : patternRecords) {
            String label = rec.getLabel();
            if (rec.getLabel().startsWith(PREFIX)) {
                try {
                    int num = Integer.parseInt(label.substring(PREFIX.length()));
                    if (no < num) {
                        no = num;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
    }


    private void initListener(Pattern pattern) {
        if (pattern instanceof PatternContainer) {
            if (pattern instanceof PatternRecord) {
                ((PatternRecord) pattern).addPatternListener(this);
            }
            for (Pattern child : ((PatternContainer) pattern).getChildren()) {
                initListener(child);
            }
        } else if (pattern instanceof Link) {
            addPatternListener((Link) pattern);
            ((Link) pattern).setLabel(patternName.get(((Link) pattern).getId()));
        }
    }


    public String nextPatternName() {
        no++;
        return PREFIX + no;
    }


    public void add(PatternRecord record) {
        add(size(), record);
    }


    public void add(int index, PatternRecord record) {
        if ((index < 0) || (index > size())) {
            return;
        }

        if (record.getLabel().equals("")) {
            record.setLabel(nextPatternName());
        }

        ptnDic.addItem(record.getIPattern());
        record.addPatternListener(this);
        patternRecords.add(index, record);
        patternAdded(new PatternEvent(null, record));
    }


    public void remove(PatternRecord record) {
        ptnDic.removeItem(record.getIPattern());
        record.removePatternListener(this);
        patternRecords.remove(record);
        patternRemoved(new PatternEvent(null, record));
    }


    public List<PatternRecord> getPatternRecords() {
        return new ArrayList<PatternRecord>(patternRecords);
    }


    public PatternRecord get(int index) {
        return patternRecords.get(index);
    }


    public int indexOf(PatternRecord record) {
        return patternRecords.indexOf(record);
    }


    public int size() {
        return patternRecords.size();
    }

    /* ****************************************
     * Listeners
     */
    private ListenerList listeners = new ListenerList();


    public void addPatternListener(IPatternListener listener) {
        listeners.add(listener);
    }


    public void removePatternListener(IPatternListener listener) {
        listeners.remove(listener);
    }


    @Override
    public void patternAdded(PatternEvent event) {
        if (event.child instanceof Link) {
            addPatternListener((Link) event.child);
        }
        for (Object l : listeners.getListeners()) {
            ((IPatternListener) l).patternAdded(event);
        }
    }


    @Override
    public void patternRemoved(PatternEvent event) {
        if (event.child instanceof Link) {
            removePatternListener((Link) event.child);
        }
        for (Object l : listeners.getListeners()) {
            ((IPatternListener) l).patternRemoved(event);
        }
    }


    @Override
    public void patternChanged(PatternEvent event) {
        for (Object l : listeners.getListeners()) {
            ((IPatternListener) l).patternChanged(event);
        }
    }


    /* ****************************************
     * other
     */
    public void dispose() {
        for (PatternRecord rec : patternRecords) {
            rec.removePatternListener(this);
        }
    }
}
