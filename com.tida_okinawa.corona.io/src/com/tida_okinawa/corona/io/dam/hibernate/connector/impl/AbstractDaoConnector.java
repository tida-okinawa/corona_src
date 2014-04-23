/**
 * @version $Id: AbstractDaoConnector.java 997 2013-06-05 01:58:11Z yukihiro-kinjyo $
 * 
 * 2011/11/04 01:04:11
 * @author imai-yoshikazu
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate.connector.impl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.tida_okinawa.corona.io.dam.hibernate.connector.DaoConnecter;

/**
 * DB読み書きのクラス
 * 
 * 大きいテーブルで、値をオンデマンドで取得するのに使う
 * 
 * TODO: 不要になったときにThreadを破棄しないと keys ごと残ってしまう
 * 
 * @author imai
 * 
 * @param <K>
 *            キーの型
 * @param <T>
 *            レコードのデータの型
 */
abstract public class AbstractDaoConnector<K, T> implements DaoConnecter<K, T> {

    /**
     * commit待ち
     */
    private ArrayBlockingQueue<Data> queue;

    /**
     * commitを実行するスレッド
     */
    protected Thread committer;

    /**
     * committerへの終了指示
     */
    private AtomicBoolean isClosed = new AtomicBoolean(false);

    /**
     * キーの一覧
     * 
     * note: new K[0] ができないのでSetにしている
     */
    protected Set<K> keys;


    AbstractDaoConnector() {
        /* コミット用のキューを用意 */
        queue = new ArrayBlockingQueue<Data>(100);
    }


