package com.cavetale.quests;

import com.cavetale.quests.goal.Goal;
import com.cavetale.quests.goal.GoalType;
import com.cavetale.quests.util.Json;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public final class Quest {
    private Tag tag;
    private List<Goal> goals;

    public Quest() { }

    @Data
    public static class Tag {
        private QuestCategory category;
        private int index;
        private String title;
        private String description;
        private QuestReward reward;
        /**
         * Optional; Keys of comleted keyed quests are saved in the
         * player's completed quests storage.
         */
        private String key;
        private Long expiry;
    }

    public static Quest newInstance() {
        Quest quest = new Quest();
        quest.tag = new Tag();
        quest.tag.reward = new QuestReward();
        quest.goals = new ArrayList<>();
        return quest;
    }

    public String serialize() {
        return Json.serialize(this);
    }

    /**
     * Quest holds generic data; consistency is maintained by this method.
     *
     * @throws RuntimeException if something goes wrong.
     */
    public static Quest deserialize(String in) {
        Quest quest = Quest.newInstance();
        quest.setGoals(new ArrayList<>());
        JsonElement elem = Json.PARSER.parse(in);
        JsonObject root = elem.getAsJsonObject();
        // goals
        JsonArray goals = root.getAsJsonArray("goals");
        for (JsonElement goalElem : goals) {
            JsonObject goalObj = goalElem.getAsJsonObject();
            String typeName = goalObj.getAsJsonPrimitive("type").getAsString();
            GoalType goalType = GoalType.valueOf(typeName);
            Goal goal = goalType.holder.deserializeGoal(goalObj);
            quest.goals.add(goal);
        }
        // tag
        if (!root.has("tag")) {
            quest.tag = new Tag();
        } else {
            quest.tag = Json.GSON.fromJson(root.get("tag"), Quest.Tag.class);
        }
        return quest;
    }

    public Quest clone() {
        return deserialize(serialize());
    }

    public QuestCategory getCategory() {
        return tag != null && tag.category != null
            ? tag.category
            : QuestCategory.DEFAULT;
    }

    public void setCategory(QuestCategory category) {
        tag.category = category;
    }

    public int getIndex() {
        return tag != null
            ? tag.index
            : 0;
    }

    public void setIndex(int index) {
        tag.index = index;
    }

    public String getTitle() {
        return tag.title != null ? tag.title : "Quest";
    }

    public void setTitle(String title) {
        tag.title = title;
    }

    public String getDescription() {
        return tag.description;
    }

    public void setDescription(String description) {
        tag.description = description;
    }

    public QuestReward getReward() {
        return tag.reward;
    }
}
