/*
 * Copyright (C) 2020. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.linzn.systemChain.events;

import de.stem.stemSystem.modules.eventModule.StemEvent;

public class SystemUpdateEvent implements StemEvent {
    private final int exitCode;
    private final String hostname;
    private final int port;

    public SystemUpdateEvent(int exitCode, String hostname, int port) {
        this.exitCode = exitCode;
        this.hostname = hostname;
        this.port = port;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }
}
