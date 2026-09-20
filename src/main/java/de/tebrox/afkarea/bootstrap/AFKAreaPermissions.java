package de.tebrox.afkarea.bootstrap;

public final class AFKAreaPermissions {
    private static final String ROOT = "afkarea.";
    private static final String ADMIN = ROOT + "admin.";
    private static final String COMMAND = ROOT + "command.";
    private static final String BYPASS = ROOT + "bypass.";
    private static final String STAFF = ROOT + "staff.";

    private AFKAreaPermissions() {}

    public static final String COMMAND_AFK = COMMAND + "afk";
    public static final String COMMAND_TELEPORT = COMMAND + "teleport";

    public static final String ADMIN_RELOAD = ADMIN + "reload";
    public static final String ADMIN_SELECTION = ADMIN + "selection";
    public static final String ADMIN_CREATE = ADMIN + "create";
    public static final String ADMIN_REDEFINE = ADMIN + "redefine";
    public static final String ADMIN_RENAME = ADMIN + "rename";
    public static final String ADMIN_DELETE = ADMIN + "delete";
    public static final String ADMIN_SET_TELEPORT = ADMIN + "setteleport";
    public static final String ADMIN_TP = ADMIN + "tp";
    public static final String ADMIN_LIST = ADMIN + "list";
    public static final String ADMIN_INFO = ADMIN + "info";
    public static final String ADMIN_SET_REGION = ADMIN + "setregion";
    public static final String ADMIN_SET_PRIORITY = ADMIN + "setpriority";
    public static final String ADMIN_STATUS = ADMIN + "status";
    public static final String ADMIN_HOUSEHOLD = ADMIN + "household";
    public static final String ADMIN_STATS = ADMIN + "stats";
    public static final String ADMIN_REWARD = ADMIN + "reward";
    public static final String ADMIN_DISPLAY = ADMIN + "display";
    public static final String ADMIN_INCLUDE_CHILDREN = ADMIN + "includechildren";

    public static final String STAFF_SEE_HIDDEN = STAFF + "see-hidden";

    public static final String BYPASS_AUTO_AFK = BYPASS + "auto-afk";
    public static final String BYPASS_IP_LIMIT = BYPASS + "ip-limit";
    public static final String BYPASS_REWARDS = BYPASS + "rewards";
}