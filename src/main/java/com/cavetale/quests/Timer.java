package com.cavetale.quests;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.Date;
import java.util.Locale;
import org.bukkit.Bukkit;
import lombok.Getter;

@Getter
public final class Timer {
    @Getter private static Timer inst;

    private ZoneId zoneId;
    private Locale locale;
    private WeekFields weekFields;

    private long now;
    private long today;
    private long tomorrow;
    private long thisWeek;
    private long nextWeek;

    private int dailyId;
    private int weeklyId;

    private Instant instant;
    private LocalDate localDate;
    private LocalDateTime localDateTime;
    private LocalDate localDateWeek;

    public void enable() {
        inst = this;
        init();
        Bukkit.getScheduler().runTaskTimer(QuestsPlugin.getInst(), this::tick, 1L, 1L);
    }

    protected void init() {
        zoneId = ZoneId.systemDefault();
        locale = Locale.GERMANY;
        weekFields = WeekFields.of(locale);
        updateNow();
        updateDay();
        updateWeek();
    }

    protected void debug() {
        System.out.println(instant);
        System.out.println(localDate);
        System.out.println(localDateTime);
        System.out.println("zone=" + zoneId);
        System.out.println("locale=" + locale);
        System.out.println("now=     " + new Date(now));
        System.out.println("today=   " + new Date(today));
        System.out.println("tomorrow=" + new Date(tomorrow));
        System.out.println("thisWeek=" + new Date(thisWeek));
        System.out.println("nextWeek=" + new Date(nextWeek));
        System.out.println("dailyId=" + dailyId);
        System.out.println("weelyId=" + weeklyId);
    }

    private void updateNow() {
        instant = Instant.now();
        now = instant.toEpochMilli();
    }

    private void updateDay() {
        localDateTime = LocalDateTime.ofInstant(instant, zoneId);
        localDate = localDateTime.toLocalDate();
        today = localDate.atStartOfDay().atZone(zoneId).toInstant().toEpochMilli();
        tomorrow = localDate.plusDays(1L).atStartOfDay().atZone(zoneId).toInstant().toEpochMilli();
        dailyId = localDate.getYear() * 10000
            + localDate.getMonth().getValue() * 100
            + localDate.getDayOfMonth();
    }

    private void updateWeek() {
        localDateWeek = localDate.with(weekFields.dayOfWeek(), 1L);
        thisWeek = localDateWeek.atStartOfDay().atZone(zoneId).toInstant().toEpochMilli();
        nextWeek = localDateWeek.plusWeeks(1).atStartOfDay().atZone(zoneId).toInstant().toEpochMilli();
        weeklyId = localDateWeek.getYear() * 100 + localDateWeek.get(weekFields.weekOfYear());
    }

    private void tick() {
        updateNow();
        if (now >= tomorrow) {
            updateDay();
            QuestsPlugin.getInst().getDailyGlobalQuests().update();
            if (now >= nextWeek) {
                updateWeek();
                QuestsPlugin.getInst().getWeeklyGlobalQuests().update();
            }
        }
    }

    public static int getTimeId(QuestCategory category) {
        switch (category) {
        case DAILY: return inst.dailyId;
        case WEEKLY: return inst.weeklyId;
        default: return inst.dailyId;
        }
    }
}
