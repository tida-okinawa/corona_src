package com.tida_okinawa.corona.correction.morphem.preference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExternalProgramSetting {
    final String path;
    final String[] opt;


    public ExternalProgramSetting(String path, String opt) {
        this.path = path;
        this.opt = opt.split(" ");
    }


    public String[] getCommandLine(String[] extOpts) {
        List<String> args = new ArrayList<String>();
        args.add(path);
        args.addAll(Arrays.asList(opt));

        if (extOpts != null) {
            args.addAll(Arrays.asList(extOpts));
        }

        return args.toArray(new String[args.size()]);
    }


    public String[] getCommandLine() {
        return getCommandLine(null);
    }
}
