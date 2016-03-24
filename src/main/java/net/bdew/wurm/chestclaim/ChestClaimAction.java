package net.bdew.wurm.chestclaim;

import com.wurmonline.server.Items;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemSettings;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.sounds.SoundPlayer;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Collections;
import java.util.List;

public class ChestClaimAction implements ModAction, BehaviourProvider, ActionPerformer {
    private final ActionEntry actionEntry;
    private final short actionId;

    public ChestClaimAction() {
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(actionId, "Claim", "claiming", new int[]{
                6 /* ACTION_TYPE_NOMOVE */,
                48 /* ACTION_TYPE_ENEMY_ALWAYS */,
                37 /* ACTION_TYPE_NEVER_USE_ACTIVE_ITEM */
        });
        ModActions.registerAction(actionEntry);
    }

    @Override
    public BehaviourProvider getBehaviourProvider() {
        return this;
    }

    @Override
    public ActionPerformer getActionPerformer() {
        return this;
    }

    @Override
    public short getActionId() {
        return actionId;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
        if (performer instanceof Player && target != null && target.getTemplateId() == ItemList.treasureChest && target.isLocked() && target.getAuxData() < 100)
            return Collections.singletonList(actionEntry);
        else
            return null;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
        return getBehavioursFor(performer, null, target);
    }

    @Override
    public boolean action(Action action, Creature performer, Item source, Item target, short num, float counter) {
        return action(action, performer, target, num, counter);
    }


    @Override
    public boolean action(Action action, Creature performer, Item target, short num, float counter) {
        try {
            Communicator comm = performer.getCommunicator();
            if (counter == 1.0f) {
                comm.sendNormalServerMessage(String.format("You start claiming the %s.", target.getName()));
                action.setTimeLeft(ChestClaimMod.claimTimer);
                performer.sendActionControl("claiming", true, ChestClaimMod.claimTimer);

            } else {
                int time = action.getTimeLeft();
                if (target.getAuxData() >= 100 || !target.isLocked()) {
                    performer.stopCurrentAction();
                    comm.sendNormalServerMessage(String.format("The %s has already been claimed by another player.", target.getName()));
                } else if (counter * 10.0f > time) {
                    target.setAuxData((byte) 100);
                    Items.destroyItem(target.getLockId());
                    target.setLockId(-10L);
                    ItemSettings.remove(target.getWurmId());
                    SoundPlayer.playSound("sound.object.lockunlock", target, 0.2f);
                    comm.sendOpenInventoryWindow(target.getWurmId(), target.getName());
                    target.sendContainedItems(target.getWurmId(), performer);
                    target.addWatcher(target.getWurmId(), performer);
                    performer.getCommunicator().sendNormalServerMessage(String.format("You gain %d karma for claiming the %s as your own.", ChestClaimMod.karmaReward, target.getName()));
                    performer.modifyKarma(ChestClaimMod.karmaReward);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            ChestClaimMod.logException("Claim action error", e);
            return true;
        }
    }
}
