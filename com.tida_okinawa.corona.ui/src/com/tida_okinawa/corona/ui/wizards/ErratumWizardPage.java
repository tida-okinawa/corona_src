/**
 * @version $Id: ErratumWizardPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/01 11:46:05
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.internal.ui.component.SortedFilterList;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;

/**
 * @author kousuke-morishima
 */
public class ErratumWizardPage extends WizardPageBase {

    protected ErratumWizardPage(String pageName) {
        super(pageName);
        setMessage("分析対象データの誤記補正を行います。\n分析したいフィールドを選択してください。");
    }


    @Override
    public void createControl(Composite parent) {
        Composite composite = CompositeUtil.defaultComposite(parent, 1);
        createFieldList(composite);

        setPageComplete(validatePage());
        setErrorMessage(null);
    }

    SortedFilterList list;


    private void createFieldList(Composite parent) {
        Composite composite = CompositeUtil.defaultComposite(parent, 1);

        list = new SortedFilterList(composite, SWT.CHECK | SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, new ColorLabelProvider(erratumedFields));
        list.setSorter(new ViewerSorter() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                IFieldHeader f1 = (IFieldHeader) e1;
                IFieldHeader f2 = (IFieldHeader) e2;
                return f1.getDispName().compareTo(f2.getDispName());
            }
        });

        list.addSelectionListener(checkListener);
        if (claimData != null) {
            setClaimData(claimData);
        }

        list.setLayout(new GridLayout());
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.heightHint = 300;
        list.setLayoutData(layoutData);

        Composite buttonGroup = CompositeUtil.defaultComposite(composite, 5);
        ((GridLayout) buttonGroup.getLayout()).makeColumnsEqualWidth = true;
        ((GridData) buttonGroup.getLayoutData()).grabExcessVerticalSpace = false;
        Button btn = CompositeUtil.createBtn(buttonGroup, SWT.PUSH, "全選択(&A)", selectAllButtonListener);
        btn.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        btn = CompositeUtil.createBtn(buttonGroup, SWT.PUSH, "全解除(&D)", deselectAllButtonListener);
        btn.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

        setControl(composite);
    }

    private List<IFieldHeader> selectedFields;


    /**
     * @return チェックの入っているフィールドすべて
     */
    public List<IFieldHeader> getSelectedFields(boolean force) {
        if ((selectedFields == null) || force) {
            selectedFields = new ArrayList<IFieldHeader>();

            if (list != null) {
                Object[] checked = list.getChecked();
                for (Object o : checked) {
                    if (o instanceof IFieldHeader) {
                        selectedFields.add((IFieldHeader) o);
                    }
                }
            }
        }

        return selectedFields;
    }


    /**
     * @return 今回新しく選択したフィールド
     */
    public List<IFieldHeader> getNewSelectedFields() {
        return newErratumFields;
    }

    private List<IFieldHeader> erratumedFields = new ArrayList<IFieldHeader>();


    List<IFieldHeader> getErratumedFields() {
        return erratumedFields;
    }

    private IClaimData claimData;


    /**
     * 誤記補正対象の問合せデータをセットする
     * 
     * @param claim
     */
    public void setClaimData(IClaimData claim) {
        if (claim != null) {
            if (list != null) {
                /* 指定された問合せデータに設定されているマイニングフィールドを初期選択する */
                erratumedFields.clear();
                Collection<Integer> ids = new ArrayList<Integer>(claim.getCorrectionMistakesFields());
                List<IFieldHeader> fields = claim.getFieldInformations();
                for (IFieldHeader header : fields) {
                    for (Iterator<Integer> itr = ids.iterator(); itr.hasNext();) {
                        Integer id = itr.next();
                        if (header.getId() == id) {
                            erratumedFields.add(header);
                            itr.remove();
                        }
                    }
                }
                list.setInput(fields.subList(1, fields.size()));
                list.setChecked(erratumedFields.toArray(), true);
            } else {
                this.claimData = claim;
            }
        }
    }


    public void setFieldHeaders(List<IFieldHeader> fieldHeaders) {
        if (fieldHeaders != null && fieldHeaders.size() > 0) {
            if (fieldHeaders.get(0).getName().equalsIgnoreCase("id")) {
                list.setInput(fieldHeaders.subList(1, fieldHeaders.size()));
            } else {
                list.setInput(fieldHeaders);
            }
        }
    }


    /* ****************************************
     * フィールドチェック
     */
    boolean validatePage() {
        if (getSelectedFields(true).size() == 0) {
            setErrorMessage("フィールドはひとつ以上選択してください");
            return false;
        }

        setErrorMessage(null);
        return true;
    }

    /* ****************************************
     * LabelProvider
     */
    private static class ColorLabelProvider extends LabelProvider implements IColorProvider {
        private final Color erratumedColor;
        private Collection<IFieldHeader> erratumed;


        public ColorLabelProvider(Collection<IFieldHeader> erratumed) {
            erratumedColor = new Color(null, 160, 160, 160);
            this.erratumed = erratumed;
        }


        @Override
        public String getText(Object element) {
            if (element instanceof IFieldHeader) {
                if (erratumed.contains(element)) {
                    return ((IFieldHeader) element).getDispName() + "  (誤記補正済み)";
                }
                return ((IFieldHeader) element).getDispName();
            }
            return super.getText(element);
        }


        @Override
        public Color getForeground(Object element) {
            if (erratumed.contains(element)) {
                return erratumedColor;
            }
            return null;
        }


        @Override
        public Color getBackground(Object element) {
            return null;
        }
    }

    /* ****************************************
     * リスナー
     */
    List<IFieldHeader> newErratumFields = new ArrayList<IFieldHeader>();
    private SelectionListener checkListener = new SuperSelectionListener() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            if ((e.detail & SWT.CHECK) != 0) {
                List<IFieldHeader> erratumed = getErratumedFields();
                TableItem item = (TableItem) e.item;
                if (!erratumed.contains(item.getData())) {
                    if (item.getChecked()) {
                        newErratumFields.add((IFieldHeader) item.getData());
                    }
                }

                setPageComplete(validatePage());
            }
        };
    };

    class SuperSelectionListener implements SelectionListener {
        @Override
        public void widgetSelected(SelectionEvent e) {
            setPageComplete(validatePage());
        }


        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }
    };

    private final SelectionListener selectAllButtonListener = new SuperSelectionListener() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            list.setCheckedAll(true);
            super.widgetSelected(e);
        }
    };

    private final SelectionListener deselectAllButtonListener = new SuperSelectionListener() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            list.setCheckedAll(false);
            super.widgetSelected(e);
        }
    };


    @Override
    public void setFocus() {
        list.setFocus();
    }
}
