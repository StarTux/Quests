package com.cavetale.quests.goal;

public enum GoalType {
    COMMAND(new CommandGoal.Holder()),
    CHAT(new ChatGoal.Holder()),
    MINE_BLOCK(new MineBlockGoal.Holder()),
    BREED_ENTITY(new BreedEntityGoal.Holder()),
    EAT(new EatGoal.Holder()),
    KILL_MOB(new KillMobGoal.Holder()),
    CATCH_FISH(new CatchFishGoal.Holder()),
    ENCHANT_ITEM(new EnchantItemGoal.Holder()),
    GROW_TREE(new GrowTreeGoal.Holder()),
    COOK_ITEM(new CookItemGoal.Holder());

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
