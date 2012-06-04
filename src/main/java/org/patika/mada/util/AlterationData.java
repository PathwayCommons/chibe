package org.patika.mada.util;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.gvt.util.Conf;

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

    @Override
    public Color getNodeColor() {
        // Reverse log-scale the color to make alterations more obvious, e.g. 10% more red
        double C = 100.0;
        double value = (getValue() * C) + 1.0D; // To prevent NaNs
        Color alt = Conf.getColor(Conf.EXPERIMENT_UP_COLOR);
        Color nonalt = Conf.getColor(Conf.EXPERIMENT_MIDDLE_COLOR);

        float redDiff = alt.getRed() - nonalt.getRed();
        float greenDiff = alt.getGreen() - nonalt.getGreen();
        float blueDiff = alt.getBlue() - nonalt.getBlue();

        redDiff = (float) (redDiff * (Math.log(value) / Math.log(C)));
        greenDiff = (float) (greenDiff * (Math.log(value) / Math.log(C)));
        blueDiff = (float) (blueDiff * (Math.log(value) / Math.log(C)));
        RGB rgb = new RGB(
                nonalt.getRed() + (int) redDiff,
                nonalt.getGreen() + (int) greenDiff,
                nonalt.getBlue() + (int) blueDiff);

        System.out.println("Value: " + value + "\t\t" + rgb + "\t\t" + (Math.log(value) / Math.log(C)));
        return new Color(alt.getDevice(), rgb);
    }

    public enum VALUES {
        ALTERED(1.0D),
        NOT_ALTERED(.0D);
        private Double numericalValue;

        private VALUES(Double numericalValue) {
            this.numericalValue = numericalValue;
        }

        public Double toDouble() {
            return numericalValue;
        }
    }

}
