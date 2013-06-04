package org.patika.mada.util;

public class CBioPortalAlterationData extends AlterationData {
    public CBioPortalAlterationData(double value) {
        super(value);
    }

    public CBioPortalAlterationData(double v1, double v2) {
        super(v1, v2);
    }

    @Override
    public Object getKey() {
        return ExperimentData.CBIOPORTAL_ALTERATION_DATA;
    }
}
