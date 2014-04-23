/**
 * @version $Id: AutoScheduleSettingDialog.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/10/15 19:20:01
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.preference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.eclipse.ui.part.PageBook;

import com.tida_okinawa.corona.correction.CorrectionActivator;
import com.tida_okinawa.corona.correction.auto.AutoSchedule;
import com.tida_okinawa.corona.correction.auto.AutoScheduleMonthly;
import com.tida_okinawa.corona.correction.auto.AutoScheduleWeekly;

/**
 * スケジュール設定を行うダイアログ
 * 
 * @author kousuke-morishima
 */
public class AutoScheduleSettingDialog extends Dialog {
    // TODO 外部化
    private AutoSchedule schedule;
    private AutoSchedule newSchedule;


    /**
     * スケジュールを新規作成する場合のコンストラクタ
     * 
     * @param shell
     *            親シェル
     */
    AutoScheduleSettingDialog(Shell shell) {
        super(shell);
    }


    /**
     * スケジュールを修正する場合のコンストラクタ
     * 
     * @param shell
     *            親シェル
     * @param schedule
     *            修正するスケジュール
     */
    AutoScheduleSettingDialog(Shell shell, AutoSchedule schedule) {
        super(shell);
        this.schedule = schedule;
    }


    /**
     * 入力内容が正しいかチェックする
     * 
     * @return 正しければtrue, そうでなければfalse
     */
    boolean validatePage() {
        if (weeklyButton.getSelection()) {
            boolean weekSelected = false;
            for (Button item : weekItems) {
                if (item.getSelection()) {
                    weekSelected = true;
                    break;
                }
            }
            if (!weekSelected) {
                return false;
            }
        } else if (monthlyButton.getSelection()) {
            boolean monthSelected = false;
            for (Button item : monthItems) {
                if (item.getSelection()) {
                    monthSelected = true;
                    break;
                }
            }
            if (!monthSelected) {
                return false;
            }
        }
        return true;
    }


