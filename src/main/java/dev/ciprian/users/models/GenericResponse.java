package dev.ciprian.users.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GenericResponse {

    @JsonIgnore
    private boolean valid;
    private final List<String> errorMessages;

    public GenericResponse(boolean valid) {
        this.valid = valid;
        this.errorMessages = new ArrayList<>();
    }

    public GenericResponse(boolean valid, String message) {
        this(valid);
        this.errorMessages.add(message);
    }

    public boolean isValid() {
        return valid;
    }

    @JsonIgnore
    public boolean isNotValid() {
        return !isValid();
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<String> getErrorMessages() {
        return Collections.unmodifiableList(errorMessages);
    }

    public void addErrorMessage(@NonNull String message) {
        errorMessages.add(message);
    }

    public void addErrorMessages(@NonNull List<String> messages) {
        errorMessages.addAll(messages);
    }
}
