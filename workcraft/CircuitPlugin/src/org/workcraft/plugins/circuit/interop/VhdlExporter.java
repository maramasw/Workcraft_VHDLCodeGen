package org.workcraft.plugins.circuit.interop;

import org.workcraft.interop.AbstractSerialiseExporter;
import org.workcraft.plugins.circuit.serialisation.VhdlSerialiser;

public class VhdlExporter extends AbstractSerialiseExporter {

    private final VhdlSerialiser serialiser = new VhdlSerialiser();

    @Override
    public VhdlFormat getFormat() {
        return VhdlFormat.getInstance();
    }

    @Override
    public VhdlSerialiser getSerialiser() {
        return serialiser;
    }

}
