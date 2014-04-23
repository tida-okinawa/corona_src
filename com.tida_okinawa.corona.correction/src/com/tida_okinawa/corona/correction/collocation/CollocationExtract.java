/**
 * @version $Id: CollocationExtract.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/11/05 11:00:58
 * @author wataru-higa
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.collocation;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.tida_okinawa.corona.common.Encoding;
import com.tida_okinawa.corona.correction.common.ExternalProgramExec;
import com.tida_okinawa.corona.correction.common.ExternalProgramExitException;
import com.tida_okinawa.corona.correction.external.ExternalActivator;
import com.tida_okinawa.corona.correction.morphem.MorphemeRelationProcessor;
import com.tida_okinawa.corona.correction.morphem.preference.MorphemePreference;
import com.tida_okinawa.corona.io.model.dic.TermCForm;

/**
 * @author wataru-higa
 */
public class CollocationExtract {
    final static int ERROR_STATE_EXCEPTION = 0;
    final static int ERROR_STATE_EMPTY = 1;
    final static int JUMAN_ELEMENT_MIDASHI = 0;
    final static int JUMAN_ELEMENT_PARTOFSPEECH = 3;
    final static int JUMAN_ELEMENT_PARTOFSPEECH_DETAIL = 5;
    final static int JUMAN_ELEMENT_CONJUGATE = 9;
    int errorState;
    Set<String> undefWordList = new HashSet<String>();
    List<String> extractResult = new ArrayList<String>();


