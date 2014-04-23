/**
 * @version $Id: DataBaseViewContentProvider.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/10/05 15:54:59
 * @author kyohei-miyazato
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.views.db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;

import com.tida_okinawa.corona.io.PropertyUtil;
import com.tida_okinawa.corona.io.PropertyUtil.PropertyItem;
import com.tida_okinawa.corona.io.model.IClaimData;
import com.tida_okinawa.corona.io.model.ICoronaProject;
import com.tida_okinawa.corona.io.model.dic.DicType;
import com.tida_okinawa.corona.io.model.dic.ICoronaDic;
import com.tida_okinawa.corona.io.model.dic.IDependDic;
import com.tida_okinawa.corona.io.model.dic.ILabelDic;
import com.tida_okinawa.corona.io.model.dic.IPatternDic;
import com.tida_okinawa.corona.io.model.dic.IUserDic;
import com.tida_okinawa.corona.io.model.dic.PatternType;
import com.tida_okinawa.corona.io.service.IIoService;
import com.tida_okinawa.corona.io.util.CoronaIoUtils;
import com.tida_okinawa.corona.ui.views.CoronaDicComparator;

/**
 * @author kyohei-miyazato
 */
public class DataBaseViewContentProvider implements ITreeContentProvider {
    private static final Object[] EMPTY_ARRAY = new Object[0];

    private IIoService service;
    final static String CHILD_CATEGORY = "カテゴリ";

    private static final DBViewFolder projFolder = new DBViewFolder("プロジェクト");
    private static final DBViewFolder dicFolder = new DBViewFolder("辞書");
    private static final DBViewFolder udicFolder = new DBViewFolder("ユーザ用語辞書");
    private static final DBViewFolder ldicFolder = new DBViewFolder("ラベル辞書");
    private static final DBViewFolder ddicFolder = new DBViewFolder("ゆらぎ・同義語辞書");
    private static final DBViewFolder pdicFolder = new DBViewFolder("構文パターン辞書");
    private static final DBViewFolder jdicFolder = new DBViewFolder("Juman辞書");
    private static final DBViewFolder claimFolder = new DBViewFolder("問い合わせデータ");
    private static final DBViewFolder catFolder = new DBViewFolder(CHILD_CATEGORY);
    private static final DBViewFolder ptypeFolder = new DBViewFolder("パターン分類");
    private static final DBViewFolder domainFolder = new DBViewFolder("分野名");


    @Override
    public Object[] getElements(Object input) {
        service = (IIoService) input;
        List<DBViewFolder> folder = new ArrayList<DBViewFolder>();
        folder.add(projFolder);
        folder.add(dicFolder);
        folder.add(claimFolder);
        folder.add(catFolder);

        return folder.toArray();
    }


    @Override
    public Object[] getChildren(Object parent) {
        // testH25 20130806 互換性テスト 20130822
        Boolean isCoronaConnect = service.isConnect();
        if (!isCoronaConnect) {
            return EMPTY_ARRAY;/* 空の配列を返す */
        }
        // testH25 20130806 互換性テスト

        /* プロパティビュー表示対応 　フォルダ名を取得　 */
        if (parent instanceof DBViewFolder) {
            if (projFolder.equals(parent)) {
                /* プロジェクト情報を取る */
                return service.getProjects().toArray();
            }
            if (dicFolder.equals(parent)) {
                /* 存在しない辞書のフォルダは表示しない。 */
                Set<DBViewFolder> ret = new HashSet<DBViewFolder>(5, 1);
                List<ICoronaDic> dics = service.getDictionarys(ICoronaDic.class);
                for (ICoronaDic dic : dics) {
                    if (dic instanceof IUserDic) {
                        if (DicType.JUMAN.equals(((IUserDic) dic).getDicType())) {
                            ret.add(jdicFolder);
                        } else {
                            ret.add(udicFolder);
                        }
                    } else if (dic instanceof ILabelDic) {
                        ret.add(ldicFolder);
                    } else if (dic instanceof IDependDic) {
                        ret.add(ddicFolder);
                    } else if (dic instanceof IPatternDic) {
                        ret.add(pdicFolder);
                    }
                }
                return ret.toArray();
            }
            /* 各辞書フォルダ */
            if (udicFolder.equals(parent)) {
                List<ICoronaDic> dics = service.getDictionarys(IUserDic.class);
                for (Iterator<ICoronaDic> itr = dics.iterator(); itr.hasNext();) {
                    if (((IUserDic) itr.next()).getDicType().equals(DicType.JUMAN)) {
                        itr.remove();
                    }
                }
                return dics.toArray();
            } else if (jdicFolder.equals(parent)) {
                List<ICoronaDic> dics = service.getDictionarys(IUserDic.class);
                for (Iterator<ICoronaDic> itr = dics.iterator(); itr.hasNext();) {
                    if (!((IUserDic) itr.next()).getDicType().equals(DicType.JUMAN)) {
                        itr.remove();
                    }
                }
                return dics.toArray();
            } else if (ldicFolder.equals(parent)) {
                return service.getDictionarys(ILabelDic.class).toArray();
            } else if (pdicFolder.equals(parent)) {
                return service.getDictionarys(IPatternDic.class).toArray();
            } else if (ddicFolder.equals(parent)) {
                return service.getDictionarys(IDependDic.class).toArray();
            }
            if (claimFolder.equals(parent)) {
                /* 問い合わせデータの情報を取る */
                return service.getClaimDatas().toArray();
            }
            if (catFolder.equals(parent)) {
                return new Object[] { ptypeFolder, domainFolder };
            }
            if (ptypeFolder.equals(parent)) {
                /* デフォルトで入っている値を除いて表示する */
                PatternType[] types = service.getPatternTypes();
                int dst = 0;
                for (int i = 0; i < types.length; i++) {
                    PatternType type = types[i];
                    if (type.getId() > 0) {
                        types[dst++] = type;
                    }
                }
                Object[] ret = new Object[dst];
                System.arraycopy(types, 0, ret, 0, dst);
                return ret;
            } else if (domainFolder.equals(parent)) {
                return service.getCategorys().toArray();
            }
        }

        return EMPTY_ARRAY;/* 空の配列を返す */
    }


