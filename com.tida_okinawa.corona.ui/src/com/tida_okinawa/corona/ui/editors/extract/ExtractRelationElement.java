/**
 * @version $Id: ExtractRelationElement.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/11 15:11:59
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.extract;

import java.util.List;

import com.tida_okinawa.corona.correction.morphem.ISyntaxStructureElement;
import com.tida_okinawa.corona.io.model.MorphemeElement;

/**
 * @author s.takuro
 *         #177 パターン自動生成（係り受け抽出）
 */
public class ExtractRelationElement implements IExtractRelationElement {

    private int elementCnt;
    private List<MorphemeElement> morphemeElement = null;
    private String hyouki = "";
    private String yomi = "";
    private IExtractRelationElement dst = null;
    private boolean completion = false;


    /**
     * 係り受け抽出の要素の管理
     */
    public ExtractRelationElement() {
        this.elementCnt = 1;
    }


    @Override
    public boolean convertSyntaxToExtractRelation(ISyntaxStructureElement sse) {
        if (sse == null) {
            return false;
        }
        setMorphemes(sse.getMorphemes());
        setHyouki(sse.getHyouki());
        setYomi(sse.getYomi());
        setDependDestination(sse.getDependDestination());
        return true;
    }


    @Override
    public void incCount() {
        elementCnt++;
    }


    @Override
    public int getCount() {
        return elementCnt;
    }


    @Override
    public void setMorphemes(List<MorphemeElement> morphemeElement) {
        this.morphemeElement = morphemeElement;
    }


    @Override
    public List<MorphemeElement> getMorphemes() {
        return morphemeElement;
    }


    @Override
    public void setHyouki(String hyouki) {
        this.hyouki = hyouki;
    }


    @Override
    public String getHyouki() {
        return hyouki;
    }


    @Override
    public void setYomi(String yomi) {
        this.yomi = yomi;
    }


    @Override
    public String getYomi() {
        return yomi;
    }


    @Override
    public boolean setDependDestination(ISyntaxStructureElement sse) {
        if (sse == null) {
            return false;
        }
        dst = new ExtractRelationElement();
        dst.setMorphemes(sse.getMorphemes());
        dst.setHyouki(sse.getHyouki());
        dst.setYomi(sse.getYomi());
        return true;
    }


    @Override
    public IExtractRelationElement getDependDestination() {
        return dst;
    }


    @Override
    public boolean setExtractRelationElement(List<MorphemeElement> morphemeEre, List<MorphemeElement> morphemeDst) {
        /* 係り元と係り先の両方がnullであるかどうかを判定するためのフラグ */
        boolean isNotNull = false;

        /* 係り元 */
        StringBuilder hyoukiEre = new StringBuilder(100);
        StringBuilder yomiEre = new StringBuilder(100);
        if (morphemeEre != null) {
            for (MorphemeElement morpheme : morphemeEre) {
                if (morpheme != null) {
                    hyoukiEre.append(morpheme.getHyouki());
                    yomiEre.append(morpheme.getYomi());
                    isNotNull = true;
                }
            }
        }

        /* 係り先 */
        StringBuilder hyoukiDst = new StringBuilder(100);
        StringBuilder yomiDst = new StringBuilder(100);
        if (morphemeDst != null) {
            for (MorphemeElement morpheme : morphemeDst) {
                if (morpheme != null) {
                    hyoukiDst.append(morpheme.getHyouki());
                    yomiDst.append(morpheme.getYomi());
                    isNotNull = true;
                }
            }
        }
        if (isNotNull != true) {
            return false;
        }
        setHyouki(hyoukiEre.toString());
        setYomi(yomiEre.toString());
        setMorphemes(morphemeEre);
        dst = new ExtractRelationElement();
        dst.setHyouki(hyoukiDst.toString());
        dst.setYomi(yomiDst.toString());
        dst.setMorphemes(morphemeDst);
        return true;
    }


    @Override
    public boolean getCompletion() {
        return completion;
    }


    @Override
    public void setCompletion(boolean completion) {
        this.completion = completion;
    }
}
