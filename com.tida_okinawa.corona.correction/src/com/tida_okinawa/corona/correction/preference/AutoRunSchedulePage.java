/**
 * @version $Id: AutoRunSchedulePage.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 *
 * 2012/10/15 17:37:21
 * @author kousuke-morishima
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.preference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.ibm.icu.text.SimpleDateFormat;
import com.tida_okinawa.corona.correction.auto.AutoSchedule;
import com.tida_okinawa.corona.correction.auto.AutoScheduleMonthly;
import com.tida_okinawa.corona.correction.auto.AutoScheduleWeekly;
import com.tida_okinawa.corona.correction.auto.AutoScheduler;
import com.tida_okinawa.corona.correction.auto.Duration;

/**
 * 自動実行スケジュールを設定するページ.
 * 
 * @author kousuke-morishima
 */
public class AutoRunSchedulePage extends PreferencePage implements IWorkbenchPreferencePage {

    /**
     * コンストラクタ
     */
    public AutoRunSchedulePage() {
        super(Messages.AutoRunSchedulePage_PageTitle, null);
    }


    AutoScheduler scheduler = AutoScheduler.Instance;

    CheckboxTableViewer viewer;
    private Button editButton;
    private Button removeButton;


    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        /* 設定済みスケジュール一覧 */
        final Table t = new Table(composite, SWT.CHECK | SWT.SINGLE | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
        t.setLinesVisible(true);
        t.setHeaderVisible(true);
        t.setLayout(new GridLayout());
        t.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createTableColumn(t, Messages.AutoRunSchedulePage_ColumnTitle_Run, 40);
        final TableColumn taskColumn = createTableColumn(t, Messages.AutoRunSchedulePage_ColumnTitle_Task, 140);
        taskColumn.addSelectionListener(new SelectionAdapter() {
            private boolean asc = false;


            @Override
            public void widgetSelected(SelectionEvent e) {
                asc = !asc;
                t.setSortColumn(taskColumn);
                t.setSortDirection((asc) ? SWT.UP : SWT.DOWN);
                viewer.setSorter(new TaskSorter(asc));
            }
        });
        final TableColumn dateColumn = createTableColumn(t, Messages.AutoRunSchedulePage_ColumnTitle_NextRun, 140);
        dateColumn.addSelectionListener(new SelectionAdapter() {
            private boolean asc = true;


            @Override
            public void widgetSelected(SelectionEvent e) {
                asc = !asc;
                t.setSortColumn(dateColumn);
                t.setSortDirection((asc) ? SWT.UP : SWT.DOWN);
                viewer.setSorter(new DateSorter(asc));
            }
        });
        createTableColumn(t, Messages.AutoRunSchedulePage_ColumnTitle_Duration, 110);

        viewer = new CheckboxTableViewer(t);
        viewer.setContentProvider(ArrayContentProvider.getInstance());
        viewer.setLabelProvider(new TableLabelProvider());

        /* 新規、編集、削除ボタン */
        Composite buttonGroup = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout();
        buttonGroup.setLayout(layout);
        GridData gd = new GridData();
        gd.widthHint = 150;
        gd.verticalAlignment = SWT.TOP;
        buttonGroup.setLayoutData(gd);
        createButton(buttonGroup, Messages.AutoRunSchedulePage_Label_New_N, new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AutoScheduleSettingDialog dialog = new AutoScheduleSettingDialog(getShell());
                if (dialog.open() == Dialog.OK) {
                    AutoSchedule newSchedule = dialog.getObject();
                    viewer.add(newSchedule);
                    addSchedule(newSchedule);
                    viewer.setChecked(newSchedule, newSchedule.isDoRun());
                    viewer.setSelection(new StructuredSelection(newSchedule), true);
                }
            }
        });
        editButton = createButton(buttonGroup, Messages.AutoRunSchedulePage_Label_Edit_E, new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Object element = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
                if (element instanceof AutoSchedule) {
                    AutoSchedule autoSchedule = ((AutoSchedule) element).clone();
                    AutoScheduleSettingDialog dialog = new AutoScheduleSettingDialog(getShell(), autoSchedule);
                    if (dialog.open() == Dialog.OK) {
                        boolean reveal = false;
                        AutoSchedule newSchedule = dialog.getObject();
                        if (!element.equals(newSchedule)) {
                            reveal = true;
                            modifySchedule(newSchedule, (AutoSchedule) element);
                            viewer.remove(element);
                            viewer.add(newSchedule);
                            viewer.setChecked(newSchedule, newSchedule.isDoRun());
                            /* 再選択のために移し替え */
                            element = newSchedule;
                        }
                        viewer.update(element, null);
                        viewer.setSelection(new StructuredSelection(element), reveal);
                    }
                }
            }
        });
        removeButton = createButton(buttonGroup, Messages.AutoRunSchedulePage_Label_Delete_D, new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Object element = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
                if (element instanceof AutoSchedule) {
                    /* 削除位置の取得 */
                    Table t = viewer.getTable();
                    int selectionIndex = t.getSelectionIndex();
                    viewer.remove(element);
                    removeSchedule((AutoSchedule) element);
                    TableItem[] items = t.getItems();
                    if (items.length > 0) {
                        /* 削除したひとつ下を選択する */
                        IStructuredSelection newSS;
                        if (items.length <= selectionIndex) {
                            newSS = new StructuredSelection(items[items.length - 1].getData());
                        } else {
                            newSS = new StructuredSelection(items[selectionIndex].getData());
                        }
                        viewer.setSelection(newSS);
                    }
                }
            }
        });

        viewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                // Memo 内部staticクラス化可能
                if (event.getElement() instanceof AutoSchedule) {
                    AutoSchedule schedule = (AutoSchedule) event.getElement();
                    schedule.updateNextRunDate();
                    viewer.refresh(schedule);
                    schedule.setDoRun(event.getChecked());
                }
            }
        });

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateButtons();
            }
        });

        /* ソートの初期状態を設定 */
        t.setSortColumn(dateColumn);
        t.setSortDirection(SWT.UP);
        viewer.setSorter(new DateSorter(true));

        restoreDefault();
        return composite;
    }

    List<AutoSchedule> schedules;


    private void restoreDefault() {
        /* データをセット */
        schedules = scheduler.getSchedules();
        viewer.setInput(schedules);

        /* 実行するかしないかをセット */
        for (AutoSchedule schedule : schedules) {
            viewer.setChecked(schedule, schedule.isDoRun());
        }
    }


    private static TableColumn createTableColumn(Table parent, String text, int width) {
        TableColumn col = new TableColumn(parent, SWT.NONE);
        col.setText(text);
        col.setWidth(width);
        return col;
    }


    private static Button createButton(Composite parent, String text, SelectionListener l) {
        Button b = new Button(parent, SWT.PUSH);
        b.setText(text);
        b.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        b.addSelectionListener(l);
        return b;
    }


    /** 新規作成されたスケジュール。PreferenceDialogがOKされるまでは確定されない */
    private List<AutoSchedule> addedSchedules = new ArrayList<AutoSchedule>();


    void addSchedule(AutoSchedule schedule) {
        addedSchedules.add(schedule);
        schedules.add(schedule);
    }

    /** 変更されたスケジュール。PreferenceDialogがOKされるまでは確定されない */
    private Map<AutoSchedule, AutoSchedule> modifiedSchedules = new HashMap<AutoSchedule, AutoSchedule>();


    void modifySchedule(AutoSchedule newSchedule, AutoSchedule oldSchedule) {
        if (addedSchedules.remove(oldSchedule)) {
            /* 新規追加されたスケジュールを変更したなら、再度追加し直せばいい */
            addedSchedules.add(newSchedule);
        } else {
            AutoSchedule originalSchedule = modifiedSchedules.get(oldSchedule);
            if (originalSchedule != null) {
                /* 前回変更されば分のスケジュールは破棄して構わない */
                modifiedSchedules.remove(oldSchedule);
                modifiedSchedules.put(newSchedule, originalSchedule);
            } else {
                modifiedSchedules.put(newSchedule, oldSchedule);
            }
        }
        schedules.remove(oldSchedule);
        schedules.add(newSchedule);
    }

    /** 削除されたスケジュール。PreferenceDialogがOKされるまでは確定されない */
    private List<AutoSchedule> removedSchedules = new ArrayList<AutoSchedule>();


    void removeSchedule(AutoSchedule schedule) {
        if (addedSchedules.remove(schedule)) {
            /* 新規追加分が消されただけなので、無視 */
        } else {
            AutoSchedule oldSchedule = modifiedSchedules.remove(schedule);
            if (oldSchedule != null) {
                /* 変更されてかつ消される */
                removedSchedules.add(oldSchedule);
            } else {
                /* 未変更で消される */
                removedSchedules.add(schedule);
            }
        }
        schedules.remove(schedule);
    }


    @Override
    public boolean performOk() {
        for (AutoSchedule schedule : addedSchedules) {
            scheduler.add(schedule);
        }
        for (Entry<AutoSchedule, AutoSchedule> e : modifiedSchedules.entrySet()) {
            scheduler.replace(e.getKey(), e.getValue());
        }
        for (AutoSchedule schedule : removedSchedules) {
            scheduler.remove(schedule);
        }
        addedSchedules.clear();
        removedSchedules.clear();
        modifiedSchedules.clear();

        saveDefault();
        return super.performOk();
    }


    private void saveDefault() {
        scheduler.save();
    }


    @Override
    public void init(IWorkbench workbench) {
    }


    @Override
    protected void performDefaults() {
        /* 現在設定されているスケジュールをすべて削除する */
        if (MessageDialog.openConfirm(getShell(), Messages.AutoRunSchedulePage_DialogTitle_ConfirmRestore,
                Messages.AutoRunSchedulePage_DialogMessage_ConfirmRestore)) {
            viewer.setInput(null);
            for (AutoSchedule schedule : scheduler.getSchedules()) {
                removeSchedule(schedule);
            }
            addedSchedules.clear();
            modifiedSchedules.clear();
        }
        super.performDefaults();
    }


    @Override
    public void setVisible(boolean visible) {
        Table t = viewer.getTable();
        if (t.getItemCount() > 0) {
            t.select(0);
        }
        updateButtons();
        super.setVisible(visible);
    }


    /** 設定済みスケジュールを選択しているかどうかで、「新規」「編集」「削除」ボタンの状態を変える */
    void updateButtons() {
        IStructuredSelection ss = (IStructuredSelection) viewer.getSelection();
        boolean editable = (ss.getFirstElement() != null);
        editButton.setEnabled(editable);
        removeButton.setEnabled(editable);
    }


    private static class DateSorter extends ViewerSorter {
        private boolean asc;


        public DateSorter(boolean asc) {
            this.asc = asc;
        }


        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            AutoSchedule s1 = (AutoSchedule) e1;
            AutoSchedule s2 = (AutoSchedule) e2;
            if (s1.getNextRunDate().before(s2.getNextRunDate())) {
                return (asc) ? -1 : 1;
            } else if (s1.getNextRunDate().equals(s2.getNextRunDate())) {
                return 0;
            } else {
                return (asc) ? 1 : -1;
            }
        }
    }

    /**
     * タスク名でソートする
     * 
     * @author kousuke-morishima
     */
    private static class TaskSorter extends ViewerSorter {
        private boolean asc;


        public TaskSorter(boolean asc) {
            this.asc = asc;
        }


        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            AutoSchedule s1 = (AutoSchedule) e1;
            AutoSchedule s2 = (AutoSchedule) e2;
            IFile f1 = s1.getTask();
            IFile f2 = s2.getTask();
            if (f1 == null) {
                return (asc) ? 1 : -1;
            }
            if (f2 == null) {
                return (asc) ? -1 : 1;
            }
            int ret = f1.getName().compareTo(f2.getName());
            return (asc) ? ret : (ret * -1);
        }
    }

    private static class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
        private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); //$NON-NLS-1$


        public TableLabelProvider() {
        }


        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }


        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (!(element instanceof AutoSchedule)) {
                return super.getText(element);
            }

            switch (columnIndex) {
            case 0: /* 実行フラグ */
                return ""; //$NON-NLS-1$
            case 1: /* タスク名 */
                IFile task = ((AutoSchedule) element).getTask();
                if (task != null) {
                    if (task.exists()) {
                        StringBuilder path = new StringBuilder(64);
                        path.append(task.getName()).append(" ("); //$NON-NLS-1$
                        IPath fullPath = task.getFullPath();
                        for (int i = 0; i < fullPath.segmentCount() - 1; i++) {
                            String segment = fullPath.segment(i);
                            path.append(segment).append("/"); //$NON-NLS-1$
                        }
                        path.deleteCharAt(path.length() - 1).append(")"); //$NON-NLS-1$
                        return path.toString();
                    } else {
                        return Messages.AutoRunSchedulePage_Label_NoFile;
                    }
                } else {
                    return Messages.AutoRunSchedulePage_Label_NoTask;
                }
            case 2: /* 次回実行日時 */
                Calendar c = ((AutoSchedule) element).getNextRunDate();
                return dateFormat.format(c.getTime());
            case 3: /* 実行間隔 */
                StringBuilder ret = new StringBuilder(32);
                if (element instanceof AutoScheduleWeekly) {
                    List<Integer> weekdays = ((AutoScheduleWeekly) element).getRunWeeks();
                    ret.append(Duration.WEEKLY.getValue());
                    if (!weekdays.isEmpty()) {
                        ret.append("("); //$NON-NLS-1$
                        for (Integer weekday : weekdays) {
                            switch (weekday) {
                            case Calendar.SUNDAY:
                                ret.append(Messages.AutoRunSchedulePage_Label_Sunday).append(","); //$NON-NLS-1$
                                break;
                            case Calendar.MONDAY:
                                ret.append(Messages.AutoRunSchedulePage_Label_Monday).append(","); //$NON-NLS-1$
                                break;
                            case Calendar.TUESDAY:
                                ret.append(Messages.AutoRunSchedulePage_Label_Tuesday).append(","); //$NON-NLS-1$
                                break;
                            case Calendar.WEDNESDAY:
                                ret.append(Messages.AutoRunSchedulePage_Label_Wednesday).append(","); //$NON-NLS-1$
                                break;
                            case Calendar.THURSDAY:
                                ret.append(Messages.AutoRunSchedulePage_Label_Thursday).append(","); //$NON-NLS-1$
                                break;
                            case Calendar.FRIDAY:
                                ret.append(Messages.AutoRunSchedulePage_Label_Friday).append(","); //$NON-NLS-1$
                                break;
                            case Calendar.SATURDAY:
                                ret.append(Messages.AutoRunSchedulePage_Label_Saturday).append(","); //$NON-NLS-1$
                                break;
                            default:
                                break;
                            }
                        }
                        ret.deleteCharAt(ret.length() - 1);
                        ret.append(")"); //$NON-NLS-1$
                    }
                } else if (element instanceof AutoScheduleMonthly) {
                    List<Integer> days = ((AutoScheduleMonthly) element).getRunDays();
                    ret.append(Duration.MONTHLY.getValue());
                    if (!days.isEmpty()) {
                        ret.append("("); //$NON-NLS-1$
                        for (Integer day : days) {
                            switch (day) {
                            case AutoScheduleMonthly.START_OF_MONTH:
                                ret.append("1,"); //$NON-NLS-1$
                                break;
                            case AutoScheduleMonthly.MIDDLE_OF_MONTH:
                                ret.append("15,"); //$NON-NLS-1$
                                break;
                            case AutoScheduleMonthly.END_OF_MONTH:
                                ret.append(Messages.AutoRunSchedulePage_Label_EndOfMonth).append(","); //$NON-NLS-1$
                                break;
                            default:
                                break;
                            }
                        }
                        ret.deleteCharAt(ret.length() - 1);
                        ret.append(")"); //$NON-NLS-1$
                    }
                }
                return ret.toString();
            default:
                return ""; //$NON-NLS-1$
            }
        }
    }
}
