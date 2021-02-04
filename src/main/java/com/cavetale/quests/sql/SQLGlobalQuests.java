package com.cavetale.quests.sql;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stores a list of global quests for all players to use, on all
 * servers.
 */
@Data @NoArgsConstructor
@Table(name = "global_quests",
       uniqueConstraints = @UniqueConstraint(columnNames = {"category", "time_id"}))
public final class SQLGlobalQuests {
    @Id
    private Integer id;
    @Column(nullable = false, length = 16)
    private String category; // daily, weekly, monthly
    @Column(nullable = false, unique = true)
    private int timeId; // dailyId, weeklyId, monthlyId
    @Column(nullable = true, length = 4096)
    private String json;

    public SQLGlobalQuests(final String category, final int timeId) {
        this.category = category;
        this.timeId = timeId;
    }
}
