/**
 * @version $Id: DicImportCommiter.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/11/11 9:18:27
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.command;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.bean.DicTableBean;
import com.tida_okinawa.corona.io.dam.hibernate.DicFactory;
import com.tida_okinawa.corona.io.dam.hibernate.FlucDic;
import com.tida_okinawa.corona.io.dam.hibernate.IoService;
import com.tida_okinawa.corona.io.dam.hibernate.LabelDic;
import com.tida_okinawa.corona.io.dam.hibernate.SynonymDic;
import com.tida_okinawa.corona.io.dam.hibernate.UserDic;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDepend;
import com.tida_okinawa.corona.io.model.dic.IDependDic;
import com.tida_okinawa.corona.io.model.dic.IDependSub;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.model.dic.PatternType;
import com.tida_okinawa.corona.io.model.dic.TermPart;
import com.tida_okinawa.corona.io.model.dic.abstraction.AbstractPrimary;
import com.tida_okinawa.corona.io.model.dic.impl.FlucItem;
import com.tida_okinawa.corona.io.model.dic.impl.FlucSubItem;
import com.tida_okinawa.corona.io.model.dic.impl.LabelItem;
import com.tida_okinawa.corona.io.model.dic.impl.SynonymItem;
import com.tida_okinawa.corona.io.model.dic.impl.SynonymSubItem;
import com.tida_okinawa.corona.io.model.dic.impl.Term;
import com.tida_okinawa.corona.io.util.CoronaIoUtils;


/**
 * 辞書インポートのコミット処理を行う.
 * 
 * @author shingo-takahashi
 * 
 */
class DicImportCommiter {

    private static List<String> errs = new ArrayList<String>();


    /**
     * @param dic
     *            コミットする辞書
     * @param items
     *            コミットする辞書アイテム
     * @param type
     *            辞書のタイプ。ゆらぎ辞書({@link DicType#FLUC})、同義語辞書(
     *            {@link DicType#SYNONYM})の場合は必須。
     * @param monitor
     *            進捗確認用モニター
     * @param ignoreLabel
     *            ラベル情報インポート有無フラグ
     * @return
     *         成功時　true
     *         失敗時　false
     * @throws SQLException
     *             辞書登録処理で何かしらのエラーが起きた
     */
    public static boolean doCommit(ICoronaDic dic, List<Object> items, DicType type, IProgressMonitor monitor, Boolean ignoreLabel) throws SQLException {
        if (dic instanceof IUserDic) {
            return doCommit((IUserDic) dic, items, monitor, ignoreLabel);
        }
        if (dic instanceof IDependDic) {
            return doCommit((IDependDic) dic, items, type, monitor, ignoreLabel);
        }
        if (dic instanceof IPatternDic) {
            return doCommit((IPatternDic) dic, items, monitor);
        }
        return false;
    }


