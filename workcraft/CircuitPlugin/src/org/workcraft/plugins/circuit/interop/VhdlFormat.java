package org.workcraft.plugins.circuit.interop;

import org.workcraft.interop.Format;

import java.util.UUID;

public final class VhdlFormat implements Format {

    private static VhdlFormat instance = null;

    private VhdlFormat() {
    }

    public static VhdlFormat getInstance() {
        if (instance == null) {
            instance = new VhdlFormat();
        }
        return instance;
    }

    @Override
    public UUID getUuid() {
        return UUID.fromString("fdd4414e-fd02-4702-b143-09b24430fdd1");
    }

    @Override
    public String getName() {
        return "VHDL";
    }

    @Override
    public String getExtension() {
        return ".vhdl";
    }

    @Override
    public String getDescription() {
        return "VHDL Code";
    }

}
