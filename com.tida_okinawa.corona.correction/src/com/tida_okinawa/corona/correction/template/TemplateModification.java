/**
 * @version $Id: TemplateModification.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/28 17:58:56
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.template;


/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 */
public class TemplateModification extends TemplateContainer {

    /** 未設定 */
    public static final int TYPE_UNSET = -1;
    /** 依存 */
    public static final int TYPE_DEPEND = 0;
    /** 並列 */
    public static final int TYPE_PARALLEL = 1;


    /**
     * 係り受け
     * 
     * @param parent
     *            親要素
     * @param createDefaultChild
     *            インスタンス化したときに、子のSourceとDestinationを一緒に作るか
     */
    public TemplateModification(TemplateContainer parent, boolean createDefaultChild) {
        super(parent);
        if (createDefaultChild) {
            createChildren();
            source = new TemplateModificationElement(this, TemplateModificationElement.TYPE_SOURCE);
            children.add(source);
            destination = new TemplateModificationElement(this, TemplateModificationElement.TYPE_DEST);
            children.add(destination);
        }
    }

    private int type = TYPE_UNSET;
    private TemplateModificationElement source;
    private TemplateModificationElement destination;


    /**
     * 係り受けタイプ取得
     * 
     * @return 係り受けタイプ
     */
    public int getType() {
        return type;
    }


    /**
     * 係り受けタイプ設定
     * 
     * @param type
     *            係り受けタイプ
     */
    public void setType(int type) {
        if (this.type == type) {
            return;
        }
        this.type = type;
        propertyChanged();
    }


    /**
     * 係り受け元取得
     * 
     * @return 係り受け元
     */
    public TemplateModificationElement getSource() {
        return source;
    }


    /**
     * 係り受け先取得
     * 
     * @return 係り受け先
     */
    public TemplateModificationElement getDestination() {
        return destination;
    }


    @Override
    public String toString() {
        String typeStr = ""; //$NON-NLS-1$
        if (type == TYPE_DEPEND) {
            typeStr = Messages.TEMPLATE_MODIFICATION_TYPE_DEPEND;
        } else if (type == TYPE_PARALLEL) {
            typeStr = Messages.TEMPLATE_MODIFICATION_TYPE_PARALLEL;
        }
        return typeStr + Messages.TEMPLATE_MODIFICATION_STRING;
    }
}
