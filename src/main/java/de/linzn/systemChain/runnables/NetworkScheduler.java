/*
 * Copyright (C) 2018. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 */

package de.linzn.systemChain.runnables;


import de.linzn.evy.EvyApp;
import de.linzn.evy.api.OperationRegister;
import de.linzn.evy.internal.containers.DataContainer;
import de.linzn.evy.internal.containers.TaskContainer;
import de.linzn.evy.internal.containers.TaskOperation;
import de.linzn.evy.internal.containers.TimeData;
import de.linzn.evy.plugin.runnables.data.Runner;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class NetworkScheduler extends Runner {

    private static float lastPing = -1;

    public static float getLastPing() {
        return lastPing;
    }

    @Override
    public void schedule() {
        DataContainer dataContainer = EvyApp.getInstance().getDatabaseModule().getData("shell_command_network");
        String command = dataContainer.getJSON().getString("command");
        String username = dataContainer.getJSON().getString("user");
        String hostname = dataContainer.getJSON().getString("host");
        int port = dataContainer.getJSON().getInt("port");

        TaskOperation taskOperation = OperationRegister.getOperation("run_linux_shell");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("useOutput", true);
        JSONObject sshObject = new JSONObject();
        jsonObject.put("ssh", sshObject);
        sshObject.put("useSSH", true);
        sshObject.put("user", username);
        sshObject.put("host", hostname);
        sshObject.put("port", port);
        JSONObject commandObject = new JSONObject();
        jsonObject.put("command", commandObject);
        commandObject.put("isScript", false);
        commandObject.put("script", command);
        TaskContainer taskContainer = new TaskContainer(taskOperation, jsonObject);
        addOperation(taskContainer);
    }

    @Override
    public void loopback(JSONObject jsonObject) {
        JSONArray jsonArray = jsonObject.getJSONArray("output");
        if (jsonObject.getInt("exitcode") != 0) {
            lastPing = -1;
        } else {
            String line = jsonArray.getString(0).substring(21).replace("ms", "");
            String[] pingArray = line.split("/");

            lastPing = getFloat(pingArray[1]);
        }

    }

    @Override
    public TimeData runnableTimer() {
        return new TimeData(5, 10, TimeUnit.SECONDS);
    }

    private float getFloat(String line) {
        return Float.valueOf(line.replaceAll("[^\\d.]", ""));
    }
}
