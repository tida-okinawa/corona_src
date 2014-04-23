/**
 * @version $Id: CompType.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/29 17:55:57
 * @author kousuke-morishima
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.parsing.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author kousuke-morishima
 */
public enum CompType {

    LT(0, "<"),

    GT(1, ">"),

    LE(2, "<="),

    GE(3, ">="),

    EQ(4, "=="),

    NE(5, "!=");

    private CompType(int id, String label) {
        this.id = id;
        this.label = label;
    }

    private int id;


    public int getInt() {
        return id;
    }

    private String label;


    public String getLabel() {
        return label;
    }


    public static CompType getValue(int id) {
        if ((id >= 0) && (id < VALUES_ARRAY.length)) {
            return VALUES_ARRAY[id];
        }

        return null;
    }

    private static final CompType[] VALUES_ARRAY = new CompType[] { LT, GT, LE, GE, EQ, NE };

    public static final List<CompType> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));
}
