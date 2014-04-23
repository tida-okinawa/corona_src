/**
 * @version $Id: SynonymCorrector.java 1840 2014-04-16 05:38:34Z yukihiro-kinjyo $
 * 
 * 2011/10/13 00:35:00
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.synonym;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;

import com.tida_okinawa.corona.correction.morphem.SyntaxStructure;
import com.tida_okinawa.corona.io.model.MorphemeElement;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDicItem;
import com.tida_okinawa.corona.io.model.dic.IFluc;
import com.tida_okinawa.corona.io.model.dic.IFlucDic;
import com.tida_okinawa.corona.io.model.dic.ISynonym;
import com.tida_okinawa.corona.io.model.dic.ISynonymDic;
import com.tida_okinawa.corona.io.model.dic.ITerm;

/**
 * 同義語補正処理
 * 
 * @author imai
 * 
 */
public class SynonymCorrector {
    /**
     * sub term の原形 -> main term のアイテムのマップ
     */
    Map<String, ITerm> map = new HashMap<String, ITerm>();


    /**
     * 
     * @param dics
     *            同義語辞書のリスト
     */
    public SynonymCorrector(List<ICoronaDic> dics) {
    }


    /**
     * 解析に必要な初期化を行う。<br />
     * 初期化に失敗した場合、<code>monitor.setCanceled(true)</code>を呼び出し、初期化処理を中断する
     * 
     * @param dics
     *            解析に使用する辞書
     * @param monitor
     *            進捗表示ダイアログ
     */
    public void init(List<ICoronaDic> dics, IProgressMonitor monitor) {
        if (dics.size() > 0) {
            monitor.beginTask("準備", dics.size());
            monitor.setTaskName("辞書データ準備");
            for (ICoronaDic dic : dics) {
                monitor.subTask(dic.getName());
                if (dic instanceof ISynonymDic) {
                    createMap((ISynonymDic) dic);
                }
                if (dic instanceof IFlucDic) {
                    createMap((IFlucDic) dic);
                }
                monitor.worked(1);
            }
            monitor.done();
            if (map.size() == 0) {
                monitor.setCanceled(true);
            }
        } else {
            monitor.setCanceled(true);
        }
    }


    /**
     * 同義語補正処理
     * 
     * 同義語形態素(Sub)を代表の形態素(Main)に置き換える
     * 
     * @param text
     *            係り受け解析結果
     *            （テキスト）
     * @return 同義語を補正した係り受け解析結果
     */
    public SyntaxStructure process(String text) {
        SyntaxStructure ss = new SyntaxStructure(text);
        process(ss);
        return ss;
    }


    /**
     * 同義語補正処理
     * 
     * 同義語形態素(Sub)を代表の形態素(Main)に置き換える
     * 
     * @param ss
     *            係り受け解析結果 - メソッド内で更新される
     */
    public void process(SyntaxStructure ss) {
        process(ss.getMorphemeElemsnts());
    }


    private void process(List<MorphemeElement> elements) {
        for (MorphemeElement element : elements) {
            process(element);
        }
    }


    private void process(MorphemeElement element) {
        String base = element.getGenkei();
        ITerm main = map.get(base);
        int n = 0;
        while (main != null && n < 100) {
            element.replace(main);

            // 代表後が別の同義語のsubにある場合
            base = main.getValue();
            main = map.get(base);
            n++;
        }
        if (n >= 100) {
            // TODO: ここに出力するとユーザーに見えないけどどうする？
            System.err.println("infinite loop");
            base = main.getValue();
            for (Entry<String, ITerm> entry : map.entrySet()) {
                if (entry.getKey().equals(base)) {
                    System.out.println(entry);
                }
            }
        }
    }


    private void createMap(ISynonymDic dic) {
        for (IDicItem item : dic.getItems()) {
            ISynonym synItem = (ISynonym) item;
            ITerm mainTerm = synItem.getMain();
            if (mainTerm != null) {
                for (ITerm subTerm : synItem.getSub()) {
                    if (subTerm != null) {
                        if (subTerm.getValue().equals(mainTerm.getValue())) {
                            System.err.println(dic.getName() + ": \"" + mainTerm.getValue() + "\"  mainとsubが同じ");
                        } else {
                            map.put(subTerm.getValue(), mainTerm);
                        }
                    }
                }
            }
        }
    }


    private void createMap(IFlucDic dic) {
        for (IDicItem item : dic.getItems()) {
            IFluc fluc = (IFluc) item;
            ITerm mainTerm = fluc.getMain();
            if (mainTerm != null) {
                for (ITerm subTerm : fluc.getSub()) {
                    if (subTerm != null) {
                        if (subTerm.getValue().equals(mainTerm.getValue())) {
                            System.err.println(dic.getName() + ": \"" + mainTerm.getValue() + "\"  mainとsubが同じ");
                        } else {
                            map.put(subTerm.getValue(), mainTerm);
                        }
                    }
                }
            }
        }
    }
}
