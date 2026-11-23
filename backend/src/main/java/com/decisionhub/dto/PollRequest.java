package com.decisionhub.dto;

import java.util.List;

public class PollRequest {

    private String title;
    private String description;
    private String pollType;
    private String duration;
    private List<String> options;

    public PollRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPollType() { return pollType; }
    public void setPollType(String pollType) { this.pollType = pollType; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
}
