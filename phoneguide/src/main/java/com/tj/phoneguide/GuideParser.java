package com.tj.phoneguide;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure (no Android classes, JVM-tested) reader of assets/steps.txt, so the structure of the guide can be checked by tests.
 *
 * Format: "plan: ..." lines before the first group make the "Your plan" box. "## Title" starts a group that is open, "##- Title" one that is closed until tapped; the text before the first ":" of a
 * title is the short name on the jump button. "@ Title" starts a card (no numbers in titles: the app numbers the cards). Then: why: / cmd: (the line to type) / alt: (a second form) / undo: (any
 * number) / note: / mark: must (the card that matters most) / special: a11y (the accessibility-merge helper). A single "#" line is a comment.
 */
final class GuideParser {
    private GuideParser() {}

    static final class Card {
        String title, why, note, special;
        boolean must;
        final List<String> cmds = new ArrayList<>(), alts = new ArrayList<>(), undos = new ArrayList<>();
        boolean optional() { return title != null && title.toLowerCase(java.util.Locale.ROOT).startsWith("optional"); }
    }

    static final class Group {
        String title;
        boolean collapsed;
        final List<Card> cards = new ArrayList<>();
        /** The short name for the jump button: the text before the first ":" (or the whole title). */
        String label() { int i = title.indexOf(':'); return (i > 0 ? title.substring(0, i) : title).trim(); }
    }

    static final class Guide {
        final List<String> plan = new ArrayList<>();
        final List<Group> groups = new ArrayList<>();
        int cardCount() { int n = 0; for (Group g : groups) n += g.cards.size(); return n; }
    }

    static Guide parse(BufferedReader r) throws IOException {
        Guide guide = new Guide();
        Group group = null;
        Card card = null;
        String line;
        while ((line = r.readLine()) != null) {
            if (line.startsWith("##- ")) { group = new Group(); group.title = line.substring(4).trim(); group.collapsed = true; guide.groups.add(group); card = null; }
            else if (line.startsWith("## ")) { group = new Group(); group.title = line.substring(3).trim(); guide.groups.add(group); card = null; }
            else if (line.startsWith("@ ")) {
                if (group == null) { group = new Group(); group.title = "Steps"; guide.groups.add(group); }
                card = new Card(); card.title = line.substring(2).trim(); group.cards.add(card);
            }
            else if (line.startsWith("plan:") && group == null) guide.plan.add(line.substring(5).trim());
            else if (card == null) continue;
            else if (line.startsWith("why:")) card.why = line.substring(4).trim();
            else if (line.startsWith("cmd:")) card.cmds.add(line.substring(4).trim());
            else if (line.startsWith("alt:")) card.alts.add(line.substring(4).trim());
            else if (line.startsWith("undo:")) card.undos.add(line.substring(5).trim());
            else if (line.startsWith("note:")) card.note = line.substring(5).trim();
            else if (line.startsWith("mark:")) card.must = line.substring(5).trim().equalsIgnoreCase("must");
            else if (line.startsWith("special:")) card.special = line.substring(8).trim();
        }
        return guide;
    }
}
