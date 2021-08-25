package org.workcraft.plugins.circuit.serialisation;

import org.workcraft.Info;
import org.workcraft.dom.Model;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.interop.VhdlFormat;
import org.workcraft.plugins.circuit.utils.RefinementUtils;
import org.workcraft.plugins.circuit.verilog.SubstitutionRule;
import org.workcraft.plugins.circuit.verilog.SubstitutionUtils;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.plugins.circuit.serialisation.SplitElementMap;
import org.workcraft.plugins.circuit.vhdl.VhdlModule;
import org.workcraft.types.Pair;
import org.workcraft.utils.*;

import java.io.*;

import java.lang.reflect.Field;
//import java.security.Key;
import java.sql.Array;
import java.util.*;
import java.util.regex.*;

public class VhdlSerialiser implements ModelSerialiser {

    private static final String KEYWORD_INPUT = "IN";
    private static final String KEYWORD_OUTPUT = "OUT";
    private static final String KEYWORD_SIGNAL = "SIGNAL";
    private static final String KEYWORD_ENTITY = "ENTITY";
    private static final String KEYWORD_BEGIN = "BEGIN";
    private static final String KEYWORD_STARTMODULE = "ARCHITECTURE DATAFLOW OF ";
    private static final String KEYWORD_ENDMODULE = "END ARCHITECTURE;";
    private static final String KEYWORD_ASSIGN = "STD_LOGIC := '0'";
    // private static final String KEYWORD_ASSIGN_DELAY = "#";
    private static final String KEYWORD_IMPORT = "\nLIBRARY IEEE;\nUSE IEEE.STD_LOGIC_1164.ALL;\n\n";

    private final Queue<Pair<File, Circuit>> refinementCircuits = new LinkedList<>();
    private final Map<String, SubstitutionRule> substitutionRules = new HashMap<>();
    private final Map<String, String> outputPortSignalMap = new HashMap<>();

    VhdlModule vhdlmodule = new VhdlModule();
    private ArrayList<String>allGateElements = new ArrayList<>();
    private final SplitElementMap gateLevelExpConversion = new SplitElementMap();

    @Override
    public ReferenceProducer serialise(Model model, OutputStream out, ReferenceProducer refs) {
        if (model instanceof Circuit) {
            Circuit circuit = (Circuit) model;
            Set<FunctionComponent> badComponents = RefinementUtils.getIncompatibleRefinementCircuitComponents(circuit);
            if (badComponents.isEmpty()) {
                PrintWriter writer = new PrintWriter(out);
                writer.println(Info.getGeneratedByText("-- VHDL Code ", ""));
                refinementCircuits.clear();
                substitutionRules.clear();
                substitutionRules.putAll(SubstitutionUtils.readExportSubstitutionRules());
                writeCircuit(writer, circuit, out);
                writer.close();
            } else {
                String msg = TextUtils.wrapMessageWithItems("Incompatible refinement interface for component",
                        ReferenceHelper.getReferenceSet(circuit, badComponents));

                DialogUtils.showError(msg);
            }
        } else {
            throw new ArgumentException("Model class not supported: " + model.getClass().getName());
        }
        return refs;
    }

    @Override
    public boolean isApplicableTo(Model model) {
        return model instanceof Circuit;
    }

    @Override
    public UUID getFormatUUID() {
        return VhdlFormat.getInstance().getUuid();
    }

    private void writeCircuit(PrintWriter writer, Circuit circuit, OutputStream out) {
        allGateElements = new ArrayList<>(Arrays.asList(vhdlmodule.getGateElements()));
        allGateElements.addAll(vhdlmodule.getSpecialGateElements());

        CircuitSignalInfo circuitInfo = new CircuitSignalInfo(circuit);
        writeImportsEntity(writer, circuitInfo, out);
        writeContents(writer, circuitInfo);
        writeOutputPortSignalMap(writer);
        writer.println(KEYWORD_ENDMODULE);
    }

