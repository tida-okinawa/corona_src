/**
 * @version $Id: TemplateTreeLabelProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/12/05 16:40:50
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.template;

import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_KIND_LABEL;
import static com.tida_okinawa.corona.correction.template.TemplateUtil.ATTR_KIND_WORD;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.tida_okinawa.corona.correction.template.IVariableTemplate;
import com.tida_okinawa.corona.correction.template.Template;
import com.tida_okinawa.corona.correction.template.TemplateContainer;
import com.tida_okinawa.corona.correction.template.TemplateLink;
import com.tida_okinawa.corona.correction.template.TemplateRecord;
import com.tida_okinawa.corona.correction.template.TemplateTerm;
import com.tida_okinawa.corona.correction.template.VariableTemplate;
import com.tida_okinawa.corona.ui.Icons;

/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 *         Tree用のラベルプロバイダ
 */
public class TemplateTreeLabelProvider extends LabelProvider implements IColorProvider {

    private static List<Template> variableTemplates = null;
    private Color color = Display.getCurrent().getSystemColor(SWT.COLOR_RED);


    @Override
    public String getText(Object element) {

        setVariableElement(element);

        if (element instanceof Template) {
            /* 単語（Term）の場合 */
            if (element instanceof TemplateTerm) {
                TemplateTerm term = (TemplateTerm) element;
                if (term.getFixCheck() != true) {
                    /* 要素の位置を取得 */
                    int cnt = getVariableElementPos(term);
                    /* 単語（Word） */
                    if (ATTR_KIND_WORD.equals(term.getState())) {
                        StringBuilder buf = new StringBuilder(128);
                        buf.append(Messages.TEMPLATE_OUTPUT_LESS_THAN).append(Messages.TEMPLATE_OUTPUT_ELEMENT).append(cnt)
                                .append(Messages.TEMPLATE_OUTPUT_GREATER_THAN).append(Messages.TEMPLATE_OUTPUT_COLON1);
                        buf.append((term.getPart() == null) ? "" : term.getPart().getName()).append(Messages.TEMPLATE_OUTPUT_COLON2); //$NON-NLS-1$
                        buf.append((term.getWordClass() == null) ? "" : term.getWordClass().getName()).append(Messages.TEMPLATE_OUTPUT_COLON2); //$NON-NLS-1$
                        buf.append("").append(Messages.TEMPLATE_OUTPUT_COLON2); //$NON-NLS-1$
                        buf.append((term.getQuant() == null) ? "" : term.getQuant().getName()); //$NON-NLS-1$
                        buf.append(Messages.TEMPLATE_OUTPUT_TERM);
                        return buf.toString();
                    }
                    /* ラベル（Label） */
                    else if (ATTR_KIND_LABEL.equals(term.getState())) {
                        StringBuilder buf = new StringBuilder(128);
                        buf.append("").append(Messages.TEMPLATE_OUTPUT_COLON1); //$NON-NLS-1$
                        buf.append((term.getPart() == null) ? "" : term.getPart().getName()).append(Messages.TEMPLATE_OUTPUT_COLON2); //$NON-NLS-1$
                        buf.append((term.getWordClass() == null) ? "" : term.getWordClass().getName()).append(Messages.TEMPLATE_OUTPUT_COLON2); //$NON-NLS-1$
                        buf.append(Messages.TEMPLATE_OUTPUT_LESS_THAN).append(Messages.TEMPLATE_OUTPUT_ELEMENT).append(cnt)
                                .append(Messages.TEMPLATE_OUTPUT_GREATER_THAN).append(Messages.TEMPLATE_OUTPUT_COLON2);
                        buf.append((term.getQuant() == null) ? "" : term.getQuant().getName()); //$NON-NLS-1$
                        buf.append(Messages.TEMPLATE_OUTPUT_TERM);
                        return buf.toString();
                    }
                    /* その他（エラー） */
                    else {
                        return Messages.TEMPLATE_ERROR_TYPE;
                    }
                }
            }
            /* 参照（Link）の場合 */
            else if (element instanceof TemplateLink) {
                TemplateLink link = (TemplateLink) element;
                if (link.getFixCheck() != true) {
                    /* 要素の位置を取得 */
                    int cnt = getVariableElementPos(link);
                    return Messages.TEMPLATE_OUTPUT_LESS_THAN + Messages.TEMPLATE_OUTPUT_ELEMENT + cnt + Messages.TEMPLATE_OUTPUT_GREATER_THAN
                            + Messages.TEMPLATE_OUTPUT_LINK;
                }
            }
            return ((Template) element).toString();
        }
        return super.getText(element);
    }


    @Override
    public Image getImage(Object element) {
        final Icons icon = Icons.INSTANCE;
        if (element instanceof TemplateRecord) {
            if (((TemplateRecord) element).isPart()) {
                return icon.get(Icons.IMG_PATTERN_PART);
            }
            return icon.get(Icons.IMG_PATTERN_RECORD);
        }
        return null;
    }


    @Override
    public Color getForeground(Object element) {
        if ((element instanceof TemplateTerm) || (element instanceof TemplateLink)) {
            if (((VariableTemplate) element).getFixCheck() != true) {
                return color;
            }
        }
        return null;
    }


    @Override
    public Color getBackground(Object element) {
        return null;
    }


    /**
     * 可変の要素を記憶する
     * 
     * @param element
     *            選択中の要素
     */
    private void setVariableElement(Object element) {
        if (element instanceof Template) {
            variableTemplates = new ArrayList<Template>(100);
            Template template = (Template) element;
            while (template.getParent() != null) {
                template = template.getParent();
            }
            recursiveSetVariableElement(template);
        }
    }


    /**
     * 再帰的に状態が可変の要素をセットする
     * 
     * @param template
     *            ひな型
     */
    private void recursiveSetVariableElement(Template template) {
        if (template == null) {
            new Exception(Messages.TEMPLATE_EXCEPTION_NULL).printStackTrace();
        }

        /* 要素が可変の場合 */
        if (template instanceof VariableTemplate) {
            if (((IVariableTemplate) template).getFixCheck() != true) {
                variableTemplates.add(template);
            }
        }

        if (template instanceof TemplateContainer) {
            List<Template> children = ((TemplateContainer) template).getChildren();
            for (Template p : children) {
                recursiveSetVariableElement(p);
            }
        }
    }


    /**
     * 可変の要素の位置を取得
     * 
     * @param template
     *            ひな型
     * @return 可変の要素の位置
     */
    private static int getVariableElementPos(Template template) {
        int cnt = 0;
        for (Template variableTemplate : variableTemplates) {
            cnt++;
            /* 要素が一致する場合 */
            if (variableTemplate.equals(template)) {
                return cnt;
            }
        }
        return 0;
    }


    @Override
    public void dispose() {
        color.dispose();
    }
}
