package com.github.hstyi.restart4j;


import org.junit.jupiter.api.Test;

import java.io.IOException;

class RestarterTest {

    private static final String os = System.getProperty("os.name").toLowerCase();
    private static final boolean isWin = os.contains("win");
    private static final boolean isMac = os.contains("mac");
    private static final boolean isUnix = os.contains("nix") || os.contains("nux");

    @Test
    public void test() throws IOException {
        if (isWin) {
            Restarter.restart(new String[]{"notepad"});
        } else if (isMac) {
            Restarter.restart(new String[]{"open", "-a", "TextEdit"});
        } else if (isUnix) {
            Restarter.restart(new String[]{"xdg-open", "http://google.com"});
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + os);
        }
    }
}