/**
 * @version $Id: CleansingFlowEditor.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/09/13 14:00:02
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.auto.editor;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;

import com.tida_okinawa.corona.common.Encoding;

/**
 * 解析フローを定義するエディタ。バッチファイルを編集することを想定しているので、常にMS932で保存する。
 * 
 * @author kousuke-morishima
 */
public class CleansingFlowEditor extends TextEditor {

    /**
     * コンストラクタ
     */
    public CleansingFlowEditor() {
    }


    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);

        if (input instanceof IFileEditorInput) {
            IFile iFile = ((IFileEditorInput) input).getFile();
            try {
                String charset = iFile.getCharset();
                if (!Encoding.MS932.toString().equalsIgnoreCase(charset) || !Encoding.Shift_JIS.toString().equalsIgnoreCase(charset)) {
                    convertCharset(iFile, Encoding.MS932.toString());
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * ファイルの中身の文字コードを
     * 
     * @param file
     *            文字コードを変換するファイル
     * @param newCharset
     *            新しい文字コード
     */
    void convertCharset(IFile file, String newCharset) {
        InputStreamReader reader = null;
        try {
            /* 現在の文字コードで読み出す */
            InputStream is = file.getContents();
            String oldCharset = file.getCharset();
            reader = new InputStreamReader(is, oldCharset);
            CharArrayWriter writer = new CharArrayWriter();
            char[] cbuf = new char[1024];
            int n;
            while ((n = reader.read(cbuf)) != -1) {
                writer.write(cbuf, 0, n);
            }

            /* 新しい文字コードで書き出す */
            ByteArrayInputStream newIs = new ByteArrayInputStream(writer.toString().getBytes(newCharset));
            file.setContents(newIs, IResource.FORCE, getProgressMonitor());
            file.setCharset(Encoding.MS932.toString(), getProgressMonitor());
        } catch (CoreException | IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
    }

}
