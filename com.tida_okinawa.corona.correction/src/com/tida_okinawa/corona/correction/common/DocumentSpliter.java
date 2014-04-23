/**
 * @version $Id:
 *
 * 2012/07/23 16:11:11
 * @author shingo-takahashi
 *
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.common;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.tida_okinawa.corona.common.Encoding;
import com.tida_okinawa.corona.correction.CorrectionPreferenceInitializer;
import com.tida_okinawa.corona.correction.Messages;
import com.tida_okinawa.corona.correction.data.CoronaDocumentDefinition;
import com.tida_okinawa.corona.correction.data.CoronaDocumentInformation;
import com.tida_okinawa.corona.correction.data.DocumentSplitType;

/**
 * ドキュメント分割<br/>
 * 
 * {@link CoronaDocumentInformation} の定義情報を用いて、入力ファイルを分割する.<br/>
 * {@link CorrectionPreferenceInitializer} にて、定義情報を編集する。
 * 
 * @author shingo-takahashi
 * 
 */
public class DocumentSpliter {
    private String encode = Encoding.UTF_8.toString();
    private List<String> output = new ArrayList<String>();
    private List<CoronaDocumentDefinition> allDefinitions = new ArrayList<CoronaDocumentDefinition>();
    private List<Integer> divPointList;
    private Map<Integer, Integer> deletePointMap;


    /**
     * コンストラクタ
     */
    public DocumentSpliter() {
        super();
    }


    /**
     * @param docInfo
     *            分割定義情報クラス
     */
    public void setDocumentInformation(CoronaDocumentInformation docInfo) {
        if (docInfo == null) {
            throw new IllegalArgumentException("docInfo must not null"); //$NON-NLS-1$
        }

        // 有効な定義リストを文頭、全体ごとにまとめる
        for (CoronaDocumentDefinition dd : docInfo.getDefinitions()) {
            if (dd.isEnabled()) {
                if (dd.getPosition() == CoronaDocumentDefinition.PHRASE) {
                    allDefinitions.add(dd);
                } else if (dd.getPosition() == CoronaDocumentDefinition.WHOLE) {
                    allDefinitions.add(dd);
                }
            }
        }
    }


    /**
     * 入出力に使用するエンコードを設定する
     * 
     * @param encode
     *            エンコード
     */
    public void setEncode(String encode) {
        this.encode = encode;
    }


    /**
     * 出力
     * 
     * @return 文字列リスト
     */
    public List<String> getOutput() {
        return output;
    }