    /**
     * ゆらぎ・同義語辞書のインポート処理を行う
     * 
     * @param dic
     *            ゆらぎ辞書
     * @param items
     *            追加辞書アイテム
     * @param type
     *            辞書タイプ
     * @param monitor
     *            進捗確認用モニター
     * @param ignoreLabel
     *            ラベル辞書インポート有無フラグ
     * @return
     *         成功時　true
     *         失敗時　false
     * @throws SQLException
     *             SQL失敗時エラー
     */
    public static boolean doCommit(IDependDic dic, List<Object> items, DicType type, IProgressMonitor monitor, Boolean ignoreLabel) throws SQLException {

        /* ユーザー辞書マップ */
        Map<String, IUserDic> userDics = new HashMap<String, IUserDic>();

        /* エラーオブジェクト */
        errs = new ArrayList<String>();

        /* ゆらぎ辞書追加アイテム */
        Map<String, Object> dependMap = new HashMap<String, Object>();

        /* ゆらぎ辞書を取得 */
        if (dic.getId() != ICoronaDic.UNSAVED_ID) {
            dic.updateRecords();
            for (IDicItem i : dic.getItems()) {
                IDepend v = (IDepend) i;
                ICoronaDic udic = IoActivator.getService().getDictionary(v.getMain().getComprehensionDicId());
                dependMap.put(udic.getName() + Messages.DicImportCommiter_comma + v.getMain().getKeyword(), v);
            }
        }

        /* レコード番号 */
        int recordCount = 0;

        /* 揺らぎ語アイテム保存用一時ID */
        int vidx = -1;

        for (Object obj : items) {
            recordCount++;

            /* 変数定義 */
            String[] strs = (String[]) obj;

            /* 辞書名が空の場合は入力辞書名を指定 */
            if (strs[DicIEConstants.USER_DIC_NAME].isEmpty()) {
                strs[DicIEConstants.USER_DIC_NAME] = dic.getName().substring(0, dic.getName().lastIndexOf(Messages.DicImportCommiter_dot))
                        + Messages.DicImportCommiter_udicWithDot;
            } else {
                /* 拡張子が含まれていない場合はデフォルトで.cdicを付与 */
                if (!strs[DicIEConstants.USER_DIC_NAME].contains(Messages.DicImportCommiter_dot)) {
                    strs[DicIEConstants.USER_DIC_NAME] = strs[DicIEConstants.USER_DIC_NAME] + Messages.DicImportCommiter_udicWithDot;
                }
            }
            String sDependDic = strs[DicIEConstants.USER_DIC_NAME];
            String sDependHeader = (strs[DicIEConstants.DEPEND_HEADER].isEmpty()) ? "" : strs[DicIEConstants.DEPEND_HEADER]; //$NON-NLS-1$
            String sDependReading = (strs[DicIEConstants.DEPEND_READING].isEmpty()) ? "" : strs[DicIEConstants.DEPEND_READING]; //$NON-NLS-1$
            String sDependPart = (strs[DicIEConstants.DEPEND_PART].isEmpty()) ? TermPart.NONE.getName() : strs[DicIEConstants.DEPEND_PART];
            // TODO 20131217 ゆらぎ・同義語辞書のインポート処理で「品詞細分類」（品詞詳細）と活用形の取得を追加する。
            String sDependClass = (strs[DicIEConstants.DEPEND_CLASS].isEmpty()) ? "" : strs[DicIEConstants.DEPEND_CLASS]; //$NON-NLS-1$
            String cformDefault = Messages.DicImportCommiter_zerSpace;
            String sDependCform = (strs[DicIEConstants.DEPEND_CFORM].isEmpty()) ? cformDefault : strs[DicIEConstants.DEPEND_CFORM];

            /* 辞書名が空の場合は入力辞書名を指定 */
            if (strs[DicIEConstants.PARENT_USER_DIC_NAME].isEmpty()) {
                strs[DicIEConstants.PARENT_USER_DIC_NAME] = dic.getName().substring(0, dic.getName().lastIndexOf(Messages.DicImportCommiter_dot))
                        + Messages.DicImportCommiter_udicWithDot;
            } else {
                /* 拡張子が含まれていない場合はデフォルトで.cdicを付与 */
                if (!strs[DicIEConstants.PARENT_USER_DIC_NAME].contains(Messages.DicImportCommiter_dot)) {
                    strs[DicIEConstants.PARENT_USER_DIC_NAME] = strs[DicIEConstants.PARENT_USER_DIC_NAME] + Messages.DicImportCommiter_udicWithDot;
                }
            }
            String sParentDic = strs[DicIEConstants.PARENT_USER_DIC_NAME];
            String sParentHeader = (strs[DicIEConstants.SYNONYM].isEmpty()) ? "" : strs[DicIEConstants.SYNONYM]; //$NON-NLS-1$
            String sParentReading = (strs[DicIEConstants.SYNONYM_READING].isEmpty()) ? "" : strs[DicIEConstants.SYNONYM_READING]; //$NON-NLS-1$
            String sParentPart = (strs[DicIEConstants.SYNONYM_PART].isEmpty()) ? "" : strs[DicIEConstants.SYNONYM_PART]; //$NON-NLS-1$
            // TODO 20131217 ゆらぎ・同義語辞書のインポート処理で「品詞細分類」（品詞詳細）と活用形の取得を追加する。
            String sParentClass = (strs[DicIEConstants.SYNONYM_CLASS].isEmpty()) ? "" : strs[DicIEConstants.SYNONYM_CLASS]; //$NON-NLS-1$
            String sParentCform = (strs[DicIEConstants.SYNONYM_CFORM].isEmpty()) ? cformDefault : strs[DicIEConstants.SYNONYM_CFORM];

            /* CSVフォーマットチェック */
            if (strs.length != DicIEConstants.DEPEND_DIC_COLUMNS) {
                errs.add(Messages.bind(Messages.DicImportCommiter_errLogCsv,
                        new String[] { Integer.toString(recordCount), Integer.toString(DicIEConstants.DEPEND_DIC_COLUMNS) }));
            } else {

                /* 代表語PrimaryKey */
                StringBuilder sb = new StringBuilder();
                sb = sb.append(sParentDic).append(Messages.DicImportCommiter_comma).append(sParentHeader).append(Messages.DicImportCommiter_comma)
                        .append(sParentReading).append(Messages.DicImportCommiter_comma).append(sParentPart);
                String parentKey = sb.toString();

                /* 従属語PrimaryKey */
                sb.setLength(0);
                sb = sb.append(sDependDic).append(Messages.DicImportCommiter_comma).append(sDependHeader).append(Messages.DicImportCommiter_comma)
                        .append(sDependReading).append(Messages.DicImportCommiter_comma).append(sDependPart);
                String dependKey = sb.toString();

                if (sParentHeader.isEmpty() && sParentPart.isEmpty() && sParentReading.isEmpty()) {
                    /* 揺らぎ語のみ定義されている場合（代表語のみの構成） */

                    /* ユーザー辞書マップから揺らぎ語の辞書アイテムを取得 */
                    ITerm depentTerm = findTerm(userDics, recordCount, sDependDic, sDependHeader, sDependReading, sDependPart, sDependClass, sDependCform);
                    if (depentTerm == null) {
                        /* 返り値がNULLの場合はエラーであるためレコードをスキップ */
                        continue;
                    }

                    /* ゆらぎマップに存在しない場合は、ゆらぎ語を追加 */
                    IDepend depend = null;
                    if (dependMap.get(dependKey) == null) {
                        if (dic instanceof SynonymDic) {
                            /* 同義語辞書の場合 */
                            depend = DicFactory.getInstance().createSynonym(depentTerm);
                        } else {
                            /* 揺らぎ辞書の場合 */
                            depend = DicFactory.getInstance().createFluc(depentTerm);
                        }
                        dependMap.put(dependKey, depend);
                    }


                } else {
                    /* 代表語が定義されている場合（従属語＋代表語の構成） */

                    /* ユーザー辞書マップから代表語の辞書アイテムを取得 */
                    ITerm parentTerm = findTerm(userDics, recordCount, sParentDic, sParentHeader, sParentReading, sParentPart, sParentClass, sParentCform);
                    if (parentTerm == null) {
                        /* 返り値がNULLの場合はエラーであるためレコードをスキップ */
                        continue;
                    }

                    /* 代表語が定義されていない場合は、代表語を新たに作成 */
                    IDepend parentDepend = null;
                    if (dependMap.get(parentKey) == null) {
                        if (dic instanceof SynonymDic) {
                            /* 同義語辞書の場合 */
                            parentDepend = DicFactory.getInstance().createSynonym(parentTerm);
                        } else {
                            /* 揺らぎ辞書の場合 */
                            parentDepend = DicFactory.getInstance().createFluc(parentTerm);
                        }
                        dependMap.put(parentKey, parentDepend);
                    }

                    /* ユーザー辞書マップから従属語の辞書アイテムを取得 */
                    ITerm dependTerm = findTerm(userDics, recordCount, sDependDic, sDependHeader, sDependReading, sDependPart, sDependClass, sDependCform);
                    if (dependTerm == null) {
                        /* 返り値がNULLの場合はエラーであるためレコードをスキップ */
                        continue;
                    }

                    /* 従属語を代表語に従属 */
                    IDependSub subItem = null;
                    IDepend depend = (IDepend) dependMap.get(parentKey);
                    List<ITerm> termList = depend.getSub();
                    if (termList.contains(dependTerm)) {
                        errs.add(Messages.DicImportCommiter_duplicateDependData);
                        errs.add(Messages.bind(Messages.DicImportCommiter_duplicateParentWord, new String[] { sParentDic, sParentHeader, sParentReading,
                                sParentPart }));
                        errs.add(Messages.bind(Messages.DicImportCommiter_duplicateChildWord, new String[] { sDependDic, sDependHeader, sDependReading,
                                sDependPart }));
                        continue;
                    } else {
                        int subId = IDepend.UNSAVED_ID;
                        depend.setDirty(true);

                        vidx = vidx - 1;
                        if (dic instanceof SynonymDic) {/* 従属語アイテム構築し、代表語アイテムに追加 */
                            subItem = new SynonymSubItem(subId, dependTerm, depend);
                            ((SynonymItem) depend).getSubs().put(vidx, subItem);
                        }
                        if (dic instanceof FlucDic) {
                            subItem = new FlucSubItem(subId, dependTerm, depend);
                            ((FlucItem) depend).getSubs().put(vidx, subItem);
                        }
                    }
                }
            }
        }

        /* 従属語が紐づいていない代表語は登録しない */
        for (Iterator<Entry<String, Object>> it = dependMap.entrySet().iterator(); it.hasNext();) {
            Entry<String, Object> e = it.next();
            IDepend parentTerm = (IDepend) e.getValue();
            if (parentTerm.getSubs().isEmpty()) {
                errs.add(Messages.DicImportCommiter_errNoSubs);
                errs.add(Messages.bind(Messages.DicImportCommiter_errNoSubsData, new String[] { parentTerm.getMain().getValue(),
                        parentTerm.getMain().getReading(), parentTerm.getMain().getTermPart().getName(), "" })); //$NON-NLS-1$
                /* TODO このメッセージでてるのに実際は削除されてないですよ */
                it.remove();
            } else {

                /* 新規登録単語のみ追加、変更については考慮しない */
                if (parentTerm.getId() == IDepend.UNSAVED_ID)
                    dic.addItem(parentTerm);
            }
        }

        /* ログ出力 */
        if (errs.size() > 0) {
            CoronaIoUtils.setErrorLogs(IStatus.WARNING, Messages.DicImportCommiter_errLogDicCommiter, errs, null);
        }

        /* ユーザー辞書をコミット */
        for (Entry<String, IUserDic> dicMap : userDics.entrySet()) {

            /* 登録アイテムが0件の場合は辞書登録を行わない */
            if (!dicMap.getValue().getItems().isEmpty()) {
                List<Object> i = Arrays.asList(dicMap.getValue().getItems().toArray());
                doCommit(dicMap.getValue(), i, monitor, ignoreLabel);
                dic.addParentId(dicMap.getValue().getId());
                dicMap.getValue().updateRecords();
            }
        }

        /* 新規辞書作成 */
        if (dic.getId() == ICoronaDic.UNSAVED_ID && !dic.getItems().isEmpty()) {
            DicTableBean dicTableBean = insertDicTable(null, dic.getName(), "", 0, type.getIntValue()); //$NON-NLS-1$
            int dicId = UserDic.addDicTableData(dic.getName(), dicTableBean);
            dic.setId(dicId);
            ((IoService) IoActivator.getService()).addDictionary(dic);
        }

        dic.commit(new SubProgressMonitor(monitor, 1));
        dic.updateRecords();

        ((IoService) IoActivator.getService()).updateDictionarys();

        return true;
    }


