/**
 * @version $Id: IIoService.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/08/09 18:11:14
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.hibernate.Session;

import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.ICoronaDics;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.TextItem;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.model.dic.PatternType;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;


/**
 * データベース IOサービスインターフェース
 * 
 * @author shingo-takahashi
 */
public interface IIoService extends ICoronaDics {

    /**
     * DBへ接続
     * 
     * @param connectionParamerter
     *            接続パラメータ
     * @param user
     *            ユーザー名
     * @param passwd
     *            パスワード
     * @return 成否
     */
    public abstract Boolean connect(String connectionParamerter, String user, String passwd);


    /**
     * DBへのテスト接続
     * 
     * @param connectionParamerter
     *            接続パラメータ
     * @param user
     *            ユーザ名
     * @param passwd
     *            パスワード
     * @return エラーメッセージ。長さ0の配列なら処理成功。
     */
    public abstract String[] connectTest(String connectionParamerter, String user, String passwd);


    /**
     * DBへ再接続
     * 
     * @return 成否
     */
    public Boolean reConnect();


    /**
     * DBとの接続を切断
     * 
     * @return 成否
     */
    public abstract Boolean disConnect();


    /**
     * DB接続状態確認
     * 
     * @return　接続状態。falseの場合切断されている。
     */
    public abstract Boolean isConnect();


    /**
     * セッションを取得
     * 
     * @return Session
     * @throws SQLException
     */
    public abstract Session getSession();


    /**
     * プロジェクトリスト取得<br>
     * IOサービス上に登録されているプロジェクトの一覧を返す。
     * 
     * @return　プロジェクトデータリスト
     */
    public abstract List<ICoronaProject> getProjects();


    /**
     * プロジェクト取得<br>
     * 指定されたIDのプロジェクトを返却する。<br>
     * 存在しないプロジェクトIDが指定された場合は、nullを返す。
     * 
     * @param id
     *            対象プロジェクトID
     * @return プロジェクトデータ
     */
    public abstract ICoronaProject getProject(int id);


    /**
     * プロジェクト追加
     * 
     * @param project
     *            ICoronaProjectを実装したプロジェクトオブジェクト
     * @return 成否
     */
    public abstract Boolean addProject(ICoronaProject project);


    /**
     * プロジェクト破棄
     * 
     * @param project
     *            ICoronaProjectを実装したプロジェクトオブジェクト
     * @return 成否
     */
    public abstract Boolean removeProject(ICoronaProject project);


    /**
     * 問い合わせデータリストを取得
     * 毎回別のListインスタンスを返す
     * 
     * @return 問い合わせデータリスト
     */
    public abstract List<IClaimData> getClaimDatas();


    /**
     * 問い合わせデータを取得
     * 
     * @param claimId
     *            問い合わせデータID
     * @return 問い合わせデータ情報
     */
    public abstract IClaimData getClaimData(int claimId);


    /**
     * 問い合わせデータインポート<br>
     * IOサービスに登録する問い合わせデータを指定する。
     * 
     * @param path
     *            問い合わせデータ
     * @param definePath
     *            定義情報
     * @param tableName
     * @param headFlg
     * @return 登録した問合せデータ、またはnull（登録失敗）
     * @throws SQLException
     * @throws IOException
     */
    public abstract IClaimData importClaimData(String path, String definePath, String tableName, boolean headFlg) throws SQLException, IOException;


    /**
     * 問い合わせデータインポート（ドキュメント用）
     * 
     * @param path
     *            データファイル
     * @param tableName
     *            テーブル名
     * @param target
     *            ターゲット名
     * @param records
     *            Splitしたデータリスト
     * @return 登録した問合せデータ、またはnull（登録失敗）
     * @throws SQLException
     * @throws IOException
     */
    public abstract IClaimData importClaimDataForDocument(String path, String tableName, String target, List<String> records) throws SQLException, IOException;


    /**
     * 問い合わせデータ破棄<br>
     * IOサービスから問い合わせデータを破棄する。<br>
     * 対象のデータがプロジェクトに追加されていたりすると、データが破棄されるため、注意が必要
     * 
     * @param claimId
     *            問い合わせデータID
     * @return 成否
     */
    public abstract Boolean removeClaimData(int claimId);


    /**
     * 問い合わせ情報追加<br>
     * 既存の問い合わせ情報に対して、レコードの追記を行う。
     * 
     * @param path
     * @param claimId
     * @return 成否
     */
    public abstract Boolean addClaimData(String path, int claimId);


