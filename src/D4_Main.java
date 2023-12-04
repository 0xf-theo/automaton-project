
import utils.StringUtils;

import java.io.IOException;
import java.util.Scanner;

public class D4_Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // Demande le jeu de test à charger
            System.out.print("Quel jeu de test voulez-vous charger ? (x pour quitter)");
            String num_test = scanner.nextLine();
            if (num_test.equals("x")) break;
            String chemin_fichier = String.format("./resources/tests/D4_%s.txt", num_test);

            System.out.println("[---------------------------------------------------------------------------]");
            System.out.println("Automate a charger : " + chemin_fichier);
            System.out.println("[---------------------------------------------------------------------------]");

            D4_Automate AFDC = null;

            try {
                D4_Automate af = D4_Automate.lireAutomateSurFichier(chemin_fichier);
                af.afficherAutomate();

                System.out.println("[---------------------------------------------------------------------------]");
                System.out.println(StringUtils.center("[Standard : %s] [Déterministe : %s] [Complet : %s]".formatted(
                        af.estStandard(false) ? "Oui" : "Non",
                        af.estUnAutomateDeterministe(false) ? "Oui" : "Non",
                        af.estUnAutomateComplet(false) ? "Oui" : "Non"
                ), 75));
                System.out.println("[---------------------------------------------------------------------------]\n");

                if (!af.estStandard(true)) {
                    System.out.print("L'automate n'est pas standard, voulez vous le standardiser ? (o/n)");
                    String a = scanner.nextLine();
                    if (a.equals("o")) {
                        var sfa = af.standardiser();
                        System.out.println("[---------------------------------------------------------------------------]");
                        System.out.println(StringUtils.center("~ Automate standard ~", 75));
                        System.out.println("[---------------------------------------------------------------------------]");
                        sfa.afficherAutomate();
                    }
                }

                if (af.estUnAutomateDeterministe(true)) {
                    if (af.estUnAutomateComplet(true)) {
                        AFDC = af;
                    } else {
                        AFDC = af.completion();
                    }
                } else {
                    AFDC = af.supprimerEpsilonTransitions().determinisationEtCompletionAutomate();
                }

                System.out.println("[---------------------------------------------------------------------------]");
                System.out.println(StringUtils.center("~ Automate déterministe complet ~", 75));
                System.out.println("[---------------------------------------------------------------------------]");
                AFDC.afficherAutomate();

                System.out.println("[---------------------------------------------------------------------------]");
                System.out.println(StringUtils.center("~ Automate déterministe complet et complémentaire ~", 75));
                System.out.println("[---------------------------------------------------------------------------]");
                var AFDCC = AFDC.complementaire();
                AFDCC.afficherAutomate();

                System.out.println("[---------------------------------------------------------------------------]");
                System.out.println(StringUtils.center("~ Automate déterministe complet minimisé ~", 75));
                System.out.println("[---------------------------------------------------------------------------]");
                var minimiser = AFDC.minimiser(true);
                minimiser.afficherAutomate();

                System.out.println("-------------------------------------------");
                System.out.println("Lecture de mot, mettez 'fin' pour terminer");
                String mot = "";
                while (!mot.equals("fin")) {
                    mot = scanner.nextLine();
                    boolean result = AFDC.reconnaitre_mot(mot);

                    if (result) {
                        System.out.println("Le mot '" + mot + "' est reconnu par l'automate");
                    } else {
                        System.out.println("Le mot '" + mot + "' n'est pas reconnu par l'automate");
                    }
                }
            } catch (IOException e) {
                System.out.println("Impossible de charger le fichier.");
                continue;
            }

            System.out.println();
        }
    }
}