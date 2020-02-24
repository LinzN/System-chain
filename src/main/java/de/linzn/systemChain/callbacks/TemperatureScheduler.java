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

package de.linzn.systemChain.callbacks;


import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.azcore.azcoreRuntime.AppLogger;
import de.azcore.azcoreRuntime.modules.notificationModule.NotificationContainer;
import de.azcore.azcoreRuntime.modules.notificationModule.NotificationPriority;
import de.azcore.azcoreRuntime.taskManagment.AbstractCallback;
import de.azcore.azcoreRuntime.taskManagment.CallbackTime;
import de.azcore.azcoreRuntime.taskManagment.operations.OperationRegister;
import de.azcore.azcoreRuntime.taskManagment.operations.OperationSettings;
import de.azcore.azcoreRuntime.taskManagment.operations.TaskOperation;
import de.azcore.azcoreRuntime.utils.Color;
import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.systemChain.SystemChainPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TemperatureScheduler extends AbstractCallback {

    private FileConfiguration fileConfiguration;
    private int[] heat = {70, 80, 90};
    private int last = 0;


    public TemperatureScheduler() {
        fileConfiguration = YamlConfiguration.loadConfiguration(new File(SystemChainPlugin.systemChainPlugin.getDataFolder(), "temperatureScheduler.yml"));
        fileConfiguration.get("hostname", "test");
        fileConfiguration.get("username", "test");
        fileConfiguration.get("port", 22);
        fileConfiguration.get("command", "ssh test");
        fileConfiguration.save();
    }


    @Override
    public void operation() {
        String command = fileConfiguration.getString("command");
        String username = fileConfiguration.getString("username");
        String hostname = fileConfiguration.getString("hostname");
        int port = fileConfiguration.getInt("port");

        TaskOperation taskOperation = OperationRegister.getOperation("run_linux_shell");
        OperationSettings operationSettings = new OperationSettings();

        operationSettings.addSetting("ssh.use", true);
        operationSettings.addSetting("ssh.user", username);
        operationSettings.addSetting("ssh.host", hostname);
        operationSettings.addSetting("ssh.port", port);

        operationSettings.addSetting("command.script", command);

        operationSettings.addSetting("output.use", true);

        this.addOperationData(taskOperation, operationSettings);
    }

    @Override
    public void callback(Object object) {
        OperationSettings operationSettings = (OperationSettings) object;
        List<String> list = (List<String>) operationSettings.getSetting("output");
        ArrayList<Float> floatList = new ArrayList<>();
        for (String s : list) {
            floatList.add(getFloat(s));
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

        AppLogger.debug(Color.GREEN + "Core temperatures " + floatList.toString());
    }

    @Override
    public CallbackTime getTime() {
        return new CallbackTime(10, 20, TimeUnit.SECONDS);
    }


    private float getFloat(String line) {
        return Float.valueOf(line.replaceAll("[^\\d.]", ""));
    }

}
