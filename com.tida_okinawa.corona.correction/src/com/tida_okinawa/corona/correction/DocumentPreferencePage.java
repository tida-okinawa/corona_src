/**
 * @version $Id: DocumentPreferencePage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2012/07/30 10:30:21
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.tida_okinawa.corona.correction.data.CoronaDocumentDefinition;
import com.tida_okinawa.corona.correction.data.CoronaDocumentInformation;
import com.tida_okinawa.corona.correction.data.DocumentSplitType;

/**
 * プリファレンスページ(ドキュメント解析関連）
 * 
 * @author shingo-takahashi
 * 
 */
public class DocumentPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    // FindBugs static 内部クラスにすれば？が残ったが、対応しない

    private TableViewer tableViewer;
    List<CoronaDocumentInformation> docInfos = new ArrayList<CoronaDocumentInformation>();
    CoronaDocumentInformation activeInfo = null;

    private Button btnInfoNew;
    private Button btnInfoEdit;
    private Button btnInfoDel;
    private Button btnInfoImport;
    private Button btnInfoExport;

    private Combo docDefineCombo;
    private Button btnNew;
    private Button btnEdit;
    private Button btnDel;


    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(CorrectionActivator.getDefault().getPreferenceStore());
        docInfos = CorrectionPreferenceInitializer.getDocumentInfomations();
    }


    /** コンストラクター */
    public DocumentPreferencePage() {
        super();
    }


    /**
     * アクティブなドキュメント情報の内容にテーブル内容に更新
     * 
     * @param index
     *            ドキュメント情報インデックス
     */
    protected void updateDocumentDefinition(int index) {
        if (index < 0) {
            activeInfo = null;
            tableViewer.setInput(null);
            updateButtons();
            return;
        } else if (docInfos.size() <= index) {
            return;
        }

        activeInfo = docInfos.get(index);
        tableViewer.setInput(activeInfo.getDefinitions());

        /* 定義内容のチェック状態を更新 */
        for (int i = 0; i < activeInfo.getDefinitions().size(); i++) {
            CoronaDocumentDefinition docInfo = activeInfo.getDefinitions().get(i);
            TableItem item = tableViewer.getTable().getItem(i);
            item.setChecked(docInfo.isEnabled());
        }
        updateButtons();
    }


    protected void updateButtons() {
        boolean isInfoActive = activeInfo != null;
        btnInfoEdit.setEnabled(isInfoActive);
        btnInfoDel.setEnabled(isInfoActive);
        btnInfoExport.setEnabled(isInfoActive);
        btnNew.setEnabled(isInfoActive);
        btnEdit.setEnabled(isInfoActive);
        btnDel.setEnabled(isInfoActive);
    }


    /* ****************************************
     * ユーザーインターフェイスの作成
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout(4, true));
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createDefineInfomationGroup(container);
        createDefineContentGroup(container);

        if (docDefineCombo.getItemCount() > 0) {
            docDefineCombo.select(0);
        } else {
            updateDocumentDefinition(-1);
        }

        restoreDefault();
        return container;
    }


    private void createDefineInfomationGroup(Composite parent) {

        final Label defLabel = new Label(parent, SWT.LEFT);
        defLabel.setText(Messages.DocumentPreferencePage_Label_DefinitionInformation);
        GridData gd = new GridData(SWT.NONE, SWT.NONE, false, false, 4, 1);
        defLabel.setLayoutData(gd);

        // コンボボックス
        docDefineCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        gd = new GridData(SWT.FILL, SWT.NONE, true, false, 3, 1);
        docDefineCombo.setLayoutData(gd);
        for (CoronaDocumentInformation i : docInfos) {
            docDefineCombo.add(i.getName() + " [" + i.getExtension() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        docDefineCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // アクティブな定義内容を反映させる。
                updateDocumentDefinition(((Combo) e.getSource()).getSelectionIndex());
            }
        });
        docDefineCombo.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                /* docDefineCombo.select が呼ばれたときにくる */
                updateDocumentDefinition(((Combo) e.getSource()).getSelectionIndex());
            }
        });

        GridData btnInfoGd = new GridData(SWT.FILL, SWT.NONE, false, false);
        final Combo finalDefineCombo = docDefineCombo;

        btnInfoEdit = new Button(parent, SWT.PUSH);
        btnInfoEdit.setText(Messages.DocumentPreferencePage_Label_Edit_Q);
        btnInfoEdit.setLayoutData(btnInfoGd);
        btnInfoEdit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // 編集ダイアログを表示
                CoronaDocumentInformation docInfo = docInfos.get(finalDefineCombo.getSelectionIndex());
                DocumentDefinitionInfomationDialog dialog = new DocumentDefinitionInfomationDialog(getShell(), docInfo);
                int ret = dialog.open();
                if (ret == IDialogConstants.OK_ID) {
                    /* 表示更新 */
                    finalDefineCombo.setItem(finalDefineCombo.getSelectionIndex(), docInfo.getName() + " [" + docInfo.getExtension() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                    updateDocumentDefinition(finalDefineCombo.getSelectionIndex());
                }
            }
        });

        btnInfoNew = new Button(parent, SWT.PUSH);
        btnInfoNew.setText(Messages.DocumentPreferencePage_Label_New_W);
        btnInfoNew.setLayoutData(btnInfoGd);
        btnInfoNew.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // 新規で編集ダイアログを表示
                CoronaDocumentInformation docInfo = new CoronaDocumentInformation();
                DocumentDefinitionInfomationDialog dialog = new DocumentDefinitionInfomationDialog(getShell(), docInfo);
                int ret = dialog.open();
                if (ret == IDialogConstants.OK_ID) {
                    docInfos.add(docInfo);
                    finalDefineCombo.add(docInfo.getName() + " [" + docInfo.getExtension() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                    finalDefineCombo.select(docInfos.size() - 1);
                }
            }
        });

        btnInfoDel = new Button(parent, SWT.PUSH);
        btnInfoDel.setText(Messages.DocumentPreferencePage_Label_Remove_D);
        btnInfoDel.setLayoutData(btnInfoGd);
        btnInfoDel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                /* データとコンボボックスからクリア */
                docInfos.remove(finalDefineCombo.getSelectionIndex());
                finalDefineCombo.remove(finalDefineCombo.getSelectionIndex());

                if (docInfos.isEmpty()) {
                    finalDefineCombo.deselectAll();
                } else {
                    finalDefineCombo.select(0);
                }
            }
        });

        btnInfoImport = new Button(parent, SWT.PUSH);
        btnInfoImport.setText(Messages.DocumentPreferencePage_Label_Import_I);
        btnInfoImport.setLayoutData(btnInfoGd);
        btnInfoImport.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // ファイルを開くダイアログ
                FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
                String[] exts = { "*.xml", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
                String[] filterNames = { Messages.DocumentPreferencePage_labelXmlType, Messages.DocumentPreferencePage_labelAllType };
                dialog.setFilterExtensions(exts);
                dialog.setFilterNames(filterNames);
                String path = dialog.open();
                if (path != null) {
                    InputStreamReader inReader = null;
                    StringWriter sWriter = null;
                    try {
                        inReader = new InputStreamReader(new FileInputStream(path));
                        sWriter = new StringWriter();

                        int size;
                        char[] cbuf = new char[2];
                        while ((size = inReader.read(cbuf)) != -1) {
                            sWriter.write(cbuf, 0, size);
                        }

                        CoronaDocumentInformation docInfo = (CoronaDocumentInformation) CorrectionPreferenceInitializer.toObject(sWriter.toString());
                        if (docInfo != null) {
                            docInfos.add(docInfo);
                            finalDefineCombo.add(docInfo.getName() + " [" + docInfo.getExtension() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                            finalDefineCombo.select(docInfos.size() - 1);
                        }
                    } catch (IOException e1) {
                        MessageDialog.openError(getShell(), Messages.ErrorTitle_FailedReadFile, Messages.bind(Messages.ErrorMessage_FailedReadFile, path));
                    } finally {
                        if (inReader != null) {
                            try {
                                inReader.close();
                            } catch (IOException e1) {
                            }
                        }
                        if (sWriter != null) {
                            try {
                                sWriter.close();
                            } catch (IOException e1) {
                            }
                        }
                    }
                }
            }
        });

        btnInfoExport = new Button(parent, SWT.PUSH);
        btnInfoExport.setText(Messages.DocumentPreferencePage_Label_Export_X);
        btnInfoExport.setLayoutData(btnInfoGd);
        btnInfoExport.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // 出力先ダイアログを開く
                FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
                String[] exts = { "*.xml", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
                String[] filterNames = { Messages.DocumentPreferencePage_labelXmlType, Messages.DocumentPreferencePage_labelAllType };
                dialog.setFilterExtensions(exts);
                dialog.setFilterNames(filterNames);
                String path = dialog.open();
                if (path != null) {
                    // XML形式にコンバート
                    String xml = CorrectionPreferenceInitializer.toXML(activeInfo);
                    // ファイルに出力
                    FileOutputStream os = null;
                    try {
                        os = new FileOutputStream(path);
                        os.write(xml.getBytes());
                    } catch (IOException e1) {
                        MessageDialog.openError(getShell(), Messages.ErrorTitle_FailedWriteFile, Messages.bind(Messages.ErrorMessage_FailedWriteFile, path));
                    } finally {
                        try {
                            if (os != null) {
                                os.close();
                            }
                        } catch (IOException e1) {
                        }
                    }
                }
            }
        });
    }


    private void createDefineContentGroup(Composite parent) {
        /* ラベルの上部にマージンを設けるためにコンポジットを作成 */
        parent = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(4, true);
        layout.marginTop = 16;
        layout.marginBottom = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        parent.setLayout(layout);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
        parent.setLayoutData(layoutData);

        Label contentLabel = new Label(parent, SWT.LEFT);
        contentLabel.setText(Messages.DocumentPreferencePage_Label_DefinitionContents);
        contentLabel.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false, 4, 1));

        tableViewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.CHECK | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        final TableViewer finalViewer = tableViewer;

        /* Tableの設定 */
        Table table = tableViewer.getTable();
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 4);
        table.setLayoutData(gd);
        TableColumn col1 = new TableColumn(table, SWT.LEFT);
        col1.setText(Messages.DocumentPreferencePage_ColumnName_Definition);
        col1.setWidth(240);
        TableColumn col2 = new TableColumn(table, SWT.LEFT);
        col2.setText(Messages.DocumentPreferencePage_ColumnName_Position);
        col2.setWidth(80);
        TableColumn col3 = new TableColumn(table, SWT.LEFT);
        col3.setText(Messages.DocumentPreferencePage_ColumnName_Target);
        col3.setWidth(80);
        TableColumn col4 = new TableColumn(table, SWT.LEFT);
        col4.setText(Messages.DocumentPreferencePage_ColumnName_Remove);
        col4.setWidth(80);
        table.setHeaderVisible(true);
        // ContentProvider
        tableViewer.setContentProvider(ArrayContentProvider.getInstance());
        // LabelProvider
        tableViewer.setLabelProvider(new TableLabelProvider());

        table.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.detail == SWT.CHECK) {
                    TableItem item = (TableItem) event.item;
                    if (item.getData() instanceof CoronaDocumentDefinition) {
                        ((CoronaDocumentDefinition) item.getData()).setEnabled(item.getChecked());
                    }
                }
            }
        });

        GridData btnGd = new GridData(SWT.FILL, SWT.NONE, false, false);

        btnNew = new Button(parent, SWT.PUSH);
        btnNew.setText(Messages.DocumentPreferencePage_Label_New_N);
        btnNew.setLayoutData(btnGd);
        final Combo finalDefineCombo = docDefineCombo;
        btnNew.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                /* 新規で設定ダイアログを表示 */
                CoronaDocumentDefinition docDefinition = new CoronaDocumentDefinition();
                DocumentDefinitionContentDialog dialog = new DocumentDefinitionContentDialog(getShell(), docDefinition);
                int ret = dialog.open();
                if (ret == IDialogConstants.OK_ID) {
                    activeInfo.getDefinitions().add(docDefinition);
                    updateDocumentDefinition(finalDefineCombo.getSelectionIndex());
                    finalViewer.setSelection(new StructuredSelection(docDefinition));
                }
            }
        });

        btnEdit = new Button(parent, SWT.PUSH);
        btnEdit.setText(Messages.DocumentPreferencePage_Label_Edit_E);
        btnEdit.setLayoutData(btnGd);
        btnEdit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                /* 選択項目を設定ダイアログを表示 */
                IStructuredSelection ss = (IStructuredSelection) finalViewer.getSelection();
                CoronaDocumentDefinition docDefinition = (CoronaDocumentDefinition) ss.getFirstElement();
                if (docDefinition != null) {
                    DocumentDefinitionContentDialog dialog = new DocumentDefinitionContentDialog(getShell(), docDefinition);
                    int ret = dialog.open();
                    if (ret == IDialogConstants.OK_ID) {
                        /* 表示更新 */
                        finalViewer.update(docDefinition, null);
                    }
                    finalViewer.getTable().setFocus();
                }
            }
        });

        btnDel = new Button(parent, SWT.PUSH);
        btnDel.setText(Messages.DocumentPreferencePage_Label_Remove_R);
        btnDel.setLayoutData(btnGd);
        btnDel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection ss = (IStructuredSelection) finalViewer.getSelection();
                if (ss.getFirstElement() != null) {
                    int selectionIndex = finalViewer.getTable().getSelectionIndex();
                    List<CoronaDocumentDefinition> definitions = activeInfo.getDefinitions();
                    definitions.remove(ss.getFirstElement());
                    updateDocumentDefinition(finalDefineCombo.getSelectionIndex());
                    if (!definitions.isEmpty()) {
                        /* 削除したひとつ下を選択する */
                        IStructuredSelection newSS;
                        if (definitions.size() <= selectionIndex) {
                            newSS = new StructuredSelection(definitions.get(definitions.size() - 1));
                        } else {
                            newSS = new StructuredSelection(definitions.get(selectionIndex));
                        }
                        finalViewer.setSelection(newSS);
                    }
                }
            }
        });
    }


    @Override
    protected void performDefaults() {
        // 問い合わせ必要？
        docInfos = CorrectionPreferenceInitializer.getDefaultDocumentInfomations();
        docDefineCombo.removeAll();
        for (CoronaDocumentInformation docInfo : docInfos) {
            docDefineCombo.add(docInfo.getName() + " [" + docInfo.getExtension() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        docDefineCombo.select(0);
    }


    @Override
    public boolean performOk() {
        CorrectionPreferenceInitializer.setDocumentInfomations(docInfos);
        saveDefault();
        return true;
    }


    private void restoreDefault() {
        // 前回選択していた定義情報を表示する
        IPreferenceStore store = CorrectionActivator.getDefault().getPreferenceStore();
        if (store != null) {
            int index = store.getInt(CorrectionPreferenceInitializer.PREF_SELECTED_DOCUMENT_INFORMATION);
            docDefineCombo.select(index);
        }
    }


    private void saveDefault() {
        int index = docDefineCombo.getSelectionIndex();
        IPreferenceStore store = CorrectionActivator.getDefault().getPreferenceStore();
        if (store != null) {
            store.setValue(CorrectionPreferenceInitializer.PREF_SELECTED_DOCUMENT_INFORMATION, index);
        }
    }

    private static class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
        public TableLabelProvider() {
        }


        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }


        @Override
        public String getColumnText(Object element, int columnIndex) {
            CoronaDocumentDefinition dd = (CoronaDocumentDefinition) element;
            switch (columnIndex) {
            case 0:
                return dd.getDefinition();
            case 1:
                return dd.getPositionStr();
            case 2:
                return dd.getTypeStr();
            case 3:
                return dd.getTrimStr();
            default:
                return ""; //$NON-NLS-1$
            }
        }
    }

    /**
     * 
     * @author shingo-takahashi
     * 
     */
    static class DocumentDefinitionInfomationDialog extends Dialog {
        CoronaDocumentInformation docInfo;


        public DocumentDefinitionInfomationDialog(Shell parent, CoronaDocumentInformation docInfo) {
            super(parent);
            this.docInfo = docInfo;
        }


        @Override
        protected Point getInitialSize() {
            return new Point(400, 230);
        }


        @Override
        protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            newShell.setText(Messages.DocumentPreferencePage_DialogTitle_DefinitionInformation);
        }


        @Override
        protected Control createContents(Composite parent) {
            Control control = super.createContents(parent);
            updateButtons(validatePage());
            setMessage(null);
            return control;
        }


        /**
         * 入力するダイアログを描画します。
         * 
         * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
         */
        @Override
        protected Control createDialogArea(Composite parent) {
            Composite composite = (Composite) super.createDialogArea(parent);
            createMessageArea(composite);
            createInputArea(composite);
            return composite;
        }

        private Label errorMessage;


        private void createMessageArea(Composite parent) {
            errorMessage = new Label(parent, SWT.BORDER);
            errorMessage.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        }


        private void setMessage(String message) {
            if (message == null) {
                message = ""; //$NON-NLS-1$
            }
            this.errorMessage.setText(message);
        }

        private Text definitionNameText;
        private Text extensionText;


        private void createInputArea(Composite parent) {
            Composite container = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout(3, false);
            layout.marginHeight = 0;
            container.setLayout(layout);
            container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

            GridData gd = new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1);
            definitionNameText = createInputItem(container, Messages.DocumentPreferencePage_DialogLabel_DefinitionName, docInfo.getName(), gd);
            definitionNameText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    updateButtons(validatePage());
                }
            });

            gd = new GridData(SWT.FILL, SWT.NONE, true, false);
            extensionText = createInputItem(container, Messages.DocumentPreferencePage_DialogLabel_FileExtension, docInfo.getExtension(), gd);
            extensionText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    updateButtons(validatePage());
                }
            });
            Label l = new Label(container, SWT.NONE);
            l.setText(Messages.DocumentPreferencePage_DialogLabel_ExsampleOfFileExtension);
        }


        protected Text createInputItem(Composite parent, String label, String defValue, GridData txtGd) {
            new Label(parent, SWT.NONE).setText(label);
            Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
            text.setLayoutData(txtGd);
            text.setText(defValue);
            return text;
        }


        public CoronaDocumentInformation getResult() {
            return docInfo;
        }


        @Override
        protected void okPressed() {
            docInfo.setName(definitionNameText.getText());
            docInfo.setExtension(extensionText.getText());
            super.okPressed();
        }


        boolean validatePage() {
            if (definitionNameText.getText().length() == 0) {
                setMessage(Messages.DocumentPreferencePage_ErrorMessage_NoDefinitionName);
                return false;
            }
            if (extensionText.getText().length() == 0) {
                setMessage(Messages.DocumentPreferencePage_ErrorMessage_NoFileExtension);
                return false;
            }
            if (!extensionText.getText().matches(Messages.DocumentPreferencePage_Regex_FileExtension)) {
                setMessage(Messages.DocumentPreferencePage_ErrorMessage_IllegalFileExtension);
                return false;
            }
            setMessage(null);
            return true;
        }


        void updateButtons(boolean enabled) {
            getButton(OK).setEnabled(enabled);
        }


        @Override
        protected boolean isResizable() {
            return true;
        }
    }

    static class DocumentDefinitionContentDialog extends Dialog {
        CoronaDocumentDefinition docDefinition;
        int focusPosition;


        public DocumentDefinitionContentDialog(Shell parent, CoronaDocumentDefinition docDefinition) {
            super(parent);
            this.docDefinition = docDefinition;
        }


        @Override
        protected Point getInitialSize() {
            return new Point(400, 280);
        }


        @Override
        protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            newShell.setText(Messages.DocumentPreferencePage_DialogTitle_DefinitionContent);
        }


        @Override
        protected Control createContents(Composite parent) {
            Control control = super.createContents(parent);
            updateButtons(validatePage());
            updateEnabled();
            setMessage(null);
            return control;
        }


        @Override
        protected Control createDialogArea(Composite parent) {
            Composite composite = (Composite) super.createDialogArea(parent);
            createMessageArea(composite);
            createInputArea(composite);
            return composite;
        }

        private Label errorMessage;


        private void createMessageArea(Composite parent) {
            errorMessage = new Label(parent, SWT.BORDER);
            errorMessage.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        }


        private void setMessage(String message) {
            if (message == null) {
                message = ""; //$NON-NLS-1$
            }
            this.errorMessage.setText(message);
        }

        private Text defineText;
        private Combo defCombo;
        private Combo positionCombo;
        private Combo typeCombo;
        private Combo removeCombo;


        private void createInputArea(Composite parent) {
            Composite container = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout(4, false);
            layout.marginHeight = 0;
            container.setLayout(layout);
            container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

            GridData txtGd = new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1);

            final Text finalText = defineText = createInputItem(container, Messages.DocumentPreferencePage_DialogLabel_Definition,
                    docDefinition.getDefinition(), txtGd);
            defineText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    updateButtons(validatePage());
                    updateEnabled();
                }
            });
            defineText.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    focusPosition = ((Text) e.getSource()).getCaretPosition();
                }
            });

            GridData gdSp = new GridData(SWT.FILL, SWT.NONE, false, false);
            gdSp.widthHint = 50;
            defCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
            defCombo.setLayoutData(gdSp);
            defCombo.add(Messages.DocumentPreferencePage_ComboItem_Insert);
            defCombo.add(DocumentSplitType.NUMBER.getValue());
            defCombo.add(DocumentSplitType.ABC.getValue());
            defCombo.add(DocumentSplitType.KANA.getValue());
            //txtCmb.add(DocumentSplitType.CAPTION.getName() ); // Memo キャプションは現状未対応
            defCombo.select(0);
            defCombo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Combo combo = (Combo) e.widget;
                    int pos = combo.getSelectionIndex();
                    if (pos == 0) {
                        return;
                    }

                    /* カーソルのあった位置に挿入 */
                    StringBuilder buf = new StringBuilder(finalText.getText());
                    buf.insert(focusPosition, combo.getText());
                    finalText.setText(buf.toString());
                    /* コンボを無効にすると、前のアイテムにフォーカスが勝手に移るので、フォーカス位置を調整 */
                    int newFocusPosition = focusPosition + combo.getText().length();
                    finalText.setSelection(newFocusPosition, newFocusPosition);

                    combo.select(0);
                    combo.setEnabled(false);
                }
            });


            GridData gd = new GridData(SWT.FILL, SWT.NONE, true, false, 3, 1);
            gd.widthHint = 200;

            new Label(container, SWT.NONE).setText(Messages.DocumentPreferencePage_DialogLabel_Position);
            positionCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
            positionCombo.setLayoutData(gd);
            positionCombo.add(CoronaDocumentDefinition.STR_WHOLE);
            positionCombo.add(CoronaDocumentDefinition.STR_PHRASE);
            positionCombo.select(docDefinition.getPosition());

            new Label(container, SWT.NONE).setText(Messages.DocumentPreferencePage_DialogLabel_Target);
            typeCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
            typeCombo.setLayoutData(gd);
            typeCombo.add(CoronaDocumentDefinition.STR_CHAR);
            typeCombo.add(CoronaDocumentDefinition.STR_STRING);
            typeCombo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateEnabled();
                }
            });
            typeCombo.select(docDefinition.getType());

            new Label(container, SWT.NONE).setText(Messages.DocumentPreferencePage_DialogLabel_Remove);
            removeCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
            removeCombo.setLayoutData(gd);
            removeCombo.add(CoronaDocumentDefinition.STR_FALSE);
            removeCombo.add(CoronaDocumentDefinition.STR_TRUE);
            removeCombo.select(docDefinition.getTrim());
        }


        @Override
        protected void okPressed() {
            docDefinition.setDefinition(defineText.getText());
            docDefinition.setPosition(positionCombo.getSelectionIndex());
            docDefinition.setType(typeCombo.getSelectionIndex());
            docDefinition.setTrim(removeCombo.getSelectionIndex());
            super.okPressed();
        }


        private static Text createInputItem(Composite parent, String label, String defValue, GridData txtGd) {
            new Label(parent, SWT.NONE).setText(label);
            Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
            text.setLayoutData(txtGd);
            text.setText(defValue);
            return text;
        }


        public CoronaDocumentDefinition getResult() {
            return docDefinition;
        }


        boolean validatePage() {
            String define = defineText.getText();
            if (define.length() == 0) {
                setMessage(Messages.DocumentPreferencePage_ErrorMessage_NoDefinition);
                return false;
            }
            /* 特殊文字を複数含んでいたらNG */
            if (DocumentSplitType.getStartPoint(define) != -1) {
                if (define.length() > 6) {
                    int splitIndex = DocumentSplitType.getStartPoint(define);
                    StringBuilder sb = new StringBuilder(define);
                    sb.delete(splitIndex, splitIndex + 6);
                    if (DocumentSplitType.getStartPoint(sb.toString()) != -1) {
                        setMessage(Messages.DocumentPreferencePage_ErrorMessage_MultipleSpecialDefinition);
                        return false;
                    }
                }
            }
            setMessage(null);
            return true;
        }


        void updateEnabled() {
            /* 挿入コンボの活性非活性を決定する */
            boolean enabled = true;
            /* 特殊文字を含んでいるかどうか */
            if (CoronaDocumentDefinition.STR_CHAR.equals(typeCombo.getText())) {
                /* 種別が１文字の場合、特殊文字１文字にヒットするイメージを与えるので、挿入は無効 */
                enabled = false;
            } else {
                String define = defineText.getText();
                if (define.length() > 0) {
                    /* 特殊文字がすでに入っている場合、挿入は無効 */
                    if (DocumentSplitType.getStartPoint(define) != -1) {
                        enabled = false;
                    }
                }
            }
            defCombo.setEnabled(enabled);
        }


        void updateButtons(boolean enabled) {
            getButton(OK).setEnabled(enabled);
        }


        @Override
        protected boolean isResizable() {
            return true;
        }
    }

}
