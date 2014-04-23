/**
 * @version $Id: TemplateModelParser.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/28 23:42:44
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.template;

import com.tida_okinawa.corona.correction.parsing.model.IModelParser;


/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 *         ひな型の表示モデル(Template)と、XML形式を相互変換する
 */
public class TemplateModelParser implements IModelParser<Template, String> {
    private static final TemplateEncoder encoder = new TemplateEncoder();
    private static final TemplateDecoder decoder = new TemplateDecoder();


    @Override
    public ModelEncoder<String, Template> getDecoder() {
        return decoder;
    }


    @Override
    public ModelEncoder<Template, String> getEncoder() {
        return encoder;
    }
}