    @Override
    public Object getParent(Object element) {
        if (element instanceof ICoronaDic) {
            return dicFolder;
        } else if (element instanceof ICoronaProject) {
            return projFolder;
        } else if (element instanceof IClaimData) {
            return claimFolder;
        } else if (element instanceof PatternType) {
            return ptypeFolder;
        } else if (ptypeFolder.equals(element)) {
            return catFolder;
        } else if (domainFolder.equals(element)) {
            return catFolder;
        }
        return null;
    }


    @Override
    public boolean hasChildren(Object element) {
        /* 子供が居るかどうか。左側の矢印を付けるかどうかを決める所。 */
        /* プロパティビュー表示対応 */
        return (element instanceof DBViewFolder);
    }


    @Override
    public void dispose() {
    }


    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }


    static ViewerSorter getSorter() {
        final CoronaDicComparator comparator = new CoronaDicComparator();
        return new ViewerSorter() {
            final Integer CATEGORY_OTHER = 99;


            @Override
            public int category(Object element) {
                if (element instanceof DBViewFolder) {
                    if (jdicFolder.equals(element)) {
                        return 9;
                    } else if (udicFolder.equals(element)) {
                        return 1;
                    } else if (ldicFolder.equals(element)) {
                        return 5;
                    } else if (ddicFolder.equals(element)) {
                        return 3;
                    } else if (pdicFolder.equals(element)) {
                        return 7;
                    }
                    String folder = ((DBViewFolder) element).getFolderName();
                    if ((DataBaseViewContentProvider.CHILD_CATEGORY).equals(folder)) {
                        return CATEGORY_OTHER;
                    }
                }
                return comparator.category(element);
            }


            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                /*
                 * DBViewで、並び順が意図したとおりにならないので、テーブル名だけをソート対象にする
                 * ex) s1 と s1_2を並べた時、s1_2が上に来てしまって、ProjExplと異なってしまうことに対応
                 */
                if (e1 instanceof IClaimData) {
                    e1 = CoronaIoUtils.getTableNameSuffix(((IClaimData) e1).getTableName());
                }
                if (e2 instanceof IClaimData) {
                    e2 = CoronaIoUtils.getTableNameSuffix(((IClaimData) e2).getTableName());
                }
                return super.compare(viewer, e1, e2);
            }
        };
    }

    static class DBViewFolder implements IPropertySource {

        private String name = "";


        DBViewFolder(String name) {
            this.name = name;
        }


        public String getFolderName() {
            return this.name;
        }


        @Override
        public int hashCode() {
            return name.hashCode();
        }


        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof DBViewFolder)) {
                return false;
            }

            DBViewFolder f2 = (DBViewFolder) obj;
            if (name.equals(f2.name)) {
                return true;
            }
            return false;
        }


        /* ****************************************
         * property view
         */

        @Override
        public Object getEditableValue() {
            return null;
        }


        @Override
        public IPropertyDescriptor[] getPropertyDescriptors() {
            PropertyUtil prop = new PropertyUtil();
            IPropertyDescriptor[] descriptor = new IPropertyDescriptor[] { prop.getDescriptor(PropertyItem.PROP_NAME), };
            return descriptor;
        }


        @Override
        public Object getPropertyValue(Object id) {
            if (PropertyItem.PROP_NAME.getKey().equals(id)) {
                return getFolderName();
            }
            return PropertyUtil.DEFAULT_VALUE;
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

    }
}
