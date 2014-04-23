/**
 * @file UserDic.java
 * @version $Id$
 *
 * 2013/10/29 13:28:58
 * @author hajime-uchihara
 *
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.bean.DicCommonBean;
import com.tida_okinawa.corona.io.bean.DicTableBean;
import com.tida_okinawa.corona.io.model.TextItem;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.TermCForm;
import com.tida_okinawa.corona.io.model.dic.TermClass;
import com.tida_okinawa.corona.io.model.dic.TermPart;
import com.tida_okinawa.corona.io.model.dic.UserDicFieldType;
import com.tida_okinawa.corona.io.model.dic.abstraction.AbstractUserDic;
import com.tida_okinawa.corona.io.model.dic.impl.Term;

/**
 * @author hajime-uchihara
 * 
 */
public final class UserDic extends AbstractUserDic {

    private boolean expFlg; /* エクスポート対象フラグ */

    Map<Integer, ITerm> dirtyItems = new HashMap<Integer, ITerm>();


    /**
     * 
     * エクスポート対象フラグチェック
     * 
     * @return true/false
     */
    public boolean isExpFlg() {
        return expFlg;
    }


    /**
     * 
     * エクスポート対象フラグ設定
     * 
     * @param expFlg
     */
    public void setExpFlg(boolean expFlg) {
        this.expFlg = expFlg;
    }


    /**
     * コンストラクタ
     * 
     * @param _id
     * @param _dispName
     * @param _fileName
     * @param _dicType
     * @param _lasted
     */
    public UserDic(int _id, String _dispName, String _fileName, DicType _dicType, Date _lasted) {
        super(_id, _dispName, _fileName, _dicType, _lasted);
    }


    /**
     * 
     * 辞書インポート処理
     * 
     * @param path
     * @param userDicName
     * @param dicType
     */
    @Override
    protected void importDicDam(String path, String parentDicName, DicType dicType) {
        // TODO DicType.JUMANの辞書はDBで運用しないので、今回の変更で不要
        // 呼び出し階層での確認ではCoronaIoUtilsクラスのメソッドjumanImportで
        // 「((UserDic) dic).importDic(path, name, DicType.JUMAN);」でのみの呼び出し

        // TODO 20131029 Juman辞書専用のImportの機能は削除予定なので、テストは未実施とする。
        // fail("Juman辞書専用のImportの機能は削除予定なので、テストは未実施とする。");

    }


    /**
     * 辞書一覧テーブル存在チェック
     * 
     * @param dicName
     * @return エラーがなければnull。エラーがあればエラーメッセージ。
     */
    public static String checkDicTable(String dicName) {
        Session session = IoService.getInstance().getSession();
        String errInfo = null;
        int dicId = 0;

        try {
            @SuppressWarnings("unchecked")
            List<DicTableBean> dicTables = session.createQuery("FROM DicTableBean WHERE dicName = :dicname") //$NON-NLS-1$
                    .setString("dicname", dicName) //$NON-NLS-1$
                    .list();
            if (dicTables.size() > 0) {
                dicId = dicTables.get(0).getDicId();
            }
        } catch (HibernateException e) {
            e.printStackTrace();
            dicId = -1;
        }
        if (dicId != 0) {
            if (dicId == -1) {
                errInfo = "システムエラーが発生しました。(辞書一覧登録済確認チェック)"; //$NON-NLS-1$
            }
        }

        return errInfo;
    }


    /**
     * 
     * JUMANフォーマット生成
     * 
     * @param term
     * @return String
     */
    public static String createJumanFormat(ITerm term) {

        String value = term.getValue();
        String reading = term.getReading();
        TermPart termPart = term.getTermPart();
        TermClass termClass = term.getTermClass();
        TermCForm cform = term.getCform();

        StringBuilder jumanbase = new StringBuilder();

        boolean bFlg = false;
        if (TermPart.NONE.equals(termPart)) {
            return null;
        }
        /* 品詞を設定 */
        jumanbase.append("(" + termPart.getName() + " ("); //$NON-NLS-1$ //$NON-NLS-2$

        /* 品詞詳細を設定 */
        if (!TermClass.NONE.equals(termClass)) {
            jumanbase.append(termClass.getName() + " ("); //$NON-NLS-1$
            bFlg = true;
        }
        /*
         * 品詞詳細は、品詞によっては必須項目ではない //else { // /* 品詞詳細がNullの場合はとりあえずエラー
         */
        // return null;
        // }

        /* 見出し語 */
        if (!"".equals(value)) { //$NON-NLS-1$
            jumanbase.append("(見出し語 (" + value + " "); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            return null;
        }

        /* スコアを設定　（優先順位を上げる為　を設定） */
        if (!"".equals(value)) { //$NON-NLS-1$
            jumanbase.append("0.1))"); //$NON-NLS-1$
        } else {
            return null;
        }

        /* 読みが存在する場合 */
        if (!"".equals(reading)) { //$NON-NLS-1$
            jumanbase.append("(読み " + reading + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            return null;
        }

        /* 活用型が存在する場合 */
        if (!TermCForm.NONE.equals(cform)) {
            jumanbase.append("(活用型 " + cform.getName() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (bFlg) {
            jumanbase.append(")))"); //$NON-NLS-1$
        } else {
            jumanbase.append("))"); //$NON-NLS-1$
        }

        return jumanbase.toString();
    }


    /**
     * エクスポート処理
     * 
     * @param path
     * @param file
     * @param jumanTexts
     * @param encoding
     * @return true/false
     */
    public static boolean export(String path, String file, List<String> jumanTexts, String encoding) { // ファイルのパス・ファイル名・jumanBaseデータ

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path, file)), encoding));

