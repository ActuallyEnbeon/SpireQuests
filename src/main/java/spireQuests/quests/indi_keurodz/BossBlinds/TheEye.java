package spireQuests.quests.indi_keurodz.BossBlinds;

import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.LoseHPAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import basemod.helpers.CardBorderGlowManager;
import spireQuests.Anniv8Mod;
import spireQuests.patches.ShowMarkedNodesOnMapPatch.ImageField;
import spireQuests.quests.indi_keurodz.BalatroQuest;
import spireQuests.quests.indi_keurodz.BalatroQuest.BossBlind;

public class TheEye {

    private static AbstractCard.CardType lastCardType = null;
    public static final String GLOW_ID = Anniv8Mod.makeID("TheEye");

    @SpirePatch2(clz = AbstractPlayer.class, method = "useCard")
    public static class EyePenaltyPatch {
        @SpirePostfixPatch
        public static void EyePenalty(AbstractPlayer __instance, AbstractCard c) {
            if (!ImageField.CheckMarks(AbstractDungeon.currMapNode, BalatroQuest.ID, BossBlind.Eye.frames))
                return;
            if (lastCardType != null && lastCardType == c.type) {
                AbstractDungeon.actionManager.addToBottom(new LoseHPAction(
                        __instance,
                        __instance,
                        3));
            }
            lastCardType = c.type;
        }
    }

    @SpirePatch2(clz = AbstractPlayer.class, method = "preBattlePrep")
    public static class AddGlowOnCombatStart {
        @SpirePostfixPatch
        public static void addGlow() {
            if (!ImageField.CheckMarks(AbstractDungeon.currMapNode, BalatroQuest.ID, BossBlind.Eye.frames))
                return;

            lastCardType = null;

            CardBorderGlowManager.addGlowInfo(new CardBorderGlowManager.GlowInfo() {
                @Override
                public boolean test(AbstractCard card) {
                    return lastCardType != null && card.type == lastCardType;
                }

                @Override
                public Color getColor(AbstractCard abstractCard) {
                    return Color.RED.cpy();
                }

                @Override
                public String glowID() {
                    return GLOW_ID;
                }
            });
        }
    }

    @SpirePatch2(clz = AbstractPlayer.class, method = "onVictory")
    public static class RemoveGlowOnCombatEnd {
        @SpirePrefixPatch
        public static void removeGlow() {
            if (!ImageField.CheckMarks(AbstractDungeon.currMapNode, BalatroQuest.ID, BossBlind.Eye.frames))
                return;

            lastCardType = null;

            CardBorderGlowManager.removeGlowInfo(GLOW_ID);
        }
    }

    @SpirePatch2(clz = GameActionManager.class, method = "callEndOfTurnActions")
    public static class ResetOnTurnEnd {
        @SpirePostfixPatch
        public static void ResetLastCardType() {
            lastCardType = null;
        }
    }
}
