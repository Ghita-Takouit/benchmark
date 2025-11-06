package com.benchmark.perf_test.jmeter;

import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

import java.io.File;
import java.io.FileInputStream;

/**
 * JMeter Test Runner for IntelliJ IDEA
 * This class allows you to run JMeter tests programmatically from within IntelliJ
 */
public class JMeterTestRunner {

    private static final String JMETER_HOME = System.getProperty("jmeter.home", "/usr/local/Cellar/jmeter/5.6.3/libexec");
    private static final String TEST_PLAN_PATH = "perf-test-plan.jmx";
    private static final String INFLUX_TEST_PLAN_PATH = "perf-test-plan-influxdb-v2.jmx";

    public static void main(String[] args) {
        try {
            // Initialize JMeter
            initializeJMeter();

            // Run the test plan
            String testPlanToRun = args.length > 0 ? args[0] : TEST_PLAN_PATH;
            runTestPlan(testPlanToRun);

        } catch (Exception e) {
            System.err.println("Error running JMeter test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initializeJMeter() {
        // Set JMeter home directory
        JMeterUtils.setJMeterHome(JMETER_HOME);

        // Initialize JMeter properties
        JMeterUtils.loadJMeterProperties(JMETER_HOME + "/bin/jmeter.properties");

        // Initialize locale
        JMeterUtils.initLocale();
    }

    public static void runTestPlan(String testPlanPath) throws Exception {
        System.out.println("Running JMeter test plan: " + testPlanPath);

        // Load test plan
        File testPlanFile = new File(testPlanPath);
        if (!testPlanFile.exists()) {
            throw new RuntimeException("Test plan file not found: " + testPlanPath);
        }

        HashTree testPlanTree = SaveService.loadTree(testPlanFile);

        // Create summariser for console output
        Summariser summer = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }

        // Create result collector
        String logFile = "target/jmeter-results-" + System.currentTimeMillis() + ".jtl";
        ResultCollector logger = new ResultCollector(summer);
        logger.setFilename(logFile);
        testPlanTree.add(testPlanTree.getArray()[0], logger);

        // Run the test
        StandardJMeterEngine jmeter = new StandardJMeterEngine();
        jmeter.configure(testPlanTree);
        jmeter.run();

        System.out.println("JMeter test completed. Results saved to: " + logFile);
    }

    /**
     * Run the basic performance test plan
     */
    public static void runBasicTest() throws Exception {
        runTestPlan(TEST_PLAN_PATH);
    }

    /**
     * Run the InfluxDB integrated test plan
     */
    public static void runInfluxDBTest() throws Exception {
        runTestPlan(INFLUX_TEST_PLAN_PATH);
    }
}
