package com.tcto.rpg.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    private int filesChecked;
    private final List<ValidationError> warnings = new ArrayList<>();
    private final List<ValidationError> errors = new ArrayList<>();

    public void checkedFile() {
        filesChecked++;
    }

    public void warn(String file, String message) {
        warnings.add(new ValidationError(ValidationError.Severity.WARN, file, message));
    }

    public void error(String file, String message) {
        errors.add(new ValidationError(ValidationError.Severity.ERROR, file, message));
    }

    public int filesChecked() {
        return filesChecked;
    }

    public List<ValidationError> warnings() {
        return warnings;
    }

    public List<ValidationError> errors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public String summary() {
        int ok = Math.max(0, filesChecked - warnings.size() - errors.size());
        return "[TCToRPG Validator] files=" + filesChecked + ", ok=" + ok
            + ", warnings=" + warnings.size() + ", errors=" + errors.size();
    }
}