    private final static void openErrorDialog(String title, String message) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        if (shell != null) {
            MessageDialog.openError(shell, title, message);
        }
    }


    /**
     * 分割処理
     * 
     * @param input
     *            入力ファイル
     * @return 成否
     */
    public boolean split(File input) {
        if (input == null) {
            throw new IllegalArgumentException("input file must not null"); //$NON-NLS-1$
        }

        /* テキスト構造解析に食わせるためのInputStream */
        final InputStream is;

        if (input.getPath().endsWith(".pdf")) { //$NON-NLS-1$
            /*
             * PDFファイルからテキストを抽出する
             */
            FileInputStream pdfStream = null;
            try {
                pdfStream = new FileInputStream(input.getPath());
                PDFParser pdfParser = new PDFParser(pdfStream);
                pdfParser.parse(); // 分析
                PDDocument pdf = pdfParser.getPDDocument();
                PDFTextStripper stripper = new PDFTextStripper();
                String spdf2txt = stripper.getText(pdf);
                is = new ByteArrayInputStream(spdf2txt.getBytes());
            } catch (FileNotFoundException e) {
                openErrorDialog(Messages.ErrorTitle_FailedReadFile, Messages.bind(Messages.ErrorMessage_FileNotFound, input.getPath()));
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                openErrorDialog(Messages.ErrorTitle_FailedReadFile, Messages.bind(Messages.ErrorMessage_FailedReadFile, input.getPath()));
                e.printStackTrace();
                return false;
            } finally {
                if (pdfStream != null) {
                    try {
                        pdfStream.close();
                    } catch (IOException e1) {
                    }
                }
            }
        } else {
            /*
             * それ以外（*.txtとか）
             */
            setEncode(Encoding.Shift_JIS.toString());
            try {
                is = new FileInputStream(input.getPath());
            } catch (FileNotFoundException e) {
                openErrorDialog(Messages.ErrorTitle_FailedReadFile, Messages.bind(Messages.ErrorMessage_FileNotFound, input.getPath()));
                e.printStackTrace();
                return false;
            }
        }

        final String Regex_HeadSpace = "^[　\\s]+"; //$NON-NLS-1$
        final String Regex_TailSpace = "[　\\s]+$"; //$NON-NLS-1$
        BufferedReader br = null;
        try {
            // 1行ごと判定
            br = new BufferedReader(new InputStreamReader(is, encode));
            String line;
            StringBuilder buff = new StringBuilder(100);
            divPointList = new ArrayList<Integer>();
            deletePointMap = new TreeMap<Integer, Integer>(new Comparator<Integer>() {
                @Override
                public int compare(Integer i1, Integer i2) {
                    return i2.compareTo(i1);
                }
            });
            if (allDefinitions.size() > 0) {
                while ((line = br.readLine()) != null) {
                    /*
                     * 行頭・行末の空白文字（全角、半角、タブ）除去をする。
                     * また、DB登録時にエラーになるので'を置換する
                     */
                    line = line.replaceAll(Regex_HeadSpace, "").replaceAll(Regex_TailSpace, "").replace("'", "\"").replace("\\", "\\\\"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                    divPointList.clear();
                    deletePointMap.clear();
                    if (line.length() > 0) {
                        for (CoronaDocumentDefinition definition : allDefinitions) {
                            if (definition.getPosition() == CoronaDocumentDefinition.PHRASE) {
                                // 文頭チェック
                                checkPhrase(line, buff, definition);
                            } else if (definition.getPosition() == CoronaDocumentDefinition.WHOLE) {
                                // 全体チェック
                                checkWhole(line, buff, definition);
                            }
                        }
                        buff.append(line).append("\n"); //$NON-NLS-1$
                        // 文章を分割
                        divisionRecord(buff);
                        divideWriting(buff);
                    } else {
                        // 段落チェック
                        if (buff.length() > 0) {
                            divideWriting(buff);
                            output.add(buff.toString());
                            buff.setLength(0);
                        }
                    }
                }
                if (buff.length() > 0) {
                    divideWriting(buff);
                    output.add(buff.toString());
                }
            } else {
                while ((line = br.readLine()) != null) {
                    line = line.replaceAll(Regex_HeadSpace, "").replaceAll(Regex_TailSpace, "").replaceAll("'", "\"").replace("\\", "\\\\"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                    buff.append(line);
                }
                if (buff.length() > 0) {
                    divideWriting(buff);
                    output.add(buff.toString());
                }
            }
        } catch (IOException e) {
            openErrorDialog(Messages.ErrorTitle_FailedReadFile, Messages.bind(Messages.ErrorMessage_FailedReadFile, input.getPath()));
            e.printStackTrace();
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
        return true;
    }


    /**
     * 分割情報の適用範囲が文頭になっているものが、対象のテキストに合致するかチェックする.<br/>
     * buffには、次の１ブロックとつながる可能性のある文章が蓄えられている。 <br/>
     * 例えば、区切り文字が★で、
     * <p>
     * ★これが１ブロック目です。★これが２<br/>
     * ブロック目です。
     * </p>
     * というように、１つのブロックが改行で区切られたとき、２ブロック目を正しく認識するためにbuffが使われている。
     * 
     * @param line
     *            チェック対象の文章
     * @param buff
     *            文区切り用バッファ
     * @param definition
     *            ドキュメント解析の定義内容
     */
    private void checkPhrase(String line, StringBuilder buff, CoronaDocumentDefinition definition) {
        boolean isHit = true;
        /* 定義内容の位置が「文頭」の場合 */
        if (definition.getType() == CoronaDocumentDefinition.CHAR) {
            /* 定義内容の対象が「1文字」の場合 */
            String regex = "^[" + definition.getDefinition() + "].*"; //$NON-NLS-1$ //$NON-NLS-2$
            if (line.matches(regex)) {
                /* 1文字一致した場合 */
                int divPoint = buff.length();
                if (!divPointList.contains(divPoint)) {
                    divPointList.add(divPoint);
                }
                if (definition.getTrim() == CoronaDocumentDefinition.TRIM) {
                    /* 定義内容の「除去」が有効な場合 */
                    if (!deletePointMap.containsKey(divPoint)) {
                        deletePointMap.put(divPoint, 1);
                    }
                }
            }
        } else {
            /* 定義内容の対象が「文字列」の場合 */
            /* TODO 検査が弱い。DocumentSpliter2を見て直すべき */
            int specialCharIndex = DocumentSplitType.getStartPoint(definition.getDefinition());
            if (specialCharIndex != -1) {
                int defPoint = 0;
                int linePoint = 0;
                /* 特殊文字を含んでいる場合 */
                if (specialCharIndex > 0) {
                    /* 特殊文字の前に記号などの文字がある場合 */
                    // 記号の数だけチェック
                    for (; defPoint < specialCharIndex; defPoint++, linePoint++) {
                        if (linePoint >= line.length()) {
                            /* 定義より文が短い場合 */
                            isHit = false;
                            break;
                        }
                        if (definition.getDefinition().charAt(defPoint) != line.charAt(linePoint)) {
                            isHit = false;
                            break;
                        }
                    }
                }
                // 特殊文字または特殊文字前の文字を確認した後の処理
                for (; defPoint < definition.getDefinition().length(); defPoint++, linePoint++) {
                    if (linePoint >= line.length()) {
                        /* 定義より文が短い場合 */
                        isHit = false;
                        break;
                    }
                    if (specialCharIndex == defPoint) {
                        /* 特殊文字の場合 */
                        int endPoint = lastIndexOf(definition.getDefinition().substring(defPoint), line, linePoint);
                        if (endPoint == -1) {
                            isHit = false;
                            break;
                        }
                        /*
                         * TODO 特殊文字の文字数を求める
                         * 乱暴だけど、特殊文字サイズ分シフト
                         * iはインクリメントされるので、特殊文字より１少なく足している
                         */
                        defPoint += 5;
                        linePoint += endPoint;
                    } else if (definition.getDefinition().charAt(defPoint) != line.charAt((linePoint))) {
                        // 特殊文字がヒットした後の場合
                        isHit = false;
                        break;
                    }
                }
                if (isHit) {
                    /* 全て一致した場合 */
                    int divPoint = buff.length();
                    if (!divPointList.contains(divPoint)) {
                        divPointList.add(divPoint);
                    }
                    if (definition.getTrim() == CoronaDocumentDefinition.TRIM) {
                        /* 定義内容の「除去」が有効な場合 */
                        updateDeleteMap(divPoint, linePoint);
                    }
                }
            } else {
                int defPoint = 0;
                int linePoint = 0;
                /* 特殊文字を含まない場合 */
                for (; defPoint < definition.getDefinition().length(); defPoint++, linePoint++) {
                    if (linePoint >= line.length()) {
                        /* 定義より文が短い場合 */
                        isHit = false;
                        break;
                    }
                    if (definition.getDefinition().charAt(defPoint) != line.charAt(linePoint)) {
                        /* 定義の文字と文中の文字が一致しない場合 */
                        isHit = false;
                        break;
                    }
                }
                if (isHit) {
                    /* 全て一致した場合 */
                    int divPoint = buff.length();
                    if (!divPointList.contains(divPoint)) {
                        divPointList.add(divPoint);
                    }
                    if (definition.getTrim() == CoronaDocumentDefinition.TRIM) {
                        /* 定義内容の「除去」が有効な場合 */
                        updateDeleteMap(divPoint, linePoint);
                    }
                }
            }
        }
    }


    /**
     * 分割情報の適用範囲が全体になっているものが、対象テキストに合致するかチェックする
     * 
     * @param line
     *            テキスト
     * @param buff
     *            チェック対象の文章 かつ、区切り用バッファ
     * @param definition
     *            ドキュメント解析の定義内容
     */
    private void checkWhole(String line, StringBuilder buff, CoronaDocumentDefinition definition) {

        /* 文頭の1文字ずつ削除して比較 */
        for (int linePoint = 0; linePoint < line.length(); linePoint++) {
            if (definition.getType() == CoronaDocumentDefinition.CHAR) {
                /* 定義内容の対象が「1文字」の場合 */
                for (char defWord : definition.getDefinition().toCharArray()) {
                    if (line.charAt(linePoint) == defWord) {
                        int divPoint = buff.length() + linePoint;
                        if (!divPointList.contains(divPoint)) {
                            divPointList.add(divPoint);
                        }
                        if (definition.getTrim() == CoronaDocumentDefinition.TRIM) {
                            /* 定義内容の「除去」が有効な場合 */
                            if (!deletePointMap.containsKey(divPoint)) {
                                deletePointMap.put(divPoint, 1);
                            }
                        } else {
                            continue;
                        }
                    }
                }
            } else {
                boolean isHit = true;
                int specialCharStartPoint = DocumentSplitType.getStartPoint(definition.getDefinition());

                // 定義内容の特殊文字開始位置を取得、特殊文字がない場合-1
                if (specialCharStartPoint != -1) {
                    int defPoint = 0;
                    int textPoint = linePoint;
                    /* 特殊文字を含んでいる場合 */
                    if (specialCharStartPoint > 0) {
                        /* 特殊文字の前に記号などの文字がある場合 */
                        for (; defPoint < specialCharStartPoint; defPoint++, textPoint++) {
                            if (textPoint >= line.length()) {
                                /* 定義より文が短い場合 */
                                isHit = false;
                                break;
                            }
                            if (definition.getDefinition().charAt(defPoint) != line.charAt(textPoint)) {
                                /* 定義の文字と文中の文字が一致しない場合 */
                                isHit = false;
                                break;
                            }
                        }
                        if (!isHit) {
                            continue;
                        }
                    }

                    for (; defPoint < definition.getDefinition().length(); defPoint++, textPoint++) {
                        if (textPoint >= line.length()) {
                            /* 定義より文が短い場合 */
                            isHit = false;
                            break;
                        }
                        if (specialCharStartPoint == defPoint) {
                            /* 特殊文字の場合 */
                            int endPoint = lastIndexOf(definition.getDefinition().substring(defPoint), line, textPoint);
                            if (endPoint != -1) {
                                /*
                                 * TODO 特殊文字の文字数を求める
                                 * 乱暴だけど、特殊文字サイズ分シフト
                                 * iはインクリメントされるので、特殊文字より１少なく足している
                                 */
                                defPoint += 5;
                                textPoint += endPoint;
                            } else {
                                isHit = false;
                                break;
                            }
                        } else if (definition.getDefinition().charAt(defPoint) != line.charAt(textPoint)) {
                            /* 定義の文字と文中の文字が一致しない場合 */
                            isHit = false;
                            break;
                        }
                    }

                    if (isHit) {
                        /* 全て一致した場合 */
                        int divPoint = buff.length() + linePoint;
                        if (!divPointList.contains(divPoint)) {
                            divPointList.add(divPoint);
                        }
                        if (definition.getTrim() == CoronaDocumentDefinition.TRIM) {
                            /* 定義内容の「除去」が有効な場合 */
                            updateDeleteMap(divPoint, textPoint - linePoint);
                        }
                        // 一致した文字数の分だけインクリメント
                        linePoint = textPoint - 1;
                    }

                } else {
                    /* 特殊文字を含まない場合 */
                    int defPoint = linePoint;
                    // 定義の文字数の分だけ繰り返し
                    for (char ch : definition.getDefinition().toCharArray()) {
                        if (ch != line.charAt(defPoint)) {
                            /* 定義の文字と文中の文字が一致しない場合 */
                            isHit = false;
                            break;
                        }
                        defPoint++;
                    }

                    if (isHit) {
                        /* 全て一致した場合 */
                        int divPoint = buff.length() + linePoint;
                        if (!divPointList.contains(divPoint)) {
                            divPointList.add(divPoint);
                        }
                        if (definition.getTrim() == CoronaDocumentDefinition.TRIM) {
                            /* 定義内容の「除去」が有効な場合 */
                            updateDeleteMap(divPoint, defPoint - linePoint);
                        }
                        // 一致した文字数の分だけインクリメント
                        linePoint = defPoint - 1;
                    }
                }
            }
        }
    }


    /**
     * 定義内容の位置が全般の場合にtargetの先頭から何文字目まで特殊文字にヒットするか判定する.
     * 例）10文字目までヒットするなら9が返る。
     * 
     * @param splitText
     *            特殊文字から始まる分割定義情報
     * @param line
     *            StringBuffer
     * @param startPoint
     *            特殊文字をStringBufferから検索する際の開始位置
     * @return 最終HIT位置
     * 
     */
    private static int lastIndexOf(String splitText, String line, int startPoint) {
        String regex;
        // タイプ別に正規表現を
        if (0 == splitText.indexOf(DocumentSplitType.NUMBER.getValue())) {
            regex = "[0-9０-９]+"; //$NON-NLS-1$
        } else if (0 == splitText.indexOf(DocumentSplitType.KANA.getValue())) {
            regex = "[ｦ-ﾟァ-ヴー]+"; //$NON-NLS-1$
        } else if (0 == splitText.indexOf(DocumentSplitType.ABC.getValue())) {
            regex = "[a-zA-Zａ-ｚＡ-Ｚ]+"; //$NON-NLS-1$
        } else {
            return -1;
        }
        // TODO もっと効率よくできない? DocumentSpliter2を見て直す
        int i = startPoint;
        int buffLength = line.length() - 0;
        for (; i < buffLength; i++) {
            if (!String.valueOf(line.charAt(i)).matches(regex)) {
                return i - 1 - startPoint;
            }
        }
        /* 末尾にヒットした時、-1が返る問題に対応 */
        if (i == buffLength) {
            return i - 1 - startPoint;
        }
        return -1;
    }


    private void updateDeleteMap(int key, int deleteLength) {
        /* 定義内容の「除去」が有効な場合 */
        if (deletePointMap.containsKey(key)) {
            if (deletePointMap.get(key) < deleteLength) {
                deletePointMap.put(key, deleteLength);
            }
        } else {
            deletePointMap.put(key, deleteLength);
        }
    }


    private void divisionRecord(StringBuilder buff) {
        if (divPointList.indexOf(0) == -1) {
            // 文頭でヒットしていない場合
            divPointList.add(0);
        }

        // 分割位置を昇順にソート
        Collections.sort(divPointList);

        if (deletePointMap.size() > 0) {
            // 削除位置の重複チェック
            deletePointExtentCheck();
        }

        // 削除する文字数分区切り位置をシフトさせる処理
        for (Entry<Integer, Integer> e : deletePointMap.entrySet()) {
            Integer startPoint = e.getKey();
            Integer deleteLength = e.getValue();

            int elementNum = 0;
            for (Integer divPoint : divPointList) {
                if (startPoint < divPoint) {
                    divPointList.set(elementNum, (divPoint - deleteLength));
                }
                elementNum += 1;
            }
            // StringBufferからヒットした文字列を削除
            buff.delete(startPoint, startPoint + deleteLength);
        }

        // 分割位置リストを用いてStringBufferからレコードに分割
        if (divPointList.size() > 0) {
            int elementNum = 0;
            for (; elementNum < divPointList.size() - 1; elementNum++) {
                if (divPointList.get(elementNum) != divPointList.get(elementNum + 1)) {
                    String divLine = buff.substring(divPointList.get(elementNum), divPointList.get(elementNum + 1));
                    if (!divLine.equals("")) { //$NON-NLS-1$
                        output.add(divLine);
                    }
                }
            }
            // 分割された最後のレコードのみを残す
            buff.delete(divPointList.get(0), divPointList.get(elementNum));
        }
    }


    private void deletePointExtentCheck() {
        // 削除範囲が被っているポイントを削除する処理
        boolean endCheck = false;
        while (!endCheck) {
            boolean updateCheck = false;
            for (Iterator<Entry<Integer, Integer>> itr1 = deletePointMap.entrySet().iterator(); itr1.hasNext();) {
                // 比較先の削除範囲を取得
                Entry<Integer, Integer> e1 = itr1.next();
                Integer startPoint1 = e1.getKey();
                Integer deleteLength1 = e1.getValue();
                Integer endPoint1 = startPoint1 + deleteLength1;

                for (Iterator<Entry<Integer, Integer>> itr2 = deletePointMap.entrySet().iterator(); itr2.hasNext();) {
                    // 比較元の削除範囲を取得
                    Entry<Integer, Integer> e2 = itr2.next();
                    Integer startPoint2 = e2.getKey();
                    Integer endPoint2 = startPoint2 + e2.getValue();
                    if (((startPoint1.intValue() < startPoint2.intValue()) && (startPoint2.intValue() < (endPoint1.intValue())))
                            || (startPoint2.intValue() == (endPoint1.intValue()))) {
                        // 比較先の範囲内に比較元の削除開始位置が重複する場合
                        if ((endPoint1) < (endPoint2)) {
                            // 削除終了位置が比較先の範囲を超えて重複する場合
                            deleteLength1 = ((endPoint2) - (endPoint1)) + deleteLength1;
                            deletePointMap.put(startPoint1, deleteLength1);
                        }
                        itr2.remove();
                        divPointList.remove(startPoint2);
                        updateCheck = true;
                        break;
                    } else if (startPoint1 > startPoint2) {
                        // 削除開始位置が範囲を超えた場合に次の範囲を検索
                        break;
                    }
                }
                if (updateCheck) {
                    // 削除位置が更新された場合、deletePointMapを更新する為、For文から一旦Break
                    break;
                }
            }
            if (!updateCheck) {
                // 重複する削除位置がなくなった場合
                for (Entry<Integer, Integer> e : deletePointMap.entrySet()) {
                    Integer startPoint = e.getKey();
                    Integer endPoint = startPoint + e.getValue();
                    // 分割位置が削除範囲内に含まれていれば削除する処理
                    for (Iterator<Integer> itr = divPointList.iterator(); itr.hasNext();) {
                        Integer divPoint = itr.next();
                        if ((startPoint < divPoint) && (divPoint < endPoint)) {
                            // 分割位置が削除範囲内の場合
                            itr.remove();
                        } else if (endPoint < divPoint) {
                            // 分割位置が削除範囲を超えた場合
                            break;
                        }
                    }
                }
                endCheck = true;
            }
        }
    }


    private void divideWriting(StringBuilder buff) {
        while (buff.length() > 25000) {
            int endPoint = buff.lastIndexOf("。", 25000) + 1; //$NON-NLS-1$
            output.add(buff.substring(0, endPoint));
            buff.delete(0, endPoint);
        }
    }
}
