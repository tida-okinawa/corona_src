package com.tida_okinawa.corona.io.dam.hibernate.hql;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.tida_okinawa.corona.io.dam.hibernate.IoService;
import com.tida_okinawa.corona.io.model.ICoronaDicPri;

/**
 * @author Kamakura
 * 
 */
public class DicPriDao {

    // TODO 20131205 ここでの２個のメソッドは全て単体テストでのみ使用している。
    // 

    /**
     * 辞書プライオリティを取得する
     * 
     * @param workId
     * @param fldId
     * @return
     */
    public static List<ICoronaDicPri> getDicPrioritys(int workId, int fldId) {
        return doGetDicPri(workId, fldId, "dic_pri"); //$NON-NLS-1$
    }


    /* ****************************************
     * 直近、全部、両方で使う系
     */
    /**
     * @param workId
     * @param fieldId
     * @param tableName
     *            only dic_pri or recent_dic_pri
     * @return
     */
    private static List<ICoronaDicPri> doGetDicPri(int workId, int fieldId, String tableName) {
        List<ICoronaDicPri> ret = new ArrayList<ICoronaDicPri>();
        try {
            StringBuilder sql = new StringBuilder(128);
            sql.append("Select dic_id, inactive, priority From ").append(tableName).append(" Where id="); //$NON-NLS-1$ //$NON-NLS-2$
            sql.append(workId).append(" And fld_id=").append(fieldId).append(" Order By priority"); //$NON-NLS-1$ //$NON-NLS-2$
            Session session = IoService.getInstance().getSession();

            @SuppressWarnings("unchecked")
            List<Object[]> list = session.createSQLQuery(sql.toString()).list();
            if (list != null) {
                for (Object[] rs : list) {
                    ret.add(new CoronaDicPri((int) rs[0], (int) rs[1] == 0 ? false : true, (int) rs[2]));
                }
            }
        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
