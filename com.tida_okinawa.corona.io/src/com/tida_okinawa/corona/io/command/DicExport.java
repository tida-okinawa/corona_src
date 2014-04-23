/**
 * @version $Id: DicExport.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/17 17:18:27
 * @author yukihiro-kinjyo
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.xml.sax.SAXException;

import com.tida_okinawa.corona.common.Encoding;
import com.tida_okinawa.corona.io.ArrayToXlsfile;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.IFlucDic;
import com.tida_okinawa.corona.io.model.dic.ILabel;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.ISynonymDic;
import com.tida_okinawa.corona.io.model.dic.ITerm;
import com.tida_okinawa.corona.io.model.dic.IUserDic;

/**
 * 辞書のエクスポートを行う.
 * 
 * @author yukihiro-kinjyo
 */
public class DicExport implements IDicExport {

    private static final String SYNONYM_HEADER = Messages.DicExport_synonymHeader;
    private static final String CSV_HEADER = Messages.DicExport_csvHeader;
    private static final String CSV_HEADER_USER = Messages.DicExport_csvHeaderUser;
    private static final String CSV_HEADER_SYNONYM = Messages.DicExport_csvHeaderSynonym;

    Map<Integer, List<ILabel>> labelMap = new HashMap<Integer, List<ILabel>>();

    private String encode = Encoding.UTF_8.toString();


    @Override
    public IStatus export(String path, ICoronaDic dic, IProgressMonitor monitor) throws IOException, ParserConfigurationException, SAXException {
        monitor.beginTask(Messages.DicExport_monitorExport, 3);
        monitor.subTask(Messages.DicExport_monitorMakingData);
        monitor.worked(1);

        /* パターン辞書は例外 */
        if (dic instanceof IPatternDic) {
            PatternDicExport patternExport = new PatternDicExport(getEncode());
            return patternExport.export(path, (IPatternDic) dic, monitor);
        }
        /* 現行　CSV　のファイル名は入力ファイル名＋".csv"を付与 */
        String setpath = path + ".csv";
        DicExportEncoder encoder = new DicExportEncoder(setpath, labelMap, encode);

        /* ヘッダを構築 */
        StringBuffer header = new StringBuffer(100);

        /* XLS出力用 */
        String xlsheader = null;
        ArrayList<String> meisai = new ArrayList<String>();

        if (dic instanceof ISynonymDic || dic instanceof IFlucDic)
            header.append(SYNONYM_HEADER);
        header.append(CSV_HEADER);
        if (dic instanceof IUserDic) {
            header.append(CSV_HEADER_USER);
        } else if (dic instanceof ISynonymDic || dic instanceof IFlucDic) {
            header.append(CSV_HEADER_SYNONYM);
        } else {
            header = null;
        }
        if (header != null) {
            header.append(DicIEConstants.CRLF);
            xlsheader = encoder.createobject(header);
        }
        monitor.subTask(Messages.DicExport_monitorOutput);
        monitor.worked(1);

        /* アイテムの書き込み */
        for (Object obj : dic.getItems()) {
            /* 新規　xlsx 出力 */
            /*
             * cdicは問題無いが、fdicは　１行に代表語、従属語を保持している為、 CRLFにてレコード分割を行う
             */
            String retmeisai = encoder.createobject(obj);
            String[] splitstrAry = retmeisai.split(DicIEConstants.CRLF);

            for (int j = 0; j < splitstrAry.length; j++) {
                meisai.add(splitstrAry[j]);
            }

        }

        /* ArrayToXlsfile */
        ArrayToXlsfile xArray = new ArrayToXlsfile(path);

        xArray.createFile(xlsheader, meisai);

        monitor.worked(1);
        monitor.done();

        return Status.OK_STATUS;
    }


    @Override
    public IStatus export(String path, IUserDic dic, Set<ILabelDic> ldics, IProgressMonitor monitor) throws IOException {
        /* ラベル辞書のMAPを作成 */
        labelMap = new HashMap<Integer, List<ILabel>>();
        if (ldics != null && ldics.size() > 0) {
            for (ILabelDic ldic : ldics) {
                for (IDicItem item : ldic.getLabelsRecursive(ldic.getLabels())) {
                    ILabel l = (ILabel) item;
                    for (ITerm t : l.getTerms()) {
                        List<ILabel> list = labelMap.get(t.getId());
                        if (list == null) {
                            list = new ArrayList<ILabel>();
                            labelMap.put(t.getId(), list);
                        }
                        list.add(l);
                    }
                }
            }
        }
        /* ユーザ辞書を渡しているので、XML解析系のExceptionが起こることはありえない */
        try {
            return export(path, dic, monitor);
        } catch (ParserConfigurationException e) {
            return new Status(IStatus.ERROR, IoActivator.PLUGIN_ID, e.getLocalizedMessage());
        } catch (SAXException e) {
            return new Status(IStatus.ERROR, IoActivator.PLUGIN_ID, e.getLocalizedMessage());
        }
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
