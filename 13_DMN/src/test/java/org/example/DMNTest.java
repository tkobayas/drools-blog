package org.example;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.core.api.DMNFactory;
import org.kie.dmn.core.compiler.RuntimeTypeCheckOption;
import org.kie.dmn.core.impl.DMNRuntimeImpl;
import org.kie.dmn.core.util.KieHelper;

public class DMNTest {

    @Test
    public void testTraficViolation() {

        final KieServices ks = KieServices.Factory.get();
        final KieContainer kieContainer = KieHelper.getKieContainer(
                                                                    ks.newReleaseId("com.sample", "dmn-example-" + UUID.randomUUID(), "1.0"),
                                                                    ks.getResources().newClassPathResource("Traffic_Violation.dmn", DMNTest.class));

        DMNRuntime dmnRuntime = kieContainer.newKieSession().getKieRuntime(DMNRuntime.class);
        ((DMNRuntimeImpl) dmnRuntime).setOption(new RuntimeTypeCheckOption(true));

        final DMNModel dmnModel = dmnRuntime.getModel("https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF", "Traffic Violation");

        final DMNContext context = DMNFactory.newContext();

        Map<String, Object> driverMap = new HashMap<>();
        driverMap.put("名前", "太郎");
        driverMap.put("年齢", 34);
        driverMap.put("ポイント", 18);
        context.set("ドライバー", driverMap);

        Map<String, Object> violationMap = new HashMap<>();
        violationMap.put("日付", LocalDate.now());
        violationMap.put("タイプ", "速度超過");
        violationMap.put("制限速度", 100);
        violationMap.put("実際の速度", 120);
        context.set("違反", violationMap);

        final DMNResult dmnResult = dmnRuntime.evaluateAll(dmnModel, context);

        System.out.println(dmnResult);
    }

}
