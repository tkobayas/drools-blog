package org.example;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class DroolsTest {

    @Test
    public void testHello() {

        KieServices ks = KieServices.Factory.get();
        KieContainer kcontainer = ks.getKieClasspathContainer();
        KieSession ksession = kcontainer.newKieSession();

        Person john = new Person("John", 25);
        ksession.insert(john);
        Person paul = new Person("Paul", 10);
        ksession.insert(paul);

        int fired = ksession.fireAllRules();

        assertEquals(2, fired);

        ksession.dispose();
    }

}
