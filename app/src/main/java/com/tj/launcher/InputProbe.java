package com.tj.launcher;

import java.lang.reflect.Method;

/**
 * Q2 RESEARCH ONLY (never run by the app itself): started by an adb line as the shell user, it asks the system's input service for an input MONITOR (a channel that would see every key and touch
 * event, including the side key, before anything reacts). If the shell user is allowed, a later build could show the power-menu card at the moment the side key has been held long enough, BEFORE
 * the real menu appears. Prints what happened; changes nothing.
 */
public final class InputProbe {
    private InputProbe() {}

    public static void main(String[] args) {
        try {
            Class<?> sm = Class.forName("android.os.ServiceManager");
            android.os.IBinder b = (android.os.IBinder) sm.getMethod("getService", String.class).invoke(null, "input");
            Class<?> stub = Class.forName("android.hardware.input.IInputManager$Stub");
            Object im = stub.getMethod("asInterface", android.os.IBinder.class).invoke(null, b);
            boolean any = false;
            for (Method m : im.getClass().getMethods()) {
                if (!m.getName().startsWith("monitor")) continue;
                any = true;
                System.out.println("input service method: " + m);
                try {
                    Object r;
                    if (m.getParameterTypes().length == 1) r = m.invoke(im, "TjProbe");
                    else if (m.getParameterTypes().length == 2) r = m.invoke(im, "TjProbe", 0);
                    else { System.out.println("  (skipped: unexpected parameters)"); continue; }
                    System.out.println("  RESULT: ALLOWED, returned " + r);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    System.out.println("  RESULT: REFUSED: " + e.getCause());
                }
            }
            if (!any) System.out.println("RESULT: this Android has no monitor* method on the input service");
        } catch (Throwable t) {
            System.out.println("RESULT: probe failed: " + t);
        }
    }
}
