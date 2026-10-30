package startapp.ai.eval;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class EventSearchEvaluator {

    public record QueryCase(String queryId, String queryText, Set<String> relevantIds) {}

    @FunctionalInterface
    public interface EventSearcher {
        List<String> search(String queryText, int k);
    }

    public record EvaluationReport(
            int k, int queries,
            double precisionAtK, double recallAtK, double mrr, double ndcgAtK,
            List<PerQuery> perQuery) {

        public record PerQuery(String queryId, double precision, double recall,
                               double reciprocalRank, double ndcg) {}

        public String format() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Retrieval evaluation @k=%d over %d queries%n", k, queries));
            sb.append(String.format("  Precision@%-2d : %.3f%n", k, precisionAtK));
            sb.append(String.format("  Recall@%-2d    : %.3f%n", k, recallAtK));
            sb.append(String.format("  MRR          : %.3f%n", mrr));
            sb.append(String.format("  nDCG@%-2d      : %.3f%n", k, ndcgAtK));
            return sb.toString();
        }
    }

    public EvaluationReport evaluate(List<QueryCase> cases, EventSearcher searcher, int k) {
        if (cases.isEmpty()) throw new IllegalArgumentException("No query cases provided");
        double pSum = 0, rSum = 0, rrSum = 0, nSum = 0;
        List<EvaluationReport.PerQuery> rows = new ArrayList<>();
        for (QueryCase c : cases) {
            List<String> ranked = searcher.search(c.queryText(), k);
            double p  = RetrievalMetrics.precisionAtK(ranked, c.relevantIds(), k);
            double r  = RetrievalMetrics.recallAtK(ranked, c.relevantIds(), k);
            double rr = RetrievalMetrics.reciprocalRank(ranked, c.relevantIds());
            double n  = RetrievalMetrics.ndcgAtK(ranked, c.relevantIds(), k);
            pSum += p; rSum += r; rrSum += rr; nSum += n;
            rows.add(new EvaluationReport.PerQuery(c.queryId(), p, r, rr, n));
        }
        int q = cases.size();
        return new EvaluationReport(k, q, pSum / q, rSum / q, rrSum / q, nSum / q, rows);
    }
}