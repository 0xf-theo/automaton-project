import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record D4_Transition(int entre, String symbole, int sortie) {
    public static Integer SOMMET_POUBELLE = -1;

    public static D4_Transition parse(String line) {
        // Expression régulière pour parser la ligne d'une transition
        String regex = "(\\d+|[Pp])([a-zA-Zε]+)(\\d+|[Pp])";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            String part1 = matcher.group(1); // Première partie : chiffre(s)
            String part2 = matcher.group(2); // Deuxième partie : lettre(s)
            String part3 = matcher.group(3); // Troisième partie : chiffre(s)

            return new D4_Transition(part1.equalsIgnoreCase("p") ? SOMMET_POUBELLE : Integer.parseInt(part1), part2, part3.equalsIgnoreCase("p") ? SOMMET_POUBELLE : Integer.parseInt(part3));
        }

        return null;
    }
}