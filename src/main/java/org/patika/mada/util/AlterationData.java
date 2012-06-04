package org.patika.mada.util;

public class AlterationData extends ExperimentData {
    private static SignificanceFilter sigFilt;

    public AlterationData(double value) {
        super(value);
    }

    public AlterationData(double v1, double v2) {
        super(v1, v2);
    }

    @Override
    public double getMaxValue() {
        return VALUES.ALTERED.toDouble();
    }

    @Override
    public double getMinValue() {
        return VALUES.NOT_ALTERED.toDouble();
    }

    @Override
    protected SignificanceFilter getSignificanceFilter() {
        return sigFilt;
    }

    public static void setSignificanceFilter(SignificanceFilter sigFilt) {
   		AlterationData.sigFilt = sigFilt;
   	}

    @Override
    public Object getKey() {
        return ExperimentData.ALTERATION_DATA;
    }

    public enum VALUES {
        ALTERED(1.0D),
        NOT_ALTERED(.0D),
        NO_DATA(-1.0D);
        private Double numericalValue;

        private VALUES(Double numericalValue) {
            this.numericalValue = numericalValue;
        }

        public Double toDouble() {
            return numericalValue;
        }
    }

}
