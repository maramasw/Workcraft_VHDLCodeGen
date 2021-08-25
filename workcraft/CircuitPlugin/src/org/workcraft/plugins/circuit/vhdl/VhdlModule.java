package org.workcraft.plugins.circuit.vhdl;

import java.util.ArrayList;

public class VhdlModule {
    public VhdlModule() {
    }

    public String[] getGateElements() {
        // please maintain the sequence. It should be same to that of getBooleanExpression.
        String[] gateNames = {
            "AND2", "AND3", "AND4", "AO21", "AO22", "AOI21",
            "AOI211", "AOI22", "AOI221", "AOI222", "AOI2BB1", "AOI2BB2",
            "AOI31", "AOI32", "AOI33", "BUF", "INV", "NAND2", "NAND2B",
            "NAND3", "NAND3B", "NAND4", "NAND4B", "NAND4BB", "NOR2", "NOR2B",
            "NOR3", "NOR3B", "NOR4", "NOR4B", "NOR4BB", "OA21", "OA22", "OAI21",
            "OAI211", "OAI22", "OAI221", "OAI222", "OAI2BB1", "OAI2BB2",
            "OAI31", "OAI32", "OAI33", "OR2", "OR3", "OR4"
        };

        return gateNames;
    }

    public ArrayList<String> getSpecialGateElements(){
        ArrayList<String> specialGateNames = new ArrayList<String>();
        specialGateNames.add("C2");
        specialGateNames.add("NC2");
        specialGateNames.add("LOGIC0");
        specialGateNames.add("LOGIC1");
        specialGateNames.add("WAIT0");
        specialGateNames.add("WAIT");
        specialGateNames.add("MUTEX");

        return specialGateNames;
    }

    public String[] getBooleanExpression() {
        // please maintain the sequence. It should be same to that of getGateElements.
        String[] boolean_expression = {
            "SIGNAL AND SIGNAL",
            "SIGNAL AND SIGNAL AND SIGNAL",
            "SIGNAL AND SIGNAL AND SIGNAL AND SIGNAL",
            "( SIGNAL AND SIGNAL ) OR SIGNAL",
            "( SIGNAL AND SIGNAL ) OR ( SIGNAL AND SIGNAL )",
            "NOT(( SIGNAL AND SIGNAL ) OR SIGNAL )",
            "NOT(( SIGNAL AND SIGNAL ) OR SIGNAL OR SIGNAL )",
            "NOT(( SIGNAL AND SIGNAL ) OR ( SIGNAL AND SIGNAL ) )",
            "NOT(( SIGNAL AND SIGNAL ) OR ( SIGNAL AND SIGNAL ) OR SIGNAL )",
            "NOT(( SIGNAL AND SIGNAL ) OR ( SIGNAL AND SIGNAL ) OR ( SIGNAL AND SIGNAL ) )",
            "NOT(( NOT-SIGNAL AND NOT-SIGNAL ) OR SIGNAL )",
            "NOT(( NOT-SIGNAL AND NOT-SIGNAL ) OR ( SIGNAL AND SIGNAL ) )",
            "NOT(( SIGNAL AND SIGNAL AND SIGNAL ) OR SIGNAL )",
            "NOT(( SIGNAL AND SIGNAL AND SIGNAL ) OR ( SIGNAL AND SIGNAL ) )",
            "NOT(( SIGNAL AND SIGNAL AND SIGNAL ) OR ( SIGNAL AND SIGNAL AND SIGNAL ) )",
            "SIGNAL", // BUF
            "NOT-SIGNAL", // INV
            "NOT( SIGNAL AND SIGNAL )",
            "NOT(( NOT-SIGNAL ) AND SIGNAL )",
            "NOT(( SIGNAL AND SIGNAL ) AND SIGNAL )",
            "NOT(( NOT-SIGNAL ) AND ( SIGNAL AND SIGNAL ) )",
            "NOT(( SIGNAL AND SIGNAL ) AND ( SIGNAL AND SIGNAL ) )",
            "NOT(( NOT-SIGNAL ) AND ( SIGNAL AND SIGNAL AND SIGNAL ) )",
            "NOT(( NOT-SIGNAL ) AND ( NOT-SIGNAL ) AND SIGNAL AND SIGNAL )",
            "NOT( SIGNAL OR SIGNAL ) )",
            "NOT(( NOT-SIGNAL ) OR SIGNAL )",
            "NOT(( SIGNAL OR SIGNAL ) OR SIGNAL )",
            "NOT(( NOT-SIGNAL ) OR SIGNAL OR SIGNAL )",
            "NOT(( SIGNAL OR SIGNAL ) OR ( SIGNAL OR SIGNAL ) )",
            "NOT(( NOT-SIGNAL ) OR SIGNAL OR SIGNAL OR SIGNAL )",
            "NOT(( NOT-SIGNAL ) OR ( NOT-SIGNAL ) OR SIGNAL OR SIGNAL )",
            "( SIGNAL OR SIGNAL ) AND SIGNAL",
            "( SIGNAL OR SIGNAL ) AND ( SIGNAL OR SIGNAL )",
            "NOT(( SIGNAL OR SIGNAL ) AND SIGNAL )",
            "NOT(( SIGNAL OR SIGNAL ) AND SIGNAL AND SIGNAL )",
            "NOT(( SIGNAL OR SIGNAL ) AND ( SIGNAL OR SIGNAL ) )",
            "NOT(( SIGNAL OR SIGNAL ) AND ( SIGNAL OR SIGNAL ) AND SIGNAL )",
            "NOT(( SIGNAL OR SIGNAL ) AND ( SIGNAL OR SIGNAL ) AND ( SIGNAL OR SIGNAL ) )",
            "NOT(( NOT-SIGNAL OR NOT-SIGNAL ) AND SIGNAL )",
            "NOT(( NOT-SIGNAL OR NOT-SIGNAL ) AND ( SIGNAL OR SIGNAL ) )",
            "NOT(( SIGNAL OR SIGNAL OR SIGNAL ) AND SIGNAL )",
            "NOT(( SIGNAL OR SIGNAL OR SIGNAL ) AND ( SIGNAL OR SIGNAL ) )",
            "NOT(( SIGNAL OR SIGNAL OR SIGNAL ) AND ( SIGNAL OR SIGNAL OR SIGNAL ) )",
            "SIGNAL OR SIGNAL",
            "SIGNAL OR SIGNAL OR SIGNAL",
            "SIGNAL OR SIGNAL OR SIGNAL OR SIGNAL",
        };

        return boolean_expression;
    }
}
