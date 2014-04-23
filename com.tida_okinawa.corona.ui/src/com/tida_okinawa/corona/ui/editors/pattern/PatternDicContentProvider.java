/**
 * @version $Id: PatternDicContentProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 17:41:19
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.tida_okinawa.corona.correction.parsing.model.IPatternListener;
import com.tida_okinawa.corona.correction.parsing.model.Pattern;
import com.tida_okinawa.corona.correction.parsing.model.PatternContainer;
import com.tida_okinawa.corona.correction.parsing.model.PatternRecord;

/**
 * @author kousuke-morishima
 */
public class PatternDicContentProvider implements ITreeContentProvider, IPatternListener {
    private static final Object[] EMPTY_ARRAY = new Object[0];


    @Override
    public Object[] getElements(Object input) {
        if (input instanceof PatternRecords) {
            return ((PatternRecords) input).getPatternRecords().toArray();
        } else if (input instanceof PatternRecord) {
            return ((PatternRecord) input).getChildren().toArray();
        }
        return EMPTY_ARRAY;
    }


    @Override
    public Object[] getChildren(Object parent) {
        if (parent instanceof PatternContainer) {
            return ((PatternContainer) parent).getChildren().toArray();
        }
        return EMPTY_ARRAY;
    }


    @Override
    public Object getParent(Object element) {
        if (element instanceof Pattern) {
            return ((Pattern) element).getParent();
        }
        return null;
    }


    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof PatternRecord) {
            return ((PatternRecord) element).hasChildren();
        }
        if (element instanceof PatternContainer) {
            return true;
        }
        return false;
    }

    private TreeViewer viewer;


    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

        assert viewer instanceof TreeViewer;
        this.viewer = (TreeViewer) viewer;
        if ((oldInput != null) && (oldInput instanceof PatternRecords)) {
            ((PatternRecords) oldInput).removePatternListener(this);
        }

        if ((newInput != null) && (newInput instanceof PatternRecords)) {
            ((PatternRecords) newInput).addPatternListener(this);
        }
    }


    @Override
    public void dispose() {
    }


    @Override
    public void patternAdded(PatternEvent event) {
        if (event.child == null) {
            return;
        }
        if (event.parent == null) {
            viewer.refresh(false);
        } else if (event.position != PatternEvent.NO_POS) {
            viewer.insert(event.parent, event.child, event.position);
        } else {
            viewer.add(event.parent, event.child);
        }
        viewer.setSelection(new StructuredSelection(event.child), true);
    }


    @Override
    public void patternRemoved(PatternEvent event) {
        if (event.child == null) {
            return;
        }
        if (event.parent == null) {
            viewer.remove(event.child);
            return;
        }
        viewer.remove(event.parent, new Object[] { event.child });
    }


    @Override
    public void patternChanged(PatternEvent event) {
        if (event.child == null) {
            return;
        }
        viewer.update(event.child, null);
    }
}
