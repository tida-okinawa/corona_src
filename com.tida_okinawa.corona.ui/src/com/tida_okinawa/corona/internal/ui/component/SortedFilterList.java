/**
 * @version $Id: SortedFilterList.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/11/22 11:49:53
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.tida_okinawa.corona.internal.ui.util.DelayTimer;

/**
 * @author kousuke-morishima
 */
public class SortedFilterList {
    // TODO Jobを使って、表示を動的に更新するようにしたい。「参考：FilteredList」

    private boolean checkStyle;
    /**
     * フィルタ用のテキストボックス
     */
    StyledText filterText;
    /**
     * フィルタ用のテキストボックスに出すヒント文字列
     */
    String hint;
    /**
     * リストに表示する文字列を提供する
     */
    LabelProvider labelProvider;

    /**
     * フィルタ結果を表示するリスト
     */
    TableViewer itemList;


    /**
     * デフォルトラベルプロバイダー（{@link LabelProvider}）を使用する
     * 
     * @param parent
     * @param style
     *            TableViewerに指定できるスタイル
     */
    public SortedFilterList(Composite parent, int style) {
        this(parent, style, new LabelProvider());
    }


    /**
     * デフォルトラベルプロバイダーとデフォルトスタイルを使用する
     * 
     * @param parent
     */
    public SortedFilterList(Composite parent) {
        this(parent, SWT.BORDER | SWT.SINGLE);
    }


    /**
     * @param parent
     * @param style
     *            TableViewerに指定できるスタイル
     * @param labelProvider
     *            must not null
     */
    public SortedFilterList(Composite parent, int style, LabelProvider labelProvider) {
        checkStyle = (style & SWT.CHECK) != 0;
        setHintText("値を絞り込みます");
        this.labelProvider = labelProvider;

        createContent(parent, style, labelProvider);
    }


    protected void createContent(Composite parent, int style, LabelProvider labelProvider) {
        parent = CompositeUtil.defaultComposite(parent, 1);
        createFilterText(parent);
        createContentList(parent, style, new ArrayContentProvider(), labelProvider);
        itemList.getTable().addSelectionListener(selectedItemCount);
    }


    /* ****************************************
     * Filter
     */
    protected void createFilterText(Composite parent) {
        filterText = new StyledText(parent, SWT.BORDER | SWT.SINGLE);

        filterText.setLayoutData(CompositeUtil.gridData(true, false, 1, 1));
        filterText.setFont(parent.getFont());
        filterText.setBackground(new Color(null, 255, 255, 255));

        filterText.addListener(SWT.Modify, new Listener() {
            final Display display = Display.getCurrent();
            DelayTimer timer = new DelayTimer("FilterTimer", new Runnable() {
                @Override
                public void run() {
                    display.asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            if (!filterText.isDisposed()) {
                                setFilter(filterText.getText());
                            }
                        }
                    });
                }
            });


