package com.cavetale.quests.goal;

import com.cavetale.quests.Quest;

/**
 * Special kind of GoalHolder which signals its purpose as a reusable
 * goal. It has methods to produce daily and weekly quests based on
 * the goal.
 */
public interface RegularGoalHolder extends GoalHolder {
    Quest newDailyQuest();

    Quest newWeeklyQuest();
}
