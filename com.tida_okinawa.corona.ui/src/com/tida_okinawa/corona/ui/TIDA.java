/**
 * @version $Id: TIDA.java 1841 2014-04-16 06:01:48Z yukihiro-kinjyo $
 * 
 * 2011/09/07 18:43:02
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import com.tida_okinawa.corona.CoronaActivator;
import com.tida_okinawa.corona.common.CleansingNameVariable;
import com.tida_okinawa.corona.internal.ui.views.model.IUIClaim;
import com.tida_okinawa.corona.internal.ui.views.model.IUIDictionary;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.IUIProduct;
import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.IClaimWorkData;
import com.tida_okinawa.corona.io.model.IClaimWorkPattern;
import com.tida_okinawa.corona.io.model.ICoronaObject;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IFlucDic;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.ISynonymDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.ui.editors.ClaimDataEditor;
import com.tida_okinawa.corona.ui.editors.ClaimDataEditorInput;
import com.tida_okinawa.corona.ui.editors.ClaimWorkDataEditorInput;
import com.tida_okinawa.corona.ui.editors.DicEditorInput;
import com.tida_okinawa.corona.ui.editors.FlucDicEditor;
import com.tida_okinawa.corona.ui.editors.FrequentTermEditor;
import com.tida_okinawa.corona.ui.editors.FrequentTermEditorInput;
import com.tida_okinawa.corona.ui.editors.LabelDicEditor;
import com.tida_okinawa.corona.ui.editors.ResultMorphemeEditor;
import com.tida_okinawa.corona.ui.editors.ResultPatternEditor;
import com.tida_okinawa.corona.ui.editors.ResultPatternEditorInput;
import com.tida_okinawa.corona.ui.editors.ResultSynonymEditor;
import com.tida_okinawa.corona.ui.editors.SynonymDicEditor;
import com.tida_okinawa.corona.ui.editors.pattern.PatternDicEditor;
import com.tida_okinawa.corona.ui.editors.user.UserDicEditor;

/**
 * @author kousuke-morishima
 */
public final class TIDA {

