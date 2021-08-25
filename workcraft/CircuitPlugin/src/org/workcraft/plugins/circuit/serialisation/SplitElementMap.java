package org.workcraft.plugins.circuit.serialisation;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.utils.LogUtils;
import org.workcraft.plugins.circuit.vhdl.VhdlModule;
import java.util.*;

public class SplitElementMap {
    private final HashMap<String, ArrayList<String>> gateToExpressionMap = new HashMap<>();
    private String[] gateNames = {};
    private ArrayList<String> specialGateNames = new ArrayList<>();
    private String[] boolean_expression = {};

    public SplitElementMap() {
        VhdlModule vhdlmodule = new VhdlModule();
        this.gateNames = vhdlmodule.getGateElements();
        this.boolean_expression = vhdlmodule.getBooleanExpression();
        this.specialGateNames = vhdlmodule.getSpecialGateElements();

        for (int i=0; i < boolean_expression.length; i++) {
            ArrayList<String> expTokenList  = new ArrayList<>(Arrays.asList(boolean_expression[i].split(" ")));
            gateToExpressionMap.put(gateNames[i], expTokenList);
        }
    }

    public String generateCompleteExpression (String gateName, String convert_exp) {
        if (gateToExpressionMap.containsKey(gateName)) {
            int iter_value = 0;

            String output_signal_name = convert_exp.substring(0, convert_exp.lastIndexOf("<=") + 3);
            String convert_exp_str = convert_exp.substring(convert_exp.lastIndexOf("<=") + 3, convert_exp.length());
            convert_exp_str = convert_exp_str.replaceAll(";","");

            ArrayList<String> iterate_arr = gateToExpressionMap.get(gateName);
            String[] convert_exp_arr = convert_exp_str.split(" ");
            convert_exp_str = "";

            int braces_count = 0;

            for (int i = 0; i < convert_exp_arr.length; i++) {
                if (iterate_arr.get(iter_value).equals("NOT(")) {
                    convert_exp_str += iterate_arr.get(iter_value);
                    iter_value ++;
                    braces_count ++;
                } else if (iterate_arr.get(iter_value).equals("NOT((")){
                    convert_exp_str += iterate_arr.get(iter_value);
                    iter_value ++;
                    braces_count += 2;
                } else if (iterate_arr.get(iter_value).equals("(")) {
                    convert_exp_str += iterate_arr.get(iter_value);
                    iter_value ++;
                    braces_count ++;
                } else if (iterate_arr.get(iter_value).equals(")")) {
                    convert_exp_str += iterate_arr.get(iter_value);
                    iter_value ++;
                    braces_count --;
                }

                if (iterate_arr.get(iter_value).equals("SIGNAL")) {
                    convert_exp_str += convert_exp_arr[i];
                } else if (iterate_arr.get(iter_value).equals("NOT-SIGNAL")) {
                    convert_exp_str += "NOT " + convert_exp_arr[i];
                } else {
                    convert_exp_str += " " + iterate_arr.get(iter_value) + " ";
                }
                iter_value++;
            }

            if (braces_count != 0) {
                for (int i=0; i<braces_count; i++) {
                    convert_exp_str = convert_exp_str.substring(0, convert_exp_str.length()) + ")";
                }
            }
            convert_exp_str = output_signal_name + convert_exp_str + ";";
            return convert_exp_str;
        } else {
            throw new ArgumentException("Element could not be found!!");
        }
    }

    public String generateExpOrProcessBlock(String gateName, String convert_exp) {
        if (specialGateNames.contains(gateName)) {
            String dataString = generateProcessCodeBlock(gateName, convert_exp);
            return dataString;
        } else {
            String dataString = generateCompleteExpression(gateName, convert_exp);
            return dataString;
        }
    }

