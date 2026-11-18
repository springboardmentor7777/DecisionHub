package com.decisionhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class OptionRequest {

    @NotBlank(message = "Option text is required")
    @Size(max = 255, message = "Option text must be at most 255 characters")
    private String optionText;

    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }
}
