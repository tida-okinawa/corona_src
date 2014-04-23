/**
 * @version $Id: EntryWizardPage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2011/10/26 20:12:08
 * @author kyohei-miyazato
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.common.Encoding;
import com.tida_okinawa.corona.correction.CorrectionPreferenceInitializer;
import com.tida_okinawa.corona.correction.common.DocumentSpliter;
import com.tida_okinawa.corona.correction.common.FileUtil;
import com.tida_okinawa.corona.correction.data.CoronaDocumentInformation;
import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.XlsToArray;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.table.IFieldHeader;
import com.tida_okinawa.corona.ui.views.db.CreateClaimTableValidator;

/**
 * 
 * @author kyohei-miyazato
 */
public class EntryWizardPage extends WizardPageBase {

    /**
     * コンストラクター
     * 
     * @param pageName
     *            ページタイトル
     */
    public String readpath;
    public String readdefinePath;


    public EntryWizardPage(String pageName) {
        super(pageName);
        setMessage(Messages.EntryWizardPage_PageMessage);
        this.sectionName = pageName;
    }

    /* ****************************************
     * getter
     */
    private IClaimData claimData;


    /**
     * @return 登録した問い合わせデータ。登録に失敗していた場合はnull
     */
    public IClaimData getClaimData() {
        return claimData;
    }

    /* カラム情報を取得する */
    private List<IFieldHeader> currentFieldHeaders;


