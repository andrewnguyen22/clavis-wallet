package build_structure;

import ui.Global;
import ui.Init;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Commands {
    /**
     * Used To Execute Sys Commands
     */
    private static final Logger LOGGER = Logger.getLogger(Commands.class.getName());
    //Lin Commands
    //TODO EDIT THESE -> JUST CALL COMMANDS AND ADD EVERYTHING IN FILES
    final static private String linStartCommand = Global.getPath() + File.separator + "start.sh";
    final static private String linGethCommand = Global.getPath() + File.separator + "geth.sh";
    final static private String linMineCommand = "x-terminal-emulator -e bash ~/BCL_CL/ethminer.sh";

    //Mac Commands
    final static private String macStartCommand = Global.getPath() + File.separator + "start.command";
    final static private String macGethCommand = Global.getPath() + File.separator + "geth.command";
    final static private String macMineCommand = "open " + Global.getPath() + File.separator + "ethminer.command";
    //Win Commands
    final static private String winStartCommand = Global.getPath() + File.separator + "start.cmd";
    final static private String winGethCommand = Global.getPath() + File.separator + "geth.cmd";
    final static private String winMineCommand = "cmd.exe /k start " + Global.getPath() +
            File.separator + "ethminer.cmd";
    final static private String winKillAllGeth = "taskkill /IM geth.exe /F";
    final static private String winKillAllEthminer = "taskkill /IM ethminer.exe /F";
    Process p = null;

    public void start() {
        LOGGER.addHandler(Global.getLog_fh());
        LOGGER.info("Running Start Command File");
        Runnable r = () -> {
            try {
                ProcessBuilder pb = null;
                if (Global.getOS().contains("mac")) {
                    /* Create the ProcessBuilder */
                    pb = new ProcessBuilder(macStartCommand);
                } else if (Global.getOS().contains("win")) {
                    pb = new ProcessBuilder(winStartCommand);
                } else if (Global.getOS().contains("lin")) {
                    pb = new ProcessBuilder(linStartCommand);
                }
                startProcess(pb);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(), e);
                e.printStackTrace();
                System.exit(1);
            }
        };
        LOGGER.info("Reordering Thread Priorities For Geth");
        reorder_threads(r);
    }

    private static void reorder_threads(Runnable r) {
        //Set Ui Thread Lower Priority Until Geth is Synced
        Global.getUiThread().setPriority(Thread.MIN_PRIORITY);
        Global.setGethThread(new Thread(r));
        Global.getGethThread().setPriority(Thread.MAX_PRIORITY);
        Global.getGethThread().start();
    }

    public void geth() {
        LOGGER.addHandler(Global.getLog_fh());
        LOGGER.info("Running Geth Command File");
        Runnable r = () -> {
            try {
                LOGGER.info("OS is " + Global.getOS());
                ProcessBuilder pb = null;
                if (Global.getOS().contains("mac")) {
                    /* Create the ProcessBuilder */
                    pb = new ProcessBuilder(macGethCommand);
                } else if (Global.getOS().contains("win")) {
                    LOGGER.info(winGethCommand);
                    pb = new ProcessBuilder(winGethCommand);
                } else if (Global.getOS().contains("lin")) {
                    LOGGER.info(linGethCommand);
                    pb = new ProcessBuilder(linGethCommand);
                }
                startProcess(pb);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(), e);
                e.printStackTrace();
                System.exit(1);
            }
        };
        //Set Ui Thread Lower Priority Until Geth is Synced
        reorder_threads(r);
    }

    public void mine() {
        LOGGER.addHandler(Global.getLog_fh());
        LOGGER.info("Running Mining_Popup Command File");
        try {
            switch (Global.getOS()) {
                case "mac":
                    p = Runtime.getRuntime().exec(macMineCommand);
                    break;
                case "windows":
                    p = Runtime.getRuntime().exec(winMineCommand);
                    break;
                case "linux":
                    p = Runtime.getRuntime().exec(linMineCommand);
            }
            Global.getAppProcesses().add(p);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void kill_geth_ethminer() {
        LOGGER.addHandler(Global.getLog_fh());
        LOGGER.info("Running Mining_Popup Command File");
        try {
            if (Global.getOS().contains("mac") || Global.getOS().contains("lin")) {
                Process p = Runtime.getRuntime().exec("killall geth");
                Process p2 = Runtime.getRuntime().exec("killall ethminer");
                synchronized (p) {
                    p.waitFor();
                }
                synchronized (p2) {
                    p2.waitFor();
                }
            } else if (Global.getOS().contains("win")) {
                Process p = Runtime.getRuntime().exec(winKillAllGeth);
                Process p2 = Runtime.getRuntime().exec(winKillAllEthminer);
                synchronized (p) {
                    p.waitFor();
                }
                synchronized (p2) {
                    p2.waitFor();
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

    private void startProcess(ProcessBuilder pb) throws IOException {
        LOGGER.addHandler(Global.getLog_fh());
        LOGGER.info("Process Builder Started");
        pb.redirectErrorStream(true);
        /* Start the process */
        Process proc = pb.start();
        Global.getAppProcesses().add(proc);
        /* Read the process's output */
        String line;
        BufferedReader in = new BufferedReader(new InputStreamReader(
                proc.getInputStream()));
        while ((line = in.readLine()) != null) {
            if(line.toLowerCase().contains("imported new chain segment")){
                Global.setBlock_sync_started(1);
            }
            LOGGER.info("GETH CLIENT: " + line);
        }
        /* Clean-up */
        proc.destroy();
        Global.getAppProcesses().add(p);
    }
}
