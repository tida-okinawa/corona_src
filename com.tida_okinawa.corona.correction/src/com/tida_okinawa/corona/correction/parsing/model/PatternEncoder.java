/**
 * @version $Id: PatternEncoder.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/05 11:38:20
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
import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.TAG_PATTERN;
import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.TAG_SEQUENCE;
import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.TAG_SOURCE;
import static com.tida_okinawa.corona.correction.parsing.model.DicPatternUtil.TAG_TERM;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * PatternをXML形式の文字列にするクラス
 * 
 * @author kousuke-morishima
 */
class PatternEncoder implements IModelParser.ModelEncoder<Pattern, String> {
    private TransformerHandler handler;
    private ByteArrayOutputStream output = new ByteArrayOutputStream();


    public PatternEncoder() {
        try {
            handler = ((SAXTransformerFactory) TransformerFactory.newInstance()).newTransformerHandler();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {
            e.printStackTrace();
        }
    }


    /**
     * 与えられたパターンのみを文字列（XML形式）に変換する。子がいても、
     * <p>
     * &lt;Pattern arguments="value" /&gt;
     * </p>
     * の形で結果が出てくると思われる
     * 
     * @param element
     *            変換するパターン
     * @return
     */
    public String encodeOwn(Pattern element) {
        if (element == null) {
            return ""; //$NON-NLS-1$
        }
        String xmlString = ""; //$NON-NLS-1$
        if (element instanceof PatternContainer) {
            List<Pattern> tmp = new ArrayList<Pattern>(((PatternContainer) element).getChildren());
            ((PatternContainer) element).getChildren().clear();
            xmlString = encode(element);
            ((PatternContainer) element).getChildren().addAll(tmp);
        } else {
            xmlString = encode(element);
        }
        return xmlString;
    }


    /**
     * 与えられたPatternの子を含めて文字列（XML形式）に変換する
     * 
     * @param element
     *            変換するパターン
     */
    @Override
    public String encode(Pattern element) {
        if (element == null) {
            return ""; //$NON-NLS-1$
        }

        String xmlString = ""; //$NON-NLS-1$
        try {
            String encode = System.getProperty("file.encoding"); //$NON-NLS-1$
            handler.getTransformer().setOutputProperty(OutputKeys.ENCODING, encode);
            output = new ByteArrayOutputStream();
            handler.setResult(new StreamResult(output));
            handler.startDocument();
            convertString(element);
            handler.endDocument();
            xmlString = output.toString(encode);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return xmlString;
    }


    /**
     * startDocumentしない
     * 子を再帰的に文字列化
     * 
     * @param element
     * @return
     * @throws SAXException
     */
    private void convertString(Pattern element) throws SAXException {
        if (element == null) {
            new Exception("変換元のelementがnullです").printStackTrace();
        }

        /* 自身を文字列化 */
        String tag = startElement(element);
        if (element instanceof PatternContainer) {
            // 子を文字列化
            List<Pattern> children = ((PatternContainer) element).getChildren();
            for (Pattern p : children) {
                convertString(p);
            }
        }
        endElement(tag);
    }


    private String startElement(Pattern p) throws SAXException {
        String tag = getTag(p);
        handler.startElement("", "", tag, createAttr(p)); //$NON-NLS-1$ //$NON-NLS-2$
        return tag;
    }


    private void endElement(String tag) throws SAXException {
        handler.endElement("", "", tag); //$NON-NLS-1$ //$NON-NLS-2$
    }


    private static AttributesImpl createAttr(Pattern p) {
        // enumの値は、数値で格納
        AttributesImpl attr = new AttributesImpl();
        if (p instanceof Term) {
            addAttr(attr, ATTR_BASE, ((Term) p).getWord());
            addAttr(attr, ATTR_CLASS, String.valueOf(((Term) p).getWordClass().getIntValue()));
            addAttr(attr, ATTR_LABEL, ((Term) p).getLabel());
            addAttr(attr, ATTR_PART, String.valueOf(((Term) p).getPart().getIntValue()));
            addAttr(attr, ATTR_QUANT, (((Term) p).getQuant() != null) ? String.valueOf(((Term) p).getQuant().getIntValue()) : "-1"); //$NON-NLS-1$
        } else if (p instanceof Order) {
            addAttr(attr, ATTR_SEARCH, (((Order) p).getScope() != null) ? String.valueOf(((Order) p).getScope().getIntValue()) : "0"); //$NON-NLS-1$
        } else if (p instanceof Sequence) {
            addAttr(attr, ATTR_SEARCH, (((Sequence) p).getScope() != null) ? String.valueOf(((Sequence) p).getScope().getIntValue()) : "0"); //$NON-NLS-1$
        } else if (p instanceof OrOperator) {
            addAttr(attr, ATTR_SEARCH, (((OrOperator) p).getScope() != null) ? String.valueOf(((OrOperator) p).getScope().getIntValue()) : "0"); //$NON-NLS-1$
        } else if (p instanceof AndOperator) {
            addAttr(attr, ATTR_SEARCH, (((AndOperator) p).getScope() != null) ? String.valueOf(((AndOperator) p).getScope().getIntValue()) : "0"); //$NON-NLS-1$
        } else if (p instanceof NotOperator) {
        } else if (p instanceof Link) {
            addAttr(attr, ATTR_ID, String.valueOf(((Link) p).getId()));
        } else if (p instanceof ModificationElement) {
        } else if (p instanceof Modification) {
            addAttr(attr, ATTR_TYPE, String.valueOf(((Modification) p).getType()));
        }
        return attr;
    }


    private static void addAttr(AttributesImpl attr, String label, String value) {
        attr.addAttribute("", "", label, "CDATA", value); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }


    private static String getTag(Pattern element) {
        String tag = ""; //$NON-NLS-1$
        if (element instanceof Term) {
            tag = TAG_TERM;
        } else if (element instanceof Order) {
            tag = TAG_ORDER;
        } else if (element instanceof Sequence) {
            tag = TAG_SEQUENCE;
        } else if (element instanceof OrOperator) {
            tag = TAG_OR;
        } else if (element instanceof AndOperator) {
            tag = TAG_AND;
        } else if (element instanceof NotOperator) {
            tag = TAG_NOT;
        } else if (element instanceof Link) {
            tag = TAG_LINK;
        } else if (element instanceof ModificationElement) {
            if (PatternKind.MODIFICATION_SOURCE.equals(((ModificationElement) element).getKind())) {
                tag = TAG_SOURCE;
            } else if (PatternKind.MODIFICATION_DESTINATION.equals(((ModificationElement) element).getKind())) {
                tag = TAG_DEST;
            }
        } else if (element instanceof Modification) {
            tag = TAG_MODIFICATION;
        } else if (element instanceof PatternRecord) {
            tag = TAG_PATTERN;
        }
        return tag;
    }
}