    /**
     * @param page
     * @param uiElement
     * @param activate
     *            　throwなしの判定(true:throwなし、false:throwあり)
     * @return
     * @throws PartInitException
     */
    public static final IEditorPart openEditor(IWorkbenchPage page, IUIElement uiElement, boolean activate) throws PartInitException {
        IEditorInput input = null;
        String editorId = null;
        boolean exceptionStep = true;
        try {
            input = createEditorInput(uiElement);
            if (input == null) {
                throw new PartInitException(Messages.TIDA_openEditorMessage01 + uiElement + Messages.TIDA_openEditorMessage02);
            }
            editorId = getEditorId(uiElement.getObject());
            if (editorId == null) {
                throw new PartInitException(Messages.TIDA_openEditorMessage03 + uiElement + Messages.TIDA_openEditorMessage04);
            }
            exceptionStep = false;
            /* エラー発生時には、エディタ画面サイズでのエラー表示を行う処理を含むエラー処理は実行される。 */
            return page.openEditor(input, editorId);
        } catch (PartInitException e) {
            if (exceptionStep) {
                /* ダイアログ表示、及びエラーログ出力 */
                eventException(uiElement, e);
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            /* 例外エラー発生時処理（エラーログ出力、ダイアログ表示） */
            eventException(uiElement, e);
            if (activate) {
                /*
                 * エラー発生による別途処理起動が存在する為
                 * (AbstractNewDictionaryWizardクラスからの起動に対応)
                 */
                throw e;
            }
            /* 例外エラー処理はここで完了。 */
        }

        /* エラー発生時の処理（空のデータを返す） */
        final IEditorPart result[] = new IEditorPart[1];
        return result[0];
    }


    public static final IEditorPart openEditor(IWorkbenchPage page, ICoronaObject object) throws PartInitException {
        IEditorInput input = null;
        String editorId = null;
        boolean exceptionStep = true;
        try {
            input = createEditorInput(object);
            if (input == null) {
                throw new PartInitException(Messages.TIDA_openEditorCMessage01 + object + Messages.TIDA_openEditorCMessage02);
            }
            editorId = getEditorId(object);
            if (editorId == null) {
                throw new PartInitException(Messages.TIDA_openEditorCMessage03 + object + Messages.TIDA_openEditorCMessage04);
            }
            exceptionStep = false;
            /* エラー発生時には、エディタ画面サイズでのエラー表示を行う処理を含むエラー処理は実行される。 */
            return page.openEditor(input, editorId);

        } catch (PartInitException e) {
            if (exceptionStep) {
                /* ダイアログ表示、及びエラーログ出力 */
                eventException(object, e);
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {

            /* 例外エラー発生時処理（エラーログ出力、ダイアログ表示） */
            eventException(object, e);

            /* 例外エラー処理はここで完了。 */
        }

        /* エラー発生時の処理（空のデータを返す） */
        final IEditorPart result[] = new IEditorPart[1];
        return result[0];
    }


    /**
     * 例外エラー発生時処理（エラーログ出力、ダイアログ表示）
     * 
     * @param element
     *            実行中のAction。
     * @param exception
     *            発生したエラーの情報。
     */
    private static void eventException(Object element, Throwable exception) {
        if (exception == null) {
            return;
        }

        /* エラー情報を判定 */
        String errorMessage = exception.getMessage();
        String nonErrorKey000 = "non Error:Type000"; //$NON-NLS-1$
        if (errorMessage != null && (errorMessage.length() >= nonErrorKey000.length())) {
            String key = errorMessage.substring(0, nonErrorKey000.length());
            String detail = errorMessage.substring(nonErrorKey000.length());
            if (key.equals(nonErrorKey000)) {
                /* 解析データと使用辞書との整合性がとれない場合に発生するエラーに対応 */

                /* 警告表示、出力 */
                exceptionWarning000(element, detail);
                return;
            }
        }

        /* StackTrace情報の出力 */
        exception.printStackTrace();
        /* 表示（ログ出力）データの生成 */
        String title = element.toString();
        String exMessage = exception.toString();
        /* titleの詳細表示 */
        String resource = Messages.TIDA_title_Detail;

        /* Project Explorerからの起動に場合、リソースパス名を付加する。 */
        if (element instanceof IUIWork) {
            IUIWork uiWork = (IUIWork) element;
            title += String.format(resource, uiWork.getResource().getFullPath());
        } else if (element instanceof IUIDictionary) {
            IUIDictionary uiDic = (IUIDictionary) element;
            title += String.format(resource, uiDic.getResource().getFullPath());
        } else if (element instanceof IUIClaim) {
            IUIClaim uiClm = (IUIClaim) element;
            title += String.format(resource, uiClm.getResource().getFullPath());
        }

        /* ダイアログ表示、及びエラーログ出力の情報を編集 */
        String resMessage = Messages.TIDA_eventExceptionMessage;
        Object args[] = { exMessage, title };
        String message = String.format(resMessage, args);
        String dialogMessage = message;
        IStatus error = new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, message, exception);
        /* エラーログ出力 */
        CoronaActivator.log(error, false);
        /* ダイアログ表示 */
        MessageDialog.openError(new Shell(), title, dialogMessage);
    }


    /**
     * 解析データと使用辞書との整合性がとれない場合に発生するエラーに対応(警告として発生させる)
     * 
     * @param element
     *            実行中のAction。
     * @param detail
     *            詳細情報（NGCode等）。
     */
    private static void exceptionWarning000(Object element, String detail) {
        /* 表示（ログ出力）データの生成 */
        String title = element.toString();
        String exMessage = Messages.TIDA_exceptionWarning000_message;
        /* titleの詳細表示 */
        String resource = Messages.TIDA_title_Detail;

        /* Project Explorerからの起動に場合、リソースパス名を付加する。 */
        if (element instanceof IUIWork) {
            IUIWork uiWork = (IUIWork) element;
            title += String.format(resource, uiWork.getResource().getFullPath());
        } else if (element instanceof IUIDictionary) {
            IUIDictionary uiDic = (IUIDictionary) element;
            title += String.format(resource, uiDic.getResource().getFullPath());
        } else if (element instanceof IUIClaim) {
            IUIClaim uiClm = (IUIClaim) element;
            title += String.format(resource, uiClm.getResource().getFullPath());
        }

        /* ダイアログ表示、及びエラーログ出力の情報を編集 */
        String resMessage = Messages.TIDA_exceptionWarning000_title;

        Object args[] = { exMessage, title };
        String message = String.format(resMessage, args);
        // TODO 複数行の空白行を含む記述は今回は行わない。
        //String resMessageDetail = Messages.TIDA_exceptionWarning000_detail;
        //String dialogMessage = message + resMessageDetail;
        String dialogMessage = message;
        IStatus Warning = new Status(IStatus.WARNING, UIActivator.PLUGIN_ID, dialogMessage);
        /* Warningログ出力 */
        CoronaActivator.log(Warning, false);
        /* Warningダイアログ表示 */
        MessageDialog.openWarning(new Shell(), title, dialogMessage);
    }


    /**
     * ローカルにファイルがあるものはこっちを使う 。{@link #createEditorInput(ICoronaObject)}
     * を使うと、DBViewから開いたと判断されて、編集などに制限がかかる(処理結果はどちらでも一緒)
     * 
     * @param uiElement
     * @return
     */
    public static IEditorInput createEditorInput(IUIElement uiElement) {
        Assert.isLegal(uiElement != null); // Assert
        IEditorInput ret = null;
        if (uiElement instanceof IUIDictionary) {
            ret = new DicEditorInput((IUIDictionary) uiElement);
        } else if (uiElement instanceof IUIWork) {
            IUIWork uiWork = (IUIWork) uiElement;
            IClaimWorkData workData = uiWork.getObject();
            IUIProduct uiProduct = (IUIProduct) CoronaModel.INSTANCE.getUIContainer(IUIProduct.class, uiWork);
            String prodName = uiProduct.getObject().getName();

            switch (workData.getClaimWorkDataType()) {
            case CORRECTION_MISTAKES:
                ret = new ClaimDataEditorInput(IoActivator.getService().getClaimData(workData.getClaimId()), uiWork, prodName,
                        CleansingNameVariable.MISTAKE_CORRECT);
                break;
            case MORPHOLOGICAL:
            case DEPENDENCY_STRUCTURE:
                ClaimWorkDataEditorInput mInput = new ClaimWorkDataEditorInput(CleansingNameVariable.MORPH_DEPEND, workData);
                ret = mInput;
                break;
            case CORRECTION_FLUC:
                break;
            case CORRECTION_SYNONYM:
                ClaimWorkDataEditorInput synInput = new ClaimWorkDataEditorInput(CleansingNameVariable.FLUC_SYNONYM, workData);
                // 形態素解析結果のEditorInputを作る
                ret = synInput;
                break;
            case RESLUT_PATTERN:
                ResultPatternEditorInput pInput = new ResultPatternEditorInput(CleansingNameVariable.PATTERN_PARSING, (IClaimWorkPattern) workData);
                ret = pInput;
                break;
            case FREQUENTLY_APPERING:
                ret = new FrequentTermEditorInput(uiProduct, CleansingNameVariable.FREQUENT, workData);
                break;
            default:
                break;
            }
        } else if (uiElement instanceof IUIClaim) {
            if (uiElement.getObject() != null) {
                ret = new ClaimDataEditorInput((IUIClaim) uiElement, "問い合わせ"); //$NON-NLS-1$
            }
        }
        return ret;
    }


    /**
     * ローカルから開いたときは、 {@link #createEditorInput(IUIElement)}を使う。
     * こっちを使うと、DBViewから開いたとみなされて、編集などで制限がかかる
     * こっちで作れるのは、ICoronaDicとIClaimDataのインプットだけ
     * 
     * @param object
     * @return
     */
    public static IEditorInput createEditorInput(ICoronaObject object) {
        IEditorInput ret = null;
        if (object instanceof ICoronaDic) {
            ret = new DicEditorInput((ICoronaDic) object);
        } else if (object instanceof IClaimData) {
            ret = new ClaimDataEditorInput((IClaimData) object, "問い合わせ"); //$NON-NLS-1$
        }
        return ret;
    }


    public static String getEditorId(ICoronaObject o) {
        String ret = null;
        if (o instanceof IUserDic) {
            ret = UserDicEditor.EDITOR_ID;
        } else if (o instanceof IPatternDic) {
            ret = PatternDicEditor.EDITOR_ID;
        } else if (o instanceof ILabelDic) {
            ret = LabelDicEditor.EDITOR_ID;
        } else if (o instanceof IFlucDic) {
            ret = FlucDicEditor.EDITOR_ID;
        } else if (o instanceof ISynonymDic) {
            ret = SynonymDicEditor.EDITOR_ID;
        } else if (o instanceof IClaimWorkData) {
            IClaimWorkData workData = (IClaimWorkData) o;
            switch (workData.getClaimWorkDataType()) {
            case CORRECTION_MISTAKES:
                ret = ClaimDataEditor.EDITOR_ID;
                break;
            case MORPHOLOGICAL:
            case DEPENDENCY_STRUCTURE:
                ret = ResultMorphemeEditor.EDITOR_ID;
                break;
            case CORRECTION_FLUC:
                break;
            case CORRECTION_SYNONYM:
                ret = ResultSynonymEditor.EDITOR_ID;
                break;
            case RESLUT_PATTERN:
                ret = ResultPatternEditor.EDITOR_ID;
                break;
            case FREQUENTLY_APPERING:
                ret = FrequentTermEditor.EDITOR_ID;
                break;
            default:
                break;
            }
        } else if (o instanceof IClaimData) {
            ret = ClaimDataEditor.EDITOR_ID;
        }
        return ret;
    }

    /**
     * URLが正しいかを判断するための正規表現
     */
    public static final String URL_REGEX = "http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%\\+&=]*)?"; //$NON-NLS-1$


    /**
     * @param url
     *            正規のURLであれば、内部ブラウザで開く
     * @param browserId
     *            may be null
     * @param title
     *            may be null
     * @param tooltip
     *            may be null
     */
    public static void openBrowser(String url, String browserId, String title, String tooltip) {
        title = ((title == null) || (title.equals(""))) ? "ブラウザ" : title; //$NON-NLS-1$ //$NON-NLS-2$
        tooltip = (tooltip == null) ? "" : tooltip; //$NON-NLS-1$
        if (url.matches(URL_REGEX)) {
            IWorkbenchBrowserSupport bs = PlatformUI.getWorkbench().getBrowserSupport();
            IWebBrowser browser;
            try {
                browser = bs.createBrowser(
                        IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR | IWorkbenchBrowserSupport.AS_EDITOR, browserId, title,
                        tooltip);
                System.out.println();
                browser.openURL(new URL(url));
            } catch (PartInitException | MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}
