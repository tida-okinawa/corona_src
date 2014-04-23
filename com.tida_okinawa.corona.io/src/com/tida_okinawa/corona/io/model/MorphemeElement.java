/**
 * @version $Id: MorphemeElement.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/02 20:34:11
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.io.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.tida_okinawa.corona.io.model.dic.ITerm;

/**
 * 形態素 (Jumanの解析結果 + 同義語補正)
 * 
 * - スペース区切りで要素を列挙(0:表記, 1:表記読み, 2:原形, 3:品詞, ...)
 * - 類義語補正した結果
 * (補正後の結果 ) " # " (元の情報)
 * 元の情報は、表記, 表記読み, 原形, 品詞
 * 
 * @author imai
 * 
 */
public class MorphemeElement implements IAdaptable {
    /** 形態素結果の、「表記」のある位置 */
    public final static int HYOUKI = 0;
    /** 形態素結果の、「よみ」のある位置 */
    public final static int YOMI = 1;
    /** 形態素結果の、「原形」のある位置 */
    public final static int GENKEI = 2;
    /** 形態素結果の、「品詞」のある位置 */
    public final static int HINSHI = 3;
    /** 形態素結果の、「品詞」の数値 */
    public final static int HINSHI_NUM = 4;
    /** 形態素結果の、「品詞細分類」のある位置 */
    public final static int HINSHI_SAIBUNRUI = 5;
    /** 形態素結果の、「品詞細分類」の数値 */
    public final static int HINSHI_SAIBUNRUI_NUM = 6;
    /** 形態素結果の、「活用形」のある位置 */
    public final static int CLASS = 7;
    /** 形態素結果の、「活用形」の数値 */
    public final static int CLASS_NUM = 8;
    /** 形態素結果の、「活用型」のある位置 */
    public final static int CTYPE = 9;
    /** 形態素結果の、「活用型」の数値 */
    public final static int CTYPE_NUM = 10;
    /** 形態素結果の、付加情報(「代表表記」や「カテゴリ」など)のある位置 */
    public final static int SEMANTIC_INFO = 11;// "で分割がずれる

    // 同義語補正の元情報 0:は 区切り符号
    /** 同義語補正された場合の、「元の表記」がある位置 */
    public final static int ORG_HYOUKI = 1;
    /** 同義語補正された場合の、「元のよみ」がある位置 */
    public final static int ORG_YOMI = 2;
    /** 同義語補正された場合の、「元の原形」がある位置 */
    public final static int ORG_GENKEI = 3;

    /**
     * 同義語置き換え符号
     */
    final static private String SYNONYM_DELIM = "#";
    /**
     * 同義語置き換えの位置
     */
    int synonym_delim_pos = -1;

    /**
     * Juman/KNP の品詞情報の区切り文字
     */
    final static char DELIM = ' ';

    final static char BAR_L = '"';

    final static char BAR_R = '"';

    /**
     * Juman/KNPの出力
     */
    String text;

    /**
     * 各要素
     */
    List<String> parts;

    /**
     * 文番号
     * TODO:本来は文->文節->単語とすべき。とりあえず、単語に所属する文番号を持たせる
     */
    public int indexSentence = -1;


    /**
     * 
     * @param text
     *            Juman/KNP の出力
     */
    public MorphemeElement(String text) {
        this.text = text;
    }


    /**
     * 同義語で補正済みか
     * 
     * @return 補正してあればtrue
     */
    public boolean hasSynonym() {
        return synonym_delim_pos != -1;
    }


    /**
     * 要素を取得
     * 
     * @param index
     *            取得する要素の位置。このクラスで定義された位置定数で指定する
     * @return 指定された要素
     * @see #HYOUKI
     * @see #YOMI
     * @see #GENKEI
     * @see #HINSHI
     * @see #HINSHI_SAIBUNRUI
     * @see #CLASS
     * @see #CTYPE
     * @see #SEMANTIC_INFO
     */
    public String get(int index) {
        if (parts == null) {
            parts = split(text, DELIM, BAR_L, BAR_R);
            int sz = parts.size();
            for (int i = 0; i < sz; i++) {
                if (SYNONYM_DELIM.equals(parts.get(i))) {
                    synonym_delim_pos = i;
                }
            }
        }
        return parts.get(index);
    }


