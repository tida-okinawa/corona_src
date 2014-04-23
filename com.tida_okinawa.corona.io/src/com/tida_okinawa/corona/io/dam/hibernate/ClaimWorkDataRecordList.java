/**
 * 2013/11/07 17:00:00
 * @author Kamakura
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.tida_okinawa.corona.io.dam.hibernate.connector.impl.ClaimWorkDataDaoConnector;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.IClaimWorkDataRecordList;
import com.tida_okinawa.corona.io.model.table.ITextRecord;

/**
 * {@link IClaimWorkData#getClaimWorkDatas()} の実装
 * 
 * テキスト部分は保持せずに、必要なときにDBから読む
 * 
 * @author Kamakura
 * 
 */
public class ClaimWorkDataRecordList implements IClaimWorkDataRecordList {
    /**
     * DB処理 (to get)
     */
    final ClaimWorkDataDaoConnector connector;

    /**
     * recordId -> {@link ITextRecord}
     */
    Map<Integer, ITextRecord> records;

    int historyId;


    /**
     * コンストラクタ
     * 
     * @param connector
     *            コンストラクタ
     */
    public ClaimWorkDataRecordList(ClaimWorkDataDaoConnector connector) {
        this.connector = connector;
    }


    /**
     * 初期化
     */
    void init() {
        ClaimWorkData workData = connector.getClaimWorkData();
        Set<Integer> ids = null;
        if (!ClaimWorkDataType.CORRECTION_MISTAKES.equals(workData.getClaimWorkDataType())) {

            /* 誤記補正以外の場合、最新履歴IDを取得する。なお、履歴IDが0の場合はサイズ0のidsをnewする。 */
            historyId = 0;
            // SQL作成
            StringBuilder sql = new StringBuilder(128).append("SELECT MAX(HISTORY_ID) FROM "); //$NON-NLS-1$
            sql.append(workData.getDbName()).append(" WHERE WORK_ID = ").append(workData.getWorkdataId()).append(" AND FLD_ID = ") //$NON-NLS-1$ //$NON-NLS-2$
                    .append(workData.getFieldId());
            Session session = IoService.getInstance().getSession();
            try {
                Object rs = session.createSQLQuery(sql.toString()).uniqueResult();
                if (rs != null) {
                    historyId = Integer.parseInt(rs.toString());
                }
            } catch (ClassCastException ex) {
                ex.printStackTrace();
                historyId = -1;
            } catch (HibernateException e) {
                e.printStackTrace();
                historyId = -1;
            }
            if (historyId > 0) {
                ids = connector.getKeys(historyId);
            } else {
                // TODO 20130718 historyIdが更新されない事
                /* Memo 解析履歴を残さない対応：HistryIdが0の場合、解析時にエラーが発生する為historyIdを1にする。 */
                historyId = 1;

                ids = new HashSet<Integer>(0);
            }
        } else {
            ids = connector.getKeys();
        }
        if (ids != null) {
            Integer[] recordIds = ids.toArray(new Integer[ids.size()]);
            records = new TreeMap<Integer, ITextRecord>();

            /* エディターからtoArray()で要求されるので用意しておく */
            for (int recordId : recordIds) {
                records.put(recordId, new TextRecord(recordId, historyId));
            }
        } else {
            System.err.println("レコードキーの一覧取得に失敗"); //$NON-NLS-1$
        }
    }

    /**
     * getText() でDBをアクセスする
     * 
     * @author imai
     * 
     */
    class TextRecord implements ITextRecord {
        final int recordId;
        @SuppressWarnings("hiding")
        final int historyId;


        TextRecord(int recordId, int historyId) {
            this.recordId = recordId;
            this.historyId = historyId;
        }


        @Override
        public int getId() {
            return recordId;
        }


        @Override
        public void setText(String text) {
            connector.commit(recordId, text);
        }


        @Override
        public String getText() {
            String text;
            if (historyId == 0) {
                text = connector.get(recordId);
            } else {
                text = connector.get(recordId, historyId);
            }
            return (text == null) ? "" : text; //$NON-NLS-1$
        }


        @Override
        public String getDispId() {
            return null;
        }


        @Override
        public void setDispId(String id) {
        }


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
                IPropertyDescriptor[] descriptor = new IPropertyDescriptor[] { new TextPropertyDescriptor("id", "ID"), //$NON-NLS-1$ //$NON-NLS-2$
                        new TextPropertyDescriptor("text", "テキスト") }; //$NON-NLS-1$ //$NON-NLS-2$
                return descriptor;
            }


