package com.tj.launcher;

import android.os.Binder;
import android.os.IBinder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * RESEARCH helper (Tj, 2026-11-05: "think outside the box ... adb"): run as the SHELL user by the emulator test (`app_process ... com.tj.launcher.ShellProbe <mode> ...`) to find out what the shell's
 * identity may do on Android 10. It prints one line per question and changes nothing that lasts: every flag it sets belongs to a Binder token of THIS process, and Android removes the flags
 * when the process ends. Not used by the app itself.
 *   statusbar <disable1> <disable2> <seconds>   set StatusBarManager disable flags (hex ok) for N seconds, print what Android answered, then clear them
 */
public final class ShellProbe {
    private ShellProbe() {}

    public static void main(String[] args) throws Exception {
        String mode = args.length > 0 ? args[0] : "help";
        if ("statusbar".equals(mode)) statusbar(args);
        else System.out.println("ShellProbe: modes: statusbar <disable1> <disable2> <seconds>");
    }

    private static Object service(String name, String stub) throws Exception {
        Class<?> sm = Class.forName("android.os.ServiceManager");
        IBinder b = (IBinder) sm.getMethod("getService", String.class).invoke(null, name);
        if (b == null) throw new IllegalStateException("no service " + name);
        return Class.forName(stub).getMethod("asInterface", IBinder.class).invoke(null, b);
    }

    private static String call(Object o, String method, Class<?>[] types, Object... a) {
        try {
            Method m = o.getClass().getMethod(method, types);
            Object r = m.invoke(o, a);
            return "OK" + (r instanceof int[] ? " " + java.util.Arrays.toString((int[]) r) : (r == null ? "" : " " + r));
        } catch (InvocationTargetException e) {
            return "REFUSED " + e.getCause();
        } catch (Throwable t) {
            return "FAILED " + t;
        }
    }

    private static void statusbar(String[] a) throws Exception {
        int d1 = Integer.decode(a[1]), d2 = Integer.decode(a[2]), secs = Integer.parseInt(a[3]);
        Object sb = service("statusbar", "com.android.internal.statusbar.IStatusBarService$Stub");
        IBinder token = new Binder();
        Class<?>[] t3 = { int.class, IBinder.class, String.class };
        System.out.println("uid=" + android.os.Process.myUid());
        if (d1 != 0) System.out.println("disable(0x" + Integer.toHexString(d1) + ") -> " + call(sb, "disable", t3, d1, token, "com.android.shell"));
        if (d2 != 0) System.out.println("disable2(0x" + Integer.toHexString(d2) + ") -> " + call(sb, "disable2", t3, d2, token, "com.android.shell"));
        System.out.println("getDisableFlags -> " + call(sb, "getDisableFlags", new Class<?>[] { IBinder.class, int.class }, token, 0));
        System.out.flush();
        Thread.sleep(secs * 1000L);
        call(sb, "disable", t3, 0, token, "com.android.shell");
        call(sb, "disable2", t3, 0, token, "com.android.shell");
        System.out.println("cleared");
    }
}
