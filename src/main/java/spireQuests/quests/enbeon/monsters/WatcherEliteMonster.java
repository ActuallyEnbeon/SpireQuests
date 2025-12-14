package spireQuests.quests.enbeon.monsters;

import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.purple.*;
import com.megacrit.cardcrawl.cards.tempCards.ThroughViolence;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.vfx.WallopEffect;
import com.megacrit.cardcrawl.vfx.combat.ViolentAttackEffect;
import com.megacrit.cardcrawl.vfx.stance.DivinityParticleEffect;
import com.megacrit.cardcrawl.vfx.stance.StanceAuraEffect;
import spireQuests.abstracts.AbstractSQMonster;
import spireQuests.quests.enbeon.powers.FakeDevotionPower;
import spireQuests.quests.enbeon.powers.FakeWaveOfTheHandPower;
import spireQuests.quests.enbeon.powers.InvisibleDivinityForMonsterPower;
import spireQuests.quests.gk.vfx.FakePlayCardEffect;
import spireQuests.util.Wiz;

import static spireQuests.Anniv8Mod.makeID;

public class WatcherEliteMonster extends AbstractSQMonster {
    public static final String ID = makeID(WatcherEliteMonster.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;

    private static final Byte DEVOTION = 0, REACH_HEAVEN = 1, WALLOP = 2, WAVE_PROTECT = 3, THROUGH_VIOLENCE = 4;

    private int devotionAmt = 2;
    private int waveAmt = 1;
    private boolean hasThroughViolence = false;
    private float particleTimer = 0.0f;
    private float auraTimer = 0.0f;
    private long sfxId;

    private final Bone eyeBone;
    protected Skeleton eyeSkeleton;
    public AnimationState eyeState;
    protected AnimationStateData eyeStateData;
    protected TextureAtlas eyeAtlas = null;

    public WatcherEliteMonster() {
        this(0, 0);
    }

    public WatcherEliteMonster(float x, float y) {
        super(NAME, ID, 80, -4f, -16f, 220f, 290f, null, x, y);
        type = EnemyType.ELITE;

        setHp(calcAscensionTankiness(80), calcAscensionTankiness(87));
        addMove(DEVOTION, Intent.BUFF);
        addMove(REACH_HEAVEN, Intent.ATTACK, calcAscensionDamage(10));
        addMove(WALLOP, Intent.ATTACK_DEFEND, calcAscensionDamage(9));
        addMove(WAVE_PROTECT, Intent.DEFEND_DEBUFF);
        addMove(THROUGH_VIOLENCE, Intent.ATTACK, calcAscensionDamage(20));

        devotionAmt = calcAscensionSpecial(devotionAmt);
        waveAmt = calcAscensionSpecial(waveAmt);

        loadAnimation("images/characters/watcher/idle/skeleton.atlas",
                "images/characters/watcher/idle/skeleton.json",
                1f);
        loadEyeAnimation();
        AnimationState.TrackEntry e = state.setAnimation(0, "Idle", true);
        stateData.setMix("Hit", "Idle", 0.1f);
        e.setTimeScale(0.6f);
        this.eyeBone = this.skeleton.findBone("eye_anchor");

        flipHorizontal = true;
    }

    private void loadEyeAnimation() {
        this.eyeAtlas = new TextureAtlas(Gdx.files.internal("images/characters/watcher/eye_anim/skeleton.atlas"));
        SkeletonJson json = new SkeletonJson(this.eyeAtlas);
        json.setScale(Settings.scale);
        SkeletonData skeletonData = json.readSkeletonData(Gdx.files
                .internal("images/characters/watcher/eye_anim/skeleton.json"));
        this.eyeSkeleton = new Skeleton(skeletonData);
        this.eyeSkeleton.setColor(Color.WHITE);
        this.eyeStateData = new AnimationStateData(skeletonData);
        this.eyeState = new AnimationState(this.eyeStateData);
        this.eyeStateData.setDefaultMix(0.2F);
        this.eyeState.setAnimation(0, "None", true);
    }

    @Override
    public void takeTurn() {
        DamageInfo info = new DamageInfo(this, moves.get(nextMove).baseDamage, DamageInfo.DamageType.NORMAL);
        info.applyPowers(this, AbstractDungeon.player);
        switch (nextMove) {
            case 0: // Devotion
                doFakePlay(new Devotion(), 18);
                Wiz.atb(new AbstractGameAction() {
                    public void update() {
                        useFastShakeAnimation(0.25f);
                        isDone = true;
                    }
                });
                addToBot(new ApplyPowerAction(this, this, new FakeDevotionPower(this, devotionAmt)));
                break;
            case 1: // Reach Heaven
                doFakePlay(new ReachHeaven(), 3);
                Wiz.atb(new AbstractGameAction() {
                    public void update() {
                        useFastAttackAnimation();
                        hasThroughViolence = true;
                        isDone = true;
                    }
                });
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.SLASH_HEAVY));
                break;
            case 2: // Wallop
                doFakePlay(new Wallop(), 3);
                useSlowAttackAnimation();
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                addToBot(new AbstractGameAction() {
                    @Override
                    public void update() {
                        AbstractPlayer p = AbstractDungeon.player;
                        if (p.lastDamageTaken > 0) {
                            addToTop(new GainBlockAction(WatcherEliteMonster.this, p.lastDamageTaken));
                            if (p.hb != null) {
                                addToTop(new VFXAction(new WallopEffect(p.lastDamageTaken, p.hb.cX, p.hb.cY)));
                            }
                        }
                        this.isDone = true;
                    }
                });
                break;
            case 3: // Wave of the Hand / Protect
                doFakePlay(new WaveOfTheHand(), 18);
                addToBot(new ApplyPowerAction(this, this, new FakeWaveOfTheHandPower(this, waveAmt)));
                doFakePlay(new Protect(), 8);
                addToBot(new GainBlockAction(this, calcAscensionTankiness(12)));
                break;
            case 4: // Through Violence
                doFakePlay(new ThroughViolence(), 3);
                Wiz.atb(new AbstractGameAction() {
                    public void update() {
                        useSlowAttackAnimation();
                        hasThroughViolence = false;
                        isDone = true;
                    }
                });
                Wiz.vfx(new ViolentAttackEffect(Wiz.adp().hb.cX, Wiz.adp().hb.cY, Color.VIOLET), 0.4F);
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.SLASH_HEAVY));
        }
        addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(int i) {
        // Start with Devotion, then:
        //  - If she's just used Devotion or Through Violence, use Reach Heaven
        //  - If she's just used Wallop, use Wave of the Hand / Protect
        //  - Otherwise, use Wallop
        // There's special behaviour when she enters Divinity, see the below method
        if (firstMove) {
            setMoveShortcut(DEVOTION, MOVES[DEVOTION]);
            firstMove = false;
        } else if (lastMove(DEVOTION) || lastMove(THROUGH_VIOLENCE)) {
            setMoveShortcut(REACH_HEAVEN, MOVES[REACH_HEAVEN]);
        } else if (lastMove(WALLOP)) {
            setMoveShortcut(WAVE_PROTECT, MOVES[WAVE_PROTECT]);
        } else {
            setMoveShortcut(WALLOP, MOVES[WALLOP]);
        }
    }

    // This is called in InvisibleDivinityForMonsterPower
    public void prepareThroughViolence() {
        // When she enters Divinity,
        //  - If Through Violence is available, use it
        //  - Otherwise, use Reach Heaven
        if (hasThroughViolence) {
            setMoveShortcut(THROUGH_VIOLENCE, MOVES[THROUGH_VIOLENCE]);
            addToBot(new SetMoveAction(this, MOVES[THROUGH_VIOLENCE], THROUGH_VIOLENCE, Intent.ATTACK));
        } else {
            setMoveShortcut(REACH_HEAVEN, MOVES[REACH_HEAVEN]);
            addToBot(new SetMoveAction(this, MOVES[REACH_HEAVEN], REACH_HEAVEN, Intent.ATTACK));
        }
        createIntent();
    }

    @Override
    public void damage(DamageInfo info) {
        if (info.owner != null && info.type != DamageInfo.DamageType.THORNS && info.output - currentBlock > 0) {
            AnimationState.TrackEntry e = state.setAnimation(0, "Hit", false);
            state.addAnimation(0, "Idle", true, 0f);
            e.setTimeScale(0.6f);
        }

        super.damage(info);
    }

    private void doFakePlay(AbstractCard c, int ascLevelToUpgrade) {
        if (AbstractDungeon.ascensionLevel >= ascLevelToUpgrade) c.upgrade();
        Wiz.vfx(new FakePlayCardEffect(this, c));
    }

    private boolean isInDivinity() {
        return hasPower(InvisibleDivinityForMonsterPower.POWER_ID);
    }

    // Animation & sound logic

    @Override
    public void update() {
        super.update();
        // Do Divinity particles
        if (isInDivinity()) {
            if (!Settings.DISABLE_EFFECTS) {
                this.particleTimer -= Gdx.graphics.getDeltaTime();
                if (this.particleTimer < 0.0F) {
                    this.particleTimer = 0.2F;
                    AbstractDungeon.effectsQueue.add(makeDivinityParticleEffect());
                }
            }
            this.auraTimer -= Gdx.graphics.getDeltaTime();
            if (this.auraTimer < 0.0F) {
                this.auraTimer = MathUtils.random(0.45F, 0.55F);
                AbstractDungeon.effectsQueue.add(makeDivinityAuraEffect());
            }
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        super.render(sb);
        // Render staff eye
        this.eyeState.update(Gdx.graphics.getDeltaTime());
        this.eyeState.apply(this.eyeSkeleton);
        this.eyeSkeleton.updateWorldTransform();
        this.eyeSkeleton.setPosition(this.skeleton.getX() + this.eyeBone.getWorldX(), this.skeleton.getY() + this.eyeBone.getWorldY());
        this.eyeSkeleton.setColor(this.tint.color);
        this.eyeSkeleton.setFlip(this.flipHorizontal, this.flipVertical);
        sb.end();
        CardCrawlGame.psb.begin();
        sr.draw(CardCrawlGame.psb, this.eyeSkeleton);
        CardCrawlGame.psb.end();
        sb.begin();
    }

    private DivinityParticleEffect makeDivinityParticleEffect() {
        DivinityParticleEffect effect = new DivinityParticleEffect();
        ReflectionHacks.setPrivate(effect, DivinityParticleEffect.class, "x",
                this.hb.cX + MathUtils.random(-this.hb.width / 2.0F - 50.0F * Settings.scale, this.hb.width / 2.0F + 50.0F * Settings.scale)
        );
        ReflectionHacks.setPrivate(effect, DivinityParticleEffect.class, "y",
                this.hb.cY + MathUtils.random(-this.hb.height / 2.0F + 10.0F * Settings.scale, this.hb.height / 2.0F - 20.0F * Settings.scale)
        );
        return effect;
    }

    private StanceAuraEffect makeDivinityAuraEffect() {
        StanceAuraEffect effect = new StanceAuraEffect("Divinity");
        ReflectionHacks.setPrivate(effect, StanceAuraEffect.class, "x",
                this.hb.cX + MathUtils.random(-this.hb.width / 16.0F, this.hb.width / 16.0F)
        );
        ReflectionHacks.setPrivate(effect, StanceAuraEffect.class, "y",
                this.hb.cY + MathUtils.random(-this.hb.height / 16.0F, this.hb.height / 16.0F)
        );
        return effect;
    }

    public void startIdleSfx() {
        sfxId = CardCrawlGame.sound.playAndLoop("STANCE_LOOP_DIVINITY");
    }

    public void stopIdleSfx() {
        if (sfxId != -1L) {
            CardCrawlGame.sound.stop("STANCE_LOOP_DIVINITY", sfxId);
            sfxId = -1L;
        }
    }
}
