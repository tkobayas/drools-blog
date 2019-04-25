package org.example;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class DroolsTest {

    @Test
    public void testHello() {

        KieServices ks = KieServices.Factory.get();
        ReleaseId releaseId = ks.newReleaseId("org.example", "drools-hello-kjar", "1.0.0");
        KieContainer kcontainer = ks.newKieContainer(releaseId);
        KieSession ksession = kcontainer.newKieSession();

        Person john = new Person("ジョン", 25);
        ksession.insert(john);
        Person paul = new Person("ポール", 10);
        ksession.insert(paul);

        int fired = ksession.fireAllRules();

        assertEquals(2, fired);

        ksession.dispose();
    }

}