    /**
     * ユーザー辞書マップ内の特定のユーザー辞書から辞書アイテムを取得する。
     * ユーザー辞書アイテムが存在しない場合は作成する。
     * 
     * @param userDics
     *            Map<String, IUserDic>のユーザー辞書マップ
     * @param recordCount
     *            レコード番号
     * @param dicName
     *            アイテムを取得するユーザー辞書
     * @param header
     *            見出し
     * @param reading
     *            読み
     * @param part
     *            品詞
     * @param termClass
     *            品詞詳細
     * @param cform
     *            活用形
     * @return
     *         辞書アイテム
     */
    public static ITerm findTerm(Map<String, IUserDic> userDics, int recordCount, String dicName, String header, String reading, String part, String termClass,
            String cform) {

        // TODO 20131217 ゆらぎ・同義語辞書のインポート処理で「品詞細分類」（品詞詳細）と活用形の取得を追加する。


        /* KeyWordを作成 */
        StringBuilder sb = new StringBuilder();
        sb = sb.append(header).append(Messages.DicImportCommiter_comma).append(reading).append(Messages.DicImportCommiter_comma).append(part);
        // TODO 20131217
        /* 「品詞細分類」（品詞詳細）をキーに追加する。 */
        sb = sb.append(Messages.DicImportCommiter_comma).append(termClass);
        String keyword = sb.toString();

        IUserDic userDic = null;
        if (userDics.get(dicName) == null) {
            /* ユーザー辞書マップに該当辞書が存在しない場合は追加 */
            userDic = (IUserDic) IoActivator.getService().getDictionary(dicName);
            if (userDic == null) {
                userDic = (IUserDic) DicFactory.getInstance().createUserDic(dicName, dicName, DicType.COMMON);
            }

            /* ユーザー辞書マップへ格納 */
            userDics.put(dicName, userDic);

        } else {
            /* ユーザー辞書マップから該当辞書を取得 */
            userDic = userDics.get(dicName);
        }

        /* 該当辞書がアイテムが存在しない場合は追加 */
        ITerm term = null;

        for (IDicItem item : userDic.getItems()) {
            ITerm search = (ITerm) item;
            String primaryKey = search.getKeyword();
            // TODO 20131217
            /* 「品詞細分類」（品詞詳細）をキーに追加する。 */
            primaryKey = primaryKey + "," + search.getTermClass().getName(); //$NON-NLS-1$

            if (primaryKey.equals(keyword)) {
                term = search;
                continue;
            }

        }

        if (term == null) {

            /* 新規用語を作成 */
            //term = DicFactory.getInstance().createTerm(header, reading, part, "", Messages.DicImportCommiter_zerSpace, Messages.DicImportCommiter_zerSpace); //$NON-NLS-1$
            term = DicFactory.getInstance().createTerm(header, reading, part, termClass, cform, Messages.DicImportCommiter_zerSpace);

            /* JUMAN辞書を生成 */
            term.setJumanBase(UserDic.createJumanFormat(term));
            /* 単語、読み、品詞に欠落があった場合はエラー表示 */
            if (term.getJumanBase().isEmpty()) {
                errs.add(Messages.bind(Messages.DicImportCommiter_errLogCsvFail, recordCount));
                return null;

            }
            userDic.addItem(term);
        }
        return term;
    }


