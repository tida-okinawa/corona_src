/**
 * @version $Id: ExtractCooccurrenceElement.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/25 13:41:36
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.extract;

import java.util.ArrayList;
import java.util.List;

/**
 * @author s.takuro
 * 
 */
public class ExtractCooccurrenceElement implements IExtractCooccurrenceElement {

    private List<String> tmtResult = null;
    private String patternType = "";
    private boolean completion = false;


    /**
     * 抽出した共起語の管理
     * 
     * @param text
     *            抽出した共起語
     */
    public ExtractCooccurrenceElement(String text) {
        init(text);
    }


    private void init(String text) {
        String[] parts = text.split(",");
        tmtResult = new ArrayList<String>(parts.length);
        for (String part : parts) {
            tmtResult.add(part);
        }
    }


    @Override
    public List<String> getTerms() {
        List<String> terms = new ArrayList<String>(100);
        /* 先頭の出現数は除く */
        for (int i = 1; i < tmtResult.size(); i++) {
            terms.add(tmtResult.get(i));
        }
        return terms;
    }


    @Override
    public String getTerm(int pos) {
        if (tmtResult == null || pos >= tmtResult.size()) {
            return "";
        }
        return tmtResult.get(pos);
    }


    @Override
    public String getCount() {
        return (tmtResult != null) ? tmtResult.get(0) : "";
    }


    @Override
    public int getSize() {
        /* 先頭はヒット数なので */
        return tmtResult.size() - 1;
    }


    @Override
    public String getPatternType() {
        return patternType;
    }


    @Override
    public void setPatternType(String patternType) {
        if (patternType == null) {
            this.patternType = "";
            return;
        }
        this.patternType = patternType;
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
