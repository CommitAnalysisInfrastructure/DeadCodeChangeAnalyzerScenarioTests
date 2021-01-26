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

import net.ssehub.comani.analysis.AnalysisSetupException;
import net.ssehub.comani.analysis.VerificationRelevantResult;
import net.ssehub.comani.extraction.ExtractionSetupException;

/**
 * This class provides some {@link net.ssehub.comani.analysis.deadcodechange.core.DeadCodeChangeAnalyzer} tests based on
 * commit files containing commits from the Coreboot firmware. The commit analysis is configured to consider changes to
 * all blocks in the code files.
 * 
 * @author Christian Kroeher
 *
 */
@RunWith(Parameterized.class)
public class CorebootCommitsTests extends AbstractCommitsTests {

    /**
     * The directory in which the test commit files are located. Each file contains the information of a particular
     * Coreboot firmware commit.
     */
    private static final File TEST_COMMITS_DIRECTORY = new File("./testdata/coreboot");
    
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
     * @see CorebootCommitsTests#setUp(File, String[], boolean)
     */
    private static final Object[][] EXPECTED_RESULTS = new Object[][]{
        {"00093a8.txt", new String[]{"/src/arch/x86/include/arch/acpi.h",
            "/src/cpu/x86/mtrr/mtrr.c",
            "/src/include/cpu/x86/lapic.h"}, false, true},
        {"000bf83.txt", new String[]{"/src/cpu/x86/lapic/lapic_cpu_init.c"}, false, false},
        {"0010bf6.txt", new String[]{}, true, true},
        {"005028e.txt", new String[]{"/src/mainboard/amd/olivehill/get_bus_conf.c",
            "/src/mainboard/amd/parmer/get_bus_conf.c",
            "/src/mainboard/amd/persimmon/get_bus_conf.c",
            "/src/mainboard/amd/thatcher/get_bus_conf.c",
            "/src/mainboard/asrock/imb-a180/get_bus_conf.c",
            "/src/mainboard/asus/f2a85-m/get_bus_conf.c",
            "/src/mainboard/gizmosphere/gizmo/get_bus_conf.c",
            "/src/mainboard/hp/pavilion_m6_1035dx/get_bus_conf.c",
            "/src/mainboard/jetway/nf81-t56n-lf/get_bus_conf.c",
            "/src/mainboard/lippert/frontrunner-af/get_bus_conf.c",
            "/src/mainboard/lippert/toucan-af/get_bus_conf.c"}, false, false},
        {"0054afa.txt", new String[]{}, true, true},
        {"006364e.txt", new String[]{"/src/northbridge/amd/pi/00630F01/chip.h",
            "/src/northbridge/amd/pi/00630F01/northbridge.c",
            "/src/northbridge/amd/pi/00630F01/northbridge.h",
            "/src/northbridge/amd/pi/00630F01/pci_devs.h",
            "/src/northbridge/amd/pi/BiosCallOuts.h"}, true, true},
        {"00636b0.txt", new String[]{"/src/northbridge/intel/sandybridge/northbridge.c",
            "/src/northbridge/intel/sandybridge/pei_data.h",
            "/src/northbridge/intel/sandybridge/raminit.c",
            "/src/northbridge/intel/sandybridge/raminit.h",
            "/src/northbridge/intel/sandybridge/sandybridge.h"}, true, true},
        {"00809eb.txt", new String[]{}, false, false},
        {"0092c99.txt", new String[]{"/src/northbridge/intel/i945/gma.c"}, false, true},
        {"00b579a.txt", new String[]{"/src/cpu/intel/microcode/microcode.c",
            "/src/include/cpu/intel/microcode.h"}, true, true},
        {"2a19fb1.txt", new String[]{"/src/mainboard/advansus/a785e-i/acpi_tables.c",
            "/src/mainboard/amd/bimini_fam10/acpi_tables.c",
            "/src/mainboard/amd/tilapia_fam10/acpi_tables.c",
            "/src/mainboard/asus/m4a78-em/acpi_tables.c",
            "/src/northbridge/amd/amdfam10/northbridge.c",
            "/src/southbridge/amd/cimx/sb800/late.c"}, true, true},
        {"2b7c88f.txt", new String[]{}, false, false},
        {"398e84c.txt", new String[]{"/src/arch/armv7/boot/coreboot_table.c"}, false, false},
        {"480b37f.txt", new String[]{}, false, false},
        {"74234eb.txt", new String[]{}, false, true},
        {"9855895.txt", new String[]{}, false, true},
        {"9d6be3e.txt", new String[]{"/src/mainboard/iwill/DK8S2/auto.c",
            "/src/mainboard/iwill/DK8X/auto.c",
            "/src/mainboard/iwill/dk8X/auto.c",
            "/src/mainboard/iwill/dk8s2/auto.c"}, false, false},
        {"c3e728f.txt", new String[]{}, true, false},
        {"eedf7a6.txt", new String[]{}, true, true},
        {"f040858.txt", new String[]{}, true, true},
        {"07f5b62.txt", new String[]{}, false, true}
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
     * The actual {@link VerificationRelevantResult} of the commit file for which this test class is currently executed.
     * Its value is set during {@link #CorebootCommitsAllBlocksTests(String, String[], boolean, boolean)} and by calling
     * {@link #getResult(String)}.
     */
    private VerificationRelevantResult actualResult;
    
    /**
     * Constructs a new {@link CorebootCommitsTests} instance.
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
    public CorebootCommitsTests(String testcommitFileName, String[] expectedCodeChanges,
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
