package net.ssehub.comani.analysis.deadcodechange.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import net.ssehub.comani.analysis.AnalysisResult;
import net.ssehub.comani.analysis.AnalysisSetupException;
import net.ssehub.comani.extraction.ExtractionSetupException;

/**
 * This class provides some {@link net.ssehub.comani.analysis.deadcodechange.core.DeadCodeChangeAnalyzer} tests based on
 * artificial commit files. The commit analysis is configured to consider changes to all blocks in the code files.
 * 
 * @author Christian Kroeher
 *
 */
@RunWith(Parameterized.class)
public class ArtificialCommitsTests extends AbstractCommitsTests {

    /**
     * The directory in which the test commit files are located. Each file contains the information of a particular
     * yet artificial commit.
     */
    private static final File TEST_COMMITS_DIRECTORY = new File("./testdata/artificial");
    
    /**
     * The expected results of the analysis. This two-dimensional array contains the following information:
     * <ul>
     * <li>First index: the result of a particular test commit (file) as an array</li>
     * <li>Second index: the four elements required to determine the correctness of the analysis of the respective
     *     commit:</li>
     *     <ul>
     *     <li>Test commit file name including its extension; should never be <code>null</code></li>
     *     <li>Possibly empty set of code file paths denoting those code files requiring a dead code analysis due to
     *         relevant changes; should never be <code>null</code></li>
     *     <li>Boolean value indicating whether the commit changes the build model in a way that requires a dead code
     *         analysis (<code>true</code>) or not (<code>false</code>)</li>
     *     <li>Boolean value indicating whether the commit changes the variability model in a way that requires a dead
     *         code analysis (<code>true</code>) or not (<code>false</code>)</li>
     *     </ul>
     * </ul>
     * The test commit file names are used during the setup of this test class.
     * 
     * @see ArtificialCommitsTests#setUp(File, String[], boolean)
     */
    private static final Object[][] EXPECTED_RESULTS = new Object[][]{
        {"simpleBuildVarChange.txt", new String[]{}, true, false},
        {"simpleBuildChange.txt", new String[]{}, false, false},
        {"multiBuildWithVarChange.txt", new String[]{}, true, false},
        
        {"simpleVariabilityModelVarChange.txt", new String[]{}, false, true},
        {"simpleVariabilityModelChange.txt", new String[]{}, false, false},
        {"multiVariabilityModelWithVarChange.txt", new String[]{}, false, true},
        
        {"simpleCodeVarChange.txt", new String[]{"/Code.c"}, false, false},
        {"simpleCodeChange.txt", new String[]{}, false, false},
        {"multiCodeWithVarChange.txt", new String[]{"/Code.c", "/other/Code.c"}, false, false},
        
        {"singleIfDefNoVarChange.txt", new String[]{"/Code.c"}, false, false},
        
        {"produceDeadConfigBlockChange.txt", new String[]{"/Code.c"}, false, false},
        
        {"produceDeadConfigBlockByElseChange.txt", new String[]{"/Code.c"}, false, false},
        
        {"produceDeadConfigBlockByEndIfChange.txt", new String[]{"/Code.c"}, false, false},
        
        {"addDeadConfigBlockChange.txt", new String[]{"/Code.c"}, false, false},
        
        {"deleteDeadConfigBlockChange.txt", new String[]{"/Code.c"}, false, false},
        
        {"deleteChildBlockChange.txt", new String[]{"/Code.c"}, false, false},
        
        {"addParentBlockChange.txt", new String[]{"/Code.c"}, false, false},
        
        {"comanCodeChanges.txt", new String[]{"/CodeFile6.c", "/CodeFile7.c", "/CodeFile8.c", "/CodeFile10.c",
            "/CodeFile11.c", "/CodeFile12.c", "/CodeFile13.c", "/CodeFile14.c"}, false, false},
        
        {"addChildBlockChange.txt", new String[]{"/Code.c"}, false, false},
        {"deleteConfigBlockChange.txt", new String[]{"/Code.c"}, false, false},
        {"narrowBlockFromVarToNonVarChange.txt", new String[]{"/Code.c"}, false, false}
    };
    
    /**
     * The name of the commit file including its file extension for which this test class is currently executed.
     */
    private String testCommitFileName;
    
    /**
     * The expected set of changed code files, which require a dead code analysis. Never <code>null</code>, but may be
     * empty.
     */
    private String[] expectedCodeChanges;
    
    /**
     * The expected boolean value indicating whether the commit changes the build model in a way that requires a dead
     * code analysis (<code>true</code>) or not (<code>false</code>).
     */
    private boolean expectedBuildChanges;
    
    /**
     * The expected boolean value indicating whether the commit changes the variability model in a way that requires a
     * dead code analysis (<code>true</code>) or not (<code>false</code>).
     */
    private boolean expectedVariabilityModelChanges;
    
