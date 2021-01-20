package com.cavetale.quests.goal;

import com.cavetale.quests.Quest;
import com.cavetale.quests.util.Json;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Data;

/**
 * Simple storage for quest progress.
 * Can be subclassed.
 */
@Data
public class Progress {
    protected int goalIndex;
    protected int amount;
    protected boolean accepted;
    protected boolean completed;

    public final String serialize() {
        return Json.serialize(this);
    }

    /**
     * @throws RuntimeException if anything goes wrong.
     */
    public static Progress deserialize(String in, Quest quest) {
        JsonElement elem = Json.PARSER.parse(in);
        JsonObject root = elem.getAsJsonObject();
        int goalIndex = root.getAsJsonPrimitive("goalIndex").getAsInt();
        Goal goal = quest.getGoals().get(goalIndex);
        Progress progress = goal.getHolder().deserializeProgress(elem);
        progress.setGoalIndex(goalIndex);
        return progress;
    }

    public final void increaseAmount() {
        amount += 1;
    }
}
