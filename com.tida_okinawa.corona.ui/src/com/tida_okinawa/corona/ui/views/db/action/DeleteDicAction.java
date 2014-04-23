/**
 * @version $Id: DeleteDicAction.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2012/01/12 18:53:00
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.views.db.action;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;

import com.tida_okinawa.corona.internal.ui.actions.CoronaElementDeleteOperation;
import com.tida_okinawa.corona.internal.ui.views.model.IUIElement;
import com.tida_okinawa.corona.internal.ui.views.model.impl.CoronaModel;
import com.tida_okinawa.corona.io.IoActivator;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IFlucDic;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.ISynonymDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.service.IIoService;
import com.tida_okinawa.corona.ui.UIActivator;

/**
 * @author kousuke-morishima
 */
public class DeleteDicAction extends Action {
    public DeleteDicAction() {
    }

    private ICoronaDic dic = null;


    public void setDictionay(ICoronaDic dic) {
        this.dic = dic;
    }


    @Override
    public void run() {
        IIoService service = IoActivator.getService();

        /* 子辞書チェック */
        int id = dic.getId();
        if (dic instanceof IUserDic) {
            if (DicType.JUMAN.equals(((IUserDic) dic).getDicType())) {
                setResult(Status.CANCEL_STATUS);
                return;
            }
            List<ICoronaDic> allDics = service.getDictionarys(ICoronaDic.class);
            for (ICoronaDic dic : allDics) {
                if (dic instanceof ILabelDic) {
                } else if (dic instanceof IFlucDic) {
                } else if (dic instanceof ISynonymDic) {
                } else {
                    continue;
                }
                for (Integer parentId : dic.getParentIds()) {
                    if (parentId == id) {
                        setResult(new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, "子辞書がいます"));
                        return;
                    }
                }
            }
        }

        List<IUIElement> uiElements = CoronaModel.INSTANCE.adapter(dic);
        /* ローカルファイルシステムから削除 */
        CoronaElementDeleteOperation op = new CoronaElementDeleteOperation(uiElements.toArray(new IUIElement[uiElements.size()]), "");
        try {
            IStatus status = op.execute(null, null);
            service.removeDictionary(id);
            setResult(status);
        } catch (ExecutionException e) {
            e.printStackTrace();
            setResult(new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, "ファイル削除中にエラーが発生しました。 "));
        }
    }

    private IStatus result;


    public IStatus getResult() {
        return result;
    }


    private void setResult(IStatus result) {
        this.result = result;
    }
}
