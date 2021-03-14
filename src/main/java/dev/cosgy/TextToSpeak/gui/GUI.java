////////////////////////////////////////////////////////////////////////////////
//  Copyright 2021 Cosgy Dev                                                   /
//                                                                             /
//     Licensed under the Apache License, Version 2.0 (the "License");         /
//     you may not use this file except in compliance with the License.        /
//     You may obtain a copy of the License at                                 /
//                                                                             /
//        http://www.apache.org/licenses/LICENSE-2.0                           /
//                                                                             /
//     Unless required by applicable law or agreed to in writing, software     /
//     distributed under the License is distributed on an "AS IS" BASIS,       /
//     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied./
//     See the License for the specific language governing permissions and     /
//     limitations under the License.                                          /
////////////////////////////////////////////////////////////////////////////////

package dev.cosgy.TextToSpeak.gui;

import dev.cosgy.TextToSpeak.Bot;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ResourceBundle;

/**
 * @author Kosugi_kun
 */
public class GUI extends JFrame {
    private final ConsolePanel console;
    private final Bot bot;

    public GUI(Bot bot) {
        super();
        this.bot = bot;
        console = new ConsolePanel();
    }

    public void init() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle(bot.GetLang().getString("appName"));
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("コンソール", console);
        getContentPane().add(tabs);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) { /* unused */ }

            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    bot.shutdown();
                } catch (Exception ex) {
                    System.exit(0);
                }
            }

            @Override
            public void windowClosed(WindowEvent e) { /* unused */ }

            @Override
            public void windowIconified(WindowEvent e) { /* unused */ }

            @Override
            public void windowDeiconified(WindowEvent e) { /* unused */ }

            @Override
            public void windowActivated(WindowEvent e) { /* unused */ }

            @Override
            public void windowDeactivated(WindowEvent e) { /* unused */ }
        });
    }
}
