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
import de.linzn.stem.STEMApp;
import de.linzn.stem.taskManagment.AbstractCallback;
import de.linzn.stem.taskManagment.CallbackTime;
import de.linzn.stem.taskManagment.operations.OperationOutput;
import de.linzn.stem.taskManagment.operations.defaultOperations.ShellOperation;
import de.linzn.systemChain.SystemChainPlugin;


import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NetworkScheduler extends AbstractCallback {

    private static float lastPing = -1;
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

    @Override
    public void operation() {
        String command = fileConfiguration.getString("command");
        String username = fileConfiguration.getString("username");
        String hostname = fileConfiguration.getString("hostname");
        int port = fileConfiguration.getInt("port");

        ShellOperation shellOperation = new ShellOperation();

        shellOperation.setUseSSH(true);
        shellOperation.setSshUser(username);
        shellOperation.setSshHost(hostname);
        shellOperation.setSshPort(port);

        shellOperation.setScriptCommand(command);
        shellOperation.setUseOutput(true);

        addOperationData(shellOperation);
    }

    @Override
    public void callback(OperationOutput operationOutput) {
        List<String> list = (List<String>) operationOutput.getData();

        if (operationOutput.getExit() != 0) {
            lastPing = -1;
        } else {
            String line = list.get(0).substring(21).replace("ms", "");
            String[] pingArray = line.split("/");

            lastPing = getFloat(pingArray[1]);
            STEMApp.LOGGER.DEBUG("Network state " + lastPing + " ms");
        }

    }

    @Override
    public CallbackTime getTime() {
        return new CallbackTime(5, 10, TimeUnit.SECONDS);
    }

    private float getFloat(String line) {
        return Float.valueOf(line.replaceAll("[^\\d.]", ""));
    }
}
