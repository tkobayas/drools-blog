package org.example;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.time.LocalDate;

import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class DroolsTest {

    @Test
    public void testHello() throws ParseException {

        // デフォルトの dateformat は "dd-MMM-yyyy" (例: "01-Apr-2019") なので変更する
        System.setProperty("drools.dateformat", "yyyy-MM-dd");

        KieServices ks = KieServices.Factory.get();
        KieContainer kcontainer = ks.getKieClasspathContainer();
        KieSession ksession = kcontainer.newKieSession();

        LocalDate memberRegisterDate = LocalDate.parse("2019-04-11");

        Person john = new Person("ジョン", memberRegisterDate);
        System.out.println("insert : " + john);
        ksession.insert(john);

        Order order = new Order(john, "ギター", 200000);
        System.out.println("insert : " + order);
        ksession.insert(order);

        int fired = ksession.fireAllRules();
        assertEquals(3, fired);

        if (order.isSpecialPointOrder()) {
            // 特別な処理
            System.out.println("======================================");
            System.out.println("ポイントキャンペーンのご活用ありがとうございます!");
            System.out.println("======================================");
        }

        ksession.dispose();
    }

}
