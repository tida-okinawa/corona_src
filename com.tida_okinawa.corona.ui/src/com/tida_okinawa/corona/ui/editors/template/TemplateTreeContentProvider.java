/**
 * @version $Id: TemplateTreeContentProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/12/05 16:39:48
 * @author s.takuro
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.template;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.tida_okinawa.corona.correction.template.Template;
import com.tida_okinawa.corona.correction.template.TemplateContainer;
import com.tida_okinawa.corona.correction.template.TemplateRecord;
import com.tida_okinawa.corona.io.model.dic.ITemplateItem;

/**
 * @author s.takuro
 *         #187 構文パターン自動生成
 *         Tree用のコンテントプロバイダ
 */
public class TemplateTreeContentProvider implements ITreeContentProvider {

    private static final Object[] EMPTY_ARRAY = new Object[0];


    @Override
    public Object[] getElements(Object input) {
        if (input instanceof TemplateRecords) {
            List<TemplateRecord> records = ((TemplateRecords) input).getTemplateRecords();
            Set<TemplateRecord> ret = new TreeSet<TemplateRecord>(new Comparator<TemplateRecord>() {
                @Override
                public int compare(TemplateRecord o1, TemplateRecord o2) {
                    return o1.getId() - o2.getId();
                }
            });
            /* ひな型IDがDEFAULT_IDの場合のみ追加（TEMPLATE_ID>0は参照用） */
            for (TemplateRecord record : records) {
                if (record.getTemplateId() == ITemplateItem.DEFAULT_ID) {
                    ret.add(record);
                }
            }
            return ret.toArray(new TemplateRecord[ret.size()]);
        } else {
            return (TemplateRecord[]) input;
        }
    }


    @Override
    public Object[] getChildren(Object parent) {
        if (parent instanceof TemplateContainer) {
            return ((TemplateContainer) parent).getChildren().toArray();
        }
        return EMPTY_ARRAY;
    }


    @Override
    public Object getParent(Object element) {
        if (element instanceof Template) {
            return ((Template) element).getParent();
        }
        return null;
    }


    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof TemplateRecord) {
            return ((TemplateRecord) element).hasChildren();
        }
        if (element instanceof TemplateContainer) {
            return true;
        }
        return false;
    }


    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        assert viewer instanceof TreeViewer;
    }


    @Override
    public void dispose() {
    }

}
