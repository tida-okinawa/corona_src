/**
 * @version $Id: Messages.java 968 2013-03-05 12:25:25Z kousuke-morishima $
 * 
 * 2013/02/21 18:41:14
 * @author s.takuro
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.io.dam.hibernate;

import org.eclipse.osgi.util.NLS;

/**
 * @author s.takuro
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.tida_okinawa.corona.io.dam.hibernate.messages"; //$NON-NLS-1$
    public static String ClaimData_NoNameTarget;
    public static String IoService_connectDatabaseFail;
    public static String IoService_connectDatabaseSuccess;
    public static String IoService_connectionFail;
    public static String IoService_connectionSuccess;
    public static String IoService_creationTime;
    public static String IoService_date;
    public static String IoService_dbname;
    public static String IoService_dicId;
    public static String IoService_dicName;
    public static String IoService_dummy;
    public static String IoService_errorConnectDatabase;
    public static String IoService_errorDisconnectionDatabase;
    public static String IoService_errorEndCoronaActivator;
    public static String IoService_errorGetProjectId;
    public static String IoService_errorGetProjectInfo;
    public static String IoService_errorReconnectDatabaseFail;
    public static String IoService_errorSetProjectInfo;
    public static String IoService_getDataFileFail;
    public static String IoService_getDicInfoFail;
    public static String IoService_id;
    public static String IoService_interrupt;
    public static String IoService_modifyProject;
    public static String IoService_name;
    public static String IoService_projectId;
    public static String IoService_projectName;
    public static String IoService_reconnect;
    public static String IoService_reconnectDatabase;
    public static String IoService_systemErrorGetCategoryInfo;
    public static String IoService_systemErrorSetCategoryInfo;
    public static String Product_creationTime;
    public static String Product_date;
    public static String Product_dicId;
    public static String Product_dicName;
    public static String Product_errorGetDataFile;
    public static String Product_errorModifyProductName;
    public static String Product_errorNotProduct;
    public static String Product_extensionDic;
    public static String Product_fldId;
    public static String Product_id;
    public static String Product_productId;
    public static String Product_productName;
    public static String Product_projectId;
    public static String Product_systemErrorCheckDicRelationExist;
    public static String Product_systemErrorGetDic;
    public static String Product_systemErrorGetDicId;
    public static String Product_systemErrorGetDicRelation;
    public static String Product_systemErrorSetDic;
    public static String Product_systemErrorSetDicRelation;
    public static String Product_type;
    public static String Project_creationTime;
    public static String Project_date;
    public static String Project_dicId;
    public static String Project_dicName;
    public static String Project_errorExistingProduct;
    public static String Project_errorNotProject;
    public static String Project_extensionDic;
    public static String Project_fldId;
    public static String Project_id;
    public static String Project_knpConfig;
    public static String Project_productId;
    public static String Project_productName;
    public static String Project_projectId;
    public static String Project_projectName;
    public static String Project_systemErrorCheckDicRelation;
    public static String Project_systemErrorCheckProductRelation;
    public static String Project_systemErrorDeleteDicRelation;
    public static String Project_systemErrorDeleteProductRelation;
    public static String Project_systemErrorEntryDicInfo;
    public static String Project_systemErrorEntryDicRelation;
    public static String Project_systemErrorEntryProductRelation;
    public static String Project_systemErrorGetDicId;
    public static String Project_systemErrorGetDicInfo;
    public static String Project_systemErrorGetProductInfo;
    public static String Project_systemErrorModifyProjectName;
    public static String Project_tblId;
    public static String Project_type;
    public static String UserDic_errorOldDicInformation;
    public static String UserDic_ImportUserDic;
    public static String UserDic_saveUserDic;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }


    private Messages() {
    }
}
