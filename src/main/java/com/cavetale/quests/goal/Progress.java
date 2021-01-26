package com.cavetale.quests.goal;

import com.cavetale.quests.util.Json;
import lombok.Data;

/**
 * Simple storage for quest progress.
 * Can be subclassed.
 */
@Data
public class Progress {
    protected int amount;

    public final String serialize() {
        return Json.serialize(this);
    }
}
