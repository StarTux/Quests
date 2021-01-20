package com.cavetale.quests.session;

import com.cavetale.quests.Quest;
import com.cavetale.quests.QuestCategory;
import com.cavetale.quests.goal.Progress;
import com.cavetale.quests.util.Text;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

@RequiredArgsConstructor
public final class QuestBook {
    private final Session session;

    /**
     * Open a book containing all quests, including overview.
     */
    public void openBook() {
        ItemStack bookStack = makeBook(meta -> {
                List<QuestInstance> quests = session.getVisibleQuests();
                Collections.sort(quests);
                if (quests.isEmpty()) {
                    meta.addPage(ChatColor.RED + "No quests to show");
                    return;
                }
                // Make index
                ComponentBuilder cb = new ComponentBuilder();
                Map<QuestCategory, List<QuestInstance>> categories = new EnumMap<>(QuestCategory.class);
                for (QuestInstance questInstance : quests) {
                    categories.computeIfAbsent(questInstance.getQuest().getCategory(), cat -> new ArrayList<>())
                        .add(questInstance);
                }
                Map<Integer, BaseComponent> clickMap = new HashMap<>();
                for (QuestCategory category : QuestCategory.values()) {
                    List<QuestInstance> list = categories.get(category);
                    if (list == null || list.isEmpty()) continue;
                    cb.append(category.humanName).color(category.color).bold(true);
                    cb.append("\n").reset();
                    for (QuestInstance questInstance : list) {
                        Quest quest = questInstance.getQuest();
                        Progress progress = questInstance.getProgress();
                        if (!progress.isAccepted()) {
                            cb.append("\u2610").color(ChatColor.DARK_RED);
                            cb.event(Text.tooltip(ChatColor.GREEN + "Accept " + quest.getTitle()));
                            cb.event(Text.button("/quest accept " + questInstance.getRow().getId()));
                        } else if (!progress.isCompleted()) {
                            cb.append("\u2610").color(ChatColor.DARK_GREEN);
                            cb.event(Text.tooltip(ChatColor.RED + "Not completed"));
                        } else if (!questInstance.getRow().isClaimed()) {
                            cb.append("\u2611").color(ChatColor.GOLD);
                            cb.event(Text.tooltip(ChatColor.GOLD + "Claim the rewards"));
                            cb.event(Text.button("/quest claim " + questInstance.getRow().getId()));
                        } else {
                            cb.append("\u2612").color(ChatColor.BLUE);
                            cb.event(Text.tooltip(ChatColor.BLUE + "\u2713 Completed"));
                        }
                        cb.append(" ").reset();
                        cb.append(quest.getTitle());
                        cb.event(Text.tooltip(quest.getTitle()));
                        clickMap.put(questInstance.getRow().getId(), cb.getCurrentComponent());
                        cb.append("\n").reset();
                    }
                }
                meta.spigot().addPage(cb.create());
                for (QuestInstance questInstance : quests) {
                    clickMap.get(questInstance.getRow().getId())
                        .setClickEvent(Text.bookmark(meta.getPageCount() + 1));
                    ComponentBuilder cb2 = makeQuestPage(questInstance);
                    cb2.append("\n\n").reset();
                    cb2.append("\u23CE back").color(ChatColor.DARK_GRAY);
                    cb2.event(Text.tooltip(ChatColor.DARK_GRAY + "Back"));
                    cb2.event(Text.bookmark(1));
                    meta.spigot().addPage(cb2.create());
                }
                meta.spigot().setPage(1, cb.create());
            });
        session.getPlayer().openBook(bookStack);
    }

    /**
     * Open a book containing a single quest.
     */
    public void openBook(QuestInstance questInstance) {
        ItemStack bookStack = makeBook(meta -> {
                ComponentBuilder cb = makeQuestPage(questInstance);
                cb.append("\n\n").reset();
                cb.append("\u23CE back").color(ChatColor.DARK_GRAY);
                cb.event(Text.tooltip(ChatColor.DARK_GRAY + "Go Back"));
                cb.event(Text.button("/quests"));
                meta.spigot().addPage(cb.create());
            });
        session.getPlayer().openBook(bookStack);
    }

    private ItemStack makeBook(Consumer<BookMeta> callback) {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) item.getItemMeta();
        meta.setTitle("Quests");
        meta.setAuthor("Cavetale");
        meta.setGeneration(BookMeta.Generation.ORIGINAL);
        callback.accept(meta);
        item.setItemMeta(meta);
        return item;
    }

    public static ComponentBuilder makeQuestPage(QuestInstance questInstance) {
        Quest quest = questInstance.getQuest();
        ComponentBuilder cb = new ComponentBuilder();
        QuestCategory category = quest.getCategory();
        cb.append(Text.colorize(String.join("&r ", "" + category.color + ChatColor.BOLD + category.humanName,
                                            quest.getTitle())));
        if (quest.getTag().getDescription() != null) {
            cb.append("\n").reset();
            cb.append(quest.getTag().getDescription()).color(ChatColor.DARK_GRAY);
        }
        Progress progress = questInstance.getProgress();
        if (progress.isAccepted() || quest.getGoals().size() == 1) {
            cb.append("\n\n").reset();
            cb.append(questInstance.getCurrentGoal().getDescription()).color(ChatColor.DARK_GRAY);
            cb.append(" ").reset();
            String progressString = Text.colorize(questInstance.getCurrentGoal().getProgressString(progress));
            cb.append(progressString);
        }
        if (!progress.isAccepted()) {
            cb.append("\n\n").reset();
            cb.append("[Accept]").color(ChatColor.DARK_GREEN);
            cb.event(Text.tooltip(ChatColor.GREEN + "Accept this Quest"));
            cb.event(Text.button("/quest accept " + questInstance.getRow().getId()));
        }
        if (progress.isCompleted()) {
            cb.append("\n\n").reset();
            if (!questInstance.getRow().isClaimed()) {
                cb.append("[Claim]").color(ChatColor.GOLD).bold(true);
                cb.event(Text.tooltip(ChatColor.GOLD + "Claim the rewards"));
                cb.event(Text.button("/quest claim " + questInstance.getRow().getId()));
            } else {
                cb.append("Complete").color(ChatColor.BLUE).bold(true);
            }
        }
        return cb;
    }
}
