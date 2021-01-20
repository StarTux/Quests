package com.cavetale.quests.session;

import com.cavetale.quests.Quest;
import com.cavetale.quests.QuestCategory;
import com.cavetale.quests.goal.Progress;
import com.cavetale.quests.util.Text;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

@RequiredArgsConstructor
public final class QuestBook {
    private final Session session;

    public void openBook() {
        openBook(meta -> {
                List<QuestInstance> quests = session.getVisibleQuests();
                if (quests.isEmpty()) {
                    meta.addPage(ChatColor.RED + "No quests to show");
                } else {
                    for (QuestInstance questInstance : quests) {
                        meta.spigot().addPage(makeQuestPage(questInstance).create());
                    }
                }
            });
    }

    public void openBook(QuestInstance questInstance) {
        openBook(meta -> {
                ComponentBuilder cb = makeQuestPage(questInstance);
                cb.append("\n\n").reset();
                cb.append("[BACK]").color(ChatColor.DARK_GRAY);
                cb.event(Text.tooltip(ChatColor.DARK_GRAY + "Go Back"));
                cb.event(Text.button("/quests"));
                meta.spigot().addPage(cb.create());
            });
    }

    private void openBook(Consumer<BookMeta> callback) {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) item.getItemMeta();
        meta.setTitle("Quests");
        meta.setAuthor("Cavetale");
        meta.setGeneration(BookMeta.Generation.ORIGINAL);
        callback.accept(meta);
        item.setItemMeta(meta);
        session.getPlayer().openBook(item);
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
                cb.append("[Claim]").color(ChatColor.DARK_AQUA);
                cb.event(Text.tooltip(ChatColor.AQUA + "Claim the rewards"));
                cb.event(Text.button("/quest claim " + questInstance.getRow().getId()));
            } else {
                cb.append("Complete").color(ChatColor.BLUE).bold(true);
            }
        }
        return cb;
    }
}
