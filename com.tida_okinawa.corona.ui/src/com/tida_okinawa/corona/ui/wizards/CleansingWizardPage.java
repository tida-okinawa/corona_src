/**
 * @version $Id: CleansingWizardPage.java 1840 2014-04-16 05:38:34Z yukihiro-kinjyo $
 * 
 * 2011/12/01 16:07:45
 * @author takayuki-matsumoto
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.wizards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;

import com.tida_okinawa.corona.internal.ui.component.CompositeUtil;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.ClaimWorkDataType;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.ICoronaDicPri;
import com.tida_okinawa.corona.io.model.ICoronaProduct;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IFlucDic;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.ISynonymDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.service.IDicService;
import com.tida_okinawa.corona.io.service.IIoService;
import com.tida_okinawa.corona.ui.views.CoronaDicComparator;

/**
 * @author takayuki-matsumoto
 */
public class CleansingWizardPage extends WizardPageBase {

    private List<IClaimWorkData> claimWorkDataList;
    private ClaimWorkDataType type;
    private boolean isDoPattern = false;
    private boolean isFieldSelected;
    private boolean isHitSelected;

    private CleansingViewerControl commonControl;
    private CleansingViewerControl fieldControl;
    private TableViewer fieldViewer;
    private Button commonSelect;
    private Button fieldSelect;
    private Button hitSelect;


    /**
     * コンストラクター
     * 
     * @param pageName
     *            ページ名
     * @param type
     *            これから実行する処理種別
     */
    protected CleansingWizardPage(String pageName, ClaimWorkDataType type) {
        super(pageName);
        this.type = type;

        /* [構文解析の時だけチェックボックスを出す]そのためのフラグ */
        isDoPattern = ClaimWorkDataType.RESLUT_PATTERN.equals(type);

        if (ClaimWorkDataType.DEPENDENCY_STRUCTURE.equals(this.type)) {
            setTitle("辞書選択");
            setDescription("使用する辞書を選択します。");
        } else {
            setTitle("辞書と優先度選択");
            setDescription("使用する辞書の選択と優先度を指定します。\n優先度が高いものを上から順に並べてください。");
        }
    }


    /**
     * 共通のプライオリティリストを設定
     * 
     * @param uiProduct
     *            ターゲット名
     * @param claimWorkDataType
     *            これから実行する処理種別
     * @param claimWorkDataList
     *            実行する処理のDB情報
     */
    public void initializeDicPriorityListCommmon(IUIProduct uiProduct, ClaimWorkDataType claimWorkDataType, List<IClaimWorkData> claimWorkDataList) {
        List<ICoronaDicPri> list = new ArrayList<ICoronaDicPri>();
        /* 共通のプライオリティリストを取得　 */
        IClaimWorkData cwd = claimWorkDataList.get(0);
        list.addAll(cwd.getDicPrioritysCom());

        /* #1301 リストの有無に関わらず必要なので外に出す */
        ICoronaProduct product = uiProduct.getObject();
        ICoronaProject project = IoActivator.getService().getProject(product.getProjectId());
        List<ICoronaDic> dics = new ArrayList<ICoronaDic>();

        if (list.size() == 0) {
            Set<ICoronaDic> wk = new TreeSet<ICoronaDic>(new CoronaDicComparator());

            switch (claimWorkDataType) {
            case DEPENDENCY_STRUCTURE:
                /* ターゲット　 */
                wk.addAll(product.getDictionarys(IUserDic.class));
                /* プロジェクト　 */
                wk.addAll(project.getDictionarys(IUserDic.class));
                for (ICoronaDic dic : wk) {
                    /* 　juman辞書を省き、Listに詰める　 */
                    if (!DicType.JUMAN.equals(((IUserDic) dic).getDicType())) {
                        dics.add(dic);
                    }
                }
                break;

            case CORRECTION_SYNONYM:
                /* ターゲット　 */
                wk.addAll(product.getDictionarys(ISynonymDic.class));
                wk.addAll(product.getDictionarys(IFlucDic.class));
                /* プロジェクト　 */
                wk.addAll(project.getDictionarys(ISynonymDic.class));
                wk.addAll(project.getDictionarys(IFlucDic.class));

                dics.addAll(wk);
                break;

            case RESLUT_PATTERN:
                /* ターゲット　 */
                wk.addAll(product.getDictionarys(IUserDic.class));
                wk.addAll(product.getDictionarys(IPatternDic.class));
                wk.addAll(product.getDictionarys(ILabelDic.class));
                /* プロジェクト　 */
                wk.addAll(project.getDictionarys(IUserDic.class));
                wk.addAll(project.getDictionarys(IPatternDic.class));
                wk.addAll(project.getDictionarys(ILabelDic.class));

                dics.addAll(wk);
                /* #1301 初期状態から使用頻度の高い辞書の優先度をあげる */
                Collections.sort(dics, new patternDicsComparator());
                break;
            default:
                break;
            }
            // プライオリティリストを作成する
            for (ICoronaDic dic : dics) {
                ICoronaDicPri pri = cwd.createDicPriority(dic.getId());
                pri.setInActive(true);
                list.add(pri);
            }
        }
        /* #1301 プライオリティが存在する(1回でもクレンジング実行が行われた)場合 */
        else {
            int count = 0;

            /* 今回のクレンジングで新たに追加された辞書がないかチェックする(新規に追加された辞書のプライオリティ値はマイナス値) */
            for (ICoronaDicPri pri : list) {
                if (pri.getDicPri() >= 0) {
                    break;
                }
                ICoronaDic wk = product.getDictionary(pri.getDicId());
                if (wk != null) {
                    dics.add(wk);
                } else {
                    wk = project.getDictionary(pri.getDicId());
                    if (wk != null) {
                        dics.add(wk);
                    }
                    count++;
                }
            }
            Collections.sort(dics, new patternDicsComparator());
            /* 新たに追加された辞書をソートし、既存のプライオリティリストに入れなおす */
            for (int i = 0; i < count; i++) {
                list.get(i).setDicId(dics.get(i).getId());
            }
        }
        setCommonDictPriorityList(list);
    }


