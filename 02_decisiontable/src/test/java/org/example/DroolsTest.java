package org.example;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;

import org.drools.decisiontable.InputType;
import org.drools.decisiontable.SpreadsheetCompiler;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class DroolsTest {

    @Test
    public void testHello() throws ParseException, IOException {

        // デフォルトの dateformat は "dd-MMM-yyyy" (例: "01-Apr-2019") なので変更する
        System.setProperty("drools.dateformat", "yyyy-MM-dd");

        KieServices ks = KieServices.Factory.get();

        System.out.println("=== デシジョンテーブルから生成される DRL ===");
        SpreadsheetCompiler compiler = new SpreadsheetCompiler();
        String drl = compiler.compile(ks.getResources().newClassPathResource("org/example/point-calc.xls").getInputStream(), InputType.XLS);
        System.out.println(drl);
        System.out.println("======================================");

        KieContainer kcontainer = ks.getKieClasspathContainer();
        KieSession ksession = kcontainer.newKieSession();

        System.out.println();
        System.out.println("+++ ルール実行開始 +++");

        LocalDate memberRegisterDate = LocalDate.parse("2019-04-11");

        Person john = new Person("ジョン", memberRegisterDate, MembershipCard.SILVER);
        System.out.println("insert : " + john);
        ksession.insert(john);

        Order order = new Order(john, "ギター", 200000);
        System.out.println("insert : " + order);
        ksession.insert(order);

        int fired = ksession.fireAllRules();
        assertEquals(2, fired);

        System.out.println("======================================");
        System.out.println("お買い上げにより、 " + order.getTotalPoint() + " ポイントが付与されます");
        System.out.println("======================================");
        
        assertEquals(6000, order.getTotalPoint());

        ksession.dispose();
    }

}
