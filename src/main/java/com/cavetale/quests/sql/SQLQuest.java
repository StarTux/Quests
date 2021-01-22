package com.cavetale.quests.sql;

import com.cavetale.quests.Quest;
import com.cavetale.quests.QuestState;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
@Table(name = "quests", indexes = @Index(columnList = "player"))
public final class SQLQuest {
    @Id
    private Integer id;
    @Column(nullable = false)
    private UUID player;
    @Column(nullable = false, length = 255)
    private String category;
    @Column(nullable = false)
    private int index;
    @Column(nullable = false, length = 4096)
    private String quest;
    @Column(nullable = false, length = 4096)
    private String state;
    @Column(nullable = false)
    private boolean complete;
    @Column(nullable = false)
    private boolean claimed;
    @Column(nullable = false)
    private Date created;

    public void store(final Quest theQuest) {
        this.quest = theQuest.serialize();
        this.category = theQuest.getCategory().key;
        this.index = theQuest.getIndex();
    }

    public void store(final QuestState theState) {
        this.state = theState.serialize();
    }
}
