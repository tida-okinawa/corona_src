/**
 * @version $Id: ResultCoronaPattern.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/08/29 14:07:15
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.tida_okinawa.corona.io.model.dic.IPattern;

/**
 * パターンと、パターンがマッチした文（平文）および文のレコードIDを保持する
 * 
 * @author shingo-takahashi
 */
public class ResultCoronaPattern implements IResultCoronaPattern {
    protected int recordId;
    protected Map<IPattern, List<String>> hitInfos;
    protected String resultText;

    /**
     * label情報の区切り文字
     */
    static final String LabelSplitter = "%&%&%&"; //$NON-NLS-1$


    /**
     * Memo CoronaPattenParserから作るときのコンストラクタ
     * 
     * @param recordId
     *            レコードID.確定していない場合は-1
     * @param hitInfos
     *            <ヒットしたパターンのID, ヒット位置情報>のマップ
     * @param resultText
     *            構文解析結果のテキスト
     */
    public ResultCoronaPattern(int recordId, Map<IPattern, List<String>> hitInfos, String resultText) {
        this.recordId = recordId;
        this.hitInfos = hitInfos;
        this.resultText = resultText;
    }


    @Override
    public String getText() {
        int end = resultText.indexOf(LabelSplitter);
        if (end != -1) {
            return resultText.substring(0, end);
        }
        return resultText;
    }


    @Override
    public int getRecordId() {
        return recordId;
    }


    /**
     * この結果に対応するレコードのIDを設定する
     * 
     * @param id
     *            レコードID
     */
    public void setId(int id) {
        this.recordId = id;
    }


    @Override
    public String[] getLabels() {
        return resultText.split(LabelSplitter);
    }


    @Override
    public String getData() {
        return resultText;
    }


    @Override
    public Map<IPattern, List<String>> getHitPositions(int history) {
        return hitInfos;
    }


    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(1000);
        s.append("text={").append(resultText).append("}\nmatched=");
        for (Entry<IPattern, List<String>> e : hitInfos.entrySet()) {
            s.append(e.getKey().getId()).append(":").append(e.getKey().getLabel()).append(",");
        }
        return s.toString();
    }


    // TODO AdapterFactory 拡張ポイントを使って、外部に出したい */
    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (adapter.equals(IPropertySource.class)) {
            return source;
        }
        return null;
    }


    private IPropertySource source = new IPropertySource() {
        @Override
        public IPropertyDescriptor[] getPropertyDescriptors() {
            List<IPropertyDescriptor> descriptor = new ArrayList<IPropertyDescriptor>();
            descriptor.add(new TextPropertyDescriptor("id", "ID"));
            descriptor.add(new TextPropertyDescriptor("text", "テキスト"));
            int i = 0;
            for (Entry<IPattern, List<String>> e : hitInfos.entrySet()) {
                descriptor.add(new PropertyDescriptor(e.getKey().getId(), "パターン[" + i + "]"));
                i++;
            }
            return descriptor.toArray(new IPropertyDescriptor[descriptor.size()]);
        }


        @Override
        public Object getPropertyValue(Object id) {
            if (id.equals("id")) {
                return String.valueOf(getRecordId());
            }
            if (id.equals("text")) {
                return getText();
            }
            if (id instanceof Integer) {
                for (Entry<IPattern, List<String>> e : hitInfos.entrySet()) {
                    if (e.getKey().getId() == (Integer) id) {
                        return e.getKey().getLabel();
                    }
                }
            }
            return null;
        }


        @Override
        public boolean isPropertySet(Object id) {
            return false;
        }


        @Override
        public void resetPropertyValue(Object id) {
        }


        @Override
        public void setPropertyValue(Object id, Object value) {
        }


        @Override
        public Object getEditableValue() {
            return null;
        }
    };

}