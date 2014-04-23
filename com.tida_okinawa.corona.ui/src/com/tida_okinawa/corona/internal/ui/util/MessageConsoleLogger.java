/**
 * @version $Id: MessageConsoleLogger.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/08/31 18:30:11
 * @author imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.internal.ui.util;

import java.io.PrintStream;

import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.tida_okinawa.corona.common.ILogger;

public class MessageConsoleLogger implements ILogger {
    static MessageConsoleLogger instance;


    private MessageConsoleLogger() {
        /* このクラスはシングルトン */
    }


    static synchronized public MessageConsoleLogger getInstance() {
        if (instance == null) {
            instance = new MessageConsoleLogger();
        }
        return instance;
    }

    PrintStream out = createPrintStream(new Color(null, 0, 0, 0)); // 黒
    PrintStream err = createPrintStream(new Color(null, 255, 0, 0)); // 赤


    @Override
    public PrintStream getOutStream() {
        return out;
    }


    @Override
    public PrintStream getErrStream() {
        return err;
    }


    PrintStream createPrintStream(Color color) {
        MessageConsole console = getConsole("Corona");
        MessageConsoleStream mcs = console.newMessageStream();
        mcs.setColor(color);
        return new PrintStream(mcs);
    }


    MessageConsole getConsole(String name) {
        ConsolePlugin plugin = ConsolePlugin.getDefault();
        IConsoleManager manager = plugin.getConsoleManager();
        IConsole[] consoles = manager.getConsoles();
        for (IConsole console : consoles) {
            if (console.getName().equals(name)) {
                return (MessageConsole) console;
            }
        }
        // 新規に作る
        MessageConsole console = new MessageConsole(name, null);
        manager.addConsoles(new IConsole[] { console });
        return console;
    }
}