    /**
     * ユーザー辞書用コミット処理
     * 
     * @param dic
     *            ユーザー辞書
     * @param items
     *            コミットする辞書のアイテム。<code>List&lt;String[]&gt;</code>または
     *            <code>List&lt;ITerm&gt;</code>であること。
     * @param monitor
     *            進捗ダイアログ管理用モニター
     * @param ignoreLabel
     *            ラベル情報インポート有無フラグ
     * @return
     *         true
     * @throws SQLException
     *             SQL失敗時エラー
     */
    public static boolean doCommit(IUserDic dic, List<Object> items, IProgressMonitor monitor, Boolean ignoreLabel) throws SQLException {
        monitor.beginTask(Messages.DicImportCommiter_monitorCommit, 2);
        monitor.subTask(Messages.DicImportCommiter_monitorUserDicCommit);

        /* ラベル辞書Map */
        Map<AbstractPrimary, Map<String, LabelItem>> labelDicMap = new HashMap<AbstractPrimary, Map<String, LabelItem>>();
        boolean bFlg = false;
        /* アイテムの型チェックで、処理を変更 */
        if ((items.size() > 0) && (items.get(0) instanceof IDicItem)) {
            bFlg = true;
        } else {
            errs = new ArrayList<String>();
            /* ユーザ辞書情報作成 */
            bFlg = makeUserDicList(items, dic, labelDicMap, ignoreLabel, errs);
            /* ログ出力 */
            if (errs.size() > 0) {
                CoronaIoUtils.setErrorLogs(IStatus.WARNING, Messages.bind(Messages.DicImportCommiter_errLogUserDicImport, new String[] { dic.getName() }),
                        errs, null);
            }
        }

        if (bFlg) {
            /* 辞書新規チェック */
            boolean bNewDic = (dic.getId() == -1) ? true : false;
            if (bNewDic) {
                /* 新規辞書の場合 */

                int dicType = dic.getDicType().getIntValue();
                String ext = dic.getName().substring(dic.getName().length() - 4);
                if (ext.equals("jdic")) { //$NON-NLS-1$
                    dicType = DicType.JUMAN.getIntValue();
                }

                DicTableBean dicTableBean = insertDicTable(null, dic.getName(), dic.getFileName(), dic.getDicCategory().getId(), dicType);
                int dicId = UserDic.addDicTableData(dic.getName(), dicTableBean);
                /* 辞書IDを設定 */
                dic.setId(dicId);
                /* 辞書をメモリへ追加 */
                ((IoService) IoActivator.getService()).addDictionary(dic);
            }
            /* ユーザ辞書コミット */
            dic.commit(new SubProgressMonitor(monitor, 1));
            monitor.worked(1);

            if (ignoreLabel == false) {
                /* ラベル辞書情報チェック */
                monitor.subTask(Messages.DicImportCommiter_monitorLabelDic);
                if (labelDicMap.size() > 0) {
                    /* ラベル辞書情報を登録 */
                    for (Entry<AbstractPrimary, Map<String, LabelItem>> entry : labelDicMap.entrySet()) {
                        Set<Integer> parents = new HashSet<Integer>();
                        LabelDic ldic = (LabelDic) entry.getKey().getItem();
                        if (ldic.getId() == ICoronaDic.UNSAVED_ID) {
                            /* 新規にラベル辞書を作成 */
                            parents.add(dic.getId());
                            int dicId = 0;
                            dicId = UserDic.addDicTableData(ldic.getName(), insertDicTable(parents, ldic.getName(), "", 0, DicType.LABEL.getIntValue())); //$NON-NLS-1$
                            /* 辞書IDを設定 */
                            ldic.setId(dicId);
                            /* 辞書をメモリへ追加 */
                            ((IoService) IoActivator.getService()).addDictionary(ldic);
                        } else {
                            /* 既存辞書の親辞書情報を取得し、編集 */
                            parents = ldic.getParentIds();
                            parents.add(dic.getId());
                        }
                        /* 親辞書ID設定 */
                        ldic.setParentIds(parents);
                        /* ラベル辞書コミット */
                        ldic.commit(new SubProgressMonitor(monitor, 1));
                        /* ラベル情報を再取得 */
                        ldic.updateRecords();
                    }
                }
            }
            monitor.worked(1);
        } else {
            CoronaIoUtils.setErrorLog(IStatus.ERROR, Messages.DicImportCommiter_errLogCsvAllRecord, null);
        }

        /**
         * dic の持つレコード (items) には、
         * 無効のため保存されなかったレコード(ex. 読みのない名詞)も入ったままになるため
         * ここでレコード一覧を更新しておく #629
         */
        dic.updateRecords();


        return true;
    }


