package org.example;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.drools.decisiontable.InputType;
import org.drools.decisiontable.SpreadsheetCompiler;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class DroolsTest {

    private static final String DATE_FORMAT = "yyyy/MM/dd";

    @Test
    public void testHello() throws ParseException, IOException {

        // デフォルトの dateformat は "dd-MMM-yyyy" (例: "01-Apr-2019") なので変更する
        System.setProperty("drools.dateformat", DATE_FORMAT);

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
        
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Date memberRegisterDate = sdf.parse("2019/04/11");

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

        ksession.dispose();
    }

}
