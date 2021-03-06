/*
 *  IssueAnalyzer - https://github.com/F-Scippacercola/anpr-github-metrics
 *  Copyright (c) 2017 F. Scippacercola, E. Battista
 *
 *  See the website for additional information about the copyright.
 *  Please, visit also our website: http://fervento.systems
 */
package systems.fervento.gitlabissueanalyzer.plugins;

import io.swagger.model.AnalysisRequest;
import io.swagger.model.AnaylsisResult;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import systems.fervento.gitlabissueanalyzer.plugins.utils.AnalysisResultBuilder;
import systems.fervento.gitlabissueanalyzer.IssuesAnalyzerPlugin;
import systems.fervento.gitlabissueanalyzer.issuefetcher.IssuesFetcher;
import systems.fervento.gitlabissueanalyzer.plugins.utils.IssueStatePluginHelper;
import systems.fervento.gitlabissueanalyzer.plugins.utils.PluginDeserializerHelper;
import systems.fervento.gitlabissueanalyzer.plugins.utils.SelectionModePluginHelper;

/**
 *
 * @author nonplay
 */
public class IssuesWithLabels implements IssuesAnalyzerPlugin {
    
    public final static String KEY_IN_LABEL_LIST = "labelList";
    public final static String KEY_IN_LABEL_LIST_MODE = "labelListMode";
    
    public final static String KEY_OUT_TOTAL_ISSUES = "totalIssues";
    public final static String KEY_OUT_ISSUES = "issues";    

    @Override
    public String getName() {
        return "IssuesWithLabels";
    }
     
    @Override
    public AnaylsisResult analyzeIssues(AnalysisRequest analysisRequest, IssuesFetcher issuesFetcher, String gitHubUser, String gitHubRepo) {
        try {
            Set<String> labelBlackList = new HashSet<>(PluginDeserializerHelper.deserializeList(analysisRequest, KEY_IN_LABEL_LIST));
            String desiredIssueState = IssueStatePluginHelper.deserializeIssueState(analysisRequest);
            boolean wantIsPresent = SelectionModePluginHelper.deserializeSelectionMode(analysisRequest, KEY_IN_LABEL_LIST_MODE, SelectionModePluginHelper.VALUE_IN_MODE_EXCLUDED);
            
            List<Issue> issuesWithoutTheLabels = new ArrayList<>();
            for (Issue issue : issuesFetcher.getIssues(gitHubUser, gitHubRepo)) {
                if (IssueStatePluginHelper.satisfyState(issue, desiredIssueState)
                        && issue.getLabels().stream()
                            .map(Label::getName)
                            .filter(labelBlackList::contains)
                            .findAny().isPresent() == wantIsPresent) {
                    issuesWithoutTheLabels.add(issue);
                }                
            }
            
            return AnalysisResultBuilder.build(this)
                    .with(KEY_OUT_TOTAL_ISSUES, issuesWithoutTheLabels.size())
                    .with(KEY_OUT_ISSUES, issuesWithoutTheLabels)
                    .build();
            
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    
}