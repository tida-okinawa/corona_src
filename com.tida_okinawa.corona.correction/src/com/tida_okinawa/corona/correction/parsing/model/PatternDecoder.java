/**
 * @version $Id: PatternDecoder.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/05 13:04:43
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;

import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.ATTR_BASE;
import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.ATTR_CLASS;
import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.ATTR_ID;
import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.ATTR_LABEL;
import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.ATTR_PART;
import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.ATTR_QUANT;
import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.ATTR_SEARCH;
import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.ATTR_TYPE;
import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.TAG_AND;
import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.TAG_DEST;
import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.TAG_LINK;
import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.TAG_MODIFICATION;
import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.TAG_NOT;
import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.TAG_OR;
import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.TAG_ORDER;
import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.TAG_SEQUENCE;
import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.TAG_SOURCE;
import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.TAG_TERM;
import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.getIntValue;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.Assert;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.tida_okinawa.corona.correction.parsing.model.IModelParser.ModelEncoder;
import com.tida_okinawa.corona.io.model.dic.TermClass;
import com.tida_okinawa.corona.io.model.dic.TermPart;

/**
 * XML形式の構文パターンを、オブジェクトに戻すクラス
 * 
 * @author kousuke-morishima
 */
public class PatternDecoder implements ModelEncoder<String, Pattern> {
    private SAXParser parser;


    /**
     * コンストラクタ
     */
    public PatternDecoder() {
        try {
            parser = SAXParserFactory.newInstance().newSAXParser();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }


    /**
     * xmlタグのencoding属性で指定されている文字コードを取得する.
     * 読み込みの文字コードがワークスペースの文字コードに依存するので、保存してある文字コードで読み込めるようにする.
     * 
     * @param element
     *            XML形式のパターン
     * @return 指定されている文字コード。指定されていなかったらnull
     */
    private static String extractCharset(String element) {
        /* encoding属性を探す */
        String lowerElement = element.toLowerCase();
        int index = lowerElement.indexOf("encoding"); //$NON-NLS-1$
        if (index != -1) {
            int startIndex = lowerElement.indexOf("\"", index) + 1; //$NON-NLS-1$
            int endIndex = lowerElement.indexOf("\"", startIndex); //$NON-NLS-1$
            /* その後ろのダブルクォートからそのまた次のダブルクォートまでが文字コード */
            return element.substring(startIndex, endIndex);
        }
        return null;
    }


    @Override
    public Pattern encode(String element) {
        Assert.isNotNull(element);

        ParseHandler handler = new ParseHandler();
        try {
            String charset = extractCharset(element);
            if (charset == null) {
                parser.parse(new ByteArrayInputStream(element.getBytes()), handler);
            } else {
                parser.parse(new ByteArrayInputStream(element.getBytes(charset)), handler);
            }
        } catch (SAXException e) {
            // Premature end of
            // fileは、子がいないPatternRecordを変換しようとしたときに発生する。処理上の問題はない
            System.out.println(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return handler.getTopPattern();
    }

    class ParseHandler extends DefaultHandler {
        private Pattern topPattern;
        private PatternContainer currentContainer;
        private Pattern currentElement;


        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            currentElement = createPattern(qName, attributes, currentContainer);
            if (topPattern == null) {
                topPattern = currentElement;
            }
            if (currentContainer != null) {
                /* パターンに親子関係を設定 */
                currentContainer.addChild(currentElement);
            }
            if (currentElement instanceof PatternContainer) {
                /* 親パターンを更新 */
                currentContainer = (PatternContainer) currentElement;
            }
        }


        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            if (currentElement instanceof PatternContainer) {
                if (currentElement.getParent() != null) {
                    currentElement = currentContainer = currentElement.getParent();
                }
            } else {
                currentElement = currentContainer;
            }
        }


        /**
         * @return 一番初めに作ったパターン(パターンのルート)
         */
        public Pattern getTopPattern() {
            return topPattern;
        }
    }


    Pattern createPattern(String tag, Attributes attr, PatternContainer parent) {
        Pattern ret = null;
        if (equals(TAG_TERM, tag)) {
            ret = new Term(parent);
            Term term = (Term) ret;
            term.setWord(attr.getValue(ATTR_BASE));
            term.setLabel(attr.getValue(ATTR_LABEL));
            term.setPart(TermPart.valueOf(getIntValue(attr, ATTR_PART, -1)));
            term.setWordClass(TermClass.valueOf(getIntValue(attr, ATTR_CLASS, -1)));
            term.setQuant(QuantifierType.valueOf(getIntValue(attr, ATTR_QUANT, -1)));
        } else if (equals(TAG_ORDER, tag)) {
            ret = new Order(parent);
            Order order = (Order) ret;
            order.setScope(SearchScopeType.valueOf(getIntValue(attr, ATTR_SEARCH, 0)));
        } else if (equals(TAG_SEQUENCE, tag)) {
            ret = new Sequence(parent);
            Sequence sequence = (Sequence) ret;
            sequence.setScope(SearchScopeType.valueOf(getIntValue(attr, ATTR_SEARCH, 0)));
        } else if (equals(TAG_OR, tag)) {
            ret = new OrOperator(parent);
            OrOperator or = (OrOperator) ret;
            or.setScope(SearchScopeType.valueOf(getIntValue(attr, ATTR_SEARCH, 0)));
        } else if (equals(TAG_AND, tag)) {
            ret = new AndOperator(parent);
            AndOperator and = (AndOperator) ret;
            and.setScope(SearchScopeType.valueOf(getIntValue(attr, ATTR_SEARCH, 0)));
        } else if (equals(TAG_NOT, tag)) {
            ret = new NotOperator(parent);
        } else if (equals(TAG_LINK, tag)) {
            ret = new Link(parent);
            Link link = (Link) ret;
            link.setId(getIntValue(attr, ATTR_ID, -1));
        } else if (equals(TAG_SOURCE, tag)) {
            ret = new ModificationElement(parent, PatternKind.MODIFICATION_SOURCE);
        } else if (equals(TAG_DEST, tag)) {
            ret = new ModificationElement(parent, PatternKind.MODIFICATION_DESTINATION);
        } else if (equals(TAG_MODIFICATION, tag)) {
            ret = new Modification(parent, false);
            Modification modifi = (Modification) ret;
            int type = Integer.parseInt(attr.getValue(ATTR_TYPE));
            modifi.setType(type);
        }
        return ret;
    }


    private static boolean equals(String s1, String s2) {
        return s1.equalsIgnoreCase(s2);
    }
}
