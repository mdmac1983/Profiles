package com.tj.phoneguide;

/** Builds the line that switches the launcher's accessibility helper on WITHOUT switching off a service that is already on. */
final class A11yLine {
    static final String COMPONENT = "app.orionmd.profiles/com.tj.launcher.TjAccessibilityService";
    private A11yLine() {}

    static String build(String printed) {
        String p = printed == null ? "" : printed.trim();
        String list = (p.isEmpty() || p.equalsIgnoreCase("null")) ? COMPONENT : (p.contains(COMPONENT) ? p : p + ":" + COMPONENT);
        return "settings put secure enabled_accessibility_services " + list;
    }
}
