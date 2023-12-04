import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record D4_Automate(Set<String> alphabet, int nbEtats, List<Integer> etatsInitiaux, List<Integer> etatsTerminaux,
                          int nbTransitions, Map<Integer, List<D4_Transition>> transitions) {

    public void afficherAutomate() {
        System.out.println(new D4_AutomateFormatter(this).write());
    }

    public static D4_Automate lireAutomateSurFichier(String nom_fichier) throws IOException {
        D4_AutomateFormatter.clear();
        File file = new File(nom_fichier);
        BufferedReader br = new BufferedReader(new FileReader(file));
        var nbAlphabet = Integer.parseInt(br.readLine().trim());
        int nbEtats = Integer.parseInt(br.readLine().trim());
        var etatsInitiauxData = br.readLine().split(" ");
        var nbEtatInitiaux = Integer.parseInt(etatsInitiauxData[0]);
        var etatsInitiaux = Arrays.stream(etatsInitiauxData).skip(1).limit(nbEtatInitiaux).map(Integer::parseInt).collect(Collectors.toList());
        var etatsFinauxData = br.readLine().split(" ");
        var nbEtatFinaux = Integer.parseInt(etatsFinauxData[0]);
        var etats_terminaux = Arrays.stream(etatsFinauxData).skip(1).limit(nbEtatFinaux).map(Integer::parseInt).collect(Collectors.toList());
        int nbTransitions = Integer.parseInt(br.readLine().trim());
        Map<Integer, List<D4_Transition>> transitionMap = new HashMap<>();
        for (int i = 0; i < nbTransitions; i++) {
            D4_Transition transitionParts = D4_Transition.parse(br.readLine());
            if (transitionParts != null) {
                if (!transitionMap.containsKey(transitionParts.entre())) {
                    transitionMap.put(transitionParts.entre(), new LinkedList<>());
                }

                transitionMap.get(transitionParts.entre()).add(transitionParts);
            }
        }

        /*
        Dans le fichier, on spécifie le nombre d'état comprenant également l'état poubelle s'il y en a un
        Ce qui veut dire que quand on boucle de 0 a N ou N égale le nombre d'état on se retrouve avec un état sans transitions sortantes ni entrantes
        Alors, on récupère ce numéro de sommet et on met a jour la table des transitions pour remplacer la valeur constant du sommet poubelle qui est -1 par celle la
         */
        Map<Integer, List<D4_Transition>> finalTransitionMap = transitionMap;
        var etatPoubelle = IntStream.range(0, nbEtats).filter(i -> !finalTransitionMap.containsKey(i) && finalTransitionMap.values().stream().flatMap(Collection::stream).noneMatch(z -> z.entre() == i && z.sortie() == i)).findFirst();

        if (etatPoubelle.isPresent() && finalTransitionMap.values().stream().flatMap(Collection::stream).anyMatch(t -> t.entre() == D4_Transition.SOMMET_POUBELLE || t.sortie() == D4_Transition.SOMMET_POUBELLE)) {
            D4_AutomateFormatter.addMapping(etatPoubelle.getAsInt(), "P");
            Map<Integer, List<D4_Transition>> newTransitionsMap = new HashMap<>();
            for (Map.Entry<Integer, List<D4_Transition>> transit : transitionMap.entrySet()) {
                newTransitionsMap.put(transit.getKey() == D4_Transition.SOMMET_POUBELLE ? etatPoubelle.getAsInt() : transit.getKey(), transit.getValue().stream().map(t -> new D4_Transition(t.entre() == D4_Transition.SOMMET_POUBELLE ? etatPoubelle.getAsInt() : t.entre(), t.symbole(), t.sortie() == D4_Transition.SOMMET_POUBELLE ? etatPoubelle.getAsInt() : t.sortie())).toList());
            }
            transitionMap = newTransitionsMap;
        }
        var alphabet = transitionMap.values().stream().flatMap(Collection::stream).map(t -> t.symbole() + "").collect(Collectors.toSet());
        return new D4_Automate(alphabet, nbEtats, etatsInitiaux, etats_terminaux, nbTransitions, transitionMap);
    }

    /**
     * <h1>Automate complet</h1>
     * Un automate fini est dit complet si :
     * <ul>
     *     <li>
     *     Depuis n’importe quel état, tous les symboles de l’alphabet doivent appartenir au moins une fois aux transitions (sortantes).
     *     </li>
     * <ul>
     */
    public boolean estUnAutomateComplet(boolean log) {
        if (alphabet.isEmpty() && nbTransitions == 0) return true;

        // Vérifier si chaque état a une transition pour chaque symbole de l'alphabet
        for (int etat = 0; etat < nbEtats; etat++) {
            // Si l'état n'a pas de transitions sortantes
            if (!transitions.containsKey(etat)) {
                if (log) System.out.printf("L'état %d ne possède pas de transition de sortie\n", etat);
                return false;
            }
            for (var symbole : alphabet) {
                // Si l'état n'as pas de transitions sortantes avec ce symbole
                if (transitions.get(etat).stream().noneMatch(t -> t.symbole().equals(symbole + ""))) {
                    if (log)
                        System.out.printf("L'automate n'est pas complet : pas de transition possible pour l'état %d et le symbole %s\n", etat, symbole);
                    return false;
                }
            }
        }

        // Si chaque état a une transition pour chaque symbole de l'alphabet, alors l'automate est complet
        return true;
    }

    /**
     * <h1>Automate standard</h1>
     * Un automate est dit standard si :
     * <ul>
     *     <li>Il est unitaire (un seul état initial)</li>
     *     <li>Il n’existe pas de transitions allant sur cet état initial</li>
     * </ul>
     */
    public boolean estStandard(boolean log) {
        if (alphabet.isEmpty() && nbTransitions == 0) return true;

        // Si aucun état initaux
        if (this.etatsInitiaux.size() == 0) {
            if (log) System.out.println("L'automate n'est pas standard, il n'y a pas d'entrée");
            return false;
        }

        // Si plus de un état initial
        if (this.etatsInitiaux.size() > 1) {
            if (log) System.out.println("L'automate n'est pas standard, il y a plus d'une entrée !");
            return false;
        }

        // On regarde s'il existe des transitions allant vers l'unique état initial
        if (this.transitions.values().stream()
                .flatMap(Collection::stream)
                .anyMatch(t -> t.sortie() == this.etatsInitiaux.get(0))
        ) {
            if (log)
                System.out.println("L'automate n'est pas standard, l'état initial possède des transitions vers celle-ci");
            return false;
        }
        return true;
    }

    /**
     * <h1>Automate déterministe</h1>
     * Un automate est dit déterministe si il respecte trois conditions :
     * <ul>
     *     <li>Il possède un unique état initial.</li>
     *     <li>Il ne possède pas d’epsilon-transitions.</li>
     *     <li>Pour chaque état de cet automate, il existe au maximum une transition issue de cet état possédant le même symbole.</li>
     *     </ul>
     */
    public boolean estUnAutomateDeterministe(boolean log) {
        if (alphabet.isEmpty() && nbTransitions == 0) return true;

        // S'il y a plusieurs états initiaux
        if (this.etatsInitiaux.size() > 1) {
            if (log) System.out.println("L'automate n'est pas déterministe, il y a plus d'une entrée !");
            return false;
        }

        // Si l'automate possède au moins une transition epsilon
        if (this.alphabet.contains("ε")) {
            if (log) System.out.println("L'automate n'est pas déterministe, il y a des epsilon-transitions");
            return false;
        }

        for (int etat = 0; etat < nbEtats; etat++) {
            for (var symbole : alphabet) {
                // On récupère toutes les transitions sortantes pour cet état avec ce symbole
                var transitions = this.transitions.getOrDefault(etat, List.of()).stream()
                        .filter(t -> t.symbole().equals(symbole + ""))
                        .toList();

                // S'il y a plusieurs transitions sortantes pour ce symbole
                if (transitions.size() > 1) {
                    if (log)
                        System.out.printf("L'automate n'est pas déterministe : plusieurs destinations possibles pour l'état %d et le symbole %s\n", etat, symbole);
                    return false;
                }

                // S'il n'y a pas de transition avec ce symbole
                if (transitions.size() == 0) {
                    if (log)
                        System.out.printf("L'automate n'est pas déterministe : aucune destination possible pour l'état %d et le symbole %s\n", etat, symbole);
                    return false;
                }
            }
        }

        // Si aucune transition ne viole la propriété de déterminisme, alors l'automate est déterministe
        return true;
    }

    public D4_Automate standardiser() {
        // Vérifier si l'automate est déjà en forme standard
        if (this.estStandard(false)) {
            return this;
        }

        // Ajouter un nouvel état initial/final
        var nouveauxTransitions = new ArrayList<D4_Transition>();
        int i = this.nbEtats;
        List<Integer> etatsInitiaux = new ArrayList<>(List.of(i));

        // On ajoute à l'état I toutes les transitions qu'ils y avaient pour les anciens états initiaux
        for (var etatInitial : this.etatsInitiaux) {
            var transitions = this.transitions.get(etatInitial);
            for (var symbole : this.alphabet) {
                transitions.stream().filter(t -> t.symbole().equals(symbole)).forEach(t -> {
                    nouveauxTransitions.add(new D4_Transition(i, symbole, t.sortie()));
                });
            }
        }

        // On ajoute l'état I
        var transitions = new HashMap<>(this.transitions);
        transitions.put(i, nouveauxTransitions);
        D4_AutomateFormatter.addMapping(i, "I");

        return new D4_Automate(this.alphabet, this.nbEtats + 1, etatsInitiaux, etatsTerminaux, this.nbTransitions + this.nbEtats * this.alphabet.size(), transitions);
    }

    public D4_Automate completion() {
        if (this.estUnAutomateComplet(false)) {
            return this;
        }

        Map<Integer, List<D4_Transition>> nouveauxTransitions = new HashMap<>();

        // On ajoute l'état poubelle
        int etatPoubelle = this.nbEtats;
        D4_AutomateFormatter.addMapping(etatPoubelle, "P");

        for (int i = 0; i < this.nbEtats; i++) {
            var tmp = new ArrayList<D4_Transition>();

            for (var symbole : this.alphabet) {
                // On récupère toutes les transitions sortantes de cet état avec ce symbole
                var transitions = this.transitions.getOrDefault(i, List.of()).stream()
                        .filter(t -> t.symbole().equals(symbole))
                        .toList();

                // S'il n'y a pas de transition, on ajoute une nouvelle transition sortante vers l'état poubelle
                if (transitions.isEmpty()) {
                    tmp.add(new D4_Transition(i, symbole, etatPoubelle));
                } else {
                    tmp.addAll(transitions);
                }
            }

            nouveauxTransitions.put(i, tmp);
        }

        // On ajoute les transitions sortantes de l'état poubelle vers lui meme pour tous les symboles
        nouveauxTransitions.put(etatPoubelle, this.alphabet.stream()
                .map(a -> new D4_Transition(etatPoubelle, a, etatPoubelle))
                .collect(Collectors.toList()));

        return new D4_Automate(this.alphabet, nbEtats + 1, etatsInitiaux, etatsTerminaux, (int) nouveauxTransitions.values().stream().mapToLong(Collection::size).sum(), nouveauxTransitions);
    }

    public D4_Automate determinisationEtCompletionAutomate() {
        return this.determiniser().completion();
    }

    public D4_Automate determiniser() {
        Map<String, Integer> mappinpSommetHyperSommet = new HashMap<>();
        Map<Integer, List<D4_Transition>> nouvellesTransition = new HashMap<>();
        Queue<String> etatsAVisiter = new LinkedList<>();
        List<String> etatsVisité = new ArrayList<>();
        var nbEtats = 0;

        // On fusionne les états initiaux et on ajoute le super état comme étant le premier état à visiter
        {
            var tmp = this.etatsInitiaux.stream().flatMap(i -> this.transitions.getOrDefault(i, List.of()).stream()).collect(Collectors.toSet());

            etatsAVisiter.add(String.join("-", tmp.stream().map(D4_Transition::entre).sorted().map(String::valueOf).collect(Collectors.toSet())));
        }

        while (!etatsAVisiter.isEmpty()) {
            var etatAVisiter = etatsAVisiter.poll();
            // Si c'est l'état vide ça ne sert à rien de le visiter
            if (etatAVisiter.isEmpty() || etatAVisiter.equals("P")) continue;

            // On génère le sommet équivalent à la combinaison qu'on visite
            var newEtat = 0;
            if (!mappinpSommetHyperSommet.containsKey(etatAVisiter)) {
                newEtat = nbEtats++;
                mappinpSommetHyperSommet.put(etatAVisiter, newEtat);
            } else {
                newEtat = mappinpSommetHyperSommet.get(etatAVisiter);
            }
            etatsVisité.add(etatAVisiter);

            var etats = etatAVisiter.split("-");
            var transitions = Arrays.stream(etats).map(Integer::valueOf)
                    .flatMap(i -> this.transitions.getOrDefault(i, List.of()).stream())
                    .collect(Collectors.toSet());

            List<D4_Transition> transitionsEtat = new ArrayList<>();

            for (var symbole : this.alphabet) {
                // On génère le sommet fusionné pour les sorties avec ce symbole pour cet état
                var sortie = transitions.stream()
                        .filter(t -> t.symbole().equals(symbole))
                        .map(D4_Transition::sortie)
                        .distinct()
                        .sorted().map(String::valueOf)
                        .collect(Collectors.joining("-"));

                if (sortie.isEmpty()) sortie = "P";

                // On génère le sommet équivalent à la combinaison de sortie de transition
                var newNewEtat = 0;
                if (!mappinpSommetHyperSommet.containsKey(sortie)) {
                    newNewEtat = nbEtats++;
                    mappinpSommetHyperSommet.put(sortie, newNewEtat);
                } else {
                    newNewEtat = mappinpSommetHyperSommet.get(sortie);
                }
                transitionsEtat.add(new D4_Transition(newEtat, symbole, newNewEtat));
                if (!etatsVisité.contains(sortie)) {
                    etatsAVisiter.add(sortie);
                }
            }

            nouvellesTransition.put(newEtat, transitionsEtat);
        }

        //On ajoute les transitions sur l'état vide si présent
        if (mappinpSommetHyperSommet.containsKey("P")) {
            var etatVide = mappinpSommetHyperSommet.get("P");
            nouvellesTransition.put(etatVide, alphabet.stream().map(i -> new D4_Transition(etatVide, i, etatVide)).collect(Collectors.toList()));
        }

        // Le premier sommet qu'on a traité (fusion des anciennes entrées) est une entrée
        List<Integer> entrées = new ArrayList<>(List.of(0));
        List<Integer> sorties = new ArrayList<>();

        //On applique les mappings pour le rendu
        for (var m : mappinpSommetHyperSommet.entrySet()) {
            D4_AutomateFormatter.addMapping(m.getValue(), m.getKey());
        }

        nouvellesTransition.keySet().forEach(sommet -> {
            // Pour chaque sommet on regarde si au moins un sommet issu de la fusion était une sortie, auquel cas le nouveau sommet est aussi une sortie
            var oldSommets = mappinpSommetHyperSommet.entrySet().stream().filter((k) -> k.getValue().equals(sommet)).map(Map.Entry::getKey).findFirst();
            if (oldSommets.isPresent()) {
                if (Arrays.stream(oldSommets.get().split("-")).filter(z -> !z.equals("P")).anyMatch(z -> this.etatsTerminaux.contains(Integer.valueOf(z)))) {
                    sorties.add(sommet);
                }
            }
        });

        return new D4_Automate(alphabet, nbEtats, entrées.stream().distinct().toList(), sorties.stream().distinct().toList(), (int) nouvellesTransition.values().stream().mapToLong(Collection::size).sum(), nouvellesTransition);
    }

    public boolean reconnaitre_mot(String mot) {
        Set<Integer> etatsCourants = new HashSet<>(etatsInitiaux);

        if (mot.trim().isEmpty() && etatsInitiaux.stream().anyMatch(etatsTerminaux::contains)) {
            return true;
        }

        // Parcours de tous les caractères du mot
        for (int i = 0; i < mot.length(); i++) {
            var symbole = String.valueOf(mot.charAt(i));
            Set<Integer> nouveauxEtatsCourants = new HashSet<>();
            for (var etat : etatsCourants) {
                for (var transition : transitions.getOrDefault(etat, Collections.emptyList())) {
                    // Si le symbole existe dans la transition courante alors on continue
                    if (transition.symbole().equals(symbole)) {
                        nouveauxEtatsCourants.add(transition.sortie());
                    }
                }
            }
            etatsCourants = nouveauxEtatsCourants;
        }

        // Vérification si l'état courant est final
        for (var etat : etatsCourants) {
            if (etatsTerminaux.contains(etat)) {
                return true;
            }
        }

        return false;
    }

    public D4_Automate complementaire() {
        Set<Integer> nouveauxEtatsTerminaux = new HashSet<>();
        // On échange tous les états terminaux en non terminaux et inversement
        for (int etat = 0; etat < this.nbEtats; etat++) {
            if (!this.etatsTerminaux.contains(etat)) {
                nouveauxEtatsTerminaux.add(etat);
            }
        }
        return new D4_Automate(this.alphabet, this.nbEtats, this.etatsInitiaux, nouveauxEtatsTerminaux.stream().toList(), this.nbTransitions, this.transitions);
    }

    public D4_Automate minimiser(boolean log) {
        if (alphabet.isEmpty() && nbTransitions == 0) return this;

        if (!estUnAutomateDeterministe(false) || !estUnAutomateComplet(false)) {
            throw new IllegalStateException("L'automate doit être déterministe et complet pour être minimisé");
        }

        if (log && this.transitions.equals(this.minimiser(false).transitions)) {
            System.out.println("L'automate est déjà minimisé");
            return this;
        }

        if (alphabet.isEmpty() && nbTransitions == 0) return this;

        // On applique l'algorithme de partition pour minimiser l'automate
        // On commence avec deux partitions, les T et NT
        List<Set<Integer>> partition = new ArrayList<>();
        Set<Integer> etatsTerminauxSet = new HashSet<>(etatsTerminaux);
        Set<Integer> etatsNonTerminauxSet = IntStream.range(0, nbEtats).boxed().filter(etat -> !etatsTerminauxSet.contains(etat)).collect(Collectors.toSet());

        partition.add(etatsNonTerminauxSet);
        partition.add(etatsTerminauxSet);

        int iteration = 0;
        boolean hasChanged;
        do {
            hasChanged = false;

            var tmpPartition = partition.stream().map(l -> l.stream().map(sommet -> D4_AutomateFormatter.etatMappings.getOrDefault(sommet, String.valueOf(sommet))).toList()).toList();
            if (log) System.out.println("Partition à l'itération " + iteration + " : " + tmpPartition);

            List<Set<Integer>> newPartition = new ArrayList<>();

            for (Set<Integer> group : partition) {
                Map<Map<Integer, Integer>, Set<Integer>> subGroups = new HashMap<>();

                for (int etat : group) {
                    Map<Integer, Integer> signature = new HashMap<>();

                    for (String symbole : alphabet) {
                        D4_Transition transition = transitions.get(etat).stream()
                                .filter(t -> t.symbole().equals(symbole))
                                .findFirst().orElseThrow();

                        int destinationGroup = -1;
                        for (int i = 0; i < partition.size(); i++) {
                            if (partition.get(i).contains(transition.sortie())) {
                                destinationGroup = i;
                                break;
                            }
                        }
                        signature.put(symbole.hashCode(), destinationGroup);
                    }

                    subGroups.computeIfAbsent(signature, k -> new HashSet<>()).add(etat);
                }

                newPartition.addAll(subGroups.values());
                hasChanged |= subGroups.size() > 1;
            }

            partition = newPartition;
            iteration++;
        } while (hasChanged);

        Map<Integer, Integer> fusionEtats = new HashMap<>();
        for (int i = 0; i < partition.size(); i++) {
            for (int etat : partition.get(i)) {
                fusionEtats.put(etat, i);
            }
        }

        var oldMapping = new HashMap<>(D4_AutomateFormatter.etatMappings);
        D4_AutomateFormatter.clear();
        Map<Integer, List<D4_Transition>> nouvellesTransitions = new HashMap<>();
        for (int i = 0; i < partition.size(); i++) {
            var sommetsMapping = partition.get(i).stream().map(t -> oldMapping.getOrDefault(t, String.valueOf(t))).collect(Collectors.toSet());
            D4_AutomateFormatter.addMapping(i, sommetsMapping.stream().map(String::valueOf).collect(Collectors.joining("-")));
            int etatRepresentant = partition.get(i).iterator().next();
            int finalI = i;
            List<D4_Transition> transitionsRepresentant = transitions.get(etatRepresentant).stream().map(t -> new D4_Transition(finalI, t.symbole(), fusionEtats.get(t.sortie()))).collect(Collectors.toList());
            nouvellesTransitions.put(i, transitionsRepresentant);
        }

        List<Integer> nouveauxEtatsInitiaux = this.etatsInitiaux.stream().map(fusionEtats::get).distinct().collect(Collectors.toList());
        List<Integer> nouveauxEtatsTerminaux = this.etatsTerminaux.stream().map(fusionEtats::get).distinct().collect(Collectors.toList());

        int nouveauNbEtats = partition.size();
        int nouveauNbTransitions = (int) nouvellesTransitions.values().stream().mapToLong(Collection::size).sum();

        return new D4_Automate(this.alphabet, nouveauNbEtats, nouveauxEtatsInitiaux, nouveauxEtatsTerminaux, nouveauNbTransitions, nouvellesTransitions);
    }

    private Set<Integer> calculerEpsilonFermeture(int etat) {
        Set<Integer> fermeture = new HashSet<>();
        Stack<Integer> pile = new Stack<>();
        pile.push(etat);

        while (!pile.isEmpty()) {
            int sommet = pile.pop();
            fermeture.add(sommet);

            for (D4_Transition transition : transitions.getOrDefault(sommet, List.of())) {
                if (transition.symbole().equals("ε") && !fermeture.contains(transition.sortie())) {
                    pile.push(transition.sortie());
                }
            }
        }
        return fermeture;
    }

    public D4_Automate supprimerEpsilonTransitions() {
        if (this.transitions.values().stream().flatMap(Collection::stream).noneMatch(t -> t.symbole().equals("ε"))) {
            return this;
        }
        // On applique l'algorithme classique en créeant des super-sommets issue du parcours des possibilités via les transitions epsilon
        System.out.println("ε-transitions détectées, suppression de ces transitions :");
        Map<Integer, List<D4_Transition>> nouveauxTransitions = new HashMap<>();
        Set<Integer> nouvellesSorties = new HashSet<>();
        Queue<Set<Integer>> toVisit = new LinkedList<>();
        var nouveauEtatNb = nbEtats;
        System.out.println("e-clôtures :");
        for (int i = 0; i < nouveauEtatNb; i++) {
            List<D4_Transition> nouveauTransitions = new ArrayList<>();

            var nouveauEtatsFusionné = calculerEpsilonFermeture(i);
            System.out.println(i + "' = " + nouveauEtatsFusionné);
            D4_AutomateFormatter.addMapping(i, i + "'");
            for (var symbole : alphabet) {
                if (symbole.equals("ε")) continue;

                List<D4_Transition> transitSymbols = new ArrayList<>();

                for (Integer nEtat : nouveauEtatsFusionné) {
                    if (etatsTerminaux.contains(nEtat)) {
                        nouvellesSorties.add(i);
                    }
                    var tmp = transitions.getOrDefault(nEtat, List.of()).stream().filter(t -> t.symbole().equals(symbole)).toList();
                    int finalI = i;
                    transitSymbols.addAll(tmp.stream().map(t -> new D4_Transition(finalI, symbole, t.sortie())).toList());
                }
                nouveauTransitions.addAll(transitSymbols);

                if (transitSymbols.size() > 1) {
                    toVisit.add(transitSymbols.stream().map(D4_Transition::sortie).collect(Collectors.toSet()));
                }
            }

            nouveauxTransitions.put(i, nouveauTransitions);
        }
        List<String> visité = new ArrayList<>();
        while (!toVisit.isEmpty()) {
            var etatAVisiter = toVisit.poll();
            var str = etatAVisiter.stream().map(String::valueOf).map(r -> r + "'").collect(Collectors.joining("-"));
            if (visité.contains(str)) continue;
            visité.add(str);
            D4_AutomateFormatter.addMapping(nouveauEtatNb, str);
            List<D4_Transition> nouveauTransitions = new ArrayList<>();
            for (var symbole : alphabet) {
                if (symbole.equals("ε")) continue;

                List<D4_Transition> transitSymbols = new ArrayList<>();
                for (Integer nEtat : etatAVisiter) {
                    if (nouvellesSorties.contains(nEtat)) {
                        nouvellesSorties.add(nouveauEtatNb);
                    }
                    var tmp = nouveauxTransitions.getOrDefault(nEtat, List.of()).stream().filter(t -> t.symbole().equals(symbole)).toList();
                    int finalI = nouveauEtatNb;
                    transitSymbols.addAll(tmp.stream().map(t -> new D4_Transition(finalI, symbole, t.sortie())).toList());
                }

                nouveauTransitions.addAll(transitSymbols);

                if (transitSymbols.size() > 1) {
                    toVisit.add(transitSymbols.stream().map(D4_Transition::sortie).collect(Collectors.toSet()));
                }
            }
            nouveauxTransitions.put(nouveauEtatNb, nouveauTransitions);
            nouveauEtatNb++;
        }

        var nAlphabet = new HashSet<>(alphabet);
        nAlphabet.remove("ε");
        int nouveauNbTransitions = (int) nouveauxTransitions.values().stream().mapToLong(Collection::size).sum();

        return new D4_Automate(nAlphabet, nouveauEtatNb, etatsInitiaux, new ArrayList<>(nouvellesSorties), nouveauNbTransitions, nouveauxTransitions);
    }

}
