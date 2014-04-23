/**
 * @version $Id:
 *
 * 2012/07/30 10:45:40
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.data;

import java.util.ArrayList;
import java.util.List;

/**
 * ドキュメント分割情報の定義を保持するクラス
 * 
 * @author shingo-takahashi
 * 
 */
public class CoronaDocumentInformation {
    private String name; //定義名
    private String extension;//デフォルト拡張子

    private List<CoronaDocumentDefinition> definitions;


    /**
     * コンストラクタ
     */
    public CoronaDocumentInformation() {
        super();

        name = ""; //$NON-NLS-1$
        extension = Messages.CoronaDocumentInformation_DefaultValue_Extension;
    }


    /**
     * @return 定義名
     */
    public String getName() {
        return name;
    }


    /**
     * @param name
     *            定義名
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * @return 定義を適用する拡張子
     */
    public String getExtension() {
        return extension;
    }


    /**
     * @param extension
     *            定義を適用する拡張子
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }


    /**
     * @return 定義済み分割情報
     */
    public List<CoronaDocumentDefinition> getDefinitions() {
        if (definitions == null) {
            definitions = new ArrayList<CoronaDocumentDefinition>();
        }
        return definitions;
    }


    /**
     * @param definitions
     *            定義済み分割情報
     */
    public void setDefinitions(List<CoronaDocumentDefinition> definitions) {
        this.definitions = definitions;
    }

}