    /**
     * この形態素結果の表記を返す
     * 
     * @return 表記
     */
    public String getHyouki() {
        return get(HYOUKI);
    }


    /**
     * この形態素結果のよみを返す
     * 
     * @return よみ
     */
    public String getYomi() {
        return get(YOMI);
    }


    /**
     * この形態素結果の原形
     * 
     * @return 原形
     */
    public String getGenkei() {
        return get(GENKEI);
    }


    /**
     * この形態素結果の品詞
     * 
     * @return 品詞
     */
    public String getHinshi() {
        return get(HINSHI);
    }


    /**
     * TODO getHinshiに統一
     * 
     * @deprecated use {@link MorphemeElement#getHinshi()}
     * 
     * @return 品詞
     */
    @Deprecated
    public String getPart() {
        return get(HINSHI);
    }


    /**
     * この形態素結果の品詞細分類
     * 
     * @return 品詞細分類
     */
    public String getHinshiSaibunrui() {
        return get(HINSHI_SAIBUNRUI);
    }


    /**
     * この形態素結果の活用形
     * 
     * @return 活用形
     */
    public String getWordClass() {
        return get(CLASS);
    }


    /**
     * この形態素結果の活用型
     * 
     * @return 活用型
     */
    public String getCform() {
        return get(CTYPE);
    }


    /**
     * 同義語補正前の情報を取得
     * 
     * @param index
     *            取得する要素の位置
     * @return 補正前の情報
     * @see #HYOUKI
     * @see #YOMI
     * @see #GENKEI
     */
    private String getOriginal(int index) {
        if (synonym_delim_pos == -1) {
            return null;
        }
        return get(synonym_delim_pos + index);
    }


    /**
     * 同義語補正前の表記
     * 
     * @return 補正前の表記
     */
    public String getOriginalHyouki() {
        return getOriginal(ORG_HYOUKI);
    }


    /**
     * 同義語補正前のよみ
     * 
     * @return 補正前のよみ
     */
    public String getOriginalYomi() {
        return getOriginal(ORG_YOMI);
    }


    /**
     * 同義語補正前の原形
     * 
     * @return 補正前の原形
     */
    public String getOriginalGenkei() {
        return getOriginal(ORG_GENKEI);
    }


    /**
     * 同義語に置き換える
     * 
     * @param replace
     *            置き換える語（代表語）
     */
    public void replace(ITerm replace) {
        if (!hasSynonym()) { // すでに置き換えられている場合は、元の情報は残す
            synonym_delim_pos = parts.size();
            parts.add(SYNONYM_DELIM); // index = synonym_delim_pos
            parts.add(get(HYOUKI)); // index = synonym_delim_pos + ORG_HYOUKI
            parts.add(get(YOMI)); // index = synonym_delim_pos + ORG_YOMI
            parts.add(get(GENKEI)); // index = synonym_delim_pos + ORG_GENKEI
            parts.add(get(HINSHI)); // index = synonym_delim_pos + ORG_PART
            parts.add(get(HINSHI_SAIBUNRUI)); // index = synonym_delim_pos +
                                              // ORG_CLASS
            parts.add(get(SEMANTIC_INFO));
            // Memo 必要なものがあれば適宜追加する
        }

        parts.set(HYOUKI, replace.getValue());
        parts.set(YOMI, replace.getReading());
        parts.set(GENKEI, replace.getValue());
        parts.set(HINSHI, replace.getTermPart().getName());
        parts.set(HINSHI_SAIBUNRUI, replace.getTermClass().getName());
        parts.set(SEMANTIC_INFO, '"' + "代表表記:" + get(HYOUKI) + "/" + get(YOMI) + '"'); // 頻出用語抽出で使うので置換
                                                                                       // */
        // Memo 必要なものがあれば適宜追加する

        StringBuilder buf = new StringBuilder(text.length() + 50);
        for (String s : parts) {
            buf.append(s).append(" ");
        }
        text = buf.substring(0, buf.length() - 1);
    }


