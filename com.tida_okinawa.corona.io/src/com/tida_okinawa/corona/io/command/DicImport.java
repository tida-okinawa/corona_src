/**
 * @version $Id: DicImport.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/11 9:18:27
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;

import org.eclipse.core.runtime.IProgressMonitor;

import com.tida_okinawa.corona.common.Encoding;
import com.tida_okinawa.corona.io.dam.hibernate.DicFactory;
import com.tida_okinawa.corona.io.dam.hibernate.IoService;
import com.tida_okinawa.corona.io.model.TextItem;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IFluc;
import com.tida_okinawa.corona.io.model.dic.IPattern;
import com.tida_okinawa.corona.io.model.dic.ISynonym;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.IUserDic;

/**
 * 辞書のインポートを行う.
 * 
 * @author shingo-takahashi
 */
public class DicImport implements IDicImport {

    private String encode = Encoding.UTF_8.toString();


    @Override
    public boolean import0(String path, String dicName, TextItem category, IProgressMonitor monitor, Boolean ignoreLabel) throws IOException,
            UnsupportedOperationException, SQLException {
        ICoronaDic dic = null;

        monitor.beginTask(Messages.DicImport_monitorImport, 2);

        // ファイル名を取得 
        String importFile = new File(path).getName();

        int extIndex = importFile.lastIndexOf(".");
        if (extIndex <= 0) {
            return false;
        }

        // 最初の拡張子を外す(.xls,.xlsx,.xml)
        importFile = importFile.substring(0, extIndex);

        extIndex = importFile.lastIndexOf('.');
        if (extIndex <= 0) {
            return false;
        }
        // 拡張子を取得
        String extention = importFile.substring(extIndex + 1);

        DicType dicType = DicType.valueOfExt(extention);
        if (dicType == null) {
            throw new UnsupportedOperationException(Messages.DicImport_errLogUnsupport);
        }

        /* ファイル情報読み込み */
        FileInputStream in = new FileInputStream(path);
        DicImportDecoder decoder = null;

        /* 辞書名編集 */
        dicName = dicName + "." + dicType.getExtension(); //$NON-NLS-1$

        /* 既存辞書が存在するかチェック */
        dic = ((IoService) IoService.getInstance()).getDictionary(dicName);
        if (dic == null) {
            dic = DicFactory.getInstance().createDic(dicName, dicType);
        }

        monitor.subTask(Messages.DicImport_monitorDecode);

        /* タイプ別辞書の生成 */
        switch (dicType) {
        case COMMON:
        case CATEGORY:
        case SPECIAL:
            decoder = new DicImportDecoder(in, ITerm.class, getEncode());

            if (dic.getId() == -1) {
                /* 新規辞書の場合 */
                String fileName = dicName.replace(dicType.getExtension(), "dic"); //$NON-NLS-1$
                ((IUserDic) dic).setFileName(fileName);
                if (category == null) {
                    category = new TextItem(0, ""); //$NON-NLS-1$
                }
                ((IUserDic) dic).setDicCategory(category);
            }
            break;
        case FLUC:
            decoder = new DicImportDecoder(in, IFluc.class, getEncode());
            break;
        case SYNONYM:
            decoder = new DicImportDecoder(in, ISynonym.class, getEncode());
            break;
        case PATTERN:
            decoder = new DicImportDecoder(in, IPattern.class, getEncode());
            break;
        default:
            throw new UnsupportedOperationException(Messages.DicImport_errLogUnsupport);
        }
        monitor.worked(1); // dummy worked for animation

        monitor.subTask(Messages.DicImport_monitorCommit);
        boolean bResult = DicImportCommiter.doCommit(dic, decoder.readArray(), dicType, monitor, ignoreLabel);
        monitor.worked(1);
        monitor.done();

        return bResult;
    }


    @Override
    public void setEncode(String encode) {
        this.encode = encode;
    }


    @Override
    public String getEncode() {
        return this.encode;
    }
}