    /**
     * パターン辞書を作成する
     * 
     * @param dic
     *            パターン辞書
     * @param importItems
     *            追加辞書アイテム
     * @param monitor
     *            進捗ダイアログ管理用モニター
     * @return true
     * @throws SQLException
     *             SQL実行時エラー
     */
    public static boolean doCommit(IPatternDic dic, List<Object> importItems, IProgressMonitor monitor) throws SQLException {

        /* エラーオブジェクト */
        errs = new ArrayList<String>();

        /* パターン分類検索マップ */
        Map<String, Integer> typeSearchMap = new HashMap<String, Integer>();
        for (PatternType type : PatternType.getPatternTypes()) {
            typeSearchMap.put(type.getPatternName(), type.getId());
        }

        /* パターン検索用マップ */
        Map<IPatternDic, Map<String, IPattern>> patternMap = new HashMap<IPatternDic, Map<String, IPattern>>();

        for (Object obj : importItems) {
            String[] str = (String[]) obj;

            /* 辞書名が空の場合は入力辞書名を指定 */
            if (str[0].isEmpty()) {
                str[0] = dic.getName().substring(0, dic.getName().lastIndexOf(Messages.DicImportCommiter_dot)) + Messages.DicImportCommiter_pdicWithDot;
            } else if (!str[0].contains(Messages.DicImportCommiter_dot)) {
                /* 拡張子を指定しない場合はデフォルトで.cdicを付与 */
                str[0] = str[0] + Messages.DicImportCommiter_pdicWithDot;
            }
            String dicName = str[0];
            String label = str[1];
            String text = str[2];
            String patternType = str[3];
            String parts = str[4];

            IPattern addItem = null;

            /* 一意キーを作成 */
            StringBuffer primaryKey = new StringBuffer();
            primaryKey.append(dicName).append(Messages.DicImportCommiter_comma).append(label).append(Messages.DicImportCommiter_comma).append(parts);

            IPatternDic patternDic = null;
            /* パターン検索用マップから辞書を取得 */
            for (Entry<IPatternDic, Map<String, IPattern>> repMap : patternMap.entrySet()) {
                if (repMap.getKey().getName().equals(dicName)) {
                    patternDic = repMap.getKey();
                    break;
                }
            }

            /* パターン検索用マップに存在しない為、新たに作成or取得 */
            if (patternDic == null) {
                ICoronaDic newDic = IoService.getInstance().getDictionary(dicName);

                Map<String, IPattern> tempMap = new HashMap<String, IPattern>();
                if (newDic == null) {

                    /* DB上にも存在しないため、パターン辞書を新たに作成 */
                    DicTableBean dicTableBean = insertDicTable(null, dicName, "", 0, DicType.PATTERN.getIntValue()); //$NON-NLS-1$

                    int dicId = UserDic.addDicTableData(dicName, dicTableBean);
                    newDic = IoActivator.getDicFactory().createPatternDic(dicName);
                    newDic.setId(dicId);
                    ((IoService) IoActivator.getService()).addDictionary(newDic);
                    patternDic = (IPatternDic) newDic;
                    patternMap.put(patternDic, tempMap);

                } else {

                    /* DB上に存在するため、取得 */
                    patternDic = (IPatternDic) newDic;

                    /* パターン検索用マップへアイテムを格納 */
                    for (IDicItem item : patternDic.getItems()) {
                        StringBuffer sbf = new StringBuffer();
                        IPattern pItem = (IPattern) item;
                        sbf.append(dicName).append(Messages.DicImportCommiter_comma).append(pItem.getLabel()).append(Messages.DicImportCommiter_comma)
                                .append(pItem.isParts());
                        tempMap.put(sbf.toString(), pItem);
                    }

                    patternMap.put(patternDic, tempMap);

                }
            }

            /* ここに来るまでの間に辞書にパターン検索用マップ辞書は存在しているはず */
            Map<String, IPattern> itemMap = patternMap.get(patternDic);

            if (itemMap == null) {
                /* TODO 存在しない場合はエラー的なサムシング */
            }

            for (Entry<String, IPattern> e : itemMap.entrySet()) {
                if (e.getKey().equals(primaryKey.toString())) {
                    addItem = e.getValue();
                    if (!addItem.getText().equals(text)) {
                        addItem.setText(text);
                        errs.add(Messages.bind(Messages.DicImportCommiter_patternOverride, new String[] { dicName, label }));
                    }
                    break;
                }
            }
            if (addItem == null) {
                addItem = DicFactory.getInstance().createPattern(label, text, Integer.parseInt(patternType), Boolean.valueOf(parts));
                addItem.setDirty(true);
                patternDic.addItem(addItem);
                patternMap.get(patternDic).put(primaryKey.toString(), addItem);
            }
        }

        /* ログ出力 */
        if (errs.size() > 0) {
            CoronaIoUtils.setErrorLogs(IStatus.WARNING, Messages.DicImportCommiter_errLogDicCommiter, errs, null);
        }

        /* 一度辞書を登録しないとアイテムIDが採番されない為、一時コミット */
        for (Entry<IPatternDic, Map<String, IPattern>> e : patternMap.entrySet()) {
            e.getKey().commit(new SubProgressMonitor(monitor, 1));
        }

        /* 辞書名とID要素の付け替え処理 */
        for (Entry<IPatternDic, Map<String, IPattern>> e : patternMap.entrySet()) {
            for (IDicItem item : e.getKey().getItems()) {
                IPattern ptn = (IPattern) item;
                String target = ptn.getText();

                Pattern searchPattern = Pattern.compile("<LINK (dicname=\"([\\S]*)\" name=\"([\\S]*)\" parts=\"([\\S]*)\" type=\"([\\S]*)\")"); //$NON-NLS-1$
                Matcher match = searchPattern.matcher(target);
                StringBuffer result = new StringBuffer(target);
                while (match.find()) {
                    /* 辞書名が空の場合は入力辞書名を指定 */
                    String dicName = (match.group(2).isEmpty()) ? dic.getName().substring(0, dic.getName().lastIndexOf(Messages.DicImportCommiter_dot))
                            + Messages.DicImportCommiter_pdicWithDot : match.group(2);
                    String label = match.group(3);
                    String parts = match.group(4);
                    /* 一意キーを作成 */
                    StringBuffer primaryKey = new StringBuffer();
                    primaryKey.append(dicName).append(Messages.DicImportCommiter_comma).append(label).append(Messages.DicImportCommiter_comma).append(parts);

                    ICoronaDic targetDic = IoService.getInstance().getDictionary(dicName);
                    IPattern source = patternMap.get(targetDic).get(primaryKey.toString());
                    target = target.substring(match.end());
                    String linkId = Integer.toString(source.getId());

                    StringBuilder replacement = new StringBuilder("<LINK ID=\"").append(linkId).append(Messages.DicImportCommiter_doubleQuote); //$NON-NLS-1$
                    target = match.replaceFirst(replacement.toString());
                    match = searchPattern.matcher(target);
                }
                if (result.length() > 0) {
                    ptn.setText(target);
                }
            }
        }

        /* 最終コミット */
        for (Entry<IPatternDic, Map<String, IPattern>> e : patternMap.entrySet()) {
            e.getKey().commit(new SubProgressMonitor(monitor, 1));
        }

        return true;
    }


