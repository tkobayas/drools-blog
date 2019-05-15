package org.example;

import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.drools.core.common.InternalFactHandle;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.Command;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;

public class DroolsTest {

    @Test
    public void testHello() throws ParseException {

        // デフォルトの dateformat は "dd-MMM-yyyy" (例: "01-Apr-2019") なので変更する
        System.setProperty("drools.dateformat", "yyyy-MM-dd");

        KieServices ks = KieServices.Factory.get();
        KieContainer kcontainer = ks.getKieClasspathContainer();
        StatelessKieSession ksession = kcontainer.newStatelessKieSession();

        LocalDate memberRegisterDate = LocalDate.parse("2019-04-11");

        Person john = new Person("ジョン", memberRegisterDate);
        System.out.println("insert : " + john);

        Order order = new Order(john, "ギター", 200000);
        System.out.println("insert : " + order);

        Command insertElementsCommand = CommandFactory.newInsertElements(Arrays.asList(john, order));

        // fireAllRule、dispose は最後に自動的に実行される
        List<InternalFactHandle> factHandleList = (List<InternalFactHandle>) ksession.execute(insertElementsCommand);

        assertTrue(order.isSpecialPointOrder());

        if (order.isSpecialPointOrder()) {
            // 特別な処理
            System.out.println("======================================");
            System.out.println("ポイントキャンペーンのご活用ありがとうございます!");
            System.out.println("======================================");
        }
    }

}
