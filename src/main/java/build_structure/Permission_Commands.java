package build_structure;

import ui.Global;
import ui.Init;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Permission_Commands {
    /**
     * Provides Permissions For Necessary Files Withing BCL_CL
     */
    private static final Logger LOGGER = Logger.getLogger(Permission_Commands.class.getName());
    final private static String macPerm = "chmod -R 777 " + System.getProperty("user.home") + File.separator + "BCL_CL";
    final private static String winPerm = "icacls " + Global.getPath() + "/grant Users:F";

    public static void permission() {
        LOGGER.addHandler(Global.getLog_fh());
        LOGGER.info("Changing Permissions of BCL_CL Folder");
        try {
            if (Global.getOS().contains("mac")||Global.getOS().contains("lin")) {
                Process p = Runtime.getRuntime().exec(macPerm);
                synchronized (p) {
                    p.waitFor();
                }
                Global.getAppProcesses().add(p);
            } else if (Global.getOS().contains("win")) {//Should grant permissions for whole folder
                LOGGER.info(winPerm);
                Process p = Runtime.getRuntime().exec(winPerm);
                synchronized (p) {
                    p.waitFor();
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
            e.printStackTrace();
            System.exit(1);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
            e.printStackTrace();
        }
    }
}
