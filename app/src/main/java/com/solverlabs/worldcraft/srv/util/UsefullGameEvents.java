package com.solverlabs.worldcraft.srv.util;

import java.util.HashSet;
import java.util.Set;


public class UsefullGameEvents {
    public static final Set<Byte> USEFULL_ACTIONS = new HashSet();

    static {
        USEFULL_ACTIONS.add((byte) 1);
        USEFULL_ACTIONS.add((byte) 10);
        USEFULL_ACTIONS.add((byte) 12);
        USEFULL_ACTIONS.add((byte) 14);
        USEFULL_ACTIONS.add((byte) 18);
        USEFULL_ACTIONS.add((byte) 24);
        USEFULL_ACTIONS.add((byte) 27);
        USEFULL_ACTIONS.add((byte) 30);
        USEFULL_ACTIONS.add((byte) 34);
        USEFULL_ACTIONS.add((byte) 37);
        USEFULL_ACTIONS.add((byte) 51);
    }

    public static boolean contains(byte b) {
        return USEFULL_ACTIONS.contains(Byte.valueOf(b));
    }
}
