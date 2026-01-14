package aeron.concurrent;

import aeron.concurrent.RequestType;
import aeron.model.Airplane;

public class Request {
    public Airplane plane;
    public RequestType type;

    public Request(Airplane plane, RequestType type) {
        this.plane = plane;
        this.type = type;
    }
    @Override
    public String toString() { return plane.getId(); }
}