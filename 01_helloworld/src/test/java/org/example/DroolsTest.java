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
        KieContainer kContainer = ks.getKieClasspathContainer();
        KieSession kSession = kContainer.newKieSession();

        Person john = new Person("John", 25);
        kSession.insert(john);
        Person paul = new Person("Paul", 10);
        kSession.insert(paul);

        int fired = kSession.fireAllRules();

        assertEquals(2, fired);

        kSession.dispose();
    }

}