    /** #1301 初期状態から使用頻度の高い辞書の優先度を上げる */
    static class patternDicsComparator implements Comparator<ICoronaDic> {

        @Override
        public int compare(ICoronaDic o1, ICoronaDic o2) {
            String name1 = o1.getName();
            String name2 = o2.getName();
            String ext1 = getExtension(name1);
            String ext2 = getExtension(name2);
            if (ext1.equals(ext2)) {
                /* 拡張子が同じ場合は辞書名で比較 */
                return name1.compareTo(name2);
            } else {
                /* パターン辞書が一番目に優先度が高い */
                if (ext2.equals("pdic")) {
                    return 1;
                }
                /* ユーザ辞書が二番目に優先度が高い */
                else if (ext2.equals("cdic") && !ext1.equals("pdic")) {
                    return 1;
                }
                /* ラベル辞書が三番目に優先度が高い */
                else if (ext2.equals("ldic") && !ext1.equals("pdic") && !ext1.equals("cdic")) {
                    return 1;
                }
                /* その他 */
                else {
                    return -1;
                }
            }
        }


        /* 拡張子を取得 */
        private static String getExtension(String str) {
            String strs[] = str.split("\\.");
            return strs[strs.length - 1];
        }
    }


    /* ****************************************
     * UI
     */
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        Composite composite = CompositeUtil.defaultComposite(parent, 1);

        if (isDoPattern) {
            /* チェックボックス */
            hitSelect = new Button(composite, SWT.CHECK);
            hitSelect.setText("1パターンの複数マッチを許可する。");
            GridData gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
            gridData.heightHint = 35;
            hitSelect.setLayoutData(gridData);
        }

