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
import de.azcore.azcoreRuntime.modules.notificationModule.NotificationContainer;
import de.azcore.azcoreRuntime.modules.notificationModule.NotificationPriority;
import de.azcore.azcoreRuntime.taskManagment.AbstractCallback;
import de.azcore.azcoreRuntime.taskManagment.CallbackTime;
import de.azcore.azcoreRuntime.taskManagment.operations.OperationRegister;
import de.azcore.azcoreRuntime.taskManagment.operations.TaskOperation;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TemperatureScheduler extends AbstractCallback {

    private int[] heat = {70, 80, 90};
    private int last = 0;


    @Override
    public void methodToCall() {
        DataContainer dataContainer = AZCoreRuntimeApp.getInstance().getDatabaseModule().getData("shell_command_temperature");
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
        this.addOperationData(taskOperation, jsonObject);
    }

    @Override
    public void callback(Object object) {
        JSONObject jsonObject = (JSONObject) object;
        JSONArray jsonArray = jsonObject.getJSONArray("output");
        ArrayList<Float> floatList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            floatList.add(getFloat(jsonArray.getString(i)));
        }
        double hotCore = -1;
        String notificationString = null;
        for (Float aFloatList : floatList) {
            double value = aFloatList;
            if (hotCore < value) {
                hotCore = value;
            }
        }
        if (hotCore >= heat[0]) {

            if (hotCore >= heat[1]) {
                if (hotCore >= heat[2]) {
                    if (last < 3) {
                        last = 3;
                        notificationString = "Die Temperatur des Hostsystems liegt mit " + hotCore + "°C im kritischen Bereich!";
                    }
                } else {
                    if (last < 2) {
                        last = 2;
                        notificationString = "Die Temperatur des Prozessors ist gefährlich heiß. " + hotCore + "°C";
                    }
                }
            } else {
                if (last < 1) {
                    last = 1;
                    notificationString = "Der Prozessor des Hostsystems ist mit " + hotCore + "°C ungewöhnlich heiß.";
                }
            }
        } else {

            if (last > 0) {
                last = 0;
                notificationString = "Die Temperatur des Prozessors ist wieder normal. " + hotCore + "°C";
            }
        }
        if (notificationString != null) {
            NotificationContainer notificationContainer = new NotificationContainer(notificationString, NotificationPriority.ASAP);
            AZCoreRuntimeApp.getInstance().getNotificationModule().pushNotification(notificationContainer);
        }
    }

    @Override
    public CallbackTime getTime() {
        return new CallbackTime(10, 20, TimeUnit.SECONDS);
    }


    private float getFloat(String line) {
        return Float.valueOf(line.replaceAll("[^\\d.]", ""));
    }

}
