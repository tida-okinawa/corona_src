/**
 * @version $Id: TemplateDecoder.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/28 22:40:41
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.template;

import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_BASE;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_CLASS;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_FIX;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_FIX_TRUE;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_ID;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_KIND;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_KIND_LABEL;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_KIND_WORD;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_LABEL;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_PART;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_QUANT;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_SEARCH;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_TYPE;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_VALUE;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.TAG_AND;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.TAG_COMPARE;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.TAG_DEST;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.TAG_LINK;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.TAG_MODIFICATION;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.TAG_NOT;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.TAG_OR;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.TAG_ORDER;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.TAG_SEQUENCE;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.TAG_SOURCE;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.TAG_TERM;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.getIntValue;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.Assert;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.tida_okinawa.corona.correction.parsing.model.CompType;
import com.tida_okinawa.corona.correction.parsing.model.IModelParser.ModelEncoder;
import com.tida_okinawa.corona.correction.parsing.model.QuantifierType;
import com.tida_okinawa.corona.correction.parsing.model.SearchScopeType;
import com.tida_okinawa.corona.io.model.dic.TermClass;
import com.tida_okinawa.corona.io.model.dic.TermPart;

/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 */
public class TemplateDecoder implements ModelEncoder<String, Template> {
    private SAXParser parser;


    /**
     * 文字列（xml形式）をひな型に変換
     */
    public TemplateDecoder() {
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
        int index = lowerElement.indexOf(Messages.TEMPLATE_DECODER_ENCODING);
        if (index != -1) {
            int startIndex = lowerElement.indexOf("\"", index) + 1; //$NON-NLS-1$
            int endIndex = lowerElement.indexOf("\"", startIndex); //$NON-NLS-1$
            /* その後ろのダブルクォートからそのまた次のダブルクォートまでが文字コード */
            return element.substring(startIndex, endIndex);
        }
        return null;
    }


    @Override
    public Template encode(String element) {
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
            System.out.println(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return handler.getTopTemplate();
    }

    class ParseHandler extends DefaultHandler {
        private Template topTemplate;
        private TemplateContainer currentContainer;
        private Template currentElement;


        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            currentElement = createTemplate(qName, attributes, currentContainer);

            if (topTemplate == null) {
                topTemplate = currentElement;
            }
            if (currentContainer != null) {
                /* パターンに親子関係を設定 */
                currentContainer.addChild(currentElement);
            }
            if (currentElement instanceof TemplateContainer) {
                /* 親パターンを更新 */
                currentContainer = (TemplateContainer) currentElement;
            }
        }


        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            if (currentElement instanceof TemplateContainer) {
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
        public Template getTopTemplate() {
            return topTemplate;
        }
    }


    Template createTemplate(String tag, Attributes attr, TemplateContainer parent) {
        Template ret = null;
        if (equals(TAG_TERM, tag)) {
            ret = new TemplateTerm(parent);
            TemplateTerm term = (TemplateTerm) ret;
            term.setWord(attr.getValue(ATTR_BASE));
            term.setLabel(attr.getValue(ATTR_LABEL));
            term.setPart(TermPart.valueOf(getIntValue(attr, ATTR_PART, -1)));
            term.setWordClass(TermClass.valueOf(getIntValue(attr, ATTR_CLASS, -1)));
            term.setQuant(QuantifierType.valueOf(getIntValue(attr, ATTR_QUANT, -1)));
            /* 固定かどうかの判定結果取得 */
            if (attr.getValue(ATTR_FIX) != null) {
                boolean fix = ATTR_FIX_TRUE.equals(attr.getValue(ATTR_FIX));
                ((VariableTemplate) ret).setFixCheck(fix);
                if (fix != true) {
                    /* 固定でない（可変）の場合に、単語（Word）かどうかの判定結果を取得 */
                    if (attr.getValue(ATTR_KIND) != null) {
                        if (ITemplateTermType.TYPE_WORD.equals(attr.getValue(ATTR_KIND))) {
                            ((TemplateTerm) ret).setState(ATTR_KIND_WORD);
                        } else if (ITemplateTermType.TYPE_LABEL.equals(attr.getValue(ATTR_KIND))) {
                            ((TemplateTerm) ret).setState(ATTR_KIND_LABEL);
                        } else {
                            /* 固定でも可変でもないのでnull */
                            ((TemplateTerm) ret).setState(null);
                        }
                    } else {
                        /* 種類の判定が存在しないのでnull */
                        ((TemplateTerm) ret).setState(null);
                    }
                } else {
                    /* 固定の場合に種類は必要ないのでnull */
                    ((TemplateTerm) ret).setState(null);
                }
            } else {
                ((VariableTemplate) ret).setFixCheck(true);
                /* 固定かどうかの判定がないのでnull */
                ((TemplateTerm) ret).setState(null);
            }
        } else if (equals(TAG_ORDER, tag)) {
            ret = new TemplateOrder(parent);
            TemplateOrder order = (TemplateOrder) ret;
            order.setScope(SearchScopeType.valueOf(getIntValue(attr, ATTR_SEARCH, 0)));
        } else if (equals(TAG_SEQUENCE, tag)) {
            ret = new TemplateSequence(parent);
            TemplateSequence sequence = (TemplateSequence) ret;
            sequence.setScope(SearchScopeType.valueOf(getIntValue(attr, ATTR_SEARCH, 0)));
        } else if (equals(TAG_OR, tag)) {
            ret = new TemplateOr(parent);
            TemplateOr or = (TemplateOr) ret;
            or.setScope(SearchScopeType.valueOf(getIntValue(attr, ATTR_SEARCH, 0)));
        } else if (equals(TAG_AND, tag)) {
            ret = new TemplateAnd(parent);
            TemplateAnd and = (TemplateAnd) ret;
            and.setScope(SearchScopeType.valueOf(getIntValue(attr, ATTR_SEARCH, 0)));
        } else if (equals(TAG_NOT, tag)) {
            ret = new TemplateNot(parent);
        } else if (equals(TAG_LINK, tag)) {
            ret = new TemplateLink(parent);
            TemplateLink link = (TemplateLink) ret;
            link.setId(getIntValue(attr, ATTR_ID, -1));
            /* 固定かどうかの判定結果取得 */
            if (attr.getValue(ATTR_FIX) != null) {
                ((VariableTemplate) ret).setFixCheck(ATTR_FIX_TRUE.equals(attr.getValue(ATTR_FIX)) ? true : false);
            } else {
                ((VariableTemplate) ret).setFixCheck(true);
            }
        } else if (equals(TAG_SOURCE, tag)) {
            ret = new TemplateModificationElement(parent, TemplateModificationElement.TYPE_SOURCE);
        } else if (equals(TAG_DEST, tag)) {
            ret = new TemplateModificationElement(parent, TemplateModificationElement.TYPE_DEST);
        } else if (equals(TAG_MODIFICATION, tag)) {
            ret = new TemplateModification(parent, false);
            TemplateModification modifi = (TemplateModification) ret;
            int type = Integer.parseInt(attr.getValue(ATTR_TYPE));
            modifi.setType(type);
        } else if (equals(TAG_COMPARE, tag)) {
            ret = new TemplateCompare(parent);
            TemplateCompare compare = (TemplateCompare) ret;
            compare.setLabel(attr.getValue(ATTR_LABEL));
            compare.setType(CompType.getValue(getIntValue(attr, ATTR_TYPE, -1)));
            compare.setValue(getIntValue(attr, ATTR_VALUE, 0));
        }
        return ret;
    }


    private static boolean equals(String s1, String s2) {
        return s1.equalsIgnoreCase(s2);
    }
}
