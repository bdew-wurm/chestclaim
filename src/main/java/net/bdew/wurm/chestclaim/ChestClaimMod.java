package net.bdew.wurm.chestclaim;

import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.ServerStartedListener;
import org.gotti.wurmunlimited.modloader.interfaces.WurmMod;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChestClaimMod implements WurmMod, ServerStartedListener, Configurable {
    private static final Logger logger = Logger.getLogger("ChestClaim");

    public static int claimTimer = 50;
    public static int karmaReward = 500;

    public static void logException(String msg, Throwable e) {
        if (logger != null)
            logger.log(Level.SEVERE, msg, e);
    }

    public static void logWarning(String msg) {
        if (logger != null)
            logger.log(Level.WARNING, msg);
    }

    public static void logInfo(String msg) {
        if (logger != null)
            logger.log(Level.INFO, msg);
    }

    @Override
    public void configure(Properties properties) {
        claimTimer = (int) (Float.parseFloat(properties.getProperty("claimActionTime")) * 10);
        karmaReward = Integer.parseInt(properties.getProperty("karmaReward"));
        logInfo(String.format("Treasure Chest Claim mod loaded, timer = %d, reward = %d", claimTimer, karmaReward));
    }

    @Override
    public void onServerStarted() {
        ModActions.registerAction(new ChestClaimAction());
    }
}