    /**
     * The actual {@link AnalysisResult} of the commit file for which this test class is currently executed. Its value
     * is set during {@link #ArtificialCommitsTests(String, String[], boolean, boolean)} and by calling 
     * {@link #getResult(String)}.
     */
    private AnalysisResult actualResult;
    
    /**
     * Constructs a new {@link ArtificialCommitsTests} instance.
     * 
     * @param testcommitFileName the name of the commit file including its file extension for which this test class
     *        should be executed
     * @param expectedCodeChanges the expected set of changed code files, which require a dead code analysis; should
     *        never be <code>null</code>, but can be empty
     * @param expectedBuildChanges the expected boolean value indicating whether the commit (file) changes the build 
     *        model in a way that requires a dead code analysis (<code>true</code>) or not (<code>false</code>)
     * @param expectedVariabilityModelChanges the expected boolean value indicating whether the commit (file) changes
     *        the variability model in a way that requires a dead code analysis (<code>true</code>) or not
     *        (<code>false</code>)
     */
    public ArtificialCommitsTests(String testcommitFileName, String[] expectedCodeChanges,
            boolean expectedBuildChanges, boolean expectedVariabilityModelChanges) {
        this.testCommitFileName = testcommitFileName;
        this.expectedCodeChanges = expectedCodeChanges;
        this.expectedBuildChanges = expectedBuildChanges;
        this.expectedVariabilityModelChanges = expectedVariabilityModelChanges;
        this.actualResult = getResult(testcommitFileName);
    }
    
    /**
     * Calls the {@link #setUp(File, String[], boolean)} of the parent class with the {@link #TEST_COMMITS_DIRECTORY},
     * the test commit file names defined as part of the {@link #EXPECTED_RESULTS}, and <code>true</code> to consider
     * all blocks. 
     * 
     * @throws ExtractionSetupException if instantiating the commit extractor fails
     * @throws AnalysisSetupException if instantiating the commit analyzer fails
     */
    @BeforeClass
    public static void setUp() throws ExtractionSetupException, AnalysisSetupException {
        // Only test those commits currently available in the EXPECTED_RESULTS
        String[] testCommitFileNames = new String[EXPECTED_RESULTS.length];
        for (int i = 0; i < EXPECTED_RESULTS.length; i++) {
            testCommitFileNames[i] = (String) EXPECTED_RESULTS[i][0];
        }
        setUp(TEST_COMMITS_DIRECTORY, testCommitFileNames);
    }    
    
    /**
     * Returns the expected results as parameters for the tests defined in this class.
     * 
     * @return the {@link #EXPECTED_RESULTS} as an object-array list
     */
    @Parameters
    public static List<Object[]> getTestData() {
        return Arrays.asList(EXPECTED_RESULTS);
    }
    
    /**
     * Tests the successful termination of the commit analysis process.
     */
    @Test
    public void testCommitAnalysisSuccessful() {
        assertTrue("The commit analysis process should terminate successfully", commitAnalysisSuccessful);
    }
    
    /**
     * Tests whether the {@link #actualResult} is not <code>null</code>.
     */
    @Test
    public void testAnalysisResultsAvailable() {
        assertNotNull("The analysis results should not be \"null\"", actualResult);
    }
    
    /**
     * Tests whether the number of {@link #expectedCodeChanges} are equal to those in the {@link #actualResult}.
     */
    @Test
    public void testCorrectNumberOfCodeChanges() {
        assertEquals("Numbers of code changes for test file \"" + testCommitFileName + "\" do not match",
                expectedCodeChanges.length, actualResult.getRelevantCodeChanges().size());
    }
    
    /**
     * Tests whether the paths in {@link #expectedCodeChanges} are equal to those in the {@link #actualResult}.
     */
    @Test
    public void testCorrectCodeChanges() {
        List<String> actualCodeChanges = actualResult.getRelevantCodeChanges();
        for (int i = 0; i < expectedCodeChanges.length; i++) {
            assertEquals("Code changes for test file \"" + testCommitFileName + "\" do not match",
                    expectedCodeChanges[i], actualCodeChanges.get(i));
        }
    }
    
    /**
     * Tests whether the {@link #expectedBuildChanges} match those in the {@link #actualResult}.
     */
    @Test
    public void testCorrectBuildChanges() {
        assertEquals("Build changes for test file \"" + testCommitFileName + "\" do not match",
                expectedBuildChanges, actualResult.getRelevantBuildChanges());
    }
    
    /**
     * Tests whether the {@link #expectedVariabilityModelChanges} match those in the {@link #actualResult}.
     */
    @Test
    public void testCorrectVariabilityModelChanges() {
        assertEquals("Variability model changes for test file \"" + testCommitFileName + "\" do not match",
                expectedVariabilityModelChanges, actualResult.getRelevantVariabilityModelChanges());
    }

}
