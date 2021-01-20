package com.cavetale.quests;

import com.cavetale.quests.goal.ChatGoal;
import com.cavetale.quests.goal.CommandGoal;
import com.cavetale.quests.goal.GoalType;
import com.cavetale.quests.util.Json;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import org.junit.Test;

public final class QuestsTest {
    @Test
    public void test() {
        Random random = new Random();
        Quest quest = Quest.newInstance();
        quest.getTag().setTitle("The Quest");
        //
        CommandGoal commandGoal = (CommandGoal) GoalType.COMMAND.newGoal();
        commandGoal.setCommand("" + random.nextInt(1000));
        //
        ChatGoal chatGoal = (ChatGoal) GoalType.CHAT.newGoal();
        //
        quest.setGoals(Arrays.asList(commandGoal, chatGoal));

        String json = Json.serialize(quest);
        System.out.println(json);

        Quest quest2 = Quest.deserialize(json);
        String json2 = Json.serialize(quest2);
        System.out.println(json2);

        System.out.println("str equals=" + Objects.equals(json, json2));
        System.out.println("obj equals=" + Objects.equals(quest, quest2));
    }
}
