package org.example;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.JMeterEngineException;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.save.SaveService;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;


public class AppTest {
    private final static String jmeterPath = "D:\\jmeter\\apache-jmeter-5.6.3";
    private final static String host = "192.168.50.241";
    private final static Integer port = 8080;

    private final static String logFile = "results.jtl";
    private StandardJMeterEngine jmeter;

    private Summariser summariser;

    enum Method {
        POST("POST"),
        GET("GET");
        public final String label;

        Method(String label) {
            this.label = label;
        }

    }

    @Before
    public void setUp() {
        File properties = new File(
                Objects.requireNonNull(getClass().getClassLoader().getResource("jmeter.properties")).getFile()
        );

        JMeterUtils.loadJMeterProperties(properties.getPath());
        JMeterUtils.setJMeterHome(jmeterPath);
        JMeterUtils.initLocale();

        jmeter = new StandardJMeterEngine();

    }

    private String generateRandomString(int strLength) {
        Random random = new Random();

        return random.ints(48, 123)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(strLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private HTTPSampler getHttpsSampler(Method method) {
        HTTPSampler sampler = new HTTPSampler();
        sampler.setDomain(host);
        sampler.setPort(port);
        sampler.setMethod(method.label);

        return sampler;
    }

    private LoopController getLoopController(List<HTTPSampler> samplers) {
        LoopController loopCtrl = new LoopController();
        loopCtrl.setLoops(1);
        loopCtrl.setFirst(true);

        for (HTTPSampler sampler : samplers) {
            loopCtrl.addTestElement(sampler);
        }

        return loopCtrl;

    }

    private SetupThreadGroup getThreadGroup(LoopController loopCtrl, Integer threadsCount, Integer rampUp) {
        SetupThreadGroup threadGroup = new SetupThreadGroup();
        threadGroup.setNumThreads(threadsCount);
        threadGroup.setRampUp(rampUp);
        threadGroup.setSamplerController(loopCtrl);

        return threadGroup;
    }

    private TestPlan getTestPlan(String testPlanName) {
        TestPlan testPlan = new TestPlan(testPlanName);
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());

        return testPlan;
    }

    @Test
    public void jmeterExample() {

        TestPlan testPlan = getTestPlan("Example1");

        HTTPSampler randStringSampler = getHttpsSampler(Method.GET);
        randStringSampler.setPath("/randStr");

        HTTPSampler postDataSampler = getHttpsSampler(Method.POST);
        postDataSampler.setPath("/postData");
        postDataSampler.addArgument("Content-Type", "application/json");
        postDataSampler.setPostBodyRaw(true);

        String jsonBody = String.format("{\"filed1\": \"%s\", \"filed2\": \"%s\"}", generateRandomString(20), generateRandomString(20));
        postDataSampler.addNonEncodedArgument("", jsonBody, "");


        ArrayList<HTTPSampler> samplers = new ArrayList<>();
        samplers.add(randStringSampler);
        samplers.add(postDataSampler);


        LoopController loopCtrl = getLoopController(samplers);

//        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
//        if (!summariserName.isEmpty()) {
//            summariser = new Summariser(summariserName);
//        }
//
//        ResultCollector logger = new ResultCollector(summariser);
//        logger.setFilename(logFile);

        HashTree hashTree = new HashTree();

        hashTree.add(testPlan);
        hashTree.add(testPlan, loopCtrl);
        hashTree.add(testPlan, getThreadGroup(loopCtrl, 5, 1));
        hashTree.add(testPlan, randStringSampler);
        hashTree.add(testPlan, postDataSampler);

//        hashTree.add(testPlan, logger);


        jmeter.configure(hashTree);

        try {
            SaveService.saveTree(hashTree, new FileOutputStream("test.jmx"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            jmeter.runTest();
            while (jmeter.isActive()) {
                Thread.sleep(5000);
            }
        } catch (JMeterEngineException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// TODO nothing works, java is crap