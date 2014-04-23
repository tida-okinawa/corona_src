/**
 * @version $Id: ExtractRelation.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/02/06 16:41:09
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.extract;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.tida_okinawa.corona.correction.morphem.ISyntaxStructureElement;
import com.tida_okinawa.corona.correction.morphem.SyntaxStructure;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.MorphemeElement;
import com.tida_okinawa.corona.io.model.dic.TermPart;
import com.tida_okinawa.corona.io.model.table.ITextRecord;

/**
 * @author s.takuro
 *         #177 係り受け抽出
 */
public class ExtractRelation {

    /**
     * 係り受け抽出
     * 
     * @param claimWorkData
     *            問い合わせデータ
     * @return 係り受け抽出結果
     * @throws InvocationTargetException
     *             例外
     * @throws InterruptedException
     *             例外
     */
    public IExtractRelationElement[] execRelation(final IClaimWorkData claimWorkData) throws InvocationTargetException, InterruptedException {
        final List<ITextRecord> claimWorkDatas = claimWorkData.getClaimWorkDatas();
        final List<IExtractRelationElement> extractRelationList = new ArrayList<IExtractRelationElement>(claimWorkDatas.size());
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        final Shell shell = window.getShell();
        ProgressMonitorDialog dialog1 = new ProgressMonitorDialog(shell);
        dialog1.run(true, false, new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

                monitor.beginTask(Messages.EXTRACT_RELATION_MONITOR_BIGINTASK, 3);
                monitor.subTask(Messages.EXTRACT_RELATION_MONITOR_SUBTASK);

                List<IExtractRelationElement> extractRelationAll = new ArrayList<IExtractRelationElement>(claimWorkDatas.size());
                Map<String, IExtractRelationElement> countMap = new HashMap<String, IExtractRelationElement>();
                monitor.worked(1);
                for (ITextRecord record : claimWorkDatas) {
                    SyntaxStructure ss = new SyntaxStructure(record.getText());
                    for (int i = 0; i < ss.size(); i++) {
                        ISyntaxStructureElement sse = ss.get(i);
                        ISyntaxStructureElement dstSse = sse.getDependDestination();
                        if (dstSse == null) {
                            /* 係り先が存在しない場合は登録しない */
                            continue;
                        }
                        /* 既に抽出された係り受けかどうかの判定 */
                        StringBuilder sseHyouki = new StringBuilder(100);
                        sseHyouki.append(sse.getHyouki()).append(" ").append(dstSse.getHyouki()); //$NON-NLS-1$
                        String strHyouki = sseHyouki.toString();
                        if (countMap.containsKey(strHyouki)) {
                            IExtractRelationElement ere = countMap.get(strHyouki);
                            ere.incCount();
                        } else {
                            /* Mapに存在しないキーの場合は新規作成 */
                            ExtractRelationElement input = new ExtractRelationElement();
                            if (input.convertSyntaxToExtractRelation(sse)) {
                                extractRelationAll.add(input);
                                countMap.put(sseHyouki.toString(), input);
                            }
                        }
                    }
                }
                monitor.worked(1);
                /* 出現回数が1回しかないアイテムを削除 */
                for (Entry<String, IExtractRelationElement> entry : countMap.entrySet()) {
                    entry.getValue().getCount();
                }
                for (IExtractRelationElement extractRelation : extractRelationAll) {
                    if (extractRelation.getCount() > 1) {
                        extractRelationList.add(extractRelation);
                    }
                }
                /* ソート */
                Collections.sort(extractRelationList, new Comparator<IExtractRelationElement>() {
                    @Override
                    public int compare(IExtractRelationElement o1, IExtractRelationElement o2) {
                        int n1 = o1.getCount();
                        int n2 = o2.getCount();
                        /* 降順で並び替え（抽出数が多い方が上に来るように） */
                        if (n1 < n2) {
                            return 1;
                        } else if (n1 == n2) {
                            //TODO 処理回数が同じだった場合に係り元の文字でソートすべきか。
                            return 0;
                            //return o1.getHyouki().compareTo(o2.getHyouki());
                            //return o1.getYomi().compareTo(o2.getYomi());
                        } else {
                            return -1;
                        }
                    }
                });
                monitor.worked(1);
                monitor.done();
            }
        });
        return extractRelationList.toArray(new IExtractRelationElement[extractRelationList.size()]);
    }


    /**
     * 係り受け抽出結果から登録候補を作成する
     * 
     * @param element
     *            係り受け抽出結果
     * @return 登録候補の係り受け一式
     */
    public IExtractRelationElement[] createRelationDetail(final ExtractRelationElement element) {
        List<MorphemeElement> inputEre = element.getMorphemes();
        List<MorphemeElement> inputDst = element.getDependDestination().getMorphemes();
        List<MorphemeElement[]> outputEre = createOutputMorpheme(inputEre);
        List<MorphemeElement[]> outputDst = createOutputMorpheme(inputDst);
        List<IExtractRelationElement> extractRelationDetail = new ArrayList<IExtractRelationElement>(100);

        for (int posEre = 0; posEre < outputEre.size(); posEre++) {
            for (int posDst = 0; posDst < outputDst.size(); posDst++) {
                IExtractRelationElement ere = new ExtractRelationElement();
                if (ere.setExtractRelationElement(Arrays.asList(outputEre.get(posEre)), Arrays.asList(outputDst.get(posDst)))) {
                    extractRelationDetail.add(ere);
                }
            }
        }
        return extractRelationDetail.toArray(new IExtractRelationElement[extractRelationDetail.size()]);
    }


    /**
     * 係り元、係り先の形態素の候補の作成
     * 形態素を助詞で区切ってまとめる
     * 但し、3語以上の場合はまとめない
     * 
     * @param input
     *            形態素一覧
     * @return 形態素の候補
     */
    @SuppressWarnings("static-method")
    private List<MorphemeElement[]> createOutputMorpheme(List<MorphemeElement> input) {
        List<MorphemeElement[]> output = new ArrayList<MorphemeElement[]>(100);
        int posPartition = -1;
        for (int index = 0; index < input.size(); index++) {
            /* 助詞を持つ＆助詞が先頭以外で助詞のあとに形態素が続く場合は、3語以上になるので区切らない */
            if ((posPartition != -1) && (posPartition != 0 && posPartition < index)) {
                posPartition = -1;
                break;
            }
            /* 助詞の形態素を持つかどうか判定 */
            if (TermPart.POSTPOSITIONAL_PARTICLE.getName().equals(input.get(index).getHinshi())) {
                /* 既に助詞の位置が設定されている状態で、他に助詞がヒットした場合は、助詞が2つ以上あることになるので区切らない */
                if (posPartition != -1) {
                    posPartition = -1;
                    break;
                }
                posPartition = index;
            }
        }
        /* (A)inputの値をそのまま入れる */
        output.add(input.toArray(new MorphemeElement[input.size()]));

        /* (B)助詞を1つ持つ3語未満の要素の場合 */
        if (posPartition != -1) {
            List<MorphemeElement> tmpParticle = new ArrayList<MorphemeElement>(input.size());
            List<MorphemeElement> tmpNoun = new ArrayList<MorphemeElement>(input.size());
            /* 先頭に助詞がある場合 */
            if (posPartition == 0) {
                /* 助詞の追加 */
                tmpParticle.add(input.get(posPartition));
                tmpParticle.add(null);
                /* 助詞以外の追加 */
                for (int pos = posPartition; pos < input.size(); pos++) {
                    tmpNoun.add(input.get(pos));
                }
            }
            /* 末尾に助詞がある場合 */
            else {
                /* 助詞の追加 */
                tmpParticle.add(null);
                tmpParticle.add(input.get(posPartition));
                /* 助詞以外の追加 */
                for (int pos = 0; pos < posPartition; pos++) {
                    tmpNoun.add(input.get(pos));
                }
            }
            output.add(tmpParticle.toArray(new MorphemeElement[tmpParticle.size()]));
            output.add(tmpNoun.toArray(new MorphemeElement[tmpNoun.size()]));
        }

        /* (C)空データを入れる */
        output.add(new MorphemeElement[0]);

        return output;
    }
}