            @Override
            public Object getPropertyValue(Object id) {
                if (id.equals("id")) { //$NON-NLS-1$
                    return String.valueOf(getId());
                }
                if (id.equals("text")) { //$NON-NLS-1$
                    return getText();
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


    /**
     * レコードIDで参照する
     * 
     * @param recordId
     *            レコードID
     * @return recordIDに一致するレコード
     */
    @Override
    public ITextRecord get(int recordId) {
        if (records == null) {
            init();
        }
        return records.get(recordId);
    }


    @Override
    public void add(int recordId, String data) {
        if (records == null) {
            init();
        }
        records.put(recordId, new TextRecord(recordId, historyId));
        connector.commit(recordId, data);
    }


    /**
     * @deprecated use {@link #add(int, String)}
     */
    @Deprecated
    @Override
    public ITextRecord set(int index, ITextRecord element) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void clear() {
        if (records == null) {
            init();
        }
        records.clear();
    }


    /**
     * DBに登録
     */
    @Override
    public void commit() {
        connector.close();
    }


    /**
     * @deprecated use {@link #add(int, String)}
     */
    @Deprecated
    @Override
    public boolean add(ITextRecord e) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated use {@link #add(int, String)}
     */
    @Deprecated
    @Override
    public void add(int index, ITextRecord element) {
        throw new UnsupportedOperationException();
    }


    @Deprecated
    @Override
    public boolean addAll(Collection<? extends ITextRecord> c) {
        throw new UnsupportedOperationException();
    }


    @Deprecated
    @Override
    public boolean addAll(int index, Collection<? extends ITextRecord> c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean contains(Object o) {
        if (o instanceof Integer) {
            return records.keySet().contains(o);
        }
        if (o instanceof ITextRecord) {
            int recordId = ((ITextRecord) o).getId();
            return records.keySet().contains(recordId);
        }
        return false;
    }


    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }


    @Deprecated
    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean isEmpty() {
        return records.isEmpty();
    }


    @Override
    public Iterator<ITextRecord> iterator() {
        if (records == null) {
            init();
        }
        return new RecordsIterator();
    }


    @Deprecated
    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }


    @Deprecated
    @Override
    public ListIterator<ITextRecord> listIterator() {
        throw new UnsupportedOperationException();
    }

    class RecordsIterator implements Iterator<ITextRecord> {
        Iterator<Integer> keyIterator = records.keySet().iterator();


        @Override
        public boolean hasNext() {
            return keyIterator.hasNext();
        }


        @Override
        public ITextRecord next() {
            int recordId = keyIterator.next();
            return records.get(recordId);
        }


        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }


    @Deprecated
    @Override
    public ListIterator<ITextRecord> listIterator(int index) {
        throw new UnsupportedOperationException();
    }


    @Deprecated
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }


    @Deprecated
    @Override
    public ITextRecord remove(int index) {
        throw new UnsupportedOperationException();
    }


    @Deprecated
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }


    @Deprecated
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public int size() {
        if (records == null) {
            init();
        }
        return records.size();
    }


    @Deprecated
    @Override
    public List<ITextRecord> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Object[] toArray() {
        if (records == null) {
            init();
        }
        return records.values().toArray();
        /* TODO ソート */
    }


    @Deprecated
    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }


    @Override
    public ITextRecord getRecord(int recordId) {
        if (records == null) {
            init();
        }
        return records.get(recordId);
    }


    @Override
    public ITextRecord getRecord(int recordId, int historyId) {
        if (records == null) {
            init();
        }

        if (this.historyId == historyId) {
            return records.get(recordId);
        } else {
            return new TextRecord(recordId, historyId);
        }
    }


    /**
     * 履歴ID更新
     * メモリ上の履歴IDをインクリメントする
     * 
     */
    public void upgradeHistoryId() {
        if (records == null) {
            init();
        }
        // TODO 20130718 historyIdが更新されない事 /* Memo 解析履歴を残さない対応：履歴を1つ以上持たない為、historyIdインクリメントする必要がない */　
        // historyId++;
    }


    /**
     * @return 履歴ID
     */
    public int getHistoryId() {
        return historyId;
    }


    /**
     * @param historyId
     *            履歴ID
     */
    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }
}