    /**
     * コミット用スレッドを開始
     */
    private void startCommitter() {
        /* スレッドを起動 */
        /* 以降 commit() が呼ばれるごとにコミットを実行 */
        isClosed.set(false);
        /* コミット用のスレッドを用意 */
        committer = new Thread() {
            List<Data> data = new ArrayList<AbstractDaoConnector<K, T>.Data>();


            @Override
            public void run() {
                try {
                    Data d = null;
                    int len = 0;
                    while (!(isClosed.get() && queue.isEmpty())) {
                        d = queue.poll(10, TimeUnit.MILLISECONDS);
                        if (d != null) {/* データがある時 */
                            int curLength = d.key.toString().length() + d.value.toString().length();
                            if ((len + curLength) >= 330000) {
                                /*
                                 * データがnullでない、かつ、リストに追加したデータの総文字数が330000byte以上になる場合
                                 * 文字列MAXが1000000。 UTF-8で日本語は大体3byte。
                                 * 1000000/3=333333なので、330000byte
                                 * MySQLは4byte文字を扱えないので、考慮しないでいいらしい！
                                 */
                                doCommit(data);
                                data.clear();
                                len = 0;
                            }
                            data.add(d);
                            len += curLength;
                        }
                    }
                    if (data.size() > 0) {/* 余りの端数分があれば、doCommitに投げる */
                        doCommit(data);
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        committer.start();
    }


    /**
     * コミット用スレッドを終了
     */
    private void stopCommitter() {
        isClosed.set(true);
        try {
            committer.join();
        } catch (InterruptedException e) {
            //
        }
        committer = null;
    }


    /**
     * {@link Connection} を取得する
     * 
     * @return
     * @throws HibernateException
     *             DB接続に失敗した
     */
    abstract Session getConnection() throws HibernateException;


    @Override
    public Set<K> getKeys() {
        try {
            /* コミット終了まで待つ */
            while (committer != null) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            }
            String strSQL = createStatementForGetKeys();
            Session connection = getConnection();
            String checkSql = "Select rec_id, pattern_id From"; //$NON-NLS-1$
            int endIndex = checkSql.length();
            if (strSQL.length() > endIndex && checkSql.equals(strSQL.subSequence(0, endIndex))) {
                @SuppressWarnings("unchecked")
                List<Object[]> ids = connection.createSQLQuery(strSQL).list();
                if (ids == null) {
                    return new HashSet<K>(0);
                }
                keys = new HashSet<K>(10000);
                for (Object[] id : ids) {
                    K key = getKey(id);
                    if (key != null) {
                        keys.add(key);
                    }
                }
            } else {
                @SuppressWarnings("unchecked")
                List<Object> ids = connection.createSQLQuery(strSQL).list();
                if (ids == null) {
                    return new HashSet<K>(0);
                }
                keys = new HashSet<K>(10000);
                for (Object id : ids) {
                    Object[] idArr = new Object[2];
                    idArr[0] = id;
                    K key = getKey(idArr);
                    if (key != null) {
                        keys.add(key);
                    }
                }
            }
        } catch (ClassCastException ex) {
            ex.printStackTrace();
            keys = new HashSet<K>(0);
        } catch (HibernateException e) {
            e.printStackTrace();
            keys = new HashSet<K>(0);
        }
        return keys;
    }


    /**
     * キーの一覧を取得するためのSQLを作る
     * 
     * @return
     */
    abstract protected String createStatementForGetKeys();


    /**
     * {@link #createStatementForGetKeys()}の結果からキーを取得する
     * 
     * @param rs
     * @return
     */
    abstract protected K getKey(Object[] rs) throws HibernateException;


    @Override
    public int size() {
        if (keys == null) {
            keys = getKeys();
        }
        return keys.size();
    }

    /**
     * get() で使う Sql(Hqlとしてでは使用しない)
     */
    protected String stmtForGet;


    @Override
    public T get(K key) {
        try {
            Session session = getConnection();
            String strSqlExec;
            if (stmtForGet == null) {
                String strSQL = prepareStatementForGet();
                stmtForGet = strSQL;
            }
            strSqlExec = setParamForGet(stmtForGet, key);
            @SuppressWarnings("unchecked")
            List<Object[]> list = session.createSQLQuery(strSqlExec).list();
            if (list != null) {
                if (list.size() > 0) {
                    T value = getResultDirect(list);
                    return value;
                }
            }
        } catch (HibernateException e) {
            System.err.println(e + ":" + key); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * {@link DaoConnecter#get(Object)} のためのSQLを用意
     * 
     * @return
     * @throws HibernateException
     */
    abstract protected String prepareStatementForGet() throws HibernateException;


    /**
     * {@link #prepareStatementForGet()} で用意したSQLにパラメータをセットする
     * 
     * @param key
     */
    abstract protected String setParamForGet(String stmt, K key) throws HibernateException;


    /**
     * クエリーの結果から値を取得する
     * 
     * @param rs
     * @return
     */
    abstract protected T getResultDirect(List<Object[]> list) throws HibernateException;

    /**
     * コミット用のレコード
     */
    class Data {
        final K key;
        final T value;


        Data(K key, T value) {
            this.key = key;
            this.value = value;
        }
    }


    @Override
    public synchronized void commit(K key, T value) {
        Data data = new Data(key, value);
        if (committer == null) {
            startCommitter();
        }
        try {
            queue.put(data);
        } catch (InterruptedException e) {
        }
    }


    /**
     * コミット実行部分<br/>
     * 今、このメソッドは使われていない。2012/02/17
     * 
     * @param data
     */
    @SuppressWarnings("unused")
    protected void doCommit(K key, T value) {

        // TODO 20131111 jdbc版での呼び出し階層での確認では未使用となっている。

    }


    protected void doCommit(List<Data> datas) {
        /*
         * 問い合わせデータのレコード量が多いとき、最後までstmtを閉じないとメモリが足りなくなる。
         * 毎回生成とcloseを行う。
         */
        String strSqlExec;
        Session session = null;
        try {
            String strSQL = prepareStatementForCommit(datas.size());
            session = getConnection();
            strSqlExec = setParamForCommit(strSQL, datas);
            /* トランザクション開始 */
            session.beginTransaction();
            session.createSQLQuery(strSqlExec).executeUpdate();
            session.flush();
            session.getTransaction().commit();
        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            /* トランザクション中だった場合はロールバックする */
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
        }
    }


    /**
     * {@link DaoConnecter#commit(Object, Object)} 用のSQLを用意
     * 
     * @return
     */
    abstract protected String prepareStatementForCommit();


    /**
     * {@link DaoConnecter#commit(Object, Object)} 用のSQLを用意(複数レコード用)
     * 
     * @param size
     *            1以上の値
     * @return
     */
    abstract protected String prepareStatementForCommit(int size);


    /**
     * {@link #commit(Object, Object)}のSQLにパラメータをセットする
     * 
     * @param stmt
     * @param key
     * @param value
     */
    abstract protected String setParamForCommit(String stmt, K key, T value) throws HibernateException;


    /**
     * {@link #commit(Object, Object)}のSQLにパラメータをセットする
     * 
     * @param stmt
     * @param key
     */
    abstract protected String setParamForCommit(String stmt, List<Data> data) throws HibernateException;


    @Override
    abstract public void clear();


    @Override
    public void close() {
        try {
            if (committer != null) {
                /* データコミット時は毎回クローズするようにした */
                stopCommitter();
            }
            if (stmtForGet != null) {
                stmtForGet = null;
            }
        } catch (HibernateException e) {
            System.err.println(e);
        }
    }
}
