/**
 * @version $Id: PatternDetailsPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/31 16:03:04
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors.pattern;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author kousuke-morishima
 */
public abstract class PatternDetailsPage extends DetailsPage {
    public static final String TEXT_SCOPE_HOVER = Messages.PatternDetailsPage_textScopeHover;

    static final String DESC_APPLY = Messages.PatternDetailsPage_descApply;
    public static final String DESCRIPTION_AND = Messages.PatternDetailsPage_descriptionAnd + DESC_APPLY;
    public static final String DESCRIPTION_LINK = Messages.PatternDetailsPage_descriptionLink + DESC_APPLY;
    public static final String DESCRIPTION_MODIFICATION = Messages.PatternDetailsPage_descriptionModification + DESC_APPLY;
    public static final String DESCRIPTION_MODIFI_DST = Messages.PatternDetailsPage_descriptionModifDst;
    public static final String DESCRIPTION_MODIFI_SRC = Messages.PatternDetailsPage_descriptionModifiSrc;
    public static final String DESCRIPTION_NOT = Messages.PatternDetailsPage_descriptionNot;
    public static final String DESCRIPTION_OR = Messages.PatternDetailsPage_descriptionOr + DESC_APPLY;
    public static final String DESCRIPTION_ORDER = Messages.PatternDetailsPage_descriptionOrder + DESC_APPLY;
    public static final String DESCRIPTION_RECORD = Messages.PatternDetailsPage_descriptionRecord + DESC_APPLY;
    public static final String DESCRIPTION_SEQUENCE = Messages.PatternDetailsPage_descriptionSequence + DESC_APPLY;
    public static final String DESCRIPTION_TERM = Messages.PatternDetailsPage_descriptionTerm + DESC_APPLY;


    public PatternDetailsPage(FormEditor editor) {
        super(editor);

        checkedControl = new HashMap<Control, Integer>();
    }

    /* ****************************************
     * 入力変更系
     */
    private Map<Control, Integer> checkedControl;


    /**
     * 指定されたコントロールに「値が変更されたらツリーを更新する」リスナーをつける
     * 
     * @param widget
     * @param eventType
     */
    protected void setDirtyCheckListenerTo(Control widget, int eventType) {
        if (!checkedControl.containsKey(widget)) {
            checkedControl.put(widget, eventType);
            widget.addListener(eventType, refreshListener);
        }
    }


    /**
     * 指定されたコントロールから「値が変更されたらページのdirtyをtrueにする」リスナーを外す
     * 読み出した情報を設定するなど、ユーザ操作以外で値を変更する場合に呼び出す。
     * 値の変更後、 {@link #setDirtyCheckListenerTo(Control, int)}を呼び出すのを忘れないこと
     * 
     * @param widget
     * @param eventType
     */
    protected void removeDirtyCheckListenerFrom(Control widget, int eventType) {
        checkedControl.remove(widget);
        widget.removeListener(eventType, refreshListener);
    }

    private final Listener refreshListener = new Listener() {
        @Override
        public void handleEvent(Event event) {
            commit();
            ((PatternDicPage) ((PatternDicEditor) editor).getActivePageInstance()).update();
        }
    };


    /*
     * TODO checkDirtyを実装する
     * これを使うには各ページで、
     * 保持しているオブジェクトと、それがDBに登録されている内容とを比較できないといけない。
     * 具体的には、CompareDetailPageはCompareを持っているが、
     * 別のフィールドでDBに登録してあるCompareの内容を保持していなければならない.
     */
    // protected abstract boolean checkDirty();


    /**
     * #755 選択中の全要素取得
     * 
     * @return 選択されている全要素
     */
    public IStructuredSelection getStructuredSelection() {
        return ss;
    }

    /* ****************************************
     * 選択変更系
     */
    private IStructuredSelection ss;


    @Override
    public void selectionChanged(IFormPart part, ISelection selection) {
        assert selection instanceof IStructuredSelection;
        ss = (IStructuredSelection) selection;

        for (Entry<Control, Integer> e : checkedControl.entrySet()) {
            e.getKey().removeListener(e.getValue(), refreshListener);
        }

        selectionChanged(part, ss.getFirstElement());

        for (Entry<Control, Integer> e : checkedControl.entrySet()) {
            e.getKey().addListener(e.getValue(), refreshListener);
        }
    }


    /**
     * マスターで選択されているアイテムが変わった時に呼び出される<br />
     * このメソッド内で、{@link #setDirtyCheckListenerTo(Control, int)}
     * に登録したControlの内容を変更しても、変更イベントは起きない
     * 
     * @param part
     * @param selectedObject
     */
    protected abstract void selectionChanged(IFormPart part, Object selectedObject);


    /* ****************************************
     * 便利メソッド
     */
    /**
     * Sectionを作る
     * 
     * @param parent
     * @param title
     *            nullならタイトルなし
     * @param description
     *            nullなら説明なし
     * @return
     */
    protected Section createSection(Composite parent, String title, String description) {
        int style = 0;
        style |= (title != null) ? Section.TITLE_BAR : 0;
        style |= (description != null) ? Section.DESCRIPTION : 0;

        Section section = kit.createSection(parent, style);
        if (title != null) {
            section.setText(title);
        }
        if (description != null) {
            section.setDescription(description);
        }

        return section;
    }


    public static IInformationControl getInformationControl(Shell parent, boolean isResizeable, Point maxSize) {
        IInformationControl informationControl = new DefaultInformationControl(parent, isResizeable);
        informationControl.setSizeConstraints(maxSize.x, maxSize.y);
        return informationControl;
    }


    /**
     * @param info
     *            must not null
     * @param control
     *            このControlにツールチップを出す
     * @param content
     *            表示する文字列
     */
    public static void setHover(final IInformationControl info, final Control control, final String content) {
        control.addMouseTrackListener(new MouseTrackListener() {
            @Override
            public void mouseHover(MouseEvent e) {
                /* こっちに書くと、カーソルが当たってしばらくしてから出てくる */
                info.setInformation(content);
                Point p = info.computeSizeHint();
                info.setSize(p.x, p.y);
                info.setLocation(control.toDisplay(new Point(10, 25)));
                info.setVisible(true);
            }


            @Override
            public void mouseExit(MouseEvent e) {
                info.setVisible(false);
            }


            @Override
            public void mouseEnter(MouseEvent e) {
                /* こっちに書くと、カーソルが当たった瞬間に出てくる */
            }
        });
    }
}
