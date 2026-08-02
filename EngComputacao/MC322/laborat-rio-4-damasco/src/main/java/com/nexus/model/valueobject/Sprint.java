package com.nexus.model.valueobject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class Sprint {
    private final String name;
    private final DateRange period;
    private final Set<String> taskIds;

    public Sprint(String name, DateRange period) {
        this(name, period, new HashSet<>());
    }

    private Sprint(String name, DateRange period, Set<String> taskIds) {
        this.name = name;
        this.period = period;
        this.taskIds = taskIds;
    }

    public Sprint withTaskAdded(String taskId) {
        Set<String> newTaskIds = new HashSet<>(this.taskIds);
        newTaskIds.add(taskId);
        return new Sprint(this.name, this.period, newTaskIds);
    }

    public String getName() {
        return name;
    }

    public DateRange getPeriod() {
        return period;
    }

    public Set<String> getTaskIds() {
        return Collections.unmodifiableSet(taskIds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return Objects.equals(name, ((Sprint) o).name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}