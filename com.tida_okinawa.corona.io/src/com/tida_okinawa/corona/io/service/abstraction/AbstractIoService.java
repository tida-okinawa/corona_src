/**
 * @version $Id: AbstractIoService.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/08/03
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.service.abstraction;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.TextItem;
import com.tida_okinawa.corona.io.model.abstraction.CoronaObject;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.io.service.IIoService;


/**
 * IOサービス抽象クラス
 * 
 * @author shingo-takahashi
 * 
 */
public abstract class AbstractIoService extends CoronaObject implements IIoService {

    protected Map<Integer, ICoronaProject> _projectList;
    protected Map<Integer, ICoronaDic> _dics;
    protected List<TextItem> _categoryList;

    protected String strConnect;
    protected String strUser;
    protected String strPasswd;


    @Override
    public Boolean connect(String connect, String user, String passwd) {
        return connectDam(connect, user, passwd);
    }


    @Override
    public synchronized Boolean reConnect() {
        return connectDam(strConnect, strUser, strPasswd);
    }


    @Override
    public Boolean disConnect() {
        return disConnectDam();
    }


    @Override
    public Boolean isConnect() {
        return isConnectDam();
    }


    @Override
    public List<ICoronaProject> getProjects() {
        List<ICoronaProject> list = new ArrayList<ICoronaProject>();
        // testH25 20130806 互換性テスト 20130822
        Boolean isCoronaConnect = isConnect();
        if (!isCoronaConnect) {
            return list;
        }
        // testH25 20130806 互換性テスト

        for (Entry<Integer, ICoronaProject> entry : getProjectsMap().entrySet()) {
            list.add(entry.getValue());
        }
        return list;
    }


    @Override
    public ICoronaProject getProject(int id) {
        if (_projectList == null || _projectList.isEmpty()) {
            getProjectsMap();
        }
        return _projectList.get(id);
    }


    /**
     * addProject
     * 
     * @param project
     * @return true/false
     * 
     */
    @Override
    public Boolean addProject(ICoronaProject project) {
        boolean ret = addProjectDam(project);

        /* Memo DBに登録できなかったら、リストにも入れないようにしてみた OK? */
        if (ret) {
            /* IDを確定させてから登録する */
            if (_projectList == null) {
                getProjectsMap();
            }
            _projectList.put(project.getId(), project);
        }

        return ret;
    }


    @Override
    public Boolean removeProject(ICoronaProject project) {
        _projectList.remove(project.getId());
        return true;
    }


    @Override
    public List<IClaimData> getClaimDatas() {
        List<IClaimData> list = new ArrayList<IClaimData>();
        for (Entry<Integer, IClaimData> entry : getClaimDatasMap().entrySet()) {
            list.add(entry.getValue());
        }
        return list;
    }


    @Override
    public IClaimData importClaimData(String path, String definePath, String tableName, boolean headFlg) throws SQLException, IOException {
        IClaimData claimData = importClaimDataDam(path, definePath, tableName, headFlg);
        return claimData;

    }


    @Override
    public IClaimData importClaimDataForDocument(String path, String tableName, String target, List<String> records) throws SQLException, IOException {
        IClaimData claimData = importClaimDataDamForDocument(path, tableName, target, records);
        return claimData;

    }


    @Override
    public Boolean addClaimData(String path, int claimId) {
        return false;
    }


    @Override
    public List<ICoronaDic> getDictionarys(Class<?> cls) {
        List<ICoronaDic> list = new ArrayList<ICoronaDic>();
        for (Entry<Integer, ICoronaDic> entry : getDictionarysMap().entrySet()) {
            ICoronaDic dic = entry.getValue();
            if (cls.isAssignableFrom(dic.getClass())) {
                list.add(entry.getValue());
            }
        }
        return list;
    }


    @Override
    public List<TextItem> getCategorys() {
        _categoryList = updateCategorys();
        return _categoryList;

    }


    abstract protected Boolean connectDam(String connect, String user, String passwd);


    abstract protected Boolean disConnectDam();


    abstract protected Boolean isConnectDam();


    abstract public Map<Integer, ICoronaProject> getProjectsMap();


    abstract public Map<Integer, IClaimData> getClaimDatasMap();


    /**
     * 辞書情報取得
     */
    abstract protected Map<Integer, ICoronaDic> getDictionarysMap();


    abstract protected Boolean addProjectDam(ICoronaProject project);


    abstract protected IClaimData importClaimDataDam(String path, String definePath, String tableName, boolean headFlg) throws SQLException, IOException;


    abstract protected IClaimData importClaimDataDamForDocument(String path, String tableName, String target, List<String> records) throws SQLException,
            IOException;


    /**
     * カテゴリ情報取得
     */
    abstract protected List<TextItem> updateCategorys();


    /**
     * カテゴリ情報作成
     * 
     * @param category
     * @return すでに存在していればnull。登録に成功すれば、新しく作成したカテゴリ。
     */
    @Override
    public TextItem createCategory(String category) {
        int id = addCategoryDam(category);
        if (id > 0) {
            TextItem newItem = new TextItem(id, category);
            _categoryList.add(newItem);
            return newItem;
        }
        return null;
    }


    /**
     * @param category
     * @return -1:例外。0:登録済み。それ以外:追加成功(カテゴリのID)
     */
    abstract protected int addCategoryDam(String category);


    @Override
    public List<IFieldHeader> getTableColumns(String path, String definePath, String tableName) throws SQLException, IOException {
        List<IFieldHeader> headers = getTableColumnsDam(path, definePath, tableName);
        return headers;

    }


    /**
     * 問い合わせデータ登録時のテーブルのカラム情報を取得する
     * 
     * @param path
     *            問い合わせデータのファイルパス
     * @param definePath
     *            問い合わせデータ登録用の定義ファイルパス
     * @param tableName
     *            問い合わせデータを登録するテーブル名
     * @return 定義ファイルで定義してあるカラムの一覧
     * @throws SQLException
     *             DB接続失敗、テンポラリテーブル作成失敗、列情報取得失敗
     * @throws IOException
     *             ファイル読み込み失敗
     */
    abstract protected List<IFieldHeader> getTableColumnsDam(String path, String definePath, String tableName) throws SQLException, IOException;


    // testH25 20130806 互換性テスト
    abstract public String getDbVersionDam(int result[]);


    @Override
    public String getDbVersion(int result[]) {
        return getDbVersionDam(result);
    }

}
