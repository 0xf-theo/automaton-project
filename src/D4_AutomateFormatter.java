import utils.StringUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public record D4_AutomateFormatter(D4_Automate automate) {

    // Map utilisé pour stocker des alias de sommet, par exemple 0 en 0,1,2
    public static Map<Integer, String> etatMappings = new HashMap<>();

    public String line(int lenght) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.automate.alphabet().size() + 2; i++) {
            sb.append("-".repeat(i == 0 ? 10 : lenght)).append("+");
        }
        sb.append("\n");
        return sb.toString();
    }

    public String write() {
        StringBuilder sb = new StringBuilder();
        sb.append("Alphabet : ")
                .append(this.automate.alphabet().toString())
                .append("\n");
        sb.append("Nombre de transitions : %d\n".formatted(this.automate.transitions().values()
                .stream()
                .mapToLong(Collection::size)
                .sum()));
        sb.append("Nombre d'état : %d\n".formatted(this.automate.nbEtats()));
        sb.append("Etat initiaux : ")
                .append(this.automate.etatsInitiaux().stream()
                        .map(i -> D4_AutomateFormatter.etatMappings.getOrDefault(i, String.valueOf(i)))
                        .toList())
                .append("\n");
        sb.append("Etat finaux : ")
                .append(this.automate.etatsTerminaux().stream()
                        .map(i -> D4_AutomateFormatter.etatMappings.getOrDefault(i, String.valueOf(i)))
                        .toList())
                .append("\n");
        var maxNeededSpace = etatMappings.values().stream()
                .max(Comparator.comparingInt(String::length))
                .map(String::length)
                .orElse(10);
        maxNeededSpace = maxNeededSpace < 10 ? 10 : maxNeededSpace + 2;
        sb.append(line(maxNeededSpace));
        sb.append("|%s|%s|".formatted(StringUtils.center("Type", 9), StringUtils.center("Sommets", maxNeededSpace)));

        for (String symbole : automate.alphabet()) {
            sb.append("%s|".formatted(StringUtils.center(symbole + "", maxNeededSpace)));
        }
        sb.append("\n");
        sb.append(line(maxNeededSpace));
        for (int i = 0; i < automate.nbEtats(); i++) {
            var marqueur = "";
            if (automate.etatsInitiaux().contains(i))
                marqueur += "E";
            if (automate.etatsTerminaux().contains(i))
                marqueur += "S";

            sb.append("|%s|%s|".formatted(StringUtils.center(marqueur, 9), StringUtils.center(etatMappings.getOrDefault(i, i + ""), maxNeededSpace)));
            for (String symbole : automate.alphabet()) {
                if (automate.transitions().containsKey(i)) {
                    int finalI = i;
                    var transitions = automate.transitions().get(i).stream()
                            .filter(t -> t.symbole().equals(symbole + "") && t.entre() == finalI)
                            .map(t -> etatMappings.getOrDefault(t.sortie(), t.sortie() + ""))
                            .collect(Collectors.joining(","));

                    if (transitions.isEmpty()) {
                        sb.append("%s|".formatted(StringUtils.center("--", maxNeededSpace)));
                    } else {
                        sb.append("%s|".formatted(StringUtils.center(transitions, maxNeededSpace)));
                    }
                } else {
                    sb.append("%s|".formatted(StringUtils.center("--", maxNeededSpace)));
                }

            }
            sb.append("\n");
        }
        sb.append(line(maxNeededSpace));
        return sb.toString();
    }

    public static void addMapping(Integer sommet, String name) {
        etatMappings.remove(sommet);
        etatMappings.put(sommet, name);
    }

    public static void clear() {
        etatMappings.clear();
    }
}