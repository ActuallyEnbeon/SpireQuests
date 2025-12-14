package spireQuests.quests.enbeon.powers;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.WeakPower;
import spireQuests.abstracts.AbstractSQPower;

import static spireQuests.Anniv8Mod.makeID;

public class FakeWaveOfTheHandPower extends AbstractSQPower {
    public static String POWER_ID = makeID(FakeWaveOfTheHandPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public FakeWaveOfTheHandPower(AbstractCreature owner, int amount) {
        super(POWER_ID, NAME, "enbeon", PowerType.BUFF, false, owner, amount);
        updateDescription();
        this.loadRegion("wave_of_the_hand");
    }

    public void atEndOfRound() {
        this.addToBot(new RemoveSpecificPowerAction(owner, owner, this));
    }

    @Override
    public void updateDescription() {
        description = DESCRIPTIONS[0] + amount + DESCRIPTIONS[1];
    }
}
