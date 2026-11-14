package com.unitconverter.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.unitconverter.app.data.provider.UnitProvider;
import com.unitconverter.app.data.repository.ConversionRepository;
import com.unitconverter.app.domain.model.Unit;
import com.unitconverter.app.domain.model.UnitCategory;

import org.junit.Test;

/** Spot-checks the Indian traditional units against their published definitions. */
public class IndianUnitsTest {

    private final UnitProvider units = UnitProvider.getInstance();
    private final ConversionRepository repo = ConversionRepository.getInstance();

    private double convert(UnitCategory c, double v, String from, String to) {
        Unit f = units.findUnit(c, from);
        Unit t = units.findUnit(c, to);
        assertNotNull(from, f);
        assertNotNull(to, t);
        return repo.convert(v, f, t).getOutputValue();
    }

    @Test
    public void tola_is_11_66_grams() {
        assertEquals(11.6638, convert(UnitCategory.WEIGHT, 1, "Tola", "Gram"), 0.0001);
    }

    @Test
    public void seer_is_80_tola_and_maund_is_40_seer() {
        assertEquals(80, convert(UnitCategory.WEIGHT, 1, "Seer", "Tola"), 1e-9);
        assertEquals(40, convert(UnitCategory.WEIGHT, 1, "Maund", "Seer"), 1e-9);
    }

    @Test
    public void land_units_match_square_feet() {
        assertEquals(27225, convert(UnitCategory.AREA, 1, "Bigha (North India)", "Square Foot"), 0.01);
        assertEquals(14400, convert(UnitCategory.AREA, 1, "Bigha (Bengal)", "Square Foot"), 0.01);
        assertEquals(1089, convert(UnitCategory.AREA, 1, "Guntha", "Square Foot"), 0.01);
        assertEquals(20, convert(UnitCategory.AREA, 1, "Kanal", "Marla"), 1e-6);
        assertEquals(100, convert(UnitCategory.AREA, 1, "Acre", "Cent"), 1e-6);
    }

    @Test
    public void ghadi_is_24_minutes() {
        assertEquals(24, convert(UnitCategory.TIME, 1, "Ghadi", "Minute"), 1e-9);
        assertEquals(60, convert(UnitCategory.TIME, 1, "Day", "Ghadi"), 1e-9);
    }

    @Test
    public void indian_units_are_tagged() {
        assertEquals(Unit.MeasureSystem.INDIAN, units.findUnit(UnitCategory.LENGTH, "Gaj").getSystem());
        assertEquals(Unit.MeasureSystem.METRIC, units.findUnit(UnitCategory.LENGTH, "Meter").getSystem());
        assertEquals(Unit.MeasureSystem.IMPERIAL, units.findUnit(UnitCategory.LENGTH, "Foot").getSystem());
    }
}
