/**
 * @version $Id: Erratum.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/08 11:27:43
 * @author wataru-higa
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.erratum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;

import com.tida_okinawa.corona.correction.CorrectionActivator;
import com.tida_okinawa.corona.correction.CorrectionPreferenceInitializer;

/**
 * 入力データを補正するクラス
 * 
 * @author wataru-higa
 */
public class Erratum {
    private static final char[] HANKAKU_ARRAY = { 'ｦ', 'ｧ', 'ｨ', 'ｩ', 'ｪ', 'ｫ', 'ｬ', 'ｭ', 'ｮ', 'ｯ', 'ｰ', 'ｱ', 'ｲ', 'ｳ', 'ｴ', 'ｵ', 'ｶ', 'ｷ', 'ｸ', 'ｹ', 'ｺ', 'ｻ',
            'ｼ', 'ｽ', 'ｾ', 'ｿ', 'ﾀ', 'ﾁ', 'ﾂ', 'ﾃ', 'ﾄ', 'ﾅ', 'ﾆ', 'ﾇ', 'ﾈ', 'ﾉ', 'ﾊ', 'ﾋ', 'ﾌ', 'ﾍ', 'ﾎ', 'ﾏ', 'ﾐ', 'ﾑ', 'ﾒ', 'ﾓ', 'ﾔ', 'ﾕ', 'ﾖ', 'ﾗ', 'ﾘ',
            'ﾙ', 'ﾚ', 'ﾛ', 'ﾜ', 'ﾝ', '.', '｡', ',', '､', '(', '[', '{', '<', '｢', '"', ')', ']', '}', '〉', '>', '》', '｣', '"', '･', ':', ';', '?', '!', '`',
            '^', '~', '～', '_', '/', '|', '+', '－', '-', '=', '\\', '$', '#', '&', '*', '@', '\'', '%', '①', '②', '③', '④', '⑤', '⑥', '⑦', '⑧', '⑨' };

