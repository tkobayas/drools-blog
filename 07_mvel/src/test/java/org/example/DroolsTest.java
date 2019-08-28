package org.example;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

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

        Person john = new Person("ジョン", 27, new BigDecimal("300000"));
        ksession.insert(john);

        int fired = ksession.fireAllRules();

        assertEquals(1, fired);
        
        System.out.println( john.getName() + " の給料は " + john.getSalary() + "円です。" );

        ksession.dispose();
    }

}
