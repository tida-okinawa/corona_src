/**
 * @version $Id: CCPUtil.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/02 13:16:12
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.actions;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

/**
 * @author kousuke-morishima
 */
public class CCPUtil {

    /** コンストラクター */
    public CCPUtil() {
    }


    /**
     * UIスレッド以外から呼び出すと、{@link SWTException}が発生します。
     * データのゲット、セットが終わったらdisposeすること。
     * 
     * @return クリップボード
     */
    public Clipboard getClipboard() {
        Display display = Display.getCurrent();
        display = (display == null) ? Display.getDefault() : display;
        Clipboard clip = new Clipboard(display);
        return clip;
    }


    /**
     * デフォルトのTransfer（{@link #defaultTransfer()}）を使って、クリップボードにデータを格納する<br/>
     * Transferを指定したいときは、 {@link #getClipboard()}
     * で取得したClipboardのsetContentsを直接呼び出す。
     * 
     * @param selection
     *            クリップボードに格納するデータ
     * @see Clipboard#setContents(Object[], Transfer[])
     */
    public void setContents(ISelection selection) {
        Clipboard clip = getClipboard();
        Transfer t = defaultTransfer();
        ((LocalSelectionTransfer) t).setSelection(selection);
        clip.setContents(new Object[] { selection }, new Transfer[] { t });
        clip.dispose();
    }


    /**
     * @return デフォルトのTransfer（{@link #defaultTransfer()}）を使ってクリップボードに格納したデータ
     */
    public ISelection getContents() {
        Clipboard clip = getClipboard();
        ISelection ret = (ISelection) clip.getContents(defaultTransfer());
        clip.dispose();
        return ret;
    }


    public static Transfer defaultTransfer() {
        Transfer t = LocalSelectionTransfer.getTransfer();
        return t;
    }
}
