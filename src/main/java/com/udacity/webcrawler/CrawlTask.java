package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

public final class CrawlTask extends RecursiveAction {
    private final Clock clock;
    private final Duration timeout;
    private final Instant deadline;
    private final String url;
    private final Map<String, Integer> counts;
    private final Set<String> visitedUrls;
    private final PageParserFactory pageParserFactory;
    private final int maxDepth;
    private final List<Pattern> ignoredUrls;

    public CrawlTask(Clock clock, Duration timeout, Instant deadline, String url,
                     Map<String, Integer> counts,
                     Set<String> visitedUrls,
                     PageParserFactory pageParserFactory, int maxDepth, List<Pattern> ignoredUrls) {
        this.clock = clock;
        this.timeout = timeout;
        this.deadline = deadline;
        this.url = url;
        this.counts = counts;
        this.visitedUrls = visitedUrls;
        this.pageParserFactory = pageParserFactory;
        this.maxDepth = maxDepth;
        this.ignoredUrls = ignoredUrls;
    }

    @Override
    protected void compute() {
        if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
            return;
        }
        for (Pattern pattern : ignoredUrls) {
            if (pattern.matcher(url).matches()) {
                return;
            }
        }
        if (visitedUrls.contains(url)) {
            return;
        }
        visitedUrls.add(url);
        PageParser.Result result = pageParserFactory.get(url).parse();
        for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
            counts.compute(e.getKey(), (k, v) -> {
                if (v == null){
                    return e.getValue();
                } else {
                    return v + e.getValue();
                }
            });
        }
        List<CrawlTask> subtasks = result.getLinks().stream()
                .map(link -> new CrawlTask(clock, timeout, deadline, link, counts, visitedUrls, pageParserFactory, maxDepth - 1, ignoredUrls))
                .toList();
        invokeAll(subtasks);
    }
}
