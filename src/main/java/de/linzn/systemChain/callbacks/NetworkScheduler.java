/*
 * Copyright (c) 2025 MirraNET, Niklas Linz. All rights reserved.
 *
 * This file is part of the MirraNET project and is licensed under the
 * GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You may use, distribute and modify this code under the terms
 * of the LGPLv3 license. You should have received a copy of the
 * license along with this file. If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>
 * or contact: niklas.linz@mirranet.de
 */


package de.linzn.systemChain.callbacks;

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.stem.taskManagment.AbstractCallback;
import de.linzn.stem.taskManagment.CallbackTime;
import de.linzn.stem.taskManagment.operations.OperationOutput;
import de.linzn.stem.taskManagment.operations.defaultOperations.ShellOperation;
import de.linzn.systemChain.SystemChainPlugin;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NetworkScheduler extends AbstractCallback {

    private static volatile float lastPing = -1;
    private final FileConfiguration fileConfiguration;

    public NetworkScheduler() {
        fileConfiguration = YamlConfiguration.loadConfiguration(new File(SystemChainPlugin.systemChainPlugin.getDataFolder(), "networkScheduler.yml"));
        fileConfiguration.get("hostname", "test");
        fileConfiguration.get("username", "test");
        fileConfiguration.get("port", 22);
        fileConfiguration.get("command", "ssh test");
        fileConfiguration.save();
    }

    public static float getLastPing() {
        return lastPing;
    }

    /**
     * Parse the average round trip time from the ping summary line,
     * e.g. "rtt min/avg/max/mdev = 1.234/2.345/3.456/0.123 ms".
     *
     * @return average in ms or -1 if the line can not be parsed
     */
    static float parseAveragePing(String line) {
        if (line == null) {
            return -1;
        }
        int equals = line.indexOf('=');
        String values = (equals >= 0 ? line.substring(equals + 1) : line).replace("ms", "").trim();
        String[] parts = values.split("/");
        if (parts.length < 2) {
            return -1;
        }
        try {
            return Float.parseFloat(parts[1].replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public void operation() {
        ShellOperation shellOperation = new ShellOperation();
        shellOperation.setUseSSH(true);
        shellOperation.setSshUser(fileConfiguration.getString("username"));
        shellOperation.setSshHost(fileConfiguration.getString("hostname"));
        shellOperation.setSshPort(fileConfiguration.getInt("port"));
        shellOperation.setScriptCommand(fileConfiguration.getString("command"));
        shellOperation.setUseOutput(true);
        addOperationData(shellOperation);
    }

    @Override
    public void callback(OperationOutput operationOutput) {
        if (operationOutput.getExit() != 0 || !(operationOutput.getData() instanceof List<?> list) || list.isEmpty()) {
            lastPing = -1;
            return;
        }
        lastPing = parseAveragePing(String.valueOf(list.get(0)));
        SystemChainPlugin.systemChainPlugin.getPluginLogger().debug("Network state " + lastPing + " ms");
    }

    @Override
    public CallbackTime getTime() {
        return new CallbackTime(5, 10, TimeUnit.SECONDS);
    }
}