    /**
     * {@link #SEMANTIC_INFO}で表す位置に格納されている情報を取得する
     * 例）"代表表記:コンピューター/こんぴゅーたー カテゴリ:..." → "コンピューター/こんぴゅーたー"
     * 
     * @param attr
     *            取り出す情報のラベル。
     *            ex. "代表表記"、"カテゴリ"
     * @return 指定されたラベルの値。指定されたラベルが存在しない場合はnull
     */
    public String getSemanticInfo(String attr) {
        String s = get(SEMANTIC_INFO);
        int pos0 = s.indexOf(attr + ":");
        if (pos0 == -1) {
            return null;
        }
        s = s.substring(pos0);
        int pos1 = s.indexOf(" ");
        if (pos1 != -1) {
            s = s.substring(0, pos1);
        }
        return s;
    }


    /**
     * この形態素結果の、代表表記を取得
     * 
     * @return 代表表記
     */
    public String getDaihyoHyouki() {
        return getDaihyo()[0];
    }


    /**
     * この形態素結果の、代表表記のよみを取得
     * 
     * @return 代表表記のよみ
     */
    public String getDaihyoYomi() {
        return getDaihyo()[1];
    }


    String[] getDaihyo() {
        String s = getSemanticInfo("代表表記");
        return s.split("/");
    }


    @Override
    public String toString() {
        // DBへの格納用
        return text;
    }


    /**
     * 要素で区切る
     * 
     * TODO: 他のプラグインでも使うので共通にしたい
     * 
     * {@link String#split(String)} は遅いので
     * 
     * @param text
     * @param delim
     * @param barent_l
     * @param barent_r
     * @param sz
     *            取り出す要素数
     * @return
     * 
     */
    private static List<String> split(String text, char delim, char barent_l, char barent_r) {
        List<String> result = new ArrayList<String>();
        String s = text;
        while (!s.isEmpty()) {
            if (s.charAt(0) == barent_l) {
                int pos = s.substring(1).indexOf(barent_r);
                if (pos == -1) {
                    throw new IllegalArgumentException(barent_r + "がない:" + text);
                }
                result.add(s.substring(1).substring(0, pos));
                s = s.substring(1).substring(pos + 1);
                // "..." の後のスペースを飛ばす
                while (!s.isEmpty() && s.charAt(0) == delim) {
                    s = s.substring(1);
                }
            } else {
                int pos = s.indexOf(delim);
                if (pos == -1) {
                    result.add(s);
                    break;
                } else {
                    result.add(s.substring(0, pos));
                }
                s = s.substring(pos + 1);
            }
        }
        return result;
    }


    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (adapter.equals(IPropertySource.class)) {
            return source;
        }
        return null;
    }

    private IPropertySource source = new IPropertySource() {
        @Override
        public IPropertyDescriptor[] getPropertyDescriptors() {
            List<IPropertyDescriptor> descriptor = new ArrayList<IPropertyDescriptor>();
            descriptor.add(new TextPropertyDescriptor(HYOUKI, "表記"));
            descriptor.add(new TextPropertyDescriptor(YOMI, "読み"));
            descriptor.add(new TextPropertyDescriptor(GENKEI, "原形"));
            descriptor.add(new TextPropertyDescriptor(HINSHI, "品詞"));
            descriptor.add(new TextPropertyDescriptor(HINSHI_SAIBUNRUI, "細分類"));
            descriptor.add(new TextPropertyDescriptor(CLASS, "活用形"));
            if (hasSynonym()) {
                descriptor.add(new TextPropertyDescriptor(synonym_delim_pos + ORG_HYOUKI, "補正元[表記]"));
                descriptor.add(new TextPropertyDescriptor(synonym_delim_pos + ORG_YOMI, "補正元[読み]"));
                descriptor.add(new TextPropertyDescriptor(synonym_delim_pos + ORG_GENKEI, "補正元[原形]"));
            }
            return descriptor.toArray(new IPropertyDescriptor[descriptor.size()]);
        }


        @Override
        public Object getPropertyValue(Object id) {
            if (id instanceof Integer) {
                return get((Integer) id);
            }
            return null;
        }


        @Override
        public boolean isPropertySet(Object id) {
            return false;
        }


        @Override
        public void resetPropertyValue(Object id) {
        }


        @Override
        public void setPropertyValue(Object id, Object value) {
        }


        @Override
        public Object getEditableValue() {
            return null;
        }

    };
}
