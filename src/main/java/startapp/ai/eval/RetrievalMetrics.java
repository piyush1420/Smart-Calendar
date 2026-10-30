package startapp.ai.eval;

import java.util.List;
import java.util.Set;

/**
 * Pure ranked-retrieval metrics with binary relevance.
 * No Spring, no external dependencies — trivially unit-testable.
 *
 * In every method:
 *   - {@code ranked}   = event IDs returned by your search, best-first
 *   - {@code relevant} = the set of event IDs that count as correct for the query
 */
public final class RetrievalMetrics {

    private RetrievalMetrics() {}

    /**
     * Fraction of the top-k results that are relevant.
     * Denominator is min(k, results returned), so a search that returns
     * fewer than k items isn't unfairly penalised on the missing slots.
     */
    public static double precisionAtK(List<String> ranked, Set<String> relevant, int k) {
        if (k <= 0) throw new IllegalArgumentException("k must be > 0");
        int limit = Math.min(k, ranked.size());
        if (limit == 0) return 0.0;
        int hits = 0;
        for (int i = 0; i < limit; i++) {
            if (relevant.contains(ranked.get(i))) hits++;
        }
        return (double) hits / limit;
    }

    /** Fraction of ALL relevant items that appear in the top k. */
    public static double recallAtK(List<String> ranked, Set<String> relevant, int k) {
        if (k <= 0) throw new IllegalArgumentException("k must be > 0");
        if (relevant.isEmpty()) return 0.0;
        int limit = Math.min(k, ranked.size());
        int hits = 0;
        for (int i = 0; i < limit; i++) {
            if (relevant.contains(ranked.get(i))) hits++;
        }
        return (double) hits / relevant.size();
    }

    /** 1 / rank of the first relevant result (1-indexed). 0 if none were found. */
    public static double reciprocalRank(List<String> ranked, Set<String> relevant) {
        for (int i = 0; i < ranked.size(); i++) {
            if (relevant.contains(ranked.get(i))) return 1.0 / (i + 1);
        }
        return 0.0;
    }

    /**
     * Normalised Discounted Cumulative Gain at k, binary relevance.
     * Rewards putting relevant items higher in the ranking, not just present.
     */
    public static double ndcgAtK(List<String> ranked, Set<String> relevant, int k) {
        if (k <= 0) throw new IllegalArgumentException("k must be > 0");
        if (relevant.isEmpty()) return 0.0;
        int limit = Math.min(k, ranked.size());

        double dcg = 0.0;
        for (int i = 0; i < limit; i++) {
            if (relevant.contains(ranked.get(i))) {
                dcg += 1.0 / log2(i + 2); // rank = i + 1, discount = log2(rank + 1)
            }
        }

        double idcg = 0.0;
        int idealHits = Math.min(k, relevant.size());
        for (int i = 0; i < idealHits; i++) {
            idcg += 1.0 / log2(i + 2);
        }

        return idcg == 0.0 ? 0.0 : dcg / idcg;
    }

    private static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }
}