    /**
     * 誤記補正結果より連語の抽出処理を行う
     * 
     * @param misstakesList
     *            誤記補正結果レコードリスト
     * @param lowestHitNum
     *            最低ヒット回数
     * @return TMT処理結果 List<String>
     * @throws InterruptedException
     *             例外
     * @throws InvocationTargetException
     *             例外
     */
    public List<String> exec(final List<String> misstakesList, final String lowestHitNum) throws InvocationTargetException, InterruptedException {
        errorState = -1;

        // プリファレンスの「形態素・係り受け」よりShift-JISへの用変換フラグを取得
        final boolean convSJIS = MorphemePreference.convSJIS();
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        final Shell shell = window.getShell();
        ProgressMonitorDialog dialog1 = new ProgressMonitorDialog(shell);
        dialog1.run(true, false, new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

                // TMT実行コマンドを取得
                monitor.beginTask(Messages.COLLOCATION_PROGRES_BEGINTASK_COLLOCATION, 3);
                monitor.subTask(Messages.COLLOCATION_PROGRESS_SUBTASK_TMT);
                String[] tmtCmd = getTmtCmdLine(lowestHitNum);

                // すべてのレコードを1つにまとめる
                final StringBuffer records = new StringBuffer(100);
                for (String record : misstakesList) {
                    records.append(record + "#EOB"); //$NON-NLS-1$
                }

                // TMTを実行する
                List<String> tmtResultList = tmtExec(records, tmtCmd);
                if ((errorState == -1) && (tmtResultList.size() == 0)) {
                    errorState = ERROR_STATE_EMPTY;
                }

                if (errorState == -1) {
                    StringBuffer tmtResults = new StringBuffer(100);
                    for (String tmtResult : tmtResultList) {
                        String[] rengo = tmtResult.split(","); //$NON-NLS-1$
                        tmtResults.append(rengo[1] + "\n"); //$NON-NLS-1$
                    }
                    monitor.worked(1);

                    // TMT結果よりJumanを実行
                    monitor.subTask(Messages.COLLOCATION_PROGRESS_SUBTASK_JUMAN);
                    MorphemeRelationProcessor morphRelationProcesser = new MorphemeRelationProcessor(0); // TODO: 暫定
                    List<String> jumanResultList = jumanExec(morphRelationProcesser, tmtResults, convSJIS);
                    monitor.worked(1);

                    monitor.subTask(Messages.COLLOCATION_PROGRESS_SUBTASK_COLLOCATION);
                    int tmtResultListIndex = 0;
                    List<String> rengoTermList = new ArrayList<String>();
                    for (String jumanResult : jumanResultList) {
                        if (jumanResult != null) {
                            // 動詞、数詞、日付等の連語を削除する
                            String rengo = deleteDisusedCollocation(jumanResult);
                            if (rengo != null) {
                                String hitNum = tmtResultList.get(tmtResultListIndex).substring(0, tmtResultList.get(tmtResultListIndex).indexOf(",")); //$NON-NLS-1$
                                /*
                                 * 抽出結果をFrequentRecordの型に合わせる
                                 * 型：表記 表記 品詞 品詞詳細 TermPart(名詞) ヒット回数
                                 */
                                String term = rengo + " " + rengo + " 名詞 普通名詞 " + TermCForm.NONE + " #" + hitNum; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                rengoTermList.add(term);
                            }
                            tmtResultListIndex++;
                        }
                    }
                    if (rengoTermList.size() == 0) {
                        errorState = ERROR_STATE_EMPTY;
                    }
                    extractResult = countUndefinedWord(rengoTermList);
                    monitor.worked(1);
                    monitor.done();
                }
            }
        });

        if (errorState != -1) {
            if (errorState == ERROR_STATE_EXCEPTION) {
                String title = Messages.COLLOCATION_MESSAGE_BOX_EXCEPTION_ERROR_TITLE;
                String text = Messages.COLLOCATION_MESSAGE_BOX_EXCEPTION_ERROR_TEXT;
                dispMessage(shell, title, text);
            } else if (errorState == ERROR_STATE_EMPTY) {
                String title = Messages.COLLOCATION_MESSAGE_BOX_EMPTY_ERROR_TITLE;
                String text = Messages.COLLOCATION_MESSAGE_BOX_EMPTY_ERROR_TEXT_1;
                dispMessage(shell, title, text);
            }
        }

        return extractResult;
    }


    /**
     * TMT実行コマンドを取得するメソッド
     * 
     * @param hitNum
     *            プリファレンスの最低ヒット回数
     * @return TMTコマンドを返却String[]、格納先がない場合null
     */
    private static String[] getTmtCmdLine(String hitNum) {
        Plugin plugin = ExternalActivator.getDefault();
        if (plugin == null) {
            return new String[] {};
        }
        String filePath = null;
        try {
            // Coronaツール格納先を取得
            URL url = FileLocator.toFileURL(plugin.getBundle().getEntry("")); //$NON-NLS-1$
            File file = new File(url.toURI());
            filePath = file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        // TMTのexeをファイルパスへ追加
        String tmtPaht = filePath + Messages.COLLOCATION_TMT_EXECUTE_COMMAND;
        String tmtKeyPaht = filePath + Messages.COLLOCATION_TMT_EXECUTE_KEY_FILE;
        // 外部コマンドを作成
        String[] tmtCmd = new String[] { tmtPaht, Messages.COLLOCATION_TMT_EXECUTE_OPTION_1, Messages.COLLOCATION_TMT_EXECUTE_OPTION_2 + hitNum,
                Messages.COLLOCATION_TMT_EXECUTE_OPTION_3 + tmtKeyPaht };
        return tmtCmd;

    }


    /*
     * TMT実行処理
     */
    private List<String> tmtExec(StringBuffer records, String[] tmtCmd) {
        List<String> tmtResultList = new ArrayList<String>();
        ExternalProgramExec externalProgramExec = new ExternalProgramExec();
        byte[] inputBytes;
        try {
            inputBytes = records.toString().getBytes(Encoding.MS932.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return tmtResultList;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayInputStream input = new ByteArrayInputStream(inputBytes);
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        BufferedReader br = null;
        try {
            externalProgramExec.exec(tmtCmd, null, input, output, err);
            br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(output.toByteArray()), Encoding.MS932.toString()));

            String tmtResult;
            while ((tmtResult = br.readLine()) != null) {
                tmtResultList.add(tmtResult);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            errorState = ERROR_STATE_EXCEPTION;
        } catch (InterruptedException | ExternalProgramExitException e) {
            e.printStackTrace();
        } finally {
            try {
                output.close();
                input.close();
                err.close();
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tmtResultList;
    }


    /*
     * Juman実行処理
     */
    private static List<String> jumanExec(MorphemeRelationProcessor morphRelationProcesser, StringBuffer tmtResults, boolean convSJIS) {
        byte[] inputBytes;
        List<String> jumanResultList = new ArrayList<String>();
        if (convSJIS) {
            try {
                inputBytes = tmtResults.toString().getBytes(Encoding.MS932.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return jumanResultList;
            }
        } else {
            inputBytes = tmtResults.toString().getBytes();
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayInputStream input = new ByteArrayInputStream(inputBytes);
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        BufferedReader br = null;
        try {
            morphRelationProcesser.exec(input, output, err, false);
            if (convSJIS) {
                br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(output.toByteArray()), Encoding.MS932.toString()));
            } else {
                br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(output.toByteArray())));
            }
            String jumanResult;
            String jumanResults = null;
            while ((jumanResult = br.readLine()) != null) {
                if (jumanResult.equals("EOS")) { //$NON-NLS-1$
                    jumanResultList.add(jumanResults);
                    jumanResults = null;
                } else {
                    if (jumanResults == null) {
                        jumanResults = jumanResult;
                    } else {
                        jumanResults = jumanResults + "," + jumanResult; //$NON-NLS-1$
                    }
                }
            }
        } catch (IOException | InterruptedException | ExternalProgramExitException e) {
            e.printStackTrace();
        } finally {
            try {
                output.close();
                input.close();
                err.close();
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jumanResultList;
    }


    /*
     * 不要な用語を抽出するメソッド
     */
    private String deleteDisusedCollocation(String jumanResult) {
        StringBuilder rengo = new StringBuilder();
        String[] jumanResults = jumanResult.split(","); //$NON-NLS-1$
        if (jumanResults.length == 1) {
            String[] jumanElement = jumanResults[0].split(" "); //$NON-NLS-1$
            // Juman結果が１単語のみの場合
            if (jumanElement[JUMAN_ELEMENT_PARTOFSPEECH].equals("未定義語")) { //$NON-NLS-1$
                // 未定義語であれば連語リストへ追加
                if (jumanElement[JUMAN_ELEMENT_MIDASHI].length() > 1) {
                    undefWordList.add(jumanElement[JUMAN_ELEMENT_MIDASHI]);
                    rengo.append(jumanElement[JUMAN_ELEMENT_MIDASHI]);
                }
            } else {
                return null;
            }
        } else if (jumanResults.length > 1) {
            // 形態素結果が１単語以上の場合
            int startIndex = -1;
            int endIndex = -1;
            boolean numeralFlag = false;
            for (int i = 0; i < jumanResults.length; i++) {
                // 連語の先頭位置を取得する処理
                String[] jumanElement = jumanResults[i].split(" "); //$NON-NLS-1$
                if (!numeralFlag) {
                    if ((!jumanElement[JUMAN_ELEMENT_MIDASHI].equals("@")) && (jumanElement[JUMAN_ELEMENT_MIDASHI].indexOf("?") == -1)) { //$NON-NLS-1$ //$NON-NLS-2$
                        if ((jumanElement[JUMAN_ELEMENT_PARTOFSPEECH].equals("名詞") || (jumanElement[JUMAN_ELEMENT_PARTOFSPEECH].equals("未定義語")))) { //$NON-NLS-1$ //$NON-NLS-2$
                            if (jumanElement[JUMAN_ELEMENT_PARTOFSPEECH_DETAIL].equals("数詞")) { //$NON-NLS-1$
                                numeralFlag = true;
                                startIndex = i;
                            } else if (!(jumanElement[JUMAN_ELEMENT_PARTOFSPEECH_DETAIL].equals("時相名詞"))) { //$NON-NLS-1$
                                startIndex = i;
                                break;
                            }
                        }
                    }
                } else {
                    if ((!jumanElement[JUMAN_ELEMENT_MIDASHI].equals("@")) && (jumanElement[JUMAN_ELEMENT_MIDASHI].indexOf("?") == -1)) { //$NON-NLS-1$ //$NON-NLS-2$
                        if ((jumanElement[JUMAN_ELEMENT_PARTOFSPEECH].equals("未定義語")) || (jumanElement[JUMAN_ELEMENT_PARTOFSPEECH].equals("名詞"))) { //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                        } else if (jumanElement[JUMAN_ELEMENT_PARTOFSPEECH_DETAIL].equals("名詞性名詞助数辞")) { //$NON-NLS-1$
                            numeralFlag = false;
                            startIndex = -1;
                        }
                    }
                }
            }

            if (startIndex != -1) {
                for (int i = (jumanResults.length - 1); startIndex <= i; i--) {
                    // 連語の末尾位置を取得する処理
                    String[] jumanElement = jumanResults[i].split(" "); //$NON-NLS-1$
                    if ((!jumanElement[0].equals("@")) && (jumanElement[0].indexOf("?") == -1)) { //$NON-NLS-1$ //$NON-NLS-2$
                        if ((jumanElement[JUMAN_ELEMENT_PARTOFSPEECH].equals("名詞")) || (jumanElement[JUMAN_ELEMENT_PARTOFSPEECH].equals("未定義語"))) { //$NON-NLS-1$ //$NON-NLS-2$
                            endIndex = i;
                            break;
                        }
                    }
                }
            }

            if (startIndex > 0) {
                for (int i = (startIndex - 1); i >= 0; i--) {
                    String[] jumanElement = jumanResults[i].split(" "); //$NON-NLS-1$
                    if (jumanElement[JUMAN_ELEMENT_PARTOFSPEECH].equals("動詞")) { //$NON-NLS-1$
                        if (jumanElement[JUMAN_ELEMENT_CONJUGATE].equals("基本連用形") && (jumanElement[JUMAN_ELEMENT_MIDASHI].length() > 1)) { //$NON-NLS-1$
                            startIndex = i;
                        }
                    } else if (jumanElement[JUMAN_ELEMENT_PARTOFSPEECH].equals("形容詞")) { //$NON-NLS-1$
                        if (jumanElement[JUMAN_ELEMENT_CONJUGATE].equals("語幹")) { //$NON-NLS-1$
                            startIndex = i;
                        }
                    } else if (jumanElement[JUMAN_ELEMENT_PARTOFSPEECH].equals("接頭辞")) { //$NON-NLS-1$
                        String[] previousJumanResult = jumanResults[i + 1].split(" "); //$NON-NLS-1$
                        if (previousJumanResult[JUMAN_ELEMENT_PARTOFSPEECH].equals("名詞")) { //$NON-NLS-1$
                            startIndex = i;
                            break;
                        }
                    }
                }
            }

            if ((startIndex != -1) && (endIndex != -1)) {
                // 開始位置から終了位置までJuman結果の表記を結合する処理
                for (int i = startIndex; i <= endIndex; i++) {
                    String[] jumanElement = jumanResults[i].split(" "); //$NON-NLS-1$
                    if ((startIndex - endIndex) == 0) {
                        if (jumanElement[JUMAN_ELEMENT_PARTOFSPEECH].equals("未定義語")) { //$NON-NLS-1$
                            if (jumanElement[JUMAN_ELEMENT_MIDASHI].length() > 1) {
                                return jumanElement[JUMAN_ELEMENT_MIDASHI];
                            } else {
                                return null;
                            }
                        } else {
                            return null;
                        }
                    } else {
                        if ((!jumanElement[0].equals("@")) && (jumanElement[0].indexOf("?") == -1)) { //$NON-NLS-1$ //$NON-NLS-2$
                            if (jumanElement[JUMAN_ELEMENT_PARTOFSPEECH].equals("未定義語")) { //$NON-NLS-1$
                                if (jumanElement[JUMAN_ELEMENT_MIDASHI].length() > 1) {
                                    undefWordList.add(jumanElement[JUMAN_ELEMENT_MIDASHI]);
                                }
                            }
                            rengo.append(jumanElement[JUMAN_ELEMENT_MIDASHI]);
                        }
                    }
                }
            } else {
                return null;
            }
        }
        return rengo.toString();
    }


    /*
     * 未定義語をカウントするメソッド
     */
    private List<String> countUndefinedWord(List<String> rengoTermList) {
        List<String> countedResultList = new ArrayList<String>(rengoTermList);
        // 未定義語の分だけ繰返す
        for (String undefinedWord : undefWordList) {
            int hit = 0;
            // 連語リストの分だけ繰返す
            for (String rengoResult : rengoTermList) {
                int cntAgreementWord = 0;
                String[] jumanElement = rengoResult.split(" "); //$NON-NLS-1$
                // 未定義語が含まれる場合
                if (jumanElement[JUMAN_ELEMENT_MIDASHI].indexOf(undefinedWord) != -1) {
                    if (jumanElement[JUMAN_ELEMENT_MIDASHI].equals(undefinedWord)) {
                        // 連語と未定義語が完全一致の場合
                        countedResultList.remove(rengoResult);
                    } else {
                        // 1つの連語に未定義語が複数ないか確認する処理
                        int index = 0;
                        while (jumanElement[JUMAN_ELEMENT_MIDASHI].indexOf(undefinedWord, index) != -1) {
                            if (index > 0) {
                                cntAgreementWord++;
                            }
                            index += jumanElement[JUMAN_ELEMENT_MIDASHI].substring(index).indexOf(undefinedWord) + 1;
                        }
                    }
                    String[] num = rengoResult.split("#"); //$NON-NLS-1$
                    hit += Integer.parseInt(num[1]) + cntAgreementWord;
                }
            }
            if (hit > 0) {
                countedResultList.add(undefinedWord + " " + undefinedWord + " 名詞 普通名詞 " + TermCForm.NONE + " #" + String.valueOf(hit)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
        return countedResultList;
    }


    /*
     * エラーメッセージを表示させるメソッド
     */
    private static void dispMessage(Shell shell, String title, String text) {
        MessageBox message = new MessageBox(shell);
        message.setText(title);
        message.setMessage(text);
        message.open();
    }
}