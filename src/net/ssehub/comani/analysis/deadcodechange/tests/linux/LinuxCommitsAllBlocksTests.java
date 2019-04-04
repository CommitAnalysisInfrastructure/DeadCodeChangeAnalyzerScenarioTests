package net.ssehub.comani.analysis.deadcodechange.tests.linux;

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

import net.ssehub.comani.analysis.AnalysisSetupException;
import net.ssehub.comani.analysis.deadcodechange.diff.AnalysisResult;
import net.ssehub.comani.analysis.deadcodechange.tests.AbstractCommitsTests;
import net.ssehub.comani.extraction.ExtractionSetupException;

/**
 * This class provides some {@link net.ssehub.comani.analysis.deadcodechange.core.DeadCodeChangeAnalyzer} tests based on
 * commit files containing commits from the Linux kernel. The commit analysis is configured to consider changes to all
 * blocks in the code files.
 * 
 * @author Christian Kroeher
 *
 */
@RunWith(Parameterized.class)
public class LinuxCommitsAllBlocksTests extends AbstractCommitsTests {

    /**
     * The directory in which the test commit files are located. Each file contains the information of a particular
     * Linux kernel commit.
     */
    private static final File TEST_COMMITS_DIRECTORY = new File("./testdata/linux");
    
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
     * @see LinuxCommitsAllBlocksTests#setUp(File, String[], boolean)
     */
    private static final Object[][] EXPECTED_RESULTS = new Object[][]{
        {"1ce6311.txt", new String[]{}, false, false},
        {"1dff333.txt", new String[]{}, false, false},
        {"2572f00.txt", new String[]{"/arch/mips/include/asm/mach-pic32/cpu-feature-overrides.h",
            "/arch/mips/include/asm/mach-pic32/irq.h",
            "/arch/mips/include/asm/mach-pic32/pic32.h",
            "/arch/mips/include/asm/mach-pic32/spaces.h",
            "/arch/mips/pic32/pic32mzda/early_console.c",
            "/arch/mips/pic32/pic32mzda/early_pin.h",
            "/arch/mips/pic32/pic32mzda/init.c",
            "/arch/mips/pic32/pic32mzda/pic32mzda.h",
            "/include/linux/platform_data/sdhci-pic32.h"}, true, true},
        {"35a7051.txt", new String[]{}, false, false},
        {"4294616.txt", new String[]{}, false, false},
        {"5793e27.txt", new String[]{"/arch/arc/include/asm/irqflags-compact.h",
            "/arch/arc/include/asm/irqflags.h",
            "/arch/arc/kernel/intc-compact.c",
            "/arch/arc/kernel/irq.c"}, false, false},
        {"5b3f341.txt", new String[]{}, false, false},
        {"60a27d6.txt", new String[]{}, false, false},
        {"60ff189.txt", new String[]{}, false, false},
        {"79c7c7a.txt", new String[]{}, false, false},
        {"b4c45fe.txt", new String[]{"/drivers/pinctrl/qcom/pinctrl-ssbi-gpio.c",
            "/drivers/pinctrl/qcom/pinctrl-ssbi-mpp.c"}, true, true},
        {"ba12ac2.txt", new String[]{}, false, false},
        {"c2e13cc.txt", new String[]{}, false, false},
        {"c61f4d5.txt", new String[]{}, false, false},
        {"dd739ea.txt", new String[]{}, false, false},
        {"efde611.txt", new String[]{}, false, false},
        {"f953ccd.txt", new String[]{}, false, false},
        {"fbfbc48.txt", new String[]{}, false, false},
        {"fcf6c5e.txt", new String[]{}, false, false} 
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
     * is set during {@link #LinuxCommitsAllBlocksTests(String, String[], boolean, boolean)} and by calling 
     * {@link #getResult(String)}.
     */
    private AnalysisResult actualResult;
    
    /**
     * Constructs a new {@link LinuxCommitsAllBlocksTests} instance.
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
    public LinuxCommitsAllBlocksTests(String testcommitFileName, String[] expectedCodeChanges,
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
        setUp(TEST_COMMITS_DIRECTORY, testCommitFileNames, true);
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