    /**
     * 入力内容をチェックして、ボタンの状態を更新する
     */
    void updateButtons() {
        getButton(OK).setEnabled(validatePage());
    }


    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        initialSelection();
        updateButtons();
        return control;
    }

    private DateTime timeBox;
    private Button weeklyButton;
    private Button monthlyButton;
    Label task;


    @Override
    protected Control createDialogArea(Composite parent) {
        Composite rootComposite = (Composite) super.createDialogArea(parent);
        rootComposite.setLayout(new GridLayout(3, false));
        rootComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createTaskArea(rootComposite);
        createScheduleArea(rootComposite);

        return parent;
    }


    private void createTaskArea(Composite parent) {
        Composite taskArea = new Composite(parent, SWT.NONE);
        taskArea.setLayout(new GridLayout(3, false));
        taskArea.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 3, 1));
        Label taskLabel = new Label(taskArea, SWT.NONE);
        taskLabel.setText("解析フロー:");
        task = new Label(taskArea, SWT.BORDER);
        task.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        Button taskBrowseButton = new Button(taskArea, SWT.PUSH);
        GridData layoutData = new GridData(SWT.NONE, SWT.NONE, false, false);
        layoutData.widthHint = 40;
        taskBrowseButton.setLayoutData(layoutData);
        taskBrowseButton.setText("...");
        taskBrowseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new PrivateLabelProvider(), new PrivateTreeContentProvider());
                dialog.setComparator(new ViewerComparator() {
                    @Override
                    public int category(Object element) {
                        /* フォルダを、ファイルより上に表示する */
                        if (element instanceof IContainer) {
                            return 0;
                        } else if (element instanceof IFile) {
                            return 1;
                        }
                        return 99;
                    }


                    @Override
                    public int compare(Viewer viewer, Object e1, Object e2) {
                        int c1 = category(e1);
                        int c2 = category(e2);
                        if (c1 == c2) {
                            return super.compare(viewer, e1, e2);
                        } else {
                            return c1 - c2;
                        }
                    }
                });
                IWorkspaceRoot input = ResourcesPlugin.getWorkspace().getRoot();
                dialog.setInput(input);
                dialog.setAllowMultiple(false);
                dialog.setBlockOnOpen(true);
                dialog.setDoubleClickSelects(true);
                if (task.getData() != null) {
                    dialog.setInitialSelection(task.getData());
                }
                dialog.setTitle("解析フロー選択");
                dialog.setMessage("解析フロー(*.bat)を選択してください");
                dialog.setValidator(new ISelectionStatusValidator() {
                    private final IStatus OK_STATUS = new Status(IStatus.OK, CorrectionActivator.PLUGIN_ID, ""); //$NON-NLS-1$


                    @Override
                    public IStatus validate(Object[] selection) {
                        if ((selection.length > 0) && (selection[0] instanceof IFile)) {
                            IFile file = (IFile) selection[0];
                            if ("bat".equalsIgnoreCase(file.getFileExtension())) {
                                return OK_STATUS;
                            }
                        }
                        return new Status(IStatus.ERROR, CorrectionActivator.PLUGIN_ID, "解析フロー(*.bat)を選択してください");
                    }
                });
                if (dialog.open() == Dialog.OK) {
                    IFile taskFile = (IFile) dialog.getFirstResult();
                    task.setData(taskFile);
                    task.setText(taskFile.getProject().getName() + "/" + taskFile.getProjectRelativePath().toString());
                }
            }
        });
    }


    private PageBook pageBook;


    private void createScheduleArea(Composite parent) {
        /* 間隔選択 */
        Composite buttonGroup = new Composite(parent, SWT.NONE);
        buttonGroup.setLayout(new GridLayout(1, false));
        buttonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        weeklyButton = new Button(buttonGroup, SWT.RADIO);
        weeklyButton.setText("週ごと(&W)");
        weeklyButton.addSelectionListener(validateListener);

        monthlyButton = new Button(buttonGroup, SWT.RADIO);
        monthlyButton.setText("月ごと(&M)");
        monthlyButton.addSelectionListener(validateListener);

        /* セパレータ */
        new Label(parent, SWT.SEPARATOR | SWT.VERTICAL).setLayoutData(new GridData(SWT.NONE, SWT.FILL, false, true));

        /* 時間設定 */
        Composite detailArea = new Composite(parent, SWT.NONE);
        detailArea.setLayout(new GridLayout(2, false));
        detailArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        new Label(detailArea, SWT.NONE).setText("実行時間(&T)");
        timeBox = new DateTime(detailArea, SWT.TIME | SWT.SHORT);

        pageBook = new PageBook(detailArea, SWT.NONE);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        layoutData.widthHint = 200;
        pageBook.setLayoutData(layoutData);
        final Control[] pages = new Control[2];
        pages[0] = createWeeklyArea(pageBook);
        pages[1] = createMonthlyArea(pageBook);

        /* ページ領域が大きいほうのサイズまでダイアログを拡張してもらうために、両方のページを表示する */
        pageBook.showPage(pages[1]);
        pageBook.showPage(pages[0]);

        weeklyButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (((Button) e.getSource()).getSelection()) {
                    updatePage(0);
                }
            }
        });
        monthlyButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (((Button) e.getSource()).getSelection()) {
                    updatePage(1);
                }
            }
        });
    }


    /**
     * 表示するページを指定する
     * 
     * @param pageNumber
     *            ページ番号
     */
    void updatePage(int pageNumber) {
        pageBook.showPage(pageBook.getChildren()[pageNumber]);
    }

    private Button[] weekItems;


    private Control createWeeklyArea(Composite parent) {
        Composite weekPage = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        weekPage.setLayout(layout);
        weekPage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Label l = new Label(weekPage, SWT.NONE);
        l.setText("実行曜日");
        GridData labelLayout = new GridData(SWT.NONE, SWT.NONE, false, false);
        labelLayout.widthHint = 50;
        l.setLayoutData(labelLayout);
        Composite weekItemArea = new Composite(weekPage, SWT.NONE);
        weekItemArea.setLayout(new GridLayout(4, true));
        weekItemArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        weekItems = new Button[7];
        weekItems[0] = createCheck(weekItemArea, "日(&S)", Calendar.SUNDAY);
        weekItems[1] = createCheck(weekItemArea, "月(&O)", Calendar.MONDAY);
        weekItems[2] = createCheck(weekItemArea, "火(&U)", Calendar.TUESDAY);
        weekItems[3] = createCheck(weekItemArea, "水(&E)", Calendar.WEDNESDAY);
        weekItems[4] = createCheck(weekItemArea, "木(&H)", Calendar.THURSDAY);
        weekItems[5] = createCheck(weekItemArea, "金(&F)", Calendar.FRIDAY);
        weekItems[6] = createCheck(weekItemArea, "土(&A)", Calendar.SATURDAY);
        return weekPage;
    }


    private Button[] monthItems;


    private Control createMonthlyArea(Composite parent) {
        Composite monthPage = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        monthPage.setLayout(layout);
        monthPage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Label l = new Label(monthPage, SWT.NONE);
        l.setText("実行日");
        GridData labelLayout = new GridData(SWT.NONE, SWT.NONE, false, false);
        labelLayout.widthHint = 50;
        l.setLayoutData(labelLayout);
        Composite monthItemArea = new Composite(monthPage, SWT.NONE);
        monthItemArea.setLayout(new GridLayout(3, true));
        monthItemArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        monthItems = new Button[3];
        monthItems[0] = createCheck(monthItemArea, "&1日", AutoScheduleMonthly.START_OF_MONTH);
        monthItems[1] = createCheck(monthItemArea, "1&5日", AutoScheduleMonthly.MIDDLE_OF_MONTH);
        monthItems[2] = createCheck(monthItemArea, "末日(&E)", AutoScheduleMonthly.END_OF_MONTH);
        return monthPage;
    }


    /**
     * 初期表示する内容を決める
     */
    void initialSelection() {
        if (schedule != null) {
            /* 解析フロー */
            IFile taskFile = schedule.getTask();
            if (taskFile != null) {
                task.setText(taskFile.getProject().getName() + "/" + taskFile.getProjectRelativePath().toString());
                task.setData(taskFile);
            }
            /* 時間 */
            Calendar time = schedule.getNextRunDate();
            timeBox.setHours(time.get(Calendar.HOUR_OF_DAY));
            timeBox.setMinutes(time.get(Calendar.MINUTE));
            /* 実行日 */
            if (schedule instanceof AutoScheduleWeekly) {
                updatePage(0);

                weeklyButton.setSelection(true);
                monthlyButton.setSelection(false);
                for (Integer weekday : ((AutoScheduleWeekly) schedule).getRunWeeks()) {
                    weekItems[weekday - 1].setSelection(true);
                }
            } else if (schedule instanceof AutoScheduleMonthly) {
                updatePage(1);

                weeklyButton.setSelection(false);
                monthlyButton.setSelection(true);
                for (Integer day : ((AutoScheduleMonthly) schedule).getRunDays()) {
                    for (Button item : monthItems) {
                        if (day.equals(item.getData())) {
                            item.setSelection(true);
                            break;
                        }
                    }
                }
            }
        } else {
            Calendar now = Calendar.getInstance(Locale.JAPAN);
            int hours = now.get(Calendar.HOUR_OF_DAY);
            int minutes = (now.get(Calendar.MINUTE) / 15 + 1) * 15;
            if (minutes == 60) {
                minutes = 0;
                hours += 1;
                if (hours == 24) {
                    hours = 0;
                }
            }
            timeBox.setHours(hours);
            timeBox.setMinutes(minutes);
            weeklyButton.setSelection(true);
            updatePage(0);
        }
    }


    @Override
    protected void okPressed() {
        if (schedule == null) {
            if (weeklyButton.getSelection()) {
                newSchedule = new AutoScheduleWeekly(getWeeks());
            } else if (monthlyButton.getSelection()) {
                newSchedule = new AutoScheduleMonthly(getDays());
            } else {
                return;
            }
        } else {
            /* 編集 */
            if (weeklyButton.getSelection()) {
                if (schedule instanceof AutoScheduleWeekly) {
                    ((AutoScheduleWeekly) schedule).setRunWeeks(getWeeks());
                    newSchedule = schedule;
                } else {
                    newSchedule = new AutoScheduleWeekly(getWeeks());
                }
            } else if (monthlyButton.getSelection()) {
                if (schedule instanceof AutoScheduleMonthly) {
                    ((AutoScheduleMonthly) schedule).setRunDays(getDays());
                    newSchedule = schedule;
                } else {
                    newSchedule = new AutoScheduleMonthly(getDays());
                }
            }
        }
        /* 共通の値設定 */
        newSchedule.setTask((IFile) task.getData());
        newSchedule.setTime(timeBox.getHours(), timeBox.getMinutes());
        super.okPressed();
    }


    private List<Integer> getWeeks() {
        /* ボタンの数をinitialCapacityに */
        List<Integer> weeks = new ArrayList<Integer>(7);
        for (Button item : weekItems) {
            if (item.getSelection()) {
                weeks.add((Integer) item.getData());
            }
        }
        return weeks;
    }


    private List<Integer> getDays() {
        /* ボタンの数をinitialCapacityに */
        List<Integer> days = new ArrayList<Integer>(3);
        for (Button item : monthItems) {
            if (item.getSelection()) {
                days.add((Integer) item.getData());
            }
        }
        return days;
    }


    @Override
    protected void cancelPressed() {
        newSchedule = null;
        super.cancelPressed();
    }


    /**
     * 設定されたスケジュール。キャンセルが押された場合や、まだダイアログが閉じていないときはnull
     * 
     * @return 設定されたスケジュール
     */
    public AutoSchedule getObject() {
        return newSchedule;
    }


    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("スケジュール設定");
    };


    @Override
    protected Point getInitialSize() {
        return new Point(430, 240);
    }

    private final SelectionAdapter validateListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            updateButtons();
        };
    };


    private Button createCheck(Composite parent, String label, Object data) {
        Button check = new Button(parent, SWT.CHECK);
        check.setText(label);
        check.setData(data);
        check.addSelectionListener(validateListener);
        return check;
    }


    /**
     * 設定済みスケジュール一覧のラベルプロバイダ
     * 
     * @author kousuke-morishima
     */
    private static class PrivateLabelProvider extends LabelProvider {
        public PrivateLabelProvider() {
        }


        @Override
        public String getText(Object element) {
            if (element instanceof IResource) {
                return ((IResource) element).getName();
            }
            return super.getText(element);
        }


        @Override
        public Image getImage(Object element) {
            if (element instanceof IProject) {
                return PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJ_PROJECT);
            } else if (element instanceof IContainer) {
                return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
            } else if (element instanceof IFile) {
                if ("bat".equalsIgnoreCase(((IFile) element).getFileExtension())) {
                    return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
                }
            }
            return null;
        }
    }

    private static class PrivateTreeContentProvider implements ITreeContentProvider {
        public PrivateTreeContentProvider() {
        }


        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }


        @Override
        public void dispose() {
        }


        @Override
        public boolean hasChildren(Object element) {
            return (element instanceof IContainer);
        }


        @Override
        public Object getParent(Object element) {
            if (element instanceof IResource) {
                return ((IResource) element).getParent();
            }
            return null;
        }


        @Override
        public Object[] getElements(Object input) {
            if (input instanceof IWorkspaceRoot) {
                IProject[] projects = ((IWorkspaceRoot) input).getProjects();
                int dst = 0;
                for (int i = 0; i < projects.length; i++) {
                    if (projects[i].isOpen()) {
                        projects[dst++] = projects[i];
                    }
                }
                if (dst == projects.length) {
                    return projects;
                }
                IProject[] ret = new IProject[dst];
                System.arraycopy(projects, 0, ret, 0, dst);
                return ret;
            }
            return new Object[0];
        }


        @Override
        public Object[] getChildren(Object parent) {
            if (parent instanceof IContainer) {
                try {
                    IResource[] members = ((IContainer) parent).members();
                    int dst = 0;
                    for (int i = 0; i < members.length; i++) {
                        if (!members[i].getName().startsWith(".")) {
                            members[dst++] = members[i];
                        }
                    }
                    if (dst == members.length) {
                        return members;
                    }
                    IResource[] ret = new IResource[dst];
                    System.arraycopy(members, 0, ret, 0, dst);
                    return ret;
                } catch (CoreException e) {
                    e.printStackTrace();
                }
            }
            return new Object[0];
        }
    }


    @Override
    protected boolean isResizable() {
        return true;
    }
}
