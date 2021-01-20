package com.cavetale.quests.goal;

public enum GoalType {
    COMMAND(new CommandGoal.Holder()),
    CHAT(new ChatGoal.Holder()),
    MINE_BLOCK(new MineBlockGoal.Holder());

    public final GoalHolder holder;

    GoalType(final GoalHolder holder) {
        this.holder = holder;
    }

    public Goal newGoal() {
        Goal goal = holder.newGoal();
        goal.setType(this);
        return goal;
    }
}