    /**
     * ユーザ辞書を作成する
     * 
     * @param items
     *            作成する辞書のアイテム一覧。List&lt;String[]&gt;であること。
     * @param dic
     *            この辞書オブジェクトにアイテムを作成する
     * @param lableDicMap
     *            ラベル辞書と追加アイテムを紐付けるマップ
     *            処理実行時にここにデータが蓄積され、辞書と対応付けられる
     * @param ignoreLabel
     *            ラベルの除外
     * @param errs
     *            ユーザ辞書作成中のエラーメッセージを格納する。
     * @return 作成成功したらtrue
     */
    public static boolean makeUserDicList(List<Object> items, IUserDic dic, Map<AbstractPrimary, Map<String, LabelItem>> lableDicMap, Boolean ignoreLabel,
            List<String> errs) {

        /* 既存辞書チェックフラグ */
        boolean bDicFlg = false;

        /* 既存辞書マップ */
        Map<String, Term> repMap = new HashMap<String, Term>();
        for (IDicItem entry : dic.getItems()) {
            Term term = (Term) entry;
            repMap.put(term.getKeyword(), term);
        }

        try {
            if (dic.getId() != -1) {
                bDicFlg = true; /* 既存辞書あり */
            }

            int iRow = 0; /* 処理対象行カウンタ */
            int iErr = 0;

            for (Object o : items) {
                String[] wkData = (String[]) o;
                iRow++;


                if (wkData.length != DicIEConstants.USER_DIC_COLUMNS) {
                    errs.add(Messages.bind(Messages.DicImportCommiter_errLogCsv,
                            new String[] { Integer.toString(iRow), Integer.toString(DicIEConstants.USER_DIC_COLUMNS) }));
                } else {
                    ITerm term = null;

                    if (bDicFlg) {/* 既存辞書存在時 */
                        /* キー項目から既存の辞書に該当するアイテムが有るか検索 */
                        StringBuilder keyword = new StringBuilder();
                        keyword.append(wkData[DicIEConstants.HEADER]).append(Messages.DicImportCommiter_comma).append(wkData[DicIEConstants.READING])
                                .append(Messages.DicImportCommiter_comma).append(wkData[DicIEConstants.PART]);
                        term = repMap.get(keyword.toString());

                        if (term != null) { /* 用語が存在する場合 */
                            /* 既存アイテムありのメッセージを出力 */
                            errs.add(Messages.DicImportCommiter_errLogExist);
                            errs.add(Messages.bind(Messages.DicImportCommiter_errLogExistData, new String[] { Integer.toString(iRow),
                                    wkData[DicIEConstants.HEADER], wkData[DicIEConstants.READING], wkData[DicIEConstants.PART], wkData[DicIEConstants.CLASS] }));
                        }

                    }

                    if (term == null) {
                        /* 新規用語作成 */
                        term = DicFactory.getInstance().createTerm(wkData[DicIEConstants.HEADER], wkData[DicIEConstants.READING], wkData[DicIEConstants.PART],
                                wkData[DicIEConstants.CLASS], wkData[DicIEConstants.CFORM], ""); //$NON-NLS-1$
                    }

                    /* JUMAN辞書を生成 */
                    term.setJumanBase(UserDic.createJumanFormat(term));
                    /* 単語、読み、品詞に欠落があった場合はエラー表示 */
                    if (term.getJumanBase().isEmpty()) {
                        errs.add(Messages.bind(Messages.DicImportCommiter_errLogCsvFail, new String[] { Integer.toString(iRow) }));
                        iErr++;
                        continue;
                    }
                    dic.addItem(term);

                    if (ignoreLabel == false) {
                        /* ダブルクォーテーションを除去 */
                        String label = wkData[DicIEConstants.LABEL].replace("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        if (label.length() != 0) {

                            /* "|"を分割記号としてラベル情報を分割 */
                            String[] labels = label.split("\\|", -1); //$NON-NLS-1$

                            /* 一行中に複数のラベル情報があるためループ */
                            for (String splitLabel : labels) {
                                makeLabelInfo(dic, splitLabel, term, lableDicMap, errs);
                            }

                        }
                    }
                }
            }

            /* エラーカウントチェック */
            if (iRow == iErr) {
                return false;
            }
            if (ignoreLabel == false) {
                for (Entry<AbstractPrimary, Map<String, LabelItem>> object : lableDicMap.entrySet()) {
                    for (Entry<String, LabelItem> mapItem : object.getValue().entrySet()) {
                        LabelItem item = mapItem.getValue();

                        /* 親ラベル情報を取得 */
                        LabelItem pLabelItem = (LabelItem) item.getParent();

                        boolean bFlg = false;
                        LabelDic labelDic = (LabelDic) object.getKey().getItem();
                        if (labelDic.getId() != -1) {
                            for (IDicItem i : labelDic.getLabelsRecursive(labelDic.getLabels())) {
                                LabelItem label = (LabelItem) i;
                                if (pLabelItem == null) {
                                    /* トップラベルの場合 */
                                    if (label.getName().equals(item.getName())) {
                                        if (label.getParent() == null) {
                                            /* 既存ラベルの場合 */
                                            item.setId(label.getId());
                                            label.setTerms(item.getTerms());
                                            bFlg = true;
                                            break;
                                        }
                                    }
                                } else {
                                    String chkParent = ""; //$NON-NLS-1$
                                    /* 親ラベルが存在する場合 */
                                    if (label.getParent() != null) {
                                        chkParent = label.getParent().getName();
                                        if (chkParent.equals(pLabelItem.getName()) && label.getName().equals(item.getName())) {
                                            /* 親ラベル名称が一致かつ自分自身のラベル名称が一致する場合 */
                                            item.setId(label.getId());
                                            label.setTerms(item.getTerms());
                                            bFlg = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (!bFlg) {
                                if (pLabelItem == null) {
                                    /* トップラベルを追加 */
                                    labelDic.addItem(item);

                                } else {
                                    /* 親ラベルに子供を追加 */
                                    pLabelItem.addChild(item);
                                }
                            }

                        } else {
                            /* トップラベルを追加 */
                            labelDic.addItem(item);
                        }

                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            /* IndexOutOfBoundsExceptionをキャッチしちゃダメ。ここに来るようなコードはバグ。 */
            e.printStackTrace();
            assert false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }


    /**
     * ラベル文字列からラベル辞書を割り出し、該当するラベル辞書へラベルアイテムを追加する。
     * 
     * @param labels
     *            ラベル文字列
     * @param term
     *            ラベルを追加するユーザー辞書アイテム
     * @param labelDicMap
     *            ラベル辞書マップ
     * @throws SQLException
     *             SQL実行時エラー
     */
    private static void makeLabelInfo(IUserDic dic, String labels, ITerm term, Map<AbstractPrimary, Map<String, LabelItem>> labelDicMap, List<String> errs)
            throws SQLException {

        /* コロンの前後でラベルを分割 */
        String[] splitWord = labels.split(Messages.DicImportCommiter_dicSplit, -1);
        String[] columns = new String[2];
        LabelDic ldic = null;

        /* 辞書名が入力されていない場合はユーザー辞書と同名のラベル辞書を作成する */
        if (splitWord.length != 2) {
            columns[0] = dic.getName().substring(0, dic.getName().lastIndexOf(".")) + ".ldic"; //$NON-NLS-1$ //$NON-NLS-2$
            columns[1] = splitWord[0];
        } else {
            /* 辞書名に拡張子がない場合はデフォルトで.ldicを付与する */
            if (!splitWord[0].contains(Messages.DicImportCommiter_dot)) {
                splitWord[0] = splitWord[0] + Messages.DicImportCommiter_ldicWithDot;
            }
            columns[0] = splitWord[0];
            columns[1] = splitWord[1];
        }

        /*
         * 現在の実装では辞書オブジェクトをマップに格納する際に
         * ObjectのKeyとして使用される値が辞書IDである為、新規辞書を複数追加
         * した場合に上書きされてしまう。
         * その為、一意となるオブジェクトを作成し格納する。
         */
        AbstractPrimary primaryObject = null;

        Map<String, LabelItem> duplicateMap = new HashMap<String, LabelItem>();
        boolean existDic = false;

        for (Entry<AbstractPrimary, Map<String, LabelItem>> obj : labelDicMap.entrySet()) {
            if (((LabelDic) obj.getKey().getItem()).getName().equals(columns[0])) {
                existDic = true;
                duplicateMap = obj.getValue();
            }
        }

        if (existDic == false) {
            /* ラベル辞書取得 */
            //int labelDicId = CommonDao.getFirstInt(DicTableDao.getDicId(columns[0]));
            int labelDicId = 0;
            Session session = IoService.getInstance().getSession();
            String hql = "from DicTableBean where dicName = :dicName"; //$NON-NLS-1$
            try {
                @SuppressWarnings("unchecked")
                List<DicTableBean> beanList = session.createQuery(hql).setString("dicName", columns[0]).list(); //$NON-NLS-1$
                if (beanList != null && beanList.size() > 0) {
                    DicTableBean bean = beanList.get(0);
                    if (bean != null) {
                        labelDicId = bean.getDicId();
                    }
                }
            } catch (HibernateException e) {
                CoronaActivator.debugLog("Error HQL : " + hql); //$NON-NLS-1$
                e.printStackTrace();
                labelDicId = -1;
            }

            if (labelDicId == -1) {
                /* system Error */
                throw new SQLException(Messages.DicImportCommiter_exceptionLabel);
            } else if (labelDicId == 0) {
                /* ラベル辞書を新規作成 */
                ldic = (LabelDic) IoActivator.getDicFactory().createLabelDic(columns[0]);

                /* 一意なオブジェクトを生成 */
                primaryObject = new AbstractPrimary(ldic);
            } else {
                ldic = (LabelDic) IoActivator.getService().getDictionary(labelDicId);
                primaryObject = new AbstractPrimary(ldic);
            }

            /* 一意なオブジェクトを生成 */
            labelDicMap.put(primaryObject, duplicateMap);
        }

        /* 要素をスラッシュごとに分割して処理を行う */
        String labelInfo[] = columns[1].split("/", -1); //$NON-NLS-1$

        /* 検索キー初期化 */
        String strKey = ""; //$NON-NLS-1$
        for (int idx = 0; idx < labelInfo.length; idx++) {
            if ("".equals(strKey)) { //$NON-NLS-1$
                strKey = labelInfo[idx];
            }

            /* ラベル情報を取得、なければ作る */
            LabelItem labelItem = duplicateMap.get(strKey);
            if (labelItem == null) {
                labelItem = (LabelItem) DicFactory.getInstance().createLabel(labelInfo[idx], null);
                duplicateMap.put(strKey, labelItem);
                errs.add(Messages.bind(Messages.DicImportCommiter_addLabelInformation, new String[] { term.getValue(), labelItem.getName() }));
            }
            labelItem.setDicId(ILabel.UNSAVED_ID);

            /* 最終列の場合は用語アイテムへ追加 */
            if (idx == (labelInfo.length - 1)) {
                labelItem.addTerm(term);
            } else {

                /* 要素が連続する場合は子要素として追加 */
                strKey = strKey + "/" + labelInfo[idx + 1]; //$NON-NLS-1$
                LabelItem labelChild = duplicateMap.get(strKey);
                if (labelChild == null) {
                    labelChild = (LabelItem) DicFactory.getInstance().createLabel(labelInfo[idx + 1], null);
                    duplicateMap.put(strKey, labelChild);
                }
                /* 親子関係なう */
                labelChild.setParent(labelItem);
            }
        }
    }


    /**
     * 
     * 辞書テーブルBean作成
     * 
     * @param parents
     *            親辞書ID
     * @param dicName
     *            辞書名
     * @param dicFileName
     *            辞書ファイル名
     * @param categoryId
     *            分野ID
     * @param dicType
     *            辞書タイプ
     * @return dicTableBean
     */
    public static DicTableBean insertDicTable(Set<Integer> parents, String dicName, String dicFileName, int categoryId, int dicType) {

        // 親辞書を編集
        String parent = CoronaIoUtils.intListToString(parents);

        // 登録用辞書テーブルビーンの作成
        DicTableBean dicTable = new DicTableBean();
        dicTable.setParentId(parent);
        dicTable.setDicName(dicName);
        dicTable.setDicFileName(dicFileName);
        dicTable.setCategoryId(categoryId);
        dicTable.setDicType(dicType);
        dicTable.setInactive(false);
        Date date = new Date();
        dicTable.setDate(date);
        dicTable.setCreationTime(date);

        return dicTable;
    }
}