    public String generateProcessCodeBlock(String gateName, String convert_exp) {
//        LogUtils.logMessage("Convert Expression String is: " + convert_exp);
        String signalNames = "";
        if (!gateName.equals("LOGIC0") || !gateName.equals("LOGIC1")) {
            signalNames = convert_exp.trim();
            signalNames = signalNames.replaceAll(";", "");
            signalNames = signalNames.replaceAll(" <=", "");
        }

        if (gateName.equals("C2")) { // Gate C2.
            // "Sample convert_exp value is --> SIGNAL_1 <= U20_ON C2 OUT_BUBBLE1_ON;"
            signalNames = signalNames.replaceAll(" C2", "");

            String[] signalArr = signalNames.split(" ");

            String formatString = "\tIF %s = '1' AND %s = '1' THEN %s <= '1'; END IF;\n" +
                    "\t\t\tIF %s = '0' AND %s = '0' THEN %s <= '0'; END IF;\n\t";

            String dataString = "\tPROCESS (" + String.join(",", signalArr) + ")\n\t\tBEGIN\n\t\t";
            dataString += String.format(formatString, signalArr[1], signalArr[2], signalArr[0], signalArr[1],
                    signalArr[2], signalArr[0]);
            dataString += "END PROCESS;";

            return dataString;
        } else if (gateName.equals("NC2")) { // Gate NC2.
            // "Sample convert_exp value is --> SIGNAL_1 <= U20_ON C2 OUT_BUBBLE1_ON;"
            signalNames = signalNames.replaceAll(" NC2", "");

            String[] signalArr = signalNames.split(" ");

            String formatString = "\tIF %s = '1' AND %s = '1' THEN %s <= '0'; END IF;\n" +
                    "\t\t\tIF %s = '0' AND %s = '0' THEN %s <= '1'; END IF;\n\t";

            String dataString = "\tPROCESS (" + String.join(",", signalArr) + ")\n\t\tBEGIN\n\t\t";
            dataString += String.format(formatString, signalArr[1], signalArr[2], signalArr[0], signalArr[1],
                    signalArr[2], signalArr[0]);
            dataString += "END PROCESS;";

            return dataString;
        } else if (gateName.equals("WAIT")) { // Gate C2.
            // "Sample convert_exp value is --> SIGNAL_1 <= U20_ON WAIT OUT_BUBBLE1_ON;"
            signalNames = signalNames.replaceAll(" WAIT", "");

            String[] signalArr = signalNames.split(" ");

            String formatString = "\tIF %s = '1' AND %s = '1' THEN %s <= '1'; END IF;\n" +
                    "\t\t\tIF %s = '0' THEN %s <= '0'; END IF;\n\t";

            String dataString = "\tPROCESS (" + String.join(",", signalArr) + ")\n\t\tBEGIN\n\t\t";
            dataString += String.format(formatString, signalArr[0], signalArr[1], signalArr[2], signalArr[1], signalArr[2]);
            dataString += "END PROCESS;";

            return dataString;
        } else if (gateName.equals("WAIT0")) { // Gate NC2.
            // "Sample convert_exp value is --> SIGNAL_1 <= U20_ON WAIT0 OUT_BUBBLE1_ON;"
            signalNames = signalNames.replaceAll(" WAIT0", "");

            String[] signalArr = signalNames.split(" ");

            String dataString = "\tPROCESS (" + String.join(",", signalArr) + ")\n\t\tBEGIN\n\t\t";

            String formatString = "\tIF %s = '1' AND %s = '1' THEN %s <= '0'; END IF;\n";
            dataString += String.format(formatString, signalArr[0], signalArr[1], signalArr[2]);

            formatString = "\t\t\tIF %s = '0' AND %s = '1' THEN %s <= '1'; END IF;\n";
            dataString += String.format(formatString, signalArr[0], signalArr[1], signalArr[2]);

            formatString = "\t\t\tIF %s = '1' AND %s = '1' AND %s = '1' THEN %s <= '1'; END IF;\n";
            dataString += String.format(formatString, signalArr[0], signalArr[1], signalArr[2], signalArr[2]);

            formatString = "\t\t\tIF %s = '1' AND %s = '1' AND %s = '0' THEN %s <= '0'; END IF;\n";
            dataString += String.format(formatString, signalArr[0], signalArr[1], signalArr[2], signalArr[2]);

            formatString = "\t\t\tIF %s = '0' THEN %s <= '0'; END IF;\n";
            dataString += String.format(formatString, signalArr[1], signalArr[2]);

            dataString += "\tEND PROCESS;";

            return dataString;
        } else if (gateName.equals("MUTEX")) {
            signalNames = signalNames.replaceAll(" MUTEX", "");
            String[] signalArr = signalNames.split(" ");

            String formatString = "\tIF %s = '1' AND %s = '0' THEN %s <= %s; %s <= '0'; END IF;";
            formatString += "\n\t\t\tIF %s = '0' AND %s = '1' THEN %s <= '0'; %s <= %s; END IF;";
            formatString += "\n\t\t\tIF %s = '1' AND %s = '1' THEN %s <= %s; %s <= '0'; END IF;";
            formatString += "\n\t\t\tIF %s = '0' AND %s = '0' THEN %s <= '0'; %s <= '0'; END IF;\n\t";

            String dataString = "\tPROCESS (" + String.join(",", signalArr) + ")\n\t\tBEGIN\n\t\t";
            dataString += String.format(formatString, signalArr[0], signalArr[2], signalArr[1], signalArr[0], signalArr[3],
                    signalArr[0], signalArr[2], signalArr[1], signalArr[3], signalArr[2],
                    signalArr[0], signalArr[2], signalArr[1], signalArr[0], signalArr[3],
                    signalArr[0], signalArr[2], signalArr[1], signalArr[3]);
            dataString += "END PROCESS;";

            return dataString;
        }

        return convert_exp;
    }
}
