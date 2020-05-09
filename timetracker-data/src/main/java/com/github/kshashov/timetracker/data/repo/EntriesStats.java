package com.github.kshashov.timetracker.data.repo;

//    @Getter
//    @Setter
//    @NoArgsConstructor
//    @AllArgsConstructor
public class EntriesStats {
    private String project;
    private double hours;

    public EntriesStats(String project, double hours) {
        this.project = project;
        this.hours = hours;
    }

    public String getProject() {
        return project;
    }

    public double getHours() {
        return hours;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public void setHours(double hours) {
        this.hours = hours;
    }
}