    private static final char[] ZENKAKU_ARRAY = { 'ヲ', 'ァ', 'ィ', 'ゥ', 'ェ', 'ォ', 'ャ', 'ュ', 'ョ', 'ッ', 'ー', 'ア', 'イ', 'ウ', 'エ', 'オ', 'カ', 'キ', 'ク', 'ケ', 'コ', 'サ',
            'シ', 'ス', 'セ', 'ソ', 'タ', 'チ', 'ツ', 'テ', 'ト', 'ナ', 'ニ', 'ヌ', 'ネ', 'ノ', 'ハ', 'ヒ', 'フ', 'ヘ', 'ホ', 'マ', 'ミ', 'ム', 'メ', 'モ', 'ヤ', 'ユ', 'ヨ', 'ラ', 'リ',
            'ル', 'レ', 'ロ', 'ワ', 'ン', '．', '。', '，', '、', '（', '［', '｛', '＜', '「', '“', '）', '］', '｝', '〉', '＞', '》', '」', '”', '・', '：', '；', '？', '！', '｀',
            '＾', 'ー', 'ー', '＿', '／', '｜', '＋', '‐', '‐', '＝', '￥', '＄', '＃', '＆', '＊', '＠', '’', '％', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

    private static final char[] HAN_DAKU_ARRAY = { 'ｳ', 'ｶ', 'ｷ', 'ｸ', 'ｹ', 'ｺ', 'ｻ', 'ｼ', 'ｽ', 'ｾ', 'ｿ', 'ﾀ', 'ﾁ', 'ﾂ', 'ﾃ', 'ﾄ', 'ﾊ', 'ﾋ', 'ﾌ', 'ﾍ', 'ﾎ' };
    private static final char[] ZEN_DAKU_ARRAY = { 'ヴ', 'ガ', 'ギ', 'グ', 'ゲ', 'ゴ', 'ザ', 'ジ', 'ズ', 'ゼ', 'ゾ', 'ダ', 'ヂ', 'ヅ', 'デ', 'ド', 'バ', 'ビ', 'ブ', 'ベ', 'ボ' };

    private static final char[] HAN_HANDAKU_ARRAY = { 'ﾊ', 'ﾋ', 'ﾌ', 'ﾍ', 'ﾎ' };
    private static final char[] ZEN_HANDAKU_ARRAY = { 'パ', 'ピ', 'プ', 'ペ', 'ポ' };

    private static final Character[] DAKU_ARRAY = { 'ﾞ', '゛', 'ﾟ', '゜' };
    private static final Character[] YOUON_ARRAY = { 'ァ', 'ぁ', 'ィ', 'ぃ', 'ゥ', 'ぅ', 'ェ', 'ぇ', 'ォ', 'ぉ', 'ャ', 'ゃ', 'ュ', 'ゅ', 'ョ', 'ょ', 'ッ', 'っ' };

    /**
     * 変換処理用に半角・全角カナのMapを作成
     */
    static Map<Character, Character> HAN2ZEN_MAP;
    static Map<Character, Character> HAN2ZEN_DAKU_MAP;
    static Map<Character, Character> HAN2ZEN_HANDAKU_MAP;
    static {
        HAN2ZEN_MAP = new HashMap<Character, Character>();
        for (int i = 0; i < HANKAKU_ARRAY.length; i++) {
            HAN2ZEN_MAP.put(Character.valueOf(HANKAKU_ARRAY[i]), ZENKAKU_ARRAY[i]);
        }
        HAN2ZEN_DAKU_MAP = new HashMap<Character, Character>();
        for (int i = 0; i < HAN_DAKU_ARRAY.length; i++) {
            HAN2ZEN_DAKU_MAP.put(HAN_DAKU_ARRAY[i], ZEN_DAKU_ARRAY[i]);
        }
        HAN2ZEN_HANDAKU_MAP = new HashMap<Character, Character>();
        for (int i = 0; i < HAN_HANDAKU_ARRAY.length; i++) {
            HAN2ZEN_HANDAKU_MAP.put(HAN_HANDAKU_ARRAY[i], ZEN_HANDAKU_ARRAY[i]);
        }
    }

    /** 濁点・半濁点の一覧 */
    static List<Character> DAKU_LIST;
    /** 拗音・促音の一覧 */
    static List<Character> YOU_LIST;
    static {
        DAKU_LIST = new ArrayList<Character>();
        for (int cnt = 0; cnt < DAKU_ARRAY.length; cnt++) {
            DAKU_LIST.add(DAKU_ARRAY[cnt]);
        }
        YOU_LIST = new ArrayList<Character>();
        for (int cnt = 0; cnt < YOUON_ARRAY.length; cnt++) {
            YOU_LIST.add(YOUON_ARRAY[cnt]);
        }
    }


    /**
     * 入力データを補正する
     * 
     * @param line
     *            補正対象文字列。
     *            must not null
     * @return 変換後
     */
    public String convert(String line) {
        Assert.isNotNull(line); // Assert
        // note: でカナ変換での不正検出を記録できるようにするため
        String result = convertZenkakuString(line);
        checkIllegalWord(result);

        return result;
    }

    private List<IllegalWordRecord> illegalWordList = new ArrayList<IllegalWordRecord>();


    /**
     * @return 前回処理結果の誤記文字一覧
     */
    public List<IllegalWordRecord> getIllegalWordList() {
        return illegalWordList;
    }


    /**
     * @return 前回処理結果に誤記文字が存在する場合、true
     */
    public boolean hasIllegalWords() {
        return illegalWordList.size() > 0;
    }


    /**
     * {@link #convert(String)}の結果をStringで返す。変換できない文字のエラーなどは返さない
     * 
     * @param line
     *            補正対象文字列。must not null
     * @return 補正後文字列
     */
    public static String convertZenkakuString(String line) {
        Assert.isNotNull(line); // Assert
        String line1 = convertKana(line);
        String line2 = convertAlphaNumeric(line1);
        String line3 = deleteSpaces(line2);
        /* 全角マイナスは文字化けするので置換 */
        String line4 = line3.replace("－", "‐"); //$NON-NLS-1$ //$NON-NLS-2$
        return line4;
    }


    /**
     * 半角カタカナを全角に変換する
     * 
     * @param line
     *            補正対象文字列
     * @return 変換後
     */
    private static String convertKana(String line) {
        char[] ch = line.toCharArray();

        /* 1文字ずつチェックして全角カナへ変換する処理 */
        for (int cntMoji = 0; cntMoji < line.length(); cntMoji++) {

            /* ch3[cntMoji + 1]に文字がある場合、濁点文字の処理 */
            if ((line.length() - 1) != cntMoji) {

                /*
                 * 先頭文字が濁点文字だったら、空白にする。
                 * TODO 空文字のほうがよくね？
                 */
                if ((cntMoji == 0) && (DAKU_LIST.contains(ch[cntMoji]))) {
                    ch[cntMoji] = ' ';
                    if (DAKU_LIST.contains(ch[cntMoji + 1])) {
                        ch[cntMoji + 1] = ' ';
                    }

                } else {
                    /* 次の文字が濁点の場合、及び半濁点の場合に全角カナへ変換させる処理 */
                    if (ch[cntMoji + 1] == 'ﾞ' || ch[cntMoji + 1] == '゛') { /* 濁点 */

                        /* 対象の濁点文字がある場合、全角の濁点文字を上書き */
                        if (HAN2ZEN_DAKU_MAP.containsKey(ch[cntMoji])) {
                            ch[cntMoji] = HAN2ZEN_DAKU_MAP.get(ch[cntMoji]);
                            ch[cntMoji + 1] = ' ';
                            cntMoji++;
                        } else {
                            ch[cntMoji + 1] = ' ';
                        }
                    } else if (ch[cntMoji + 1] == 'ﾟ' || ch[cntMoji + 1] == '゜') { /* 半濁点 */

                        /* 対象の半濁点文字がある場合、全角の半濁点文字を上書き */
                        if (HAN2ZEN_HANDAKU_MAP.containsKey(ch[cntMoji])) {
                            ch[cntMoji] = HAN2ZEN_HANDAKU_MAP.get(ch[cntMoji]);
                            ch[cntMoji + 1] = ' ';
                            cntMoji++;
                        } else {
                            ch[cntMoji + 1] = ' ';
                        }
                    }
                }
            }

            Character zen = HAN2ZEN_MAP.get(ch[cntMoji]);

            /* 濁点・半濁点以外の半角カナを変換する処理 */
            if (zen != null) {
                ch[cntMoji] = zen;
            }
            /* 同じ拗音・促音が連続する場合に削除する処理 */
            if (cntMoji != 0) {
                if ((YOU_LIST.contains(ch[cntMoji - 1]) && YOU_LIST.contains(ch[cntMoji])) && (ch[cntMoji - 1] == ch[cntMoji])) {
                    ch[cntMoji - 1] = ' ';
                }
            }
        }

        return String.valueOf(ch);
    }


    /**
     * 半角英数字を全角英数字に変換する
     * 
     * @param line
     *            変換前文字列
     * @return 変換後文字列
     */
    private static String convertAlphaNumeric(String line) {
        StringBuilder sb = new StringBuilder(line);

        /* 1文字ずつ半角英数字を検索し、全角英数字に変換させる処理 */
        for (int mojicnt1 = 0; line.length() > mojicnt1; mojicnt1++) {
            char ch = sb.charAt(mojicnt1);

            if (ch >= 'a' && ch <= 'z') {
                sb.setCharAt(mojicnt1, (char) (ch - 'a' + 'Ａ'));
            } else if (ch >= 'A' && ch <= 'Z') {
                sb.setCharAt(mojicnt1, (char) (ch - 'A' + 'Ａ'));
            } else if (ch >= 'ａ' && ch <= 'ｚ') {
                sb.setCharAt(mojicnt1, (char) (ch - 'ａ' + 'Ａ'));
            } else if (ch >= '0' && ch <= '9') {
                sb.setCharAt(mojicnt1, (char) (ch - '0' + '０'));
            }
        }

        return sb.toString();
    }


    /**
     * 連続する濁点・半濁点、拗音・促音を選定する。
     * 不正な表記（ﾏﾞなど）があれば、エラーを記録
     * 
     * @param line
     *            変換前文字列
     */
    private void checkIllegalWord(String line) {
        illegalWordList.clear();

        char ch[] = line.toCharArray();
        int setStartId = 0;
        int checkErra = 0;

        /* 連続する濁点・半濁点・拗音・促音を選別する処理 */
        for (int cnt = 0; ch.length > cnt; cnt++) {

            if (ch.length - 1 >= cnt) {

                /* 連続する誤記文字列が存在する場合フラグを立てる処理 */
                if (DAKU_LIST.contains(ch[cnt])) {
                    checkErra += 2;
                }


                /* 誤記の開始位置を記録する処理 */
                if (checkErra == 2) {
                    /* 1文字目又は2文字目以降に開始したかを判断 */
                    if (cnt == 0) {
                        setStartId = 0;
                    } else {
                        setStartId = cnt - 1;
                    }
                    checkErra++;
                }

                if (!((DAKU_LIST.contains(ch[cnt])) || ((YOU_LIST.contains(ch[cnt])))) || ((ch.length - 1) == cnt)) {
                    /* 開始位置と誤記文字列をリストに追加する処理 */
                    if (checkErra > 2) {
                        IllegalWordRecord illRecord = new IllegalWordRecord();
                        illRecord.startId = setStartId;
                        if (((ch.length - 1) == cnt) && (DAKU_LIST.contains(ch[cnt]))) {
                            illRecord.illegalWord = line.substring(setStartId, cnt + 1);
                        } else {
                            illRecord.illegalWord = line.substring(setStartId, cnt);
                        }

                        illegalWordList.add(illRecord);
                        checkErra = 0;
                    }
                }

            }
        }
    }


    private static final char HalfSpace = ' ';
    private static final char FullSpace = '　';


    /**
     * 指定文字列中のすべてのスペースを削除する
     * 
     * @param line
     *            補正対象文字列
     * @return 変換後
     */
    private static String deleteSpaces(String line) {
        String erase;
        if (CorrectionActivator.getDefault() != null) {
            IPreferenceStore store = CorrectionActivator.getDefault().getPreferenceStore();
            erase = store.getString(CorrectionPreferenceInitializer.PREF_ERRATUM_SPACES);
        } else {
            erase = CorrectionPreferenceInitializer.ERASE_VERBOSE_SPACES;
        }
        if (CorrectionPreferenceInitializer.ERASE_ALL_SPACES.equals(erase)) {
            // すべての空白を除去する
            line = line.replaceAll(String.valueOf(HalfSpace), ""); //$NON-NLS-1$
            line = line.replaceAll(String.valueOf(FullSpace), ""); //$NON-NLS-1$
        } else if (CorrectionPreferenceInitializer.ERASE_VERBOSE_SPACES.equals(erase)) {
            /*
             * 1.連続した空白はひとつにする
             * 2.英数字間の空白はひとつは残す
             * 3.それ以外の空白は消す
             */
            char[] ch = line.toCharArray();
            if (ch.length > 0) {
                char[] newLine = new char[ch.length];
                int newIndex = 0;
                final int STATE_CHAR = 0;
                final int STATE_SPACE = 1;
                int state = STATE_SPACE; /* 前回読み込んだ文字種 */
                for (int i = 0; i < ch.length; i++) {
                    char c = ch[i];
                    switch (state) {
                    case STATE_CHAR:
                        if (isSpace(c)) {
                            /* スペースは問答無用で全角空白 */
                            newLine[newIndex++] = FullSpace;
                            state = STATE_SPACE;
                        } else {
                            newLine[newIndex++] = c;
                        }
                        break;
                    case STATE_SPACE:
                        if (!isSpace(c)) {
                            /* 英数字で挟まれているか確認 */
                            if (newIndex >= 2) {
                                boolean isAlphaNumericBefore2Char = (isAlpha(newLine[newIndex - 2]) || isNumber(newLine[newIndex - 2]));
                                boolean isAlphaNumericChar = isAlpha(c) || isNumber(c);
                                if (!isAlphaNumericBefore2Char || !isAlphaNumericChar) {
                                    /*
                                     * 2文字前が英数字でないか、今の文字が英数字でなければ、
                                     * 前回読み込んだ空白を除去するためにインデックスをひとつ戻す
                                     */
                                    newIndex--;
                                }
                            }
                            newLine[newIndex++] = c;
                            state = STATE_CHAR;
                        }
                        break;
                    default:
                        break;
                    }
                }
                char[] retCh = new char[newIndex];
                System.arraycopy(newLine, 0, retCh, 0, newIndex);
                line = new String(retCh);
            }
        } else {
            line = line.replaceAll(String.valueOf(HalfSpace), String.valueOf(FullSpace));
        }

        return line;
    }


    private static boolean isSpace(char c) {
        return (c == HalfSpace) || (c == FullSpace);
    }


    private static boolean isAlpha(char c) {
        boolean isAlpha = true;
        if (('ａ' <= c) && (c <= 'ｚ')) {
        } else if (('Ａ' <= c) && (c <= 'Ｚ')) {
        } else if (('a' <= c) && (c <= 'z')) {
        } else if (('A' <= c) && (c <= 'Z')) {
        } else {
            isAlpha = false;
        }
        return isAlpha;
    }


    private static boolean isNumber(char c) {
        // 全角化後に呼ばれているので、半角の判定は要らない
        boolean isNumber = true;
        if (('０' <= c) && (c <= '９')) {
        } else if (('0' <= c) && (c <= '9')) {
        } else {
            isNumber = false;
        }
        return isNumber;
    }

}