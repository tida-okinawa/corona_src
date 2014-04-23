/**
 * @version $Id: PatternModelParser.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/05 10:09:35
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;

/**
 * パターン辞書の表示モデル(Pattern)と、XML形式を相互変換する
 * 
 * @author kousuke-morishima
 */
public class PatternModelParser implements IModelParser<Pattern, String> {
    private static final PatternEncoder encoder = new PatternEncoder();
    private static final PatternDecoder decoder = new PatternDecoder();


    @Override
    public ModelEncoder<String, Pattern> getDecoder() {
        return decoder;
    }


    @Override
    public ModelEncoder<Pattern, String> getEncoder() {
        return encoder;
    }
}
