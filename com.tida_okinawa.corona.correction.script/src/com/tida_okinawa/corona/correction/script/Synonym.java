/**
 * @version $Id: Synonym.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/05 19:39:36
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.script;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IFlucDic;
import com.tida_okinawa.corona.io.model.dic.ISynonymDic;
import com.tida_okinawa.corona.ui.controllers.SynonymController;
import com.tida_okinawa.corona.ui.data.MorphemeRecord;
import com.tida_okinawa.corona.ui.data.SynonymRecord;

/**
 * ゆらぎ・同義語補正をコンソールから行うためのクラス
 * 
 * @author kousuke-morishima
 */
public class Synonym extends Cleansing2<MorphemeRecord, SynonymRecord> {
    /**
     * @param args
     *            前から順に、データベース接続URL、データベースユーザ名、データベースパスワード、
     *            対象プロジェクト名、対象ターゲット名、入力データ種別
     */
    public static void main(String[] args) {
        try {
            new Synonym(args).run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(Cleansing.ERROR_CODE_ANY_EXCEPTION);
        }
        System.exit(0);
    }


    /**
     * 自動実行用
     * 
     * @param args
     *            引数
     */
    public Synonym(String[] args) {
        super(args);
    }

    /** 引数の数がおかしいときのエラーコード */
    public static final int ERROR_CODE_ARGS = 7;
    /** 入力データ種別がおかしいときのエラーコード */
    public static final int ERROR_CODE_INVALID_INPUT = 8;

    private static final int URL = 0;
    private static final int USER = 1;
    private static final int PASS = 2;
    private static final int PROJECT = 3;
    private static final int TARGET = 4;
    private static final int INPUT_TYPE = 5;

    private ICoronaProject project;
    private ICoronaProduct product;
    private ClaimWorkDataType inputType;


    @Override
    int check(String[] args) {
        if (args.length != 6) {
            return ERROR_CODE_ARGS;
        }

        service = createService(args[URL], args[USER], args[PASS]);
        if (service == null) {
            return ERROR_CODE_NO_DATABASE_CONNECTION;
        }

        /* プロジェクトチェック */
        project = searchProject(args[PROJECT]);
        if (project == null) {
            return ERROR_CODE_NO_PROJECT;
        }

        /* ターゲットチェック */
        product = searchProduct(project, args[TARGET]);
        if (product == null) {
            return ERROR_CODE_NO_TARGET;
        }

        String type = args[INPUT_TYPE];
        if (TYPE_MOR.equals(type)) {
            //inputType = ClaimWorkDataType.MORPHOLOGICAL;
            inputType = ClaimWorkDataType.DEPENDENCY_STRUCTURE;
        } else if (TYPE_DEP.equals(type)) {
            inputType = ClaimWorkDataType.DEPENDENCY_STRUCTURE;
        } else if (TYPE_SYN.equals(type)) {
            inputType = ClaimWorkDataType.CORRECTION_SYNONYM;
        } else {
            return ERROR_CODE_INVALID_INPUT;
        }
        return CODE_OK;
    }


    @Override
    String getErrorMessage(int errorCode) {
        switch (errorCode) {
        case ERROR_CODE_ARGS:
            return Messages.bind(Messages.ErrorMessage_ArgsNum, new Object[] { Messages.NameSynonym, 6, Messages.Synonym_ErrorMessage_ArgsDetail });
        case ERROR_CODE_INVALID_INPUT:
            return Messages.Synonym_ErrorMessage_InvalidInputType;
        default:
            return super.getErrorMessage(errorCode);
        }
    }


    void run() {
        /* 処理対象の選定 */
        List<IClaimWorkData> works = new ArrayList<IClaimWorkData>();
        for (IClaimWorkData work : product.getClaimWorkDatas()) {
            if (inputType.equals(work.getClaimWorkDataType())) {
                works.add(work);
            }
        }
        /* 使用辞書の準備 */
        List<ICoronaDic> dics = new ArrayList<ICoronaDic>();
        Set<ICoronaDic> synonyms = new HashSet<ICoronaDic>();
        synonyms.addAll(product.getDictionarys(ISynonymDic.class));
        synonyms.addAll(project.getDictionarys(ISynonymDic.class));
        Set<ICoronaDic> flucs = new HashSet<ICoronaDic>();
        flucs.addAll(product.getDictionarys(IFlucDic.class));
        flucs.addAll(project.getDictionarys(IFlucDic.class));
        dics.addAll(synonyms);
        dics.addAll(flucs);
        SynonymController controller = new SynonymController(product, works, null, dics);
        run(controller);
    }
}