    /**
     * 辞書リスト取得<br>
     * IOで管理されている辞書リストの取得をする
     * 
     * 　@note　辞書の作成はプロジェクトまたは、プロダクトから作成するIFのみ存在する。
     * 
     * return 辞書リスト
     */
    @Override
    public abstract List<ICoronaDic> getDictionarys(Class<?> cls);


    /**
     * 辞書名で検索して、辞書を返す
     * 
     * @param name
     *            辞書名
     * @return may be null
     */
    public abstract ICoronaDic getDictionary(String name);


    /**
     * 辞書IDで検索して、辞書を返す
     * Memo 他の機能がしっかりと辞書を追加していれば、ここでnullが返ることはありえない
     * 
     * @param id
     *            辞書ID
     * @return may be null
     */
    @Override
    public abstract ICoronaDic getDictionary(int id);


    /**
     * @param dicId
     */
    @Override
    public abstract void removeDictionary(int dicId);


    /**
     * 指定した辞書一覧から、対応する条件の用語を持つ辞書を返す。
     * 条件の絞り込み対象にしい項目にはnullを設定する。
     * 
     * @param searchDics
     *            検索対象のユーザー辞書
     * @param header
     *            見出し語
     * @param reading
     *            読み
     * @param part
     *            品詞
     * @param clas
     *            品詞詳細
     * @param cform
     *            活用形
     * @return 条件に一致する辞書一覧
     * @throws SQLException
     *             データベースへの接続が確立できなかった
     */
    public abstract Collection<IUserDic> searchParentDic(List<IUserDic> searchDics, String header, String reading, String part, String clas, String cform)
            throws SQLException;


    /**
     * パターン分類新規登録ウィザードに入力された分類IDと分類名を返却
     * 
     * @param name
     * @return パターン分類ID及び分類名
     */
    public abstract PatternType addPatternType(String name);


    /**
     * パターン分類の削除
     * DB及びインスタンスから削除完了の場合はtrueを返却
     * 
     * @param type
     * @return 成否
     */
    public abstract boolean removePatternType(PatternType type);


    /**
     * DBからパターン分類を取得
     * 
     * @return パターン分類リスト
     */
    public abstract PatternType[] getPatternTypes();


    /**
     * @param productId
     * @return may be null if error occurred
     */
    public abstract String getProductName(int productId);


    /**
     * 問い合わせデータ新規登録時に問い合わせデータのカラム情報リストを取得
     * 
     * @param path
     *            問い合わせデータのファイルパス
     * @param definePath
     *            問い合わせデータ登録用の定義ファイルパス
     * @param tableName
     *            問い合わせデータを登録するテーブル名
     * @return カラムリスト
     * @throws SQLException
     *             接続失敗、SQL実行失敗
     * @throws IOException
     *             問い合わせデータ、定義ファイルを読み込めない
     */
    public abstract List<IFieldHeader> getTableColumns(String path, String definePath, String tableName) throws SQLException, IOException;


    /* ****************************************
     * 分野
     */
    /**
     * 辞書カテゴリ取得<br>
     * 分野辞書用の分野リストを取得する
     * 
     * @return 分野リスト
     */
    public abstract List<TextItem> getCategorys();


    /**
     * 辞書カテゴリ追加<br>
     * 分野辞書用の分野を追加する。
     * 
     * @param category
     * @return すでに存在していればnull。登録に成功すれば、新しく作成したカテゴリ。
     */
    public abstract TextItem createCategory(String category);


    /**
     * 辞書カテゴリ破棄<br>
     * 分野辞書用の分野を破棄する。すでに割り当てられている辞書が存在する場合は
     * 
     * @param category
     */
    public abstract void removeCategory(String category);


    /**
     * 分野名を変更する
     * 
     * @param categoryId
     *            変更する分野のID
     * @param newName
     *            新しい分野名
     * @return 成否
     */
    public abstract boolean modifyCategory(int categoryId, String newName);


    /**
     * クレームデータがプロジェクトに紐づいているかチェックする
     * 
     * @param id
     * @return 紐づいていれば、True
     */
    public abstract boolean chkRelPrjClm(int id);


    // testH25 20130806 互換性テスト
    /**
     * DBからDBバージョン情報を取得
     * 
     * @param result
     *            実行結果をセット（0:正常)
     * @return DBから取得したDBバージョン情報（取得できなかった場合は空白文字列）
     */
    public abstract String getDbVersion(int result[]);
}