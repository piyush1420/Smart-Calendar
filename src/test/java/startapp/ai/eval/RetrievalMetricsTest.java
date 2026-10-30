package startapp.ai.eval;

import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the metrics themselves. Verifying evaluation logic.
 */
public class RetrievalMetricsTest {

    private static final double EPS = 1e-9;

    @Test
    public void precisionCountsRelevantInTopK() {
        List<String> ranked = List.of("a", "b", "c", "d", "e"); // a and c are relevant
        Set<String> relevant = Set.of("a", "c", "x");
        assertEquals(2.0 / 3, RetrievalMetrics.precisionAtK(ranked, relevant, 3), EPS);
        assertEquals(2.0 / 5, RetrievalMetrics.precisionAtK(ranked, relevant, 5), EPS);
    }

    @Test
    public void recallIsHitsOverAllRelevant() {
        List<String> ranked = List.of("a", "b", "c", "d", "e");
        Set<String> relevant = Set.of("a", "c", "x"); // 'x' is never retrieved
        assertEquals(2.0 / 3, RetrievalMetrics.recallAtK(ranked, relevant, 5), EPS);
        assertEquals(1.0 / 3, RetrievalMetrics.recallAtK(ranked, relevant, 1), EPS); // only 'a' in top1
    }

    @Test
    public void reciprocalRankUsesFirstRelevant() {
        List<String> ranked = List.of("b", "a", "c"); // first relevant ('a') sits at rank 2
        Set<String> relevant = Set.of("a", "c");
        assertEquals(1.0 / 2, RetrievalMetrics.reciprocalRank(ranked, relevant), EPS);
    }

    @Test
    public void reciprocalRankIsZeroWhenNothingRelevantFound() {
        assertEquals(0.0, RetrievalMetrics.reciprocalRank(List.of("x", "y"), Set.of("a")), EPS);
    }

    @Test
    public void ndcgIsOneForPerfectRanking() {
        List<String> ranked = List.of("a", "c", "b"); // both relevant items ranked first
        Set<String> relevant = Set.of("a", "c");
        assertEquals(1.0, RetrievalMetrics.ndcgAtK(ranked, relevant, 3), EPS);
    }

    @Test
    public void ndcgPenalizesRelevantItemsRankedLower() {
        List<String> ranked = List.of("b", "a", "c"); // relevant items pushed to ranks 2 and 3
        Set<String> relevant = Set.of("a", "c");
        double dcg  = 1.0 / (Math.log(3) / Math.log(2)) + 1.0 / (Math.log(4) / Math.log(2));
        double idcg = 1.0 / (Math.log(2) / Math.log(2)) + 1.0 / (Math.log(3) / Math.log(2));
        assertEquals(dcg / idcg, RetrievalMetrics.ndcgAtK(ranked, relevant, 3), EPS);
    }
}