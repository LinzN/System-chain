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

package de.linzn.systemChain.runnables;


import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.azcore.azcoreRuntime.modules.databaseModule.DataContainer;
import de.azcore.azcoreRuntime.taskManagment.AbstractCallback;
import de.azcore.azcoreRuntime.taskManagment.CallbackTime;
import de.azcore.azcoreRuntime.taskManagment.operations.OperationRegister;
import de.azcore.azcoreRuntime.taskManagment.operations.TaskOperation;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class NetworkScheduler extends AbstractCallback {

    private static float lastPing = -1;

    public static float getLastPing() {
        return lastPing;
    }

    @Override
    public void operation() {
        DataContainer dataContainer = AZCoreRuntimeApp.getInstance().getDatabaseModule().getData("shell_command_network");
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
        addOperationData(taskOperation, jsonObject);
    }

    @Override
    public void callback(Object object) {
        JSONObject jsonObject = (JSONObject) object;
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
    public CallbackTime getTime() {
        return new CallbackTime(5, 10, TimeUnit.SECONDS);
    }

    private float getFloat(String line) {
        return Float.valueOf(line.replaceAll("[^\\d.]", ""));
    }
}
