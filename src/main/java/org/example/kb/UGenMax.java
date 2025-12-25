package org.example.kb;

import java.util.*;

public class UGenMax {
    private final List<MTransaction> dataset;
    private final double minEsup;

    private List<MItemset> maximalItemsets = new ArrayList<>();
    private int nodeCount = 0;
    private int prunedCount = 0;
    private Map<String, UGenMaxNode> singleItemNodes = new HashMap<>();

    public UGenMax(List<MTransaction> dataset, double minEsup) {
        this.dataset = dataset;
        this.minEsup = minEsup;
    }

    public List<MItemset> mfi() {
        for(MTransaction t : dataset) {
            t.getUnits().entrySet().stream().toList().forEach(e -> {
                if(!singleItemNodes.containsKey(e.getKey())) {
                    UGenMaxNode newNode = new UGenMaxNode(Set.of(e.getKey()), new HashMap<>());
                    newNode.addTidProb(t.getId(), e.getValue());
                    singleItemNodes.put(e.getKey(), newNode);
                }
                else {
                    singleItemNodes.get(e.getKey()).addTidProb(t.getId(), e.getValue());
                }
            });
        }

        List<UGenMaxNode> firstCandidates = singleItemNodes.values().stream()
            .filter(n -> n.getEsup() >= minEsup)
            .sorted(Comparator.comparingDouble(UGenMaxNode::getEsup))
            .toList();

        for(int i = 0; i < firstCandidates.size(); i++) {
            UGenMaxNode firstCurrent = firstCandidates.get(i);
            backtrack(firstCurrent, firstCandidates.subList(i + 1, firstCandidates.size()));
        }

        maximalItemsets.forEach(itemset -> {System.out.println(itemset.getItems() + " - " + itemset.getExSup());});
        System.out.println("Found: " + maximalItemsets.size() + " itemsets");
        System.out.println("Node count: " + nodeCount);
        System.out.println("Pruned count: " + prunedCount);

        return null;
    }

    private void backtrack(UGenMaxNode current, List<UGenMaxNode> candidates) {
        System.out.println("Current: " + current.getItemset());
//        System.out.println("Candidates: " + candidates.size() + " (" + candidates.getFirst().getItemset() + ")");
        nodeCount++;

        // pruning part
        Set<String> maximalPossible = new HashSet<>(current.getItemset());
        for(UGenMaxNode candidate : candidates) {
            maximalPossible.addAll(candidate.getItemset());
        }
        //System.out.println("Maximal possible: " + maximalPossible);
        for(MItemset i : maximalItemsets) {
            if(i.getItems().containsAll(maximalPossible)) {
                System.out.println("Pruning");
                prunedCount++;
                return;
            }
        }

        List<UGenMaxNode> newCandidates = generateNewCandidates(current, candidates);

        if(newCandidates.isEmpty()) {
            for(MItemset itemset : maximalItemsets) {
                if(itemset.getItems().containsAll(current.getItemset())) {
                    return;
                }
            }
            maximalItemsets.add(new MItemset(
                new HashSet<>(current.getItemset()),
                current.getEsup()
            ));
            return;
        }

        newCandidates.sort(Comparator.comparingDouble(UGenMaxNode::getEsup));
        for(int i = 0; i < newCandidates.size(); i++) {
            UGenMaxNode newCurrent = newCandidates.get(i);
            backtrack(newCurrent, newCandidates.subList(i + 1, newCandidates.size()));
        }
    }

    private List<UGenMaxNode> generateNewCandidates(UGenMaxNode current, List<UGenMaxNode> candidates) {
        List<UGenMaxNode> newCandidates = new ArrayList<>();
        for(UGenMaxNode candidate : candidates) {
            if(candidate.getItemset().equals(current.getItemset())) {
                continue;
            }

            Set<String> combinedItemset = new HashSet<>(candidate.getItemset());
            combinedItemset.addAll(current.getItemset());

            Set<String> addedItemAsSet = new HashSet<>(combinedItemset);
            addedItemAsSet.removeAll(current.getItemset());
            String addedItem = addedItemAsSet.iterator().next();
            Map<String, Double> addedItemTidProd = singleItemNodes.get(addedItem).getTidProb();

            Map<String, Double> combinedTidProb = new HashMap<>();
            current.getTidProb().forEach((tid, prob) -> {
                if(addedItemTidProd.containsKey(tid)) {
                    combinedTidProb.put(tid, prob * addedItemTidProd.get(tid));
                }
            });

            UGenMaxNode newCandidate = new UGenMaxNode(combinedItemset, combinedTidProb);
            if(newCandidate.getEsup() >= minEsup) {
                newCandidates.add(newCandidate);
            }
        }
        return newCandidates;
    }
}
