package com.nexus.model;

import java.util.Objects;

/**
 * Class responsible for represent a given status in a workflow.
 */
public class Status {
    private final String name;
    private final boolean isTerminal;

    /**
     * Status contructor.
     * 
     * @param name
     * @param isTerminal
     */
    public Status(String name, boolean isTerminal) {
        String cleanName = name.strip().toLowerCase();
        this.name = cleanName;
        this.isTerminal = isTerminal;
    }

    /**
     * Obtem o nome do Status
     * 
     * @return um {@link String} que e o nome do status
     */
    public String getName() {
        return name;
    }

    /**
     * Retorna um boolean se e um Status terminal
     * 
     * @return True se for terminal, False se nao for
     */
    public boolean isTerminal() {
        return isTerminal;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Status s = (Status) o;
        boolean namesAreEqual = this.name.equals(s.name);
        return namesAreEqual;
    }

    @Override
    public int hashCode() {
        int hash = Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public String toString() {
        return "Status(name=" + this.name + ",  isTerminal=" + this.isTerminal + ")";
    }
}
