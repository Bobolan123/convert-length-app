package com.example.convertlengthunit;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText inputValueField;
    private TextInputLayout inputValueLayout;
    private ChipGroup fromUnitChipGroup, toUnitChipGroup;
    private TextView conversionResultDisplay;
    private MaterialButton performConversionButton;
    private FloatingActionButton swapUnitsButton;
    private MaterialToolbar toolbar;

    private static final LengthUnit[] MEASUREMENT_UNITS = {
        new LengthUnit("Metre", "m", 1.0),
        new LengthUnit("Centimetre", "cm", 100.0),
        new LengthUnit("Millimetre", "mm", 1000.0),
        new LengthUnit("Kilometre", "km", 0.001),
        new LengthUnit("Mile", "mi", 0.000621371),
        new LengthUnit("Foot", "ft", 3.28084),
        new LengthUnit("Inch", "in", 39.3701),
        new LengthUnit("Yard", "yd", 1.09361)
    };

    private LengthUnit selectedFromUnit = MEASUREMENT_UNITS[0];
    private LengthUnit selectedToUnit = MEASUREMENT_UNITS[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUserInterface();
        setupToolbar();
        setupUnitChips();
        setupConversionButtonListener();
        setupSwapUnitsButton();
    }

    private void initializeUserInterface() {
        toolbar = findViewById(R.id.toolbar);
        inputValueField = findViewById(R.id.inputValue);
        inputValueLayout = findViewById(R.id.inputValueLayout);
        fromUnitChipGroup = findViewById(R.id.fromUnitChipGroup);
        toUnitChipGroup = findViewById(R.id.toUnitChipGroup);
        conversionResultDisplay = findViewById(R.id.result);
        performConversionButton = findViewById(R.id.convertButton);
        swapUnitsButton = findViewById(R.id.fabSwapUnits);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
    }

    private void setupUnitChips() {
        // Setup FROM unit chips
        for (int i = 0; i < MEASUREMENT_UNITS.length; i++) {
            LengthUnit unit = MEASUREMENT_UNITS[i];
            Chip chip = new Chip(this);
            chip.setText(unit.getName());
            chip.setCheckable(true);
            chip.setChecked(i == 0); // Select first unit by default
            
            final int unitIndex = i;
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedFromUnit = MEASUREMENT_UNITS[unitIndex];
                    performRealTimeConversion();
                }
            });
            
            fromUnitChipGroup.addView(chip);
        }

        // Setup TO unit chips
        for (int i = 0; i < MEASUREMENT_UNITS.length; i++) {
            LengthUnit unit = MEASUREMENT_UNITS[i];
            Chip chip = new Chip(this);
            chip.setText(unit.getName());
            chip.setCheckable(true);
            chip.setChecked(i == 1); // Select second unit by default
            
            final int unitIndex = i;
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedToUnit = MEASUREMENT_UNITS[unitIndex];
                    performRealTimeConversion();
                }
            });
            
            toUnitChipGroup.addView(chip);
        }
    }

    private void setupConversionButtonListener() {
        performConversionButton.setOnClickListener(v -> performLengthConversion());
    }

    private void setupSwapUnitsButton() {
        swapUnitsButton.setOnClickListener(v -> swapSelectedUnits());
    }

    private void swapSelectedUnits() {
        // Find currently selected chips
        int fromIndex = -1, toIndex = -1;
        
        for (int i = 0; i < fromUnitChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) fromUnitChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                fromIndex = i;
                break;
            }
        }
        
        for (int i = 0; i < toUnitChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) toUnitChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                toIndex = i;
                break;
            }
        }

        // Swap selections
        if (fromIndex != -1 && toIndex != -1) {
            ((Chip) fromUnitChipGroup.getChildAt(fromIndex)).setChecked(false);
            ((Chip) toUnitChipGroup.getChildAt(toIndex)).setChecked(false);
            
            ((Chip) fromUnitChipGroup.getChildAt(toIndex)).setChecked(true);
            ((Chip) toUnitChipGroup.getChildAt(fromIndex)).setChecked(true);
            
            Snackbar.make(swapUnitsButton, "Units swapped!", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void performLengthConversion() {
        String userInput = inputValueField.getText().toString().trim();
        
        if (TextUtils.isEmpty(userInput)) {
            inputValueLayout.setError(getString(R.string.error_empty_value));
            return;
        }

        try {
            double inputValue = Double.parseDouble(userInput);
            inputValueLayout.setError(null);
            
            double convertedValue = performUnitConversion(inputValue, selectedFromUnit, selectedToUnit);
            displayConversionResult(convertedValue, selectedToUnit);

        } catch (NumberFormatException e) {
            inputValueLayout.setError(getString(R.string.error_invalid_number));
        }
    }

    private void performRealTimeConversion() {
        String userInput = inputValueField.getText().toString().trim();
        if (!TextUtils.isEmpty(userInput)) {
            try {
                double inputValue = Double.parseDouble(userInput);
                double convertedValue = performUnitConversion(inputValue, selectedFromUnit, selectedToUnit);
                displayConversionResult(convertedValue, selectedToUnit);
            } catch (NumberFormatException e) {
                // Ignore for real-time conversion
            }
        }
    }

    private double performUnitConversion(double value, LengthUnit fromUnit, LengthUnit toUnit) {
        // Convert to metres first, then to target unit
        double valueInMetres = value / fromUnit.getConversionFactor();
        return valueInMetres * toUnit.getConversionFactor();
    }

    private void displayConversionResult(double result, LengthUnit unit) {
        String formattedResult = String.format("%.6f %s (%s)", 
            result, unit.getName(), unit.getSymbol());
        conversionResultDisplay.setText(formattedResult);
        conversionResultDisplay.setTextColor(getColor(R.color.md_theme_light_primary));
    }

    // Inner class for better unit management
    private static class LengthUnit {
        private final String name;
        private final String symbol;
        private final double conversionFactor; // Factor to convert to metres

        public LengthUnit(String name, String symbol, double conversionFactor) {
            this.name = name;
            this.symbol = symbol;
            this.conversionFactor = conversionFactor;
        }

        public String getName() { return name; }
        public String getSymbol() { return symbol; }
        public double getConversionFactor() { return conversionFactor; }
    }
}
