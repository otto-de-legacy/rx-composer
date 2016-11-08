package de.otto.edison.aggregator;

import javax.ws.rs.core.Response;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

public final class Content {

    public enum Status {
        OK,
        EMPTY,
        ERROR
    }

    private final Position position;
    private final String content;
    private final LocalDateTime timestamp = now();
    private final Status status;

    public Content(final Position position, final Response response) {
        this.position = position;
        this.content = response.readEntity(String.class);
        status = response.getStatus() > 399
                ? Status.ERROR
                : content == null || content.isEmpty() ? Status.EMPTY : Status.OK;
        System.out.println("Created (" + position + "): " + timestamp.toString());
    }

    public Position getPosition() {
        return position;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreated() {
        return timestamp;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Content content1 = (Content) o;

        if (position != null ? !position.equals(content1.position) : content1.position != null) return false;
        if (content != null ? !content.equals(content1.content) : content1.content != null) return false;
        return timestamp != null ? timestamp.equals(content1.timestamp) : content1.timestamp == null;

    }

    @Override
    public int hashCode() {
        int result = position != null ? position.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Content{" +
                "stepId='" + position + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

    public Status status() {
        return this.status;
    }
}
