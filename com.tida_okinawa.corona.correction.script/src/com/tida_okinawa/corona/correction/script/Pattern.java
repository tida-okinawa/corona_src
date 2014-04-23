/**
 * @version $Id: Pattern.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/05 21:14:36
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
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.ui.controllers.PatternMatcher;
import com.tida_okinawa.corona.ui.data.MorphemeRecord;
import com.tida_okinawa.corona.ui.data.PatternMatcherRecord;

/**
 * @author kousuke-morishima
 * 
 */
public class Pattern extends Cleansing2<MorphemeRecord, PatternMatcherRecord> {
    /**
     * @param args
     *            前から順に、データベース接続URL、データベースユーザ名、データベースパスワード、
     *            対象プロジェクト名、対象ターゲット名、入力データ種別、複数ヒットさせるかどうか(true/false)
     */
    public static void main(String[] args) {
        try {
            new Pattern(args).run();
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
    public Pattern(String[] args) {
        super(args);
    }

    /** 引数の数がおかしいときのエラーコード */
    public static final int ERROR_CODE_ARGS = 7;
    /** 入力データ種別がおかしいときのエラーコード */
    public static final int ERROR_CODE_INVALID_INPUT = 8;
    /** 複数ヒットさせるかどうかの値がおかしいときのエラーコード */
    public static final int ERROR_CODE_INVALID_BOOL = 9;

    private static final int URL = 0;
    private static final int USER = 1;
    private static final int PASS = 2;
    private static final int PROJECT = 3;
    private static final int TARGET = 4;
    private static final int INPUT_TYPE = 5;
    private static final int MULTI_HIT = 6;

    private ICoronaProject project;
    private ICoronaProduct product;
    private ClaimWorkDataType inputType;
    private boolean isMultiHit;


    @Override
    int check(String[] args) {
        if (args.length != 7) {
            return ERROR_CODE_ARGS;
        }

        service = createService(args[URL], args[USER], args[PASS]);
        if (service == null) {
            return ERROR_CODE_NO_DATABASE_CONNECTION;
        }

        project = searchProject(args[PROJECT]);
        if (project == null) {
            return ERROR_CODE_NO_PROJECT;
        }

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

        /* 複数ヒットフラグの確認 */
        String boolValue = args[MULTI_HIT];
        if ("true".equalsIgnoreCase(boolValue)) { //$NON-NLS-1$
            isMultiHit = true;
        } else if ("false".equalsIgnoreCase(boolValue)) { //$NON-NLS-1$
            isMultiHit = false;
        } else {
            return ERROR_CODE_INVALID_BOOL;
        }

        return CODE_OK;
    }


    @Override
    String getErrorMessage(int errorCode) {
        switch (errorCode) {
        case ERROR_CODE_ARGS:
            return Messages.bind(Messages.ErrorMessage_ArgsNum, new Object[] { Messages.NamePattern, 7, Messages.Pattern_ErrorMessage_ArgsDetail });
        case ERROR_CODE_INVALID_INPUT:
            return Messages.Pattern_ErrorMessage_InvalidInputType;
        case ERROR_CODE_INVALID_BOOL:
            return Messages.Pattern_ErrorMessage_InvalidBoolean;
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
        Set<ICoronaDic> patterns = new HashSet<ICoronaDic>();
        patterns.addAll(product.getDictionarys(IPatternDic.class));
        patterns.addAll(project.getDictionarys(IPatternDic.class));
        Set<ICoronaDic> terms = new HashSet<ICoronaDic>();
        terms.addAll(product.getDictionarys(IUserDic.class));
        terms.addAll(project.getDictionarys(IUserDic.class));
        Set<ICoronaDic> labels = new HashSet<ICoronaDic>();
        labels.addAll(product.getDictionarys(ILabelDic.class));
        labels.addAll(project.getDictionarys(ILabelDic.class));
        List<ICoronaDic> dics = new ArrayList<ICoronaDic>(patterns);
        dics.addAll(terms);
        dics.addAll(labels);
        PatternMatcher controller = new PatternMatcher(product, works, null, dics, isMultiHit);
        run(controller);

    }

}