        /* 共通設定エリア */
        commonSelect = new Button(composite, SWT.RADIO);
        commonSelect.setText("共通");
        commonSelect.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        commonSelect.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateEnabled();
                validatePage();
            }
        });

        commonControl = new CleansingViewerControl(composite);
        commonControl.setInput(commonDicPris);
        commonControl.setUsePriorityFunction(!ClaimWorkDataType.DEPENDENCY_STRUCTURE.equals(type));
        commonControl.addCheckStateChangedListener(checkStateChangeListener);

        /* 個別設定エリア */
        fieldSelect = new Button(composite, SWT.RADIO);
        fieldSelect.setText("フィールド毎");
        fieldSelect.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        fieldSelect.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateEnabled();
                validatePage();
            }
        });

        /* フィールド名 */
        Composite fieldGroup = CompositeUtil.defaultComposite(composite, 2);
        fieldViewer = new TableViewer(fieldGroup, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
        final Table tbl = fieldViewer.getTable();
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, false, false);
        tbl.setLayoutData(layoutData);
        tbl.setHeaderVisible(true);
        CompositeUtil.createColumn(tbl, "フィールド名", 300);

        fieldViewer.setContentProvider(new ArrayContentProvider());
        fieldViewer.setLabelProvider(new PrivateLabelProvider());
        /* IClaimWorkDataごとに、control2の値を切り替えたいので、フィールドではなくIClaimWorkDataをセットしている */
        fieldViewer.setInput(claimWorkDataList);

        // 一つ目を選択する
        fieldViewer.getTable().select(0);

        fieldControl = new CleansingViewerControl(fieldGroup);
        fieldControl.setInput(fieldPriMap.get(claimWorkDataList.get(0)));
        fieldControl.setUsePriorityFunction(!ClaimWorkDataType.DEPENDENCY_STRUCTURE.equals(type));
        fieldControl.addCheckStateChangedListener(checkStateChangeListener);

        final CleansingViewerControl finalControl = fieldControl;
        fieldViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                // フィールドごとの辞書一覧をセット
                IClaimWorkData cwd = (IClaimWorkData) ((IStructuredSelection) event.getSelection()).getFirstElement();
                List<ICoronaDicPri> priorities = getDicPriorityMap().get(cwd);
                if (priorities == null) {
                    priorities = new ArrayList<ICoronaDicPri>(0);
                }
                finalControl.setInput(priorities);
            }
        });


        setControl(composite);

        restoreDefault();
        validatePage();
    }


    @Override
    public void setFocus() {
        if (commonSelect.getSelection()) {
            commonControl.setFocus();
        } else {
            fieldViewer.getControl().setFocus();
        }
    }

    static class PrivateLabelProvider extends LabelProvider {
        @Override
        public String getText(Object element) {
            if (element instanceof IClaimWorkData) {
                int claimId = ((IClaimWorkData) element).getClaimId();
                int fieldId = ((IClaimWorkData) element).getFieldId();
                IClaimData claimData = IoActivator.getService().getClaimData(claimId);
                return claimData.getFieldInformation(fieldId).getName();
            }
            return super.getText(element);
        }
    }


    /**
     * @return フィールドごとに辞書優先度を指定しているならtrue
     */
    protected boolean isFieldSelected() {
        /* dispose後に参照されることがあるため、フィールドに取っておく */
        return isFieldSelected;
    }

    /* ****************************************
     * validate
     */
    private Listener checkStateChangeListener = new Listener() {
        @Override
        public void handleEvent(Event event) {
            validatePage();
        }
    };


    protected void validatePage() {
        switch (type) {
        case MORPHOLOGICAL:
        case DEPENDENCY_STRUCTURE:
            return;
        case CORRECTION_FLUC:
        case CORRECTION_SYNONYM: {
            boolean ok = false;
            if (isFieldSelected()) {
                for (Entry<IClaimWorkData, List<ICoronaDicPri>> e : fieldPriMap.entrySet()) {
                    ok = isValidDicSelected(e.getValue());
                    if (ok) {
                        break;
                    }
                }
            } else {
                ok = isValidDicSelected(commonDicPris);
            }
            if (!ok) {
                setErrorMessage("用語が登録してあるゆらぎ・同義語辞書をひとつ以上選択してください。");
                setPageComplete(false);
                return;
            }
        }
            break;
        case RESLUT_PATTERN: {
            /* 選択しているパターン辞書がなかったらNG */
            boolean ok = false;
            if (isFieldSelected()) {
                for (Entry<IClaimWorkData, List<ICoronaDicPri>> e : fieldPriMap.entrySet()) {
                    ok = isValidPatternDicSelected(e.getValue());
                    if (ok) {
                        break;
                    }
                }
            } else {
                ok = isValidPatternDicSelected(commonDicPris);
            }
            if (!ok) {
                setErrorMessage("パターン辞書をひとつ以上選択してください。");
                setPageComplete(false);
                return;
            }
        }
            break;
        default:
            return;
        }
        setErrorMessage(null);
        setPageComplete(true);
    }


    /**
     * @param list
     *            優先度リスト
     * @return パターン辞書をひとつ以上選択していたらtrue
     */
    private static boolean isValidPatternDicSelected(List<ICoronaDicPri> list) {
        IDicService service = IoActivator.getDicUtil();
        for (ICoronaDicPri pri : list) {
            if (!pri.isInActive()) {
                DicType type = service.getDicType(pri.getDicId());
                if (DicType.PATTERN.equals(type)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * @param list
     *            優先度リスト
     * @return 中身があるゆらぎ・同義語辞書を選択していたらtrue
     */
    private static boolean isValidDicSelected(List<ICoronaDicPri> list) {
        IDicService service = IoActivator.getDicUtil();
        IIoService ioService = IoActivator.getService();
        for (ICoronaDicPri pri : list) {
            if (!pri.isInActive()) {
                int dicId = pri.getDicId();
                DicType type = service.getDicType(pri.getDicId());
                if (DicType.FLUC.equals(type) || DicType.SYNONYM.equals(type)) {
                    ICoronaDic dic = ioService.getDictionary(dicId);
                    if (dic.getItemCount() > 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /* ****************************************
     * 辞書優先度リスト
     */
    private List<ICoronaDicPri> commonDicPris;


    /**
     * @return 共通の辞書優先度リスト
     */
    public List<ICoronaDicPri> getCommonDicPriorityList() {
        return this.commonDicPris;
    }


    /**
     * @param dicPris
     *            must not null
     */
    public void setCommonDictPriorityList(List<ICoronaDicPri> dicPris) {
        this.commonDicPris = dicPris;
    }

    private HashMap<IClaimWorkData, List<ICoronaDicPri>> fieldPriMap;


    /**
     * @return フィールドごとの辞書優先度リスト
     */
    protected HashMap<IClaimWorkData, List<ICoronaDicPri>> getDicPriorityMap() {
        return fieldPriMap;
    }


    /**
     * プライオリティリストを作成
     * 
     * @param claimWorkDataList
     *            実行する処理のDB情報
     */
    public void setClaimWorkData(List<IClaimWorkData> claimWorkDataList) {
        this.claimWorkDataList = claimWorkDataList;
        /* 　プライオリティリスト取得　 */
        fieldPriMap = new HashMap<IClaimWorkData, List<ICoronaDicPri>>();
        for (IClaimWorkData cwd : this.claimWorkDataList) {
            if (cwd.getDicPrioritys().size() == 0) {
                // プライオリティリストを作成する
                List<ICoronaDicPri> list = new ArrayList<ICoronaDicPri>();
                for (ICoronaDicPri cdp : this.commonDicPris) {
                    ICoronaDicPri pri = cwd.createDicPriority(cdp.getDicId());
                    pri.setInActive(true);
                    list.add(pri);
                }
                fieldPriMap.put(cwd, list);
            } else {
                fieldPriMap.put(cwd, cwd.getDicPrioritys());
            }
        }
    }


    void updateEnabled() {
        commonControl.setControlsEnabled(commonSelect.getSelection());
        fieldControl.setControlsEnabled(fieldSelect.getSelection());
        fieldViewer.getTable().setEnabled(fieldSelect.getSelection());
        isFieldSelected = fieldSelect.getSelection();
    }


    protected void finished() {
        saveDefault();
        if (isDoPattern) {
            isHitSelected = hitSelect.getSelection();
        }
    }

    /* ****************************************
     * ページ情報の保存
     */
    private String sectionName = "com.tida_okinawa.corona.ui.wizards.ClensingWizardPage";
    private static final String KEY_COMMON_SELECT = "com.tida_okinawa.corona.ui.wizards.ClensingWizardPage#commonSelect";
    private static final String KEY_MALTI_SELECT = "com.tida_okinawa.corona.ui.wizards.ClensingWizardPage#hitSelect";


    private void restoreDefault() {
        // default設定 共通を選択

        section = getSection(sectionName, false);
        if (section != null) {
            if (section.getBoolean(KEY_COMMON_SELECT)) {
                commonSelect.setSelection(true);
                fieldSelect.setSelection(false);
            } else {
                commonSelect.setSelection(false);
                fieldSelect.setSelection(true);
            }
            if (isDoPattern) {
                if (section.getBoolean(KEY_MALTI_SELECT)) {
                    hitSelect.setSelection(true);
                } else {
                    hitSelect.setSelection(false);
                }
            }
        }
        updateEnabled();
    }


    private void saveDefault() {
        section = getSection(sectionName, true);
        section.put(KEY_COMMON_SELECT, commonSelect.getSelection());
        if (isDoPattern) {
            section.put(KEY_MALTI_SELECT, hitSelect.getSelection());
        }
    }


    /**
     * 複数Hitを行うかどうか
     * 
     * @return 複数Hitを行うならtrue
     */
    public boolean isHitSelectStatus() {
        return isHitSelected;
    }
}
