/**
 * @version $Id: ConnectionPreferencePage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/12/20 10:55:34
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.tida_okinawa.corona.internal.ui.util.PreferenceUtils;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.ui.data.ConnectionParameter;

/**
 * プリファレンスページ(DB接続関連）
 * 
 * @author shingo-takahashi
 * 
 */
public class ConnectionPreferencePage extends PreferencePage {

    private TableViewer tableView;
    List<ConnectionParameter> conParamList;
    Button btnNew;
    Button btnEdit;
    Button btnDel;
    Button btnMrk;
    Button btnTest;
    Button btnConn;
    private int markup = -1;


    /** コンストラクター */
    public ConnectionPreferencePage() {
        super();
        setPreferenceStore(UIActivator.getDefault().getPreferenceStore());
    }


    /* ****************************************
     * ユーザーインターフェイスの作成
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout(4, true));

        final Label lbl = new Label(container, SWT.LEFT);
        lbl.setText(Messages.ConnectionPreferencePage_textDatabaseOutline);
        GridData gd = new GridData();
        gd.horizontalSpan = 4;
        gd.verticalSpan = 1;
        lbl.setLayoutData(gd);

        tableView = new TableViewer(container, SWT.FULL_SELECTION | SWT.CHECK | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        final TableViewer finalViewer = tableView;

        /* Tableの設定 */
        Table table = tableView.getTable();
        gd = new GridData();
        gd.horizontalSpan = 3;
        gd.verticalSpan = 8;
        gd.grabExcessVerticalSpace = true;
        gd.verticalAlignment = SWT.FILL;
        gd.horizontalAlignment = SWT.FILL;
        table.setLayoutData(gd);
        TableColumn col1 = new TableColumn(table, SWT.LEFT);
        col1.setText(Messages.ConnectionPreferencePage_columnConnectionName);
        col1.setWidth(160);
        TableColumn col2 = new TableColumn(table, SWT.LEFT);
        col2.setText(Messages.ConnectionPreferencePage_columnConnectionString);
        col2.setWidth(320);
        table.setHeaderVisible(true);
        tableView.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                int index = finalViewer.getTable().getSelectionIndex();
                if (index != -1) {
                    updateButtons(index);
                }
            }
        });
        // ContentProvider
        tableView.setContentProvider(new TableContentProvider());
        // LabelProvider
        tableView.setLabelProvider(new TableLabelProvider());

        conParamList = PreferenceUtils.getConnectionParameters();
        if (conParamList.size() == 0) {
            MessageDialog.openInformation(getShell(), Messages.ConnectionPreferencePage_messageConnectionDatabase,
                    Messages.ConnectionPreferencePage_messageConnectionDatabaseFail);
            conParamList = PreferenceUtils.getDefaultConnectionParameters();
        }
        tableView.setInput(conParamList);

        table.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.detail == SWT.CHECK) {
                    TableItem ti = (TableItem) event.item;
                    int index = ti.getParent().indexOf(ti);
                    setMarkup(index);
                }
            }
        });

        GridData btnGd = new GridData();
        btnGd.horizontalSpan = 1;
        btnGd.verticalSpan = 1;
        btnGd.widthHint = 160;

        /**
         * 新規ボタン
         * 新規接続先作成用のダイアログを表示する
         */
        btnNew = new Button(container, SWT.PUSH);
        btnNew.setText(Messages.ConnectionPreferencePage_textNew);
        btnNew.setLayoutData(btnGd);
        btnNew.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                /* 新規で設定ダイアログを表示 */
                ConnectionParameter cp = new ConnectionParameter(
                        "", Messages.ConnectionPreferencePage_connectionH2DbParameter, Messages.ConnectionPreferencePage_connectH2DbUser, Messages.ConnectionPreferencePage_connectH2DbPassWord); //$NON-NLS-1$ 
                DBConnectionDialog dialog = new DBConnectionDialog(getShell(), cp);
                int ret = dialog.open();
                if (ret == IDialogConstants.OK_ID) {
                    conParamList.add(dialog.getResult());
                    finalViewer.add(dialog.getResult());
                }
            }
        });

        /**
         * 編集ボタン
         * 選択された接続先を元に編集用のダイアログを表示する
         */
        btnEdit = new Button(container, SWT.PUSH);
        btnEdit.setText(Messages.ConnectionPreferencePage_buttonEdit);
        btnEdit.setLayoutData(btnGd);
        btnEdit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                /* 選択項目を設定ダイアログを表示 */
                ConnectionParameter editCP = conParamList.get(finalViewer.getTable().getSelectionIndex());
                DBConnectionDialog dialog = new DBConnectionDialog(getShell(), (ConnectionParameter) editCP.clone());
                int ret = dialog.open();
                if (ret == IDialogConstants.OK_ID) {
                    /* 内容の差し替え */
                    editCP.copy(dialog.getResult());
                    finalViewer.update(editCP, null);
                }
            }
        });

        /**
         * 削除ボタン
         * 選択された接続先を削除する
         */
        btnDel = new Button(container, SWT.PUSH);
        btnDel.setText(Messages.ConnectionPreferencePage_buttonRemove);
        btnDel.setLayoutData(btnGd);
        btnDel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Table t = finalViewer.getTable();
                int oldSelected = t.getSelectionIndex();

                IStructuredSelection sel = (IStructuredSelection) finalViewer.getSelection();
                /* 消す */
                Object[] objs = sel.toArray();
                for (int i = 0; i < objs.length; i++) {
                    ConnectionParameter cp = (ConnectionParameter) objs[i];
                    conParamList.remove(cp);
                }
                finalViewer.remove(objs);

                /* 一覧の上にあるものを消すと、マークアップのインデックスがずれる */
                /* Memo indexじゃなくてcpで管理すれば、これは要らない */
                if ((oldSelected != -1) && (getMarkup() > oldSelected)) {
                    setMarkup(getMarkup() - 1);
                }

                /* 選択し直す */
                int itemCount = t.getItemCount();
                int newSelection = -1;
                if (itemCount > oldSelected) {
                    /* 消した後の数より選択位置が小さければ、同じ位置を選択 */
                    newSelection = oldSelected;
                } else if (itemCount > 0) {
                    if (oldSelected > 0) {
                        /* 消したものが末尾なら、ひとつ上を選択する */
                        newSelection = itemCount - 1;
                    }
                }
                if (newSelection != -1) {
                    finalViewer.setSelection(new StructuredSelection(t.getItem(newSelection).getData()));
                }
                updateButtons(newSelection);
            }
        });

        /**
         * マークアップボタン
         * 選択された接続先にチェックを入れる
         * 
         */
        btnMrk = new Button(container, SWT.PUSH);
        btnMrk.setText(Messages.ConnectionPreferencePage_buttonMarkup);
        btnMrk.setLayoutData(btnGd);
        btnMrk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int selected = finalViewer.getTable().getSelectionIndex();
                if (selected != -1) {
                    setMarkup(selected);
                }
            }
        });

        /**
         * テスト接続ボタン
         * TODO:接続確認しなくてもよくなるので、削除する
         */
        // btnTest = new Button(container, SWT.PUSH);
        // btnTest.setText(Messages.ConnectionPreferencePage_buttonConnectTest);
        // btnTest.setLayoutData(btnGd);
        // btnTest.addSelectionListener(new SelectionAdapter() {
        // @Override
        // public void widgetSelected(SelectionEvent e) {
        // IStructuredSelection sel = (IStructuredSelection)
        // finalViewer.getSelection();
        // if (sel.getFirstElement() != null) {
        // connectionTest(getShell(), (ConnectionParameter)
        // sel.getFirstElement(), true);
        // }
        // }
        // });

        /**
         * 接続復帰ボタン
         * 
         * TODO：削除するかどうか検討要
         * 
         */
        btnConn = new Button(container, SWT.PUSH);
        btnConn.setText(Messages.ConnectionPreferencePage_buttonConnectComeback);
        btnConn.setLayoutData(btnGd);
        btnConn.setToolTipText(Messages.ConnectionPreferencePage_buttonConnectComebackToolTip);
        btnConn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (IoActivator.getService().isConnect()) {
                    MessageDialog qDlg = new MessageDialog(getShell(), Messages.ConnectionPreferencePage_messageConnectComeback, null,
                            Messages.ConnectionPreferencePage_messageConnectOk, MessageDialog.INFORMATION,
                            new String[] { Messages.ConnectionPreferencePage_messageOk }, 0);
                    qDlg.open();
                    return;
                }
                boolean result = IoActivator.getService().reConnect();
                String msg = null;
                if (result) {
                    msg = Messages.ConnectionPreferencePage_messageConnectSuccess;
                } else {
                    msg = Messages.ConnectionPreferencePage_messageConnectFail;
                }
                MessageDialog qDlg = new MessageDialog(getShell(), Messages.ConnectionPreferencePage_messageConnectComeback, null, msg,
                        MessageDialog.INFORMATION, new String[] { Messages.ConnectionPreferencePage_messageOk }, 0);
                qDlg.open();
            }
        });

        updateButtons(-1);
        setMarkup(PreferenceUtils.getConnectionIndex());

        return container;
    }


    @Override
    protected void performDefaults() {
        conParamList = PreferenceUtils.getDefaultConnectionParameters();
        tableView.setInput(conParamList);
        setMarkup(PreferenceUtils.getDefaultConnectionIndex());
    }


    @Override
    public boolean performOk() {
        PreferenceUtils.setConnectionParameters(conParamList);
        PreferenceUtils.setConnectionIndex(getMarkup());
        return super.performOk();
    }

    static class TableContentProvider extends ArrayContentProvider {
    }

    class TableLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider {
        public TableLabelProvider() {
            FontData fd = new FontData();
            fd.setStyle(SWT.BOLD);
            fd.height = 10;
            boldFont = new Font(null, fd);
        }


        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }


        @Override
        public String getColumnText(Object element, int columnIndex) {
            ConnectionParameter cp = (ConnectionParameter) element;
            switch (columnIndex) {
            case 0:
                return cp.name;
            case 1:
                return cp.url;
            case 2:
                return cp.user;
            case 3:
                return cp.passwd;
            }
            return ""; //$NON-NLS-1$
        }

        private final Font boldFont;


        @Override
        public Font getFont(Object element) {
            int index = conParamList.indexOf(element);
            if (index == getMarkup()) {
                return boldFont;
            }
            return null;
        }


        @Override
        public void dispose() {
            boldFont.dispose();
            super.dispose();
        }
    }


    /**
     * <pre>
     * 使用中の接続情報が選択中の場合は
     * 除去ボタン及びマークアップボタンの非活性化する
     * </pre>
     * 
     * @param selectionIndex
     */
    void updateButtons(int selectionIndex) {
        boolean enabled = true;
        if (selectionIndex == -1) {
            enabled = false;
        }
        boolean isMarkup = (selectionIndex != getMarkup());

        btnDel.setEnabled(enabled && isMarkup);
        btnEdit.setEnabled(enabled);
        btnMrk.setEnabled(enabled && isMarkup);
        // btnTest.setEnabled(enabled);
    }


    /**
     * 前回のマークアップを解除して、indexのアイテムをマークアップする
     * 
     * @param index
     */
    void setMarkup(int index) {
        TableItem[] items = tableView.getTable().getItems();
        if ((index < 0) || (items.length <= index)) {
            throw new ArrayIndexOutOfBoundsException(index);
        }

        /* チェック状態とボールド状態を更新 */
        int oldMarkup = markup;
        markup = index;
        if ((0 <= oldMarkup) && (oldMarkup < items.length)) {
            tableView.update(items[oldMarkup].getData(), null);
            items[oldMarkup].setChecked(false);
        }
        if ((0 <= markup) && (markup < items.length)) {
            tableView.update(items[markup].getData(), null);
            items[markup].setChecked(true);
        }

        /* 選択しているアイテムをマークアップしたときは、ボタンの活性状態が変わる */
        updateButtons(tableView.getTable().getSelectionIndex());
    }


    int getMarkup() {
        return markup;
    }


    boolean connectionTest(Shell parent, ConnectionParameter cp, boolean disp) {
        String[] err = com.tida_okinawa.corona.io.IoActivator.getService().connectTest(cp.url, cp.user, cp.passwd);

        if (disp) {
            if (err.length == 0) {
                MessageDialog.openInformation(parent, Messages.ConnectionPreferencePage_messageConnectTest,
                        Messages.ConnectionPreferencePage_messageConnectSuccess);
            } else {
                MessageDialog.openError(parent, Messages.ConnectionPreferencePage_messageConnectTest,
                        Messages.ConnectionPreferencePage_messageConnectFailDetail + err[0] + Messages.ConnectionPreferencePage_rightSquareBracket);
            }
        }
        return err.length == 0;
    }

    private static class DBConnectionDialog extends Dialog {
        ConnectionParameter cp;


        public DBConnectionDialog(Shell parent, ConnectionParameter cp) {
            super(parent);
            this.cp = cp;
        }


        @Override
        protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            newShell.setText(Messages.ConnectionPreferencePage_shellConnectDatabaseSetting);
        }


        @Override
        protected Control createContents(Composite parent) {
            Control control = super.createContents(parent);
            updateButtons(validatePage());
            setMessage(null);
            return control;
        }


        /**
         * URLを入力するダイアログを描画します。
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


        private void createInputArea(Composite parent) {
            Composite container = new Composite(parent, SWT.NONE);
            container.setLayout(new GridLayout(4, true));
            VerifyListener verifyListener = new DBConnectionVerifyListener();

            GridData txtGd = new GridData(GridData.FILL_HORIZONTAL);
            txtGd.grabExcessHorizontalSpace = true;
            txtGd.horizontalSpan = 3;

            /**
             * 接続名テキストボックス
             */
            Text nameText = createInputItem(container, Messages.ConnectionPreferencePage_textConnectionName, cp.name, txtGd, verifyListener);
            nameText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    Text source = (Text) e.getSource();
                    cp.name = source.getText();
                    updateButtons(validatePage());
                }
            });

            /**
             * 接続文字列
             * 
             */
            final Text urlText = createInputItem(container, Messages.ConnectionPreferencePage_textConnectionString, cp.url, txtGd, verifyListener);
            urlText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    Text source = (Text) e.getSource();
                    cp.url = source.getText();
                    updateButtons(validatePage());
                }
            });

            /**
             * 接続ユーザ
             */
            Text usrText = createInputItem(container, Messages.ConnectionPreferencePage_textConnectionUser, cp.user, txtGd, verifyListener);
            // usrText.addModifyListener(new ModifyListener() {
            // @Override
            // public void modifyText(ModifyEvent e) {
            // Text source = (Text) e.getSource();
            // cp.user = source.getText();
            // updateButtons(validatePage());
            // }
            // });
            // ユーザIDの入力を不可とする
            usrText.setEnabled(false);
            new Label(container, SWT.NONE).setText(Messages.ConnectionPreferencePage_textConnectionPassword);
            /**
             * 接続パスワード
             */
            Text pswdText = new Text(container, SWT.PASSWORD | SWT.SINGLE | SWT.BORDER);
            pswdText.setLayoutData(txtGd);
            pswdText.setText(cp.passwd);
            // pswdText.addModifyListener(new ModifyListener() {
            // @Override
            // public void modifyText(ModifyEvent e) {
            // Text source = (Text) e.getSource();
            // cp.passwd = source.getText();
            // updateButtons(validatePage());
            // }
            // });
            // pswdText.addVerifyListener(verifyListener);
            // パスワードの入力を不可とする
            pswdText.setEnabled(false);

            GridData informGd = new GridData(GridData.FILL_HORIZONTAL);
            informGd.grabExcessHorizontalSpace = true;
            informGd.horizontalSpan = 4;
            Label inform = new Label(container, SWT.RIGHT);
            inform.setText(Messages.ConnectionPreferencePage_textWarningComma);
            inform.setLayoutData(informGd);

            /**
             * テスト接続ボタン
             * TODO:削除予定
             */
            // testButton = new Button(container, SWT.PUSH);
            // testButton.setText(Messages.ConnectionPreferencePage_buttonConnectionTest);
            // testButton.addSelectionListener(new SelectionAdapter() {
            // @Override
            // public void widgetSelected(SelectionEvent e) {
            // if (connectionTest(getShell(), cp, true)) {
            // setButtonFocus(OK);
            // } else {
            // urlText.setFocus();
            // }
            // }
            // });
        }


        // void setButtonFocus(int buttonId) {
        // getButton(buttonId).setFocus();
        // }

        // private Button testButton;


        private static Text createInputItem(Composite parent, String label, String defValue, GridData txtGd, VerifyListener verifyListener) {
            new Label(parent, SWT.NONE).setText(label);
            Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
            text.setLayoutData(txtGd);
            text.setText(defValue);
            text.addVerifyListener(verifyListener);
            return text;
        }


        public ConnectionParameter getResult() {
            return cp;
        }


        boolean validatePage() {
            /* パスワード以外は、空白不可 */
            if (cp.name.length() == 0) {
                setMessage(Messages.ConnectionPreferencePage_messageEntryConnectionName);
                return false;
            }
            if (cp.url.length() == 0) {
                setMessage(Messages.ConnectionPreferencePage_messageEntryConnectionString);
                return false;
            }
            if (cp.user.length() == 0) {
                setMessage(Messages.ConnectionPreferencePage_messageEntryConnectionUser);
                return false;
            }

            setMessage(null);
            return true;
        }


        @Override
        protected boolean isResizable() {
            return true;
        }


        @Override
        protected Point getInitialSize() {
            return new Point(480, 320);
        }


        void updateButtons(boolean enabled) {
            getButton(OK).setEnabled(enabled);
            // testButton.setEnabled(enabled);
        }
    }

    /**
     * DBConnectionDialog で接続情報欄が編集される直前リスナー
     * 
     * @author miyaguni
     * 
     */
    static class DBConnectionVerifyListener implements VerifyListener {
        boolean removeComma = false;


        /**
         * 入力された文字列が正しいか、変更前にチェックする。
         * 不正な文字が入っていれば変更されない
         * 
         * 1. 文字列にカンマが含まれているとダメ
         * 
         * TODO カンマ入りの文字列をコピペすると現在はまったく貼れないけど、カンマ以外は貼りつけられた方がいいのかな
         * Memo カンマを抜いたほうがいいのかわからなかったので、分岐でカンマ抜きを実装してみた。
         * 
         * @param e
         */
        @Override
        public void verifyText(VerifyEvent e) {
            if (e.text.indexOf(",") != -1) { //$NON-NLS-1$
                if (removeComma) {
                    e.text = e.text.replaceAll(",", ""); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    e.doit = false;
                }
            }
        }
    }

}
