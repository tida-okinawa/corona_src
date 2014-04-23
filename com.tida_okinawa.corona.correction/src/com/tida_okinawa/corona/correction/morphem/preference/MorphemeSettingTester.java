package com.tida_okinawa.corona.correction.morphem.preference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.tida_okinawa.corona.correction.CorrectionActivator;
import com.tida_okinawa.corona.correction.common.ExternalProgramExec;

/**
 * 形態素・係り受け解析のチェック
 * 
 * @author imai
 * 
 */
public class MorphemeSettingTester {

    static void test() {
        Dialog dialog = new TestDialog(new Shell(CorrectionActivator.getDefault().getWorkbench().getDisplay()));
        dialog.open();
    }

    static class TestDialog extends Dialog {
        TestDialog(Shell parent) {
            super(parent);
        }


        @Override
        protected void configureShell(Shell newShell) {
            newShell.setText("接続テスト");
            newShell.setSize(800, 600);
            super.configureShell(newShell);
        }


        @Override
        protected boolean isResizable() {
            return true;
        }


        @Override
        protected Control createDialogArea(Composite parent) {
            Composite composite = (Composite) super.createDialogArea(parent);

            composite.setLayout(new GridLayout());

            // テスト文字列
            final Text testText = new Text(composite, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
            testText.setText("日本語を解析する");
            GridData testTextGd = new GridData();
            testTextGd.heightHint = 80;
            testTextGd.horizontalAlignment = SWT.FILL;
            testTextGd.grabExcessHorizontalSpace = true;
            testText.setLayoutData(testTextGd);

            // 結果
            final Text resultText = new Text(composite, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
            GridData resultTextGd = new GridData();
            resultTextGd.verticalAlignment = SWT.FILL;
            resultTextGd.horizontalAlignment = SWT.FILL;
            resultTextGd.grabExcessHorizontalSpace = true;
            resultTextGd.grabExcessVerticalSpace = true;

            resultText.setLayoutData(resultTextGd);

            final Button testJumanBtn = new Button(parent, SWT.NONE);
            testJumanBtn.setText("Jumanテスト");
            testJumanBtn.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    String text = testText.getText().trim();
                    if (text.isEmpty()) {
                        return; // 空白だとJumanが固まる
                    }
                    text += "\n";

                    StringBuilder sb = new StringBuilder();
                    int numOfServers = MorphemePreference.getJumanServerNumber();
                    if (numOfServers <= 0) {
                        // スタンドアローン
                        String[] cmd = MorphemePreference.getJumanCmdLine(0);
                        sb.append(test(cmd, text, MorphemePreference.getJumanIniDir()));
                    } else {
                        // サーバーモード
                        for (int serverId = 0; serverId < numOfServers; serverId++) {
                            String[] cmd = MorphemePreference.getJumanCmdLine(serverId);
                            sb.append("###  server ").append(serverId).append(": ").append(concat(cmd)).append("\n");
                            sb.append(test(cmd, text, MorphemePreference.getJumanIniDir()));

                        }
                    }

                    resultText.setText(sb.toString());
                }


                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                }
            });

            final Button testKnpBtn = new Button(parent, SWT.NONE);
            testKnpBtn.setText("Knpテスト");
            testKnpBtn.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    String text = testText.getText().trim();
                    if (text.isEmpty()) {
                        return; // 空白だとJumanが固まる
                    }
                    text += "\n";

                    StringBuilder sb = new StringBuilder();
                    int numOfServers = MorphemePreference.getKnpServerNumber();
                    if (numOfServers <= 0) {
                        // スタンドアローン
                        String[] cmd1 = MorphemePreference.getJumanCmdLine(0);
                        String text2 = test(cmd1, text, MorphemePreference.getJumanIniDir());

                        String[] cmd2 = MorphemePreference.getKnpCmdLine(0);
                        sb.append(test(cmd2, text2, MorphemePreference.getKnpIniDir()));
                    } else {
                        for (int serverId = 0; serverId < numOfServers; serverId++) {
                            // juman
                            String[] cmd1 = MorphemePreference.getJumanCmdLine(serverId);
                            String text2 = test(cmd1, text, MorphemePreference.getJumanIniDir());

                            String[] cmd2 = MorphemePreference.getKnpCmdLine(serverId);
                            sb.append("###  server ").append(serverId).append(": ").append(concat(cmd2)).append("\n");
                            sb.append(test(cmd2, text2, MorphemePreference.getKnpIniDir()));
                        }
                    }

                    resultText.setText(sb.toString());
                }


                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                }
            });


            return composite;
        }


        /**
         * 
         * @param cmd
         * @param text
         */
        private static String test(String[] cmd, String text, File dir) {
            try {
                byte[] bytes = text.getBytes();
                if (MorphemePreference.convSJIS()) {
                    bytes = text.getBytes("MS932");
                }

                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ByteArrayOutputStream err = new ByteArrayOutputStream();

                ExternalProgramExec pep = new ExternalProgramExec();
                pep.exec(cmd, dir, in, out, err);
                if (MorphemePreference.convSJIS()) {
                    return new String(out.toByteArray(), "MS932");
                } else {
                    return new String(out.toByteArray());
                }
            } catch (Exception e) {
                return e.getMessage();
            }
        }


        String concat(String[] ary) {
            StringBuilder sb = new StringBuilder();
            for (String a : ary) {
                sb.append(a);
                sb.append(" ");
            }
            return sb.toString();
        }
    }
}
