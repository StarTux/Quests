package com.cavetale.quests.session;

import com.cavetale.quests.util.Text;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

@RequiredArgsConstructor
public final class ProgressBar {
    private final QuestInstance questInstance;
    BossBar bossBar;
    long visibilityTimeout; // millis

    void enable() {
        bossBar = Bukkit.createBossBar("Goal", BarColor.BLUE, BarStyle.SOLID);
        bossBar.setVisible(false);
        bossBar.addPlayer(questInstance.getSession().getPlayer());
        onNewGoal();
    }

    void disable() {
        bossBar.removeAll();
        bossBar.setVisible(false);
        bossBar = null;
    }

    void onNewGoal() {
        visibilityTimeout = 0L;
        bossBar.setVisible(false);
        bossBar.setTitle(Text.colorize(questInstance.getCurrentGoal().getDescription()));
        double prog = (double) questInstance.getCurrentProgress().getAmount()
            / (double) Math.max(1, questInstance.getCurrentGoal().getAmount());
        bossBar.setProgress(Math.max(0.0, Math.min(1.0, prog)));
    }

    void showProgress() {
        double prog = (double) questInstance.getCurrentProgress().getAmount()
            / (double) Math.max(1, questInstance.getCurrentGoal().getAmount());
        bossBar.setProgress(Math.max(0.0, Math.min(1.0, prog)));
        bossBar.setVisible(true);
        visibilityTimeout = System.currentTimeMillis() + 5000L;
    }

    void tick() {
        long now = System.currentTimeMillis();
        if (now >= visibilityTimeout) {
            bossBar.setVisible(false);
            visibilityTimeout = 0L;
        }
    }

    void hide() {
        visibilityTimeout = 0L;
        bossBar.setVisible(false);
    }
}
