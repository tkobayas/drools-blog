package org.example;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class DroolsTest {

    @Test
    public void testHello() {

        long start = System.currentTimeMillis();
        
        KieServices ks = KieServices.Factory.get();
        ReleaseId releaseId = ks.newReleaseId("org.example", "09_executable-model-kjar", "1.0.0");
        KieContainer kcontainer = ks.newKieContainer(releaseId);
        KieSession ksession = kcontainer.newKieSession();
        
        System.out.println("elapsed time for load  = " + (System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();
        
        List<String> resultList = new ArrayList<>();
        ksession.setGlobal("resultList", resultList);

        for (int i = 0; i < 1000; i++) {
            Person john = new Person("ジョン" + i, i);
            ksession.insert(john);
            ksession.fireAllRules();
        }
        
        System.out.println("elapsed time for execution = " + (System.currentTimeMillis() - start) + "ms");
        
        System.out.println("resultList size = " + ((List<String>)ksession.getGlobal("resultList")).size());
        ksession.dispose();
    }

}