    /**
     * 定義ファイルに記述したテーブル構造から、カラム情報を取得する
     * 
     * @return 問い合わせデータを登録するテーブルのカラム情報
     * @throws IOException
     */
    public List<IFieldHeader> getFieldHeaders() throws IOException {
        String path = dataFileCombo.getText();
        String definePath = defineFileCombo.getText();
        String tableName = tableText.getText();
        String setflnm = ""; //$NON-NLS-1$

        readpath = dataFileCombo.getText();
        readdefinePath = defineFileCombo.getText();
        /* ファイル名を取得 */
        Pattern searchPattern = Pattern.compile(".*\\\\(.+)\\..*$"); //$NON-NLS-1$
        Matcher match = searchPattern.matcher(path);
        if (match.find()) {
            setflnm = match.group(1);
        }

        if (xlsTypeButton.getSelection()) {
            String chkstr = null;
            int loopCnt = 0;

            try {
                XlsToArray xArray = new XlsToArray(path);
                ArrayList<String> csvArray = xArray.getData();
                /* 拡張子を取得 */

                // SQLファイルファイルパスを指定
                File sqlFile = File.createTempFile(setflnm, ".sql"); //$NON-NLS-1$
                sqlFile.deleteOnExit();
                BufferedWriter sqlwriter = null;
                sqlwriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sqlFile)));
                // CSVファイル作成
                File csvFile = File.createTempFile(setflnm, ".csv"); //$NON-NLS-1$
                csvFile.deleteOnExit();
                BufferedWriter csvwriter = null;
                csvwriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile)));

                for (String str : csvArray) {
                    if (loopCnt == 0) {
                        String setstr = str.substring(1, str.length() - 1);
                        sqlwriter.write(setstr);
                        sqlwriter.newLine();
                    } else if (loopCnt == 1) {
                        chkstr = str;
                    } else if (!str.equals(chkstr)) {
                        /*
                         * memo NEWLINTで改行させるとシステム依存のため、「MS932」で出力される。　　
                         * 内部的にはUTF8で動作する必要があるため「"\n"」を使用し改行する。
                         */
                        str = str + "\n"; //$NON-NLS-1$
                        csvwriter.write(str);
                    }
                    loopCnt++;
                }
                sqlwriter.close();
                csvwriter.close();


                StringBuilder destPath = new StringBuilder();
                destPath.append(csvFile.getParent()).append("\\").append(setflnm).append(path.substring(path.lastIndexOf("."), path.length())); //$NON-NLS-1$ //$NON-NLS-2$
                FileChannel srcChannel = new FileInputStream(csvFile.getPath()).getChannel();
                FileChannel dstChannel = new FileOutputStream(destPath.toString()).getChannel();
                try {
                    srcChannel.transferTo(0, srcChannel.size(), dstChannel);
                } finally {
                    srcChannel.close();
                    dstChannel.close();
                }
                path = csvFile.getPath();
                readpath = destPath.toString();
                definePath = sqlFile.getPath();
                readdefinePath = sqlFile.getPath();
                headerCheck.setSelection(false);
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else {
            if (sjiTypeButton.getSelection()) {
                BufferedReader csvbr = null;
                BufferedWriter csvbw = null;
                // CSVファイルの場合、入力ファイルのファイルパスを指定
                csvbr = new BufferedReader(new InputStreamReader(new FileInputStream(path), Encoding.MS932.toString()));
                /* 書き込み用の一時ファイル作成 */
                File csvFile = File.createTempFile(setflnm, ".csv"); //$NON-NLS-1$
                csvFile.deleteOnExit();
                csvbw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile)));
                String line = ""; //$NON-NLS-1$
                while ((line = csvbr.readLine()) != null) {
                    String outline = line + "\n"; //$NON-NLS-1$
                    csvbw.write(outline);
                }
                csvbw.close();

                StringBuilder csvdestPath = new StringBuilder();
                csvdestPath.append(csvFile.getParent()).append("\\").append(setflnm).append(path.substring(path.lastIndexOf("."), path.length())); //$NON-NLS-1$ //$NON-NLS-2$
                FileChannel csvsrcChannel = new FileInputStream(csvFile.getPath()).getChannel();
                FileChannel csvdstChannel = new FileOutputStream(csvdestPath.toString()).getChannel();
                try {
                    csvsrcChannel.transferTo(0, csvsrcChannel.size(), csvdstChannel);
                } finally {
                    csvsrcChannel.close();
                    csvdstChannel.close();
                }
                path = csvFile.getPath();
                readpath = csvdestPath.toString();

                BufferedReader sqlbr = null;
                BufferedWriter sqlbw = null;
                // CSVファイルの場合、入力ファイルのファイルパスを指定
                sqlbr = new BufferedReader(new InputStreamReader(new FileInputStream(definePath), Encoding.MS932.toString()));
                /* 書き込み用の一時ファイル作成 */
                File sqlFile = File.createTempFile(setflnm, ".sql"); //$NON-NLS-1$
                sqlFile.deleteOnExit();
                sqlbw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sqlFile)));
                line = ""; //$NON-NLS-1$
                while ((line = sqlbr.readLine()) != null) {
                    String outline = line + "\n"; //$NON-NLS-1$
                    sqlbw.write(outline);
                }
                sqlbw.close();
                StringBuilder sqldestPath = new StringBuilder();
                sqldestPath.append(sqlFile.getParent())
                        .append("\\").append(setflnm).append(definePath.substring(definePath.lastIndexOf("."), definePath.length())); //$NON-NLS-1$ //$NON-NLS-2$
                FileChannel sqlsrcChannel = new FileInputStream(sqlFile.getPath()).getChannel();
                FileChannel sqldstChannel = new FileOutputStream(sqldestPath.toString()).getChannel();
                try {
                    sqlsrcChannel.transferTo(0, sqlsrcChannel.size(), sqldstChannel);
                } finally {
                    sqlsrcChannel.close();
                    sqldstChannel.close();
                }
                definePath = sqlFile.getPath();
                readdefinePath = sqldestPath.toString();
            }
        }

        try {
            List<IFieldHeader> newFieldHeaders = IoActivator.getService().getTableColumns(path, definePath, tableName);
            if (currentFieldHeaders == null) {
                currentFieldHeaders = newFieldHeaders;
            } else if (!currentFieldHeaders.equals(newFieldHeaders)) {
                currentFieldHeaders = newFieldHeaders;
            }
        } catch (SQLException e) {
            currentFieldHeaders = new ArrayList<IFieldHeader>();
            setPageComplete(false);

            MessageDialog.openError(getShell(), Messages.EntryWizardPage_ErrorMessage_FailedGetColumn, e.getLocalizedMessage());
            e.printStackTrace();
        } catch (IOException e) {
            currentFieldHeaders = new ArrayList<IFieldHeader>();
            setPageComplete(false);
            MessageDialog.openError(getShell(), Messages.EntryWizardPage_ErrorMessage_FailedGetColumn, e.getLocalizedMessage());
            e.printStackTrace();
        }
        return currentFieldHeaders;
    }


    private List<IFieldHeader> targetFields = new ArrayList<IFieldHeader>();


    /**
     * @return 誤記補正処理の対象フィールド一覧。問い合わせデータの登録に失敗したときは、null
     */
    public List<IFieldHeader> getTargetFieldHeaders() {
        return targetFields;
    }


    /* ****************************************
     * 処理
     */
    /**
     * ページの情報をもとに、問い合わせデータを登録する。
     * 
     * @return 登録した問い合わせデータ。処理に失敗したらnull
     */
    protected IClaimData finished() {
        /*
         * 「登録」ボタンを押したときの処理 誤記補正を呼び出し、処理させる。
         */
        String path = dataFileCombo.getText();
        /* オブジェクトのText取得 */
        String tableName = tableText.getText();

        // ドキュメント形式
        if (docTypeButton.getSelection()) {
            // フォーマットを変換
            CoronaDocumentInformation docInfo = docInfos.get(docInfoCombo.getSelectionIndex());
            DocumentSpliter spliter = new DocumentSpliter();
            spliter.setDocumentInformation(docInfo);
            if (!spliter.split(new File(path))) {
                MessageDialog.openError(getShell(), Messages.EntryWizardPage_ErrorTitle_FailedReadDataFile,
                        Messages.EntryWizardPage_ErrorMessage_FailedReadDataFile);
                return null;
            }
            if (spliter.getOutput().isEmpty()) {
                MessageDialog
                        .openError(getShell(), Messages.EntryWizardPage_ErrorTitle_FailedReadDataFile, Messages.EntryWizardPage_ErrorMessage_EmptyDocument);
            } else {
                List<String> records = spliter.getOutput();
                try {
                    long s = System.currentTimeMillis();
                    // インポート
                    claimData = IoActivator.getService().importClaimDataForDocument(path, tableName, tgtText.getText(), records);
                    CoronaActivator.debugLog(Messages.EntryWizardPage_entryDatabaseTime + (System.currentTimeMillis() - s) + "mSec"); //$NON-NLS-1$
                } catch (SQLException e) {
                    e.printStackTrace();
                    MessageDialog.openError(getShell(), Messages.EntryWizardPage_ErrorTitle_FailedReadDataFile, e.getLocalizedMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    MessageDialog.openError(getShell(), Messages.EntryWizardPage_ErrorTitle_FailedReadDataFile, e.getLocalizedMessage());
                }
                if (claimData != null) {
                    // フィールド指定を固定で設定
                    claimData.setDispIdField(1);
                    claimData.setProductField(3);
                    claimData.commit();
                    targetFields = new ArrayList<IFieldHeader>();
                    targetFields.add(claimData.getFieldInformation(2));
                }
            }
        } else {

            /* DBへインポート */
            try {
                long s = System.currentTimeMillis();
                claimData = IoActivator.getService().importClaimData(readpath, readdefinePath, tableName, headerCheck.getSelection());
                CoronaActivator.debugLog(Messages.EntryWizardPage_entryDatabaseTime + (System.currentTimeMillis() - s) + "mSec"); //$NON-NLS-1$
            } catch (SQLException e) {
                e.printStackTrace();
                MessageDialog.openError(getShell(), Messages.EntryWizardPage_ErrorTitle_FailedReadDataFile, e.getLocalizedMessage());
            } catch (IOException e) {
                e.printStackTrace();
                MessageDialog.openError(getShell(), Messages.EntryWizardPage_ErrorTitle_FailedReadDataFile, e.getLocalizedMessage());
            }
        }
        // コンボボックス入力情報をsave
        saveDefault();
        return claimData;
    }


    /**
     * データ形式のチェック状態に応じて、入力項目の有効無効を切り替える
     */
    void updateEnabled() {
        boolean csvEnabled = csvTypeButton.getSelection();
        boolean docEnabled = docTypeButton.getSelection();
        headerCheck.setEnabled(csvEnabled);
        sjiTypeButton.setEnabled(csvEnabled);
        utfTypeButton.setEnabled(csvEnabled);
        defineFileButton.setEnabled(csvEnabled);
        defineFileCombo.setEnabled(csvEnabled);
        tgtText.setEnabled(docEnabled);
        docInfoCombo.setEnabled(docEnabled);
    }


    /* ****************************************
     * UI
     */
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        Composite composite = CompositeUtil.defaultComposite(parent, 3);
        GridLayout gridLayout = new GridLayout(3, false);
        gridLayout.marginWidth = 10;
        gridLayout.marginHeight = 5;
        composite.setLayout(gridLayout);
        GridData layoutData = new GridData(SWT.FILL, SWT.NONE, true, false);
        composite.setLayoutData(layoutData);

        createRadioButtonGroup(composite);
        createRadi2ButtonGroup(composite);
        createClaimFileGroup(composite);
        createDefineFileGroup(composite);
        createTableNameGroup(composite);
        createTargetNameGroup(composite);
        createFormatGroup(composite);

        setControl(composite);

        restoreDefault();
        updateEnabled();

        fieldValidate();
        setErrorMessage(null);
    }

    private List<CoronaDocumentInformation> docInfos;
    Combo docInfoCombo;


    private void createFormatGroup(Composite parent) {
        docInfos = CorrectionPreferenceInitializer.getDocumentInfomations();
        /* ラベル */
        Label dataLabel = new Label(parent, SWT.NONE);
        dataLabel.setText("定義："); //$NON-NLS-1$
        /* テキストボックス */
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false, 2, 0);
        docInfoCombo = new Combo(parent, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        docInfoCombo.setLayoutData(gridData);
        for (CoronaDocumentInformation docInfo : docInfos) {
            docInfoCombo.add(docInfo.getName());
            docInfoCombo.setData(docInfo.getName(), docInfo);
        }
        if (docInfoCombo.getItemCount() > 0) {
            docInfoCombo.select(0);
        }
    }

    Text tgtText;


    private void createTargetNameGroup(Composite parent) {
        /* ラベル */
        Label dataLabel = new Label(parent, SWT.NONE);
        dataLabel.setText(Messages.EntryWizardPage_Label_Target);

        GridData gd = new GridData(SWT.FILL, SWT.NONE, true, false, 2, 0);
        tgtText = new Text(parent, SWT.SINGLE | SWT.BORDER);
        tgtText.setLayoutData(gd);

        tgtText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                fieldValidate();
            }
        });
    }

    private Button csvTypeButton;
    Button xlsTypeButton;
    Button docTypeButton;
    Button sjiTypeButton;
    Button utfTypeButton;


    /**
     * @return データ形式で、ドキュメント形式を選択しているとtrue
     * @exception NullPointerException
     *                ページ作成前にこのメソッドを呼び出すと発生する
     */
    public boolean isDocTypeSelected() {
        return docTypeButton.getSelection();
    }


    /**
     * @return データ形式で、エクセル形式を選択しているとtrue
     * @exception NullPointerException
     *                ページ作成前にこのメソッドを呼び出すと発生する
     */
    public boolean isXlsTypeSelected() {
        return xlsTypeButton.getSelection();
    }


    private void createRadioButtonGroup(Composite parent) {
        /* ラベル */
        Label dataLabel = new Label(parent, SWT.NONE);
        dataLabel.setText("データ形式："); //$NON-NLS-1$s

        Composite comDataField = new Composite(parent, SWT.FILL);
        GridLayout gr = new GridLayout();
        gr.numColumns = 3;
        comDataField.setLayout(gr);
        comDataField.setLayoutData(new GridData());


        // ラジオボタン
        GridData gd = new GridData(SWT.FILL, SWT.NONE, true, false);
        csvTypeButton = new Button(comDataField, SWT.RADIO);
        csvTypeButton.setText("CSV形式 [*.csv]"); //$NON-NLS-1$
        csvTypeButton.setLayoutData(gd);
        csvTypeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateEnabled();
                fieldValidate();
            }
        });
        // ラジオボタン
        gd = new GridData(SWT.FILL, SWT.NONE, true, false);
        xlsTypeButton = new Button(comDataField, SWT.RADIO);
        xlsTypeButton.setText("表形式 [*.xlsx]"); //$NON-NLS-1$
        xlsTypeButton.setLayoutData(gd);
        xlsTypeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateEnabled();
                fieldValidate();
            }
        });
        // ラジオボタン
        gd = new GridData(SWT.FILL, SWT.NONE, true, false);
        docTypeButton = new Button(comDataField, SWT.RADIO);
        docTypeButton.setText("ドキュメント形式 [*.txt , *.pdf]"); //$NON-NLS-1$
        docTypeButton.setLayoutData(gd);
        docTypeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateEnabled();
                fieldValidate();
            }
        });

        CompositeUtil.createLabel(parent, "", 0); //$NON-NLS-1$
    }

    Combo dataFileCombo;
    private Button dataFileButton;
    Button headerCheck;


    private void createRadi2ButtonGroup(Composite parent) {
        /* ラベル */
        Label dataLabel = new Label(parent, SWT.NONE);
        dataLabel.setText("文字コード："); //$NON-NLS-1$

        Composite comStringField = new Composite(parent, SWT.FILL);
        GridLayout gr = new GridLayout();
        gr.numColumns = 3;
        comStringField.setLayout(gr);
        comStringField.setLayoutData(new GridData());

        // ラジオボタン
        GridData gd = new GridData(SWT.FILL, SWT.NONE, true, false);
        sjiTypeButton = new Button(comStringField, SWT.RADIO);
        sjiTypeButton.setText("SJIS"); //$NON-NLS-1$
        sjiTypeButton.setLayoutData(gd);
        sjiTypeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateEnabled();
                fieldValidate();
            }
        });
        // ラジオボタン
        gd = new GridData(SWT.FILL, SWT.NONE, true, false);
        utfTypeButton = new Button(comStringField, SWT.RADIO);
        utfTypeButton.setText("UTF-8"); //$NON-NLS-1$
        utfTypeButton.setLayoutData(gd);
        utfTypeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateEnabled();
                fieldValidate();
            }
        });

        Label labelDummy = new Label(comStringField, SWT.LEFT);
        labelDummy.setText(""); //$NON-NLS-1$

        CompositeUtil.createLabel(parent, "", 0); //$NON-NLS-1$
    }

    Button headerCheck2;


    String[] getFileExtensions() {
        if (csvTypeButton.getSelection()) {
            return new String[] { "*.csv", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
        } else if (xlsTypeButton.getSelection()) {
            return new String[] { "*.xlsx", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
        } else if (docTypeButton.getSelection()) {
            List<String> exts = new ArrayList<String>(docInfos.size() + 1);
            for (CoronaDocumentInformation di : docInfos) {
                exts.add(di.getExtension());
            }
            exts.add("*.*"); //$NON-NLS-1$
            return exts.toArray(new String[exts.size()]);
        }
        return new String[] { "*.*" }; //$NON-NLS-1$
    }


    private void createClaimFileGroup(Composite parent) {
        /* 問い合わせデータのセット(ラベル、テキストボックス、ボタン) */
        Label dataLabel = new Label(parent, SWT.NONE);
        dataLabel.setText(Messages.EntryWizardPage_textDataFile);
        dataFileCombo = createCombo(parent, ""); //$NON-NLS-1$
        dataFileButton = new Button(parent, SWT.PUSH);
        dataFileButton.setText("参照"); //$NON-NLS-1$
        /* (データファイルの)「参照」ボタンを押したときの処理 */
        final Combo finalCombo = dataFileCombo;
        dataFileButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent fileDialog) {
                FileDialog openDialog = new FileDialog(getShell(), SWT.OPEN);
                openDialog.setFilterExtensions(getFileExtensions());
                String file = openDialog.open();
                if (file != null) {
                    finalCombo.setText(file);
                    /* ユーザが選択した拡張子に応じて、[定義]コンボボックスの選択を変更する */
                    if (docTypeButton.getSelection()) {
                        if (docInfoCombo.getItemCount() > openDialog.getFilterIndex()) {
                            docInfoCombo.select(openDialog.getFilterIndex());
                        }
                    }
                }
            }
        });

        /* 問い合わせデータが履歴から選択されたときの処理 */
        dataFileCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (docTypeButton.getSelection()) {
                    String[] fileExt = getFileExtensions(); // 拡張子の配列
                    String selectPath = dataFileCombo.getText();
                    // 履歴から選択されたファイルの拡張子が、拡張子配列の何番目かを取得
                    String ext = "*." + FileUtil.getExtension(new File(selectPath)); //$NON-NLS-1$
                    for (int index = 0; index < fileExt.length - 1; index++) {
                        if (fileExt[index].contains(ext)) {
                            docInfoCombo.select(index);
                            break;
                        }
                    }
                }
            }
        });

        /* ヘッダ行有無のチェックボックス */
        CompositeUtil.createLabel(parent, "", 1); //$NON-NLS-1$
        headerCheck = new Button(parent, SWT.CHECK);
        headerCheck.setText(Messages.EntryWizardPage_textUseHeadLine);
        /* チェックボックスの動作 */
        headerCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent check) {
                fieldValidate();
            }
        });
        GridData layoutData = new GridData(SWT.NONE, SWT.NONE, false, false, 2, 0);
        headerCheck.setLayoutData(layoutData);
    }

    Combo defineFileCombo;
    Button defineFileButton;


    private void createDefineFileGroup(Composite parent) {
        /* データファイル系セット(ラベル、テキストボックス、ボタン) */
        Label defineLabel = new Label(parent, SWT.NONE);
        defineLabel.setText("定義ファイル："); //$NON-NLS-1$
        /* テキストボックス */
        defineFileCombo = createCombo(parent, ""); //$NON-NLS-1$
        /* ボタン */
        defineFileButton = new Button(parent, SWT.PUSH);
        defineFileButton.setText("参照"); //$NON-NLS-1$
        /* (定義ファイルの)「参照」ボタンを押したときの処理 */
        defineFileButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent fileDialog) {
                Shell shell = getShell();
                FileDialog openDialog = new FileDialog(shell, SWT.OPEN);
                openDialog.setFilterExtensions(new String[] { "*.sql", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
                String file = openDialog.open();
                if (file != null) {
                    defineFileCombo.setText(file);
                }
            }
        });
    }


    private Combo createCombo(Composite parent, String text) {
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        gridData.horizontalSpan = 1;
        Combo retCombo = new Combo(parent, SWT.SINGLE | SWT.BORDER);
        retCombo.setText(text);
        retCombo.setLayoutData(gridData);
        retCombo.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                fieldValidate();
            }
        });
        return retCombo;
    }

    Text tableText;


    private void createTableNameGroup(Composite parent) {
        Composite composite = parent;
        /* テーブルのセット(ラベル、テキストボックス) */
        Label tableLabel = new Label(composite, SWT.NONE);
        tableLabel.setText(Messages.EntryWizardPage_text);
        /* テキストボックス */
        tableText = createText(composite, ""); //$NON-NLS-1$
        ((GridData) tableText.getLayoutData()).horizontalSpan = 2;
    }


    private Text createText(Composite parent, String text) {
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1);
        Text retText = new Text(parent, SWT.SINGLE | SWT.BORDER);
        retText.setText(text);
        retText.setLayoutData(gridData);
        retText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                fieldValidate();
            }
        });
        return retText;
    }


    /* ****************************************
     * フィールドチェック
     */
    void fieldValidate() {
        /*
         * 「新規登録」 全項目入力されている。 かつ、正しいファイルパスが入力されている。 かつ、新規登録テーブル名は重複していない。
         */
        if (dataFileCombo.getText().trim().length() == 0) {
            /* dataFileText入力なし(length==0) */
            setPageComplete(false);
            setErrorMessage(Messages.EntryWizardPage_ErrorMessage_NoDataFile);
            return;
        }
        if (!new File(dataFileCombo.getText()).isFile()) {
            setPageComplete(false);
            setErrorMessage(Messages.EntryWizardPage_ErrorMessage_IllegalDataFile);
            return;
        }

        if (csvTypeButton.getSelection()) {
            if (defineFileCombo.getText().trim().length() == 0) {
                setPageComplete(false);
                setErrorMessage(Messages.EntryWizardPage_ErrorMessage_NoDefineFile);
                return;
            }
            if (!new File(defineFileCombo.getText()).isFile()) {
                /* defineFileTextに入力されたパスが正しくない */
                setPageComplete(false);
                setErrorMessage(Messages.EntryWizardPage_ErrorMessage_IllegalDefineFile);
                return;
            }
        }

        /* Table名チェック */
        /* 重複 */
        CreateClaimTableValidator tableValidator = new CreateClaimTableValidator();
        if (tableValidator.exists(tableText.getText())) {
            setPageComplete(false);
            setErrorMessage(Messages.EntryWizardPage_ErrorMessage_DBExisted);
            return;
        }
        /* 正しい形式の名前か */
        String warning = tableValidator.isValid(tableText.getText());
        if (tableText.getText().trim().length() == 0 || (warning != null)) {
            /* tableTextに入力なし(length==0) */
            setPageComplete(false);
            setErrorMessage(warning);
            return;
        }

        /* ターゲット名チェック */
        if (docTypeButton.getSelection()) {
            if (tgtText.getText().isEmpty()) {
                setPageComplete(false);
                setErrorMessage(Messages.EntryWizardPage_ErrorMessage_NoTargetName);
                return;
            }
            if (tgtText.getText().length() > 256) {
                setPageComplete(false);
                setErrorMessage(Messages.EntryWizardPage_ErrorMessage_LimitOver_TargetName);
                return;
            }
        }

        /* ドキュメント定義の選択チェック */
        if (isDocTypeSelected()) {
            if (docInfoCombo.getSelectionIndex() == -1) {
                setPageComplete(false);
                setErrorMessage(Messages.EntryWizardPage_ErrorMessage_NoDocumentSelection);
                return;
            }
        }

        setPageComplete(true);
        setErrorMessage(null);
    }


    @Override
    public boolean canFlipToNextPage() {
        /* ドキュメント形式を選択している場合、次のページには進まない */
        if (isDocTypeSelected()) {
            return false;
        }
        return super.canFlipToNextPage();
    }

    /* ****************************************
     * ページ情報の保存
     */
    private String sectionName;
    private static final String Key_CsvType = "com.tida_okinawa.corona.ui.wizards.EntryWizardPage.csvTypeButton"; //$NON-NLS-1$
    private static final String Key_xlsType = "com.tida_okinawa.corona.ui.wizards.EntryWizardPage.xlsTypeButton"; //$NON-NLS-1$
    private static final String Key_DocType = "com.tida_okinawa.corona.ui.wizards.EntryWizardPage.docTypeButton"; //$NON-NLS-1$
    private static final String Key_sjiType = "com.tida_okinawa.corona.ui.wizards.EntryWizardPage.sjiTypeButton"; //$NON-NLS-1$
    private static final String Key_utfType = "com.tida_okinawa.corona.ui.wizards.EntryWizardPage.utfTypeButton"; //$NON-NLS-1$
    private static final String Key_DataFile = "com.tida_okinawa.corona.ui.wizards.EntryWizardPage.dataFilePath"; //$NON-NLS-1$
    private static final String Key_DefineFile = "com.tida_okinawa.corona.ui.wizards.EntryWizardPage.defineFilePath"; //$NON-NLS-1$


    /**
     * 問い合わせデータファイル登録画面初期設定
     */
    private void restoreDefault() {
        section = getSection(sectionName, false);
        if (section != null) {
            boolean csvType = section.getBoolean(Key_CsvType);
            boolean xlsType = section.getBoolean(Key_xlsType);
            boolean docType = section.getBoolean(Key_DocType);
            boolean sjiType = section.getBoolean(Key_sjiType);
            boolean utfType = section.getBoolean(Key_utfType);
            if (!csvType && !xlsType && !docType) {
                csvType = true;
            }

            if (!sjiType && !utfType) {
                sjiType = true;
            }

            csvTypeButton.setSelection(csvType);
            xlsTypeButton.setSelection(xlsType);
            docTypeButton.setSelection(docType);

            sjiTypeButton.setSelection(sjiType);
            utfTypeButton.setSelection(utfType);

            if (section.getArray(Key_DataFile) != null) {
                dataFileCombo.setItems(section.getArray(Key_DataFile));
            }

            if (section.getArray(Key_DefineFile) != null) {
                defineFileCombo.setItems(section.getArray(Key_DefineFile));
            }

        } else {
            csvTypeButton.setSelection(true);
            xlsTypeButton.setSelection(false);
            docTypeButton.setSelection(false);
            sjiTypeButton.setSelection(true);
            utfTypeButton.setSelection(false);
        }
    }


    private void saveDefault() {
        section = getSection(sectionName, true);
        section.put(Key_CsvType, csvTypeButton.getSelection());
        section.put(Key_xlsType, xlsTypeButton.getSelection());
        section.put(Key_DocType, docTypeButton.getSelection());
        section.put(Key_sjiType, sjiTypeButton.getSelection());
        section.put(Key_utfType, utfTypeButton.getSelection());
        saveCombo(dataFileCombo.getText(), Key_DataFile);
        saveCombo(defineFileCombo.getText(), Key_DefineFile);
    }


    @Override
    public void setFocus() {
        dataFileCombo.setFocus();
    }
}
