package com.solverlabs.worldcraft.srv;

import com.solverlabs.worldcraft.srv.util.OSDetector;


public class Consts {
    public static final boolean DEBUG;

    static {
        if (OSDetector.isServer()) {
        }
        DEBUG = false;
    }
}
