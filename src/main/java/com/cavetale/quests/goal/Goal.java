package com.cavetale.quests.goal;

import com.cavetale.quests.util.Text;
import lombok.Data;

/**
 * Superclass of quest goals. It contains a generic config data tag.
 *
 * This class will be serialized to Json. The Quest::deserialize
 * method will take charge of proper deserialization of the generic
 * parts.
 */
@Data
public abstract class Goal {
    protected GoalType type;
    protected int amount;
    protected String description;

    public final GoalHolder getHolder() {
        return type.holder;
    }

    /**
     * Override to perform an action every tick.
     */
    public void onTick() { }

    /**
     * Produce a human readable interpretation of the current
     * progress. Defaults to "<amount>/<required>".
     */
    public String getProgressString(Progress progress) {
        return Text.getProgressString(progress.getAmount(), amount);
    }

    /**
     * Concrete goals may override this but should respect the field
     * if set.
     * The description should not change over the course of the
     * goal. Override a dynamid getProgressString() instead.
     */
    public String getDescription() {
        return description != null
            ? description
            : "Goal";
    }
}
