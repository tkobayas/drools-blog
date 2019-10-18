package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Generate simple rules which have AlphaNode + constraints
 */
public class SimpleRuleGen {

    public static void main(String[] args) throws Exception {
        
        StringBuilder builder = new StringBuilder();
        
        builder.append("package org.example\n");
        builder.append("import org.example.*\n\n");
        builder.append("global java.util.List resultList;\n\n");
        
        int ruleNum = 1000;
        
        for (int i = 0; i < ruleNum; i++) {
            builder.append("rule \"rule" + i + "\"\n");
            builder.append("  when\n");
            builder.append("    $p : Person( age >= " + i*5 + " && age < " + (i+1)*5 + " )\n");
            builder.append("  then\n");
            builder.append("    resultList.add( kcontext.getRule().getName() + \" : \" + $p );\n");
            builder.append("end\n");
            builder.append("\n");
        }
        
        PrintWriter pr = new PrintWriter(new FileWriter(new File("src/main/resources/org/example/Sample.drl")));
        pr.write(builder.toString());
        pr.close();
        
        System.out.println("finish");
    }
}
