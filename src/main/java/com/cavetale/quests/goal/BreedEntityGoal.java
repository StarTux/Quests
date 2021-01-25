package com.cavetale.quests.goal;

import com.cavetale.quests.Quest;
import com.cavetale.quests.QuestCategory;
import com.cavetale.quests.session.QuestInstance;
import com.cavetale.quests.util.Text;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;

@Data @EqualsAndHashCode(callSuper = true)
public final class BreedEntityGoal extends Goal {
    private EntityType entityType;

    @Override
    public String getDescription() {
        return description != null
            ? description
            : "Breed " + Text.toCamelCase(entityType.name().split("_"));
    }

    public boolean onEntityBreed(QuestInstance questInstance, EntityBreedEvent event) {
        if (entityType != event.getEntity().getType()) return false;
        questInstance.increaseAmount();
        return true;
    }

    @Getter
    public static final class Holder implements RegularGoalHolder {
        private final Class<? extends Goal> goalClass = BreedEntityGoal.class;
        private final Listener eventListener = new EventListener();
        private List<EntityType> entityTypes;

        public List<EntityType> getEntityTypes() {
            if (entityTypes == null) {
                entityTypes = Arrays.asList(EntityType.BEE, EntityType.CAT, EntityType.CHICKEN, EntityType.COW,
                                            EntityType.DONKEY, EntityType.FOX, EntityType.HOGLIN, EntityType.HORSE,
                                            EntityType.LLAMA, EntityType.MULE, EntityType.MUSHROOM_COW,
                                            EntityType.PANDA, EntityType.PARROT, EntityType.PIG, EntityType.RABBIT,
                                            EntityType.SHEEP, EntityType.STRIDER, EntityType.TURTLE, EntityType.WOLF);
            }
            return entityTypes;
        }

        @Override
        public Quest newDailyQuest() {
            Random random = ThreadLocalRandom.current();
            List<EntityType> list = getEntityTypes();
            EntityType entityType = list.get(random.nextInt(list.size()));
            Quest quest = Quest.newInstance();
            quest.setCategory(QuestCategory.DAILY);
            quest.setTitle(Text.toCamelCase(entityType) + " Rancher");
            BreedEntityGoal goal = (BreedEntityGoal) GoalType.BREED_ENTITY.newGoal();
            goal.setEntityType(entityType);
            goal.setAmount(1);
            quest.getGoals().add(goal);
            return quest;
        }

        @Override
        public Quest newWeeklyQuest() {
            Quest quest = Quest.newInstance();
            quest.setCategory(QuestCategory.WEEKLY);
            quest.setTitle("Rancher");
            int amount = 5;
            quest.setDescription("Breed " + amount + " different animals");
            Random random = ThreadLocalRandom.current();
            List<EntityType> list = getEntityTypes();
            Collections.shuffle(list, random);
            for (int i = 0; i < amount; i += 1) {
                EntityType entityType = list.get(i);
                BreedEntityGoal goal = (BreedEntityGoal) GoalType.BREED_ENTITY.newGoal();
                goal.setEntityType(entityType);
                goal.setAmount(1);
                quest.getGoals().add(goal);
            }
            return quest;
        }
    }

    public static final class EventListener implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        void onEntityBreed(EntityBreedEvent event) {
            if (!(event.getBreeder() instanceof Player)) return;
            Player player = (Player) event.getBreeder();
            for (QuestInstance questInstance : QuestInstance.of(player, BreedEntityGoal.class)) {
                BreedEntityGoal goal = (BreedEntityGoal) questInstance.getCurrentGoal();
                goal.onEntityBreed(questInstance, event);
            }
        }
    }
}
