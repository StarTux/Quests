package com.cavetale.quests.sql;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
@Table(name = "players")
public final class SQLPlayer {
    @Id
    private Integer id;
    @Column(nullable = false, unique = true)
    private UUID uuid;
    @Column(nullable = false)
    private int dailyId;
    @Column(nullable = false)
    private int weeklyId;
    @Column(nullable = false)
    private int monthlyId;
    @Column(nullable = true, length = 4096)
    private String json;

    public SQLPlayer(final UUID uuid) {
        this.uuid = uuid;
    }
}
