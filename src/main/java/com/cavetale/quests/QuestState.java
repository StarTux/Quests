package com.cavetale.quests;

import com.cavetale.quests.goal.Goal;
import com.cavetale.quests.goal.Progress;
import com.cavetale.quests.util.Json;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Data;

/**
 * Maintain the current completion state of a quest for one
 * player. Will be (de)serialized.
 */
@Data
public final class QuestState {
    protected Tag tag;
    protected List<Progress> goals;

    public QuestState() { }

    @Data
    public static final class Tag {
        protected boolean accepted;
        protected int currentGoal;
    }

    public void prepare(Quest quest) {
        tag = new Tag();
        goals = new ArrayList<>(quest.getGoals().size());
        for (Goal goal : quest.getGoals()) {
            Progress progress = goal.getHolder().newProgress();
            goals.add(progress);
        }
    }

    public String serialize() {
        return Json.serialize(this);
    }

    public static QuestState deserialize(String in, Quest quest) {
        QuestState questState = new QuestState();
        JsonElement elem = Json.PARSER.parse(in);
        JsonObject root = elem.getAsJsonObject();
        // tag
        questState.tag = Json.GSON.fromJson(root.get("tag"), Tag.class);
        // progress
        JsonArray array = root.getAsJsonArray("goals");
        questState.goals = new ArrayList<>(quest.getGoals().size());
        Iterator<JsonElement> iter = array.iterator();
        for (Goal goal : quest.getGoals()) {
            Progress progress;
            if (iter.hasNext()) {
                JsonElement it = iter.next();
                progress = goal.getHolder().deserializeProgress(it);
            } else {
                progress = goal.getHolder().newProgress();
            }
            questState.goals.add(progress);
        }
        return questState;
    }

    public Progress getCurrentProgress() {
        return goals.get(tag.currentGoal);
    }
}