            for (String jbase_tmp : jumanTexts) {
                /* jumanTextsデータをファイルへ書き込み */
                bw.write(jbase_tmp);
                bw.write("\n"); //$NON-NLS-1$
            }
            bw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }


    @Override
    public List<IDicItem> getItems() {
        if (items == null) {
            updateRecords();
        }
        return new ArrayList<IDicItem>(items);
    }


    @Override
    public int getRecCount() {
        String sql = getTermCount(this.getId(), 0, -1, getFilters(), false, isAcceptOrNot());
        Session session = IoService.getInstance().getSession();
        int id = 0;
        try {
            BigInteger val = (BigInteger) session.createSQLQuery(sql).uniqueResult();
            id = val.intValue();
        } catch (HibernateException e) {
            id = -1;
        }

        return id;
    }


    @Override
    protected boolean doCommit(boolean bRecords, IProgressMonitor monitor) {
        monitor.beginTask("ユーザ辞書保存", 4); //$NON-NLS-1$

        Session session = IoService.getInstance().getSession();
        DicTableBean dicTable = null;

        /* 辞書名の変更 */
        if (!this.getName().isEmpty()) {
            try {
                dicTable = (DicTableBean) session.get(DicTableBean.class, this.getId());

                if (dicTable == null) {
                    dicTable = new DicTableBean();
                    dicTable.setDicId(this.getId());
                    // TODO 20131203
                    // UserDicではParentIdはnullでよい
                    // dicTable.setParentId(String.valueOf(this.getParentId()));
                    dicTable.setDicName(this.getName());
                    dicTable.setDicFileName(this.getFileName());
                    dicTable.setDicType(this.getDicType().getIntValue());
                    dicTable.setCategoryId(getDicCategory().getId());
                    Date nowDate = new Date();
                    dicTable.setDate(nowDate);
                    dicTable.setInactive(false);
                    Date createTime = getCreationTime();
                    if (createTime == null) {
                        createTime = nowDate;
                    }
                    dicTable.setCreationTime(createTime);
                }
                dicTable.setDicName(this.getName());
                dicTable.setDicFileName(this.getFileName());
                /* トランザクション開始 */
                session.beginTransaction();

                session.save(dicTable);
                session.flush();

                /* トランザクションコミット */
                session.getTransaction().commit();

            } catch (HibernateException e) {
                return false;

            } finally {
                if (session.getTransaction().isActive()) {
                    /* トランザクションロールバック */
                    session.getTransaction().rollback();
                }
            }

        } else {
            return false;
        }

        if (this.getDicType().getIntValue() == DicType.CATEGORY.getIntValue()) {
            try {

                dicTable = (DicTableBean) session.get(DicTableBean.class, getId());
                if (dicTable == null) {
                    dicTable = new DicTableBean();
                    dicTable.setDicId(getId());
                }
                dicTable.setCategoryId(getDicCategory().getId());
                /* トランザクション開始 */
                session.beginTransaction();

                session.save(dicTable);
                session.flush();
                /* トランザクションコミット */
                session.getTransaction().commit();

            } catch (HibernateException e) {
                return false;

            } finally {
                if (session.getTransaction().isActive()) {
                    /* トランザクションロールバック */
                    session.getTransaction().rollback();
                }
            }
        }
        if (!bRecords)
            return true;
        boolean execute = false;
        List<ITerm> addTermList = new ArrayList<ITerm>();
        List<ITerm> updTermList = new ArrayList<ITerm>();
        String jumanBase;
        Date lasted = null;

        /* 登録対象存在チェック */
        if (items == null) {
            return false;
        }

        /* 辞書のタイプをチェック */
        if (this.getDicType().getIntValue() == 0) {
            /* JUMAN辞書は処理対象外 */
            return true;
        }

        /* データ振り分け(エラーアイテムは保存しないように無視している) */
        monitor.subTask("データチェック"); //$NON-NLS-1$
        for (ITerm term : items) {
            if (term.getId() != IDicItem.UNSAVED_ID) {
                /* 既存データの場合 */
                if (term.isDirty() && !term.isError()) {
                    jumanBase = createJumanFormat(term);
                    term.setJumanBase(jumanBase);
                    updTermList.add(term);
                }
            } else {
                /* 新規データの場合 */
                if (!term.isError()) {
                    jumanBase = createJumanFormat(term);
                    term.setJumanBase(jumanBase);
                    addTermList.add(term);
                }
            }
        }
        monitor.worked(1);

        try {
            if (addTermList.size() == 0) {
                /* 新規追加がない場合 */
                /* DB上の辞書IDに対する更新日付をチェック */

                dicTable = (DicTableBean) session.get(DicTableBean.class, this.getId());
                lasted = dicTable.getDate();

                if (lasted == null) {
                    throw new HibernateException("システムエラーが発生しました。"); //$NON-NLS-1$
                } else if (lasted.compareTo(this.getLasted()) != 0) {
                    /* 更新日が取得できなかった場合、DB上と同期がとれていない状態 */
                    throw new HibernateException("辞書情報はすでに更新されています。最新を取得してください。"); //$NON-NLS-1$
                }

            }
            /* 用語登録処理 */
            if (addTermList.size() > 0) {
                monitor.subTask("用語登録"); //$NON-NLS-1$
                for (ITerm term : addTermList) {
                    /* DicCommonDao.insertDicCommon(term, this.getId()); を置換する。 */

                    /* NULL値変換 */
                    setDataValue(term);

                    DicCommonBean dicCommon = new DicCommonBean();
                    dicCommon.setName(term.getValue());
                    dicCommon.setReading(term.getReading());
                    dicCommon.setPartId(term.getTermPart().getIntValue());
                    dicCommon.setClassId(term.getTermClass().getIntValue());
                    dicCommon.setCformId(term.getCform().getIntValue());
                    dicCommon.setDicId(this.getId());
                    dicCommon.setInactive(false);
                    dicCommon.setJumanBase(term.getJumanBase());

                    try {
                        /* トランザクション開始 */
                        session.beginTransaction();

                        session.save(dicCommon);
                        session.flush();

                        /* トランザクションコミット */
                        session.getTransaction().commit();

                    } catch (HibernateException e) {
                        throw new HibernateException("用語登録処理にてエラーが発生しました。"); //$NON-NLS-1$

                    } finally {
                        if (session.getTransaction().isActive()) {
                            /* トランザクションロールバック */
                            session.getTransaction().rollback();
                        }
                    }

                    try {

                        String getIdSql = "select max(itemId) from DicCommonBean"; //$NON-NLS-1$

                        itemsForSearch.remove(term.getId());
                        final ITerm finalTerm = term;

                        // TODO 20131030 jdbc版ではCommonDao.executeWithReturnを使用
                        int maxItemId = 0;
                        try {
                            maxItemId = (int) (session.createQuery(getIdSql).uniqueResult());
                            finalTerm.setId(maxItemId);
                        } catch (HibernateException e) {
                            e.printStackTrace();
                        }
                        itemsForSearch.put(term.getId(), term);
                        term.setDirty(false);
                    } catch (HibernateException e) {
                        e.printStackTrace();
                    }
                }
                execute = true;
            }
            monitor.worked(1);

            /* 用語更新処理 */
            if (updTermList.size() > 0) {
                monitor.subTask("用語更新"); //$NON-NLS-1$
                for (ITerm term : updTermList) {
                    /* DicCommonDao.updateDicCommon(term); を置換する。 */

                    /* NULL値変換 */
                    setDataValue(term);

                    DicCommonBean dicCommon = (DicCommonBean) session.get(DicCommonBean.class, term.getId());

                    if (dicCommon == null) {
                        dicCommon = new DicCommonBean();
                        dicCommon.setItemId(term.getId());
                        dicCommon.setDicId(this.getId());
                        dicCommon.setInactive(false);
                    }
                    dicCommon.setName(term.getValue());
                    dicCommon.setReading(term.getReading());
                    dicCommon.setPartId(term.getTermPart().getIntValue());
                    dicCommon.setClassId(term.getTermClass().getIntValue());
                    dicCommon.setCformId(term.getCform().getIntValue());
                    dicCommon.setJumanBase(term.getJumanBase());
                    try {
                        /* トランザクション開始 */
                        session.beginTransaction();

                        session.save(dicCommon);
                        session.flush();

                        /* トランザクションコミット */
                        session.getTransaction().commit();

                    } catch (HibernateException e) {
                        throw new HibernateException("用語更新処理にてエラーが発生しました。"); //$NON-NLS-1$

                    } finally {
                        if (session.getTransaction().isActive()) {
                            /* トランザクションロールバック */
                            session.getTransaction().rollback();
                        }
                    }

                    term.setDirty(false);
                }
                execute = true;
            }
            monitor.worked(1);

            /* 用語削除 */
            if (delItems.size() > 0) {
                monitor.subTask("用語削除"); //$NON-NLS-1$
                for (IDicItem item : delItems) {
                    ITerm term = (ITerm) item;
                    if (term.getId() > 0) {
                        /* DicCommonDao.deleteDicCommon(term); を置換する。 */

                        /* NULL値変換 */
                        setDataValue(term);

                        DicCommonBean dicCommon = (DicCommonBean) session.get(DicCommonBean.class, term.getId());
                        if (dicCommon == null) {
                            dicCommon = new DicCommonBean();
                            dicCommon.setName(term.getValue());
                            dicCommon.setReading(term.getReading());
                            dicCommon.setPartId(term.getTermPart().getIntValue());
                            dicCommon.setClassId(term.getTermClass().getIntValue());
                            dicCommon.setCformId(term.getCform().getIntValue());
                            dicCommon.setDicId(this.getId());
                            dicCommon.setJumanBase(term.getJumanBase());
                            dicCommon.setItemId(term.getId());
                        }
                        dicCommon.setInactive(true);
                        try {
                            /* トランザクション開始 */
                            session.beginTransaction();

                            session.save(dicCommon);
                            session.flush();

                            /* トランザクションコミット */
                            session.getTransaction().commit();

                        } catch (HibernateException e) {
                            /* TODO:例外？ */
                            // throw new SQLException("用語更新処理にてエラーが発生しました。");

                        } finally {
                            if (session.getTransaction().isActive()) {
                                /* トランザクションロールバック */
                                session.getTransaction().rollback();
                            }
                        }
                    }
                }
                execute = true;
                delItems.clear();
            }
            monitor.worked(1);

            if (execute == false)
                return true;

            /* 更新日時の更新はsuperクラスでやっている */
            dirtyItems.clear();
            monitor.done();
            return true;

        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * ユーザ辞書UPDATE 辞書IDに紐づく情報をDBより取得し、メモリへ展開
     * 
     */
    @Override
    public boolean update() {
        super.update();

        try {
            List<TextItem> categoryList = ((IoService) IoService.getInstance()).getCategorys();

            /* SQL生成 */
            String strSQL = "FROM DicTableBean WHERE dicId = :DicId AND inactive = false"; //$NON-NLS-1$
            Session session = IoService.getInstance().getSession();
            try {
                @SuppressWarnings("unchecked")
                List<DicTableBean> dicTable = session.createQuery(strSQL).setInteger("DicId", this.getId()) //$NON-NLS-1$
                        .list();
                for (DicTableBean rs : dicTable) {
                    this._name = rs.getDicName();
                    this._fileName = rs.getDicFileName();
                    if (rs.getCategoryId() != null) {
                        for (TextItem item : categoryList) {
                            if (rs.getCategoryId().intValue() == item.getId()) {
                                this._dicCategory = item;
                                break;
                            }
                        }
                    }
                    this._dicType = DicType.valueOf(rs.getDicType());
                    this._creationTime = rs.getCreationTime();
                    this._lasted = rs.getDate();

                    /* 辞書に紐づく用語の再取得 */
                    if (!updateRecords()) {
                        throw new HibernateException("用語再取得時にエラーが発生しました。"); //$NON-NLS-1$
                    }

                }
                return true;
            } catch (HibernateException e) {
                throw e;
            }

        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * エクスポート
     * 
     * @param path
     * 
     */
    @Override
    protected void exportDicDam(String path, String encoding) {

        List<String> list = new ArrayList<String>();
        try {
            /* DIC_IDをキーにデータを取得 */
            /* DicCommonDao.getJumanBase(_id); を置換する。 */
            String strSQL = "From DicCommonBean where inactive = false And dicId = :DicId"; //$NON-NLS-1$
            Session session = IoService.getInstance().getSession();

            try {
                @SuppressWarnings("unchecked")
                List<DicCommonBean> dicCommon = session.createQuery(strSQL).setInteger("DicId", _id) //$NON-NLS-1$
                        .list();
                for (DicCommonBean rs : dicCommon) {
                    String str = rs.getJumanBase();
                    list.add(str);
                }
            } catch (HibernateException e) {
                throw e;
            }
            export(path, _fileName, list, encoding); /*
                                                      * ファイルのパス・ファイル名・jumanBaseデータ
                                                      */
        } catch (HibernateException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean updateRecords() {
        delItems.clear(); /* itemsが更新されるので、削除マークアイテムはクリアされるべき by Morishima */
        dirtyItems.clear();

        /* DIC_IDをキーにデータを取得 */
        /* DicCommonDao.getTermInfo(this.getId()); を置換する。 */
        String strSQL = "FROM DicCommonBean WHERE inactive = false AND dicId = :DicId"; //$NON-NLS-1$
        try {
            /* 辞書に紐づく用語を取得 */
            Session session = IoService.getInstance().getSession();

            try {
                @SuppressWarnings("unchecked")
                List<DicCommonBean> dicCommon = session.createQuery(strSQL).setInteger("DicId", this.getId()) //$NON-NLS-1$
                        .list();
                /* 取得結果編集 */
                /*
                 * ITerm#equalsが実装され、インスタンスが違っても正確に比較できるようになったため、毎回newしている
                 */
                if (items == null) {
                    items = new ArrayList<ITerm>();
                    itemsForSearch = new TreeMap<Integer, IDicItem>();
                } else {
                    items.clear();
                    itemsForSearch.clear();
                }

                for (DicCommonBean rs : dicCommon) {
                    int itemId = rs.getItemId();
                    ITerm term;

                    term = new Term(rs.getName(), rs.getReading(), TermPart.valueOf(rs.getPartId()).getName(), TermClass.valueOf(rs.getClassId()).getName(),
                            TermCForm.valueOf(rs.getCformId()).getName(), rs.getJumanBase());
                    items.add(term); /* bufferを使わずに直で */

                    /* TERM_ID を設定 */
                    term.setId(itemId);
                    ((Term) term).setDicId(this.getId());
                    /* term.setDirty(false)はコンストラクタで行われる */
                    itemsForSearch.put(itemId, term);
                }
                setDirty(false);
            } catch (HibernateException e) {
                throw e;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 辞書一覧データ登録
     * 
     * @param dicName
     * @param strExecuteSQL
     * @return 辞書ID
     * @throws HibernateException
     */
    public static int addDicTableData(String dicName, DicTableBean dicTable) throws HibernateException {

        /* 辞書一覧へ一般辞書を作成 */
        String errInfo = checkDicTable(dicName);
        if (errInfo != null) {
            throw new HibernateException(errInfo);
        }

        Session session = IoService.getInstance().getSession();

        try {
            /* トランザクション開始 */
            session.beginTransaction();

            session.save(dicTable);
            session.flush();

            /* トランザクションコミット */
            session.getTransaction().commit();

        } catch (HibernateException e) {
            e.printStackTrace();
            throw new HibernateException("システムエラーが発生しました。(辞書一覧登録)"); //$NON-NLS-1$

        } finally {
            if (session.getTransaction().isActive()) {
                /* トランザクションロールバック */
                session.getTransaction().rollback();
            }
        }

        /**
         * 辞書IDの取得
         */
        int dicId = 0;
        String dicIdHql = "FROM DicTableBean WHERE dicName = :dicName"; //$NON-NLS-1$
        try {
            @SuppressWarnings("unchecked")
            List<DicTableBean> dicTables = session.createQuery(dicIdHql).setString("dicName", dicName) //$NON-NLS-1$
                    .list();

            if (dicTables.size() > 0) {
                dicId = dicTables.get(0).getDicId();
            }
        } catch (HibernateException e) {
            dicId = -1;
        }

        return dicId;
    }


    @Override
    public int getItemCount() {
        if (items == null) {
            return getItemCount("DicCommonBean", getId(), false); //$NON-NLS-1$
        }
        return items.size();
    }


    /**
     * @param tableName
     * @param dicId
     * @param includeDeleted
     * @return
     */
    private static int getItemCount(String tableName, int dicId, boolean includeDeleted) {

        // TODO 20131105 元々はDicCommonDaoで実装（SynonymDic等のjdbc版で使用している箇所での使用状況は不明）

        StringBuilder sql = new StringBuilder("Select count(*) from " + tableName + " "); //$NON-NLS-1$ //$NON-NLS-2$
        sql.append("where dicId= :DicId "); //$NON-NLS-1$
        if (!includeDeleted) {
            sql.append("and inactive=false "); //$NON-NLS-1$
        }

        Session session = IoService.getInstance().getSession();

        // TODO 20131030 jdbc版ではCommonDao.executeWithReturnを使用

        try {
            int resultCnt = (int) ((long) session.createQuery(sql.toString()).setInteger("DicId", dicId) //$NON-NLS-1$
                    .uniqueResult());
            return resultCnt;
        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return 0;
    }


    @Override
    public List<?> getItemsPaging(int page, int limit) {
        List<ITerm> createItems = new ArrayList<ITerm>();
        /* 変更・新規をチェック */
        if (items != null) {
            for (ITerm item : items) {
                if (item.isDirty()) {
                    if (item.getId() == Term.UNSAVED_ID) {
                        /* 新規を保持 */
                        createItems.add(item);
                    } else {
                        /* 変更を保持 */
                        ITerm dItem = dirtyItems.get(item.getId());
                        if (dItem == null) {
                            dirtyItems.put(item.getId(), item);
                        }
                    }
                }
            }
        } else {
            updateRecords();
        }

        Session session = IoService.getInstance().getSession();
        try {
            /* 辞書に紐づく用語を取得 */
            /* DIC_IDをキーにデータを取得 */
            String strSQL = getTermInfo(this.getId(), (page - 1) * limit, limit, getSortField(), isAscending(), getFilters(), false, isAcceptOrNot());

            /* SQL実行 */
            @SuppressWarnings("unchecked")
            List<Object[]> dicCommonList = session.createSQLQuery(strSQL).list();

            List<ITerm> buf = new ArrayList<ITerm>();

            /* 取得結果編集 */
            for (Object[] rs : dicCommonList) {
                int itemId = rs[0] == null ? 0 : Integer.parseInt(rs[0].toString());
                String name = (String) rs[1];
                String reading = (String) rs[2];
                int partId = rs[3] == null ? 0 : Integer.parseInt(rs[3].toString());
                int classId = rs[4] == null ? 0 : Integer.parseInt(rs[4].toString());
                int cFormId = rs[5] == null ? 0 : Integer.parseInt(rs[5].toString());
                String jumanBase = (String) rs[6];

                ITerm term = getItem(itemId);
                if (term == null) {
                    if (isDeleted(itemId)) {
                        /*
                         * 削除されたアイテムだったら、新しく作らない 削除済みは何もしない
                         * itemsForSearchにも追加しなくていいので、continue
                         */
                        continue;
                    } else {
                        term = new Term(name, reading, TermPart.valueOf(partId).getName(), TermClass.valueOf(classId).getName(), TermCForm.valueOf(cFormId)
                                .getName(), jumanBase);
                        buf.add(term);
                    }
                } else {
                    ITerm dItem = dirtyItems.get(itemId);
                    if (dItem != null) {
                        /* 変更済みレコードは、DBのデータで上書きせず、元のデータを入れる。 */
                        buf.add(dItem);
                        continue; /* clearDirtyされたくないので Morishima */
                    }

                    else {
                        term.setValue(name);
                        term.setReading(reading);
                        term.setTermPart(TermPart.valueOf(partId));
                        term.setTermClass(TermClass.valueOf(classId));
                        term.setCform(TermCForm.valueOf(cFormId));
                        buf.add(term);
                    }
                }

                /* TERM_ID を設定 */
                itemsForSearch.remove(term.getId());
                term.setId(itemId);
                itemsForSearch.put(itemId, term);
                ((Term) term).setDicId(this.getId());
                term.setDirty(false);

            }
            /* 新規分を付与 */
            if (!createItems.isEmpty()) {
                buf.addAll(createItems);
            }

            return buf;
        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return new ArrayList<Object>(0);

    }


    private boolean isDeleted(int itemId) {
        for (IDicItem item : delItems) {
            if (item.getId() == itemId) {
                return true;
            }
        }
        return false;
    }


    @Override
    public ITerm getItem(int id) {
        if (items == null) {
            updateRecords();
        }
        return (ITerm) itemsForSearch.get(id);
    }


    @Override
    public List<ITerm> findItems(String value, String reading, TermPart termPart, TermClass termClass, TermCForm cform, boolean equals) {

        // TODO 20131105 このメソッドはjdbc版では呼び出し階層での確認では未使用となっている。

        // TODO 20131030 jdbc版ではCommonDao.executeWithReturnを使用

        /* Memo 今は未使用のメソッドです。使用する段階でこのassertを除去してください */
        assert false;

        Term searchTerm = new Term(value, reading, termPart, termClass, cform, ""); //$NON-NLS-1$
        String strSQL = findTerms(searchTerm, equals);

        Session session = IoService.getInstance().getSession();
        try {
            /* SQL実行 */
            @SuppressWarnings("unchecked")
            List<DicCommonBean> dicCommon = session.createQuery(strSQL).list();
            List<ITerm> ret = new ArrayList<ITerm>();
            for (DicCommonBean rs : dicCommon) {
                int id = rs.getItemId();
                String value0 = rs.getName();
                String reading0 = rs.getReading();
                TermPart termPart0 = TermPart.valueOf(rs.getPartId());
                TermClass termClass0 = TermClass.valueOf(rs.getClassId());
                TermCForm cform0 = TermCForm.valueOf(rs.getCformId());
                int dicId = rs.getDicId();
                String jumanBase0 = rs.getJumanBase();
                Term term = new Term(value0, reading0, termPart0, termClass0, cform0, jumanBase0);
                term.setId(id);
                term.setDicId(dicId);
                ret.add(term);
            }
            return ret;
        } catch (HibernateException e) {
            e.printStackTrace();
            /* 検索失敗 */
            return new ArrayList<ITerm>(0);
        }
    }


    /**
     * 単語を検索するSQLを作成する
     * 
     * @param searchTerm
     * @param equals
     * @return
     */
    private static String findTerms(Term searchTerm, boolean equals) {

        // TODO 20131105 このメソッドはjdbc版では呼び出し階層での確認では未使用となっている
        // メソッドfindItems()でのみ使用されている。

        String comp = equals ? "=" : " like "; //$NON-NLS-1$ //$NON-NLS-2$

        StringBuilder buf = new StringBuilder(256);
        buf.append("From DicCommonBean "); //$NON-NLS-1$

        StringBuilder where = new StringBuilder(256).append("Where inactive=false And "); //$NON-NLS-1$
        if (!"".equals(searchTerm.getValue())) { //$NON-NLS-1$
            where.append("name").append(comp).append(" ").append(searchTerm.getValue()).append(" And "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        if (!"".equals(searchTerm.getReading())) { //$NON-NLS-1$
            where.append("reading").append(comp).append(" ").append(searchTerm.getReading()).append(" And "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        if (!TermPart.NONE.equals(searchTerm.getTermPart())) {
            where.append("partId=").append(searchTerm.getTermPart().getIntValue()).append(" And "); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (!TermClass.NONE.equals(searchTerm.getTermClass())) {
            where.append("classId=").append(searchTerm.getTermClass().getIntValue()).append(" And "); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (!TermCForm.NONE.equals(searchTerm.getCform())) {
            where.append("cformId=").append(searchTerm.getCform().getIntValue()).append(" And "); //$NON-NLS-1$ //$NON-NLS-2$
        }
        where.delete(where.length() - 4, where.length());
        buf.append(where);
        return buf.toString();
    }


    @Override
    public void addItem(IDicItem item) {
        super.addItem(item);
        if (item.getId() != IDicItem.UNSAVED_ID) {
            itemsForSearch.put(item.getId(), item);
        }
    }


    @Override
    public void removeItem(IDicItem item) {
        if (items == null) {
            updateRecords();
        }
        super.removeItem(item);
        if (item.getId() != IDicItem.UNSAVED_ID) {
            itemsForSearch.remove(item.getId());
        }
    }


    /**
     * 条件にあてはまるレコードの件数を返す。
     * 
     * @param dicId
     * @param offset
     *            データ取得開始位置。ヒットしたレコードのうち、先頭何件のデータを飛ばすか指定する。
     * @param limit
     *            データの最大取得件数。-1を指定すると、pageの値にかかわらずすべてのデータを取得する。
     * @param filters
     * @param filterEquals
     *            文字列の判定をlikeで行うならfalse
     * @param acceptOrNot
     *            TODO 条件にあてはまるレコードを取得するならtrue。条件にあてはまるレコード以外を取得するならfalse。
     *            ただしまだ機能していない。
     * @return sql文
     */
    private static String getTermCount(int dicId, int offset, int limit, Map<UserDicFieldType, Object[]> filters, boolean filterEquals, boolean acceptOrNot) {

        // DicUserDaoに配置されていたメソッド

        String select = "Select count(*) "; //$NON-NLS-1$
        return getFilterSortedSql(dicId, offset, limit, null, true, filters, filterEquals, 1).insert(0, select).toString();
    }


    /**
     * 用語情報取得（ページング、ソート）
     * 
     * @param dicId
     * @param offset
     *            データ取得開始位置。ヒットしたレコードのうち、先頭何件のデータを飛ばすか指定する。
     * @param limit
     *            データの最大取得件数。-1を指定すると、pageの値にかかわらずすべてのデータを取得する。
     * @param sortType
     *            nullならITEM_IDでソート
     * @param asc
     *            昇順ならtrue
     * @param filters
     * @param filterEquals
     *            文字列の判定をlikeで行うならfalse
     * @param acceptOrNot
     *            TODO 条件にあてはまるレコードを取得するならtrue。条件にあてはまるレコード以外を取得するならfalse。
     *            ただしまだ機能していない。
     * @return sql文
     */
    private static String getTermInfo(int dicId, int offset, int limit, UserDicFieldType sortType, boolean asc, Map<UserDicFieldType, Object[]> filters,
            boolean filterEquals, boolean acceptOrNot) {

        // DicUserDaoに配置されていたメソッド

        String select = "Select ITEM_ID, NAME, READING, PART_ID, CLASS_ID, CFORM_ID, JUMAN_BASE "; //$NON-NLS-1$
        // String select = "";
        return getFilterSortedSql(dicId, offset, limit, sortType, asc, filters, filterEquals, 0).insert(0, select).toString();
    }


    /**
     * Select句以外のSQL文を返す。
     */
    private static StringBuffer getFilterSortedSql(int dicId, int offset, int limit, UserDicFieldType sortType, boolean asc,
            Map<UserDicFieldType, Object[]> filters, boolean filterEquals, int cntFlag) {

        // DicUserDaoに配置されていたメソッド

        StringBuffer strSQL = new StringBuffer(2048);

        boolean isLabelSort = UserDicFieldType.LABEL.equals(sortType);

        /* ラベルでのフィルタがかかっているか判定 */
        boolean isLabelFilter = false;
        for (Entry<UserDicFieldType, Object[]> e : filters.entrySet()) {
            if (UserDicFieldType.LABEL.equals(e.getKey())) {
                isLabelFilter = true;
                break;
            }
        }

        if (isLabelSort || isLabelFilter) {
            strSQL.append("From (Select distinct t.ITEM_ID ITEM_ID, t.NAME, t.READING, t.PART_ID, t.CLASS_ID, t.CFORM_ID, t.JUMAN_BASE "); //$NON-NLS-1$
            strSQL.append("From DIC_COMMON t left join (rel_common_label r inner join dic_label l on r.label_id=l.label_id) "); //$NON-NLS-1$
            strSQL.append("on t.item_id=r.item_id "); //$NON-NLS-1$
        } else {
            strSQL.append("FROM DIC_COMMON t "); //$NON-NLS-1$
        }
        strSQL.append("WHERE t.INACTIVE=false AND t.DIC_ID=").append(dicId).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
        if (!DicType.JUMAN.equals(IoActivator.getDicUtil().getDicType(dicId))) {
            /* ユーザ辞書取得において、JUMAN辞書以外は名詞のみ取得 */
            strSQL.append(" AND t.PART_ID =").append(TermPart.NOUN.getIntValue()).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
        }
        /* フィルタ条件指定 */
        StringBuffer otherFilter = new StringBuffer(1024);
        for (Entry<UserDicFieldType, Object[]> e : filters.entrySet()) {
            String field = null;
            String comp = "="; //$NON-NLS-1$
            String qt = ""; //$NON-NLS-1$
            switch (e.getKey()) {
            case HEADER:
                field = "name"; //$NON-NLS-1$
                comp = filterEquals ? "=" : " like "; //$NON-NLS-1$ //$NON-NLS-2$
                qt = "'"; //$NON-NLS-1$
                break;
            case READING:
                field = "reading"; //$NON-NLS-1$
                comp = filterEquals ? "=" : " like "; //$NON-NLS-1$ //$NON-NLS-2$
                qt = "'"; //$NON-NLS-1$
                break;
            case LABEL:
                field = "label_name"; //$NON-NLS-1$
                qt = "'"; //$NON-NLS-1$
                break;
            case PART:
                field = "part_id"; //$NON-NLS-1$
                break;
            case CLASS:
                field = "class_id"; //$NON-NLS-1$
                break;
            case CFORM:
                field = "cform_id"; //$NON-NLS-1$
                break;
            default:
                break;
            }
            if (e.getValue().length > 0) {
                otherFilter.append("And ("); //$NON-NLS-1$
                for (Object o : e.getValue()) {
                    otherFilter.append(field).append(comp).append(qt).append(o).append(qt).append(" Or "); //$NON-NLS-1$
                }
                otherFilter.delete(otherFilter.length() - 4, otherFilter.length()).append(")"); //$NON-NLS-1$
            }
        }
        if (otherFilter.length() > 0) {
            strSQL.append(otherFilter);
        }

        /* TODO 20131204 */
        /* [Select count(*) ]の場合、[ORDER BY t.ITEM_ID ]が付加された状態では例外エラーが発生する事象への対策 */
        if (cntFlag == 0) {
            /* ソート条件指定 */
            if ((sortType == null) || UserDicFieldType.NONE.equals(sortType)) {
                strSQL.append("ORDER BY t.ITEM_ID "); //$NON-NLS-1$
            } else if (isLabelSort) {
                strSQL.append("ORDER BY label_name "); //$NON-NLS-1$
            } else if (sortType != UserDicFieldType.NONE) {
                strSQL.append("ORDER BY t.").append(sortType.getName()).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (!asc) {
                strSQL.append("DESC "); //$NON-NLS-1$
            }

            if (limit >= 0) {
                strSQL.append("LIMIT ").append(offset).append(",").append(limit).append(" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            if (isLabelSort || isLabelFilter) {
                strSQL.append(") sub "); //$NON-NLS-1$
            }
        }

        return strSQL;
    }


    /**
     * null値変換
     * 
     * @param term
     */
    private static void setDataValue(ITerm term) {

        // DicCommonDaoに配置されていたメソッド

        if (term.getTermPart() == null) {
            /* 品詞がNULLの場合、未定義語に対応する値を指定 */
            term.setTermPart(TermPart.UNKNOWN);
        }
        if (term.getTermClass() == null) {
            /* 品詞詳細がNULLの場合、””に対応する値を指定 */
            term.setTermClass(TermClass.NONE);
        }
        if (term.getCform() == null) {
            /* 活用型がNULLの場合、””に対応する値を指定 */
            term.setCform(TermCForm.NONE);
        }
    }
}
