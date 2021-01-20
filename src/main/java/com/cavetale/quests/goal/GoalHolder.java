package com.cavetale.quests.goal;

import com.cavetale.quests.util.Json;
import com.google.gson.JsonElement;
import org.bukkit.event.Listener;

/**
 * A singleton instance of this class represents the static aspects of
 * a type of goal.
 */
public interface GoalHolder {
    Class<? extends Goal> getGoalClass();

    default Class<? extends Progress> getProgressClass() {
        return Progress.class;
    }

    default Goal newGoal() {
        try {
            return getGoalClass().newInstance();
        } catch (InstantiationException ie) {
            throw new IllegalStateException(ie);
        } catch (IllegalAccessException iae) {
            throw new IllegalStateException(iae);
        }
    }

    default Progress newProgress() {
        try {
            return getProgressClass().newInstance();
        } catch (InstantiationException ie) {
            throw new IllegalStateException(ie);
        } catch (IllegalAccessException iae) {
            throw new IllegalStateException(iae);
        }
    }

    default Listener getEventListener() {
        return null;
    }

    default Goal deserializeGoal(final JsonElement in) {
        try {
            return Json.GSON.fromJson(in, getGoalClass());
        } catch (RuntimeException re) {
            re.printStackTrace();
            return newGoal();
        }
    }

    default Progress deserializeProgress(final JsonElement in) {
        try {
            return Json.GSON.fromJson(in, getProgressClass());
        } catch (RuntimeException re) {
            re.printStackTrace();
            return newProgress();
        }
    }
}
