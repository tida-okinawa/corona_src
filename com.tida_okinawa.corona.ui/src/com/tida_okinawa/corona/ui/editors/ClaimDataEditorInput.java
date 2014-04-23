/**
 * @version $Id: ClaimDataEditorInput.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/09/29 13:44:17
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.editors;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.views.properties.IPropertySource;

import com.tida_okinawa.corona.internal.ui.views.model.IUIClaim;
import com.tida_okinawa.corona.internal.ui.views.model.IUIWork;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.ui.Icons;

/**
 * @author kousuke-morishima
 */
public class ClaimDataEditorInput implements IEditorInput {

    private IUIClaim uiClaim;
    private IClaimData claim;
    private boolean isMistakes;
    private String productName;
    private String name;
    private IUIWork uiWork;


    public ClaimDataEditorInput(IUIClaim uiClaim, String name) {
        this(uiClaim.getObject(), name);
        this.uiClaim = uiClaim;
    }


    public ClaimDataEditorInput(IClaimData claim, String name) {
        this.claim = claim;
        this.name = name;
        this.isMistakes = false;
    }


    /**
     * @param claim
     * @param uiWork
     *            誤記補正結果を保持している中間データ
     * @param productName
     *            このターゲットのレコードのみ表示する場合に指定する。nullなら制限しない。
     * @param name
     *            must not null
     */
    public ClaimDataEditorInput(IClaimData claim, IUIWork uiWork, String productName, String name) {
        if (claim == null) {
            throw new IllegalArgumentException("IClaimData must not null");
        }
        this.uiWork = uiWork;
        this.claim = claim;
        this.isMistakes = (uiWork != null);
        this.productName = productName;
        this.name = name;
    }


    /**
     * @return may be null
     */
    public IUIClaim getUIClaim() {
        return uiClaim;
    }


    /**
     * @return may be null
     */
    public IUIWork getUIWork() {
        return uiWork;
    }


    /**
     * @return not null
     */
    public IClaimData getClaim() {
        return claim;
    }


    /**
     * 誤記補正済みデータを表示するためのEditorInputならtrueを返す
     * この値がtrueなら、{@link #getUIClaim()}は必ずnullを返す
     * 
     * @return
     */
    public boolean isMistakesData() {
        return isMistakes;
    }


    /**
     * この値がnullでなければ、このターゲット名のレコードのみ表示することを期待されている
     * 
     * @return may be null.
     */
    public String getProductName() {
        return productName;
    }


    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj))
            return true;

        if (!(obj instanceof ClaimDataEditorInput))
            return false;

        ClaimDataEditorInput e2 = (ClaimDataEditorInput) obj;

        if (uiClaim != null) {
            if (e2.uiClaim != null) {
                return uiClaim.equals(e2.uiClaim);
            }
            return false;
        } else {
            if (e2.uiClaim != null) {
                return false;
            }
        }

        if (uiWork != null) {
            if (e2.uiWork != null) {
                return uiWork.equals(e2.uiWork);
            }
            return false;
        } else {
            if (e2.uiWork != null) {
                return false;
            }
        }

        if (claim.equals(e2.claim)) {
            if (name.equals(e2.name)) {
                return true;
            }
            return false;
        }
        return false;
    }


    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (adapter.equals(IPropertySource.class)) {
            return uiClaim.getEditableValue();
        }
        return null;
    }


    @Override
    public boolean exists() {
        return false;
    }


    @Override
    public ImageDescriptor getImageDescriptor() {
        return Icons.INSTANCE.getDescriptor(Icons.IMG_CLAIM);
    }


    @Override
    public String getName() {
        if (name != null) {
            if (uiWork != null) {
                return name + "(" + claim.getFileName() + ")";
            }
            return claim.getFileName();
        }
        if (uiClaim != null) {
            return uiClaim.toString();
        }
        return claim.getFileName();
    }


    @Override
    public IPersistableElement getPersistable() {
        return null;
    }


    @Override
    public String getToolTipText() {
        if (uiClaim != null) {
            IResource res = uiClaim.getResource();
            return res.getProject().getName() + "/" + res.getProjectRelativePath().toString();
        }
        if (uiWork != null) {
            IResource res = uiWork.getResource();
            return res.getProject().getName() + "/" + res.getProjectRelativePath().toString();
        }
        return claim.getName();
    }
}
