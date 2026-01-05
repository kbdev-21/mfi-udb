package org.example.ugenmax;

import org.example.data.MItemset;
import org.example.data.MTransaction;

import java.util.*;

public class UGenMax {
    private final List<MTransaction> dataset;
    private final double minEsup;

    private final List<MItemset> maximalItemsets = new ArrayList<>();
    private final Map<String, UGenMaxNode> singleItemNodes = new HashMap<>();
    private int nodeCount = 0;
    private int prunedCount = 0;

    public UGenMax(List<MTransaction> dataset, double minEsup) {
        this.dataset = dataset;
        this.minEsup = minEsup;
    }

    /**
     * Start the mining MFI algorithm.
     * @return the result which is a list of maximal frequent items
     */
    public List<MItemset> mfi() {
        /* Scan through the dataset once to set up the singleItemNodes */
        setUpSingleItemNodes();

        /*
        Generate first candidates which is the 1-item itemsets
        Sort in ASC order to improve performance by increase the pruning chance
        */
        List<UGenMaxNode> firstCandidates = singleItemNodes.values().stream()
            .filter(n -> n.getEsup() >= minEsup)
            .sorted(Comparator.comparingDouble(UGenMaxNode::getEsup))
            .toList();

        /* Start the recursive DFS */
        dfs(new UGenMaxNode(Set.of(""), new HashMap<>()), firstCandidates);

        /* Display the statistics */
        maximalItemsets.forEach(itemset -> {System.out.println(itemset.getItems() + " - " + itemset.getExSup());});
        System.out.println("Found: " + maximalItemsets.size() + " itemsets");
        System.out.println("Node count: " + nodeCount);
        System.out.println("Pruned count: " + prunedCount);

        return maximalItemsets;
    }

    /**
     * Set up the HashMap singleItemNodes by scanning through the dataset.
     */
    private void setUpSingleItemNodes() {
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
    }

    /**
     * The main recursion to achieve the algorithm's deep-first search
     * @param current is the current node
     * @param candidates is the same k-level nodes that will possibly combine with current to
     *                   generate the next k-level candidates for the DFS.
     */
    private void dfs(UGenMaxNode current, List<UGenMaxNode> candidates) {
        System.out.println("Current node: " + current.getItemset());
        nodeCount++;

        if(isNodePrunable(current, candidates)) {
            System.out.println("Pruning");
            prunedCount++;
            return;
        }

        List<UGenMaxNode> newCandidates = generateNewCandidates(current, candidates);

        /* Empty newCandidates means this node is the leaf of the DFS tree.
        * If the leaf's itemset is NOT a subset of one of the founded maximal itemsets,
        * we add it into the maximalItemsets list
        */
        if(newCandidates.isEmpty()) {
            if(isSubsetOfFoundedMaximalItemsets(current.getItemset())) {
                return;
            }
            maximalItemsets.add(new MItemset(
                new HashSet<>(current.getItemset()),
                current.getEsup()
            ));
            return;
        }

        for(int i = 0; i < newCandidates.size(); i++) {
            UGenMaxNode newCurrent = newCandidates.get(i);
            dfs(newCurrent, newCandidates.subList(i + 1, newCandidates.size()));
        }
    }

    /**
     * Check if the current node is prunable. First, calculate the largest possible itemset (LPI)
     * that the current node can generate with the candidates, then check if that LPI is a subset
     * of one of the founded maximal itemsets. If then we can prune the node to reduce a lot of
     * unnecessary branches.
     * @param current
     * @param candidates
     * @return true or false for is prunable or not
     */
    private boolean isNodePrunable(UGenMaxNode current, List<UGenMaxNode> candidates) {
        Set<String> largestPossibleItemset = new HashSet<>(current.getItemset());
        for(UGenMaxNode candidate : candidates) {
            largestPossibleItemset.addAll(candidate.getItemset());
        }

        return isSubsetOfFoundedMaximalItemsets(largestPossibleItemset);
    }

    /**
     * Check if the given itemset is a subset of one of the founded maximal itemsets
     * @param itemset
     * @return true or false for is subset or not
     */
    private boolean isSubsetOfFoundedMaximalItemsets(Set<String> itemset) {
        for(MItemset i : maximalItemsets) {
            if(i.getItems().containsAll(itemset)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generate the next k-level candidates
     * @param current
     * @param candidates
     * @return a list of new candidates
     */
    private List<UGenMaxNode> generateNewCandidates(UGenMaxNode current, List<UGenMaxNode> candidates) {
        if(current.getItemset().equals(Set.of(""))) {
            return new ArrayList<>(candidates);
        }

        List<UGenMaxNode> newCandidates = new ArrayList<>();
        for(UGenMaxNode candidate : candidates) {
            if(candidate.getItemset().equals(current.getItemset())) {
                continue;
            }

            UGenMaxNode newCandidate = combineForNewCandidate(current, candidate);
            if(newCandidate.getEsup() >= minEsup) {
                newCandidates.add(newCandidate);
            }
        }
        return newCandidates;
    }

    /**
     * Combine the current node and a candidate to create new candidate by joining their itemsets and
     * TID/probability maps
     * @param current
     * @param candidate
     * @return the combined node
     */
    private UGenMaxNode combineForNewCandidate(UGenMaxNode current, UGenMaxNode candidate) {
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

        return new UGenMaxNode(combinedItemset, combinedTidProb);
    }
}
