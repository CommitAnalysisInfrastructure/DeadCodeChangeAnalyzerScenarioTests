package net.ssehub.comani.analysis.deadcodechange.tests;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import net.ssehub.comani.analysis.AnalysisSetupException;
import net.ssehub.comani.analysis.deadcodechange.core.DeadCodeChangeAnalyzer;
import net.ssehub.comani.analysis.deadcodechange.diff.AnalysisResult;
import net.ssehub.comani.data.CommitQueue;
import net.ssehub.comani.data.CommitQueue.QueueState;
import net.ssehub.comani.extraction.ExtractionSetupException;
import net.ssehub.comani.extraction.git.GitCommitExtractor;
import net.ssehub.comani.utility.FileUtilities;

/**
 * This abstract class provides common attributes and methods used by the specific test classes, which perform tests
 * with commit files.
 * 
 * @author Christian Kroeher
 *
 */
public abstract class AbstractCommitsTests {
    
    /**
     * The definition of whether the commit analysis terminated successfully (<code>true</code>) or not
     * (<code>false</code>).
     */
    protected static boolean commitAnalysisSuccessful;
    
    /**
     * The results of the {@link DeadCodeChangeAnalyzer} in terms of the commit id (key) and their specific 
     * {@link AnalysisResult}s (value).
     */
    protected static HashMap<String, AnalysisResult> analysisResults;
    
    /**
     * The regular expression for identifying variability model files.
     */
    private static final String VM_FILES_REGEX = ".*/Kconfig((\\.|\\-|\\_|\\+|\\~).*)?";
    
    /**
     * The regular expression for identifying code files.
     */
    private static final String CODE_FILES_REGEX = ".*/.*\\.[hcS]((\\.|\\-|\\_|\\+|\\~).*)?";
    
    /**
     * The regular expression for identifying build files.
     */
    private static final String BUILD_FILES_REGEX = ".*/(Makefile|Kbuild)((\\.|\\-|\\_|\\+|\\~).*)?";
    
    /**
     * Performs the commit extraction and analysis for each commit represented by an individual file in the
     * given test commits directory and sets the values for {@link #commitAnalysisSuccessful} as well as
     * {@link #analysisResults}, which serve as input for the respective tests.
     * 
     * @param testCommitsDirectory the {@link File} denoting the directory in which the test commit files are located
     * @param testCommitFileNames the names of the test commit files located in the given directory
     * @throws ExtractionSetupException if instantiating the commit extractor fails
     * @throws AnalysisSetupException if instantiating the commit analyzer fails
     */
    public static void setUp(File testCommitsDirectory, String[] testCommitFileNames)
            throws ExtractionSetupException, AnalysisSetupException {
        System.out.println("## Setting up tests based on commits located at \"" + testCommitsDirectory.getPath() 
                + "\" ##");
        // Define the required properties for the commit extractor and analyzer
        Properties pluginProperties = new Properties();
        pluginProperties.setProperty("core.version_control_system", "git");
        pluginProperties.setProperty("analysis.output", ""); // Unused but mandatory
        pluginProperties.setProperty("analysis.dead_code_change_analyzer.vm_files_regex", VM_FILES_REGEX);
        pluginProperties.setProperty("analysis.dead_code_change_analyzer.code_files_regex", CODE_FILES_REGEX);
        pluginProperties.setProperty("analysis.dead_code_change_analyzer.build_files_regex", BUILD_FILES_REGEX);
        // Instantiate the common commit queue for the commit extractor and analyzer
        CommitQueue commitQueue = new CommitQueue(testCommitFileNames.length);
        // Instantiate the commit extractor and analyzer
        GitCommitExtractor commitExtractor = new GitCommitExtractor(pluginProperties, commitQueue);
        DeadCodeChangeAnalyzer commitAnalyzer = new DeadCodeChangeAnalyzer(pluginProperties, commitQueue);
        // Extract the commits based on the commit files in the test commits directory
        commitQueue.setState(QueueState.OPEN);
        extractCommits(commitExtractor, testCommitsDirectory, testCommitFileNames);
        commitQueue.setState(QueueState.CLOSED); // Actual closing after all commits are analyzed
        // Analyze the extracted commits
        commitAnalysisSuccessful = commitAnalyzer.analyze();
        // Get the analysis results
        analysisResults = commitAnalyzer.getResults();
    }
    
    /**
     * Performs the commit extraction by calling the given commit extractor for the files in the
     * {@link #TEST_COMMITS_DIRECTORY}. However, only the content of those files is passed to the extractor, where the
     * file name matches on of those in the {@link #EXPECTED_RESULTS}.
     * 
     * @param commitExtractor the {@link GitCommitExtractor}, which shall be used to extract the commits
     * @param testCommitsDirectory the {@link File} denoting the directory in which the test commit files are located
     * @param testCommitFileNames the names of the test commit files located in the given directory
     */
    private static void extractCommits(GitCommitExtractor commitExtractor, File testCommitsDirectory,
            String[] testCommitFileNames) {
        File[] testCommitFiles = testCommitsDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean accept = false;
                int testCommitFileNamesCounter = 0;
                while (!accept && testCommitFileNamesCounter < testCommitFileNames.length) {
                    if (testCommitFileNames[testCommitFileNamesCounter].equals(name)) {
                        accept = true;
                    }
                    testCommitFileNamesCounter++;
                }
                return accept;
            }
        });
        for (int i = 0; i < testCommitFiles.length; i++) {
            List<String> commitFileLines = FileUtilities.getInstance().readFile(testCommitFiles[i]);
            StringBuilder commitBuilder = new StringBuilder();
            commitBuilder.append(commitFileLines.get(0));
            for (int j = 1; j < commitFileLines.size(); j++) {
                commitBuilder.append("\n");
                commitBuilder.append(commitFileLines.get(j));
            }
            commitExtractor.extract(commitBuilder.toString());
        }
    }
    
    /**
     * Returns the result of the analysis of the commit denoted by the given commit file name.
     * 
     * @param commitFileName the name of the commit file for which the results should be returned
     * @return the {@link AnalysisResult} of the given commit file or <code>null</code>, if no results for that commit
     *         exist
     */
    protected static AnalysisResult getResult(String commitFileName) {
        return analysisResults.get(commitFileName);
    }
}
