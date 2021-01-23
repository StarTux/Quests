package com.cavetale.quests.goal;

import com.cavetale.quests.Quest;
import com.cavetale.quests.QuestCategory;
import com.cavetale.quests.session.QuestInstance;
import com.cavetale.quests.util.Entities;
import com.cavetale.quests.util.Text;
import com.destroystokyo.paper.MaterialTags;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

/**
 * Kill a type of (monster) mob. Optionally with a specific weapon.
 */
@Data @EqualsAndHashCode(callSuper = true)
public final class KillMobGoal extends Goal {
    private EntityType entityType;
    private WeaponType weaponType;

    public enum WeaponType {
        SWORD,
        AXE,
        MELEE,
        ARROW,
        BOW,
        CROSSBOW,
        TRIDENT;

        public String getVerb() {
            switch (this) {
            case SWORD: case AXE: case MELEE: return "Slay";
            case ARROW: case BOW: case CROSSBOW: case TRIDENT: return "Hunt";
            default: return "Kill";
            }
        }

        public String getKiller() {
            switch (this) {
            case SWORD: case AXE: case MELEE: return "Slayer";
            case ARROW: case BOW: case CROSSBOW: case TRIDENT: return "Hunter";
            default: return "Killer";
            }
        }

        public String getHumanName() {
            return Text.toCamelCase(this);
        }
    }

    @Override
    public String getDescription() {
        return description != null
            ? description
            : ((weaponType != null ? weaponType.getVerb() : "Kill")
               + " " + Entities.singularOrPlural(entityType, amount)
               + (weaponType != null ? " via " + weaponType.getHumanName() : ""));
    }

    public boolean onDeadlyDamage(QuestInstance questInstance, EntityDamageByEntityEvent event, Projectile projectile) {
        if (entityType != event.getEntity().getType()) return false;
        if (weaponType != null && !checkWeaponType(questInstance, event, projectile)) return false;
        questInstance.increaseAmount();
        return true;
    }

    static boolean isMeleeAttack(DamageCause damageCause) {
        switch (damageCause) {
        case ENTITY_ATTACK:
        case ENTITY_SWEEP_ATTACK:
            return true;
        default:
            return false;
        }
    }

    public boolean checkWeaponType(QuestInstance questInstance, EntityDamageByEntityEvent event, Projectile projectile) {
        DamageCause cause = event.getCause();
        if (weaponType == null) return true;
        switch (weaponType) {
        case SWORD: {
            ItemStack hand = questInstance.getSession().getPlayer().getInventory().getItemInMainHand();
            return isMeleeAttack(cause) && hand != null && MaterialTags.SWORDS.isTagged(hand);
        }
        case AXE: {
            ItemStack hand = questInstance.getSession().getPlayer().getInventory().getItemInMainHand();
            return isMeleeAttack(cause) && hand != null && MaterialTags.AXES.isTagged(hand);
        }
        case MELEE:
            return isMeleeAttack(cause);
        case ARROW:
            return cause == DamageCause.PROJECTILE && (projectile instanceof Arrow || projectile instanceof SpectralArrow);
        case BOW:
            return cause == DamageCause.PROJECTILE
                && projectile instanceof AbstractArrow && !((AbstractArrow) projectile).isShotFromCrossbow();
        case CROSSBOW:
            return cause == DamageCause.PROJECTILE
                && projectile instanceof AbstractArrow && ((AbstractArrow) projectile).isShotFromCrossbow();
        case TRIDENT:
            return cause == DamageCause.PROJECTILE && projectile instanceof Trident;
        default:
            return false;
        }
    }

    @Getter
    public static final class Holder implements RegularGoalHolder {
        private final Class<? extends Goal> goalClass = KillMobGoal.class;
        private final Listener eventListener = new EventListener();

        public List<EntityType> getEntityTypes() {
            return Arrays.asList(EntityType.BLAZE, EntityType.ZOMBIE, EntityType.CREEPER, EntityType.DROWNED,
                                 EntityType.GHAST, EntityType.GUARDIAN, EntityType.HOGLIN, EntityType.HUSK,
                                 EntityType.MAGMA_CUBE, EntityType.SKELETON, EntityType.SLIME, EntityType.SPIDER,
                                 EntityType.CAVE_SPIDER, EntityType.STRAY, EntityType.WITCH,
                                 EntityType.WITHER_SKELETON, EntityType.ZOGLIN, EntityType.ZOMBIE_VILLAGER);
        }

        public WeaponType randomWeaponType() {
            WeaponType[] array = WeaponType.values();
            return array[ThreadLocalRandom.current().nextInt(array.length)];
        }

        @Override
        public Quest newDailyQuest() {
            Random random = ThreadLocalRandom.current();
            List<EntityType> list = getEntityTypes();
            EntityType entityType = list.get(random.nextInt(list.size()));
            String entityName = Entities.singular(entityType);
            WeaponType weaponType = randomWeaponType();
            Quest quest = Quest.newInstance();
            quest.setCategory(QuestCategory.DAILY);
            quest.setTitle(entityName + " " + weaponType.getKiller());
            KillMobGoal goal = (KillMobGoal) GoalType.KILL_MOB.newGoal();
            goal.setEntityType(entityType);
            goal.setAmount(1 + random.nextInt(7));
            goal.setWeaponType(weaponType);
            quest.getGoals().add(goal);
            return quest;
        }

        @Override
        public Quest newWeeklyQuest() {
            Random random = ThreadLocalRandom.current();
            List<EntityType> list = getEntityTypes();
            Collections.shuffle(list, random);
            WeaponType weaponType = randomWeaponType();
            Quest quest = Quest.newInstance();
            quest.setCategory(QuestCategory.WEEKLY);
            quest.setTitle(weaponType.getHumanName() + " Training");
            int amount = 5;
            quest.setDescription(weaponType.getVerb() + " " + amount
                                 + " different mobs via " + weaponType.getHumanName());
            for (int i = 0; i < amount; i += 1) {
                KillMobGoal goal = (KillMobGoal) GoalType.KILL_MOB.newGoal();
                EntityType entityType = list.get(i);
                String entityName = Entities.singular(entityType);
                goal.setEntityType(entityType);
                goal.setAmount(1 + random.nextInt(3));
                goal.setWeaponType(weaponType);
                quest.getGoals().add(goal);
            }
            return quest;
        }
    }

    public static final class EventListener implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
            if (!(event.getEntity() instanceof Mob)) return;
            Mob mob = (Mob) event.getEntity();
            if (event.getFinalDamage() < mob.getHealth()) return;
            final Player player;
            final Projectile projectile;
            switch (event.getCause()) {
            case ENTITY_ATTACK:
            case ENTITY_SWEEP_ATTACK:
                if (!(event.getDamager() instanceof Player)) return;
                player = (Player) event.getDamager();
                projectile = null;
                break;
            case PROJECTILE:
                if (!(event.getDamager() instanceof Projectile)) return;
                projectile = (Projectile) event.getDamager();
                if (!(projectile.getShooter() instanceof Player)) return;
                player = (Player) projectile.getShooter();
                break;
            default: return;
            }
            for (QuestInstance questInstance : QuestInstance.of(player, KillMobGoal.class)) {
                KillMobGoal goal = (KillMobGoal) questInstance.getCurrentGoal();
                goal.onDeadlyDamage(questInstance, event, projectile);
            }
        }
    }
}