    private void writeImportsEntity(PrintWriter writer, CircuitSignalInfo circuitInfo, OutputStream out) {
        String title = ExportUtils.asIdentifier(circuitInfo.getCircuit().getTitle());
        // If title is Untitled then fetch the filename and set the entity name from it.
        if (title.equals("Untitled")) {
            try {
                // Set the entity name.
                Field pathField = FileOutputStream.class.getDeclaredField("path");
                pathField.setAccessible(true);

                String path = (String) pathField.get(out);
                String filename = path.substring(path.lastIndexOf("\\")+1);
                filename = filename.substring(0, filename.lastIndexOf("."));
                title = filename;
            } catch (Exception e) {
                LogUtils.logMessage("Error: " + e);
            }
        }
        writer.print(KEYWORD_IMPORT);

        writer.print(KEYWORD_ENTITY + " " + title + " IS\n");
        ArrayList<String> inputPorts = new ArrayList<>();
        ArrayList<String> outputPorts = new ArrayList<>();

        for (Contact contact : circuitInfo.getCircuit().getPorts()) {
            String signal = circuitInfo.getContactSignal(contact);
            if (contact.isInput()) {
                inputPorts.add(signal);
            } else {
                outputPorts.add(signal);
            }
        }

        if (!inputPorts.isEmpty()) {
            writer.println("    PORT(\n         " + String.join(", ", inputPorts) +
                    ": " + KEYWORD_INPUT + " " + KEYWORD_ASSIGN + ";");
        }

        if (!outputPorts.isEmpty()) {
            writer.println("         " + String.join(", ", outputPorts) +
                    ": " + KEYWORD_OUTPUT + " " + KEYWORD_ASSIGN + "\n    );");
        }
        writer.print("END " + KEYWORD_ENTITY + ";\n");

        Set<String> wires = new LinkedHashSet<>();
        outputPortSignalMap.clear();
        for (int i=0; i<outputPorts.size(); i++) {
            String signal_name = "SIGNAL_"+i;
            outputPortSignalMap.put(outputPorts.get(i), signal_name);
            wires.add(signal_name);
        }

        for (FunctionComponent component : circuitInfo.getCircuit().getFunctionComponents()) {
            for (FunctionContact contact : component.getFunctionOutputs()) {
                String signal = circuitInfo.getContactSignal(contact);
                if (inputPorts.contains(signal) || outputPorts.contains(signal)) continue;
                wires.add(signal);
            }
        }
        writer.print("\n" + KEYWORD_STARTMODULE + title + " IS\n");
        if (!wires.isEmpty()) {
            writer.println("    " + KEYWORD_SIGNAL + " " + String.join(", ", wires) +
                    ": " + KEYWORD_ASSIGN + ";\n\n" + KEYWORD_BEGIN);
        }
        writer.println();
    }

    private void writeContents(PrintWriter writer, CircuitSignalInfo circuitInfo) {
        // Write writer mapped components
        boolean hasMappedComponents = false;
        for (FunctionComponent component : circuitInfo.getCircuit().getFunctionComponents()) {
            if (component.isMapped() || (component.getRefinementFile() != null)) {
                writeInstance(writer, circuitInfo, component);
                hasMappedComponents = true;
            }
        }
        if (hasMappedComponents) {
            writer.println();
        }
    }

    private void writeInstance(PrintWriter writer, CircuitSignalInfo circuitInfo, FunctionComponent component) {
        // Module name
        String title = component.getModule();
        Pair<File, Circuit> refinementCircuit = RefinementUtils.getRefinementCircuit(component);
        if (refinementCircuit != null) {
            refinementCircuits.add(refinementCircuit);
            title = refinementCircuit.getSecond().getTitle();
        }
        String moduleName = ExportUtils.asIdentifier(title);

        Pattern pat = Pattern.compile("^[^a-zA-Z0-9].*");

        // Instance name
        String instanceFlatName = circuitInfo.getComponentFlattenReference(component);
        SubstitutionRule substitutionRule = substitutionRules.get(moduleName);
        String msg = "Processing instance '" + instanceFlatName + "'";
        String circuitTitle = circuitInfo.getCircuit().getTitle();
        if (!circuitTitle.isEmpty()) {
            msg += " in circuit '" + circuitTitle + "'";
        }
        msg += ": ";

        moduleName = SubstitutionUtils.getModuleSubstitutionName(moduleName, substitutionRule, msg);
        // LogUtils.logMessage("Module Name: " + moduleName);

        if (!allGateElements.contains(moduleName)) {
            throw new NotSupportedException("Custom component(s) are not supported: '" + moduleName + "' !!");
        }

        boolean first = true;
        String dataString = "";

        for (Contact contact: component.getContacts()) {
            String signalName = circuitInfo.getContactSignal(contact);
            // LogUtils.logMessage("Signal Name: " + signalName);
            Matcher match = pat.matcher(signalName);
            if (match.matches()) {
                throw new NotSupportedException("Incompatible signal name: " + signalName + " !!");
            }

            if (first) {
                first = false;
                if (outputPortSignalMap.containsKey(signalName)) {
                    dataString = outputPortSignalMap.get(signalName) + " <=";
                } else {
                    if (moduleName.equals("LOGIC1")) {
                        dataString = signalName + " <= '1' LOGIC1;";
                    } else if (moduleName.equals("LOGIC0")) {
                        dataString = signalName + " <= '0' LOGIC0;";
                    } else {
                        dataString = signalName + " <=";
                    }
                }
            } else {
                if (outputPortSignalMap.containsKey(signalName)) {
                    dataString += " " + outputPortSignalMap.get(signalName) + " " + moduleName;
                } else {
                    dataString += " " + signalName + " " + moduleName;
                }
            }
        }

        dataString = "    " + dataString.substring(0, dataString.lastIndexOf(moduleName)).trim() + ";";
        dataString = gateLevelExpConversion.generateExpOrProcessBlock(moduleName, dataString);
        writer.println(dataString);
    }

    private void writeOutputPortSignalMap(PrintWriter writer) {
        Iterator iter = outputPortSignalMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry KeyPair = (Map.Entry)iter.next();
            String dataString = "    " + KeyPair.getKey() + " <= " + KeyPair.getValue() + ";";
            writer.println(dataString);
        }
        writer.println();
    }
}
