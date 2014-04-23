/**
 * @version $Id: TemplateEncoder.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/28 22:40:59
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.template;

import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_BASE;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_CLASS;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_FIX;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_FIX_FALSE;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_FIX_TRUE;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_ID;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_KIND;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_KIND_LABEL;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_KIND_NULL;
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
import static com.tida_okinawa.corona.correction.template.TemplateUtil.TAG_PATTERN;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.TAG_SEQUENCE;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.TAG_SOURCE;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.TAG_TERM;

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

import com.tida_okinawa.corona.correction.parsing.model.IModelParser.ModelEncoder;

/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 */
public class TemplateEncoder implements ModelEncoder<Template, String> {
    private TransformerHandler handler;
    private ByteArrayOutputStream output = new ByteArrayOutputStream();


    /**
     * ひな型を文字列（xml形式）に変換
     */
    public TemplateEncoder() {
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
     * &lt;Template arguments="value" /&gt;
     * </p>
     * の形で結果が出てくると思われる
     * 
     * @param element
     *            変換するパターン
     * @return 変換した文字列
     */
    public String encodeOwn(Template element) {
        if (element == null) {
            return ""; //$NON-NLS-1$
        }
        String xmlString = ""; //$NON-NLS-1$
        if (element instanceof TemplateContainer) {
            List<Template> tmp = new ArrayList<Template>(((TemplateContainer) element).getChildren());
            ((TemplateContainer) element).getChildren().clear();
            xmlString = encode(element);
            ((TemplateContainer) element).getChildren().addAll(tmp);
        } else {
            xmlString = encode(element);
        }
        return xmlString;
    }


    /**
     * 与えられたTemplateの子を含めて文字列（XML形式）に変換する
     * 
     * @param element
     *            変換するパターン
     */
    @Override
    public String encode(Template element) {
        if (element == null) {
            return ""; //$NON-NLS-1$
        }

        String xmlString = ""; //$NON-NLS-1$
        try {
            String encode = System.getProperty(Messages.TEMPLATE_ENCODER_ENCODING);
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
     * startDocumentしない 子を再帰的に文字列化
     * 
     * @param element
     *            要素
     * @throws SAXException
     *             例外
     */
    private void convertString(Template element) throws SAXException {
        if (element == null) {
            new Exception(Messages.TEMPLATE_ENCODER_STRING_NULL).printStackTrace();
        }

        /* 自身を文字列化 */
        String tag = startElement(element);
        if (element instanceof TemplateContainer) {
            // 子を文字列化
            List<Template> children = ((TemplateContainer) element).getChildren();
            for (Template p : children) {
                convertString(p);
            }
        }
        endElement(tag);
    }


    private String startElement(Template p) throws SAXException {
        String tag = getTag(p);
        handler.startElement("", "", tag, createAttr(p)); //$NON-NLS-1$ //$NON-NLS-2$
        return tag;
    }


    private void endElement(String tag) throws SAXException {
        handler.endElement("", "", tag); //$NON-NLS-1$ //$NON-NLS-2$
    }


    private static AttributesImpl createAttr(Template p) {
        AttributesImpl attr = new AttributesImpl();
        if (p instanceof TemplateTerm) {
            addAttr(attr, ATTR_BASE, ((TemplateTerm) p).getWord());
            addAttr(attr, ATTR_PART, String.valueOf(((TemplateTerm) p).getPart().getIntValue()));
            addAttr(attr, ATTR_CLASS, String.valueOf(((TemplateTerm) p).getWordClass().getIntValue()));
            addAttr(attr, ATTR_LABEL, ((TemplateTerm) p).getLabel());
            addAttr(attr, ATTR_QUANT, (((TemplateTerm) p).getQuant() != null) ? String.valueOf(((TemplateTerm) p).getQuant().getIntValue()) : "-1"); //$NON-NLS-1$
            /* 固定かどうかの判定を追加 */
            boolean fix = (((TemplateTerm) p).getFixCheck() == true);
            addAttr(attr, ATTR_FIX, fix ? ATTR_FIX_TRUE : ATTR_FIX_FALSE);

            /* 固定かどうかに関わらず種類は設定しておく */
            String state = ((TemplateTerm) p).getState();
            if (state == null) {
                addAttr(attr, ATTR_KIND, ATTR_KIND_NULL);
            }
            /* 単語の場合 */
            else if (ITemplateTermType.TYPE_WORD.equals(state)) {
                addAttr(attr, ATTR_KIND, ATTR_KIND_WORD);
            }
            /* ラベルの場合 */
            else if (ITemplateTermType.TYPE_LABEL.equals(state)) {
                addAttr(attr, ATTR_KIND, ATTR_KIND_LABEL);
            }
            /* 上記以外の場合 */
            else {
                addAttr(attr, ATTR_KIND, ATTR_KIND_NULL);
            }
        } else if (p instanceof TemplateOrder) {
            addAttr(attr, ATTR_SEARCH, (((TemplateOrder) p).getScope() != null) ? String.valueOf(((TemplateOrder) p).getScope().getIntValue()) : "0"); //$NON-NLS-1$
        } else if (p instanceof TemplateSequence) {
            addAttr(attr, ATTR_SEARCH, (((TemplateSequence) p).getScope() != null) ? String.valueOf(((TemplateSequence) p).getScope().getIntValue()) : "0"); //$NON-NLS-1$
        } else if (p instanceof TemplateOr) {
            addAttr(attr, ATTR_SEARCH, (((TemplateOr) p).getScope() != null) ? String.valueOf(((TemplateOr) p).getScope().getIntValue()) : "0"); //$NON-NLS-1$
        } else if (p instanceof TemplateAnd) {
            addAttr(attr, ATTR_SEARCH, (((TemplateAnd) p).getScope() != null) ? String.valueOf(((TemplateAnd) p).getScope().getIntValue()) : "0"); //$NON-NLS-1$
        } else if (p instanceof TemplateNot) {
        } else if (p instanceof TemplateLink) {
            addAttr(attr, ATTR_ID, String.valueOf(((TemplateLink) p).getId()));
            /* 固定かどうかの判定の追加 */
            addAttr(attr, ATTR_FIX, (((TemplateLink) p).getFixCheck() == true) ? ATTR_FIX_TRUE : ATTR_FIX_FALSE);
        } else if (p instanceof TemplateModificationElement) {
        } else if (p instanceof TemplateModification) {
            addAttr(attr, ATTR_TYPE, String.valueOf(((TemplateModification) p).getType()));
        } else if (p instanceof TemplateCompare) {
            addAttr(attr, ATTR_LABEL, ((TemplateCompare) p).getLabel());
            addAttr(attr, ATTR_TYPE, (((TemplateCompare) p).getType() != null) ? String.valueOf(((TemplateCompare) p).getType().getInt()) : "-1"); //$NON-NLS-1$
            addAttr(attr, ATTR_VALUE, String.valueOf(((TemplateCompare) p).getValue()));
        }
        return attr;
    }


    private static void addAttr(AttributesImpl attr, String label, String value) {
        attr.addAttribute("", "", label, Messages.TEMPLATE_ENCODER_CDDATA, value); //$NON-NLS-1$ //$NON-NLS-2$
    }


    private static String getTag(Template element) {
        String tag = ""; //$NON-NLS-1$
        if (element instanceof TemplateTerm) {
            tag = TAG_TERM;
        } else if (element instanceof TemplateOrder) {
            tag = TAG_ORDER;
        } else if (element instanceof TemplateSequence) {
            tag = TAG_SEQUENCE;
        } else if (element instanceof TemplateOr) {
            tag = TAG_OR;
        } else if (element instanceof TemplateAnd) {
            tag = TAG_AND;
        } else if (element instanceof TemplateNot) {
            tag = TAG_NOT;
        } else if (element instanceof TemplateLink) {
            tag = TAG_LINK;
        } else if (element instanceof TemplateModificationElement) {
            if (((TemplateModificationElement) element).getType() == TemplateModificationElement.TYPE_SOURCE) {
                tag = TAG_SOURCE;
            } else if (((TemplateModificationElement) element).getType() == TemplateModificationElement.TYPE_DEST) {
                tag = TAG_DEST;
            }
        } else if (element instanceof TemplateModification) {
            tag = TAG_MODIFICATION;
        } else if (element instanceof TemplateRecord) {
            tag = TAG_PATTERN;
        } else if (element instanceof TemplateCompare) {
            tag = TAG_COMPARE;
        }
        return tag;
    }
}