            @Override
            public void handleEvent(Event e) {
                timer.run(200);
            }
        });

        filterText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.ARROW_DOWN) {
                    itemList.getTable().setFocus();
                }
            }
        });

        initHint();
    }


    /**
     * 表示アイテムを指定のフィルタで絞る
     * 
     * @param filter
     */
    public void setFilter(ViewerFilter filter) {
        itemList.setFilters(new ViewerFilter[] { filter });
        restoreCheckState();
    }


    /**
     * 表示アイテムを指定された文字で絞る
     * 
     * @param filterText
     */
    public void setFilter(String filterText) {
        if (showHint) {
            return;
        }

        setFilter(createFilter(filterText));
    }


    protected ViewerFilter createFilter(final String filterText) {
        return new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                String text = labelProvider.getText(element).trim().toUpperCase();
                if (text.length() == 0) {
                    /* 空文字は空文字にのみヒット */
                    return "".equals(filterText);
                }

                if (filterText.contains("*")) {
                    String[] words = filterText.toUpperCase().split("\\*");

                    if (!filterText.startsWith("*")) {
                        /* アスターで始まっていなければ、先頭一致 */
                        if (!text.startsWith(words[0])) {
                            return false;
                        }
                    }

                    int from = 0;
                    for (String word : words) {
                        if (word.equals("")) {
                            continue;
                        }
                        int index = text.indexOf(word, from);
                        if (index == -1) {
                            return false;
                        }
                        from = index;
                    }
                } else {
                    /* アスターを含まなければ先頭一致 */
                    return text.startsWith(filterText.toUpperCase());
                }
                return true;
            }
        };
    }

    /* ********************
     * ヒント
     */
    private static final Color COLOR_HINT = new Color(null, 160, 180, 200);
    private static final Color COLOR_USER = new Color(null, 0, 0, 0);
    boolean showHint = true;


    /**
     * @return フィルタ文字列の入力欄に表示しているヒント文字列
     */
    public String getHintText() {
        return hint;
    }


    /**
     * @param hint
     *            フィルタ文字列の入力欄に表示するヒント文字列<br />
     *            must not null
     */
    public void setHintText(String hint) {
        Assert.isNotNull(hint);
        this.hint = hint;
        if (filterText != null) {
            updateHintText(hint);
        }
    }


    private void initHint() {
        if (getHintText().length() > 0) {
            filterText.setText(getHintText());
            filterText.setForeground(COLOR_HINT);
        }
        filterText.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                showHint = (filterText.getText().length() == 0);
                if (showHint) {
                    setText(getHintText(), COLOR_HINT);
                }
            }


            @Override
            public void focusGained(FocusEvent e) {
                if (showHint) {
                    setText("", COLOR_USER);
                    showHint = false;
                }
            }


            private void setText(String text, Color foreground) {
                filterText.setText(text);
                filterText.setForeground(foreground);
            }
        });
    }


    private void updateHintText(String newHint) {
        if (showHint) {
            filterText.setText(newHint);
        }
    }


    /* ********************
     * list
     */
    protected void createContentList(Composite parent, int style, IStructuredContentProvider contentProvider, LabelProvider labelProvider) {
        itemList = new TableViewer(parent, style);
        itemList.setContentProvider(contentProvider);
        itemList.setLabelProvider(labelProvider);
        itemList.setUseHashlookup(true);
    }


    /* ****************************************
     * Interface
     */
    /**
     * 並びを指定する
     * 
     * @param sorter
     */
    public void setSorter(ViewerSorter sorter) {
        itemList.setSorter(sorter);
    }


    private void restoreCheckState() {
        for (Entry<Object, Object> e : checkedItems.entrySet()) {
            TableItem item = (TableItem) itemList.testFindItem(e.getKey());
            if (item != null) {
                checkedItems.put(e.getKey(), PRESENT);
                item.setChecked(true);
            }
        }
    }


    /* ********************
     * Other Interface
     */
    /**
     * 指定しなければ {@link ArrayContentProvider}
     * 
     * @param provider
     */
    public void setContentProvider(IStructuredContentProvider provider) {
        itemList.setContentProvider(provider);
    }


    public void setLabelProvider(LabelProvider labelProvider) {
        this.labelProvider = labelProvider;
        itemList.setLabelProvider(labelProvider);
    }


    public void setInput(Object input) {
        itemList.setInput(input);
    }


    /* ****************************************
     * チェックの実装
     */
    static final Object PRESENT = new Object();
    private SelectionListener selectedItemCount = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            if (e.detail == SWT.CHECK) {
                TableItem item = (TableItem) e.item;
                if (item.getChecked()) {
                    checkedItems.put(item.getData(), PRESENT);
                } else {
                    checkedItems.remove(item.getData());
                }
            }
        }
    };
    Map<Object, Object> checkedItems = new HashMap<Object, Object>();


    /**
     * styleに{@link SWT#CHECK}を指定していないときは常に長さ0の配列が返る
     * 
     * @return チェックされているアイテムすべて。
     */
    public Object[] getChecked() {
        if (checkStyle) {
            return checkedItems.keySet().toArray();
        }
        return new Object[0];
    }


    /**
     * styleに{@link SWT#CHECK}を指定していないときは何もしない
     * 
     * @param checked
     *            すべてのアイテムにチェックを入れたり外したり
     */
    public void setCheckedAll(boolean checked) {
        if (checkStyle) {
            doSetCheckedAll(checked);
        }
    }


    protected void doSetCheckedAll(boolean checked) {
        Table t = itemList.getTable();
        doSetChecked(t.getItems(), checked);
    }


    /**
     * elementにチェックを入れる。
     * styleに{@link SWT#CHECK}を指定していないときは何もしない
     * 
     * @param element
     * @param checked
     */
    public void setChecked(Object element, boolean checked) {
        if (checkStyle) {
            doSetChecked(new Object[] { element }, checked);
        }
    }


    /**
     * elementsすべてにチェックを入れる。
     * styleに{@link SWT#CHECK}を指定していないときは何もしない
     * 
     * @param elements
     * @param checked
     */
    public void setChecked(Object[] elements, boolean checked) {
        if (checkStyle) {
            doSetChecked(elements, checked);
        }
    }


    protected void doSetChecked(Object[] elements, boolean checked) {
        for (Object element : elements) {
            TableItem item = null;
            if (element instanceof TableItem) {
                item = (TableItem) element;
                element = item.getData();
            } else {
                item = (TableItem) itemList.testFindItem(element);
                if (item == null) {
                    continue;
                }
            }
            item.setChecked(checked);
            if (checked) {
                checkedItems.put(element, PRESENT);
            } else {
                checkedItems.remove(element);
            }
        }
    }


    /**
     * @return 選択しているアイテムすべて
     */
    public Object[] getSelected() {
        TableItem[] items = itemList.getTable().getSelection();
        Object[] ret = new Object[items.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = items[i].getData();
        }
        return ret;
    }


    public void setFocus() {
        filterText.setFocus();
    }


    /* ****************************************
     * Listeners
     */
    /**
     * {@link Table}にSelectionListenerを追加する
     * 
     * @param listener
     */
    public void addSelectionListener(SelectionListener listener) {
        itemList.getTable().addSelectionListener(listener);
    }


    /**
     * {@link Table}からSelectionListenerを削除する
     * 
     * @param listener
     */
    public void removeSelectionListener(SelectionListener listener) {
        itemList.getTable().removeSelectionListener(listener);
    }


    /* ****************************************
     * Layout
     */
    /**
     * {@link Table}にLayoutを指定する
     * 
     * @param layout
     */
    public void setLayout(Layout layout) {
        itemList.getTable().setLayout(layout);
    }


    /**
     * {@link Table}にLayoutDataを指定する
     * 
     * @param layoutData
     */
    public void setLayoutData(Object layoutData) {
        itemList.getTable().setLayoutData(layoutData);
    }


    /**
     * ラベルリスト関連情報クリア処理
     * 1.フィルタ用テキストボックスをクリア
     * 2.チェックON情報を持つマップをクリア
     * 3.リストの表示を初期化
     * 4.フィルタ用テキストボックスにヒントを設定
     */
    public void sortedFilterListClear() {
        filterText.setText("");
        checkedItems.clear();
        setFilter(filterText.getText());
        showHint = (filterText.getText().length() == 0);
        if (showHint) {
            filterText.setText(getHintText());
            filterText.setForeground(COLOR_HINT);
        }

    }

}
