package org.example;

import java.util.List;
import java.util.Objects;

public class Commit {
    private String type;
    private String commitMessage;

    public Commit () {}

    public Commit(String type, String commitMessage) {
        this.type = type;
        this.commitMessage = commitMessage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, commitMessage);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Commit) {
            Commit commit = (Commit) object;

            return commit.commitMessage.equals(this.commitMessage);
        } else {
            return false;
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCommitMessages() {
        return commitMessage;
    }

    public void setCommitMessages(String commitMessage) {
        this.commitMessage = commitMessage;
    }
}
