package spireQuests.quests.enbeon.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import spireQuests.quests.enbeon.powers.OnGainBlockMonsterPower;

@SpirePatch(clz = AbstractCreature.class, method = "addBlock")
public class OnGainBlockMonsterPowerPatch {
    @SpirePrefixPatch
    public static void triggerPowers(AbstractCreature __instance, int blockAmount) {
        for (AbstractPower po : __instance.powers) {
            if (po instanceof OnGainBlockMonsterPower) {
                ((OnGainBlockMonsterPower) po).onGainBlock(blockAmount);
            }
        }
    }
}
